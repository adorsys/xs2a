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

package de.adorsys.psd2.consent.web.xs2a.controller;

import de.adorsys.psd2.consent.api.AuthorisationApi;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.CmsScaMethod;
import de.adorsys.psd2.consent.api.authorisation.AuthorisationParentHolder;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationRequest;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationResponse;
import de.adorsys.psd2.consent.api.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.consent.api.service.AuthorisationServiceEncrypted;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthorisationController implements AuthorisationApi {
    private final AuthorisationServiceEncrypted authorisationServiceEncrypted;

    @Override
    public ResponseEntity<CreateAuthorisationResponse> createConsentAuthorisation(AuthorisationType authorisationType, String parentId, CreateAuthorisationRequest authorisationRequest) {
        CmsResponse<CreateAuthorisationResponse> cmsResponse = authorisationServiceEncrypted.createAuthorisation(new AuthorisationParentHolder(authorisationType, parentId), authorisationRequest);

        if (cmsResponse.hasError()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(cmsResponse.getPayload(), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Authorisation> getAuthorisation(String authorisationId) {
        CmsResponse<Authorisation> response = authorisationServiceEncrypted.getAuthorisationById(authorisationId);

        if (response.hasError()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(response.getPayload(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> updateAuthorisation(String authorisationId, UpdateAuthorisationRequest authorisationRequest) {
        CmsResponse<Authorisation> response = authorisationServiceEncrypted.updateAuthorisation(authorisationId, authorisationRequest);

        if (response.isSuccessful() && response.getPayload() != null && response.getPayload().getParentId() != null) {
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseEntity<Void> updateAuthorisationStatus(String authorisationId, String scaStatus) {
        try {
            CmsResponse<Boolean> response = authorisationServiceEncrypted.updateAuthorisationStatus(authorisationId, ScaStatus.valueOf(scaStatus));
            if (response.isSuccessful() && BooleanUtils.isTrue(response.getPayload())) {
                return new ResponseEntity<>(HttpStatus.OK);
            }
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("Invalid sca status: [{}] for authorisation-ID [{}]", scaStatus, authorisationId);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseEntity<ScaStatus> getAuthorisationScaStatus(AuthorisationType authorisationType, String parentId, String authorisationId) {
        CmsResponse<ScaStatus> response = authorisationServiceEncrypted.getAuthorisationScaStatus(authorisationId, new AuthorisationParentHolder(authorisationType, parentId));

        if (response.hasError()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(response.getPayload(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<String>> getAuthorisationsByParentId(AuthorisationType authorisationType, String parentId) {
        CmsResponse<List<String>> response = authorisationServiceEncrypted.getAuthorisationsByParentId(new AuthorisationParentHolder(authorisationType, parentId));

        if (response.hasError()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(response.getPayload(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Boolean> isAuthenticationMethodDecoupled(String authorisationId, String authenticationMethodId) {
        CmsResponse<Boolean> response = authorisationServiceEncrypted.isAuthenticationMethodDecoupled(authorisationId, authenticationMethodId);
        return new ResponseEntity<>(response.getPayload(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> saveAuthenticationMethods(String authorisationId, List<CmsScaMethod> methods) {
        CmsResponse<Boolean> response = authorisationServiceEncrypted.saveAuthenticationMethods(authorisationId, methods);

        if (response.isSuccessful() && BooleanUtils.isTrue(response.getPayload())) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseEntity<Boolean> updateScaApproach(String authorisationId, ScaApproach scaApproach) {
        CmsResponse<Boolean> response = authorisationServiceEncrypted.updateScaApproach(authorisationId, scaApproach);

        if (response.isSuccessful() && BooleanUtils.isTrue(response.getPayload())) {
            return new ResponseEntity<>(true, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseEntity<AuthorisationScaApproachResponse> getAuthorisationScaApproach(String authorisationId) {
        CmsResponse<AuthorisationScaApproachResponse> response = authorisationServiceEncrypted.getAuthorisationScaApproach(authorisationId);

        if (response.hasError()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(response.getPayload(), HttpStatus.OK);
    }
}
