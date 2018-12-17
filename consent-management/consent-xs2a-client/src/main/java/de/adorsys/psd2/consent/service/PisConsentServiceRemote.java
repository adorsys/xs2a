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

import de.adorsys.psd2.consent.api.CmsAuthorisationType;
import de.adorsys.psd2.consent.api.pis.authorisation.CreatePisConsentAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.GetPisConsentAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisConsentPsuDataRequest;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisConsentPsuDataResponse;
import de.adorsys.psd2.consent.api.pis.proto.CreatePisConsentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisConsentRequest;
import de.adorsys.psd2.consent.api.pis.proto.PisConsentResponse;
import de.adorsys.psd2.consent.api.service.PisConsentServiceEncrypted;
import de.adorsys.psd2.consent.config.CmsRestException;
import de.adorsys.psd2.consent.config.PisConsentRemoteUrls;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
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
import java.util.Optional;

// TODO discuss error handling (e.g. 400 HttpCode response) https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/498
@Slf4j
@Service
@RequiredArgsConstructor
public class PisConsentServiceRemote implements PisConsentServiceEncrypted {
    @Qualifier("consentRestTemplate")
    private final RestTemplate consentRestTemplate;
    private final PisConsentRemoteUrls remotePisConsentUrls;

    @Override
    public Optional<CreatePisConsentResponse> createPaymentConsent(PisConsentRequest request) {
        return Optional.ofNullable(consentRestTemplate.postForEntity(remotePisConsentUrls.createPisConsent(), request, CreatePisConsentResponse.class).getBody());
    }

    @Override
    public Optional<ConsentStatus> getConsentStatusById(String consentId) {
        return Optional.empty();
    }

    @Override
    public Optional<PisConsentResponse> getConsentById(String consentId) {
        return Optional.ofNullable(consentRestTemplate.getForEntity(remotePisConsentUrls.getPisConsentById(), PisConsentResponse.class, consentId)
                                       .getBody());
    }

    @Override
    public Optional<Boolean> updateConsentStatusById(String consentId, ConsentStatus status) {
        HttpStatus statusCode = consentRestTemplate.exchange(remotePisConsentUrls.updatePisConsentStatus(), HttpMethod.PUT,
            null, Void.class, consentId, status).getStatusCode();

        return Optional.of(statusCode == HttpStatus.OK);
    }

    @Override
    public Optional<String> getDecryptedId(String encryptedId) {
        return Optional.ofNullable(consentRestTemplate.getForEntity(remotePisConsentUrls.getPaymentIdByEncryptedString(), String.class, encryptedId)
                                       .getBody());
    }

    @Override
    public Optional<CreatePisConsentAuthorisationResponse> createAuthorization(String paymentId, CmsAuthorisationType authorizationType, PsuIdData psuData) {
        return Optional.ofNullable(consentRestTemplate.postForEntity(remotePisConsentUrls.createPisConsentAuthorisation(),
            psuData, CreatePisConsentAuthorisationResponse.class, paymentId)
                                       .getBody());
    }

    @Override
    public Optional<CreatePisConsentAuthorisationResponse> createAuthorizationCancellation(String paymentId, CmsAuthorisationType authorizationType, PsuIdData psuData) {
        return Optional.ofNullable(consentRestTemplate.postForEntity(remotePisConsentUrls.createPisConsentAuthorisationCancellation(),
            psuData, CreatePisConsentAuthorisationResponse.class, paymentId)
                                       .getBody());
    }

    @Override
    public Optional<UpdatePisConsentPsuDataResponse> updateConsentAuthorisation(String authorisationId, UpdatePisConsentPsuDataRequest request) {
        return Optional.ofNullable(consentRestTemplate.exchange(remotePisConsentUrls.updatePisConsentAuthorisation(), HttpMethod.PUT, new HttpEntity<>(request),
            UpdatePisConsentPsuDataResponse.class, request.getAuthorizationId()).getBody());
    }

    @Override
    public Optional<UpdatePisConsentPsuDataResponse> updateConsentCancellationAuthorisation(String authorisationId, UpdatePisConsentPsuDataRequest request) {
        return Optional.ofNullable(consentRestTemplate.exchange(remotePisConsentUrls.updatePisConsentCancellationAuthorisation(), HttpMethod.PUT, new HttpEntity<>(request),
            UpdatePisConsentPsuDataResponse.class, request.getAuthorizationId()).getBody());
    }

    @Override
    public void updatePaymentConsent(PisConsentRequest request, String consentId) {
        consentRestTemplate.exchange(remotePisConsentUrls.updatePisConsentPayment(), HttpMethod.PUT, new HttpEntity<>(request), Void.class, consentId);
    }

    @Override
    public Optional<GetPisConsentAuthorisationResponse> getPisConsentAuthorisationById(String authorizationId) {
        return Optional.ofNullable(consentRestTemplate.exchange(remotePisConsentUrls.getPisConsentAuthorisationById(), HttpMethod.GET, null, GetPisConsentAuthorisationResponse.class, authorizationId)
                                       .getBody());
    }

    @Override
    public Optional<GetPisConsentAuthorisationResponse> getPisConsentCancellationAuthorisationById(String cancellationId) {
        return Optional.ofNullable(consentRestTemplate.exchange(remotePisConsentUrls.getPisConsentCancellationAuthorisationById(), HttpMethod.GET, null, GetPisConsentAuthorisationResponse.class, cancellationId)
                                       .getBody());
    }

    @Override
    public Optional<List<String>> getAuthorisationsByPaymentId(String paymentId, CmsAuthorisationType authorisationType) {
        String url = getAuthorisationSubResourcesUrl(authorisationType);
        try {
            ResponseEntity<List<String>> request = consentRestTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<String>>() {
                }, paymentId);
            return Optional.ofNullable(request.getBody());
        } catch (CmsRestException cmsRestException) {
            log.warn("No authorisation found by paymentId {}", paymentId);
        }
        return Optional.empty();
    }

    private String getAuthorisationSubResourcesUrl(CmsAuthorisationType authorisationType) {
        switch (authorisationType) {
            case CREATED:
                return remotePisConsentUrls.getAuthorisationSubResources();
            case CANCELLED:
                return remotePisConsentUrls.getCancellationAuthorisationSubResources();
            default:
                log.error("Unknown payment authorisation type {}", authorisationType);
                throw new IllegalArgumentException("Unknown payment authorisation type " + authorisationType);
        }
    }

    @Override
    public Optional<PsuIdData> getPsuDataByPaymentId(String paymentId) {
        return Optional.ofNullable(consentRestTemplate.getForEntity(remotePisConsentUrls.getPsuDataByPaymentId(), PsuIdData.class, paymentId)
                                       .getBody());
    }

    @Override
    public Optional<PsuIdData> getPsuDataByConsentId(String consentId) {
        return Optional.ofNullable(consentRestTemplate.getForEntity(remotePisConsentUrls.getPsuDataByConsentId(), PsuIdData.class, consentId)
                                       .getBody());
    }
}
