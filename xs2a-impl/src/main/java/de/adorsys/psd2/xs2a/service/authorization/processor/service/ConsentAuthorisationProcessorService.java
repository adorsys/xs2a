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

package de.adorsys.psd2.xs2a.service.authorization.processor.service;

import de.adorsys.psd2.core.data.Consent;
import de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.authorisation.CommonAuthorisationParameters;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.*;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiVerifyScaAuthorisationResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings("PMD.TooManyMethods")
@Slf4j
public abstract class ConsentAuthorisationProcessorService<T extends Consent> extends BaseAuthorisationProcessorService {
    private static final String CONSENT_NOT_FOUND_LOG_MESSAGE = "Apply authorisation when update consent PSU data has failed. Consent not found by id.";
    private static final String PSU_CREDENTIALS_INVALID = "Authorise PSU when apply authorisation has failed. PSU credentials invalid.";

    private final Xs2aAuthorisationService authorisationService;
    private final SpiContextDataProvider spiContextDataProvider;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final SpiErrorMapper spiErrorMapper;
    private final Xs2aToSpiPsuDataMapper psuDataMapper;

    protected ConsentAuthorisationProcessorService(Xs2aAuthorisationService authorisationService,
                                                   SpiContextDataProvider spiContextDataProvider,
                                                   SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory,
                                                   SpiErrorMapper spiErrorMapper, Xs2aToSpiPsuDataMapper psuDataMapper) {
        this.authorisationService = authorisationService;
        this.spiContextDataProvider = spiContextDataProvider;
        this.aspspConsentDataProviderFactory = aspspConsentDataProviderFactory;
        this.spiErrorMapper = spiErrorMapper;
        this.psuDataMapper = psuDataMapper;
    }

