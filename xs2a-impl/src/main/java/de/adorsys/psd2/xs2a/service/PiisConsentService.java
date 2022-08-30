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

import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.Xs2aResponse;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCreatePiisConsentResponse;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.domain.fund.CreatePiisConsentRequest;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationChainResponsibilityService;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationMethodDecider;
import de.adorsys.psd2.xs2a.service.authorization.ConsentAuthorizationService;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.piis.PiisScaAuthorisationServiceResolver;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.PiisAuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.consent.AccountReferenceInConsentUpdater;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPiisConsentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAccountReferenceMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aLinksMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPiisConsentMapper;
import de.adorsys.psd2.xs2a.service.spi.InitialSpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.service.validator.piis.CreatePiisConsentValidator;
import de.adorsys.psd2.xs2a.service.validator.piis.dto.CreatePiisConsentRequestObject;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaStatusResponse;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiConsentStatusResponse;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiInitiatePiisConsentResponse;
import de.adorsys.psd2.xs2a.spi.domain.piis.SpiPiisConsent;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PiisConsentSpi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;

@Slf4j
@Service
@RequiredArgsConstructor
public class PiisConsentService {
    private final Xs2aPiisConsentService xs2aPiisConsentService;
    private final TppService tppService;
    private final Xs2aEventService xs2aEventService;
    private final RequestProviderService requestProviderService;
    private final PiisConsentSpi piisConsentSpi;
    private final SpiContextDataProvider spiContextDataProvider;
    private final Xs2aToSpiPiisConsentMapper xs2aToSpiPiisConsentMapper;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final SpiErrorMapper spiErrorMapper;
    private final AccountReferenceInConsentUpdater accountReferenceUpdater;
    private final SpiToXs2aAccountReferenceMapper spiToXs2aAccountReferenceMapper;
    private final CreatePiisConsentValidator createPiisConsentValidator;
    private final AuthorisationMethodDecider authorisationMethodDecider;
    private final PiisScaAuthorisationServiceResolver piisScaAuthorisationServiceResolver;
    private final ConfirmationOfFundsConsentValidationService confirmationOfFundsConsentValidationService;
    private final PiisConsentAuthorisationService piisConsentAuthorisationService;
    private final Xs2aAuthorisationService xs2aAuthorisationService;
    private final SpiToXs2aLinksMapper spiToXs2aLinksMapper;
    private final ScaApproachResolver scaApproachResolver;
    private final AuthorisationChainResponsibilityService authorisationChainResponsibilityService;
    private final LoggingContextService loggingContextService;

