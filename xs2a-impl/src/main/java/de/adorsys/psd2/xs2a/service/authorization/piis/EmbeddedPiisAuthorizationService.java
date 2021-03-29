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

package de.adorsys.psd2.xs2a.service.authorization.piis;

import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorizationResponse;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.consent.Xs2aConsentService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPiisConsentService;
import de.adorsys.psd2.xs2a.service.mapper.ConsentPsuDataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmbeddedPiisAuthorizationService implements PiisAuthorizationService {
    private final Xs2aConsentService consentService;
    private final Xs2aPiisConsentService piisConsentService;
    private final Xs2aAuthorisationService authorisationService;
    private final ConsentPsuDataMapper consentPsuDataMapper;

    @Override
    public Optional<CreateConsentAuthorizationResponse> createConsentAuthorization(PsuIdData psuData, String consentId) {
        Optional<PiisConsent> piisConsentOptional = piisConsentService.getPiisConsentById(consentId);
        if (piisConsentOptional.isEmpty()) {
            log.info("Consent-ID [{}]. Create consent authorisation has failed. Consent not found by id.", consentId);
            return Optional.empty();
        }

        return consentService.createConsentAuthorisation(consentId, ScaStatus.RECEIVED, psuData)
                   .map(auth -> {
                       CreateConsentAuthorizationResponse resp = new CreateConsentAuthorizationResponse();

                       resp.setConsentId(consentId);
                       resp.setAuthorisationId(auth.getAuthorizationId());
                       resp.setScaStatus(auth.getScaStatus());
                       resp.setPsuIdData(psuData);

                       return resp;
                   });
    }

    @Override
    public AuthorisationProcessorResponse updateConsentPsuData(UpdateAuthorisationRequest request, AuthorisationProcessorResponse response) {
        if (response.hasError()) {
            log.info("Consent-ID [{}], Authorisation-ID [{}]. Update consent authorisation failed. Error msg: {}.",
                     request.getBusinessObjectId(), request.getAuthorisationId(), response.getErrorHolder());
        } else {
            piisConsentService.updateConsentAuthorisation(consentPsuDataMapper.mapToUpdateConsentPsuDataReq(request, response));
        }

        return response;
    }

    @Override
    public Optional<Authorisation> getConsentAuthorizationById(String authorizationId) {
        return authorisationService.getAuthorisationById(authorizationId);
    }

    @Override
    public Optional<ScaStatus> getAuthorisationScaStatus(String consentId, String authorisationId) {
        return consentService.getAuthorisationScaStatus(consentId, authorisationId);
    }

    @Override
    public ScaApproach getScaApproachServiceType() {
        return ScaApproach.EMBEDDED;
    }
}
