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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationMethodDecider;
import de.adorsys.psd2.xs2a.service.authorization.ais.AisAuthorizationService;
import de.adorsys.psd2.xs2a.service.authorization.ais.AisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.ais.AisScaAuthorisationServiceResolver;
import de.adorsys.psd2.xs2a.service.consent.AccountReferenceInConsentUpdater;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAccountAccessMapper;
import de.adorsys.psd2.xs2a.service.spi.InitialSpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.service.validator.AisEndpointAccessCheckerService;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.ais.CommonConsentObject;
import de.adorsys.psd2.xs2a.service.validator.ais.consent.*;
import de.adorsys.psd2.xs2a.service.validator.ais.consent.dto.CreateConsentRequestObject;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiAisConsentStatusResponse;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiInitiateAisConsentResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse.VoidResponse;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.*;

@Slf4j
@Service
@RequiredArgsConstructor
//TODO Refactor Service: split responsibilities https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/569
public class ConsentService {
    private final Xs2aAisConsentMapper aisConsentMapper;
    private final SpiToXs2aAccountAccessMapper spiToXs2aAccountAccessMapper;
    private final Xs2aAisConsentService aisConsentService;
    private final AisScaAuthorisationServiceResolver aisScaAuthorisationServiceResolver;
    private final TppService tppService;
    private final AisEndpointAccessCheckerService endpointAccessCheckerService;
    private final SpiContextDataProvider spiContextDataProvider;
    private final AuthorisationMethodDecider authorisationMethodDecider;
    private final AisConsentSpi aisConsentSpi;
    private final Xs2aEventService xs2aEventService;
    private final AccountReferenceInConsentUpdater accountReferenceUpdater;
    private final SpiErrorMapper spiErrorMapper;

    private final CreateConsentRequestValidator createConsentRequestValidator;
    private final GetAccountConsentsStatusByIdValidator getAccountConsentsStatusByIdValidator;
    private final GetAccountConsentByIdValidator getAccountConsentByIdValidator;
    private final DeleteAccountConsentsByIdValidator deleteAccountConsentsByIdValidator;
    private final CreateConsentAuthorisationValidator createConsentAuthorisationValidator;
    private final UpdateConsentPsuDataValidator updateConsentPsuDataValidator;
    private final GetConsentAuthorisationsValidator getConsentAuthorisationsValidator;
    private final GetConsentAuthorisationScaStatusValidator getConsentAuthorisationScaStatusValidator;
    private final AisScaAuthorisationService aisScaAuthorisationService;
    private final RequestProviderService requestProviderService;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;

    /**
     * Performs create consent operation either by filling the appropriate AccountAccess fields with corresponding
     * account details or by getting account details from ASPSP by psuId and filling the appropriate fields in
     * AccountAccess determined by availableAccounts or allPsd2 variables
     *
     * @param request           body of create consent request carrying such parameters as AccountAccess, validity terms etc.
     * @param psuData           PsuIdData container of authorisation data about PSU
     * @param explicitPreferred is TPP explicit authorisation preferred
     * @param tppRedirectUri    URI for redirect SCA approach
     * @return CreateConsentResponse representing the complete response to create consent request
     */
    public ResponseObject<CreateConsentResponse> createAccountConsentsWithResponse(CreateConsentReq request, PsuIdData psuData, boolean explicitPreferred, TppRedirectUri tppRedirectUri) { // NOPMD // TODO we need to refactor this method and class. https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/749
        xs2aEventService.recordTppRequest(EventType.CREATE_AIS_CONSENT_REQUEST_RECEIVED, request);

        ValidationResult validationResult = createConsentRequestValidator.validate(new CreateConsentRequestObject(request, psuData));
        if (validationResult.isNotValid()) {
            log.info("InR-ID: [{}], X-Request-ID: [{}]. Create account consent with response - validation failed: {}",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), validationResult.getMessageError());
            return ResponseObject.<CreateConsentResponse>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        if (request.isGlobalOrAllAccountsAccessConsent()) {
            request.setAccess(getAccessForGlobalOrAllAvailableAccountsConsent(request));
        }

        TppInfo tppInfo = tppService.getTppInfo();
        tppInfo.setTppRedirectUri(tppRedirectUri);

        String consentId = aisConsentService.createConsent(request, psuData, tppInfo);

        if (StringUtils.isBlank(consentId)) {
            return ResponseObject.<CreateConsentResponse>builder()
                       .fail(AIS_400, of(RESOURCE_UNKNOWN_400))
                       .build();
        }

        Optional<AccountConsent> accountConsentOptional = aisConsentService.getInitialAccountConsentById(consentId);
        // TODO we need to refactor this method and class. https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/749
        if (!accountConsentOptional.isPresent()) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Consent-ID: [{}]. Create account consent  with response failed: Actual consent not found by id",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), consentId);
            return ResponseObject.<CreateConsentResponse>builder()
                       .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                       .build();
        }

