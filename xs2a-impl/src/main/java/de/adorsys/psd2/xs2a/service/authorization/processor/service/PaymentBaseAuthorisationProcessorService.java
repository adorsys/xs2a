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

import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ChallengeData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.consent.PisAspspDataService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisCommonPaymentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aPisCommonPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.*;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.sca.ScaStatus.*;

abstract class PaymentBaseAuthorisationProcessorService extends BaseAuthorisationProcessorService {

    private static final String EMBEDDED_SELECTING_SCA_METHOD_FAILED_MSG = "Proceed embedded approach when performs authorisation depending on selected SCA method has failed.";

    private List<PisScaAuthorisationService> services;
    private Xs2aPisCommonPaymentService xs2aPisCommonPaymentService;
    private Xs2aToSpiPaymentMapper xs2aToSpiPaymentMapper;
    private SpiContextDataProvider spiContextDataProvider;
    private SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private SpiErrorMapper spiErrorMapper;
    private PisAspspDataService pisAspspDataService;
    private Xs2aPisCommonPaymentMapper xs2aPisCommonPaymentMapper;
    private Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper;

    protected PaymentBaseAuthorisationProcessorService(List<PisScaAuthorisationService> services,
                                                       Xs2aPisCommonPaymentService xs2aPisCommonPaymentService,
                                                       Xs2aToSpiPaymentMapper xs2aToSpiPaymentMapper,
                                                       SpiContextDataProvider spiContextDataProvider,
                                                       SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory,
                                                       SpiErrorMapper spiErrorMapper,
                                                       PisAspspDataService pisAspspDataService,
                                                       Xs2aPisCommonPaymentMapper xs2aPisCommonPaymentMapper,
                                                       Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper) {
        this.services = services;
        this.xs2aPisCommonPaymentService = xs2aPisCommonPaymentService;
        this.xs2aToSpiPaymentMapper = xs2aToSpiPaymentMapper;
        this.spiContextDataProvider = spiContextDataProvider;
        this.aspspConsentDataProviderFactory = aspspConsentDataProviderFactory;
        this.spiErrorMapper = spiErrorMapper;
        this.pisAspspDataService = pisAspspDataService;
        this.xs2aPisCommonPaymentMapper = xs2aPisCommonPaymentMapper;
        this.xs2aToSpiPsuDataMapper = xs2aToSpiPsuDataMapper;
    }

    @Override
    public AuthorisationProcessorResponse doScaPsuIdentified(AuthorisationProcessorRequest authorisationProcessorRequest) {
        return doScaReceived(authorisationProcessorRequest);
    }

    @Override
    public AuthorisationProcessorResponse doScaFinalised(AuthorisationProcessorRequest authorisationProcessorRequest) {
        UpdateAuthorisationRequest request = authorisationProcessorRequest.getUpdateAuthorisationRequest();
        return new Xs2aUpdatePisCommonPaymentPsuDataResponse(FINALISED, request.getBusinessObjectId(), request.getAuthorisationId(), request.getPsuData());
    }

    @Override
    public AuthorisationProcessorResponse doScaPsuAuthenticated(AuthorisationProcessorRequest authorisationProcessorRequest) {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = (Xs2aUpdatePisCommonPaymentPsuDataRequest) authorisationProcessorRequest.getUpdateAuthorisationRequest();

        SpiPayment payment = getSpiPayment(request.getPaymentId());

        if (isDecoupledApproach(request.getAuthorisationId(), request.getAuthenticationMethodId())) {
            xs2aPisCommonPaymentService.updateScaApproach(request.getAuthorisationId(), ScaApproach.DECOUPLED);
            return proceedDecoupledApproach(request, payment, request.getAuthenticationMethodId());
        }

        return proceedEmbeddedApproach(authorisationProcessorRequest, payment);
    }

