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
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.TppInfo;
import de.adorsys.aspsp.xs2a.domain.Xs2aTransactionStatus;
import de.adorsys.aspsp.xs2a.domain.consent.Xs2aPisConsent;
import de.adorsys.aspsp.xs2a.domain.pis.*;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.consent.PisConsentDataService;
import de.adorsys.aspsp.xs2a.service.consent.PisConsentService;
import de.adorsys.aspsp.xs2a.service.mapper.PaymentMapper;
import de.adorsys.aspsp.xs2a.service.mapper.consent.Xs2aPisConsentMapper;
import de.adorsys.aspsp.xs2a.service.payment.CreateBulkPaymentService;
import de.adorsys.aspsp.xs2a.service.payment.CreatePeriodicPaymentService;
import de.adorsys.aspsp.xs2a.service.payment.CreateSinglePaymentService;
import de.adorsys.aspsp.xs2a.service.payment.ReadPayment;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiBulkPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.BulkPaymentSpi;
import de.adorsys.psd2.xs2a.spi.service.PeriodicPaymentSpi;
import de.adorsys.psd2.xs2a.spi.service.SinglePaymentSpi;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static de.adorsys.aspsp.xs2a.domain.MessageErrorCode.CONSENT_UNKNOWN_400;
import static de.adorsys.aspsp.xs2a.domain.MessageErrorCode.RESOURCE_UNKNOWN_403;
import static de.adorsys.aspsp.xs2a.domain.pis.PaymentType.PERIODIC;
import static de.adorsys.aspsp.xs2a.domain.pis.PaymentType.SINGLE;

@Slf4j
@Service
@AllArgsConstructor
public class PaymentService {
    private final PaymentMapper paymentMapper;
    private final ReadPaymentFactory readPaymentFactory;
    private final PisConsentService pisConsentService;
    private final PisConsentDataService pisConsentDataService;
    private final TppService tppService;
    private final CreateSinglePaymentService createSinglePaymentService;
    private final CreatePeriodicPaymentService createPeriodicPaymentService;
    private final CreateBulkPaymentService createBulkPaymentService;
    private final Xs2aPisConsentMapper xs2aPisConsentMapper;
    private final SinglePaymentSpi singlePaymentSpi;
    private final PeriodicPaymentSpi periodicPaymentSpi;
    private final BulkPaymentSpi bulkPaymentSpi;

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
        Xs2aPisConsent pisConsent = xs2aPisConsentMapper.mapToXs2aPisConsent(pisConsentService.createPisConsent(paymentInitiationParameters, tppInfo));
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
    public ResponseObject<Object> getPaymentById(PaymentType paymentType, String paymentId) {
        ReadPayment service = readPaymentFactory.getService(paymentType.getValue());
        Optional<Object> payment = Optional.ofNullable(service.getPayment(paymentId, "sepa-credit-transfers")); //TODO Add actual value https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/441
        return payment.map(p -> ResponseObject.builder()
                                    .body(p)
                                    .build())
                   .orElseGet(ResponseObject.builder()
                                  .fail(new MessageError(RESOURCE_UNKNOWN_403))
                                  ::build);
    }

    /**
     * Retrieves payment status from ASPSP
     *
     * @param paymentId   String representation of payment primary ASPSP identifier
     * @param paymentType The addressed payment category Single, Periodic or Bulk
     * @return Information about the status of a payment
     */
    public ResponseObject<Xs2aTransactionStatus> getPaymentStatusById(PaymentType paymentType, String paymentId) {
        AspspConsentData aspspConsentData = pisConsentDataService.getAspspConsentDataByPaymentId(paymentId);
        SpiPsuData psuData = new SpiPsuData(null, null, null, null); // TODO get it from XS2A Interface https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/332
        SpiResponse<SpiTransactionStatus> spiResponse;
        if (paymentType == SINGLE) {
            SpiSinglePayment payment = new SpiSinglePayment(null);
            payment.setPaymentId(paymentId);
            spiResponse = singlePaymentSpi.getPaymentStatusById(psuData, payment, aspspConsentData);
        } else if (paymentType == PERIODIC) {
            SpiPeriodicPayment payment = new SpiPeriodicPayment(null);
            payment.setPaymentId(paymentId);
            spiResponse = periodicPaymentSpi.getPaymentStatusById(psuData, payment, aspspConsentData);
        } else {
            SpiBulkPayment payment = new SpiBulkPayment();
            spiResponse = bulkPaymentSpi.getPaymentStatusById(psuData, payment, aspspConsentData);
        }

        pisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());
        Xs2aTransactionStatus transactionStatus = paymentMapper.mapToTransactionStatus(spiResponse.getPayload());
        return Optional.ofNullable(transactionStatus)
                   .map(tr -> ResponseObject.<Xs2aTransactionStatus>builder().body(tr).build())
                   .orElseGet(ResponseObject.<Xs2aTransactionStatus>builder()
                                  .fail(new MessageError(RESOURCE_UNKNOWN_403))
                                  ::build);
    }
}
