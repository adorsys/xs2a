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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.CmsScaMethod;
import de.adorsys.psd2.consent.api.ais.*;
import de.adorsys.psd2.consent.api.service.AisConsentService;
import de.adorsys.psd2.consent.api.service.AisConsentServiceEncrypted;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AisConsentServiceInternalEncrypted implements AisConsentServiceEncrypted {
    private final SecurityDataService securityDataService;
    private final AisConsentService aisConsentService;

    @Override
    @Transactional
    public Optional<String> createConsent(CreateAisConsentRequest request) {
        return aisConsentService.createConsent(request)
                   .flatMap(securityDataService::encryptId);
    }

    @Override
    @Transactional
    public Optional<ConsentStatus> getConsentStatusById(String encryptedConsentId) {
        return securityDataService.decryptId(encryptedConsentId)
                   .flatMap(aisConsentService::getConsentStatusById);
    }

    @Override
    @Transactional
    public boolean updateConsentStatusById(String encryptedConsentId, ConsentStatus status) {
        return securityDataService.decryptId(encryptedConsentId)
                   .map(id -> aisConsentService.updateConsentStatusById(id, status))
                   .orElse(false);
    }

    @Override
    @Transactional
    public Optional<AisAccountConsent> getAisAccountConsentById(String encryptedConsentId) {
        return securityDataService.decryptId(encryptedConsentId)
                   .flatMap(aisConsentService::getAisAccountConsentById);
    }

    @Override
    @Transactional
    public Optional<AisAccountConsent> getInitialAisAccountConsentById(String encryptedConsentId) {
        return securityDataService.decryptId(encryptedConsentId)
                   .flatMap(aisConsentService::getInitialAisAccountConsentById);
    }

    @Override
    @Transactional
    public boolean findAndTerminateOldConsentsByNewConsentId(String encryptedNewConsentId) {
        return securityDataService.decryptId(encryptedNewConsentId)
                   .map(aisConsentService::findAndTerminateOldConsentsByNewConsentId)
                   .orElse(false);
    }

    @Override
    @Transactional
    public void checkConsentAndSaveActionLog(AisConsentActionRequest encryptedRequest) {
        String consentId = encryptedRequest.getConsentId();
        Optional<String> decryptedConsentId = securityDataService.decryptId(consentId);
        if (!decryptedConsentId.isPresent()) {
            return;
        }

        AisConsentActionRequest decryptedRequest = new AisConsentActionRequest(encryptedRequest.getTppId(),
            decryptedConsentId.get(),
            encryptedRequest.getActionStatus());
        aisConsentService.checkConsentAndSaveActionLog(decryptedRequest);
    }

    @Override
    @Transactional
    public Optional<String> updateAspspAccountAccess(String encryptedConsentId, AisAccountAccessInfo request) {
        return securityDataService.decryptId(encryptedConsentId)
                   .flatMap(decrypted -> aisConsentService.updateAspspAccountAccess(decrypted, request))
                   .flatMap(securityDataService::encryptId);
    }

    @Override
    @Transactional
    public Optional<String> createAuthorization(String encryptedConsentId, AisConsentAuthorizationRequest request) {
        return securityDataService.decryptId(encryptedConsentId)
                   .flatMap(id -> aisConsentService.createAuthorization(id, request));
    }

    @Override
    public Optional<AisConsentAuthorizationResponse> getAccountConsentAuthorizationById(String authorisationId,
                                                                                        String encryptedConsentId) {
        return securityDataService.decryptId(encryptedConsentId)
                   .flatMap(id -> aisConsentService.getAccountConsentAuthorizationById(authorisationId, id));
    }

    @Override
    @Transactional
    public boolean updateConsentAuthorization(String authorizationId, AisConsentAuthorizationRequest request) {
        return aisConsentService.updateConsentAuthorization(authorizationId, request);
    }

    @Override
    public Optional<List<PsuIdData>> getPsuDataByConsentId(String encryptedConsentId) {
        return securityDataService.decryptId(encryptedConsentId)
                   .flatMap(aisConsentService::getPsuDataByConsentId);
    }

    @Override
    public Optional<List<String>> getAuthorisationsByConsentId(String encryptedConsentId) {
        return securityDataService.decryptId(encryptedConsentId)
                   .flatMap(aisConsentService::getAuthorisationsByConsentId);
    }

    @Override
    @Transactional
    public Optional<ScaStatus> getAuthorisationScaStatus(String encryptedConsentId, String authorisationId) {
        return securityDataService.decryptId(encryptedConsentId)
                   .flatMap(consentId -> aisConsentService.getAuthorisationScaStatus(consentId, authorisationId));
    }

    @Override
    public boolean isAuthenticationMethodDecoupled(String authorisationId, String authenticationMethodId) {
        return aisConsentService.isAuthenticationMethodDecoupled(authorisationId, authenticationMethodId);
    }

    @Override
    @Transactional
    public boolean saveAuthenticationMethods(String authorisationId, List<CmsScaMethod> methods) {
        return aisConsentService.saveAuthenticationMethods(authorisationId, methods);
    }

    @Override
    @Transactional
    public boolean updateScaApproach(String authorisationId, ScaApproach scaApproach) {
        return aisConsentService.updateScaApproach(authorisationId, scaApproach);
    }

    @Override
    @Transactional
    public boolean updateMultilevelScaRequired(String encryptedConsentId, boolean multilevelScaRequired) {
        return securityDataService.decryptId(encryptedConsentId)
                   .map(consentId -> aisConsentService.updateMultilevelScaRequired(consentId, multilevelScaRequired))
                   .orElse(false);
    }
}
