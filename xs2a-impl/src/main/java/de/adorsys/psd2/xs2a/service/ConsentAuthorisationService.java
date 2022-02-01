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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationChainResponsibilityService;
import de.adorsys.psd2.xs2a.service.authorization.ConsentAuthorizationService;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.ais.AisAuthorisationConfirmationService;
import de.adorsys.psd2.xs2a.service.authorization.ais.AisScaAuthorisationServiceResolver;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AisAuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.event.EventAuthorisationType;
import de.adorsys.psd2.xs2a.service.event.EventTypeService;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.validator.ConsentEndpointAccessCheckerService;
import de.adorsys.psd2.xs2a.service.validator.ais.consent.dto.CreateConsentAuthorisationObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.core.error.ErrorType.*;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsentAuthorisationService {

    private final Xs2aAuthorisationService xs2aAuthorisationService;
    private final Xs2aAisConsentService aisConsentService;
    private final AisScaAuthorisationServiceResolver aisScaAuthorisationServiceResolver;
    private final ConsentEndpointAccessCheckerService endpointAccessCheckerService;
    private final Xs2aEventService xs2aEventService;
    private final ConsentValidationService consentValidationService;
    private final AuthorisationChainResponsibilityService authorisationChainResponsibilityService;
    private final LoggingContextService loggingContextService;
    private final AisAuthorisationConfirmationService aisAuthorisationConfirmationService;
    private final PsuIdDataAuthorisationService psuIdDataAuthorisationService;
    private final EventTypeService eventTypeService;
    private final ScaApproachResolver scaApproachResolver;

    public ResponseObject<AuthorisationResponse> createAisAuthorisation(PsuIdData psuData, String consentId, String password) {
        ResponseObject<CreateConsentAuthorizationResponse> createAisAuthorizationResponse = createConsentAuthorizationWithResponse(psuData, consentId);

        if (createAisAuthorizationResponse.hasError()) {
            return ResponseObject.<AuthorisationResponse>builder()
                       .fail(createAisAuthorizationResponse.getError())
                       .build();
        }

        PsuIdData psuIdDataFromResponse = createAisAuthorizationResponse.getBody().getPsuIdData();
        if (psuIdDataFromResponse == null || psuIdDataFromResponse.isEmpty() || StringUtils.isBlank(password)) {
            loggingContextService.storeScaStatus(createAisAuthorizationResponse.getBody().getScaStatus());
            return ResponseObject.<AuthorisationResponse>builder()
                       .body(createAisAuthorizationResponse.getBody())
                       .build();
        }

        String authorisationId = createAisAuthorizationResponse.getBody().getAuthorisationId();

        ConsentAuthorisationsParameters updatePsuData = new ConsentAuthorisationsParameters();
        updatePsuData.setPsuData(psuData);
        updatePsuData.setConsentId(consentId);
        updatePsuData.setAuthorizationId(authorisationId);
        updatePsuData.setPassword(password);

        ResponseObject<UpdateConsentPsuDataResponse> updatePsuDataResponse = updateConsentPsuData(updatePsuData);
        if (updatePsuDataResponse.hasError()) {
            return ResponseObject.<AuthorisationResponse>builder()
                       .fail(updatePsuDataResponse.getError())
                       .build();
        }

        return ResponseObject.<AuthorisationResponse>builder()
                   .body(updatePsuDataResponse.getBody())
                   .build();
    }

    public ResponseObject<Xs2aAuthorisationSubResources> getConsentInitiationAuthorisations(String consentId) {
        xs2aEventService.recordConsentTppRequest(consentId, EventType.GET_CONSENT_AUTHORISATION_REQUEST_RECEIVED);

        Optional<AisConsent> aisConsentOptional = aisConsentService.getAccountConsentById(consentId);
        if (aisConsentOptional.isEmpty()) {
            log.info("Consent-ID: [{}]. Get consent initiation authorisations failed: consent not found by id",
                     consentId);
            return ResponseObject.<Xs2aAuthorisationSubResources>builder()
                       .fail(AIS_403, of(CONSENT_UNKNOWN_403)).build();
        }
        AisConsent aisConsent = aisConsentOptional.get();

        ValidationResult validationResult = consentValidationService.validateConsentAuthorisationOnGettingById(aisConsent);
        if (validationResult.isNotValid()) {
            log.info("Consent-ID: [{}]. Get consent authorisations - validation failed: {}",
                     consentId, validationResult.getMessageError());
            return ResponseObject.<Xs2aAuthorisationSubResources>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        loggingContextService.storeConsentStatus(aisConsent.getConsentStatus());

        return getAuthorisationSubResources(consentId)
                   .map(resp -> ResponseObject.<Xs2aAuthorisationSubResources>builder().body(resp).build())
                   .orElseGet(() -> {
                       log.info("Consent-ID: [{}]. Get consent initiation authorisations failed: authorisation not found at CMS by consent id",
                                consentId);
                       return ResponseObject.<Xs2aAuthorisationSubResources>builder()
                                  .fail(AIS_404, of(RESOURCE_UNKNOWN_404))
                                  .build();
                   });
    }

    public ResponseObject<ConsentScaStatus> getConsentAuthorisationScaStatus(String consentId, String authorisationId) {
        xs2aEventService.recordConsentTppRequest(consentId, EventType.GET_CONSENT_SCA_STATUS_REQUEST_RECEIVED);

        Optional<AisConsent> aisConsentOptional = aisConsentService.getAccountConsentById(consentId);
        if (aisConsentOptional.isEmpty()) {
            log.info("Consent-ID: [{}]. Get consent authorisation SCA status failed: consent not found by id", consentId);
            return ResponseObject.<ConsentScaStatus>builder()
                       .fail(AIS_403, of(CONSENT_UNKNOWN_403)).build();
        }
        AisConsent accountConsent = aisConsentOptional.get();

        ValidationResult validationResult = consentValidationService.validateConsentAuthorisationScaStatus(accountConsent, authorisationId);
        if (validationResult.isNotValid()) {
            log.info("Consent-ID: [{}], Authorisation-ID [{}]. Get consent authorisation SCA status - validation failed: {}",
                     consentId, authorisationId, validationResult.getMessageError());
            return ResponseObject.<ConsentScaStatus>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        ConsentAuthorizationService authorizationService = aisScaAuthorisationServiceResolver.getService(authorisationId);
        Optional<ScaStatus> scaStatusOptional = authorizationService
                                                    .getAuthorisationScaStatus(consentId, authorisationId);

        if (scaStatusOptional.isEmpty()) {
            log.info("Consent-ID: [{}]. Get consent authorisation SCA status failed: consent not found at CMS by id",
                     consentId);
            return ResponseObject.<ConsentScaStatus>builder()
                       .fail(AIS_403, of(RESOURCE_UNKNOWN_403))
                       .build();
        }

        ScaStatus scaStatus = scaStatusOptional.get();

        PsuIdData psuIdData = psuIdDataAuthorisationService.getPsuIdData(authorisationId, accountConsent.getPsuIdDataList());

        ConsentScaStatus consentScaStatus = new ConsentScaStatus(psuIdData, accountConsent, scaStatus);

        loggingContextService.storeConsentStatus(accountConsent.getConsentStatus());
        loggingContextService.storeScaStatus(scaStatus);

        return ResponseObject.<ConsentScaStatus>builder()
                   .body(consentScaStatus)
                   .build();
    }

    public ResponseObject<UpdateConsentPsuDataResponse> updateConsentPsuData(ConsentAuthorisationsParameters updatePsuData) {
        xs2aEventService.recordConsentTppRequest(updatePsuData.getConsentId(), eventTypeService.getEventType(updatePsuData, EventAuthorisationType.AIS), updatePsuData);

        String consentId = updatePsuData.getConsentId();

        Optional<AisConsent> aisConsentOptional = aisConsentService.getAccountConsentById(consentId);

        if (aisConsentOptional.isEmpty()) {
            log.info("Consent-ID: [{}]. Update consent PSU data failed: consent not found by id", consentId);
            return ResponseObject.<UpdateConsentPsuDataResponse>builder()
                       .fail(AIS_403, of(CONSENT_UNKNOWN_403)).build();
        }

        String authorisationId = updatePsuData.getAuthorizationId();
        boolean confirmationCodeReceived = StringUtils.isNotBlank(updatePsuData.getConfirmationCode());

        if (!endpointAccessCheckerService.isEndpointAccessible(authorisationId, confirmationCodeReceived)) {
            log.info("Consent-ID: [{}], Authorisation-ID [{}]. Update consent PSU data failed: update endpoint is blocked for current authorisation",
                     consentId, authorisationId);
            return ResponseObject.<UpdateConsentPsuDataResponse>builder()
                       .fail(AIS_403, of(SERVICE_BLOCKED))
                       .build();
        }

        AisConsent aisConsent = aisConsentOptional.get();

        loggingContextService.storeConsentStatus(aisConsent.getConsentStatus());
        ValidationResult validationResult = consentValidationService.validateConsentPsuDataOnUpdate(aisConsent, updatePsuData);

        if (validationResult.isNotValid()) {
            MessageErrorCode messageErrorCode = validationResult.getMessageError().getTppMessage().getMessageErrorCode();

            if (EnumSet.of(PSU_CREDENTIALS_INVALID, FORMAT_ERROR_NO_PSU).contains(messageErrorCode)) {
                xs2aAuthorisationService.updateAuthorisationStatus(authorisationId, ScaStatus.FAILED);
            }

            log.info("Consent-ID: [{}], Authorisation-ID [{}]. Update consent PSU data - validation failed: {}",
                     consentId, authorisationId, validationResult.getMessageError());
            return ResponseObject.<UpdateConsentPsuDataResponse>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        if (aisConsent.isExpired()) {
            log.info("Consent-ID: [{}]. Update consent PSU data failed: consent expired", consentId);
            return ResponseObject.<UpdateConsentPsuDataResponse>builder()
                       .fail(AIS_401, of(CONSENT_EXPIRED))
                       .build();
        }

        return getUpdateConsentPsuDataResponse(updatePsuData);
    }

    private ResponseObject<UpdateConsentPsuDataResponse> getUpdateConsentPsuDataResponse(ConsentAuthorisationsParameters updatePsuData) {
        ConsentAuthorizationService service = aisScaAuthorisationServiceResolver.getService(updatePsuData.getAuthorizationId());

        Optional<Authorisation> authorizationOptional = service.getConsentAuthorizationById(updatePsuData.getAuthorizationId());

        if (authorizationOptional.isEmpty()) {
            log.info("Authorisation-ID: [{}]. Update consent PSU data failed: authorisation not found by id",
                     updatePsuData.getAuthorizationId());
            return ResponseObject.<UpdateConsentPsuDataResponse>builder()
                       .fail(AIS_403, of(CONSENT_UNKNOWN_403)).build();
        }

        Authorisation authorisation = authorizationOptional.get();

        if (authorisation.getChosenScaApproach() == ScaApproach.REDIRECT) {
            return aisAuthorisationConfirmationService.processAuthorisationConfirmation(updatePsuData);
        }

        UpdateConsentPsuDataResponse response = (UpdateConsentPsuDataResponse) authorisationChainResponsibilityService.apply(
            new AisAuthorisationProcessorRequest(authorisation.getChosenScaApproach(),
                                                 authorisation.getScaStatus(),
                                                 updatePsuData,
                                                 authorisation));
        loggingContextService.storeScaStatus(response.getScaStatus());

        return Optional.ofNullable(response)
                   .map(s -> Optional.ofNullable(s.getErrorHolder())
                                 .map(e -> ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                               .fail(e)
                                               .build())
                                 .orElseGet(ResponseObject.<UpdateConsentPsuDataResponse>builder().body(response)::build))
                   .orElseGet(ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                  .fail(AIS_400, of(FORMAT_ERROR))
                                  ::build);
    }

    private ResponseObject<CreateConsentAuthorizationResponse> createConsentAuthorizationWithResponse(PsuIdData psuDataFromRequest, String consentId) {
        xs2aEventService.recordConsentTppRequest(consentId, EventType.START_AIS_CONSENT_AUTHORISATION_REQUEST_RECEIVED);

        Optional<AisConsent> aisConsentOptional = aisConsentService.getAccountConsentById(consentId);

        if (aisConsentOptional.isEmpty()) {
            log.info("Consent-ID: [{}]. Create consent authorisation with response failed: consent not found by id",
                     consentId);
            return ResponseObject.<CreateConsentAuthorizationResponse>builder()
                       .fail(AIS_403, of(CONSENT_UNKNOWN_403)).build();
        }
        AisConsent aisConsent = aisConsentOptional.get();

        ValidationResult validationResult = consentValidationService.validateConsentAuthorisationOnCreate(new CreateConsentAuthorisationObject(aisConsent, psuDataFromRequest));

        if (validationResult.isNotValid()) {
            log.info("Consent-ID: [{}]. Create consent authorisation with response - validation failed: {}",
                     consentId, validationResult.getMessageError());
            return ResponseObject.<CreateConsentAuthorizationResponse>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        if (aisConsent.isExpired()) {
            log.info("Consent-ID: [{}]. Create consent authorisation with response failed: consent expired", consentId);
            return ResponseObject.<CreateConsentAuthorizationResponse>builder()
                       .fail(AIS_401, of(CONSENT_EXPIRED))
                       .build();
        }

        loggingContextService.storeConsentStatus(aisConsent.getConsentStatus());

        PsuIdData psuIdData = getActualPsuData(psuDataFromRequest, aisConsent);
        // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/-/issues/1629
        ScaStatus scaStatus = ScaStatus.STARTED;
        String authorisationId = UUID.randomUUID().toString();
        ScaApproach scaApproach = scaApproachResolver.resolveScaApproach();
        StartAuthorisationsParameters startAuthorisationsParameters = StartAuthorisationsParameters.builder()
                                                                          .psuData(psuIdData)
                                                                          .businessObjectId(consentId)
                                                                          .scaStatus(scaStatus)
                                                                          .authorisationId(authorisationId)
                                                                          .build();

        Authorisation authorisation = new Authorisation(authorisationId, psuIdData, consentId, AuthorisationType.CONSENT, scaStatus);
        AisAuthorisationProcessorRequest processorRequest = new AisAuthorisationProcessorRequest(scaApproach, scaStatus, startAuthorisationsParameters, authorisation);
        CreateConsentAuthorisationProcessorResponse processorResponse =
            (CreateConsentAuthorisationProcessorResponse) authorisationChainResponsibilityService.apply(processorRequest);

        Xs2aCreateAuthorisationRequest createAuthorisationRequest = Xs2aCreateAuthorisationRequest.builder()
                                                                        .psuData(psuIdData)
                                                                        .consentId(consentId)
                                                                        .authorisationId(authorisationId)
                                                                        .scaStatus(processorResponse.getScaStatus())
                                                                        .scaApproach(processorResponse.getScaApproach())
                                                                        .build();

        ConsentAuthorizationService service = aisScaAuthorisationServiceResolver.getService();
        Optional<CreateConsentAuthorizationResponse> consentAuthorizationResponse = service.createConsentAuthorization(createAuthorisationRequest);

        if (consentAuthorizationResponse.isEmpty()) {
            return ResponseObject.<CreateConsentAuthorizationResponse>builder()
                       .fail(AIS_403, of(CONSENT_UNKNOWN_403))
                       .build();
        }
        CreateConsentAuthorizationResponse createConsentAuthorizationResponse = consentAuthorizationResponse.get();

        setPsuMessageAndTppMessages(createConsentAuthorizationResponse,
                                    processorResponse.getPsuMessage(),
                                    processorResponse.getTppMessages());

        return ResponseObject.<CreateConsentAuthorizationResponse>builder()
                   .body(createConsentAuthorizationResponse)
                   .build();
    }

    private void setPsuMessageAndTppMessages(AuthorisationResponse response,
                                             String psuMessage, Set<TppMessageInformation> tppMessageInformationSet) {
        if (psuMessage != null) {
            response.setPsuMessage(psuMessage);
        }
        if (tppMessageInformationSet != null) {
            response.getTppMessageInformation().addAll(tppMessageInformationSet);
        }
    }

    private PsuIdData getActualPsuData(PsuIdData psuDataFromRequest, AisConsent aisConsent) {
        boolean isMultilevel = aisConsent.isMultilevelScaRequired();

        if (psuDataFromRequest.isNotEmpty() || isMultilevel) {
            return psuDataFromRequest;
        }

        return aisConsent.getPsuIdDataList().stream()
                   .findFirst()
                   .orElse(psuDataFromRequest);
    }

    private Optional<Xs2aAuthorisationSubResources> getAuthorisationSubResources(String consentId) {
        return aisConsentService.getAuthorisationSubResources(consentId)
                   .map(Xs2aAuthorisationSubResources::new);
    }
}