    @Override
    public AuthorisationProcessorResponse doScaStarted(AuthorisationProcessorRequest authorisationProcessorRequest) {
        CommonAuthorisationParameters updateAuthorisationRequest = authorisationProcessorRequest.getUpdateAuthorisationRequest();
        PsuIdData psuIdData = updateAuthorisationRequest.getPsuData();
        String consentId = updateAuthorisationRequest.getBusinessObjectId();
        ScaApproach scaApproach = authorisationProcessorRequest.getScaApproach();

        Optional<T> consentOptional = getConsentByIdFromCms(consentId);
        if (consentOptional.isEmpty()) {
            ErrorHolder errorHolder = ErrorHolder.builder(getErrorType400())
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_400))
                                          .build();
            writeErrorLog(authorisationProcessorRequest, psuIdData, errorHolder, CONSENT_NOT_FOUND_LOG_MESSAGE);
            return new CreateConsentAuthorisationProcessorResponse(errorHolder, scaApproach, consentId, psuIdData);
        }

        SpiResponse<SpiStartAuthorisationResponse> spiResponse = getSpiStartAuthorisationResponse(spiContextDataProvider.provideWithPsuIdData(psuIdData),
                                                                                                  scaApproach,
                                                                                                  authorisationProcessorRequest.getScaStatus(),
                                                                                                  updateAuthorisationRequest.getAuthorisationId(),
                                                                                                  consentOptional.get(),
                                                                                                  aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId));

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, getServiceType());
            writeErrorLog(authorisationProcessorRequest, psuIdData, errorHolder, "Verify SCA authorisation failed when create authorisation.");
            return new CreateConsentAuthorisationProcessorResponse(errorHolder, scaApproach, consentId, psuIdData);
        }

        SpiStartAuthorisationResponse startAuthorisationResponse = spiResponse.getPayload();

        ScaStatus scaStatusFromSpi = startAuthorisationResponse.getScaStatus();
        ScaApproach scaApproachFromSpi = startAuthorisationResponse.getScaApproach();
        String psuMessage = startAuthorisationResponse.getPsuMessage();
        Set<TppMessageInformation> tppMessages = startAuthorisationResponse.getTppMessages();

        return new CreateConsentAuthorisationProcessorResponse(scaStatusFromSpi, scaApproachFromSpi, psuMessage, tppMessages, consentId, psuIdData);
    }

    protected abstract SpiResponse<SpiStartAuthorisationResponse> getSpiStartAuthorisationResponse(SpiContextData spiContextData, ScaApproach scaApproach, ScaStatus scaStatus, String authorisationId, T consent, SpiAspspConsentDataProvider spiAspspConsentDataProvider);

    @Override
    public AuthorisationProcessorResponse doScaReceived(AuthorisationProcessorRequest authorisationProcessorRequest) {
        return doScaPsuIdentified(authorisationProcessorRequest);
    }

    @Override
    public AuthorisationProcessorResponse doScaPsuIdentified(AuthorisationProcessorRequest authorisationProcessorRequest) {
        CommonAuthorisationParameters request = authorisationProcessorRequest.getUpdateAuthorisationRequest();
        return request.isUpdatePsuIdentification()
                   ? applyIdentification(authorisationProcessorRequest)
                   : applyAuthorisation(authorisationProcessorRequest);
    }

    @Override
    public AuthorisationProcessorResponse doScaPsuAuthenticated(AuthorisationProcessorRequest authorisationProcessorRequest) {
        CommonAuthorisationParameters request = authorisationProcessorRequest.getUpdateAuthorisationRequest();
        String consentId = request.getBusinessObjectId();
        String authorisationId = request.getAuthorisationId();

        Optional<T> consentOptional = getConsentByIdFromCms(consentId);
        if (consentOptional.isEmpty()) {
            ErrorHolder errorHolder = ErrorHolder.builder(getErrorType400())
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_400))
                                          .build();
            writeErrorLog(authorisationProcessorRequest, request.getPsuData(), errorHolder, CONSENT_NOT_FOUND_LOG_MESSAGE);
            return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId, request.getPsuData());
        }

        PsuIdData psuData = extractPsuIdData(request, authorisationProcessorRequest.getAuthorisation());
        T consent = consentOptional.get();

        String authenticationMethodId = request.getAuthenticationMethodId();
        if (isDecoupledApproach(request.getAuthorisationId(), authenticationMethodId)) {
            authorisationService.updateScaApproach(request.getAuthorisationId(), ScaApproach.DECOUPLED);
            return proceedDecoupledApproach(request.getBusinessObjectId(), request.getAuthorisationId(), consent, authenticationMethodId, psuData);
        }

        return proceedEmbeddedApproach(authorisationProcessorRequest, authenticationMethodId, consent, psuData);
    }

    @Override
    public AuthorisationProcessorResponse doScaMethodSelected(AuthorisationProcessorRequest authorisationProcessorRequest) {
        CommonAuthorisationParameters request = authorisationProcessorRequest.getUpdateAuthorisationRequest();
        String consentId = request.getBusinessObjectId();
        String authorisationId = request.getAuthorisationId();
        Optional<T> consentOptional = getConsentByIdFromCms(consentId);

        if (consentOptional.isEmpty()) {
            ErrorHolder errorHolder = ErrorHolder.builder(getErrorType400())
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_400))
                                          .build();
            writeErrorLog(authorisationProcessorRequest, request.getPsuData(), errorHolder, CONSENT_NOT_FOUND_LOG_MESSAGE);
            return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId, request.getPsuData());
        }
        T consent = consentOptional.get();

        PsuIdData psuData = extractPsuIdData(request, authorisationProcessorRequest.getAuthorisation());

        SpiResponse<SpiVerifyScaAuthorisationResponse> spiResponse = verifyScaAuthorisation(spiContextDataProvider.provideWithPsuIdData(psuData),
                                                                                            request,
                                                                                            psuData,
                                                                                            consent,
                                                                                            aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId));

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, getServiceType());
            writeErrorLog(authorisationProcessorRequest, request.getPsuData(), errorHolder, "Verify SCA authorisation failed when update PSU data.");

            SpiVerifyScaAuthorisationResponse spiAuthorisationResponse = spiResponse.getPayload();
            return getSpiErrorResponse(authorisationProcessorRequest, consentId, authorisationId, psuData, errorHolder, spiAuthorisationResponse);
        }

        ConsentStatus responseConsentStatus = spiResponse.getPayload().getConsentStatus();

        if (ConsentStatus.PARTIALLY_AUTHORISED == responseConsentStatus && !consent.isMultilevelScaRequired()) {
            updateMultilevelScaRequired(consentId, true);
        }

        if (consent.getConsentStatus() != responseConsentStatus) {
            updateConsentStatus(consentId, responseConsentStatus);
        }

        findAndTerminateOldConsents(consentId, consent);

        return new UpdateConsentPsuDataResponse(ScaStatus.FINALISED, consentId, request.getAuthorisationId(), psuData);
    }

    private AuthorisationProcessorResponse getSpiErrorResponse(AuthorisationProcessorRequest authorisationProcessorRequest, String consentId, String authorisationId, PsuIdData psuData, ErrorHolder errorHolder, SpiVerifyScaAuthorisationResponse spiAuthorisationResponse) {
        if (spiAuthorisationResponse != null && spiAuthorisationResponse.getSpiAuthorisationStatus() == SpiAuthorisationStatus.ATTEMPT_FAILURE) {
            return new UpdateConsentPsuDataResponse(authorisationProcessorRequest.getScaStatus(), errorHolder, consentId, authorisationId, psuData);
        }

        Optional<MessageErrorCode> first = errorHolder.getFirstErrorCode();
        if (first.isPresent() && first.get() == MessageErrorCode.PSU_CREDENTIALS_INVALID) {
            authorisationService.updateAuthorisationStatus(authorisationId, ScaStatus.FAILED);
        }
        return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId, psuData);
    }

    @Override
    public AuthorisationProcessorResponse doScaFinalised(AuthorisationProcessorRequest authorisationProcessorRequest) {
        CommonAuthorisationParameters request = authorisationProcessorRequest.getUpdateAuthorisationRequest();
        return new UpdateConsentPsuDataResponse(ScaStatus.FINALISED, request.getBusinessObjectId(), request.getAuthorisationId(), request.getPsuData());
    }

    private UpdateConsentPsuDataResponse proceedEmbeddedApproach(AuthorisationProcessorRequest authorisationProcessorRequest, String authenticationMethodId, T consent, PsuIdData psuData) {
        CommonAuthorisationParameters request = authorisationProcessorRequest.getUpdateAuthorisationRequest();
        SpiResponse<SpiAuthorizationCodeResult> spiResponse = requestAuthorisationCode(spiContextDataProvider.provideWithPsuIdData(psuData),
                                                                                       authenticationMethodId,
                                                                                       consent,
                                                                                       aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(request.getBusinessObjectId()));


        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, getServiceType());
            writeErrorLog(authorisationProcessorRequest, psuData, errorHolder, "Proceed embedded approach when performs authorisation depending on selected SCA method has failed.");

            Optional<MessageErrorCode> first = errorHolder.getFirstErrorCode();
            if (first.isPresent() && first.get() == MessageErrorCode.PSU_CREDENTIALS_INVALID) {
                authorisationService.updateAuthorisationStatus(request.getAuthorisationId(), ScaStatus.FAILED);
            }
            return new UpdateConsentPsuDataResponse(errorHolder, request.getBusinessObjectId(), request.getAuthorisationId(), psuData);
        }

        SpiAuthorizationCodeResult authorizationCodeResult = spiResponse.getPayload();
        ScaStatus scaStatus = authorizationCodeResult.isScaExempted() ?
                                  ScaStatus.EXEMPTED :
                                  ObjectUtils.defaultIfNull(authorizationCodeResult.getScaStatus(), ScaStatus.SCAMETHODSELECTED);

        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse(
            scaStatus, request.getBusinessObjectId(), request.getAuthorisationId(), psuData);
        response.setChosenScaMethod(authorizationCodeResult.getSelectedScaMethod());
        response.setChallengeData(authorizationCodeResult.getChallengeData());
        return response;
    }

    private UpdateConsentPsuDataResponse applyAuthorisation(AuthorisationProcessorRequest authorisationProcessorRequest) {
        CommonAuthorisationParameters request = authorisationProcessorRequest.getUpdateAuthorisationRequest();
        String consentId = request.getBusinessObjectId();
        String authorisationId = request.getAuthorisationId();
        Optional<T> consentOptional = getConsentByIdFromCms(consentId);

        if (consentOptional.isEmpty()) {
            ErrorHolder errorHolder = ErrorHolder.builder(getErrorType400())
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_400))
                                          .build();
            writeErrorLog(authorisationProcessorRequest, request.getPsuData(), errorHolder, CONSENT_NOT_FOUND_LOG_MESSAGE);
            return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId, request.getPsuData());
        }

        Authorisation authorisation = authorisationProcessorRequest.getAuthorisation();
        PsuIdData psuData = extractPsuIdData(request, authorisation);
        T consent = consentOptional.get();
        SpiResponse<SpiPsuAuthorisationResponse> authorisationStatusSpiResponse = authorisePsu(spiContextDataProvider.provideWithPsuIdData(psuData),
                                                                                               authorisation.getAuthorisationId(),
                                                                                               psuDataMapper.mapToSpiPsuData(psuData),
                                                                                               request.getPassword(),
                                                                                               consent,
                                                                                               aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId));

        SpiPsuAuthorisationResponse spiAuthorisationResponse = authorisationStatusSpiResponse.getPayload();
        if (authorisationStatusSpiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(authorisationStatusSpiResponse, getServiceType());
            writeErrorLog(authorisationProcessorRequest, psuData, errorHolder, "Authorise PSU when apply authorisation has failed.");

            if (spiAuthorisationResponse != null && spiAuthorisationResponse.getSpiAuthorisationStatus() == SpiAuthorisationStatus.ATTEMPT_FAILURE) {
                return new UpdateConsentPsuDataResponse(authorisationProcessorRequest.getScaStatus(), errorHolder, consentId, authorisationId, psuData);
            }
            return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId, psuData);
        }

        if (spiAuthorisationResponse != null && spiAuthorisationResponse.getSpiAuthorisationStatus() == SpiAuthorisationStatus.FAILURE) {
            ErrorHolder errorHolder = ErrorHolder.builder(getErrorType401())
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.PSU_CREDENTIALS_INVALID))
                                          .build();
            writeErrorLog(authorisationProcessorRequest, psuData, errorHolder, PSU_CREDENTIALS_INVALID);
            authorisationService.updateAuthorisationStatus(authorisationId, ScaStatus.FAILED);
            return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId, psuData);
        }

        if (isOneFactorAuthorisation(consent)) {
            updateConsentStatus(consentId, ConsentStatus.VALID);
            return new UpdateConsentPsuDataResponse(ScaStatus.FINALISED, consentId, authorisationId, psuData);
        }

        return requestAvailableScaMethods(authorisationProcessorRequest, consentId, authorisationId, psuData, consent);
    }

    private UpdateConsentPsuDataResponse requestAvailableScaMethods(AuthorisationProcessorRequest authorisationProcessorRequest, String consentId, String authorisationId, PsuIdData psuData, T consent) {
        SpiResponse<SpiAvailableScaMethodsResponse> spiResponse = requestAvailableScaMethods(spiContextDataProvider.provideWithPsuIdData(psuData),
                                                                                             consent,
                                                                                             aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId));

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, getServiceType());
            writeErrorLog(authorisationProcessorRequest, psuData, errorHolder, "Request available SCA methods when apply authorisation has failed.");
            return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId, psuData);
        }

        List<AuthenticationObject> availableScaMethods = spiResponse.getPayload().getAvailableScaMethods();

        return processScaMethods(authorisationProcessorRequest, consentId, authorisationId, psuData, consent, availableScaMethods);
    }

    private UpdateConsentPsuDataResponse processScaMethods(AuthorisationProcessorRequest authorisationProcessorRequest, String consentId, String authorisationId, PsuIdData psuData, T consent, List<AuthenticationObject> availableScaMethods) {
        if (CollectionUtils.isEmpty(availableScaMethods)) {
            return buildUpdateResponseWithoutSca(authorisationProcessorRequest, consentId, authorisationId, psuData);
        } else if (isMultipleScaMethods(availableScaMethods)) {
            return createResponseForMultipleAvailableMethods(availableScaMethods, authorisationId, consentId, psuData);
        } else {
            return createResponseForOneAvailableMethod(authorisationProcessorRequest, consent, availableScaMethods, psuData);
        }
    }

    private UpdateConsentPsuDataResponse buildUpdateResponseWithoutSca(AuthorisationProcessorRequest authorisationProcessorRequest,
                                                                       String consentId, String authorisationId, PsuIdData psuData) {
        ErrorHolder errorHolder = ErrorHolder.builder(getErrorType400())
                                      .tppMessages(TppMessageInformation.of(MessageErrorCode.SCA_METHOD_UNKNOWN))
                                      .build();
        writeErrorLog(authorisationProcessorRequest, psuData, errorHolder, "Apply authorisation has failed. Consent was rejected because PSU has no available SCA methods.");
        updateConsentStatus(consentId, ConsentStatus.REJECTED);
        authorisationService.updateAuthorisationStatus(authorisationId, ScaStatus.FAILED);
        return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId, psuData);
    }

    private UpdateConsentPsuDataResponse createResponseForMultipleAvailableMethods(List<AuthenticationObject> availableScaMethods,
                                                                                   String authorisationId,
                                                                                   String consentId,
                                                                                   PsuIdData psuIdData) {
        authorisationService.saveAuthenticationMethods(authorisationId, availableScaMethods);
        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse(ScaStatus.PSUAUTHENTICATED, consentId, authorisationId, psuIdData);
        response.setAvailableScaMethods(availableScaMethods);
        return response;
    }

    private UpdateConsentPsuDataResponse createResponseForOneAvailableMethod(AuthorisationProcessorRequest authorisationProcessorRequest, T consent,
                                                                             List<AuthenticationObject> availableScaMethods, PsuIdData psuData) {
        CommonAuthorisationParameters request = authorisationProcessorRequest.getUpdateAuthorisationRequest();
        authorisationService.saveAuthenticationMethods(request.getAuthorisationId(), availableScaMethods);
        AuthenticationObject chosenMethod = availableScaMethods.get(0);

        if (chosenMethod.isDecoupled()) {
            authorisationService.updateScaApproach(request.getAuthorisationId(), ScaApproach.DECOUPLED);
            return proceedDecoupledApproach(request.getBusinessObjectId(), request.getAuthorisationId(), consent, chosenMethod.getAuthenticationMethodId(), psuData);
        }

        return proceedEmbeddedApproach(authorisationProcessorRequest, chosenMethod.getAuthenticationMethodId(), consent, psuData);
    }

    private UpdateConsentPsuDataResponse applyIdentification(AuthorisationProcessorRequest authorisationProcessorRequest) {
        CommonAuthorisationParameters request = authorisationProcessorRequest.getUpdateAuthorisationRequest();
        if (!isPsuExist(request.getPsuData())) {
            ErrorHolder errorHolder = ErrorHolder.builder(getErrorType400())
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_NO_PSU))
                                          .build();
            writeErrorLog(authorisationProcessorRequest, request.getPsuData(), errorHolder, "Apply identification when update consent PSU data has failed. No PSU data available in request.");
            return new UpdateConsentPsuDataResponse(errorHolder, request.getBusinessObjectId(), request.getAuthorisationId(), request.getPsuData());
        }

        return new UpdateConsentPsuDataResponse(ScaStatus.PSUIDENTIFIED, request.getBusinessObjectId(), request.getAuthorisationId(), request.getPsuData());
    }

    private boolean isDecoupledApproach(String authorisationId, String authenticationMethodId) {
        return authorisationService.isAuthenticationMethodDecoupled(authorisationId, authenticationMethodId);
    }

    private void writeErrorLog(AuthorisationProcessorRequest request, PsuIdData psuData, ErrorHolder errorHolder, String message) {
        String messageToLog = String.format("Consent-ID [{}], Authorisation-ID [{}], PSU-ID [{}], SCA Approach [{}]. %s Error msg: [{}]", message);
        log.warn(messageToLog,
                 request.getUpdateAuthorisationRequest().getBusinessObjectId(),
                 request.getUpdateAuthorisationRequest().getAuthorisationId(),
                 psuData != null ? psuData.getPsuId() : "-",
                 request.getScaApproach(),
                 errorHolder);
    }

    abstract UpdateConsentPsuDataResponse proceedDecoupledApproach(String consentId, String authorisationId, T consent, String authenticationMethodId, PsuIdData psuData);

    abstract boolean isOneFactorAuthorisation(T consent);

    abstract SpiResponse<SpiAvailableScaMethodsResponse> requestAvailableScaMethods(SpiContextData spiContextData, T consent, SpiAspspConsentDataProvider spiAspspConsentDataProvider);

    abstract SpiResponse<SpiPsuAuthorisationResponse> authorisePsu(SpiContextData spiContextData, String authorisationId, SpiPsuData spiPsuData, String password, T consent, SpiAspspConsentDataProvider spiAspspConsentDataProvider);

    abstract void findAndTerminateOldConsents(String encryptedNewConsentId, T consent);

    abstract void updateConsentStatus(String consentId, ConsentStatus responseConsentStatus);

    abstract void updateMultilevelScaRequired(String consentId, boolean multilevelScaRequired);

    abstract ServiceType getServiceType();

    abstract SpiResponse<SpiVerifyScaAuthorisationResponse> verifyScaAuthorisation(SpiContextData spiContextData, CommonAuthorisationParameters request, PsuIdData psuData, T consent, SpiAspspConsentDataProvider spiAspspConsentDataProvider);

    abstract Optional<T> getConsentByIdFromCms(String consentId);

    abstract ErrorType getErrorType400();

    abstract ErrorType getErrorType401();

    abstract SpiResponse<SpiAuthorizationCodeResult> requestAuthorisationCode(SpiContextData spiContextData, String authenticationMethodId, T consent, SpiAspspConsentDataProvider spiAspspConsentDataProvider);
}
