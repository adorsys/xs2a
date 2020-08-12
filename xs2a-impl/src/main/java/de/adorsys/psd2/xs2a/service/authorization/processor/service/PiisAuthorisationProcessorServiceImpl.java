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

package de.adorsys.psd2.xs2a.service.authorization.processor.service;

import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
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
import de.adorsys.psd2.xs2a.domain.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.piis.CommonDecoupledPiisService;
import de.adorsys.psd2.xs2a.service.authorization.piis.PiisAuthorizationService;
import de.adorsys.psd2.xs2a.service.authorization.piis.PiisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPiisConsentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPiisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAvailableScaMethodsResponse;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiPsuAuthorisationResponse;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiVerifyScaAuthorisationResponse;
import de.adorsys.psd2.xs2a.spi.domain.piis.SpiPiisConsent;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PiisConsentSpi;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PiisAuthorisationProcessorServiceImpl extends BaseAuthorisationProcessorService {
    private static final String CONSENT_NOT_FOUND_LOG_MESSAGE = "Apply authorisation when update consent PSU data has failed. Consent not found by id.";

    private final List<PiisAuthorizationService> services;
    private final Xs2aAuthorisationService authorisationService;
    private final Xs2aPiisConsentService piisConsentService;
    private final PiisConsentSpi piisConsentSpi;
    private final Xs2aToSpiPiisConsentMapper xs2aToSpiPiisConsentMapper;
    private final SpiContextDataProvider spiContextDataProvider;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final SpiErrorMapper spiErrorMapper;
    private final CommonDecoupledPiisService commonDecoupledPiisService;
    private final PiisScaAuthorisationService piisScaAuthorisationService;
    private final Xs2aToSpiPsuDataMapper psuDataMapper;

    public PiisAuthorisationProcessorServiceImpl(List<PiisAuthorizationService> services,
                                                 Xs2aAuthorisationService authorisationService,
                                                 Xs2aPiisConsentService piisConsentService,
                                                 PiisConsentSpi piisConsentSpi, Xs2aToSpiPiisConsentMapper xs2aToSpiPiisConsentMapper,
                                                 SpiContextDataProvider spiContextDataProvider,
                                                 SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory,
                                                 SpiErrorMapper spiErrorMapper,
                                                 CommonDecoupledPiisService commonDecoupledPiisService,
                                                 PiisScaAuthorisationService piisScaAuthorisationService,
                                                 Xs2aToSpiPsuDataMapper psuDataMapper) {
        this.services = services;
        this.authorisationService = authorisationService;
        this.piisConsentService = piisConsentService;
        this.piisConsentSpi = piisConsentSpi;
        this.xs2aToSpiPiisConsentMapper = xs2aToSpiPiisConsentMapper;
        this.spiContextDataProvider = spiContextDataProvider;
        this.aspspConsentDataProviderFactory = aspspConsentDataProviderFactory;
        this.spiErrorMapper = spiErrorMapper;
        this.commonDecoupledPiisService = commonDecoupledPiisService;
        this.piisScaAuthorisationService = piisScaAuthorisationService;
        this.psuDataMapper = psuDataMapper;
    }

    @Override
    public void updateAuthorisation(AuthorisationProcessorRequest request, AuthorisationProcessorResponse response) {
        PiisAuthorizationService authorizationService = getService(request.getScaApproach());
        authorizationService.updateConsentPsuData(request.getUpdateAuthorisationRequest(), response);
    }

    @Override
    public AuthorisationProcessorResponse doScaReceived(AuthorisationProcessorRequest authorisationProcessorRequest) {
        return doScaPsuIdentified(authorisationProcessorRequest);
    }

    @Override
    public AuthorisationProcessorResponse doScaPsuIdentified(AuthorisationProcessorRequest authorisationProcessorRequest) {
        UpdateAuthorisationRequest request = authorisationProcessorRequest.getUpdateAuthorisationRequest();
        return request.isUpdatePsuIdentification()
                   ? applyIdentification(authorisationProcessorRequest)
                   : applyAuthorisation(authorisationProcessorRequest);
    }

    @Override
    public AuthorisationProcessorResponse doScaPsuAuthenticated(AuthorisationProcessorRequest authorisationProcessorRequest) {
        UpdateAuthorisationRequest request = authorisationProcessorRequest.getUpdateAuthorisationRequest();
        String consentId = request.getBusinessObjectId();
        String authorisationId = request.getAuthorisationId();
        Optional<PiisConsent> piisConsentOptional = piisConsentService.getPiisConsentById(consentId);
        if (piisConsentOptional.isEmpty()) {
            ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIIS_400)
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_400))
                                          .build();
            writeErrorLog(authorisationProcessorRequest, request.getPsuData(), errorHolder, CONSENT_NOT_FOUND_LOG_MESSAGE);
            return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId, request.getPsuData());
        }

        PsuIdData psuData = extractPsuIdData(request, authorisationProcessorRequest.getAuthorisation());
        PiisConsent piisConsent = piisConsentOptional.get();

        SpiPiisConsent spiPiisConsent = xs2aToSpiPiisConsentMapper.mapToSpiPiisConsent(piisConsent);

        String authenticationMethodId = request.getAuthenticationMethodId();
        if (isDecoupledApproach(request.getAuthorisationId(), authenticationMethodId)) {
            authorisationService.updateScaApproach(request.getAuthorisationId(), ScaApproach.DECOUPLED);
            return commonDecoupledPiisService.proceedDecoupledApproach(request.getBusinessObjectId(), request.getAuthorisationId(), spiPiisConsent, authenticationMethodId, psuData);
        }

        return proceedEmbeddedApproach(authorisationProcessorRequest, authenticationMethodId, spiPiisConsent, psuData);
    }

    @Override
    public AuthorisationProcessorResponse doScaMethodSelected(AuthorisationProcessorRequest authorisationProcessorRequest) {
        UpdateAuthorisationRequest request = authorisationProcessorRequest.getUpdateAuthorisationRequest();
        String consentId = request.getBusinessObjectId();
        String authorisationId = request.getAuthorisationId();
        Optional<PiisConsent> piisConsentOptional = piisConsentService.getPiisConsentById(consentId);

        if (piisConsentOptional.isEmpty()) {
            ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIIS_400)
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_400))
                                          .build();
            writeErrorLog(authorisationProcessorRequest, request.getPsuData(), errorHolder, CONSENT_NOT_FOUND_LOG_MESSAGE);
            return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId, request.getPsuData());
        }
        PiisConsent piisConsent = piisConsentOptional.get();

        PsuIdData psuData = extractPsuIdData(request, authorisationProcessorRequest.getAuthorisation());

        SpiResponse<SpiVerifyScaAuthorisationResponse> spiResponse = piisConsentSpi.verifyScaAuthorisation(spiContextDataProvider.provideWithPsuIdData(psuData),
                                                                                                           xs2aToSpiPiisConsentMapper.toSpiScaConfirmation(request, psuData),
                                                                                                           xs2aToSpiPiisConsentMapper.mapToSpiPiisConsent(piisConsent),
                                                                                                           aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId));

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIIS);
            writeErrorLog(authorisationProcessorRequest, request.getPsuData(), errorHolder, "Verify SCA authorisation failed when update PSU data.");

            SpiVerifyScaAuthorisationResponse spiAuthorisationResponse = spiResponse.getPayload();
            return getSpiErrorResponse(authorisationProcessorRequest, consentId, authorisationId, psuData, errorHolder, spiAuthorisationResponse);
        }

        ConsentStatus responseConsentStatus = spiResponse.getPayload().getConsentStatus();

        if (ConsentStatus.PARTIALLY_AUTHORISED == responseConsentStatus && !piisConsent.isMultilevelScaRequired()) {
            piisConsentService.updateMultilevelScaRequired(consentId, true);
        }

        if (piisConsent.getConsentStatus() != responseConsentStatus) {
            piisConsentService.updateConsentStatus(consentId, responseConsentStatus);
        }

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
        UpdateAuthorisationRequest request = authorisationProcessorRequest.getUpdateAuthorisationRequest();
        return new UpdateConsentPsuDataResponse(ScaStatus.FINALISED, request.getBusinessObjectId(), request.getAuthorisationId(), request.getPsuData());
    }

    private UpdateConsentPsuDataResponse proceedEmbeddedApproach(AuthorisationProcessorRequest authorisationProcessorRequest, String authenticationMethodId, SpiPiisConsent spiPiisConsent, PsuIdData psuData) {
        UpdateAuthorisationRequest request = authorisationProcessorRequest.getUpdateAuthorisationRequest();
        SpiResponse<SpiAuthorizationCodeResult> spiResponse =
            piisConsentSpi.requestAuthorisationCode(spiContextDataProvider.provideWithPsuIdData(psuData),
                                                    authenticationMethodId, spiPiisConsent,
                                                    aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(request.getBusinessObjectId()));

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIIS);
            writeErrorLog(authorisationProcessorRequest, psuData, errorHolder, "Proceed embedded approach when performs authorisation depending on selected SCA method has failed.");

            Optional<MessageErrorCode> first = errorHolder.getFirstErrorCode();
            if (first.isPresent() && first.get() == MessageErrorCode.PSU_CREDENTIALS_INVALID) {
                authorisationService.updateAuthorisationStatus(request.getAuthorisationId(), ScaStatus.FAILED);
            }
            return new UpdateConsentPsuDataResponse(errorHolder, request.getBusinessObjectId(), request.getAuthorisationId(), psuData);
        }

        SpiAuthorizationCodeResult authorizationCodeResult = spiResponse.getPayload();

        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse(ScaStatus.SCAMETHODSELECTED, request.getBusinessObjectId(), request.getAuthorisationId(), psuData);
        response.setChosenScaMethod(authorizationCodeResult.getSelectedScaMethod());
        response.setChallengeData(authorizationCodeResult.getChallengeData());
        return response;
    }

    private PiisAuthorizationService getService(ScaApproach scaApproach) {
        return services.stream().filter(s -> s.getScaApproachServiceType() == scaApproach).findFirst()
                   .orElseThrow(() -> new IllegalArgumentException("Piis authorisation service was not found for approach " + scaApproach));
    }

    private UpdateConsentPsuDataResponse applyAuthorisation(AuthorisationProcessorRequest authorisationProcessorRequest) {
        UpdateAuthorisationRequest request = authorisationProcessorRequest.getUpdateAuthorisationRequest();
        String consentId = request.getBusinessObjectId();
        String authorisationId = request.getAuthorisationId();
        Optional<PiisConsent> piisConsentOptional = piisConsentService.getPiisConsentById(consentId);

        if (piisConsentOptional.isEmpty()) {
            ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIIS_400)
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_400))
                                          .build();
            writeErrorLog(authorisationProcessorRequest, request.getPsuData(), errorHolder, CONSENT_NOT_FOUND_LOG_MESSAGE);
            return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId, request.getPsuData());
        }

        Authorisation authorisation = authorisationProcessorRequest.getAuthorisation();
        PsuIdData psuData = extractPsuIdData(request, authorisation);
        PiisConsent piisConsent = piisConsentOptional.get();

        SpiPiisConsent spiPiisConsent = xs2aToSpiPiisConsentMapper.mapToSpiPiisConsent(piisConsent);
        SpiContextData spiContextData = spiContextDataProvider.provideWithPsuIdData(psuData);
        SpiPsuData spiPsuData = psuDataMapper.mapToSpiPsuData(psuData);

        SpiResponse<SpiPsuAuthorisationResponse> authorisationStatusSpiResponse = piisConsentSpi.authorisePsu(spiContextData,
                                                                                                              authorisation.getAuthorisationId(),
                                                                                                              spiPsuData,
                                                                                                              request.getPassword(),
                                                                                                              spiPiisConsent,
                                                                                                              aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId));
        if (authorisationStatusSpiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(authorisationStatusSpiResponse, ServiceType.PIIS);
            writeErrorLog(authorisationProcessorRequest, psuData, errorHolder, "Authorise PSU when apply authorisation has failed.");
            return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId, psuData);
        }

        SpiPsuAuthorisationResponse spiAuthorisationResponse = authorisationStatusSpiResponse.getPayload();
        if (spiAuthorisationResponse.getSpiAuthorisationStatus() == SpiAuthorisationStatus.ATTEMPT_FAILURE) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(authorisationStatusSpiResponse, ServiceType.PIIS);
            return new UpdateConsentPsuDataResponse(authorisationProcessorRequest.getScaStatus(), errorHolder, consentId, authorisationId, psuData);
        }

        if (spiAuthorisationResponse.getSpiAuthorisationStatus() == SpiAuthorisationStatus.FAILURE) {
            ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIIS_401)
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.PSU_CREDENTIALS_INVALID))
                                          .build();
            writeErrorLog(authorisationProcessorRequest, psuData, errorHolder, "Authorise PSU when apply authorisation has failed. PSU credentials invalid.");
            authorisationService.updateAuthorisationStatus(authorisationId, ScaStatus.FAILED);
            return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId, psuData);
        }

        if (piisScaAuthorisationService.isOneFactorAuthorisation(piisConsent)) {
            piisConsentService.updateConsentStatus(consentId, ConsentStatus.VALID);

            return new UpdateConsentPsuDataResponse(ScaStatus.FINALISED, consentId, authorisationId, psuData);
        }

        if (authorisation.getChosenScaApproach() == ScaApproach.DECOUPLED) {
            return commonDecoupledPiisService.proceedDecoupledApproach(consentId, authorisationId, spiPiisConsent, psuData);
        }

        return requestAvailableScaMethods(authorisationProcessorRequest, consentId, authorisationId, psuData, spiPiisConsent);
    }

    private UpdateConsentPsuDataResponse requestAvailableScaMethods(AuthorisationProcessorRequest authorisationProcessorRequest, String consentId, String authorisationId, PsuIdData psuData, SpiPiisConsent spiPiisConsent) {
        SpiResponse<SpiAvailableScaMethodsResponse> spiResponse =
            piisConsentSpi.requestAvailableScaMethods(spiContextDataProvider.provideWithPsuIdData(psuData),
                                                      spiPiisConsent,
                                                      aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId));

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIIS);
            writeErrorLog(authorisationProcessorRequest, psuData, errorHolder, "Request available SCA methods when apply authorisation has failed.");
            return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId, psuData);
        }

        List<AuthenticationObject> availableScaMethods = spiResponse.getPayload().getAvailableScaMethods();
        if (CollectionUtils.isNotEmpty(availableScaMethods)) {
            authorisationService.saveAuthenticationMethods(authorisationId, availableScaMethods);

            return getScaMethodsResponse(authorisationProcessorRequest, consentId, authorisationId, psuData, spiPiisConsent, availableScaMethods);
        }

        ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIIS_400)
                                      .tppMessages(TppMessageInformation.of(MessageErrorCode.SCA_METHOD_UNKNOWN))
                                      .build();
        writeErrorLog(authorisationProcessorRequest, psuData, errorHolder, "Apply authorisation has failed. Consent was rejected because PSU has no available SCA methods.");
        piisConsentService.updateConsentStatus(consentId, ConsentStatus.REJECTED);
        authorisationService.updateAuthorisationStatus(authorisationId, ScaStatus.FAILED);
        return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId, psuData);
    }

    private UpdateConsentPsuDataResponse getScaMethodsResponse(AuthorisationProcessorRequest authorisationProcessorRequest, String consentId, String authorisationId, PsuIdData psuData, SpiPiisConsent spiPiisConsent, List<AuthenticationObject> availableScaMethods) {
        if (isMultipleScaMethods(availableScaMethods)) {
            return createResponseForMultipleAvailableMethods(availableScaMethods, authorisationId, consentId, psuData);
        } else {
            return createResponseForOneAvailableMethod(authorisationProcessorRequest, spiPiisConsent, availableScaMethods.get(0), psuData);
        }
    }

    private UpdateConsentPsuDataResponse createResponseForMultipleAvailableMethods(List<AuthenticationObject> availableScaMethods,
                                                                                   String authorisationId,
                                                                                   String consentId,
                                                                                   PsuIdData psuIdData) {
        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse(ScaStatus.PSUAUTHENTICATED, consentId, authorisationId, psuIdData);
        response.setAvailableScaMethods(availableScaMethods);
        return response;
    }

    private UpdateConsentPsuDataResponse createResponseForOneAvailableMethod(AuthorisationProcessorRequest authorisationProcessorRequest, SpiPiisConsent spiPiisConsent, AuthenticationObject scaMethod, PsuIdData psuData) {
        UpdateAuthorisationRequest request = authorisationProcessorRequest.getUpdateAuthorisationRequest();
        if (scaMethod.isDecoupled()) {
            authorisationService.updateScaApproach(request.getAuthorisationId(), ScaApproach.DECOUPLED);
            return commonDecoupledPiisService.proceedDecoupledApproach(request.getBusinessObjectId(), request.getAuthorisationId(), spiPiisConsent, scaMethod.getAuthenticationMethodId(), psuData);
        }

        return proceedEmbeddedApproach(authorisationProcessorRequest, scaMethod.getAuthenticationMethodId(), spiPiisConsent, psuData);
    }

    private UpdateConsentPsuDataResponse applyIdentification(AuthorisationProcessorRequest authorisationProcessorRequest) {
        UpdateAuthorisationRequest request = authorisationProcessorRequest.getUpdateAuthorisationRequest();
        if (!isPsuExist(request.getPsuData())) {
            ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIIS_400)
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
}
