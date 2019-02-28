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

package de.adorsys.psd2.xs2a.service.payment;

import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.pis.CancelPaymentResponse;
import de.adorsys.psd2.xs2a.service.authorization.PaymentCancellationAuthorisationNeededDecider;
import de.adorsys.psd2.xs2a.service.consent.PisAspspDataService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aCancelPaymentMapper;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentCancellationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PaymentCancellationSpi;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.RESOURCE_BLOCKED;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.PIS_400;

@Service
@RequiredArgsConstructor
public class CancelPaymentService {
    private final PaymentCancellationSpi paymentCancellationSpi;
    private final PisAspspDataService pisAspspDataService;
    private final Xs2aUpdatePaymentStatusAfterSpiService updatePaymentStatusAfterSpiService;
    private final PaymentCancellationAuthorisationNeededDecider cancellationScaNeededDecider;
    private final SpiContextDataProvider spiContextDataProvider;
    private final SpiErrorMapper spiErrorMapper;
    private final SpiToXs2aCancelPaymentMapper spiToXs2aCancelPaymentMapper;

    /**
     * Cancels payment with or without performing strong customer authentication
     *
     * @param psuData            ASPSP identifier(s) of the psu
     * @param payment            Payment to be cancelled
     * @param encryptedPaymentId encrypted identifier of the payment
     * @return Response containing information about cancelled payment or corresponding error
     */
    public ResponseObject<CancelPaymentResponse> initiatePaymentCancellation(PsuIdData psuData, SpiPayment payment, String encryptedPaymentId) {
        SpiContextData spiContextData = spiContextDataProvider.provideWithPsuIdData(psuData);
        AspspConsentData aspspConsentData = pisAspspDataService.getAspspConsentData(encryptedPaymentId);
        SpiResponse<SpiPaymentCancellationResponse> spiResponse = paymentCancellationSpi.initiatePaymentCancellation(spiContextData, payment, aspspConsentData);
        pisAspspDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiResponse.hasError()) {
            return ResponseObject.<CancelPaymentResponse>builder()
                       .fail(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS))
                       .build();
        }

        CancelPaymentResponse cancelPaymentResponse = spiToXs2aCancelPaymentMapper.mapToCancelPaymentResponse(spiResponse.getPayload());
        TransactionStatus resultStatus = cancelPaymentResponse.getTransactionStatus();

        if (resultStatus != null) {
            updatePaymentStatusAfterSpiService.updatePaymentStatus(encryptedPaymentId, resultStatus);
        } else {
            resultStatus = payment.getPaymentStatus();
            cancelPaymentResponse.setTransactionStatus(resultStatus);
        }

        if (resultStatus == TransactionStatus.CANC) {
            return ResponseObject.<CancelPaymentResponse>builder()
                       .body(cancelPaymentResponse)
                       .build();
        }

        if (resultStatus.isFinalisedStatus()) {
            return ResponseObject.<CancelPaymentResponse>builder()
                       .fail(PIS_400, of(RESOURCE_BLOCKED))
                       .build();
        }

        if (resultStatus == TransactionStatus.RCVD
                || cancellationScaNeededDecider.isNoScaRequired(cancelPaymentResponse.isStartAuthorisationRequired())) {
            payment.setPaymentStatus(resultStatus);
            return proceedNoScaCancellation(payment, spiContextData, aspspConsentData, encryptedPaymentId);
        } else {
            return ResponseObject.<CancelPaymentResponse>builder()
                       .body(cancelPaymentResponse)
                       .build();
        }
    }

    private ResponseObject<CancelPaymentResponse> proceedNoScaCancellation(SpiPayment payment, SpiContextData spiContextData, AspspConsentData aspspConsentData, String encryptedPaymentId) {
        SpiResponse<SpiResponse.VoidResponse> spiResponse = paymentCancellationSpi.cancelPaymentWithoutSca(spiContextData, payment, aspspConsentData);
        pisAspspDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiResponse.hasError()) {
            return ResponseObject.<CancelPaymentResponse>builder()
                       .fail(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS))
                       .build();
        }

        updatePaymentStatusAfterSpiService.updatePaymentStatus(encryptedPaymentId, TransactionStatus.CANC);
        CancelPaymentResponse cancelPaymentResponse = new CancelPaymentResponse();
        cancelPaymentResponse.setTransactionStatus(TransactionStatus.CANC);

        return ResponseObject.<CancelPaymentResponse>builder()
                   .body(cancelPaymentResponse)
                   .build();
    }
}