    @Override
    public AuthorisationProcessorResponse doScaMethodSelected(AuthorisationProcessorRequest authorisationProcessorRequest) {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = (Xs2aUpdatePisCommonPaymentPsuDataRequest) authorisationProcessorRequest.getUpdateAuthorisationRequest();

        Authorisation authorisation = authorisationProcessorRequest.getAuthorisation();
        PsuIdData psuData = extractPsuIdData(request, authorisation);
        String authorisationId = request.getAuthorisationId();
        String paymentId = request.getPaymentId();

        // we need to get decrypted payment ID
        String internalId = pisAspspDataService.getInternalPaymentIdByEncryptedString(paymentId);
        SpiScaConfirmation spiScaConfirmation = xs2aPisCommonPaymentMapper.buildSpiScaConfirmation(request, authorisation.getParentId(), internalId, psuData);

        SpiContextData contextData = spiContextDataProvider.provideWithPsuIdData(psuData);
        SpiAspspConsentDataProvider aspspConsentDataProvider = aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(paymentId);

        SpiPayment payment = getSpiPayment(request.getPaymentId());

        SpiResponse spiResponse = verifyScaAuthorisationAndExecutePayment(authorisation, payment,
                                                                          spiScaConfirmation,
                                                                          contextData,
                                                                          aspspConsentDataProvider);

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS);
            writeErrorLog(authorisationProcessorRequest, psuData, errorHolder, "Verify SCA authorisation and execute payment has failed.");

