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

package de.adorsys.psd2.xs2a.service.consent;

import de.adorsys.psd2.consent.api.ActionStatus;
import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.WrongChecksumException;
import de.adorsys.psd2.consent.api.ais.AisAccountAccessInfo;
import de.adorsys.psd2.consent.api.ais.AisConsentActionRequest;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.authorisation.AisAuthorisationParentHolder;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationRequest;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationResponse;
import de.adorsys.psd2.consent.api.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.consent.api.consent.CmsCreateConsentResponse;
import de.adorsys.psd2.consent.api.service.AisConsentServiceEncrypted;
import de.adorsys.psd2.consent.api.service.AuthorisationServiceEncrypted;
import de.adorsys.psd2.consent.api.service.ConsentServiceEncrypted;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCreateAisConsentResponse;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentAuthorisationMapper;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAuthenticationObjectToCmsScaMethodMapper;
import de.adorsys.psd2.xs2a.service.profile.FrequencyPerDateCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class Xs2aAisConsentService {
    private final ConsentServiceEncrypted consentService;
    private final AisConsentServiceEncrypted aisConsentService;
    // ToDo move authorisation-related methods out of the service https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/1198
    private final AuthorisationServiceEncrypted authorisationServiceEncrypted;
    private final Xs2aAisConsentMapper aisConsentMapper;
    private final Xs2aAisConsentAuthorisationMapper aisConsentAuthorisationMapper;
    private final Xs2aAuthenticationObjectToCmsScaMethodMapper xs2AAuthenticationObjectToCmsScaMethodMapper;
    private final FrequencyPerDateCalculationService frequencyPerDateCalculationService;
    private final ScaApproachResolver scaApproachResolver;
    private final RequestProviderService requestProviderService;
    private final LoggingContextService loggingContextService;

    /**
     * Sends a POST request to CMS to store created AIS consent
     *
     * @param request Request body storing main consent details
     * @param psuData PsuIdData container of authorisation data about PSU
     * @param tppInfo Information about particular TPP from TPP Certificate
     * @return create consent response, containing consent and its encrypted ID
     */
    public Optional<Xs2aCreateAisConsentResponse> createConsent(CreateConsentReq request, PsuIdData psuData, TppInfo tppInfo) {
        int allowedFrequencyPerDay = frequencyPerDateCalculationService.getMinFrequencyPerDay(request.getFrequencyPerDay());
        CmsConsent cmsConsent = aisConsentMapper.mapToCmsConsent(request, psuData, tppInfo, allowedFrequencyPerDay);

        CmsResponse<CmsCreateConsentResponse> response;
        try {
            response = consentService.createConsent(cmsConsent);
        } catch (WrongChecksumException e) {
            log.info("Consent cannot be created, checksum verification failed");
            return Optional.empty();
        }

        if (response.hasError()) {
            log.info("Consent cannot be created, because can't save to cms DB");
            return Optional.empty();
        }

        CmsCreateConsentResponse createConsentResponse = response.getPayload();
        return Optional.of(new Xs2aCreateAisConsentResponse(createConsentResponse.getConsentId(),
                                                            aisConsentMapper.mapToAisConsent(createConsentResponse.getCmsConsent()),
                                                            createConsentResponse.getCmsConsent().getTppInformation().getTppNotificationSupportedModes()));
    }

    /**
     * Requests CMS to retrieve AIS consent by its identifier
     *
     * @param consentId String representation of identifier of stored consent
     * @return Response containing AIS Consent
     */
    public Optional<AisConsent> getAccountConsentById(String consentId) {
        CmsResponse<CmsConsent> consentById = consentService.getConsentById(consentId);

        if (consentById.hasError()) {
            log.info("Get consent by id failed due to CMS problems");
            return Optional.empty();
        }

        return Optional.ofNullable(aisConsentMapper.mapToAisConsent(consentById.getPayload()));
    }

    /**
     * Requests CMS to find old consents for current TPP and PSU and terminate them.
     *
     * @param newConsentId id of new consent
     * @return true if any consents have been terminated, false - if none
     */
    public boolean findAndTerminateOldConsentsByNewConsentId(String newConsentId) {
        CmsResponse<Boolean> response = consentService.findAndTerminateOldConsentsByNewConsentId(newConsentId);
        return response.isSuccessful() && response.getPayload();
    }

    /**
     * Requests CMS to update consent status into provided one
     *
     * @param consentId     String representation of identifier of stored consent
     * @param consentStatus ConsentStatus the consent be changed to
     */
    public void updateConsentStatus(String consentId, ConsentStatus consentStatus) {
        CmsResponse<Boolean> statusUpdated;

        try {
            statusUpdated = consentService.updateConsentStatusById(consentId, consentStatus);
        } catch (WrongChecksumException e) {
            log.info("updateConsentStatus cannot be executed, checksum verification failed");
            return;
        }

        if (statusUpdated.isSuccessful() && BooleanUtils.isTrue(statusUpdated.getPayload())) {
            loggingContextService.storeConsentStatus(consentStatus);
        }
    }

    /**
     * Sends a POST request to CMS to perform decrement of consent usages and report status of the operation held with certain AIS consent
     *
     * @param tppId        String representation of TPP`s identifier from TPP Certificate
     * @param consentId    String representation of identifier of stored consent
     * @param actionStatus Enum value representing whether the action is successful or errors occurred
     * @param requestUri   target URL of the request
     * @param updateUsage  Update usage indicator
     * @param resourceId   The identification that denotes the addressed account
     * @param transactionId String representation of ASPSP transaction primary identifier
     */
    public void consentActionLog(String tppId, String consentId, ActionStatus actionStatus, String requestUri, boolean updateUsage,
                                 String resourceId, String transactionId) {
        try {
            aisConsentService.checkConsentAndSaveActionLog(new AisConsentActionRequest(tppId, consentId, actionStatus, requestUri, updateUsage, resourceId, transactionId));
        } catch (WrongChecksumException e) {
            log.info("consentActionLog cannot be executed, checksum verification failed");
        }
    }

    /**
     * Sends a POST request to CMS to store created consent authorisation
     *
     * @param consentId String representation of identifier of stored consent
     * @param scaStatus Enum for status of the SCA method applied
     * @param psuData   authorisation data about PSU
     * @return CreateAisConsentAuthorizationResponse object with authorisation ID and scaStatus
     */
    public Optional<CreateAuthorisationResponse> createAisConsentAuthorization(String consentId, ScaStatus scaStatus, PsuIdData psuData) {
        String tppRedirectURI = requestProviderService.getTppRedirectURI();
        String tppNOKRedirectURI = requestProviderService.getTppNokRedirectURI();
        CreateAuthorisationRequest request = aisConsentAuthorisationMapper.mapToAuthorisationRequest(scaStatus, psuData, scaApproachResolver.resolveScaApproach(), tppRedirectURI, tppNOKRedirectURI);
        CmsResponse<CreateAuthorisationResponse> authorisationResponse = authorisationServiceEncrypted.createAuthorisation(new AisAuthorisationParentHolder(consentId), request);

        if (authorisationResponse.hasError()) {
            return Optional.empty();
        }

        return Optional.ofNullable(authorisationResponse.getPayload());
    }

    /**
     * Requests CMS to retrieve AIS consent authorisation by its identifier
     *
     * @param authorizationId String representation of identifier of stored consent authorisation
     * @return Response containing AIS Consent Authorisation
     */

    public Optional<Authorisation> getAccountConsentAuthorizationById(String authorizationId) {
        CmsResponse<Authorisation> authorisationResponse = authorisationServiceEncrypted.getAuthorisationById(authorizationId);

        if (authorisationResponse.hasError()) {
            return Optional.empty();
        }

        return Optional.ofNullable(authorisationResponse.getPayload());
    }

    /**
     * Sends a PUT request to CMS to update created AIS consent authorisation
     *
     * @param updatePsuData Consent PSU data
     */
    public void updateConsentAuthorization(UpdateConsentPsuDataReq updatePsuData) {
        Optional.ofNullable(updatePsuData)
            .ifPresent(req -> {
                final String authorizationId = req.getAuthorizationId();
                final UpdateAuthorisationRequest request = aisConsentAuthorisationMapper.mapToAuthorisationRequest(req);

                authorisationServiceEncrypted.updateAuthorisation(authorizationId, request);
            });
    }

    /**
     * Sends a PUT request to CMS to update status in consent authorisation
     *
     * @param authorisationId String representation of authorisation identifier
     * @param scaStatus       Enum for status of the SCA method applied
     */
    public void updateConsentAuthorisationStatus(String authorisationId, ScaStatus scaStatus) {
        authorisationServiceEncrypted.updateAuthorisationStatus(authorisationId, scaStatus);
    }

    /**
     * Sends a PUT request to CMS to update AIS account access information by consent ID
     *
     * @param consentId            consentId String representation of identifier of stored consent
     * @param aisAccountAccessInfo AIS account access information
     * @return Response containing AIS Consent
     */
    public CmsResponse<AisConsent> updateAspspAccountAccess(String consentId, AisAccountAccessInfo aisAccountAccessInfo) {
        CmsResponse<CmsConsent> response;

        CmsResponse.CmsResponseBuilder<AisConsent> builder = CmsResponse.builder();

        try {
            response = aisConsentService.updateAspspAccountAccess(consentId, aisAccountAccessInfo);
        } catch (WrongChecksumException e) {
            return builder.error(CmsError.CHECKSUM_ERROR).build();
        }

        if (response.hasError()) {
            return builder.error(response.getError()).build();
        }

        return builder.payload(aisConsentMapper.mapToAisConsent(response.getPayload())).build();
    }

    /**
     * Requests CMS to retrieve AIS consent authorisation IDs by consent ID
     *
     * @param consentId String representation of identifier of stored consent
     * @return list of consent authorisation IDs
     */
    public Optional<List<String>> getAuthorisationSubResources(String consentId) {
        CmsResponse<List<String>> response = authorisationServiceEncrypted.getAuthorisationsByParentId(new AisAuthorisationParentHolder(consentId));

        if (response.hasError()) {
            return Optional.empty();
        }

        return Optional.ofNullable(response.getPayload());
    }

    /**
     * Requests CMS to retrieve SCA status of AIS consent authorisation
     *
     * @param consentId       String representation of consent identifier
     * @param authorisationId String representation of authorisation identifier
     * @return SCA status of the authorisation
     */
    public Optional<ScaStatus> getAuthorisationScaStatus(String consentId, String authorisationId) {
        CmsResponse<ScaStatus> response = authorisationServiceEncrypted.getAuthorisationScaStatus(authorisationId, new AisAuthorisationParentHolder(consentId));

        if (response.hasError()) {
            return Optional.empty();
        }

        return Optional.ofNullable(response.getPayload());
    }

    /**
     * Requests CMS to retrieve authentication method and checks if requested authentication method is decoupled.
     *
     * @param authorisationId        String representation of the authorisation identifier
     * @param authenticationMethodId String representation of the available authentication method identifier
     * @return <code>true</code>, if authentication method is decoupled and <code>false</code> otherwise.
     */
    public boolean isAuthenticationMethodDecoupled(String authorisationId, String authenticationMethodId) {
        CmsResponse<Boolean> response = authorisationServiceEncrypted.isAuthenticationMethodDecoupled(authorisationId, authenticationMethodId);
        return response.isSuccessful() && response.getPayload();
    }

    /**
     * Saves authentication methods in provided authorisation
     *
     * @param authorisationId String representation of the authorisation identifier
     * @param methods         List of authentication methods to be saved
     * @return <code>true</code> if authorisation was found and updated, <code>false</code> otherwise
     */
    public boolean saveAuthenticationMethods(String authorisationId, List<AuthenticationObject> methods) {
        CmsResponse<Boolean> response = authorisationServiceEncrypted.saveAuthenticationMethods(authorisationId, xs2AAuthenticationObjectToCmsScaMethodMapper.mapToCmsScaMethods(methods));
        return response.isSuccessful() && response.getPayload();
    }

    /**
     * Updates AIS SCA approach in authorisation
     *
     * @param authorisationId String representation of the authorisation identifier
     * @param scaApproach     Chosen SCA approach
     */
    public void updateScaApproach(String authorisationId, ScaApproach scaApproach) {
        authorisationServiceEncrypted.updateScaApproach(authorisationId, scaApproach);
    }

    /**
     * Updates multilevel SCA required field
     *
     * @param consentId             String representation of the consent identifier
     * @param multilevelScaRequired multilevel SCA required indicator
     */
    public void updateMultilevelScaRequired(String consentId, boolean multilevelScaRequired) {
        try {
            consentService.updateMultilevelScaRequired(consentId, multilevelScaRequired);
        } catch (WrongChecksumException e) {
            log.info("updateMultilevelScaRequired cannot be executed, checksum verification failed");
        }
    }

    /**
     * Gets SCA approach from the authorisation
     *
     * @param authorisationId String representation of the authorisation identifier
     * @return SCA approach
     */
    public Optional<AuthorisationScaApproachResponse> getAuthorisationScaApproach(String authorisationId) {
        CmsResponse<AuthorisationScaApproachResponse> response = authorisationServiceEncrypted.getAuthorisationScaApproach(authorisationId);

        if (response.hasError()) {
            return Optional.empty();
        }

        return Optional.ofNullable(response.getPayload());
    }
}
