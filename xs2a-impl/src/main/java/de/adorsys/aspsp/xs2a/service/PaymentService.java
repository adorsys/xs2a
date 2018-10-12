/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.config.factory.ReadPaymentFactory;
import de.adorsys.aspsp.xs2a.domain.MessageErrorCode;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.TppInfo;
import de.adorsys.aspsp.xs2a.domain.Xs2aTransactionStatus;
import de.adorsys.aspsp.xs2a.domain.pis.*;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.authorization.pis.CreateSinglePaymentService;
import de.adorsys.aspsp.xs2a.service.consent.PisConsentDataService;
import de.adorsys.aspsp.xs2a.service.consent.PisConsentService;
import de.adorsys.aspsp.xs2a.service.mapper.PaymentMapper;
import de.adorsys.aspsp.xs2a.service.payment.ReadPayment;
import de.adorsys.aspsp.xs2a.service.payment.ScaPaymentService;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.aspsp.xs2a.domain.MessageErrorCode.*;
import static de.adorsys.aspsp.xs2a.domain.Xs2aTransactionStatus.RJCT;
import static de.adorsys.aspsp.xs2a.domain.pis.PaymentType.*;

@Slf4j
@Service
@AllArgsConstructor
public class PaymentService {
    private final PaymentSpi paymentSpi;
    private final PaymentMapper paymentMapper;
    private final ScaPaymentService scaPaymentService;
    private final ReadPaymentFactory readPaymentFactory;
    private final AccountReferenceValidationService referenceValidationService;
    private final PisConsentService pisConsentService;
    private final PisConsentDataService pisConsentDataService;
    private final TppService tppService;
    private final CreateSinglePaymentService createSinglePaymentService;

    /**
     * Initiates a payment though "payment service" corresponding service method
     *
     * @param payment Payment information
     * @return Response containing information about created payment or corresponding error
     */
    public ResponseObject createPayment(Object payment, PaymentRequestParameters requestParameters) {
        ResponseObject response;
        TppInfo tppInfo = tppService.getTppInfo();
        tppInfo.setRedirectUri(requestParameters.getTppRedirectUri());
        tppInfo.setNokRedirectUri(requestParameters.getTppNokRedirectUri());

        if (requestParameters.getPaymentType() == SINGLE) {
            String consentId = pisConsentService.createPisConsentV2(requestParameters);
            if (StringUtils.isBlank(consentId)) {
                return ResponseObject.builder()
                           .fail(new MessageError(CONSENT_UNKNOWN_400))
                           .build();
            }
            return createSinglePaymentService.createPayment((SinglePayment) payment, requestParameters.getPaymentProduct(), requestParameters.isTppExplicitAuthorisationPreferred(), consentId, tppInfo);
        } else if (requestParameters.getPaymentType() == PERIODIC) {
            response = initiatePeriodicPayment((PeriodicPayment) payment, tppInfo, requestParameters.getPaymentProduct().getCode());
        } else {
            response = createBulkPayments((BulkPayment) payment, tppInfo, requestParameters.getPaymentProduct().getCode());
        }
        if (!response.hasError() && paymentHasNoTppMessages(response, requestParameters.getPaymentType())) {//TODO Refactor this https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/332
            response = pisConsentService.createPisConsent(payment, response.getBody(), requestParameters, tppInfo);
            getAspspConsentDataFromResponseObject(response, requestParameters.getPaymentType())
                .ifPresent(pisConsentDataService::updateAspspConsentData);

        }
        return response;
    }

