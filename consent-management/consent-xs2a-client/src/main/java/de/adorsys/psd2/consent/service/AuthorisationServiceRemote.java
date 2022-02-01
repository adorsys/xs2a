/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.CmsScaMethod;
import de.adorsys.psd2.consent.api.authorisation.AuthorisationParentHolder;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationRequest;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationResponse;
import de.adorsys.psd2.consent.api.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.consent.api.service.AuthorisationServiceEncrypted;
import de.adorsys.psd2.consent.config.AuthorisationRemoteUrls;
import de.adorsys.psd2.consent.config.CmsRestException;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorisationServiceRemote implements AuthorisationServiceEncrypted {
    @Qualifier("consentRestTemplate")
    private final RestTemplate consentRestTemplate;
    private final AuthorisationRemoteUrls authorisationRemoteUrls;

    @Override
    public CmsResponse<CreateAuthorisationResponse> createAuthorisation(AuthorisationParentHolder parentHolder, CreateAuthorisationRequest request) {
        try {
            CreateAuthorisationResponse response = consentRestTemplate.postForEntity(authorisationRemoteUrls.createAuthorisation(),
                                                                                     request, CreateAuthorisationResponse.class, parentHolder.getAuthorisationType(), parentHolder.getParentId()).getBody();
            return CmsResponse.<CreateAuthorisationResponse>builder()
                       .payload(response)
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.info("Couldn't create new authorisation, HTTP response status: {}", cmsRestException.getHttpStatus());
        }

        return CmsResponse.<CreateAuthorisationResponse>builder()
                   .error(TECHNICAL_ERROR)
                   .build();
    }

    @Override
    public CmsResponse<Authorisation> getAuthorisationById(String authorisationId) {
        try {
            Authorisation response = consentRestTemplate.getForEntity(authorisationRemoteUrls.getAuthorisationById(), Authorisation.class, authorisationId)
                                         .getBody();
            return CmsResponse.<Authorisation>builder()
                       .payload(response)
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.info("Couldn't get authorisation by authorisation ID {}, HTTP response status: {}",
                     authorisationId, cmsRestException.getHttpStatus());
        }

        return CmsResponse.<Authorisation>builder()
                   .error(TECHNICAL_ERROR)
                   .build();
    }

    @Override
    public CmsResponse<Authorisation> updateAuthorisation(String authorisationId, UpdateAuthorisationRequest request) {
        try {
            ResponseEntity<Authorisation> responseEntity = consentRestTemplate.exchange(authorisationRemoteUrls.updateAuthorisation(), HttpMethod.PUT, new HttpEntity<>(request), Authorisation.class, authorisationId);
            return CmsResponse.<Authorisation>builder()
                       .payload(responseEntity.getBody())
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.info("Couldn't update authorisation by authorisation ID {}, HTTP response status: {}",
                     authorisationId, cmsRestException.getHttpStatus());
        }

        return CmsResponse.<Authorisation>builder()
                   .error(TECHNICAL_ERROR)
                   .build();
    }

    @Override
    public CmsResponse<Boolean> updateAuthorisationStatus(String authorisationId, ScaStatus scaStatus) {
        try {
            consentRestTemplate.put(authorisationRemoteUrls.updateAuthorisationStatus(), null, authorisationId, scaStatus);
            return CmsResponse.<Boolean>builder()
                       .payload(true)
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.info("Couldn't update authorisation status by authorisation ID {}, HTTP response status: {}",
                     authorisationId, cmsRestException.getHttpStatus());
        }

        return CmsResponse.<Boolean>builder()
                   .payload(false)
                   .build();
    }

    @Override
    public CmsResponse<List<String>> getAuthorisationsByParentId(AuthorisationParentHolder parentHolder) {
        String parentId = parentHolder.getParentId();
        try {
            ResponseEntity<List<String>> request = consentRestTemplate.exchange(
                authorisationRemoteUrls.getAuthorisationsByParentId(), HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {
                }, parentHolder.getAuthorisationType(), parentId);
            return CmsResponse.<List<String>>builder()
                       .payload(request.getBody())
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.info("Couldn't get authorisations by parent ID {}, HTTP response status: {}",
                     parentId, cmsRestException.getHttpStatus());
        }

        return CmsResponse.<List<String>>builder()
                   .error(TECHNICAL_ERROR)
                   .build();
    }

    @Override
    public CmsResponse<ScaStatus> getAuthorisationScaStatus(String authorisationId, AuthorisationParentHolder parentHolder) {
        String parentId = parentHolder.getParentId();
        try {
            ResponseEntity<ScaStatus> request = consentRestTemplate.getForEntity(
                authorisationRemoteUrls.getAuthorisationScaStatus(), ScaStatus.class, parentHolder.getAuthorisationType(), parentId, authorisationId);
            return CmsResponse.<ScaStatus>builder()
                       .payload(request.getBody())
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.info("Couldn't get SCA Status by parent ID {} and authorisation ID {}, HTTP response status: {}",
                     parentId, authorisationId, cmsRestException.getHttpStatus());
        }

        return CmsResponse.<ScaStatus>builder()
                   .error(TECHNICAL_ERROR)
                   .build();
    }

    @Override
    public CmsResponse<Boolean> isAuthenticationMethodDecoupled(String authorisationId, String authenticationMethodId) {
        Boolean body = consentRestTemplate.getForEntity(authorisationRemoteUrls.isAuthenticationMethodDecoupled(), Boolean.class, authorisationId, authenticationMethodId)
                           .getBody();

        return CmsResponse.<Boolean>builder()
                   .payload(body)
                   .build();
    }

    @Override
    public CmsResponse<Boolean> saveAuthenticationMethods(String authorisationId, List<CmsScaMethod> methods) {
        try {
            ResponseEntity<Void> responseEntity = consentRestTemplate.exchange(authorisationRemoteUrls.saveAuthenticationMethods(), HttpMethod.POST, new HttpEntity<>(methods), Void.class, authorisationId);

            if (responseEntity.getStatusCode() == HttpStatus.NO_CONTENT) {
                return CmsResponse.<Boolean>builder()
                           .payload(true)
                           .build();
            }
        } catch (CmsRestException cmsRestException) {
            log.warn("Couldn't save authentication methods {} by authorisation ID {}, HTTP response status: {}",
                     methods, authorisationId, cmsRestException.getHttpStatus());
        }

        return CmsResponse.<Boolean>builder()
                   .payload(false)
                   .build();
    }

    @Override
    public CmsResponse<Boolean> updateScaApproach(String authorisationId, ScaApproach scaApproach) {
        try {
            Boolean body = consentRestTemplate.exchange(authorisationRemoteUrls.updateScaApproach(), HttpMethod.PUT, null, Boolean.class, authorisationId, scaApproach)
                               .getBody();
            return CmsResponse.<Boolean>builder()
                       .payload(body)
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.warn("Couldn't update SCA approach {} by authorisation ID {}, HTTP response status: {}",
                     scaApproach, authorisationId, cmsRestException.getHttpStatus());
        }

        return CmsResponse.<Boolean>builder()
                   .payload(false)
                   .build();
    }

    @Override
    public CmsResponse<AuthorisationScaApproachResponse> getAuthorisationScaApproach(String authorisationId) {
        try {
            ResponseEntity<AuthorisationScaApproachResponse> request = consentRestTemplate.getForEntity(
                authorisationRemoteUrls.getAuthorisationScaApproach(), AuthorisationScaApproachResponse.class, authorisationId);
            return CmsResponse.<AuthorisationScaApproachResponse>builder()
                       .payload(request.getBody())
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.warn("Couldn't get SCA Approach by authorisation ID {}, HTTP response status: {}",
                     authorisationId, cmsRestException.getHttpStatus());
        }

        return CmsResponse.<AuthorisationScaApproachResponse>builder()
                   .error(TECHNICAL_ERROR)
                   .build();
    }
}
