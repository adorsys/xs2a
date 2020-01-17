/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

import de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.pis.PaymentAuthorisationType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisCommonPaymentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationDecoupledScaResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PaymentAuthorisationSpi;
import de.adorsys.psd2.xs2a.spi.service.PaymentCancellationSpi;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.sca.ScaStatus.SCAMETHODSELECTED;

@Slf4j
@Service
@RequiredArgsConstructor
public class PisCommonDecoupledService {
    private final PaymentAuthorisationSpi paymentAuthorisationSpi;
    private final PaymentCancellationSpi paymentCancellationSpi;
    private final SpiContextDataProvider spiContextDataProvider;
    private final SpiErrorMapper spiErrorMapper;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final Xs2aPisCommonPaymentService xs2aPisCommonPaymentService;

    public Xs2aUpdatePisCommonPaymentPsuDataResponse proceedDecoupledInitiation(Xs2aUpdatePisCommonPaymentPsuDataRequest request, SpiPayment payment) {
        return proceedDecoupledInitiation(request, payment, null);
    }

    public Xs2aUpdatePisCommonPaymentPsuDataResponse proceedDecoupledInitiation(Xs2aUpdatePisCommonPaymentPsuDataRequest request, SpiPayment payment, String authenticationMethodId) {
        return proceedDecoupled(request, payment, authenticationMethodId, PaymentAuthorisationType.CREATED);
    }

    public Xs2aUpdatePisCommonPaymentPsuDataResponse proceedDecoupledCancellation(Xs2aUpdatePisCommonPaymentPsuDataRequest request, SpiPayment payment) {
        return proceedDecoupledCancellation(request, payment, null);
    }

    public Xs2aUpdatePisCommonPaymentPsuDataResponse proceedDecoupledCancellation(Xs2aUpdatePisCommonPaymentPsuDataRequest request, SpiPayment payment, String authenticationMethodId) {
        return proceedDecoupled(request, payment, authenticationMethodId, PaymentAuthorisationType.CANCELLED);
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse proceedDecoupled(Xs2aUpdatePisCommonPaymentPsuDataRequest request, SpiPayment payment, String authenticationMethodId, PaymentAuthorisationType scaType) {
        String authenticationId = request.getAuthorisationId();
        String paymentId = request.getPaymentId();
        PsuIdData psuData = request.getPsuData();
        SpiAspspConsentDataProvider aspspConsentDataProvider = aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(request.getPaymentId());
        SpiResponse<SpiAuthorisationDecoupledScaResponse> spiResponse = null;

        switch (scaType) {
            case CREATED:
                spiResponse = paymentAuthorisationSpi.startScaDecoupled(spiContextDataProvider.provideWithPsuIdData(psuData), authenticationId, authenticationMethodId, payment, aspspConsentDataProvider);
                break;
            case CANCELLED:
                spiResponse = paymentCancellationSpi.startScaDecoupled(spiContextDataProvider.provideWithPsuIdData(psuData), authenticationId, authenticationMethodId, payment, aspspConsentDataProvider);
                break;
            default:
                log.info("Payment-ID [{}]. Payment Authorisation Type {} is not supported.", paymentId, scaType);
                throw new IllegalArgumentException("This SCA type is not supported: " + scaType);
        }

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS);
            log.warn("Payment-ID [{}], Authorisation-ID [{}], PSU-ID [{}]. Start SPI Authorisation Decoupled SCA has failed. Error msg: {}.",
                     paymentId, authenticationId, psuData.getPsuId(), errorHolder);

            Optional<MessageErrorCode> first = errorHolder.getFirstErrorCode();
            if (first.isPresent() && first.get() == MessageErrorCode.PSU_CREDENTIALS_INVALID) {
                xs2aPisCommonPaymentService.updatePisAuthorisationStatus(authenticationId, ScaStatus.FAILED);
            }
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authenticationId, psuData);
        }

        Xs2aUpdatePisCommonPaymentPsuDataResponse response = new Xs2aUpdatePisCommonPaymentPsuDataResponse(SCAMETHODSELECTED, request.getPaymentId(), request.getAuthorisationId(), psuData);
        response.setPsuMessage(spiResponse.getPayload().getPsuMessage());
        response.setChosenScaMethod(buildXs2aAuthenticationObjectForDecoupledApproach(request.getAuthenticationMethodId()));
        return response;
    }

    // Should ONLY be used for switching from Embedded to Decoupled approach during SCA method selection
    private AuthenticationObject buildXs2aAuthenticationObjectForDecoupledApproach(String authenticationMethodId) {
        AuthenticationObject authenticationObject = new AuthenticationObject();
        authenticationObject.setAuthenticationMethodId(authenticationMethodId);
        return authenticationObject;
    }
}

