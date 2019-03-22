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
import de.adorsys.psd2.consent.api.ais.AisAccountAccessInfo;
import de.adorsys.psd2.consent.api.ais.AisConsentActionRequest;
import de.adorsys.psd2.consent.api.ais.AisConsentAuthorizationRequest;
import de.adorsys.psd2.consent.api.ais.CreateAisConsentRequest;
import de.adorsys.psd2.consent.api.service.AisConsentServiceEncrypted;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentAuthorisationMapper;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAuthenticationObjectToCmsScaMethodMapper;
import de.adorsys.psd2.xs2a.service.profile.FrequencyPerDateCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class Xs2aAisConsentService {
    private final AisConsentServiceEncrypted aisConsentService;
    private final Xs2aAisConsentMapper aisConsentMapper;
    private final Xs2aAisConsentAuthorisationMapper aisConsentAuthorisationMapper;
    private final Xs2aAuthenticationObjectToCmsScaMethodMapper xs2AAuthenticationObjectToCmsScaMethodMapper;
    private final FrequencyPerDateCalculationService frequencyPerDateCalculationService;
    private final ScaApproachResolver scaApproachResolver;

    /**
     * Sends a POST request to CMS to store created AISconsent
     *
     * @param request Request body storing main consent details
     * @param psuData PsuIdData container of authorisation data about PSU
     * @param tppInfo Information about particular TPP from TPP Certificate
     * @return String representation of identifier of stored consent
     */
    public String createConsent(CreateConsentReq request, PsuIdData psuData, TppInfo tppInfo) {
        int allowedFrequencyPerDay = frequencyPerDateCalculationService.getMinFrequencyPerDay(request.getFrequencyPerDay());
        CreateAisConsentRequest createAisConsentRequest = aisConsentMapper.mapToCreateAisConsentRequest(request, psuData, tppInfo, allowedFrequencyPerDay);
        Optional<String> consent = aisConsentService.createConsent(createAisConsentRequest);
        return consent.orElse(null);
    }

    /**
     * Requests CMS to retrieve AIS consent by its identifier
     *
     * @param consentId String representation of identifier of stored consent
     * @return Response containing AIS Consent
     */
    public Optional<AccountConsent> getAccountConsentById(String consentId) {
        return aisConsentService.getAisAccountConsentById(consentId)
                   .map(aisConsentMapper::mapToAccountConsent);
    }

    /**
     * Requests CMS to retrieve AIS consent by its identifier
     *
     * @param consentId String representation of identifier of stored consent
     * @return Response containing AIS Consent
     */
    public Optional<AccountConsent> getInitialAccountConsentById(String consentId) {
        return aisConsentService.getInitialAisAccountConsentById(consentId)
                   .map(aisConsentMapper::mapToAccountConsent);
    }

    /**
     * Requests CMS to retrieve AIS consent status by its identifier
     *
     * @param consentId String representation of identifier of stored consent
     * @return Response containing AIS Consent Status
     */
    public Optional<ConsentStatus> getAccountConsentStatusById(String consentId) {
        return aisConsentService.getConsentStatusById(consentId);
    }

    /**
     * Requests CMS to find old consents for current TPP and PSU and terminate them.
     *
     * @param newConsentId id of new consent
     * @return true if any consents have been terminated, false - if none
     */
    public boolean findAndTerminateOldConsentsByNewConsentId(String newConsentId) {
        return aisConsentService.findAndTerminateOldConsentsByNewConsentId(newConsentId);
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
     * @param scaStatus Enum for status of the SCA method applied
     * @param psuData   authorisation data about PSU
     * @return long representation of identifier of stored consent authorization
     */
    public Optional<String> createAisConsentAuthorization(String consentId, ScaStatus scaStatus, PsuIdData psuData) {
        AisConsentAuthorizationRequest request = aisConsentAuthorisationMapper.mapToAisConsentAuthorization(scaStatus, psuData, scaApproachResolver.resolveScaApproach());
        return aisConsentService.createAuthorization(consentId, request);
    }

    /**
     * Requests CMS to retrieve AIS consent authorization by its identifier
     *
     * @param authorizationId String representation of identifier of stored consent authorization
     * @param consentId       ID of the consent
     * @return Response containing AIS Consent Authorization
     */

    public Optional<AccountConsentAuthorization> getAccountConsentAuthorizationById(String authorizationId, String consentId) {
        return aisConsentService.getAccountConsentAuthorizationById(authorizationId, consentId)
                   .map(aisConsentAuthorisationMapper::mapToAccountConsentAuthorization);
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
    public void updateAspspAccountAccess(String consentId, AisAccountAccessInfo aisAccountAccessInfo) {
        aisConsentService.updateAspspAccountAccess(consentId, aisAccountAccessInfo);
    }

    /**
     * Requests CMS to retrieve AIS consent authorisation IDs by consent ID
     *
     * @param consentId String representation of identifier of stored consent
     * @return list of consent authorisation IDs
     */
    public Optional<List<String>> getAuthorisationSubResources(String consentId) {
        return aisConsentService.getAuthorisationsByConsentId(consentId);
    }

    /**
     * Requests CMS to retrieve SCA status of AIS consent authorisation
     *
     * @param consentId       String representation of consent identifier
     * @param authorisationId String representation of authorisation identifier
     * @return SCA status of the authorisation
     */
    public Optional<ScaStatus> getAuthorisationScaStatus(String consentId, String authorisationId) {
        return aisConsentService.getAuthorisationScaStatus(consentId, authorisationId);
    }

    /**
     * Requests CMS to retrieve authentication method and checks if requested authentication method is decoupled.
     *
     * @param authorisationId        String representation of the authorisation identifier
     * @param authenticationMethodId String representation of the available authentication method identifier
     * @return <code>true</code>, if authentication method is decoupled and <code>false</code> otherwise.
     */
    public boolean isAuthenticationMethodDecoupled(String authorisationId, String authenticationMethodId) {
        return aisConsentService.isAuthenticationMethodDecoupled(authorisationId, authenticationMethodId);
    }

    /**
     * Saves authentication methods in provided authorisation
     *
     * @param authorisationId String representation of the authorisation identifier
     * @param methods         List of authentication methods to be saved
     * @return <code>true</code> if authorisation was found and updated, <code>false</code> otherwise
     */
    public boolean saveAuthenticationMethods(String authorisationId, List<Xs2aAuthenticationObject> methods) {
        return aisConsentService.saveAuthenticationMethods(authorisationId, xs2AAuthenticationObjectToCmsScaMethodMapper.mapToCmsScaMethods(methods));
    }

    /**
     * Updates AIS SCA approach in authorisation
     *
     * @param authorisationId String representation of the authorisation identifier
     * @param scaApproach     Chosen SCA approach
     */
    public void updateScaApproach(String authorisationId, ScaApproach scaApproach) {
        aisConsentService.updateScaApproach(authorisationId, scaApproach);
    }

    /**
     * Updates multilevel SCA required field
     *
     * @param consentId             String representation of the consent identifier
     * @param multilevelScaRequired multilevel SCA required indicator
     */
    public void updateMultilevelScaRequired(String consentId, boolean multilevelScaRequired) {
        aisConsentService.updateMultilevelScaRequired(consentId, multilevelScaRequired);
    }
}
