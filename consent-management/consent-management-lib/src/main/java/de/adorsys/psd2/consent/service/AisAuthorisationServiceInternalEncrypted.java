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

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.CmsScaMethod;
import de.adorsys.psd2.consent.api.ais.AisConsentAuthorizationRequest;
import de.adorsys.psd2.consent.api.ais.AisConsentAuthorizationResponse;
import de.adorsys.psd2.consent.api.ais.CreateAisConsentAuthorizationResponse;
import de.adorsys.psd2.consent.api.service.AisConsentAuthorisationService;
import de.adorsys.psd2.consent.api.service.AisConsentAuthorisationServiceEncrypted;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.consent.api.CmsError.TECHNICAL_ERROR;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AisAuthorisationServiceInternalEncrypted implements AisConsentAuthorisationServiceEncrypted {
    private final SecurityDataService securityDataService;
    private final AisConsentAuthorisationService aisConsentAuthorisationService;

    @Override
    @Transactional
    public CmsResponse<CreateAisConsentAuthorizationResponse> createAuthorizationWithResponse(String encryptedConsentId, AisConsentAuthorizationRequest request) {
        Optional<String> decryptIdOptional = securityDataService.decryptId(encryptedConsentId);

        if (!decryptIdOptional.isPresent()) {
            log.info("Encrypted Consent ID: [{}]. Create authorisation with response failed, couldn't decrypt consent id",
                     encryptedConsentId);
            return CmsResponse.<CreateAisConsentAuthorizationResponse>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return aisConsentAuthorisationService.createAuthorizationWithResponse(decryptIdOptional.get(), request);
    }

    @Override
    public CmsResponse<AisConsentAuthorizationResponse> getAccountConsentAuthorizationById(String authorisationId,
                                                                                           String encryptedConsentId) {
        Optional<String> decryptIdOptional = securityDataService.decryptId(encryptedConsentId);

        if (!decryptIdOptional.isPresent()) {
            log.info("Encrypted Consent ID: [{}]. Getaccount consent authorisation failed, couldn't decrypt consent id",
                     encryptedConsentId);
            return CmsResponse.<AisConsentAuthorizationResponse>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return aisConsentAuthorisationService.getAccountConsentAuthorizationById(authorisationId, decryptIdOptional.get());

    }

    @Override
    @Transactional
    public CmsResponse<Boolean> updateConsentAuthorization(String authorizationId, AisConsentAuthorizationRequest request) {
        return aisConsentAuthorisationService.updateConsentAuthorization(authorizationId, request);
    }

    @Override
    @Transactional
    public CmsResponse<Boolean> updateConsentAuthorisationStatus(String authorisationId, ScaStatus scaStatus) {
        return aisConsentAuthorisationService.updateConsentAuthorisationStatus(authorisationId, scaStatus);
    }

    @Override
    public CmsResponse<List<String>> getAuthorisationsByConsentId(String encryptedConsentId) {
        Optional<String> decryptIdOptional = securityDataService.decryptId(encryptedConsentId);

        if (!decryptIdOptional.isPresent()) {
            log.info("Encrypted Consent ID: [{}]. Get the list of authorisation IDs failed, couldn't decrypt consent id",
                     encryptedConsentId);
            return CmsResponse.<List<String>>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return aisConsentAuthorisationService.getAuthorisationsByConsentId(decryptIdOptional.get());
    }

    @Override
    @Transactional
    public CmsResponse<ScaStatus> getAuthorisationScaStatus(String encryptedConsentId, String authorisationId) {
        Optional<String> decryptIdOptional = securityDataService.decryptId(encryptedConsentId);

        if (!decryptIdOptional.isPresent()) {
            log.info("Encrypted Consent ID: [{}]. Get authorisation SCA status failed, couldn't decrypt consent id",
                     encryptedConsentId);
            return CmsResponse.<ScaStatus>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return aisConsentAuthorisationService.getAuthorisationScaStatus(decryptIdOptional.get(), authorisationId);
    }

    @Override
    public CmsResponse<Boolean> isAuthenticationMethodDecoupled(String authorisationId, String authenticationMethodId) {
        return aisConsentAuthorisationService.isAuthenticationMethodDecoupled(authorisationId, authenticationMethodId);
    }

    @Override
    @Transactional
    public CmsResponse<Boolean> saveAuthenticationMethods(String authorisationId, List<CmsScaMethod> methods) {
        return aisConsentAuthorisationService.saveAuthenticationMethods(authorisationId, methods);
    }

    @Override
    @Transactional
    public CmsResponse<Boolean> updateScaApproach(String authorisationId, ScaApproach scaApproach) {
        return aisConsentAuthorisationService.updateScaApproach(authorisationId, scaApproach);
    }

    @Override
    public CmsResponse<AuthorisationScaApproachResponse> getAuthorisationScaApproach(String authorisationId) {
        return aisConsentAuthorisationService.getAuthorisationScaApproach(authorisationId);
    }
}
