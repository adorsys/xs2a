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

package de.adorsys.psd2.xs2a.service.consent;

import de.adorsys.psd2.consent.api.ActionStatus;
import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.WrongChecksumException;
import de.adorsys.psd2.consent.api.ais.AisConsentActionRequest;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.consent.api.consent.CmsCreateConsentResponse;
import de.adorsys.psd2.consent.api.service.AisConsentServiceEncrypted;
import de.adorsys.psd2.consent.api.service.ConsentServiceEncrypted;
import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.TerminateOldConsentsRequest;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.Xs2aResponse;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCreateAisConsentResponse;
import de.adorsys.psd2.xs2a.domain.consent.ConsentAuthorisationsParameters;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.psd2.xs2a.service.CmsCreateConsentResponseService;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aConsentAuthorisationMapper;
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
    private final Xs2aAuthorisationService authorisationService;
    private final Xs2aAisConsentMapper aisConsentMapper;
    private final Xs2aConsentAuthorisationMapper consentAuthorisationMapper;
    private final FrequencyPerDateCalculationService frequencyPerDateCalculationService;
    private final LoggingContextService loggingContextService;
    private final CmsCreateConsentResponseService cmsCreateConsentResponseService;

    /**
     * Sends a POST request to CMS to store created AIS consent
     *
     * @param request Request body storing main consent details
     * @param psuData PsuIdData container of authorisation data about PSU
     * @param tppInfo Information about particular TPP from TPP Certificate
     * @return create consent response, containing consent and its encrypted ID
     */
    public Xs2aResponse<Xs2aCreateAisConsentResponse> createConsent(CreateConsentReq request, PsuIdData psuData, TppInfo tppInfo) {
        int allowedFrequencyPerDay = frequencyPerDateCalculationService.getMinFrequencyPerDay(request.getFrequencyPerDay());
        CmsConsent cmsConsent = aisConsentMapper.mapToCmsConsent(request, psuData, tppInfo, allowedFrequencyPerDay);

        Xs2aResponse<CmsCreateConsentResponse> createConsentResponse = cmsCreateConsentResponseService.getCmsCreateConsentResponse(cmsConsent);

        Xs2aCreateAisConsentResponse xs2aCreateAisConsentResponse = Optional.ofNullable(createConsentResponse.getPayload())
                                                                        .map(c -> new Xs2aCreateAisConsentResponse(c.getConsentId(),
                                                                                                                   aisConsentMapper.mapToAisConsent(c.getCmsConsent()),
                                                                                                                   c.getCmsConsent().getTppInformation().getTppNotificationSupportedModes()))
                                                                        .orElse(null);

        return Xs2aResponse.<Xs2aCreateAisConsentResponse>builder()
                   .payload(xs2aCreateAisConsentResponse)
                   .build();
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
    public boolean findAndTerminateOldConsents(String newConsentId, TerminateOldConsentsRequest request) {
        CmsResponse<Boolean> response = consentService.findAndTerminateOldConsents(newConsentId, request);
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
     * @param tppId         String representation of TPP`s identifier from TPP Certificate
     * @param consentId     String representation of identifier of stored consent
     * @param actionStatus  Enum value representing whether the action is successful or errors occurred
     * @param requestUri    target URL of the request
     * @param updateUsage   Update usage indicator
     * @param resourceId    The identification that denotes the addressed account
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
     * Sends a PUT request to CMS to update created AIS consent authorisation
     *
     * @param updatePsuData Consent PSU data
     */
    public void updateConsentAuthorisation(ConsentAuthorisationsParameters updatePsuData) {
        Optional.ofNullable(updatePsuData)
            .ifPresent(req -> {
                final UpdateAuthorisationRequest request = consentAuthorisationMapper.mapToAuthorisationRequest(req);

                authorisationService.updateAuthorisation(request, req.getAuthorizationId());
            });
    }

    /**
     * Sends a PUT request to CMS to update AIS account access information by consent ID
     *
     * @param consentId     consentId String representation of identifier of stored consent
     * @param accountAccess AIS account access information
     * @return Response containing AIS Consent
     */
    public CmsResponse<AisConsent> updateAspspAccountAccess(String consentId, AccountAccess accountAccess) {
        CmsResponse<CmsConsent> response;

        CmsResponse.CmsResponseBuilder<AisConsent> builder = CmsResponse.builder();

        try {
            response = aisConsentService.updateAspspAccountAccess(consentId, accountAccess);
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
        return authorisationService.getAuthorisationSubResources(consentId, AuthorisationType.CONSENT);
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
}