    public ResponseObject<Xs2aConfirmationOfFundsResponse> createPiisConsentWithResponse(CreatePiisConsentRequest request, PsuIdData psuData, boolean explicitPreferred) {
        xs2aEventService.recordTppRequest(EventType.CREATE_PIIS_CONSENT_REQUEST_RECEIVED, request);

        ValidationResult validationResult = createPiisConsentValidator.validate(new CreatePiisConsentRequestObject(request, psuData));
        if (validationResult.isNotValid()) {
            log.info("Create funds confirmation consent with response - validation failed: {}",
                     validationResult.getMessageError());
            return ResponseObject.<Xs2aConfirmationOfFundsResponse>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        TppInfo tppInfo = tppService.getTppInfo();
        Xs2aResponse<Xs2aCreatePiisConsentResponse> xs2aResponse = xs2aPiisConsentService.createConsent(request, psuData, tppInfo);

        if (xs2aResponse.hasError()) {
            return resolveConsentCreationError(xs2aResponse.getErrors()
                                                   .stream()
                                                   .map(TppMessage::getErrorCode)
                                                   .collect(Collectors.toList()));
        }

        Xs2aCreatePiisConsentResponse xs2aCreatePiisConsentResponse = xs2aResponse.getPayload();
        PiisConsent piisConsent = xs2aCreatePiisConsentResponse.getPiisConsent();
        String encryptedConsentId = xs2aCreatePiisConsentResponse.getConsentId();

        SpiContextData contextData = spiContextDataProvider.provide(psuData, tppInfo);
        SpiPiisConsent spiPiisConsent = xs2aToSpiPiisConsentMapper.mapToSpiPiisConsent(piisConsent);
        InitialSpiAspspConsentDataProvider aspspConsentDataProvider = aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider();
        SpiResponse<SpiInitiatePiisConsentResponse> spiInitiatePiisConsentResponseSpiResponse = piisConsentSpi.initiatePiisConsent(contextData, spiPiisConsent, aspspConsentDataProvider);
        aspspConsentDataProvider.saveWith(encryptedConsentId);

        if (spiInitiatePiisConsentResponseSpiResponse.hasError()) {
            xs2aPiisConsentService.updateConsentStatus(encryptedConsentId, ConsentStatus.REJECTED);
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiInitiatePiisConsentResponseSpiResponse, ServiceType.PIIS);
            log.info("Consent-ID: [{}]. Create piis consent  with response failed. Consent rejected. Couldn't initiate PIIS consent at SPI level: {}",
                     encryptedConsentId, errorHolder);
            return ResponseObject.<Xs2aConfirmationOfFundsResponse>builder()
                       .fail(new MessageError(errorHolder))
                       .build();
        }

        SpiInitiatePiisConsentResponse spiInitiatePiisConsentResponse = spiInitiatePiisConsentResponseSpiResponse.getPayload();

        SpiAccountReference spiAccountReference = spiInitiatePiisConsentResponse.getSpiAccountReference();
        AccountAccess accountAccess = new AccountAccess(Collections.singletonList(spiToXs2aAccountReferenceMapper.mapToXs2aAccountReference(spiAccountReference)), Collections.emptyList(), Collections.emptyList(), null);
        accountReferenceUpdater.rewriteAccountAccess(encryptedConsentId, accountAccess, ConsentType.PIIS_TPP);

        ConsentStatus consentStatus = piisConsent.getConsentStatus();
        boolean multilevelScaRequired = spiInitiatePiisConsentResponse.isMultilevelScaRequired();

        updateMultilevelSca(encryptedConsentId, multilevelScaRequired);
        Xs2aConfirmationOfFundsResponse xs2aConfirmationOfFundsResponse = new Xs2aConfirmationOfFundsResponse(consentStatus.getValue(),
                                                                                                              encryptedConsentId,
                                                                                                              multilevelScaRequired,
                                                                                                              requestProviderService.getInternalRequestIdString());
        xs2aConfirmationOfFundsResponse.setPsuMessage(spiInitiatePiisConsentResponse.getPsuMessage());

        if (authorisationMethodDecider.isImplicitMethod(explicitPreferred, multilevelScaRequired)) {
            proceedImplicitCaseForCreateConsent(xs2aConfirmationOfFundsResponse, psuData, encryptedConsentId);
        }

        return ResponseObject.<Xs2aConfirmationOfFundsResponse>builder().body(xs2aConfirmationOfFundsResponse).build();
    }

    private ResponseObject<Xs2aConfirmationOfFundsResponse> resolveConsentCreationError(List<MessageErrorCode> errors) {
        if (errors.contains(MessageErrorCode.PSU_CREDENTIALS_INVALID)) {
            return ResponseObject.<Xs2aConfirmationOfFundsResponse>builder()
                       .fail(ErrorType.PIIS_400, of(MessageErrorCode.PSU_CREDENTIALS_INVALID))
                       .build();
        }
        return ResponseObject.<Xs2aConfirmationOfFundsResponse>builder()
                   .fail(ErrorType.PIIS_400, of(MessageErrorCode.RESOURCE_UNKNOWN_400))
                   .build();
    }

