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

package de.adorsys.aspsp.xs2a.service.consent;

import de.adorsys.aspsp.xs2a.config.rest.consent.AisConsentRemoteUrls;
import de.adorsys.aspsp.xs2a.domain.consent.AccountConsentAuthorization;
import de.adorsys.aspsp.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.aspsp.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.aspsp.xs2a.service.mapper.consent.Xs2aAisConsentAuthorisationMapper;
import de.adorsys.aspsp.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.psd2.consent.api.ActionStatus;
import de.adorsys.psd2.consent.api.ais.*;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AisConsentService {
    @Qualifier("consentRestTemplate")
    private final RestTemplate consentRestTemplate;
    private final AisConsentRemoteUrls remoteAisConsentUrls;
    private final Xs2aAisConsentMapper aisConsentMapper;
    private final Xs2aAisConsentAuthorisationMapper aisConsentAuthorisationMapper;

    /**
     * Sends a POST request to CMS to store created AISconsent
     *
     * @param request          Request body storing main consent details
     * @param psuId            String representation of PSU`s identifier at ASPSP
     * @param tppId            String representation of TPP`s identifier from TPP Certificate
     * @return String representation of identifier of stored consent
     */
    public String createConsent(CreateConsentReq request, String psuId, String tppId) {
        CreateAisConsentRequest createAisConsentRequest = aisConsentMapper.mapToCreateAisConsentRequest(request, psuId, tppId);
        CreateAisConsentResponse createAisConsentResponse = consentRestTemplate.postForEntity(remoteAisConsentUrls.createAisConsent(), createAisConsentRequest, CreateAisConsentResponse.class).getBody();

        return Optional.ofNullable(createAisConsentResponse)
                   .map(CreateAisConsentResponse::getConsentId)
                   .orElse(null);
    }

    /**
     * Requests CMS to retrieve AIS consent by its identifier
     *
     * @param consentId String representation of identifier of stored consent
     * @return Response containing AIS Consent
     */
    // TODO don't use Spi models here https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/431
    public SpiAccountConsent getAccountConsentById(String consentId) {
        return consentRestTemplate.getForEntity(remoteAisConsentUrls.getAisConsentById(), SpiAccountConsent.class, consentId).getBody();
    }

    /**
     * Requests CMS to retrieve AIS consent status by its identifier
     *
     * @param consentId String representation of identifier of stored consent
     * @return Response containing AIS Consent Status
     */
    public ConsentStatus getAccountConsentStatusById(String consentId) {
        AisConsentStatusResponse response = consentRestTemplate.getForEntity(remoteAisConsentUrls.getAisConsentStatusById(), AisConsentStatusResponse.class, consentId).getBody();
        return response.getConsentStatus();
    }

    /**
     * Requests CMS to update consent status to "Revoked by PSU" state
     *
     * @param consentId String representation of identifier of stored consent
     */
    public void revokeConsent(String consentId) {
        consentRestTemplate.put(remoteAisConsentUrls.updateAisConsentStatus(), null, consentId, ConsentStatus.REVOKED_BY_PSU);
    }

    /**
     * Requests CMS to update consent status into provided one
     *
     * @param consentId String representation of identifier of stored consent
     * @param consentStatus ConsentStatus the consent be changed to
     */
    public void updateConsentStatus(String consentId, ConsentStatus consentStatus) {
        consentRestTemplate.put(remoteAisConsentUrls.updateAisConsentStatus(), null, consentId, consentStatus);
    }

    /**
     * Sends a POST request to CMS to perform decrement of consent usages and report status of the operation held with certain AIS consent
     *
     * @param tppId        String representation of TPP`s identifier from TPP Certificate
     * @param consentId    String representation of identifier of stored consent
     * @param actionStatus Enum value representing whether the acition is successful or errors occured
     */
    public void consentActionLog(String tppId, String consentId, ActionStatus actionStatus) {
        consentRestTemplate.postForEntity(remoteAisConsentUrls.consentActionLog(), new AisConsentActionRequest(tppId, consentId, actionStatus), Void.class);
    }

    /**
     * Sends a POST request to CMS to store created consent authorization
     *
     * @param consentId String representation of identifier of stored consent
     * @return long representation of identifier of stored consent authorization
     */
    public Optional<String> createAisConsentAuthorization(String consentId, ScaStatus scaStatus, String psuId) {
        AisConsentAuthorizationRequest request = aisConsentAuthorisationMapper.mapToAisConsentAuthorization(scaStatus, psuId);

        CreateAisConsentAuthorizationResponse response = consentRestTemplate.postForEntity(remoteAisConsentUrls.createAisConsentAuthorization(),
            request, CreateAisConsentAuthorizationResponse.class, consentId).getBody();

        return Optional.ofNullable(response)
                   .map(CreateAisConsentAuthorizationResponse::getAuthorizationId);
    }

    /**
     * Requests CMS to retrieve AIS consent authorization by its identifier
     *
     * @param authorizationId String representation of identifier of stored consent authorization
     * @return Response containing AIS Consent Authorization
     */

    public AccountConsentAuthorization getAccountConsentAuthorizationById(String authorizationId, String consentId) {
        AisConsentAuthorizationResponse resp = consentRestTemplate.getForEntity(remoteAisConsentUrls.getAisConsentAuthorizationById(), AisConsentAuthorizationResponse.class, consentId, authorizationId).getBody();
        return aisConsentAuthorisationMapper.mapToAccountConsentAuthorization(resp);
    }

    /**
     * Sends a PUT request to CMS to update created AIS consent authorization
     *
     * @param updatePsuData Consent psu data
     */
    public void updateConsentAuthorization(UpdateConsentPsuDataReq updatePsuData) {
        Optional.ofNullable(updatePsuData)
            .ifPresent(req -> {
                final String authorizationId = req.getAuthorizationId();
                final AisConsentAuthorizationRequest request = aisConsentAuthorisationMapper.mapToAisConsentAuthorizationRequest(req);

                consentRestTemplate.put(remoteAisConsentUrls.updateAisConsentAuthorization(), request, authorizationId);
            });
    }
}
