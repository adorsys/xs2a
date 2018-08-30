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

package de.adorsys.aspsp.xs2a.service.consent.ais;

import de.adorsys.aspsp.xs2a.config.rest.consent.AisConsentRemoteUrls;
import de.adorsys.aspsp.xs2a.consent.api.ActionStatus;
import de.adorsys.aspsp.xs2a.consent.api.AisConsentStatusResponse;
import de.adorsys.aspsp.xs2a.consent.api.ConsentActionRequest;
import de.adorsys.aspsp.xs2a.consent.api.TypeAccess;
import de.adorsys.aspsp.xs2a.consent.api.ais.CreateAisConsentAuthorizationResponse;
import de.adorsys.aspsp.xs2a.consent.api.ais.CreateAisConsentResponse;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.aspsp.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.aspsp.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.aspsp.xs2a.service.authorization.AisAuthorizationService;
import de.adorsys.aspsp.xs2a.service.mapper.ConsentMapper;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsentAuthorization;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiScaStatus;
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
    private final ConsentMapper consentMapper;
    private final AisAuthorizationService authorizationService;

    /**
     * Sends a POST request to CMS to store created AISconsent
     *
     * @param request Request body storing main consent details
     * @param psuId   String representation of PSU`s identifier at ASPSP
     * @param tppId   String representation of TPP`s identifier from TPP Certificate
     * @return String representation of identifier of stored consent
     */
    public String createConsent(CreateConsentReq request, String psuId, String tppId) {
        AspspConsentData aspspConsentData = new AspspConsentData("zzzzzzzzzzzzzz".getBytes());

        CreateAisConsentResponse createAisConsentResponse = consentRestTemplate.postForEntity(remoteAisConsentUrls.createAisConsent(), consentMapper.mapToCreateAisConsentRequest(request, psuId, tppId, aspspConsentData), CreateAisConsentResponse.class).getBody();

        return Optional.ofNullable(createAisConsentResponse)
            .map(CreateAisConsentResponse::getConsentId)
            .orElse(null);
    }

    public void updateConsent(SpiAccountConsent consent) {
    }

    /**
     * Requests CMS to retrieve AIS consent by its identifier
     *
     * @param consentId String representation of identifier of stored consent
     * @return Response containing AIS Consent
     */
    public SpiAccountConsent getAccountConsentById(String consentId) {
        return consentRestTemplate.getForEntity(remoteAisConsentUrls.getAisConsentById(), SpiAccountConsent.class, consentId).getBody();
    }

    /**
     * Requests CMS to retrieve AIS consent status by its identifier
     *
     * @param consentId String representation of identifier of stored consent
     * @return Response containing AIS Consent Status
     */
    public SpiConsentStatus getAccountConsentStatusById(String consentId) {
        AisConsentStatusResponse response = consentRestTemplate.getForEntity(remoteAisConsentUrls.getAisConsentStatusById(), AisConsentStatusResponse.class, consentId).getBody();
        return consentMapper.mapToSpiConsentStatus(response.getConsentStatus())
            .orElse(null);
    }

    /**
     * Requests CMS to update consent status to "Revoked by PSU" state
     *
     * @param consentId String representation of identifier of stored consent
     */
    public void revokeConsent(String consentId) {
        consentRestTemplate.put(remoteAisConsentUrls.updateAisConsentStatus(), null, consentId, SpiConsentStatus.REVOKED_BY_PSU);
    }

    /**
     * Sends a POST request to CMS to perform decrement of consent usages and report status of the operation held with certain AIS consent
     *
     * @param tppId       String representation of TPP`s identifier from TPP Certificate
     * @param consentId   String representation of identifier of stored consent
     * @param withBalance Boolean representation of request to include Balances
     * @param access      Type of access initially requested(Access to Accounts/Balances/Transactions)
     * @param response    AIS Service response
     */
    public void consentActionLog(String tppId, String consentId, boolean withBalance, TypeAccess access, ResponseObject response) {
        ActionStatus status = response.hasError()
            ? consentMapper.mapActionStatusError(response.getError().getTppMessage().getMessageErrorCode(), withBalance, access)
            : ActionStatus.SUCCESS;

        consentRestTemplate.postForEntity(remoteAisConsentUrls.consentActionLog(), new ConsentActionRequest(tppId, consentId, status), Void.class);
    }

    /**
     * Sends a POST request to CMS to store created consent authorization
     *
     * @param consentId String representation of identifier of stored consent
     * @return long representation of identifier of stored consent authorization
     */
    public String createConsentAuthorization(String consentId, SpiScaStatus scaStatus) {
        CreateAisConsentAuthorizationResponse response = consentRestTemplate.postForEntity(remoteAisConsentUrls.createAisConsentAuthorization(),
            consentMapper.mapToAisConsentAuthorization(scaStatus), CreateAisConsentAuthorizationResponse.class, consentId).getBody();

        return Optional.ofNullable(response)
            .map(CreateAisConsentAuthorizationResponse::getAuthorizationId)
            .orElse(null);
    }

    /**
     * Requests CMS to retrieve AIS consent authorization by its identifier
     *
     * @param authorizationId String representation of identifier of stored consent authorization
     * @return Response containing AIS Consent Authorization
     */
    public SpiAccountConsentAuthorization getAccountConsentAuthorizationById(String authorizationId) {
        return consentRestTemplate.getForEntity(remoteAisConsentUrls.getAisConsentAuthorizationById(), SpiAccountConsentAuthorization.class, authorizationId).getBody();
    }

    /**
     * Sends a PUT request to CMS to update created AIS consent authorization
     *
     * @param updatePsuData Consent psu data
     */
    public UpdateConsentPsuDataResponse updateConsentAuthorization(UpdateConsentPsuDataReq updatePsuData, SpiAccountConsentAuthorization consentAuthorization) {
        return Optional.ofNullable(authorizationService.updateConsentPsuData(updatePsuData, consentAuthorization))
            .map(response -> {
                consentRestTemplate.put(remoteAisConsentUrls.updateAisConsentAuthorization(),
                    consentMapper.mapToAisConsentAuthorization(response), CreateAisConsentAuthorizationResponse.class);
                return response;
            })
            .orElse(null);
    }
}

