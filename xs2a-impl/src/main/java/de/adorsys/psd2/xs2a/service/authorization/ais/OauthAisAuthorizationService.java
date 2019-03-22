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
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OauthAisAuthorizationService implements AisAuthorizationService {
    @Override
    public Optional<CreateConsentAuthorizationResponse> createConsentAuthorization(PsuIdData psuData, String consentId) {
        return Optional.empty();
    }

    @Override
    public UpdateConsentPsuDataResponse updateConsentPsuData(UpdateConsentPsuDataReq updatePsuData, AccountConsentAuthorization consentAuthorization) {
        return new UpdateConsentPsuDataResponse();
    }

    @Override
    public Optional<AccountConsentAuthorization> getAccountConsentAuthorizationById(String authorizationId, String consentId) {
        return Optional.empty();
    }

    @Override
    public Optional<Xs2aAuthorisationSubResources> getAuthorisationSubResources(String consentId) {
        return Optional.empty();
    }

    @Override
    public Optional<ScaStatus> getAuthorisationScaStatus(String consentId, String authorisationId) {
        return Optional.empty();
    }

    @Override
    public ScaApproach getScaApproachServiceType() {
        return ScaApproach.OAUTH;
    }
}
