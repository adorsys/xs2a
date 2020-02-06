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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.CmsScaMethod;
import de.adorsys.psd2.consent.api.authorisation.AuthorisationParentHolder;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationRequest;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationResponse;
import de.adorsys.psd2.consent.api.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.consent.api.service.AuthorisationService;
import de.adorsys.psd2.consent.api.service.AuthorisationServiceEncrypted;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
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
public class AuthorisationServiceInternalEncrypted implements AuthorisationServiceEncrypted {
    private final SecurityDataService securityDataService;
    private final AuthorisationService authorisationService;

    @Transactional
    @Override
    public CmsResponse<CreateAuthorisationResponse> createAuthorisation(AuthorisationParentHolder parentHolder, CreateAuthorisationRequest request) {
        String encryptedId = parentHolder.getParentId();
        Optional<String> decryptedIdOptional = securityDataService.decryptId(encryptedId);

        if (!decryptedIdOptional.isPresent()) {
            log.info("Encrypted Parent ID: [{}]. Create authorisation has failed, couldn't decrypt parent id",
                     encryptedId);
            return CmsResponse.<CreateAuthorisationResponse>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return authorisationService.createAuthorisation(new AuthorisationParentHolder(parentHolder.getAuthorisationType(), decryptedIdOptional.get()), request);
    }

    @Transactional(readOnly = true)
    @Override
    public CmsResponse<Authorisation> getAuthorisationById(String authorisationId) {
        return authorisationService.getAuthorisationById(authorisationId);
    }

    @Transactional
    @Override
    public CmsResponse<Authorisation> updateAuthorisation(String authorisationId, UpdateAuthorisationRequest request) {
        return authorisationService.updateAuthorisation(authorisationId, request);
    }

    @Transactional
    @Override
    public CmsResponse<Boolean> updateAuthorisationStatus(String authorisationId, ScaStatus scaStatus) {
        return authorisationService.updateAuthorisationStatus(authorisationId, scaStatus);
    }

    @Transactional
    @Override
    public CmsResponse<List<String>> getAuthorisationsByParentId(AuthorisationParentHolder parentHolder) {
        String encryptedId = parentHolder.getParentId();
        Optional<String> decryptedIdOptional = securityDataService.decryptId(encryptedId);

        if (!decryptedIdOptional.isPresent()) {
            log.info("Encrypted Parent ID: [{}]. Get authorisation has failed, couldn't decrypt parent id",
                     encryptedId);
            return CmsResponse.<List<String>>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return authorisationService.getAuthorisationsByParentId(new AuthorisationParentHolder(parentHolder.getAuthorisationType(), decryptedIdOptional.get()));
    }

    @Transactional
    @Override
    public CmsResponse<ScaStatus> getAuthorisationScaStatus(String authorisationId, AuthorisationParentHolder parentHolder) {
        String encryptedId = parentHolder.getParentId();
        Optional<String> decryptedIdOptional = securityDataService.decryptId(encryptedId);

        if (!decryptedIdOptional.isPresent()) {
            log.info("Encrypted Parent ID: [{}]. Get SCA status has failed, couldn't decrypt parent id",
                     encryptedId);
            return CmsResponse.<ScaStatus>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return authorisationService.getAuthorisationScaStatus(authorisationId, new AuthorisationParentHolder(parentHolder.getAuthorisationType(), decryptedIdOptional.get()));
    }

    @Transactional(readOnly = true)
    @Override
    public CmsResponse<Boolean> isAuthenticationMethodDecoupled(String authorisationId, String authenticationMethodId) {
        return authorisationService.isAuthenticationMethodDecoupled(authorisationId, authenticationMethodId);
    }

    @Transactional
    @Override
    public CmsResponse<Boolean> saveAuthenticationMethods(String authorisationId, List<CmsScaMethod> methods) {
        return authorisationService.saveAuthenticationMethods(authorisationId, methods);
    }

    @Transactional
    @Override
    public CmsResponse<Boolean> updateScaApproach(String authorisationId, ScaApproach scaApproach) {
        return authorisationService.updateScaApproach(authorisationId, scaApproach);
    }

    @Transactional(readOnly = true)
    @Override
    public CmsResponse<AuthorisationScaApproachResponse> getAuthorisationScaApproach(String authorisationId) {
        return authorisationService.getAuthorisationScaApproach(authorisationId);
    }
}
