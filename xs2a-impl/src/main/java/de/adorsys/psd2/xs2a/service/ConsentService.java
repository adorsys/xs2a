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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCreateAisConsentResponse;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationMethodDecider;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.ais.AisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.ais.AisScaAuthorisationServiceResolver;
import de.adorsys.psd2.xs2a.service.consent.AccountReferenceInConsentUpdater;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAccountAccessMapper;
import de.adorsys.psd2.xs2a.service.spi.InitialSpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaStatusResponse;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiConsentStatusResponse;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiInitiateAisConsentResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse.VoidResponse;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsentService {
    private final Xs2aAisConsentMapper aisConsentMapper;
    private final SpiToXs2aAccountAccessMapper spiToXs2aAccountAccessMapper;
    private final Xs2aAisConsentService aisConsentService;
    private final AisScaAuthorisationServiceResolver aisScaAuthorisationServiceResolver;
    private final TppService tppService;
    private final SpiContextDataProvider spiContextDataProvider;
    private final AuthorisationMethodDecider authorisationMethodDecider;
    private final AisConsentSpi aisConsentSpi;
    private final Xs2aEventService xs2aEventService;
    private final AccountReferenceInConsentUpdater accountReferenceUpdater;
    private final SpiErrorMapper spiErrorMapper;

    private final ConsentValidationService consentValidationService;
    private final ConsentAuthorisationService consentAuthorisationService;
    private final AisScaAuthorisationService aisScaAuthorisationService;
    private final RequestProviderService requestProviderService;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final LoggingContextService loggingContextService;
    private final AdditionalInformationSupportedService additionalInformationSupportedService;
    private final Xs2aAuthorisationService xs2aAuthorisationService;

    /**
     * Performs create consent operation either by filling the appropriate AccountAccess fields with corresponding
     * account details or by getting account details from ASPSP by psuId and filling the appropriate fields in
     * AccountAccess determined by availableAccounts or allPsd2 variables
     *
     * @param request           body of create consent request carrying such parameters as AccountAccess, validity terms etc.
     * @param psuData           PsuIdData container of authorisation data about PSU
     * @param explicitPreferred is TPP explicit authorisation preferred
     * @return CreateConsentResponse representing the complete response to create consent request
     */
    public ResponseObject<CreateConsentResponse> createAccountConsentsWithResponse(CreateConsentReq request, PsuIdData psuData,
                                                                                   boolean explicitPreferred) {
        xs2aEventService.recordTppRequest(EventType.CREATE_AIS_CONSENT_REQUEST_RECEIVED, request);
        CreateConsentReq requestAfterCheck = additionalInformationSupportedService.checkIfAdditionalInformationSupported(request);

        ValidationResult validationResult = consentValidationService.validateConsentOnCreate(requestAfterCheck, psuData);
        if (validationResult.isNotValid()) {
            log.info("Create account consent with response - validation failed: {}",
                     validationResult.getMessageError());
            return ResponseObject.<CreateConsentResponse>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        if (requestAfterCheck.isGlobalOrAllAccountsAccessConsent()) {
            requestAfterCheck.setAccess(spiToXs2aAccountAccessMapper.getAccessForGlobalOrAllAvailableAccountsConsent(requestAfterCheck));
        }

        TppInfo tppInfo = tppService.getTppInfo();
        Optional<Xs2aCreateAisConsentResponse> createAisConsentResponseOptional = aisConsentService.createConsent(requestAfterCheck, psuData, tppInfo);

        if (createAisConsentResponseOptional.isEmpty()) {
            return ResponseObject.<CreateConsentResponse>builder()
                       .fail(ErrorType.AIS_400, of(MessageErrorCode.RESOURCE_UNKNOWN_400))
                       .build();
        }

        Xs2aCreateAisConsentResponse createAisConsentResponse = createAisConsentResponseOptional.get();
        SpiContextData contextData = spiContextDataProvider.provide(psuData, tppInfo);
        InitialSpiAspspConsentDataProvider aspspConsentDataProvider = aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider();
        AisConsent aisConsent = createAisConsentResponse.getAisConsent();

        SpiResponse<SpiInitiateAisConsentResponse> initiateAisConsentSpiResponse = aisConsentSpi.initiateAisConsent(contextData, aisConsentMapper.mapToSpiAccountConsent(aisConsent), aspspConsentDataProvider);

        String encryptedConsentId = createAisConsentResponse.getConsentId();
        aspspConsentDataProvider.saveWith(encryptedConsentId);

        if (initiateAisConsentSpiResponse.hasError()) {
            aisConsentService.updateConsentStatus(encryptedConsentId, ConsentStatus.REJECTED);
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(initiateAisConsentSpiResponse, ServiceType.AIS);
            log.info("Consent-ID: [{}]. Create account consent  with response failed. Consent rejected. Couldn't initiate AIS consent at SPI level: {}",
                     encryptedConsentId, errorHolder);
            return ResponseObject.<CreateConsentResponse>builder()
                       .fail(new MessageError(errorHolder))
                       .build();
        }

        SpiInitiateAisConsentResponse spiResponsePayload = initiateAisConsentSpiResponse.getPayload();
        boolean multilevelScaRequired = spiResponsePayload.isMultilevelScaRequired()
                                            && !aisScaAuthorisationService.isOneFactorAuthorisation(aisConsent);

        updateMultilevelSca(encryptedConsentId, multilevelScaRequired);

        Optional<AccountAccess> xs2aAccountAccess = spiToXs2aAccountAccessMapper.mapToAccountAccess(spiResponsePayload.getAccountAccess());
        xs2aAccountAccess.ifPresent(accountAccess ->
                                        accountReferenceUpdater.rewriteAccountAccess(encryptedConsentId, accountAccess, ConsentType.AIS));

        ConsentStatus consentStatus = aisConsent.getConsentStatus();
        CreateConsentResponse createConsentResponse = new CreateConsentResponse(consentStatus.getValue(), encryptedConsentId,
                                                                                null, null, null,
                                                                                spiResponsePayload.getPsuMessage(), multilevelScaRequired,
                                                                                requestProviderService.getInternalRequestIdString(),
                                                                                createAisConsentResponse.getTppNotificationContentPreferred());

        createConsentResponse.setTppMessageInformation(consentValidationService.buildWarningMessages(requestAfterCheck));

        ResponseObject<CreateConsentResponse> createConsentResponseObject = ResponseObject.<CreateConsentResponse>builder().body(createConsentResponse).build();

        if (authorisationMethodDecider.isImplicitMethod(explicitPreferred, multilevelScaRequired)) {
            proceedImplicitCaseForCreateConsent(createConsentResponseObject.getBody(), psuData, encryptedConsentId);
        }

        loggingContextService.storeConsentStatus(consentStatus);

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
     * @param consentId String representation of Consent identification
     * @return ConsentStatus
     */
    public ResponseObject<ConsentStatusResponse> getAccountConsentsStatusById(String consentId) {
        xs2aEventService.recordConsentTppRequest(consentId, EventType.GET_AIS_CONSENT_STATUS_REQUEST_RECEIVED);
        ResponseObject.ResponseBuilder<ConsentStatusResponse> responseBuilder = ResponseObject.builder();

        Optional<AisConsent> validatedAisConsentOptional = aisConsentService.getAccountConsentById(consentId);

        if (validatedAisConsentOptional.isEmpty()) {
            log.info("Consent-ID: [{}]. Get account consents status failed: consent not found by ID", consentId);
            return responseBuilder
                       .fail(ErrorType.AIS_403, of(MessageErrorCode.CONSENT_UNKNOWN_403))
                       .build();
        }

        AisConsent validatedAccountConsent = validatedAisConsentOptional.get();

        ValidationResult validationResult = consentValidationService.validateConsentOnGettingStatusById(validatedAccountConsent);
        if (validationResult.isNotValid()) {
            log.info("Get account consents status - validation failed: {}", validationResult.getMessageError());
            return ResponseObject.<ConsentStatusResponse>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        ConsentStatus consentStatus = validatedAccountConsent.getConsentStatus();
        if (consentStatus.isFinalisedStatus()) {
            loggingContextService.storeConsentStatus(consentStatus);
            return responseBuilder
                       .body(new ConsentStatusResponse(consentStatus, null))
                       .build();
        }

        SpiResponse<SpiConsentStatusResponse> spiResponse = getConsentStatusFromSpi(validatedAccountConsent, consentId);

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS);
            log.info("Get account consents status failed: Couldn't get AIS consent status at SPI level: {}", errorHolder);
            return responseBuilder
                       .fail(new MessageError(errorHolder))
                       .build();
        }

        SpiConsentStatusResponse spiPayload = spiResponse.getPayload();
        ConsentStatus spiConsentStatus = spiPayload.getConsentStatus();
        aisConsentService.updateConsentStatus(consentId, spiConsentStatus);
        loggingContextService.storeConsentStatus(spiConsentStatus);

        return responseBuilder
                   .body(new ConsentStatusResponse(spiConsentStatus, spiPayload.getPsuMessage()))
                   .build();
    }

    /**
     * Terminates account consent on PSU request
     *
     * @param consentId String representation of Consent identification
     * @return VOID
     */
    public ResponseObject<Void> deleteAccountConsentsById(String consentId) {
        xs2aEventService.recordConsentTppRequest(consentId, EventType.DELETE_AIS_CONSENT_REQUEST_RECEIVED);
        Optional<AisConsent> aisConsentOptional = aisConsentService.getAccountConsentById(consentId);

        if (aisConsentOptional.isPresent()) {
            AisConsent accountConsent = aisConsentOptional.get();
            ValidationResult validationResult = consentValidationService.validateConsentOnDelete(accountConsent);
            if (validationResult.isNotValid()) {
                log.info("Consent-ID: [{}]. Delete account consent - validation failed: {}",
                         accountConsent.getId(), validationResult.getMessageError());
                return ResponseObject.<Void>builder()
                           .fail(validationResult.getMessageError())
                           .build();
            }

            SpiContextData contextData = getSpiContextData();

            SpiAspspConsentDataProvider aspspDataProvider = aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId);
            SpiResponse<VoidResponse> revokeAisConsentResponse = aisConsentSpi.revokeAisConsent(contextData, aisConsentMapper.mapToSpiAccountConsent(accountConsent), aspspDataProvider);

            if (revokeAisConsentResponse.hasError()) {
                ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(revokeAisConsentResponse, ServiceType.AIS);
                log.info("Consent-ID: [{}]. Delete account consent failed: Couldn't revoke AIS consent at SPI level: {}",
                         accountConsent.getId(), errorHolder);
                return ResponseObject.<Void>builder()
                           .fail(new MessageError(errorHolder))
                           .build();
            }

            ConsentStatus newConsentStatus = accountConsent.getConsentStatus() == ConsentStatus.RECEIVED
                                                 ? ConsentStatus.REJECTED
                                                 : ConsentStatus.TERMINATED_BY_TPP;

            loggingContextService.storeConsentStatus(newConsentStatus);

            aisConsentService.updateConsentStatus(consentId, newConsentStatus);
            return ResponseObject.<Void>builder().build();
        }

        log.info("Consent-ID: [{}]. Delete account consent failed: consent not found by id", consentId);
        return ResponseObject.<Void>builder()
                   .fail(ErrorType.AIS_403, of(MessageErrorCode.CONSENT_UNKNOWN_403)).build();
    }

    /**
     * Returns account consent by its id
     *
     * @param consentId String representation of Consent identification
     * @return AisConsent requested by consentId
     */
    public ResponseObject<AisConsent> getAccountConsentById(String consentId) {
        xs2aEventService.recordConsentTppRequest(consentId, EventType.GET_AIS_CONSENT_REQUEST_RECEIVED);

        Optional<AisConsent> consentOptional = aisConsentService.getAccountConsentById(consentId);

        if (consentOptional.isEmpty()) {
            log.info("Consent-ID: [{}]. Get account consent failed: initial consent not found by id", consentId);
            return ResponseObject.<AisConsent>builder()
                       .fail(ErrorType.AIS_403, of(MessageErrorCode.CONSENT_UNKNOWN_403))
                       .build();
        }

        AisConsent consent = consentOptional.get();

        ValidationResult validationResult = consentValidationService.validateConsentOnGettingById(consent);
        if (validationResult.isNotValid()) {
            log.info("Consent-ID: [{}]. Get account consent - validation failed: {}",
                     consentId, validationResult.getMessageError());
            return ResponseObject.<AisConsent>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        if (Objects.nonNull(consent.getConsentStatus()) && consent.getConsentStatus().isFinalisedStatus()) {
            loggingContextService.storeConsentStatus(consent.getConsentStatus());
            return ResponseObject.<AisConsent>builder()
                       .body(consent)
                       .build();
        }

        SpiResponse<SpiConsentStatusResponse> spiConsentStatus = getConsentStatusFromSpi(consent, consentId);

        if (spiConsentStatus.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiConsentStatus, ServiceType.AIS);
            log.info("Get account consents failed: Couldn't get AIS consent at SPI level: {}", errorHolder);
            return ResponseObject.<AisConsent>builder()
                       .fail(new MessageError(errorHolder))
                       .build();
        }

        ConsentStatus consentStatus = spiConsentStatus.getPayload().getConsentStatus();
        aisConsentService.updateConsentStatus(consentId, consentStatus);
        loggingContextService.storeConsentStatus(consent.getConsentStatus());

        consent.setConsentStatus(consentStatus);
        return ResponseObject.<AisConsent>builder()
                   .body(consent)
                   .build();
    }

    /**
     * Creates new authorisation for given consent and updates it if PSU Data or password are present in the request
     *
     * @param psuData   PSU authorisation data, can be empty
     * @param consentId String representation of Consent identification
     * @param password  PSU password, can be omitted
     * @return authorisation response
     */
    public ResponseObject<AuthorisationResponse> createAisAuthorisation(PsuIdData psuData, String consentId, String password) {
        return consentAuthorisationService.createAisAuthorisation(psuData, consentId, password);
    }

    public ResponseObject<UpdateConsentPsuDataResponse> updateConsentPsuData(UpdateConsentPsuDataReq updatePsuData) {
        return consentAuthorisationService.updateConsentPsuData(updatePsuData);
    }

    public ResponseObject<Xs2aAuthorisationSubResources> getConsentInitiationAuthorisations(String consentId) {
        return consentAuthorisationService.getConsentInitiationAuthorisations(consentId);
    }

    /**
     * Gets SCA status response of consent authorisation
     *
     * @param consentId       String representation of consent identifier
     * @param authorisationId String representation of authorisation identifier
     * @return Response containing SCA status of the authorisation and optionally trusted beneficiaries flag or corresponding error
     */
    public ResponseObject<Xs2aScaStatusResponse> getConsentAuthorisationScaStatus(String consentId, String authorisationId) {
        ResponseObject<ConsentScaStatus> cmsScaStatusResponse = consentAuthorisationService.getConsentAuthorisationScaStatus(consentId, authorisationId);
        if (cmsScaStatusResponse.hasError()) {
            return ResponseObject.<Xs2aScaStatusResponse>builder()
                       .fail(cmsScaStatusResponse.getError())
                       .build();
        }

        SpiContextData contextData = getSpiContextData();
        SpiAspspConsentDataProvider spiAspspConsentDataProvider = aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId);
        ScaStatus scaStatus = cmsScaStatusResponse.getBody().getScaStatus();

        SpiResponse<SpiScaStatusResponse> spiScaInformation = aisConsentSpi.getScaStatus(scaStatus, contextData, authorisationId, spiAspspConsentDataProvider);

        if (spiScaInformation.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiScaInformation, ServiceType.AIS);
            log.info("Authorisation-ID [{}], Consent-ID [{}]. Get SCA status failed.", authorisationId, consentId);
            return ResponseObject.<Xs2aScaStatusResponse>builder()
                       .fail(errorHolder)
                       .build();
        }

        SpiScaStatusResponse spiScaInformationPayload = spiScaInformation.getPayload();

        if (scaStatus.isNotFinalisedStatus() && scaStatus != spiScaInformationPayload.getScaStatus()) {
            scaStatus = spiScaInformationPayload.getScaStatus();
            xs2aAuthorisationService.updateAuthorisationStatus(authorisationId, scaStatus);
            log.info("Authorisation-ID [{}], Consent-ID [{}]. SCA status was changed to [{}] from SPI.", authorisationId, consentId, scaStatus);
        }

        Boolean beneficiaryFlag = scaStatus.isFinalisedStatus() ? spiScaInformationPayload.getTrustedBeneficiaryFlag() : null;
        Xs2aScaStatusResponse response = new Xs2aScaStatusResponse(scaStatus,
                                                                   beneficiaryFlag,
                                                                   spiScaInformationPayload.getPsuMessage());

        return ResponseObject.<Xs2aScaStatusResponse>builder()
                   .body(response)
                   .build();
    }

    private SpiResponse<SpiConsentStatusResponse> getConsentStatusFromSpi(AisConsent aisConsent, String consentId) {
        SpiAccountConsent spiAccountConsent = aisConsentMapper.mapToSpiAccountConsent(aisConsent);
        SpiAspspConsentDataProvider aspspDataProvider = aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId);
        return aisConsentSpi.getConsentStatus(spiContextDataProvider.provide(), spiAccountConsent, aspspDataProvider);
    }

    private void proceedImplicitCaseForCreateConsent(CreateConsentResponse response, PsuIdData psuData, String consentId) {
        aisScaAuthorisationServiceResolver.getService().createConsentAuthorization(psuData, consentId)
            .ifPresent(a -> {
                response.setAuthorizationId(a.getAuthorisationId());
                loggingContextService.storeScaStatus(a.getScaStatus());
            });
    }

    private SpiContextData getSpiContextData() {
        PsuIdData psuIdData = requestProviderService.getPsuIdData();
        log.info("Corresponding PSU-ID {} was provided from request.", psuIdData);
        return spiContextDataProvider.provideWithPsuIdData(psuIdData);
    }
}