            Optional<MessageErrorCode> first = errorHolder.getFirstErrorCode();
            if (first.isPresent() && first.get() == MessageErrorCode.PSU_CREDENTIALS_INVALID) {
                xs2aPisCommonPaymentService.updatePisAuthorisationStatus(authorisationId, FAILED);
            }
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, psuData);
        }

        updatePaymentData(paymentId, spiResponse);
        return new Xs2aUpdatePisCommonPaymentPsuDataResponse(FINALISED, paymentId, authorisationId, psuData);
    }

    abstract void updatePaymentData(String paymentId, SpiResponse spiResponse);

    abstract SpiResponse<SpiAuthorizationCodeResult> requestAuthorisationCode(SpiPayment payment, String authenticationMethodId,
                                                                              SpiContextData spiContextData,
                                                                              SpiAspspConsentDataProvider spiAspspConsentDataProvider);

    abstract SpiResponse verifyScaAuthorisationAndExecutePayment(Authorisation authorisation,
                                                                 SpiPayment payment,
                                                                 SpiScaConfirmation spiScaConfirmation,
                                                                 SpiContextData contextData,
                                                                 SpiAspspConsentDataProvider spiAspspConsentDataProvider);

    abstract SpiResponse<SpiPsuAuthorisationResponse> authorisePsu(Xs2aUpdatePisCommonPaymentPsuDataRequest request, SpiPayment payment,
                                                                   SpiAspspConsentDataProvider aspspConsentDataProvider, SpiPsuData spiPsuData,
                                                                   SpiContextData contextData);

    abstract SpiResponse<SpiAvailableScaMethodsResponse> requestAvailableScaMethods(SpiPayment payment,
                                                                                    SpiAspspConsentDataProvider aspspConsentDataProvider,
                                                                                    SpiContextData contextData);

    abstract Xs2aUpdatePisCommonPaymentPsuDataResponse executePaymentWithoutSca(AuthorisationProcessorRequest authorisationProcessorRequest,
                                                                                PsuIdData psuData, PaymentType paymentType, SpiPayment payment,
                                                                                SpiContextData contextData, ScaStatus resultScaStatus);

    abstract Xs2aUpdatePisCommonPaymentPsuDataResponse proceedDecoupledApproach(Xs2aUpdatePisCommonPaymentPsuDataRequest request,
                                                                                SpiPayment payment, String authenticationMethodId);


    abstract boolean needProcessExemptedSca(PaymentType paymentType, boolean isScaExempted);

    PisScaAuthorisationService getService(ScaApproach scaApproach) {
        return services.stream().filter(s -> s.getScaApproachServiceType() == scaApproach).findFirst()
                   .orElseThrow(() -> new IllegalArgumentException("Pis cancellation authorisation service was not found for approach " + scaApproach));
    }

    Xs2aUpdatePisCommonPaymentPsuDataResponse applyAuthorisation(AuthorisationProcessorRequest authorisationProcessorRequest) {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = (Xs2aUpdatePisCommonPaymentPsuDataRequest) authorisationProcessorRequest.getUpdateAuthorisationRequest();
        Authorisation authorisation = authorisationProcessorRequest.getAuthorisation();
        PsuIdData psuData = extractPsuIdData(request, authorisation);
        String authorisationId = request.getAuthorisationId();
        String paymentId = request.getPaymentId();
        SpiPayment payment = getSpiPayment(paymentId);

        if (authorisation.getChosenScaApproach() == ScaApproach.DECOUPLED) {
            request.setPsuData(psuData);
        }

        SpiAspspConsentDataProvider aspspConsentDataProvider = aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(paymentId);

        SpiPsuData spiPsuData = xs2aToSpiPsuDataMapper.mapToSpiPsuData(psuData);
        SpiContextData contextData = spiContextDataProvider.provideWithPsuIdData(psuData);

        SpiResponse<SpiPsuAuthorisationResponse> authPsuResponse = authorisePsu(request, payment, aspspConsentDataProvider, spiPsuData, contextData);
        if (authPsuResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(authPsuResponse, ServiceType.PIS);
            writeErrorLog(authorisationProcessorRequest, psuData, errorHolder, "Authorise PSU when apply authorisation has failed.");
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, psuData);
        }

        SpiPsuAuthorisationResponse psuAuthorisationResponse = authPsuResponse.getPayload();

        if (psuAuthorisationResponse.getSpiAuthorisationStatus() == SpiAuthorisationStatus.FAILURE) {
            ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_401)
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.PSU_CREDENTIALS_INVALID))
                                          .build();
            writeErrorLog(authorisationProcessorRequest, psuData, errorHolder, "PSU authorisation failed due to incorrect credentials.");
            xs2aPisCommonPaymentService.updatePisAuthorisationStatus(authorisationId, FAILED);
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, psuData);
        }

        PaymentType paymentType = request.getPaymentService();
        if (needProcessExemptedSca(paymentType, psuAuthorisationResponse.isScaExempted())) {
            writeInfoLog(authorisationProcessorRequest, psuData, "SCA was exempted for the payment after AuthorisationSpi#authorisePsu.");
            return executePaymentWithoutSca(authorisationProcessorRequest, psuData, paymentType, payment, contextData, EXEMPTED);
        }

        if (authorisation.getChosenScaApproach() == ScaApproach.DECOUPLED) {
            return proceedDecoupledApproach(request, payment);
        }

        SpiResponse<SpiAvailableScaMethodsResponse> availableScaMethodsResponse = requestAvailableScaMethods(payment, aspspConsentDataProvider, contextData);

        if (availableScaMethodsResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(availableScaMethodsResponse, ServiceType.PIS);
            writeErrorLog(authorisationProcessorRequest, psuData, errorHolder, "Request available SCA methods has failed.");
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, psuData);
        }

        SpiAvailableScaMethodsResponse availableScaMethods = availableScaMethodsResponse.getPayload();

        if (needProcessExemptedSca(paymentType, availableScaMethods.isScaExempted())) {
            writeInfoLog(authorisationProcessorRequest, psuData, "SCA was exempted for the payment after AuthorisationSpi#requestAvailableScaMethods.");
            return executePaymentWithoutSca(authorisationProcessorRequest, psuData, paymentType, payment, contextData, EXEMPTED);
        }

        List<AuthenticationObject> spiScaMethods = availableScaMethods.getAvailableScaMethods();

        return processScaMethods(authorisationProcessorRequest, psuData, paymentType, payment, aspspConsentDataProvider,
                                 contextData, spiScaMethods);
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse processScaMethods(@NotNull AuthorisationProcessorRequest authorisationProcessorRequest,
                                                                        PsuIdData psuData, PaymentType paymentType, SpiPayment payment,
                                                                        SpiAspspConsentDataProvider aspspConsentDataProvider, SpiContextData contextData,
                                                                        List<AuthenticationObject> spiScaMethods) {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = (Xs2aUpdatePisCommonPaymentPsuDataRequest) authorisationProcessorRequest.getUpdateAuthorisationRequest();

        if (CollectionUtils.isEmpty(spiScaMethods)) {
            writeInfoLog(authorisationProcessorRequest, psuData, "Available SCA methods is empty.");
            return executePaymentWithoutSca(authorisationProcessorRequest, psuData, paymentType, payment, contextData, FINALISED);
        } else if (isSingleScaMethod(spiScaMethods)) {
            return buildUpdateResponseWhenScaMethodIsSingle(authorisationProcessorRequest, psuData, payment, aspspConsentDataProvider, contextData, spiScaMethods);
        } else if (isMultipleScaMethods(spiScaMethods)) {
            return buildUpdateResponseWhenScaMethodsAreMultiple(request, psuData, spiScaMethods);
        }

        writeInfoLog(authorisationProcessorRequest, psuData, "Apply authorisation when update payment PSU data set SCA status failed.");
        return new Xs2aUpdatePisCommonPaymentPsuDataResponse(FAILED, request.getPaymentId(), request.getAuthorisationId(), psuData);
    }

    Xs2aUpdatePisCommonPaymentPsuDataResponse applyIdentification(AuthorisationProcessorRequest authorisationProcessorRequest) {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = (Xs2aUpdatePisCommonPaymentPsuDataRequest) authorisationProcessorRequest.getUpdateAuthorisationRequest();
        String paymentId = request.getPaymentId();
        String authorisationId = request.getAuthorisationId();
        PsuIdData psuData = request.getPsuData();

        if (!isPsuExist(psuData)) {
            ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_400)
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_NO_PSU))
                                          .build();
            writeErrorLog(authorisationProcessorRequest, psuData, errorHolder, "Apply identification when update payment PSU data has failed. No PSU data available in request.");
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, psuData);
        }

        return new Xs2aUpdatePisCommonPaymentPsuDataResponse(PSUIDENTIFIED, paymentId, authorisationId, psuData);
    }

    Xs2aUpdatePisCommonPaymentPsuDataResponse buildUpdateResponseWhenScaMethodsAreMultiple(Xs2aUpdatePisCommonPaymentPsuDataRequest request, PsuIdData psuData, List<AuthenticationObject> spiScaMethods) {
        xs2aPisCommonPaymentService.saveAuthenticationMethods(request.getAuthorisationId(), spiScaMethods);
        Xs2aUpdatePisCommonPaymentPsuDataResponse response = new Xs2aUpdatePisCommonPaymentPsuDataResponse(PSUAUTHENTICATED, request.getPaymentId(), request.getAuthorisationId(), psuData);
        response.setAvailableScaMethods(spiScaMethods);
        return response;
    }

    Xs2aUpdatePisCommonPaymentPsuDataResponse buildUpdateResponseWhenScaMethodIsSingle(AuthorisationProcessorRequest authorisationProcessorRequest, PsuIdData psuData, SpiPayment payment, SpiAspspConsentDataProvider aspspConsentDataProvider, SpiContextData contextData, List<AuthenticationObject> scaMethods) {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = (Xs2aUpdatePisCommonPaymentPsuDataRequest) authorisationProcessorRequest.getUpdateAuthorisationRequest();
        xs2aPisCommonPaymentService.saveAuthenticationMethods(request.getAuthorisationId(), scaMethods);
        AuthenticationObject chosenMethod = scaMethods.get(0);

        if (chosenMethod.isDecoupled()) {
            xs2aPisCommonPaymentService.updateScaApproach(request.getAuthorisationId(), ScaApproach.DECOUPLED);
            return proceedDecoupledApproach(request, payment, chosenMethod.getAuthenticationMethodId());
        }

        return proceedSingleScaEmbeddedApproach(authorisationProcessorRequest, payment, chosenMethod, contextData, aspspConsentDataProvider, psuData);
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse proceedSingleScaEmbeddedApproach(AuthorisationProcessorRequest authorisationProcessorRequest,
                                                                                       SpiPayment payment,
                                                                                       AuthenticationObject chosenMethod,
                                                                                       SpiContextData contextData,
                                                                                       SpiAspspConsentDataProvider spiAspspConsentDataProvider,
                                                                                       PsuIdData psuData) {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = (Xs2aUpdatePisCommonPaymentPsuDataRequest) authorisationProcessorRequest.getUpdateAuthorisationRequest();
        String authorisationId = request.getAuthorisationId();

        SpiResponse<SpiAuthorizationCodeResult> authCodeResponse = requestAuthorisationCode(payment, chosenMethod.getAuthenticationMethodId(), contextData, spiAspspConsentDataProvider);

        if (authCodeResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(authCodeResponse, ServiceType.PIS);
            writeErrorLog(authorisationProcessorRequest, psuData, errorHolder, "Proceed single SCA embedded approach when performs authorisation has failed.");
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, request.getPaymentId(), authorisationId, psuData);
        }

        SpiAuthorizationCodeResult authorizationCodeResult = authCodeResponse.getPayload();
        if (needProcessExemptedSca(payment.getPaymentType(), authorizationCodeResult.isScaExempted())) {
            writeInfoLog(authorisationProcessorRequest, psuData, "SCA was exempted for the payment after AuthorisationSpi#requestAuthorisationCode.");
            return executePaymentWithoutSca(authorisationProcessorRequest, psuData, payment.getPaymentType(), payment, contextData, EXEMPTED);
        }

        AuthenticationObject authenticationObject = authorizationCodeResult.getSelectedScaMethod();
        ChallengeData challengeData = mapToChallengeData(authorizationCodeResult);

        Xs2aUpdatePisCommonPaymentPsuDataResponse response = new Xs2aUpdatePisCommonPaymentPsuDataResponse(SCAMETHODSELECTED, payment.getPaymentId(), authorisationId, psuData);
        response.setChosenScaMethod(authenticationObject);
        response.setChallengeData(challengeData);
        return response;
    }

    private boolean isDecoupledApproach(String authorisationId, String authenticationMethodId) {
        return xs2aPisCommonPaymentService.isAuthenticationMethodDecoupled(authorisationId, authenticationMethodId);
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse proceedEmbeddedApproach(AuthorisationProcessorRequest authorisationProcessorRequest, SpiPayment payment) {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = (Xs2aUpdatePisCommonPaymentPsuDataRequest) authorisationProcessorRequest.getUpdateAuthorisationRequest();
        Authorisation authorisation = authorisationProcessorRequest.getAuthorisation();
        String authenticationMethodId = request.getAuthenticationMethodId();
        String authorisationId = request.getAuthorisationId();
        String paymentId = request.getPaymentId();
        PsuIdData psuData = extractPsuIdData(request, authorisation);

        SpiContextData contextData = spiContextDataProvider.provideWithPsuIdData(psuData);
        SpiAspspConsentDataProvider aspspConsentDataProvider = aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(paymentId);

        SpiResponse<SpiAuthorizationCodeResult> spiResponse = requestAuthorisationCode(payment, authenticationMethodId, contextData, aspspConsentDataProvider);

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS);
            writeErrorLog(authorisationProcessorRequest, psuData, errorHolder, EMBEDDED_SELECTING_SCA_METHOD_FAILED_MSG);

            Optional<MessageErrorCode> first = errorHolder.getFirstErrorCode();
            if (first.isPresent() && first.get() == MessageErrorCode.PSU_CREDENTIALS_INVALID) {
                xs2aPisCommonPaymentService.updatePisAuthorisationStatus(authorisationId, FAILED);
            }
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, psuData);
        }

        SpiAuthorizationCodeResult authorizationCodeResult = spiResponse.getPayload();
        if (needProcessExemptedSca(payment.getPaymentType(), authorizationCodeResult.isScaExempted())) {
            writeInfoLog(authorisationProcessorRequest, psuData, "SCA was exempted for the payment after AuthorisationSpi#requestAuthorisationCode.");
            return executePaymentWithoutSca(authorisationProcessorRequest, psuData, payment.getPaymentType(), payment, contextData, EXEMPTED);
        }

        if (authorizationCodeResult.isEmpty()) {
            ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_400)
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR))
                                          .build();
            writeErrorLog(authorisationProcessorRequest, psuData, errorHolder, EMBEDDED_SELECTING_SCA_METHOD_FAILED_MSG);
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, psuData);
        }

        AuthenticationObject authenticationObject = authorizationCodeResult.getSelectedScaMethod();
        ChallengeData challengeData = authorizationCodeResult.getChallengeData();

        Xs2aUpdatePisCommonPaymentPsuDataResponse response = new Xs2aUpdatePisCommonPaymentPsuDataResponse(SCAMETHODSELECTED, paymentId, authorisationId, psuData);
        response.setChosenScaMethod(authenticationObject);
        response.setChallengeData(challengeData);
        return response;
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse proceedDecoupledApproach(Xs2aUpdatePisCommonPaymentPsuDataRequest request,
                                                                               SpiPayment payment) {
        return proceedDecoupledApproach(request, payment, null);
    }

    private SpiPayment getSpiPayment(String encryptedPaymentId) {
        Optional<PisCommonPaymentResponse> commonPaymentById = xs2aPisCommonPaymentService.getPisCommonPaymentById(encryptedPaymentId);
        return commonPaymentById
                   .map(pisCommonPaymentResponse -> xs2aToSpiPaymentMapper.mapToSpiPayment(pisCommonPaymentResponse))
                   .orElse(null);
    }
}