    /**
     * Retrieves payment status from ASPSP
     *
     * @param paymentId   String representation of payment primary ASPSP identifier
     * @param paymentType The addressed payment category Single, Periodic or Bulk
     * @return Information about the status of a payment
     */
    public ResponseObject<Xs2aTransactionStatus> getPaymentStatusById(String paymentId, PaymentType paymentType) {
        SpiResponse<SpiTransactionStatus> spiResponse = paymentSpi.getPaymentStatusById(paymentId, paymentMapper.mapToSpiPaymentType(paymentType), pisConsentDataService.getAspspConsentDataByPaymentId(paymentId));
        pisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());
        Xs2aTransactionStatus transactionStatus = paymentMapper.mapToTransactionStatus(spiResponse.getPayload());
        return Optional.ofNullable(transactionStatus)
                   .map(tr -> ResponseObject.<Xs2aTransactionStatus>builder().body(tr).build())
                   .orElseGet(ResponseObject.<Xs2aTransactionStatus>builder()
                                  .fail(new MessageError(RESOURCE_UNKNOWN_403))
                                  ::build);
    }

    /**
     * Initiates periodic payment
     *
     * @param periodicPayment Periodic payment information
     * @param paymentProduct  The addressed payment product
     * @return Response containing information about created periodic payment or corresponding error
     */
    public ResponseObject<PaymentInitialisationResponse> initiatePeriodicPayment(PeriodicPayment periodicPayment, TppInfo tppInfo, String paymentProduct) {
        return validatePayment(periodicPayment, periodicPayment.areValidExecutionAndPeriodDates())
                   .map(e -> ResponseObject.<PaymentInitialisationResponse>builder()
                                 .body(paymentMapper.mapToPaymentInitResponseFailedPayment(periodicPayment, e))
                                 .build())
                   .orElseGet(ResponseObject.<PaymentInitialisationResponse>builder()
                                  .body(scaPaymentService.createPeriodicPayment(periodicPayment, tppInfo, paymentProduct))
                                  ::build);
    }

    /**
     * Initiates a bulk payment
     *
     * @param bulkPayment    BulkPayment information
     * @param paymentProduct The addressed payment product
     * @return List of payment initiation responses containing information about created payments or an error if non of the payments could pass the validation
     */
    public ResponseObject<List<PaymentInitialisationResponse>> createBulkPayments(BulkPayment bulkPayment, TppInfo tppInfo, String paymentProduct) {
        if (bulkPayment == null || CollectionUtils.isEmpty(bulkPayment.getPayments())) {
            return ResponseObject.<List<PaymentInitialisationResponse>>builder()
                       .fail(new MessageError(FORMAT_ERROR))
                       .build();
        }
        List<SinglePayment> validPayments = new ArrayList<>();
        List<PaymentInitialisationResponse> invalidPayments = new ArrayList<>();
        for (SinglePayment payment : bulkPayment.getPayments()) {
            payment.setDebtorAccount(bulkPayment.getDebtorAccount());
            validatePayment(payment, payment.isValidExecutionDateAndTime())
                .map(e -> invalidPayments.add(paymentMapper.mapToPaymentInitResponseFailedPayment(payment, e)))
                .orElseGet(() -> validPayments.add(payment));
        }
        bulkPayment.setPayments(validPayments);
        return processValidPayments(tppInfo, paymentProduct, invalidPayments, bulkPayment);
    }

    /**
     * Retrieves payment from ASPSP by its ASPSP identifier, product and payment type
     *
     * @param paymentType type of payment (payments, bulk-payments, periodic-payments)
     * @param paymentId   ASPSP identifier of the payment
     * @return Response containing information about payment or corresponding error
     */
    public ResponseObject<Object> getPaymentById(PaymentType paymentType, String paymentId) {
        ReadPayment service = readPaymentFactory.getService(paymentType.getValue());
        Optional<Object> payment = Optional.ofNullable(service.getPayment(paymentId, "TMP")); //NOT USED IN 1.2
        return payment.map(p -> ResponseObject.builder()
                                    .body(p)
                                    .build())
                   .orElseGet(ResponseObject.builder()
                                  .fail(new MessageError(RESOURCE_UNKNOWN_403))
                                  ::build);
    }

    private ResponseObject<List<PaymentInitialisationResponse>> processValidPayments(TppInfo tppInfo, String paymentProduct, List<PaymentInitialisationResponse> invalidPayments, BulkPayment bulkPayment) {
        if (CollectionUtils.isNotEmpty(bulkPayment.getPayments())) {
            List<PaymentInitialisationResponse> paymentResponses = scaPaymentService.createBulkPayment(bulkPayment, tppInfo, paymentProduct);
            if (paymentResponses.stream()
                    .anyMatch(pr -> pr.getTransactionStatus() != RJCT)) {
                paymentResponses.addAll(invalidPayments);
                return ResponseObject.<List<PaymentInitialisationResponse>>builder()
                           .body(paymentResponses)
                           .build();
            }
        }
        return ResponseObject.<List<PaymentInitialisationResponse>>builder()
                   .fail(new MessageError(PAYMENT_FAILED))
                   .build();
    }

    private Optional<MessageErrorCode> validatePayment(SinglePayment payment, boolean areValidDates) {
        return areValidDates
                   ? Optional.ofNullable(referenceValidationService.validateAccountReferences(payment.getAccountReferences()).getError())
                         .map(e -> e.getTppMessage().getMessageErrorCode())
                   : Optional.of(EXECUTION_DATE_INVALID);
    }

    //TODO Refactor and remove this method https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/332
    private Optional<AspspConsentData> getAspspConsentDataFromResponseObject(ResponseObject responseObject, PaymentType paymentType) {
        if (paymentType == SINGLE ||
                paymentType == PERIODIC) {

            PaymentInitialisationResponse paymentInitialisationResponse = (PaymentInitialisationResponse) responseObject.getBody();//TODO Refactor https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/332
            AspspConsentData aspspConsentData = paymentInitialisationResponse.getAspspConsentData();
            return Optional.of(new AspspConsentData(Optional.ofNullable(aspspConsentData).map(AspspConsentData::getAspspConsentData).orElse(null), paymentInitialisationResponse.getPisConsentId()));
        } else if (paymentType == BULK) {

            List<PaymentInitialisationResponse> paymentInitialisationResponseList = (List<PaymentInitialisationResponse>) responseObject.getBody();//TODO Refactor https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/332
            if (CollectionUtils.isNotEmpty(paymentInitialisationResponseList)) {
                PaymentInitialisationResponse paymentInitialisationResponse = paymentInitialisationResponseList.get(0);
                AspspConsentData aspspConsentData = paymentInitialisationResponse.getAspspConsentData();
                return Optional.of(new AspspConsentData(Optional.ofNullable(aspspConsentData).map(AspspConsentData::getAspspConsentData).orElse(null), paymentInitialisationResponse.getPisConsentId()));
            }
        }

        return Optional.empty();
    }

    //TODO remove response object casting https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/428
    private  <T> boolean paymentHasNoTppMessages(ResponseObject<T> responseObject, PaymentType paymentType) {
        switch (paymentType) {
            case SINGLE:
            case PERIODIC:
                PaymentInitialisationResponse paymentInitialisationResponse = (PaymentInitialisationResponse) responseObject.getBody();
                return paymentInitialisationResponse.getTppMessages() == null;
            case BULK:
                List<PaymentInitialisationResponse> bulkPaymentResponse = (List<PaymentInitialisationResponse>) responseObject.getBody();
                List<PaymentInitialisationResponse> responsesWithoutErrors = bulkPaymentResponse.stream()
                                                                                 .filter(r -> r.getTppMessages() == null)
                                                                                 .collect(Collectors.toList());
                return CollectionUtils.isNotEmpty(responsesWithoutErrors);
            default:
                return false;
        }
    }
}
