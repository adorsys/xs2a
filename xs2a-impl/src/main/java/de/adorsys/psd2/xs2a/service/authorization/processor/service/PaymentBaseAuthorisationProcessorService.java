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

import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.pis.Xs2aCurrencyConversionInfo;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ChallengeData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.authorisation.CommonAuthorisationParameters;
import de.adorsys.psd2.xs2a.domain.consent.CreatePaymentAuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.domain.consent.pis.PaymentAuthorisationParameters;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.consent.PisAspspDataService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisCommonPaymentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aPisCommonPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.*;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentExecutionResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.adorsys.psd2.xs2a.core.sca.ScaStatus.*;

@Slf4j
abstract class PaymentBaseAuthorisationProcessorService extends BaseAuthorisationProcessorService {// NOPMD

    private static final String EMBEDDED_SELECTING_SCA_METHOD_FAILED_MSG = "Proceed embedded approach when performs authorisation depending on selected SCA method has failed.";

    private final List<PisScaAuthorisationService> services;
    private final Xs2aAuthorisationService xs2aAuthorisationService;
    private final Xs2aPisCommonPaymentService xs2aPisCommonPaymentService;
    private final Xs2aToSpiPaymentMapper xs2aToSpiPaymentMapper;
    private final SpiContextDataProvider spiContextDataProvider;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final SpiErrorMapper spiErrorMapper;
    private final PisAspspDataService pisAspspDataService;
    private final Xs2aPisCommonPaymentMapper xs2aPisCommonPaymentMapper;
    private final Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper;

    protected PaymentBaseAuthorisationProcessorService(List<PisScaAuthorisationService> services,
                                                       Xs2aAuthorisationService xs2aAuthorisationService,
                                                       Xs2aPisCommonPaymentService xs2aPisCommonPaymentService,
                                                       Xs2aToSpiPaymentMapper xs2aToSpiPaymentMapper,
                                                       SpiContextDataProvider spiContextDataProvider,
                                                       SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory,
                                                       SpiErrorMapper spiErrorMapper,
                                                       PisAspspDataService pisAspspDataService,
                                                       Xs2aPisCommonPaymentMapper xs2aPisCommonPaymentMapper,
                                                       Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper) {
        this.services = services;
        this.xs2aAuthorisationService = xs2aAuthorisationService;
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
    public AuthorisationProcessorResponse doScaStarted(AuthorisationProcessorRequest authorisationProcessorRequest) {
        CommonAuthorisationParameters updateAuthorisationRequest = authorisationProcessorRequest.getUpdateAuthorisationRequest();
        PsuIdData psuIdData = updateAuthorisationRequest.getPsuData();
        String paymentId = updateAuthorisationRequest.getBusinessObjectId();
        ScaApproach scaApproach = authorisationProcessorRequest.getScaApproach();

        SpiPayment spiPayment = getSpiPayment(paymentId);
        SpiResponse<SpiStartAuthorisationResponse> spiResponse = getSpiStartAuthorisationResponse(spiContextDataProvider.provideWithPsuIdData(psuIdData),
                                                                                                  scaApproach,
                                                                                                  authorisationProcessorRequest.getScaStatus(),
                                                                                                  updateAuthorisationRequest.getAuthorisationId(),
                                                                                                  spiPayment,
                                                                                                  aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(paymentId));

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS);
            writeErrorLog(authorisationProcessorRequest, psuIdData, errorHolder, "Verify SCA authorisation failed when create authorisation.");
            return new CreatePaymentAuthorisationProcessorResponse(errorHolder, scaApproach, paymentId, psuIdData);
        }

        SpiStartAuthorisationResponse startAuthorisationResponse = spiResponse.getPayload();

        ScaStatus scaStatusFromSpi = startAuthorisationResponse.getScaStatus();
        ScaApproach scaApproachFromSpi = startAuthorisationResponse.getScaApproach();
        String psuMessage = startAuthorisationResponse.getPsuMessage();
        Set<TppMessageInformation> tppMessages = startAuthorisationResponse.getTppMessages();

