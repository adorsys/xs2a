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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.CmsScaMethod;
import de.adorsys.psd2.consent.api.ais.AisConsentAuthorizationRequest;
import de.adorsys.psd2.consent.api.ais.AisConsentAuthorizationResponse;
import de.adorsys.psd2.consent.api.service.AisConsentAuthorisationService;
import de.adorsys.psd2.consent.api.service.AisConsentAuthorisationServiceEncrypted;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AisAuthorisationServiceInternalEncrypted implements AisConsentAuthorisationServiceEncrypted {
    private final SecurityDataService securityDataService;
    private final AisConsentAuthorisationService aisConsentAuthorisationService;

    @Override
    @Transactional
    public Optional<String> createAuthorization(String encryptedConsentId, AisConsentAuthorizationRequest request) {
        return securityDataService.decryptId(encryptedConsentId)
                   .flatMap(id -> aisConsentAuthorisationService.createAuthorization(id, request));
    }

    @Override
    public Optional<AisConsentAuthorizationResponse> getAccountConsentAuthorizationById(String authorisationId,
                                                                                        String encryptedConsentId) {
        return securityDataService.decryptId(encryptedConsentId)
                   .flatMap(id -> aisConsentAuthorisationService.getAccountConsentAuthorizationById(authorisationId, id));
    }

    @Override
    @Transactional
    public boolean updateConsentAuthorization(String authorizationId, AisConsentAuthorizationRequest request) {
        return aisConsentAuthorisationService.updateConsentAuthorization(authorizationId, request);
    }

    @Override
    public Optional<List<String>> getAuthorisationsByConsentId(String encryptedConsentId) {
        return securityDataService.decryptId(encryptedConsentId)
                   .flatMap(aisConsentAuthorisationService::getAuthorisationsByConsentId);
    }

    @Override
    @Transactional
    public Optional<ScaStatus> getAuthorisationScaStatus(String encryptedConsentId, String authorisationId) {
        return securityDataService.decryptId(encryptedConsentId)
                   .flatMap(consentId -> aisConsentAuthorisationService.getAuthorisationScaStatus(consentId, authorisationId));
    }

    @Override
    public boolean isAuthenticationMethodDecoupled(String authorisationId, String authenticationMethodId) {
        return aisConsentAuthorisationService.isAuthenticationMethodDecoupled(authorisationId, authenticationMethodId);
    }

    @Override
    @Transactional
    public boolean saveAuthenticationMethods(String authorisationId, List<CmsScaMethod> methods) {
        return aisConsentAuthorisationService.saveAuthenticationMethods(authorisationId, methods);
    }

    @Override
    @Transactional
    public boolean updateScaApproach(String authorisationId, ScaApproach scaApproach) {
        return aisConsentAuthorisationService.updateScaApproach(authorisationId, scaApproach);
    }
}
