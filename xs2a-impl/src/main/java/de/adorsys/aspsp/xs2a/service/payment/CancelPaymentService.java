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

package de.adorsys.aspsp.xs2a.service.payment;

import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.Xs2aTransactionStatus;
import de.adorsys.aspsp.xs2a.domain.pis.CancelPaymentResponse;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.consent.PisConsentDataService;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aCancelPaymentMapper;
import de.adorsys.psd2.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentCancellationResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PaymentCancellationSpi;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static de.adorsys.aspsp.xs2a.domain.MessageErrorCode.RESOURCE_UNKNOWN_403;

@Service
@RequiredArgsConstructor
public class CancelPaymentService {
    private final PaymentCancellationSpi paymentCancellationSpi;
    private final SpiToXs2aCancelPaymentMapper spiToXs2aCancelPaymentMapper;
    private final PisConsentDataService pisConsentDataService;

    /**
     * Cancels payment without performing strong customer authentication
     *
     * @param psuData     ASPSP identifier(s) of the psu
     * @param payment     Payment to be cancelled
     * @param consentData Encrypted data that may be stored in the consent management system in the consent linked to a request.
     * @return Response containing information about cancelled payment or corresponding error
     */
    public ResponseObject<CancelPaymentResponse> cancelPaymentWithoutAuthorisation(SpiPsuData psuData, SpiPayment payment, AspspConsentData consentData) {
        SpiResponse<SpiResponse.VoidResponse> spiResponse = paymentCancellationSpi.cancelPaymentWithoutSca(psuData, payment, consentData);
        pisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiResponse.hasError()) {
            return ResponseObject.<CancelPaymentResponse>builder()
                       .fail(new MessageError(RESOURCE_UNKNOWN_403))
                       .build();
        }

        CancelPaymentResponse cancelPaymentResponse = new CancelPaymentResponse();
        cancelPaymentResponse.setTransactionStatus(Xs2aTransactionStatus.CANC);

        return ResponseObject.<CancelPaymentResponse>builder()
                   .body(cancelPaymentResponse)
                   .build();
    }

    /**
     * Cancels payment without performing strong customer authentication
     *
     * @param psuData     ASPSP identifier(s) of the psu
     * @param payment     Payment to be cancelled
     * @param consentData Encrypted data that may be stored in the consent management system in the consent linked to a request.
     * @return Response containing information about cancelled payment or corresponding error
     */
    public ResponseObject<CancelPaymentResponse> initiatePaymentCancellation(SpiPsuData psuData, SpiPayment payment, AspspConsentData consentData) {
        SpiResponse<SpiPaymentCancellationResponse> spiResponse = paymentCancellationSpi.initiatePaymentCancellation(psuData, payment, consentData);
        pisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiResponse.hasError()) {
            return ResponseObject.<CancelPaymentResponse>builder()
                       .fail(new MessageError(RESOURCE_UNKNOWN_403))
                       .build();
        }

        CancelPaymentResponse cancelPaymentResponse = spiToXs2aCancelPaymentMapper.mapToCancelPaymentResponse(spiResponse.getPayload());

        return Optional.ofNullable(cancelPaymentResponse)
                   .map(p -> ResponseObject.<CancelPaymentResponse>builder()
                                 .body(p)
                                 .build())
                   .orElseGet(ResponseObject.<CancelPaymentResponse>builder()
                                  .fail(new MessageError(RESOURCE_UNKNOWN_403))::build);
    }
}
