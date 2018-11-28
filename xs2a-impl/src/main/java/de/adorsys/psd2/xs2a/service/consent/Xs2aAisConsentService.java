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

package de.adorsys.psd2.xs2a.service.consent;

import de.adorsys.psd2.consent.api.ActionStatus;
import de.adorsys.psd2.consent.api.ais.*;
import de.adorsys.psd2.consent.api.service.AisConsentService;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsentAuthorization;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentAuthorisationMapper;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.profile.FrequencyPerDateCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class Xs2aAisConsentService {
    private final AisConsentService aisConsentService;
    private final Xs2aAisConsentMapper aisConsentMapper;
    private final Xs2aAisConsentAuthorisationMapper aisConsentAuthorisationMapper;
    private final FrequencyPerDateCalculationService frequencyPerDateCalculationService;

    /**
     * Sends a POST request to CMS to store created AISconsent
     *
     * @param request Request body storing main consent details
     * @param psuData PsuIdData container of authorisation data about PSU
     * @param tppId   String representation of TPP`s identifier from TPP Certificate
     * @return String representation of identifier of stored consent
     */
    public String createConsent(CreateConsentReq request, PsuIdData psuData, String tppId) {
        int allowedFrequencyPerDay = frequencyPerDateCalculationService.getMinFrequencyPerDay(request.getFrequencyPerDay());
        CreateAisConsentRequest createAisConsentRequest = aisConsentMapper.mapToCreateAisConsentRequest(request, psuData, tppId, allowedFrequencyPerDay);
        Optional<String> consent = aisConsentService.createConsent(createAisConsentRequest);
        return consent.orElse(null);
    }

    /**
     * Requests CMS to retrieve AIS consent by its identifier
     *
     * @param consentId String representation of identifier of stored consent
     * @return Response containing AIS Consent
     */
    public AccountConsent getAccountConsentById(String consentId) {
        AisAccountConsent aisAccountConsent = aisConsentService.getAisAccountConsentById(consentId)
                                                  .orElse(null);
        return aisConsentMapper.mapToAccountConsent(aisAccountConsent);
    }

    /**
     * Requests CMS to retrieve AIS consent status by its identifier
     *
     * @param consentId String representation of identifier of stored consent
     * @return Response containing AIS Consent Status
     */
    public ConsentStatus getAccountConsentStatusById(String consentId) {
        return aisConsentService.getConsentStatusById(consentId)
                   .orElse(null);
    }

    /**
     * Requests CMS to update consent status to "Revoked by PSU" state
     *
     * @param consentId String representation of identifier of stored consent
     */
    public void revokeConsent(String consentId) {
        aisConsentService.updateConsentStatusById(consentId, ConsentStatus.REVOKED_BY_PSU);
    }

    /**
     * Requests CMS to update consent status into provided one
     *
     * @param consentId     String representation of identifier of stored consent
     * @param consentStatus ConsentStatus the consent be changed to
     */
    public void updateConsentStatus(String consentId, ConsentStatus consentStatus) {
        aisConsentService.updateConsentStatusById(consentId, consentStatus);
    }

    /**
     * Sends a POST request to CMS to perform decrement of consent usages and report status of the operation held with certain AIS consent
     *
     * @param tppId        String representation of TPP`s identifier from TPP Certificate
     * @param consentId    String representation of identifier of stored consent
     * @param actionStatus Enum value representing whether the acition is successful or errors occured
     */
    public void consentActionLog(String tppId, String consentId, ActionStatus actionStatus) {
        aisConsentService.checkConsentAndSaveActionLog(new AisConsentActionRequest(tppId, consentId, actionStatus));
    }

    /**
     * Sends a POST request to CMS to store created consent authorization
     *
     * @param consentId String representation of identifier of stored consent
     * @return long representation of identifier of stored consent authorization
     */
    public Optional<String> createAisConsentAuthorization(String consentId, ScaStatus scaStatus, PsuIdData psuData) {
        AisConsentAuthorizationRequest request = aisConsentAuthorisationMapper.mapToAisConsentAuthorization(scaStatus, psuData);
        return aisConsentService.createAuthorization(consentId, request);
    }

    /**
     * Requests CMS to retrieve AIS consent authorization by its identifier
     *
     * @param authorizationId String representation of identifier of stored consent authorization
     * @return Response containing AIS Consent Authorization
     */

    public AccountConsentAuthorization getAccountConsentAuthorizationById(String authorizationId, String consentId) {
        AisConsentAuthorizationResponse response = aisConsentService.getAccountConsentAuthorizationById(authorizationId, consentId)
                                                       .orElse(null);
        return aisConsentAuthorisationMapper.mapToAccountConsentAuthorization(response);
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

                aisConsentService.updateConsentAuthorization(authorizationId, request);
            });
    }

    /**
     * Sends a PUT request to CMS to update AIS account access information by consent ID
     *
     * @param consentId            consentId String representation of identifier of stored consent
     * @param aisAccountAccessInfo AIS account access information
     */
    public void updateAccountAccess(String consentId, AisAccountAccessInfo aisAccountAccessInfo) {
        aisConsentService.updateAccountAccess(consentId, aisAccountAccessInfo);
    }

    /**
     * Requests CMS to retrieve AIS consent authorisation IDs by consent ID
     *
     * @param consentId String representation of identifier of stored consent
     * @return list of consent authorisation IDs
     */
    public Optional<List<String>> getAuthorisationSubResources(String consentId) {
        return aisConsentService.getAuthorizationByConsentId(consentId);
    }
}
