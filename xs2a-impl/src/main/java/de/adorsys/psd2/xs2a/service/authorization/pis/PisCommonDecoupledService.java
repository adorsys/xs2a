/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.authorization.pis;

import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthenticationObject;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aDecoupledUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.domain.pis.PaymentAuthorisationType;
import de.adorsys.psd2.xs2a.service.consent.PisAspspDataService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationDecoupledScaResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PaymentAuthorisationSpi;
import de.adorsys.psd2.xs2a.spi.service.PaymentCancellationSpi;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static de.adorsys.psd2.xs2a.core.sca.ScaStatus.SCAMETHODSELECTED;

@Service
@RequiredArgsConstructor
public class PisCommonDecoupledService {
    private final PaymentAuthorisationSpi paymentAuthorisationSpi;
    private final PaymentCancellationSpi paymentCancellationSpi;
    private final SpiContextDataProvider spiContextDataProvider;
    private final PisAspspDataService pisAspspDataService;
    private final SpiErrorMapper spiErrorMapper;

    public Xs2aUpdatePisCommonPaymentPsuDataResponse proceedDecoupledInitiation(Xs2aUpdatePisCommonPaymentPsuDataRequest request, SpiPayment payment) {
        return proceedDecoupledInitiation(request, payment, null);
    }

    public Xs2aUpdatePisCommonPaymentPsuDataResponse proceedDecoupledInitiation(Xs2aUpdatePisCommonPaymentPsuDataRequest request, SpiPayment payment, String authenticationMethodId) {
        return proceedDecoupled(request, payment, authenticationMethodId, PaymentAuthorisationType.INITIATION);
    }

    public Xs2aUpdatePisCommonPaymentPsuDataResponse proceedDecoupledCancellation(Xs2aUpdatePisCommonPaymentPsuDataRequest request, SpiPayment payment) {
        return proceedDecoupledCancellation(request, payment, null);
    }

    public Xs2aUpdatePisCommonPaymentPsuDataResponse proceedDecoupledCancellation(Xs2aUpdatePisCommonPaymentPsuDataRequest request, SpiPayment payment, String authenticationMethodId) {
        return proceedDecoupled(request, payment, authenticationMethodId, PaymentAuthorisationType.CANCELLATION);
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse proceedDecoupled(Xs2aUpdatePisCommonPaymentPsuDataRequest request, SpiPayment payment, String authenticationMethodId, PaymentAuthorisationType scaType) {
        PsuIdData psuData = request.getPsuData();
        AspspConsentData aspspConsentData = pisAspspDataService.getAspspConsentData(request.getPaymentId());
        SpiResponse<SpiAuthorisationDecoupledScaResponse> spiResponse = null;

        switch (scaType) {
            case INITIATION:
                spiResponse = paymentAuthorisationSpi.startScaDecoupled(spiContextDataProvider.provideWithPsuIdData(psuData), request.getAuthorisationId(), authenticationMethodId, payment, aspspConsentData);
                break;
            case CANCELLATION:
                spiResponse = paymentCancellationSpi.startScaDecoupled(spiContextDataProvider.provideWithPsuIdData(psuData), request.getAuthorisationId(), authenticationMethodId, payment, aspspConsentData);
                break;
            default:
                throw new IllegalArgumentException("This SCA type is not supported: " + scaType);
        }

        pisAspspDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiResponse.hasError()) {
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS), request.getPaymentId(), request.getAuthorisationId(), psuData);
        }

        Xs2aUpdatePisCommonPaymentPsuDataResponse response = new Xs2aDecoupledUpdatePisCommonPaymentPsuDataResponse(SCAMETHODSELECTED, request.getPaymentId(), request.getAuthorisationId(), psuData);
        response.setPsuMessage(spiResponse.getPayload().getPsuMessage());
        response.setChosenScaMethod(buildXs2aAuthenticationObjectForDecoupledApproach(request.getAuthenticationMethodId()));
        return response;
    }

    // Should ONLY be used for switching from Embedded to Decoupled approach during SCA method selection
    private Xs2aAuthenticationObject buildXs2aAuthenticationObjectForDecoupledApproach(String authenticationMethodId) {
        Xs2aAuthenticationObject xs2aAuthenticationObject = new Xs2aAuthenticationObject();
        xs2aAuthenticationObject.setAuthenticationMethodId(authenticationMethodId);
        return xs2aAuthenticationObject;
    }
}