        SpiContextData contextData = spiContextDataProvider.provide(psuData, tppInfo);
        InitialSpiAspspConsentDataProvider aspspConsentDataProvider = aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider();
        aspspConsentDataProvider.saveWith(consentId);

        SpiResponse<SpiInitiateAisConsentResponse> initiateAisConsentSpiResponse = aisConsentSpi.initiateAisConsent(contextData, aisConsentMapper.mapToSpiAccountConsent(accountConsentOptional.get()), aspspConsentDataProvider);

        if (initiateAisConsentSpiResponse.hasError()) {
            aisConsentService.updateConsentStatus(consentId, ConsentStatus.REJECTED);
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(initiateAisConsentSpiResponse, ServiceType.AIS);
            log.info("InR-ID: [{}], X-Request-ID: [{}], Consent-ID: [{}]. Create account consent  with response failed. Consent rejected. Couldn't initiate AIS consent at SPI level: {}",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), consentId, errorHolder);
            return ResponseObject.<CreateConsentResponse>builder()
                       .fail(new MessageError(errorHolder))
                       .build();
        }

        SpiInitiateAisConsentResponse spiResponsePayload = initiateAisConsentSpiResponse.getPayload();
        boolean multilevelScaRequired = spiResponsePayload.isMultilevelScaRequired()
                                            && !aisScaAuthorisationService.isOneFactorAuthorisation(request.isConsentForAllAvailableAccounts(), request.isOneAccessType());

        updateMultilevelSca(consentId, multilevelScaRequired);

        Optional<Xs2aAccountAccess> xs2aAccountAccess = spiToXs2aAccountAccessMapper.mapToAccountAccess(spiResponsePayload.getAccountAccess());
        xs2aAccountAccess.ifPresent(accountAccess ->
                                        accountReferenceUpdater.rewriteAccountAccess(consentId, accountAccess));

        CreateConsentResponse createConsentResponse = new CreateConsentResponse(ConsentStatus.RECEIVED.getValue(), consentId, null, null, null, spiResponsePayload.getPsuMessage(), multilevelScaRequired);
        ResponseObject<CreateConsentResponse> createConsentResponseObject = ResponseObject.<CreateConsentResponse>builder().body(createConsentResponse).build();

        if (authorisationMethodDecider.isImplicitMethod(explicitPreferred, multilevelScaRequired)) {
            proceedImplicitCaseForCreateConsent(createConsentResponseObject.getBody(), psuData, consentId);
        }

        return createConsentResponseObject;
    }

    private void updateMultilevelSca(String consentId, boolean multilevelScaRequired) {
        // default value is false, so we do the call only for non-default (true) case
        if (multilevelScaRequired) {
            aisConsentService.updateMultilevelScaRequired(consentId, multilevelScaRequired);
        }
    }

    /**
     * Returns status of requested consent
     *
     * @param consentId String representation of AccountConsent identification
     * @return ConsentStatus
     */
    public ResponseObject<ConsentStatusResponse> getAccountConsentsStatusById(String consentId) {
        xs2aEventService.recordAisTppRequest(consentId, EventType.GET_AIS_CONSENT_STATUS_REQUEST_RECEIVED);
        ResponseObject.ResponseBuilder<ConsentStatusResponse> responseBuilder = ResponseObject.builder();

        Optional<AccountConsent> validatedAccountConsentOptional = aisConsentService.getAccountConsentById(consentId);

        if (!validatedAccountConsentOptional.isPresent()) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Consent-ID: [{}]. Get account consents status failed: consent not found by id",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), consentId);
            return responseBuilder
                       .fail(AIS_403, of(CONSENT_UNKNOWN_403))
                       .build();
        }

        AccountConsent validatedAccountConsent = validatedAccountConsentOptional.get();

        ValidationResult validationResult = getAccountConsentsStatusByIdValidator.validate(new CommonConsentObject(validatedAccountConsent));
        if (validationResult.isNotValid()) {
            log.info("InR-ID: [{}], X-Request-ID: [{}]. Create account consents status - validation failed: {}",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), validationResult.getMessageError());
            return ResponseObject.<ConsentStatusResponse>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        ConsentStatus consentStatus = validatedAccountConsent.getConsentStatus();
        if (consentStatus.isFinalisedStatus()) {
            return responseBuilder
                       .body(new ConsentStatusResponse(consentStatus))
                       .build();
        }

        SpiResponse<SpiAisConsentStatusResponse> spiResponse = getConsentStatusFromSpi(validatedAccountConsent, consentId);

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS);
            log.info("InR-ID: [{}], X-Request-ID: [{}]. Get account consents status failed: Couldn't get AIS consent status at SPI level: {}",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), errorHolder);
            return responseBuilder
                       .fail(new MessageError(errorHolder))
                       .build();
        }
        ConsentStatus spiConsentStatus = spiResponse.getPayload().getConsentStatus();
        aisConsentService.updateConsentStatus(consentId, spiConsentStatus);

        return responseBuilder.body(new ConsentStatusResponse(spiConsentStatus)).build();
    }

    /**
     * Terminates account consent on PSU request
     *
     * @param consentId String representation of AccountConsent identification
     * @return VOID
     */
    public ResponseObject<Void> deleteAccountConsentsById(String consentId) {
        xs2aEventService.recordAisTppRequest(consentId, EventType.DELETE_AIS_CONSENT_REQUEST_RECEIVED);
        Optional<AccountConsent> accountConsentOptional = aisConsentService.getAccountConsentById(consentId);

        if (accountConsentOptional.isPresent()) {
            // TODO this is not correct. https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/569

            AccountConsent accountConsent = accountConsentOptional.get();
            ValidationResult validationResult = deleteAccountConsentsByIdValidator.validate(new CommonConsentObject(accountConsent));
            if (validationResult.isNotValid()) {
                log.info("InR-ID: [{}], X-Request-ID: [{}], Consent-ID: [{}]. Delete account consent - validation failed: {}",
                         requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), accountConsent.getId(), validationResult.getMessageError());
                return ResponseObject.<Void>builder()
                           .fail(validationResult.getMessageError())
                           .build();
            }

            SpiContextData contextData = getSpiContextData();

            SpiAspspConsentDataProvider aspspDataProvider = aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId);
            SpiResponse<VoidResponse> revokeAisConsentResponse = aisConsentSpi.revokeAisConsent(contextData, aisConsentMapper.mapToSpiAccountConsent(accountConsent), aspspDataProvider);

            if (revokeAisConsentResponse.hasError()) {
                ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(revokeAisConsentResponse, ServiceType.AIS);
                log.info("InR-ID: [{}], X-Request-ID: [{}], Consent-ID: [{}]. Delete account consent failed: Couldn't revoke AIS consent at SPI level: {}",
                         requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), accountConsent.getId(), errorHolder);
                return ResponseObject.<Void>builder()
                           .fail(new MessageError(errorHolder))
                           .build();
            }

            ConsentStatus newConsentStatus = accountConsent.getConsentStatus() == ConsentStatus.RECEIVED
                                                 ? ConsentStatus.REJECTED
                                                 : ConsentStatus.TERMINATED_BY_TPP;

            aisConsentService.updateConsentStatus(consentId, newConsentStatus);
            return ResponseObject.<Void>builder().build();
        }

        log.info("InR-ID: [{}], X-Request-ID: [{}], Consent-ID: [{}]. Delete account consent failed: consent not found by id",
                 requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), consentId);
        return ResponseObject.<Void>builder()
                   .fail(AIS_403, of(CONSENT_UNKNOWN_403)).build();
    }

    /**
     * Returns account consent by its id
     *
     * @param consentId String representation of AccountConsent identification
     * @return AccountConsent requested by consentId
     */
    public ResponseObject<AccountConsent> getAccountConsentById(String consentId) {
        xs2aEventService.recordAisTppRequest(consentId, EventType.GET_AIS_CONSENT_REQUEST_RECEIVED);

        Optional<AccountConsent> consentOptional = aisConsentService.getAccountConsentById(consentId);

        if (!consentOptional.isPresent()) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Consent-ID: [{}]. Get account consent failed: initial consent not found by id",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), consentId);
            return ResponseObject.<AccountConsent>builder()
                       .fail(AIS_403, of(CONSENT_UNKNOWN_403))
                       .build();
        }

        AccountConsent consent = consentOptional.get();

        ValidationResult validationResult = getAccountConsentByIdValidator.validate(new CommonConsentObject(consent));
        if (validationResult.isNotValid()) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Consent-ID: [{}]. Get account consent - validation failed: {}",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), consentId, validationResult.getMessageError());
            return ResponseObject.<AccountConsent>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        if (Objects.nonNull(consent.getConsentStatus()) &&
                consent.getConsentStatus().isFinalisedStatus()) {

            return ResponseObject.<AccountConsent>builder()
                       .body(consent)
                       .build();
        }

        SpiResponse<SpiAisConsentStatusResponse> spiConsentStatus = getConsentStatusFromSpi(consent, consentId);

        if (spiConsentStatus.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiConsentStatus, ServiceType.AIS);
            log.info("InR-ID: [{}], X-Request-ID: [{}]. Get account consents failed: Couldn't get AIS consent at SPI level: {}",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), errorHolder);
            return ResponseObject.<AccountConsent>builder()
                       .fail(new MessageError(errorHolder))
                       .build();
        }

        ConsentStatus consentStatus = spiConsentStatus.getPayload().getConsentStatus();
        aisConsentService.updateConsentStatus(consentId, consentStatus);

        return ResponseObject.<AccountConsent>builder()
                   .body(aisConsentMapper.mapToAccountConsentWithNewStatus(consent, consentStatus))
                   .build();
    }

    /**
     * Creates new authorisation for given consent and updates it if PSU Data or password are present in the request
     *
     * @param psuData   PSU authorisation data, can be empty
     * @param consentId String representation of AccountConsent identification
     * @param password  PSU password, can be omitted
     * @return authorisation response
     */
    public ResponseObject<AuthorisationResponse> createAisAuthorisation(PsuIdData psuData, String consentId, String password) {
        ResponseObject<CreateConsentAuthorizationResponse> createAisAuthorizationResponse = createConsentAuthorizationWithResponse(psuData, consentId);

        if (createAisAuthorizationResponse.hasError()) {
            return ResponseObject.<AuthorisationResponse>builder()
                       .fail(createAisAuthorizationResponse.getError())
                       .build();
        }

        if (psuData.isEmpty()
                || StringUtils.isBlank(password)) {
            return ResponseObject.<AuthorisationResponse>builder()
                       .body(createAisAuthorizationResponse.getBody())
                       .build();
        }

        String authorisationId = createAisAuthorizationResponse.getBody().getAuthorisationId();

        UpdateConsentPsuDataReq updatePsuData = new UpdateConsentPsuDataReq();
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

    private ResponseObject<CreateConsentAuthorizationResponse> createConsentAuthorizationWithResponse(PsuIdData psuData, String consentId) {
        xs2aEventService.recordAisTppRequest(consentId, EventType.START_AIS_CONSENT_AUTHORISATION_REQUEST_RECEIVED);

        // TODO temporary solution: CMS should be refactored to return response objects instead of Strings, Enums, Booleans etc., so we should receive this error from CMS https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/581
        Optional<AccountConsent> accountConsent = aisConsentService.getAccountConsentById(consentId);

        if (!accountConsent.isPresent()) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Consent-ID: [{}]. Create consent authorisation with response failed: consent not found by id",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), consentId);
            return ResponseObject.<CreateConsentAuthorizationResponse>builder()
                       .fail(AIS_403, of(CONSENT_UNKNOWN_403)).build();
        }

        ValidationResult validationResult = createConsentAuthorisationValidator.validate(new CommonConsentObject(accountConsent.get()));
        if (validationResult.isNotValid()) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Consent-ID: [{}]. Create consent authorisation with response - validation failed: {}",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), consentId, validationResult.getMessageError());
            return ResponseObject.<CreateConsentAuthorizationResponse>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        if (accountConsent.get().isExpired()) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Consent-ID: [{}]. Create consent authorisation with response failed: consent expired",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), consentId);
            return ResponseObject.<CreateConsentAuthorizationResponse>builder()
                       .fail(AIS_401, of(CONSENT_EXPIRED))
                       .build();
        }

        AisAuthorizationService service = aisScaAuthorisationServiceResolver.getService();
        return service.createConsentAuthorization(psuData, consentId)
                   .map(resp -> ResponseObject.<CreateConsentAuthorizationResponse>builder().body(resp).build())
                   .orElseGet(ResponseObject.<CreateConsentAuthorizationResponse>builder()
                                  .fail(AIS_403, of(CONSENT_UNKNOWN_403))
                                  ::build);
    }

    public ResponseObject<UpdateConsentPsuDataResponse> updateConsentPsuData(UpdateConsentPsuDataReq updatePsuData) {
        xs2aEventService.recordAisTppRequest(updatePsuData.getConsentId(), EventType.UPDATE_AIS_CONSENT_PSU_DATA_REQUEST_RECEIVED, updatePsuData);

        String consentId = updatePsuData.getConsentId();
        String authorisationId = updatePsuData.getAuthorizationId();

        if (!endpointAccessCheckerService.isEndpointAccessible(authorisationId, consentId)) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Consent-ID: [{}], Authorisation-ID [{}]. Update consent PSU data failed: update endpoint is blocked for current authorisation",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), consentId, authorisationId);
            return ResponseObject.<UpdateConsentPsuDataResponse>builder()
                       .fail(AIS_403, of(SERVICE_BLOCKED))
                       .build();
        }

        // TODO temporary solution: CMS should be refactored to return response objects instead of Strings, Enums, Booleans etc., so we should receive this error from CMS https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/581
        Optional<AccountConsent> accountConsent = aisConsentService.getAccountConsentById(consentId);

        if (!accountConsent.isPresent()) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Consent-ID: [{}]. Update consent PSU data failed: consent not found by id",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), consentId);
            return ResponseObject.<UpdateConsentPsuDataResponse>builder()
                       .fail(AIS_403, of(CONSENT_UNKNOWN_403)).build();
        }

        ValidationResult validationResult = updateConsentPsuDataValidator.validate(new CommonConsentObject(accountConsent.get()));
        if (validationResult.isNotValid()) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Consent-ID: [{}], Authorisation-ID [{}]. Update consent PSU data - validation failed: {}",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), consentId, authorisationId, validationResult.getMessageError());
            return ResponseObject.<UpdateConsentPsuDataResponse>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        if (accountConsent.get().isExpired()) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Consent-ID: [{}]. Update consent PSU data failed: consent expired",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), consentId);
            return ResponseObject.<UpdateConsentPsuDataResponse>builder()
                       .fail(AIS_401, of(CONSENT_EXPIRED))
                       .build();
        }

        return aisScaAuthorisationServiceResolver.getServiceInitiation(updatePsuData.getAuthorizationId())
                   .getAccountConsentAuthorizationById(updatePsuData.getAuthorizationId(), updatePsuData.getConsentId())
                   .map(conAuth -> getUpdateConsentPsuDataResponse(updatePsuData, conAuth))
                   .orElseGet(() -> {
                       log.info("InR-ID: [{}], X-Request-ID: [{}], Consent-ID: [{}]. Update consent PSU data failed: consent not found at CMS by id",
                                requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), consentId);
                       return ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                  .fail(AIS_404, of(RESOURCE_UNKNOWN_404))
                                  .build();
                   });
    }

    private ResponseObject<UpdateConsentPsuDataResponse> getUpdateConsentPsuDataResponse(UpdateConsentPsuDataReq updatePsuData, AccountConsentAuthorization consentAuthorization) {
        UpdateConsentPsuDataResponse response = aisScaAuthorisationServiceResolver.getServiceInitiation(updatePsuData.getAuthorizationId())
                                                    .updateConsentPsuData(updatePsuData, consentAuthorization);

        return Optional.ofNullable(response)
                   .map(s -> Optional.ofNullable(s.getMessageError())
                                 .map(e -> ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                               .fail(e)
                                               .build())
                                 .orElseGet(ResponseObject.<UpdateConsentPsuDataResponse>builder().body(response)::build))
                   .orElseGet(ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                  .fail(AIS_400, of(FORMAT_ERROR))
                                  ::build);
    }

    public ResponseObject<Xs2aAuthorisationSubResources> getConsentInitiationAuthorisations(String consentId) {
        xs2aEventService.recordAisTppRequest(consentId, EventType.GET_CONSENT_AUTHORISATION_REQUEST_RECEIVED);

        Optional<AccountConsent> accountConsent = aisConsentService.getAccountConsentById(consentId);
        if (!accountConsent.isPresent()) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Consent-ID: [{}]. Get consent initiation authorisations failed: consent not found by id",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), consentId);
            return ResponseObject.<Xs2aAuthorisationSubResources>builder()
                       .fail(AIS_403, of(CONSENT_UNKNOWN_403)).build();
        }

        ValidationResult validationResult = getConsentAuthorisationsValidator.validate(new CommonConsentObject(accountConsent.get()));
        if (validationResult.isNotValid()) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Consent-ID: [{}]. Get consent authorisations - validation failed: {}",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), consentId, validationResult.getMessageError());
            return ResponseObject.<Xs2aAuthorisationSubResources>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        return getAuthorisationSubResources(consentId)
                   .map(resp -> ResponseObject.<Xs2aAuthorisationSubResources>builder().body(resp).build())
                   .orElseGet(() -> {
                       log.info("InR-ID: [{}], X-Request-ID: [{}], Consent-ID: [{}]. Get consent initiation authorisations failed: authorisation not found at CMS by consent id",
                                requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), consentId);
                       return ResponseObject.<Xs2aAuthorisationSubResources>builder()
                                  .fail(AIS_404, of(RESOURCE_UNKNOWN_404))
                                  .build();
                   });
    }

    /**
     * Gets SCA status of consent authorisation
     *
     * @param consentId       String representation of consent identifier
     * @param authorisationId String representation of authorisation identifier
     * @return Response containing SCA status of the authorisation or corresponding error
     */
    public ResponseObject<ScaStatus> getConsentAuthorisationScaStatus(String consentId, String authorisationId) {
        xs2aEventService.recordAisTppRequest(consentId, EventType.GET_CONSENT_SCA_STATUS_REQUEST_RECEIVED);

        Optional<AccountConsent> accountConsent = aisConsentService.getAccountConsentById(consentId);
        if (!accountConsent.isPresent()) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Consent-ID: [{}]. Get consent authorisation SCA status failed: consent not found by id",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), consentId);
            return ResponseObject.<ScaStatus>builder()
                       .fail(AIS_403, of(CONSENT_UNKNOWN_403)).build();
        }

        ValidationResult validationResult = getConsentAuthorisationScaStatusValidator.validate(new CommonConsentObject(accountConsent.get()));
        if (validationResult.isNotValid()) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Consent-ID: [{}], Authorisation-ID [{}]. Get consent authorisation SCA status - validation failed: {}",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), consentId, authorisationId, validationResult.getMessageError());
            return ResponseObject.<ScaStatus>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        Optional<ScaStatus> scaStatus = aisScaAuthorisationServiceResolver.getServiceInitiation(authorisationId)
                                            .getAuthorisationScaStatus(consentId, authorisationId);

        if (!scaStatus.isPresent()) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Consent-ID: [{}]. Get consent authorisation SCA status failed: consent not found at CMS by id",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), consentId);
            return ResponseObject.<ScaStatus>builder()
                       .fail(AIS_403, of(RESOURCE_UNKNOWN_403))
                       .build();
        }

        return ResponseObject.<ScaStatus>builder()
                   .body(scaStatus.get())
                   .build();
    }

    private SpiResponse<SpiAisConsentStatusResponse> getConsentStatusFromSpi(AccountConsent accountConsent, String consentId) {
        SpiAccountConsent spiAccountConsent = aisConsentMapper.mapToSpiAccountConsent(accountConsent);
        SpiAspspConsentDataProvider aspspDataProvider = aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId);
        return aisConsentSpi.getConsentStatus(spiContextDataProvider.provide(), spiAccountConsent, aspspDataProvider);
    }

    private Xs2aAccountAccess getAccessForGlobalOrAllAvailableAccountsConsent(CreateConsentReq request) {
        return new Xs2aAccountAccess(
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>(),
            request.getAccess().getAvailableAccounts(),
            request.getAccess().getAllPsd2(),
            request.getAccess().getAvailableAccountsWithBalance()
        );
    }

    private void proceedImplicitCaseForCreateConsent(CreateConsentResponse response, PsuIdData psuData, String consentId) {
        aisScaAuthorisationServiceResolver.getService().createConsentAuthorization(psuData, consentId)
            .ifPresent(a -> response.setAuthorizationId(a.getAuthorisationId()));
    }

    private SpiContextData getSpiContextData() {
        PsuIdData psuIdData = requestProviderService.getPsuIdData();
        log.info("X-Request-ID: [{}]. Corresponding PSU-ID {} was provided from request.", requestProviderService.getRequestId(), psuIdData);
        return spiContextDataProvider.provideWithPsuIdData(psuIdData);
    }

    private Optional<Xs2aAuthorisationSubResources> getAuthorisationSubResources(String consentId) {
        return aisConsentService.getAuthorisationSubResources(consentId)
                   .map(Xs2aAuthorisationSubResources::new);
    }
}