        return new CreatePaymentAuthorisationProcessorResponse(scaStatusFromSpi, scaApproachFromSpi, psuMessage, tppMessages, paymentId, psuIdData);
    }

    protected abstract SpiResponse<SpiStartAuthorisationResponse> getSpiStartAuthorisationResponse(SpiContextData spiContextData,
                                                                                                   ScaApproach scaApproach,
                                                                                                   ScaStatus scaStatus,
                                                                                                   String authorisationId,
                                                                                                   SpiPayment spiPayment,
                                                                                                   SpiAspspConsentDataProvider spiAspspDataProviderFor);

    @Override
    public AuthorisationProcessorResponse doScaPsuIdentified(AuthorisationProcessorRequest authorisationProcessorRequest) {
        return doScaReceived(authorisationProcessorRequest);
    }

    @Override
    public AuthorisationProcessorResponse doScaFinalised(AuthorisationProcessorRequest authorisationProcessorRequest) {
        PaymentAuthorisationParameters request = (PaymentAuthorisationParameters) authorisationProcessorRequest.getUpdateAuthorisationRequest();

        Xs2aCurrencyConversionInfo xs2aCurrencyConversionInfo = getSpiCurrencyConversionInfoSpiResponse(authorisationProcessorRequest, request);
        return new Xs2aUpdatePisCommonPaymentPsuDataResponse(FINALISED, request.getBusinessObjectId(),
                                                             request.getAuthorisationId(), request.getPsuData(),
                                                             xs2aCurrencyConversionInfo);
    }

    private Xs2aCurrencyConversionInfo getSpiCurrencyConversionInfoSpiResponse(AuthorisationProcessorRequest authorisationProcessorRequest,
                                                                               PaymentAuthorisationParameters request) {
        Authorisation authorisation = authorisationProcessorRequest.getAuthorisation();
        PsuIdData psuData = extractPsuIdData(request, authorisation);
        String authorisationId = request.getAuthorisationId();
        SpiPayment payment = getSpiPayment(request.getPaymentId());
        SpiContextData contextData = spiContextDataProvider.provideWithPsuIdData(psuData);
        SpiAspspConsentDataProvider aspspConsentDataProvider = aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(request.getPaymentId());

        return getCurrencyConversionInfo(contextData, payment, authorisationId, aspspConsentDataProvider);
    }

    @Override
    public AuthorisationProcessorResponse doScaPsuAuthenticated(AuthorisationProcessorRequest authorisationProcessorRequest) {
        PaymentAuthorisationParameters request = (PaymentAuthorisationParameters) authorisationProcessorRequest.getUpdateAuthorisationRequest();

        SpiPayment payment = getSpiPayment(request.getPaymentId());

        if (isDecoupledApproach(request.getAuthorisationId(), request.getAuthenticationMethodId())) {
            xs2aAuthorisationService.updateScaApproach(request.getAuthorisationId(), ScaApproach.DECOUPLED);
            return proceedDecoupledApproach(request, payment, request.getAuthenticationMethodId());
        }

        return proceedEmbeddedApproach(authorisationProcessorRequest, payment);
    }

    @Override
    public AuthorisationProcessorResponse doScaMethodSelected(AuthorisationProcessorRequest authorisationProcessorRequest) {
        PaymentAuthorisationParameters request = (PaymentAuthorisationParameters) authorisationProcessorRequest.getUpdateAuthorisationRequest();

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
        SpiResponse<SpiPaymentExecutionResponse> spiResponse = verifyScaAuthorisationAndExecutePayment(authorisation, payment,
                                                                                              spiScaConfirmation,
                                                                                              contextData,
                                                                                              aspspConsentDataProvider);

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS);
            writeErrorLog(authorisationProcessorRequest, psuData, errorHolder, "Verify SCA authorisation and execute payment has failed.");

            SpiPaymentExecutionResponse spiPaymentResponse = spiResponse.getPayload();
            if (spiPaymentResponse != null && spiPaymentResponse.getSpiAuthorisationStatus() == SpiAuthorisationStatus.ATTEMPT_FAILURE) {
                return new Xs2aUpdatePisCommonPaymentPsuDataResponse(authorisationProcessorRequest.getScaStatus(), errorHolder, paymentId, authorisationId, psuData);
            }

            Optional<MessageErrorCode> first = errorHolder.getFirstErrorCode();
            if (first.isPresent() && first.get() == MessageErrorCode.PSU_CREDENTIALS_INVALID) {
                xs2aAuthorisationService.updateAuthorisationStatus(authorisationId, FAILED);
            }
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, psuData);
        }

        updatePaymentDataByPaymentResponse(paymentId, spiResponse);
        Xs2aCurrencyConversionInfo xs2aCurrencyConversionInfo = getCurrencyConversionInfo(contextData, payment,
                                                                                          request.getAuthorisationId(), aspspConsentDataProvider);

        return new Xs2aUpdatePisCommonPaymentPsuDataResponse(FINALISED, paymentId, authorisationId, psuData, xs2aCurrencyConversionInfo);
    }

    abstract void updatePaymentDataByPaymentResponse(String paymentId, SpiResponse<SpiPaymentExecutionResponse> spiResponse);

    abstract SpiResponse<SpiAuthorizationCodeResult> requestAuthorisationCode(SpiPayment payment, String authenticationMethodId,
                                                                              SpiContextData spiContextData,
                                                                              SpiAspspConsentDataProvider spiAspspConsentDataProvider);

    abstract SpiResponse<SpiPaymentExecutionResponse> verifyScaAuthorisationAndExecutePayment(Authorisation authorisation,
                                                                 SpiPayment payment,
                                                                 SpiScaConfirmation spiScaConfirmation,
                                                                 SpiContextData contextData,
                                                                 SpiAspspConsentDataProvider spiAspspConsentDataProvider);

    abstract SpiResponse<SpiPsuAuthorisationResponse> authorisePsu(PaymentAuthorisationParameters request, SpiPayment payment,
                                                                   SpiAspspConsentDataProvider aspspConsentDataProvider, SpiPsuData spiPsuData,
                                                                   SpiContextData contextData, String authorisationId);

    abstract SpiResponse<SpiAvailableScaMethodsResponse> requestAvailableScaMethods(SpiPayment payment,
                                                                                    SpiAspspConsentDataProvider aspspConsentDataProvider,
                                                                                    SpiContextData contextData);

    abstract Xs2aUpdatePisCommonPaymentPsuDataResponse executePaymentWithoutSca(AuthorisationProcessorRequest authorisationProcessorRequest,
                                                                                PsuIdData psuData, PaymentType paymentType, SpiPayment payment,
                                                                                SpiContextData contextData, ScaStatus resultScaStatus,
                                                                                Xs2aCurrencyConversionInfo xs2aCurrencyConversionInfo);

    abstract Xs2aUpdatePisCommonPaymentPsuDataResponse proceedDecoupledApproach(PaymentAuthorisationParameters request,
                                                                                SpiPayment payment, String authenticationMethodId);


    abstract boolean needProcessExemptedSca(PaymentType paymentType, boolean isScaExempted);

    abstract Xs2aCurrencyConversionInfo getCurrencyConversionInfo(SpiContextData contextData, SpiPayment payment,
                                                                   String authorisationId, SpiAspspConsentDataProvider aspspConsentDataProvider);

    PisScaAuthorisationService getService(ScaApproach scaApproach) {
        return services.stream().filter(s -> s.getScaApproachServiceType() == scaApproach).findFirst()
                   .orElseThrow(() -> new IllegalArgumentException("Pis cancellation authorisation service was not found for approach " + scaApproach));
    }

    Xs2aUpdatePisCommonPaymentPsuDataResponse applyAuthorisation(AuthorisationProcessorRequest authorisationProcessorRequest) {
        PaymentAuthorisationParameters request = (PaymentAuthorisationParameters) authorisationProcessorRequest.getUpdateAuthorisationRequest();
        Authorisation authorisation = authorisationProcessorRequest.getAuthorisation();
        PsuIdData psuData = extractPsuIdData(request, authorisation);
        String authorisationId = request.getAuthorisationId();
        String paymentId = request.getPaymentId();
        SpiPayment payment = getSpiPayment(paymentId);
        request.setPsuData(psuData);

        SpiAspspConsentDataProvider aspspConsentDataProvider = aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(paymentId);

        SpiPsuData spiPsuData = xs2aToSpiPsuDataMapper.mapToSpiPsuData(psuData);
        SpiContextData contextData = spiContextDataProvider.provideWithPsuIdData(psuData);

        SpiResponse<SpiPsuAuthorisationResponse> authPsuResponse = authorisePsu(request, payment, aspspConsentDataProvider, spiPsuData, contextData, authorisationId);
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
            xs2aAuthorisationService.updateAuthorisationStatus(authorisationId, FAILED);
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, psuData);
        }

        Xs2aCurrencyConversionInfo xs2aCurrencyConversionInfo = getCurrencyConversionInfo(contextData, payment, request.getAuthorisationId(), aspspConsentDataProvider);

        PaymentType paymentType = request.getPaymentService();
        if (needProcessExemptedSca(paymentType, psuAuthorisationResponse.isScaExempted())) {
            writeInfoLog(authorisationProcessorRequest, psuData, "SCA was exempted for the payment after AuthorisationSpi#authorisePsu.");
            return executePaymentWithoutSca(authorisationProcessorRequest, psuData, paymentType, payment, contextData, EXEMPTED, xs2aCurrencyConversionInfo);
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
            return executePaymentWithoutSca(authorisationProcessorRequest, psuData, paymentType, payment, contextData, EXEMPTED, xs2aCurrencyConversionInfo);
        }

        List<AuthenticationObject> spiScaMethods = availableScaMethods.getAvailableScaMethods();

        return processScaMethods(authorisationProcessorRequest, psuData, paymentType, payment, aspspConsentDataProvider,
                                 contextData, spiScaMethods, xs2aCurrencyConversionInfo);
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse processScaMethods(@NotNull AuthorisationProcessorRequest authorisationProcessorRequest,
                                                                        PsuIdData psuData, PaymentType paymentType, SpiPayment payment,
                                                                        SpiAspspConsentDataProvider aspspConsentDataProvider, SpiContextData contextData,
                                                                        List<AuthenticationObject> spiScaMethods, Xs2aCurrencyConversionInfo xs2aCurrencyConversionInfo) {
        PaymentAuthorisationParameters request = (PaymentAuthorisationParameters) authorisationProcessorRequest.getUpdateAuthorisationRequest();

        if (CollectionUtils.isEmpty(spiScaMethods)) {
            writeInfoLog(authorisationProcessorRequest, psuData, "Available SCA methods is empty.");
            return executePaymentWithoutSca(authorisationProcessorRequest, psuData, paymentType, payment, contextData, FINALISED, xs2aCurrencyConversionInfo);
        } else if (isSingleScaMethod(spiScaMethods)) {
            return buildUpdateResponseWhenScaMethodIsSingle(authorisationProcessorRequest, psuData, payment, aspspConsentDataProvider, contextData, spiScaMethods, xs2aCurrencyConversionInfo);
        } else if (isMultipleScaMethods(spiScaMethods)) {
            return buildUpdateResponseWhenScaMethodsAreMultiple(request, psuData, spiScaMethods);
        }

        writeInfoLog(authorisationProcessorRequest, psuData, "Apply authorisation when update payment PSU data set SCA status failed.");
        return new Xs2aUpdatePisCommonPaymentPsuDataResponse(FAILED, request.getPaymentId(), request.getAuthorisationId(),
                                                             psuData, xs2aCurrencyConversionInfo);
    }

    Xs2aUpdatePisCommonPaymentPsuDataResponse applyIdentification(AuthorisationProcessorRequest authorisationProcessorRequest) {
        PaymentAuthorisationParameters request = (PaymentAuthorisationParameters) authorisationProcessorRequest.getUpdateAuthorisationRequest();
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
        SpiContextData contextData = spiContextDataProvider.provideWithPsuIdData(psuData);
        SpiAspspConsentDataProvider aspspConsentDataProvider = aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(request.getPaymentId());
        SpiPayment payment = getSpiPayment(request.getPaymentId());

        Xs2aCurrencyConversionInfo xs2aCurrencyConversionInfo = getCurrencyConversionInfo(contextData, payment, authorisationId, aspspConsentDataProvider);
        return new Xs2aUpdatePisCommonPaymentPsuDataResponse(PSUIDENTIFIED, paymentId, authorisationId, psuData,xs2aCurrencyConversionInfo);
    }

    Xs2aUpdatePisCommonPaymentPsuDataResponse buildUpdateResponseWhenScaMethodsAreMultiple(PaymentAuthorisationParameters request,
                                                                                           PsuIdData psuData,
                                                                                           List<AuthenticationObject> spiScaMethods) {
        xs2aAuthorisationService.saveAuthenticationMethods(request.getAuthorisationId(), spiScaMethods);

        Xs2aUpdatePisCommonPaymentPsuDataResponse response = new Xs2aUpdatePisCommonPaymentPsuDataResponse
                                                                 (PSUAUTHENTICATED,
                                                                  request.getPaymentId(),
                                                                  request.getAuthorisationId(),
                                                                  psuData);
        response.setAvailableScaMethods(spiScaMethods);
        return response;
    }

    Xs2aUpdatePisCommonPaymentPsuDataResponse buildUpdateResponseWhenScaMethodIsSingle(AuthorisationProcessorRequest authorisationProcessorRequest,
                                                                                       PsuIdData psuData, SpiPayment payment, SpiAspspConsentDataProvider aspspConsentDataProvider,
                                                                                       SpiContextData contextData, List<AuthenticationObject> scaMethods, Xs2aCurrencyConversionInfo xs2aCurrencyConversionInfo) {
        PaymentAuthorisationParameters request = (PaymentAuthorisationParameters) authorisationProcessorRequest.getUpdateAuthorisationRequest();
        xs2aAuthorisationService.saveAuthenticationMethods(request.getAuthorisationId(), scaMethods);
        AuthenticationObject chosenMethod = scaMethods.get(0);

        if (chosenMethod.isDecoupled()) {
            xs2aAuthorisationService.updateScaApproach(request.getAuthorisationId(), ScaApproach.DECOUPLED);
            return proceedDecoupledApproach(request, payment, chosenMethod.getAuthenticationMethodId());
        }

        return proceedSingleScaEmbeddedApproach(authorisationProcessorRequest, payment, chosenMethod, contextData,
                                                aspspConsentDataProvider, psuData, xs2aCurrencyConversionInfo);
    }

    protected SpiPayment getSpiPayment(String encryptedPaymentId) {
        Optional<PisCommonPaymentResponse> commonPaymentById = xs2aPisCommonPaymentService.getPisCommonPaymentById(encryptedPaymentId);
        return commonPaymentById
                   .map(pisCommonPaymentResponse -> xs2aToSpiPaymentMapper.mapToSpiPayment(pisCommonPaymentResponse))
                   .orElse(null);
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse proceedSingleScaEmbeddedApproach(AuthorisationProcessorRequest authorisationProcessorRequest,
                                                                                       SpiPayment payment,
                                                                                       AuthenticationObject chosenMethod,
                                                                                       SpiContextData contextData,
                                                                                       SpiAspspConsentDataProvider spiAspspConsentDataProvider,
                                                                                       PsuIdData psuData, Xs2aCurrencyConversionInfo xs2aCurrencyConversionInfo) {
        PaymentAuthorisationParameters request = (PaymentAuthorisationParameters) authorisationProcessorRequest.getUpdateAuthorisationRequest();
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
            return executePaymentWithoutSca(authorisationProcessorRequest, psuData, payment.getPaymentType(), payment, contextData, EXEMPTED, xs2aCurrencyConversionInfo);
        }

        ScaStatus scaStatus = ObjectUtils.defaultIfNull(authorizationCodeResult.getScaStatus(), ScaStatus.SCAMETHODSELECTED);

        Xs2aUpdatePisCommonPaymentPsuDataResponse response = new Xs2aUpdatePisCommonPaymentPsuDataResponse(
            scaStatus, payment.getPaymentId(), authorisationId, psuData, xs2aCurrencyConversionInfo);
        response.setChosenScaMethod(authorizationCodeResult.getSelectedScaMethod());
        response.setChallengeData(mapToChallengeData(authorizationCodeResult));
        return response;
    }

    private boolean isDecoupledApproach(String authorisationId, String authenticationMethodId) {
        return xs2aAuthorisationService.isAuthenticationMethodDecoupled(authorisationId, authenticationMethodId);
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse proceedEmbeddedApproach(AuthorisationProcessorRequest authorisationProcessorRequest, SpiPayment payment) {
        PaymentAuthorisationParameters request = (PaymentAuthorisationParameters) authorisationProcessorRequest.getUpdateAuthorisationRequest();
        Authorisation authorisation = authorisationProcessorRequest.getAuthorisation();
        String authenticationMethodId = request.getAuthenticationMethodId();
        String authorisationId = request.getAuthorisationId();
        String paymentId = request.getPaymentId();
        PsuIdData psuData = extractPsuIdData(request, authorisation);

        SpiContextData contextData = spiContextDataProvider.provideWithPsuIdData(psuData);
        SpiAspspConsentDataProvider aspspConsentDataProvider = aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(paymentId);

        SpiResponse<SpiAuthorizationCodeResult> spiResponse = requestAuthorisationCode(payment, authenticationMethodId, contextData, aspspConsentDataProvider);

        if (payment == null || spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS);
            writeErrorLog(authorisationProcessorRequest, psuData, errorHolder, EMBEDDED_SELECTING_SCA_METHOD_FAILED_MSG);

            Optional<MessageErrorCode> first = errorHolder.getFirstErrorCode();
            if (first.isPresent() && first.get() == MessageErrorCode.PSU_CREDENTIALS_INVALID) {
                xs2aAuthorisationService.updateAuthorisationStatus(authorisationId, FAILED);
            }
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, psuData);
        }

        Xs2aCurrencyConversionInfo xs2aCurrencyConversionInfo = getCurrencyConversionInfo(contextData, payment, request.getAuthorisationId(), aspspConsentDataProvider);

        SpiAuthorizationCodeResult authorizationCodeResult = spiResponse.getPayload();
        if (needProcessExemptedSca(payment.getPaymentType(), authorizationCodeResult.isScaExempted())) {
            writeInfoLog(authorisationProcessorRequest, psuData, "SCA was exempted for the payment after AuthorisationSpi#requestAuthorisationCode.");
            return executePaymentWithoutSca(authorisationProcessorRequest, psuData, payment.getPaymentType(), payment, contextData, EXEMPTED, xs2aCurrencyConversionInfo);
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

        Xs2aUpdatePisCommonPaymentPsuDataResponse response = new Xs2aUpdatePisCommonPaymentPsuDataResponse(
            SCAMETHODSELECTED, paymentId, authorisationId, psuData, xs2aCurrencyConversionInfo);
        response.setChosenScaMethod(authenticationObject);
        response.setChallengeData(challengeData);
        return response;
    }

    private void writeErrorLog(AuthorisationProcessorRequest request, PsuIdData psuData, ErrorHolder errorHolder, String message) {
        String messageToLog = String.format("Payment-ID [{}], Authorisation-ID [{}], PSU-ID [{}], SCA Approach [{}]. %s Error msg: [{}]", message);
        log.warn(messageToLog,
                 request.getUpdateAuthorisationRequest().getBusinessObjectId(),
                 request.getUpdateAuthorisationRequest().getAuthorisationId(),
                 psuData != null ? psuData.getPsuId() : "-",
                 request.getScaApproach(),
                 errorHolder);
    }

    private void writeInfoLog(AuthorisationProcessorRequest request, PsuIdData psuData, String message) {
        String messageToLog = String.format("Payment-ID [{}], Authorisation-ID [{}], PSU-ID [{}], SCA Approach [{}]. %s", message);
        log.info(messageToLog,
                 request.getUpdateAuthorisationRequest().getBusinessObjectId(),
                 request.getUpdateAuthorisationRequest().getAuthorisationId(),
                 psuData != null ? psuData.getPsuId() : "-",
                 request.getScaApproach());
    }
}
