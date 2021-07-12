/*
 * Copyright 2018-2021 adorsys GmbH & Co KG
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

import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorizationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreateAuthorisationRequest;
import de.adorsys.psd2.xs2a.service.authorization.ConsentAuthorizationService;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aConsentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * ConsentAuthorizationService implementation to be used in case of redirect approach
 */
@Slf4j
@RequiredArgsConstructor
public abstract class RedirectConsentAuthorizationService implements ConsentAuthorizationService {
    private final Xs2aAuthorisationService authorisationService;
    private final Xs2aConsentService consentService;

    /**
     * Creates consent authorisation using provided psu id and consent id by invoking CMS through ConsentAuthorizationService
     * See {@link Xs2aConsentService#createConsentAuthorisation(String, String, ScaApproach, ScaStatus, PsuIdData)} for details
     *
     * @param createAuthorisationRequest   create authorisation request
     * @return Optional of CreateConsentAuthorizationResponse with consent creating data
     */
    @Override
    public Optional<CreateConsentAuthorizationResponse> createConsentAuthorization(@NotNull Xs2aCreateAuthorisationRequest createAuthorisationRequest) {
        String consentId = createAuthorisationRequest.getConsentId();
        if (isConsentAbsent(consentId)) {
            log.warn("Consent-ID [{}]. Create consent authorisation has failed. Consent not found by id.", consentId);
            return Optional.empty();
        }
        PsuIdData psuData = createAuthorisationRequest.getPsuData();
        return consentService.createConsentAuthorisation(consentId,
                                                         createAuthorisationRequest.getAuthorisationId(),
                                                         createAuthorisationRequest.getScaApproach(),
                                                         createAuthorisationRequest.getScaStatus(),
                                                         psuData)
                   .map(auth -> {
                       CreateConsentAuthorizationResponse resp = new CreateConsentAuthorizationResponse();

                       resp.setConsentId(consentId);
                       resp.setAuthorisationId(auth.getAuthorizationId());
                       resp.setScaStatus(auth.getScaStatus());
                       resp.setPsuIdData(psuData);
                       resp.setScaApproach(auth.getScaApproach());
                       resp.setInternalRequestId(auth.getInternalRequestId());
                       return resp;
                   });
    }

    protected abstract boolean isConsentAbsent(String consentId);

    @Override
    public Optional<Authorisation> getConsentAuthorizationById(String authorizationId) {
        return authorisationService.getAuthorisationById(authorizationId);
    }

    /**
     * Gets SCA status of the authorisation from CMS
     *
     * @param consentId       String representation of consent identifier
     * @param authorisationId String representation of authorisation identifier
     * @return SCA status of the authorisation
     */
    @Override
    public Optional<ScaStatus> getAuthorisationScaStatus(String consentId, String authorisationId) {
        return consentService.getAuthorisationScaStatus(consentId, authorisationId);
    }

    @Override
    public ScaApproach getScaApproachServiceType() {
        return ScaApproach.REDIRECT;
    }
}
