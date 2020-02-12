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

package de.adorsys.psd2.xs2a.service.authorization.ais;

import de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationDecoupledScaResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommonDecoupledAisService {
    private final AisConsentSpi aisConsentSpi;
    private final SpiErrorMapper spiErrorMapper;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final SpiContextDataProvider spiContextDataProvider;
    private final Xs2aAisConsentService aisConsentService;

    public UpdateConsentPsuDataResponse proceedDecoupledApproach(String consentId, String authorisationId, SpiAccountConsent spiAccountConsent, PsuIdData psuData) {
        return proceedDecoupledApproach(consentId, authorisationId, spiAccountConsent, null, psuData);
    }

    public UpdateConsentPsuDataResponse proceedDecoupledApproach(String consentId, String authorisationId, SpiAccountConsent spiAccountConsent,
                                                                 String authenticationMethodId, PsuIdData psuData) {
        SpiResponse<SpiAuthorisationDecoupledScaResponse> spiResponse = aisConsentSpi.startScaDecoupled(spiContextDataProvider.provideWithPsuIdData(psuData), authorisationId, authenticationMethodId, spiAccountConsent, aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId));

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS);
            log.info("Consent-ID [{}], Authorisation-ID [{}], PSU-ID [{}], Authentication-Method-ID [{}]. Notifies a decoupled app about starting SCA when proceed decoupled approach has failed. Error msg: {}.",
                     consentId, authorisationId, psuData.getPsuId(), authenticationMethodId, errorHolder);

            Optional<MessageErrorCode> first = errorHolder.getFirstErrorCode();
            if (first.isPresent() && first.get() == MessageErrorCode.PSU_CREDENTIALS_INVALID) {
                aisConsentService.updateConsentAuthorisationStatus(authorisationId, ScaStatus.FAILED);
            }
            return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId, psuData);
        }

        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse(ScaStatus.SCAMETHODSELECTED, consentId, authorisationId, psuData);
        response.setPsuMessage(spiResponse.getPayload().getPsuMessage());
        response.setChosenScaMethod(buildXs2aAuthenticationObjectForDecoupledApproach(authenticationMethodId));
        return response;
    }

    // Should ONLY be used for switching from Embedded to Decoupled approach during SCA method selection
    private AuthenticationObject buildXs2aAuthenticationObjectForDecoupledApproach(String authenticationMethodId) {
        AuthenticationObject authenticationObject = new AuthenticationObject();
        authenticationObject.setAuthenticationMethodId(authenticationMethodId);
        return authenticationObject;
    }
}
