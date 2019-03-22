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

package de.adorsys.psd2.xs2a.service.authorization.ais;

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static de.adorsys.psd2.xs2a.domain.consent.ConsentAuthorizationResponseLinkType.SCA_REDIRECT;

/**
 * AisAuthorizationService implementation to be used in case of redirect approach
 */
@Service
@RequiredArgsConstructor
public class RedirectAisAuthorizationService implements AisAuthorizationService {
    private final Xs2aAisConsentService aisConsentService;

    /**
     * Creates consent authorisation using provided psu id and consent id by invoking CMS through AisConsentService
     * See {@link Xs2aAisConsentService#createAisConsentAuthorization(String, ScaStatus, PsuIdData)} for details
     *
     * @param psuData   PsuIdData container of authorisation data about PSU
     * @param consentId String identification of consent
     * @return Optional of CreateConsentAuthorizationResponse with consent creating data
     */
    @Override
    public Optional<CreateConsentAuthorizationResponse> createConsentAuthorization(PsuIdData psuData, String consentId) {
        return aisConsentService.createAisConsentAuthorization(consentId, ScaStatus.STARTED, psuData)
                   .map(authId -> {
                       CreateConsentAuthorizationResponse resp = new CreateConsentAuthorizationResponse();

                       resp.setConsentId(consentId);
                       resp.setAuthorizationId(authId);
                       resp.setScaStatus(ScaStatus.STARTED);
                       resp.setResponseLinkType(SCA_REDIRECT);

                       return resp;
                   });
    }

    @Override
    public UpdateConsentPsuDataResponse updateConsentPsuData(UpdateConsentPsuDataReq updatePsuData, AccountConsentAuthorization consentAuthorization) {
        return null;
    }

    @Override
    public Optional<AccountConsentAuthorization> getAccountConsentAuthorizationById(String authorizationId, String consentId) {
        return Optional.empty();
    }

    /**
     * Gets list of consent authorisation IDs by invoking CMS through AisConsentService
     * See {@link Xs2aAisConsentService#getAuthorisationSubResources(String)} for details
     *
     * @param consentId String identification of consent
     * @return Optional of Xs2aAuthorisationSubResources with list of authorisation IDs
     */
    @Override
    public Optional<Xs2aAuthorisationSubResources> getAuthorisationSubResources(String consentId) {
        return aisConsentService.getAuthorisationSubResources(consentId)
                   .map(Xs2aAuthorisationSubResources::new);
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
        return aisConsentService.getAuthorisationScaStatus(consentId, authorisationId);
    }

    @Override
    public ScaApproach getScaApproachServiceType() {
        return ScaApproach.REDIRECT;
    }
}
