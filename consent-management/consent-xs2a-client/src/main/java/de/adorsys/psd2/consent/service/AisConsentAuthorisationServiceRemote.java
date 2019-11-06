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
import de.adorsys.psd2.consent.api.service.AisConsentAuthorisationServiceEncrypted;
import de.adorsys.psd2.consent.config.AisConsentRemoteUrls;
import de.adorsys.psd2.consent.config.CmsRestException;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static de.adorsys.psd2.consent.api.CmsError.TECHNICAL_ERROR;

// TODO discuss error handling (e.g. 400 HttpCode response) https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/498
@Slf4j
@Service
@RequiredArgsConstructor
public class AisConsentAuthorisationServiceRemote implements AisConsentAuthorisationServiceEncrypted {
    @Qualifier("consentRestTemplate")
    private final RestTemplate consentRestTemplate;
    private final AisConsentRemoteUrls remoteAisConsentUrls;

    @Override
    public CmsResponse<CreateAisConsentAuthorizationResponse> createAuthorizationWithResponse(String consentId, AisConsentAuthorizationRequest request) {
        try {
            CreateAisConsentAuthorizationResponse response = consentRestTemplate.postForEntity(remoteAisConsentUrls.createAisConsentAuthorization(),
                                                                                               request, CreateAisConsentAuthorizationResponse.class, consentId).getBody();
            return CmsResponse.<CreateAisConsentAuthorizationResponse>builder()
                       .payload(response)
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.warn("Remote create authorisation with response failed");
        }

        return CmsResponse.<CreateAisConsentAuthorizationResponse>builder()
                   .error(TECHNICAL_ERROR)
                   .build();
    }

    @Override
    public CmsResponse<AisConsentAuthorizationResponse> getAccountConsentAuthorizationById(String authorizationId, String consentId) {
        try {
            AisConsentAuthorizationResponse response = consentRestTemplate.getForEntity(remoteAisConsentUrls.getAisConsentAuthorizationById(), AisConsentAuthorizationResponse.class, consentId, authorizationId)
                                                           .getBody();
            return CmsResponse.<AisConsentAuthorizationResponse>builder()
                       .payload(response)
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.info("Couldn't get account consent authorisation by consentId {} and authorisationId {}", consentId, authorizationId);
        }

        return CmsResponse.<AisConsentAuthorizationResponse>builder()
                   .error(TECHNICAL_ERROR)
                   .build();
    }

    @Override
    public CmsResponse<Boolean> updateConsentAuthorization(String authorizationId, AisConsentAuthorizationRequest request) {
        try {
            consentRestTemplate.put(remoteAisConsentUrls.updateAisConsentAuthorization(), request, authorizationId);
            return CmsResponse.<Boolean>builder()
                       .payload(true)
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.info("Couldn't update authorisation by authorisationId {}", authorizationId);
        }

        return CmsResponse.<Boolean>builder()
                   .payload(false)
                   .build();
    }

    @Override
    public CmsResponse<Boolean> updateConsentAuthorisationStatus(String authorisationId, ScaStatus scaStatus) {
        try {
            consentRestTemplate.put(remoteAisConsentUrls.updateAisConsentAuthorisationStatus(), null, authorisationId, scaStatus);
            return CmsResponse.<Boolean>builder()
                       .payload(true)
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.info("Couldn't update authorisation status by authorisationId {}", authorisationId);
        }

        return CmsResponse.<Boolean>builder()
                   .payload(false)
                   .build();
    }

    @Override
    public CmsResponse<List<String>> getAuthorisationsByConsentId(String encryptedConsentId) {
        try {
            ResponseEntity<List<String>> request = consentRestTemplate.exchange(
                remoteAisConsentUrls.getAuthorisationSubResources(), HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {
                }, encryptedConsentId);
            return CmsResponse.<List<String>>builder()
                       .payload(request.getBody())
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.warn("No authorisation found by consentId {}", encryptedConsentId);
        }

        return CmsResponse.<List<String>>builder()
                   .error(TECHNICAL_ERROR)
                   .build();
    }

    @Override
    public CmsResponse<ScaStatus> getAuthorisationScaStatus(String encryptedConsentId, String authorisationId) {
        try {
            ResponseEntity<ScaStatus> request = consentRestTemplate.getForEntity(
                remoteAisConsentUrls.getAuthorisationScaStatus(), ScaStatus.class, encryptedConsentId, authorisationId);
            return CmsResponse.<ScaStatus>builder()
                       .payload(request.getBody())
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.info("Couldn't get authorisation SCA Status by consentId {} and authorisationId {}", encryptedConsentId, authorisationId);
        }

        return CmsResponse.<ScaStatus>builder()
                   .error(TECHNICAL_ERROR)
                   .build();
    }

    @Override
    public CmsResponse<Boolean> isAuthenticationMethodDecoupled(String authorisationId, String authenticationMethodId) {
        Boolean body = consentRestTemplate.getForEntity(remoteAisConsentUrls.isAuthenticationMethodDecoupled(), Boolean.class, authorisationId, authenticationMethodId)
                           .getBody();

        return CmsResponse.<Boolean>builder()
                   .payload(body)
                   .build();
    }

    @Override
    public CmsResponse<Boolean> saveAuthenticationMethods(String authorisationId, List<CmsScaMethod> methods) {
        try {
            ResponseEntity<Void> responseEntity = consentRestTemplate.exchange(remoteAisConsentUrls.saveAuthenticationMethods(), HttpMethod.POST, new HttpEntity<>(methods), Void.class, authorisationId);

            if (responseEntity.getStatusCode() == HttpStatus.NO_CONTENT) {
                return CmsResponse.<Boolean>builder()
                           .payload(true)
                           .build();
            }
        } catch (CmsRestException cmsRestException) {
            log.warn("Couldn't save authentication methods {} by authorisationId {}", methods, authorisationId);
        }

        return CmsResponse.<Boolean>builder()
                   .payload(false)
                   .build();
    }

    @Override
    public CmsResponse<Boolean> updateScaApproach(String authorisationId, ScaApproach scaApproach) {
        try {
            Boolean body = consentRestTemplate.exchange(remoteAisConsentUrls.updateScaApproach(), HttpMethod.PUT, null, Boolean.class, authorisationId, scaApproach)
                               .getBody();
            return CmsResponse.<Boolean>builder()
                       .payload(body)
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.warn("Couldn't update SCA approach {} by authorisationId {}", scaApproach, authorisationId);
        }

        return CmsResponse.<Boolean>builder()
                   .payload(false)
                   .build();
    }

    @Override
    public CmsResponse<AuthorisationScaApproachResponse> getAuthorisationScaApproach(String authorisationId) {
        try {
            ResponseEntity<AuthorisationScaApproachResponse> request = consentRestTemplate.getForEntity(
                remoteAisConsentUrls.getAuthorisationScaApproach(), AuthorisationScaApproachResponse.class, authorisationId);
            return CmsResponse.<AuthorisationScaApproachResponse>builder()
                       .payload(request.getBody())
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.warn("Couldn't get authorisation SCA Approach by authorisationId {}", authorisationId);
        }

        return CmsResponse.<AuthorisationScaApproachResponse>builder()
                   .error(TECHNICAL_ERROR)
                   .build();
    }
}
