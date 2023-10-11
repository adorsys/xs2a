/*
 * Copyright 2018-2023 adorsys GmbH & Co KG
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
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.*;
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

@SuppressWarnings({"PMD.TooManyMethods", "PMD.GodClass"})
@Slf4j
public abstract class ConsentAuthorisationProcessorService<T extends Consent> extends BaseAuthorisationProcessorService {
    protected static final String AUTHORISATION_FOR_CONSENT_HAS_FAILED = "Start authorisation for consent has failed.";
    private static final String PSU_CREDENTIALS_INVALID = "Apply authorisation has failed, PSU credentials invalid.";

    protected final SpiToXs2aTppMessageInformationMapper spiToXs2aTppMessageInformationMapper;

    private final SpiContextDataProvider spiContextDataProvider;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final SpiErrorMapper spiErrorMapper;
    private final SpiToXs2aScaStatusMapper spiToXs2aScaStatusMapper;
    private final SpiToXs2aScaApproachMapper spiToXs2aScaApproachMapper;
    private final Xs2aAuthorisationService authorisationService;
    private final SpiToXs2aConsentStatusMapper spiToXs2aConsentStatusMapper;
    private final SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper;
    private final SpiToXs2aChallengeDataMapper spiToXs2aChallengeDataMapper;
    private final Xs2aToSpiPsuDataMapper psuDataMapper;

    protected ConsentAuthorisationProcessorService(Xs2aAuthorisationService authorisationService,
                                                   SpiContextDataProvider spiContextDataProvider,
                                                   SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory,
                                                   SpiErrorMapper spiErrorMapper,
                                                   SpiToXs2aScaStatusMapper spiToXs2aScaStatusMapper,
                                                   SpiToXs2aScaApproachMapper spiToXs2aScaApproachMapper,
                                                   SpiToXs2aTppMessageInformationMapper spiToXs2aTppMessageInformationMapper,
                                                   SpiToXs2aConsentStatusMapper spiToXs2aConsentStatusMapper,
                                                   SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper,
                                                   SpiToXs2aChallengeDataMapper spiToXs2aChallengeDataMapper,
                                                   Xs2aToSpiPsuDataMapper psuDataMapper) {
        super(spiToXs2aChallengeDataMapper);
        this.authorisationService = authorisationService;
        this.spiContextDataProvider = spiContextDataProvider;
        this.aspspConsentDataProviderFactory = aspspConsentDataProviderFactory;
        this.spiErrorMapper = spiErrorMapper;
        this.psuDataMapper = psuDataMapper;
        this.spiToXs2aScaStatusMapper = spiToXs2aScaStatusMapper;
        this.spiToXs2aScaApproachMapper = spiToXs2aScaApproachMapper;
        this.spiToXs2aTppMessageInformationMapper = spiToXs2aTppMessageInformationMapper;
        this.spiToXs2aConsentStatusMapper = spiToXs2aConsentStatusMapper;
        this.spiToXs2aAuthenticationObjectMapper = spiToXs2aAuthenticationObjectMapper;
        this.spiToXs2aChallengeDataMapper = spiToXs2aChallengeDataMapper;
    }

    // Common functionality for AIS and PIIS consent authorisation.
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
            writeErrorLog(authorisationProcessorRequest, psuIdData, errorHolder, AUTHORISATION_FOR_CONSENT_HAS_FAILED);
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
            writeErrorLog(authorisationProcessorRequest, psuIdData, errorHolder, "Start authorisation has failed on SPI level.");
            return new CreateConsentAuthorisationProcessorResponse(errorHolder, scaApproach, consentId, psuIdData);
        }

        SpiStartAuthorisationResponse startAuthorisationResponse = spiResponse.getPayload();

        ScaStatus scaStatusFromSpi = spiToXs2aScaStatusMapper.mapToXs2aScaStatus(startAuthorisationResponse.getScaStatus());
        ScaApproach scaApproachFromSpi = spiToXs2aScaApproachMapper.mapToXs2aScaApproach(startAuthorisationResponse.getScaApproach());

        return processAuthorisation(authorisationProcessorRequest, startAuthorisationResponse, scaStatusFromSpi, scaApproachFromSpi);
    }

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
            writeErrorLog(authorisationProcessorRequest, request.getPsuData(), errorHolder, AUTHORISATION_FOR_CONSENT_HAS_FAILED);
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
            writeErrorLog(authorisationProcessorRequest, request.getPsuData(), errorHolder, AUTHORISATION_FOR_CONSENT_HAS_FAILED);
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

        ConsentStatus responseConsentStatus = spiToXs2aConsentStatusMapper.mapToXs2aConsentStatus(spiResponse.getPayload().getConsentStatus());

        if (ConsentStatus.PARTIALLY_AUTHORISED == responseConsentStatus && !consent.isMultilevelScaRequired()) {
            updateMultilevelScaRequired(consentId, true);
        }

        if (consent.getConsentStatus() != responseConsentStatus) {
            updateConsentStatus(consentId, responseConsentStatus);
        }

        findAndTerminateOldConsents(consentId, consent);

        return new UpdateConsentPsuDataResponse(ScaStatus.FINALISED, consentId, request.getAuthorisationId(), psuData);
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
            writeErrorLog(authorisationProcessorRequest, psuData, errorHolder, "Authorisation has failed on selected SCA method in Embedded approach.");

            Optional<MessageErrorCode> first = errorHolder.getFirstErrorCode();
            if (first.isPresent() && first.get() == MessageErrorCode.PSU_CREDENTIALS_INVALID) {
                authorisationService.updateAuthorisationStatus(request.getAuthorisationId(), ScaStatus.FAILED);
            }
            return new UpdateConsentPsuDataResponse(errorHolder, request.getBusinessObjectId(), request.getAuthorisationId(), psuData);
        }

        SpiAuthorizationCodeResult authorizationCodeResult = spiResponse.getPayload();
        ScaStatus scaStatus = authorizationCodeResult.isScaExempted() ?
                                  ScaStatus.EXEMPTED :
                                  ObjectUtils.defaultIfNull(spiToXs2aScaStatusMapper.mapToXs2aScaStatus(authorizationCodeResult.getScaStatus()), ScaStatus.SCAMETHODSELECTED);

        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse(
            scaStatus, request.getBusinessObjectId(), request.getAuthorisationId(), psuData);
        response.setChosenScaMethod(spiToXs2aAuthenticationObjectMapper.toAuthenticationObject(authorizationCodeResult.getSelectedScaMethod()));
        response.setChallengeData(spiToXs2aChallengeDataMapper.toChallengeData(authorizationCodeResult.getChallengeData()));
        return response;
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

    private UpdateConsentPsuDataResponse applyAuthorisation(AuthorisationProcessorRequest authorisationProcessorRequest) {
        CommonAuthorisationParameters request = authorisationProcessorRequest.getUpdateAuthorisationRequest();
        String consentId = request.getBusinessObjectId();
        String authorisationId = request.getAuthorisationId();
        Optional<T> consentOptional = getConsentByIdFromCms(consentId);

        if (consentOptional.isEmpty()) {
            ErrorHolder errorHolder = ErrorHolder.builder(getErrorType400())
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_400))
                                          .build();
            writeErrorLog(authorisationProcessorRequest, request.getPsuData(), errorHolder, AUTHORISATION_FOR_CONSENT_HAS_FAILED);
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
            writeErrorLog(authorisationProcessorRequest, psuData, errorHolder, "PSU authorisation on SPI has failed.");

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
            writeErrorLog(authorisationProcessorRequest, psuData, errorHolder, "Request available SCA methods on SPI has failed.");
            return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId, psuData);
        }

        List<AuthenticationObject> availableScaMethods = spiToXs2aAuthenticationObjectMapper.toAuthenticationObjectList(spiResponse.getPayload().getAvailableScaMethods());

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
        writeErrorLog(authorisationProcessorRequest, psuData, errorHolder, "Consent was rejected, PSU has no available SCA methods.");
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
            writeErrorLog(authorisationProcessorRequest, request.getPsuData(), errorHolder, "Apply identification during PSU data updating has failed, no PSU data in request.");
            return new UpdateConsentPsuDataResponse(errorHolder, request.getBusinessObjectId(), request.getAuthorisationId(), request.getPsuData());
        }

        return new UpdateConsentPsuDataResponse(ScaStatus.PSUIDENTIFIED, request.getBusinessObjectId(), request.getAuthorisationId(), request.getPsuData());
    }

    private boolean isDecoupledApproach(String authorisationId, String authenticationMethodId) {
        return authorisationService.isAuthenticationMethodDecoupled(authorisationId, authenticationMethodId);
    }

    protected void writeErrorLog(AuthorisationProcessorRequest request, PsuIdData psuData, ErrorHolder errorHolder, String message) {
        String messageToLog = String.format("Consent-ID [{}], Authorisation-ID [{}], PSU-ID [{}], SCA Approach [{}]. %s Error msg: [{}]", message);
        log.warn(messageToLog,
                 request.getUpdateAuthorisationRequest().getBusinessObjectId(),
                 request.getUpdateAuthorisationRequest().getAuthorisationId(),
                 psuData != null ? psuData.getPsuId() : "-",
                 request.getScaApproach(),
                 errorHolder);
    }

    protected abstract AuthorisationProcessorResponse processAuthorisation(AuthorisationProcessorRequest authorisationProcessorRequest,
                                                                           SpiStartAuthorisationResponse startAuthorisationResponse,
                                                                           ScaStatus scaStatusFromSpi,
                                                                           ScaApproach scaApproachFromSpi);

    protected abstract SpiResponse<SpiStartAuthorisationResponse> getSpiStartAuthorisationResponse(SpiContextData spiContextData,
                                                                                                   ScaApproach scaApproach,
                                                                                                   ScaStatus scaStatus,
                                                                                                   String authorisationId,
                                                                                                   T consent,
                                                                                                   SpiAspspConsentDataProvider spiAspspConsentDataProvider);

    abstract UpdateConsentPsuDataResponse proceedDecoupledApproach(String consentId,
                                                                   String authorisationId,
                                                                   T consent,
                                                                   String authenticationMethodId,
                                                                   PsuIdData psuData);

    abstract boolean isOneFactorAuthorisation(T consent);

    abstract SpiResponse<SpiAvailableScaMethodsResponse> requestAvailableScaMethods(SpiContextData spiContextData,
                                                                                    T consent,
                                                                                    SpiAspspConsentDataProvider spiAspspConsentDataProvider);

    abstract SpiResponse<SpiPsuAuthorisationResponse> authorisePsu(SpiContextData spiContextData,
                                                                   String authorisationId,
                                                                   SpiPsuData spiPsuData,
                                                                   String password,
                                                                   T consent,
                                                                   SpiAspspConsentDataProvider spiAspspConsentDataProvider);

    abstract void findAndTerminateOldConsents(String encryptedNewConsentId, T consent);

    abstract void updateConsentStatus(String consentId, ConsentStatus responseConsentStatus);

    abstract void updateMultilevelScaRequired(String consentId, boolean multilevelScaRequired);

    abstract ServiceType getServiceType();

    abstract SpiResponse<SpiVerifyScaAuthorisationResponse> verifyScaAuthorisation(SpiContextData spiContextData,
                                                                                   CommonAuthorisationParameters request,
                                                                                   PsuIdData psuData,
                                                                                   T consent,
                                                                                   SpiAspspConsentDataProvider spiAspspConsentDataProvider);

    abstract Optional<T> getConsentByIdFromCms(String consentId);

    abstract ErrorType getErrorType400();

    abstract ErrorType getErrorType401();

    abstract SpiResponse<SpiAuthorizationCodeResult> requestAuthorisationCode(SpiContextData spiContextData,
                                                                              String authenticationMethodId,
                                                                              T consent,
                                                                              SpiAspspConsentDataProvider spiAspspConsentDataProvider);
}
