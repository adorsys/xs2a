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
import de.adorsys.aspsp.xs2a.domain.ErrorHolder;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.TppInfo;
import de.adorsys.aspsp.xs2a.domain.Xs2aTransactionStatus;
import de.adorsys.aspsp.xs2a.domain.consent.Xs2aPisConsent;
import de.adorsys.aspsp.xs2a.domain.pis.*;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.consent.PisConsentDataService;
import de.adorsys.aspsp.xs2a.service.consent.PisPsuDataService;
import de.adorsys.aspsp.xs2a.service.consent.Xs2aPisConsentService;
import de.adorsys.aspsp.xs2a.service.mapper.consent.Xs2aPisConsentMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aTransactionalStatusMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.aspsp.xs2a.service.payment.*;
import de.adorsys.aspsp.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.profile.PaymentProduct;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiBulkPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.BulkPaymentSpi;
import de.adorsys.psd2.xs2a.spi.service.PeriodicPaymentSpi;
import de.adorsys.psd2.xs2a.spi.service.SinglePaymentSpi;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static de.adorsys.aspsp.xs2a.domain.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.core.profile.PaymentType.PERIODIC;
import static de.adorsys.psd2.xs2a.core.profile.PaymentType.SINGLE;

@Slf4j
@Service
@AllArgsConstructor
public class PaymentService {
    private final ReadPaymentFactory readPaymentFactory;
    private final Xs2aPisConsentService pisConsentService;
    private final PisConsentDataService pisConsentDataService;
    private final PisPsuDataService pisPsuDataService;
    private final TppService tppService;
    private final CreateSinglePaymentService createSinglePaymentService;
    private final CreatePeriodicPaymentService createPeriodicPaymentService;
    private final CreateBulkPaymentService createBulkPaymentService;
    private final Xs2aPisConsentMapper xs2aPisConsentMapper;
    private final Xs2aToSpiPsuDataMapper psuDataMapper;
    private final SinglePaymentSpi singlePaymentSpi;
    private final PeriodicPaymentSpi periodicPaymentSpi;
    private final BulkPaymentSpi bulkPaymentSpi;
    private final SpiToXs2aTransactionalStatusMapper spiToXs2aTransactionalStatus;
    private final AspspProfileServiceWrapper profileService;
    private final CancelPaymentService cancelPaymentService;
    private final SpiErrorMapper spiErrorMapper;

    /**
     * Initiates a payment though "payment service" corresponding service method
     *
     * @param payment Payment information
     * @return Response containing information about created payment or corresponding error
     */
    public ResponseObject createPayment(Object payment, PaymentInitiationParameters paymentInitiationParameters) {
        TppInfo tppInfo = tppService.getTppInfo();
        tppInfo.setRedirectUri(paymentInitiationParameters.getTppRedirectUri());
        tppInfo.setNokRedirectUri(paymentInitiationParameters.getTppNokRedirectUri());
        Xs2aPisConsent pisConsent = xs2aPisConsentMapper.mapToXs2aPisConsent(pisConsentService.createPisConsent(paymentInitiationParameters, tppInfo), paymentInitiationParameters.getPsuData());
        if (StringUtils.isBlank(pisConsent.getConsentId())) {
            return ResponseObject.builder()
                       .fail(new MessageError(CONSENT_UNKNOWN_400))
                       .build();
        }

        if (paymentInitiationParameters.getPaymentType() == SINGLE) {
            return createSinglePaymentService.createPayment((SinglePayment) payment, paymentInitiationParameters, tppInfo, pisConsent);
        } else if (paymentInitiationParameters.getPaymentType() == PERIODIC) {
            return createPeriodicPaymentService.createPayment((PeriodicPayment) payment, paymentInitiationParameters, tppInfo, pisConsent);
        } else {
            return createBulkPaymentService.createPayment((BulkPayment) payment, paymentInitiationParameters, tppInfo, pisConsent);
        }
    }

    /**
     * Retrieves payment from ASPSP by its ASPSP identifier, product and payment type
     *
     * @param paymentType type of payment (payments, bulk-payments, periodic-payments)
     * @param paymentId   ASPSP identifier of the payment
     * @return Response containing information about payment or corresponding error
     */
    public ResponseObject getPaymentById(PaymentType paymentType, String paymentId) {
        PsuIdData psuData = pisPsuDataService.getPsuDataByPaymentId(paymentId);

        ReadPaymentService<PaymentInformationResponse> readPaymentService = readPaymentFactory.getService(paymentType.getValue());
        PaymentInformationResponse response = readPaymentService.getPayment(paymentId, PaymentProduct.SEPA, psuData); //NOT USED IN 1.2 //TODO clarify why here Payment product is hardcoded and what should be done instead https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/332

        if (response.hasError()) {
            return ResponseObject.builder()
                       .fail(new MessageError(response.getErrorHolder().getErrorCode(), response.getErrorHolder().getMessage()))
                       .build();
        }
        return ResponseObject.builder()
                   .body(response.getPayment())
                   .build();
    }