    public ResponseObject<PiisConsent> getPiisConsentById(String consentId) {
        xs2aEventService.recordConsentTppRequest(consentId, EventType.GET_PIIS_CONSENT_REQUEST_RECEIVED);
        Optional<PiisConsent> piisConsentById = xs2aPiisConsentService.getPiisConsentById(consentId);

        if (piisConsentById.isEmpty()) {
            log.info("Consent-ID: [{}]. Get PIIS consent failed: initial consent not found by id", consentId);
            return ResponseObject.<PiisConsent>builder()
                       .fail(ErrorType.PIIS_403, of(MessageErrorCode.CONSENT_UNKNOWN_403))
                       .build();
        }

        PiisConsent piisConsent = piisConsentById.get();
        SpiResponse<SpiConsentStatusResponse> spiResponse = getConsentStatusFromSpi(piisConsent, consentId);

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIIS);
            log.info("Get PIIS consent status failed: Couldn't get PIIS consent status at SPI level: {}", errorHolder);
            return ResponseObject.<PiisConsent>builder()
                       .fail(new MessageError(errorHolder))
                       .build();
        }

        ConsentStatus consentStatus = spiResponse.getPayload().getConsentStatus();
        piisConsent.setConsentStatus(consentStatus);
        xs2aPiisConsentService.updateConsentStatus(consentId, consentStatus);
        return ResponseObject.<PiisConsent>builder().body(piisConsent).build();
    }

    public ResponseObject<ConsentStatusResponse> getPiisConsentStatusById(String consentId) {
        xs2aEventService.recordConsentTppRequest(consentId, EventType.GET_PIIS_CONSENT_STATUS_REQUEST_RECEIVED);
        ResponseObject.ResponseBuilder<ConsentStatusResponse> responseBuilder = ResponseObject.builder();

        Optional<PiisConsent> piisConsentById = xs2aPiisConsentService.getPiisConsentById(consentId);

        if (piisConsentById.isEmpty()) {
            log.info("Consent-ID: [{}]. Get PIIS consent failed: initial consent not found by id", consentId);
            return responseBuilder
                       .fail(ErrorType.PIIS_403, of(MessageErrorCode.CONSENT_UNKNOWN_403))
                       .build();
        }

        PiisConsent piisConsent = piisConsentById.get();
        SpiResponse<SpiConsentStatusResponse> spiResponse = getConsentStatusFromSpi(piisConsent, consentId);

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIIS);
            log.info("Get PIIS consent status failed: Couldn't get PIIS consent status at SPI level: {}", errorHolder);
            return responseBuilder
                       .fail(new MessageError(errorHolder))
                       .build();
        }

        SpiConsentStatusResponse spiPayload = spiResponse.getPayload();
        ConsentStatus consentStatus = spiPayload.getConsentStatus();
        xs2aPiisConsentService.updateConsentStatus(consentId, consentStatus);

        return responseBuilder
                   .body(new ConsentStatusResponse(consentStatus, spiPayload.getPsuMessage()))
                   .build();
    }

    public ResponseObject<Xs2aAuthorisationSubResources> getConsentInitiationAuthorisations(String consentId) {
        return piisConsentAuthorisationService.getConsentInitiationAuthorisations(consentId);
    }

    public ResponseObject<Xs2aScaStatusResponse> getConsentAuthorisationScaStatus(String consentId, String authorisationId) {
        ResponseObject<ConfirmationOfFundsConsentScaStatus> cmsConsentScaStatusResponse = piisConsentAuthorisationService.getConsentAuthorisationScaStatus(consentId, authorisationId);
        if (cmsConsentScaStatusResponse.hasError()) {
            return ResponseObject.<Xs2aScaStatusResponse>builder()
                       .fail(cmsConsentScaStatusResponse.getError())
                       .build();
        }

        SpiContextData contextData = getSpiContextData();
        SpiAspspConsentDataProvider spiAspspConsentDataProvider = aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId);
        ScaStatus scaStatus = cmsConsentScaStatusResponse.getBody().getScaStatus();
        SpiPiisConsent spiPiisConsent = xs2aToSpiPiisConsentMapper.mapToSpiPiisConsent(cmsConsentScaStatusResponse.getBody().getPiisConsent());

        SpiResponse<SpiScaStatusResponse> spiScaStatusResponse = piisConsentSpi.getScaStatus(scaStatus, contextData, authorisationId, spiPiisConsent, spiAspspConsentDataProvider);

        if (spiScaStatusResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiScaStatusResponse, ServiceType.PIIS);
            log.info("Authorisation-ID [{}], PIIS consent-ID [{}]. Get SCA status failed.", authorisationId, consentId);
            return ResponseObject.<Xs2aScaStatusResponse>builder()
                       .fail(errorHolder)
                       .build();
        }

        SpiScaStatusResponse spiScaInformationPayload = spiScaStatusResponse.getPayload();
        if (scaStatus.isNotFinalisedStatus() && scaStatus != spiScaInformationPayload.getScaStatus()) {
            scaStatus = spiScaInformationPayload.getScaStatus();
            xs2aAuthorisationService.updateAuthorisationStatus(authorisationId, scaStatus);
            log.info("Authorisation-ID [{}], PIIS consent-ID [{}]. SCA status was changed to [{}] from SPI.", authorisationId, consentId, scaStatus);
        }

        return ResponseObject.<Xs2aScaStatusResponse>builder()
                   .body(new Xs2aScaStatusResponse(scaStatus, null, spiScaInformationPayload.getPsuMessage(),
                                                   spiToXs2aLinksMapper.toXs2aLinks(spiScaInformationPayload.getLinks()),
                                                   spiScaInformationPayload.getTppMessageInformation()
                   ))
                   .build();
    }

    public ResponseObject<Void> deleteAccountConsentsById(String consentId) {
        xs2aEventService.recordConsentTppRequest(consentId, EventType.DELETE_PIIS_CONSENT_REQUEST_RECEIVED);
        ResponseObject.ResponseBuilder<Void> responseBuilder = ResponseObject.builder();
        Optional<PiisConsent> piisConsentById = xs2aPiisConsentService.getPiisConsentById(consentId);

        if (piisConsentById.isEmpty()) {
            log.info("Consent-ID: [{}]. Delete PIIS consent failed: initial consent not found by id", consentId);
            return responseBuilder
                       .fail(ErrorType.PIIS_403, of(MessageErrorCode.CONSENT_UNKNOWN_403))
                       .build();
        }

        PiisConsent piisConsent = piisConsentById.get();

        ValidationResult validationResult = confirmationOfFundsConsentValidationService.validateConsentOnDelete(piisConsent);
        if (validationResult.isNotValid()) {
            log.info("Consent-ID: [{}]. Delete Confirmation of Funds Consent - validation failed: {}", piisConsent.getId(), validationResult.getMessageError());
            return ResponseObject.<Void>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        SpiContextData contextData = getSpiContextData();

        SpiAspspConsentDataProvider aspspDataProvider = aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId);
        SpiPiisConsent spiPiisConsent = xs2aToSpiPiisConsentMapper.mapToSpiPiisConsent(piisConsent);
        SpiResponse<SpiResponse.VoidResponse> revokePiisConsentResponse = piisConsentSpi.revokePiisConsent(contextData, spiPiisConsent, aspspDataProvider);

        if (revokePiisConsentResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(revokePiisConsentResponse, ServiceType.PIIS);
            log.info("Consent-ID: [{}]. Delete Confirmation of Funds Consent failed: Couldn't revoke PIIS consent at SPI level: {}", piisConsent.getId(), errorHolder);
            return ResponseObject.<Void>builder()
                       .fail(new MessageError(errorHolder))
                       .build();
        }

        ConsentStatus newConsentStatus = piisConsent.getConsentStatus() == ConsentStatus.RECEIVED
                                             ? ConsentStatus.REJECTED
                                             : ConsentStatus.TERMINATED_BY_TPP;

        xs2aPiisConsentService.updateConsentStatus(consentId, newConsentStatus);
        return ResponseObject.<Void>builder().build();
    }

    public ResponseObject<AuthorisationResponse> createPiisAuthorisation(PsuIdData psuData, String consentId, String password) {
        return piisConsentAuthorisationService.createPiisAuthorisation(psuData, consentId, password);
    }

    private void updateMultilevelSca(String consentId, boolean multilevelScaRequired) {
        // default value is false, so we do the call only for non-default (true) case
        if (multilevelScaRequired) {
            xs2aPiisConsentService.updateMultilevelScaRequired(consentId, multilevelScaRequired);
        }
    }

    public ResponseObject<UpdateConsentPsuDataResponse> updateConsentPsuData(ConsentAuthorisationsParameters updatePsuData) {
        return piisConsentAuthorisationService.updateConsentPsuData(updatePsuData);
    }

    private SpiContextData getSpiContextData() {
        PsuIdData psuIdData = requestProviderService.getPsuIdData();
        log.info("Corresponding PSU-ID {} was provided from request.", psuIdData);
        return spiContextDataProvider.provideWithPsuIdData(psuIdData);
    }

    private SpiResponse<SpiConsentStatusResponse> getConsentStatusFromSpi(PiisConsent piisConsent, String consentId) {
        SpiPiisConsent spiPiisConsent = xs2aToSpiPiisConsentMapper.mapToSpiPiisConsent(piisConsent);
        SpiAspspConsentDataProvider aspspDataProvider = aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId);
        return piisConsentSpi.getConsentStatus(spiContextDataProvider.provide(), spiPiisConsent, aspspDataProvider);
    }

    private void proceedImplicitCaseForCreateConsent(Xs2aConfirmationOfFundsResponse createConsentResponse, PsuIdData psuIdData, String consentId) {
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
        PiisAuthorisationProcessorRequest processorRequest = new PiisAuthorisationProcessorRequest(scaApproach, scaStatus, startAuthorisationsParameters, authorisation);
        CreateConsentAuthorisationProcessorResponse processorResponse =
            (CreateConsentAuthorisationProcessorResponse) authorisationChainResponsibilityService.apply(processorRequest);

        loggingContextService.storeScaStatus(processorResponse.getScaStatus());

        Xs2aCreateAuthorisationRequest createAuthorisationRequest = Xs2aCreateAuthorisationRequest.builder()
                                                                        .psuData(psuIdData)
                                                                        .consentId(consentId)
                                                                        .authorisationId(authorisationId)
                                                                        .scaStatus(processorResponse.getScaStatus())
                                                                        .scaApproach(processorResponse.getScaApproach())
                                                                        .build();

        ConsentAuthorizationService service = piisScaAuthorisationServiceResolver.getService();
        Optional<CreateConsentAuthorizationResponse> consentAuthorizationResponse = service.createConsentAuthorization(createAuthorisationRequest);

        if (consentAuthorizationResponse.isPresent()) {
            CreateConsentAuthorizationResponse createConsentAuthorizationResponse = consentAuthorizationResponse.get();
            createConsentResponse.setAuthorizationId(createConsentAuthorizationResponse.getAuthorisationId());
            createConsentResponse.setScaStatus(createConsentAuthorizationResponse.getScaStatus());
            createConsentResponse.setScaApproach(createConsentAuthorizationResponse.getScaApproach());

            setPsuMessageAndTppMessages(createConsentResponse, processorResponse.getPsuMessage(), processorResponse.getTppMessages());

            loggingContextService.storeScaStatus(createConsentAuthorizationResponse.getScaStatus());
        }
    }

    private void setPsuMessageAndTppMessages(Xs2aConfirmationOfFundsResponse response,
                                             String psuMessage, Set<TppMessageInformation> tppMessageInformationSet) {
        if (psuMessage != null) {
            response.setPsuMessage(psuMessage);
        }
        if (tppMessageInformationSet != null) {
            response.getTppMessageInformation().addAll(tppMessageInformationSet);
        }
    }
}