    /**
     * Retrieves payment status from ASPSP
     *
     * @param paymentType The addressed payment category Single, Periodic or Bulk
     * @param paymentId   String representation of payment primary ASPSP identifier
     * @return Information about the status of a payment
     */
    public ResponseObject<Xs2aTransactionStatus> getPaymentStatusById(PaymentType paymentType, String paymentId) {
        AspspConsentData aspspConsentData = pisConsentDataService.getAspspConsentDataByPaymentId(paymentId);
        PsuIdData psuData = pisPsuDataService.getPsuDataByPaymentId(paymentId);

        SpiPsuData spiPsuData = psuDataMapper.mapToSpiPsuData(psuData);

        SpiResponse<SpiTransactionStatus> spiResponse;
        if (paymentType == SINGLE) {
            SpiSinglePayment payment = new SpiSinglePayment(null);
            payment.setPaymentId(paymentId);
            spiResponse = singlePaymentSpi.getPaymentStatusById(spiPsuData, payment, aspspConsentData);
        } else if (paymentType == PERIODIC) {
            SpiPeriodicPayment payment = new SpiPeriodicPayment(null);
            payment.setPaymentId(paymentId);
            spiResponse = periodicPaymentSpi.getPaymentStatusById(spiPsuData, payment, aspspConsentData);
        } else {
            SpiBulkPayment payment = new SpiBulkPayment();
            payment.setPaymentId(paymentId);
            spiResponse = bulkPaymentSpi.getPaymentStatusById(spiPsuData, payment, aspspConsentData);
        }
        pisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse);
            return ResponseObject.<Xs2aTransactionStatus>builder()
                       .fail(new MessageError(errorHolder.getErrorCode(),  errorHolder.getMessage()))
                       .build();
        }

        Xs2aTransactionStatus transactionStatus = spiToXs2aTransactionalStatus.mapToTransactionStatus(spiResponse.getPayload());
        return Optional.ofNullable(transactionStatus)
                   .map(tr -> ResponseObject.<Xs2aTransactionStatus>builder().body(tr).build())
                   .orElseGet(ResponseObject.<Xs2aTransactionStatus>builder()
                                  .fail(new MessageError(RESOURCE_UNKNOWN_403))
                                  ::build);
    }

    /**
     * Cancels payment by its ASPSP identifier and payment type
     *
     * @param paymentType type of payment (payments, bulk-payments, periodic-payments)
     * @param paymentId   ASPSP identifier of the payment
     * @return Response containing information about cancelled payment or corresponding error
     */
    public ResponseObject<CancelPaymentResponse> cancelPayment(PaymentType paymentType, String paymentId) {
        SpiPayment payment;
        switch (paymentType) {
            case SINGLE:
                SpiSinglePayment singlePayment = new SpiSinglePayment(null);
                singlePayment.setPaymentId(paymentId);
                payment = singlePayment;
                break;
            case PERIODIC:
                SpiPeriodicPayment periodicPayment = new SpiPeriodicPayment(null);
                periodicPayment.setPaymentId(paymentId);
                payment = periodicPayment;
                break;
            case BULK:
                SpiBulkPayment bulkPayment = new SpiBulkPayment();
                bulkPayment.setPaymentId(paymentId);
                payment = bulkPayment;
                break;
            default:
                log.error("Unknown payment type: {}", paymentType);
                return ResponseObject.<CancelPaymentResponse>builder()
                           .fail(new MessageError(FORMAT_ERROR))
                           .build();
        }

        AspspConsentData consentData = pisConsentDataService.getAspspConsentDataByPaymentId(paymentId);
        PsuIdData psuData = pisPsuDataService.getPsuDataByPaymentId(paymentId);
        SpiPsuData spiPsuData = psuDataMapper.mapToSpiPsuData(psuData);

        if (profileService.isPaymentCancellationAuthorizationMandated()) {
            return cancelPaymentService.initiatePaymentCancellation(spiPsuData, payment, consentData);
        } else {
            return cancelPaymentService.cancelPaymentWithoutAuthorisation(spiPsuData, payment, consentData);
        }
    }
}
