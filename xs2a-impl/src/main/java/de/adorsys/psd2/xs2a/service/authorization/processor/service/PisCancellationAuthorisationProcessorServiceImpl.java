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

import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.pis.InternalPaymentStatus;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.pis.Xs2aCurrencyConversionInfo;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisCommonDecoupledService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.consent.PisAspspDataService;
import de.adorsys.psd2.xs2a.service.consent.PisPsuDataService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisCommonPaymentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aPisCommonPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.service.payment.Xs2aUpdatePaymentAfterSpiService;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAvailableScaMethodsResponse;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiPsuAuthorisationResponse;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentExecutionResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PaymentCancellationSpi;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PisCancellationAuthorisationProcessorServiceImpl extends PaymentBaseAuthorisationProcessorService {

    private final PaymentCancellationSpi paymentCancellationSpi;
    private final SpiErrorMapper spiErrorMapper;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final Xs2aUpdatePaymentAfterSpiService updatePaymentAfterSpiService;
    private final PisCommonDecoupledService pisCommonDecoupledService;
    private final PisPsuDataService pisPsuDataService;

    public PisCancellationAuthorisationProcessorServiceImpl(List<PisScaAuthorisationService> services,
                                                            Xs2aToSpiPaymentMapper xs2aToSpiPaymentMapper, PaymentCancellationSpi paymentCancellationSpi,
                                                            PisAspspDataService pisAspspDataService, Xs2aPisCommonPaymentMapper xs2aPisCommonPaymentMapper,
                                                            SpiContextDataProvider spiContextDataProvider, SpiErrorMapper spiErrorMapper,
                                                            SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory,
                                                            Xs2aUpdatePaymentAfterSpiService updatePaymentAfterSpiService,
                                                            Xs2aAuthorisationService xs2aAuthorisationService,
                                                            Xs2aPisCommonPaymentService xs2aPisCommonPaymentService,
                                                            PisCommonDecoupledService pisCommonDecoupledService,
                                                            PisPsuDataService pisPsuDataService,
                                                            Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper) {
        super(services, xs2aAuthorisationService, xs2aPisCommonPaymentService, xs2aToSpiPaymentMapper,
              spiContextDataProvider, aspspConsentDataProviderFactory, spiErrorMapper,
              pisAspspDataService, xs2aPisCommonPaymentMapper, xs2aToSpiPsuDataMapper);
        this.paymentCancellationSpi = paymentCancellationSpi;
        this.spiErrorMapper = spiErrorMapper;
        this.aspspConsentDataProviderFactory = aspspConsentDataProviderFactory;
        this.updatePaymentAfterSpiService = updatePaymentAfterSpiService;
        this.pisCommonDecoupledService = pisCommonDecoupledService;
        this.pisPsuDataService = pisPsuDataService;
    }

    @Override
    public void updateAuthorisation(AuthorisationProcessorRequest request, AuthorisationProcessorResponse response) {
        PisScaAuthorisationService authorizationService = getService(request.getScaApproach());
        authorizationService.updateCancellationAuthorisation(request.getUpdateAuthorisationRequest(), response);
    }

    @Override
    public AuthorisationProcessorResponse doScaReceived(AuthorisationProcessorRequest authorisationProcessorRequest) {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = (Xs2aUpdatePisCommonPaymentPsuDataRequest) authorisationProcessorRequest.getUpdateAuthorisationRequest();
        Authorisation authorisation = authorisationProcessorRequest.getAuthorisation();
        return request.isUpdatePsuIdentification() && authorisation.getChosenScaApproach() != ScaApproach.DECOUPLED
                   ? applyIdentification(authorisationProcessorRequest)
                   : applyAuthorisation(authorisationProcessorRequest);
    }

    @Override
    SpiResponse<SpiPaymentExecutionResponse> verifyScaAuthorisationAndExecutePayment(Authorisation authorisation,
                                                                            SpiPayment payment, SpiScaConfirmation spiScaConfirmation,
                                                                            SpiContextData contextData, SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return paymentCancellationSpi.verifyScaAuthorisationAndCancelPaymentWithResponse(contextData, spiScaConfirmation, payment, spiAspspConsentDataProvider);
    }

    @Override
    SpiResponse<SpiPsuAuthorisationResponse> authorisePsu(Xs2aUpdatePisCommonPaymentPsuDataRequest request, SpiPayment payment, SpiAspspConsentDataProvider aspspConsentDataProvider, SpiPsuData spiPsuData, SpiContextData contextData, String authorisationId) {
        return paymentCancellationSpi.authorisePsu(contextData, authorisationId, spiPsuData, request.getPassword(),
                                                   payment, aspspConsentDataProvider);
    }

    @Override
    SpiResponse<SpiAvailableScaMethodsResponse> requestAvailableScaMethods(SpiPayment payment, SpiAspspConsentDataProvider aspspConsentDataProvider, SpiContextData contextData) {
        return paymentCancellationSpi.requestAvailableScaMethods(contextData, payment, aspspConsentDataProvider);
    }

    @Override
    Xs2aUpdatePisCommonPaymentPsuDataResponse executePaymentWithoutSca(AuthorisationProcessorRequest authorisationProcessorRequest,
                                                                       PsuIdData psuData, PaymentType paymentType, SpiPayment payment,
                                                                       SpiContextData contextData, ScaStatus resultScaStatus, Xs2aCurrencyConversionInfo xs2aCurrencyConversionInfo) {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = (Xs2aUpdatePisCommonPaymentPsuDataRequest) authorisationProcessorRequest.getUpdateAuthorisationRequest();
        String authorisationId = request.getAuthorisationId();
        String paymentId = request.getPaymentId();

        final SpiAspspConsentDataProvider aspspConsentDataProvider = aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(paymentId);

        writeInfoLog(authorisationProcessorRequest, psuData, "Available SCA methods is empty.");
        SpiResponse<SpiResponse.VoidResponse> executePaymentResponse = paymentCancellationSpi.cancelPaymentWithoutSca(contextData, payment, aspspConsentDataProvider);

        if (executePaymentResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(executePaymentResponse, ServiceType.PIS);
            writeErrorLog(authorisationProcessorRequest, psuData, errorHolder, "Cancel payment without SCA has failed.");
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, psuData);
        }

        updatePaymentAfterSpiService.updatePaymentStatus(paymentId, TransactionStatus.CANC);
        updatePaymentAfterSpiService.updateInternalPaymentStatus(paymentId, InternalPaymentStatus.CANCELLED_FINALISED);

        return new Xs2aUpdatePisCommonPaymentPsuDataResponse(ScaStatus.FINALISED, paymentId, authorisationId, psuData);
    }

    @Override
    boolean needProcessExemptedSca(PaymentType paymentType, boolean isScaExempted) {
        return false;
    }

    @Override
    Xs2aCurrencyConversionInfo getCurrencyConversionInfo(SpiContextData contextData, SpiPayment payment,
                                                         String authorisationId, SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return null;
    }

    @Override
    void updatePaymentDataByPaymentResponse(String paymentId, SpiResponse<SpiPaymentExecutionResponse> spiResponse) {
        updatePaymentAfterSpiService.updatePaymentStatus(paymentId, TransactionStatus.CANC);
        updatePaymentAfterSpiService.updateInternalPaymentStatus(paymentId, InternalPaymentStatus.CANCELLED_FINALISED);
    }

    @Override
    Xs2aUpdatePisCommonPaymentPsuDataResponse applyIdentification(AuthorisationProcessorRequest authorisationProcessorRequest) {

        Xs2aUpdatePisCommonPaymentPsuDataRequest request = (Xs2aUpdatePisCommonPaymentPsuDataRequest) authorisationProcessorRequest.getUpdateAuthorisationRequest();
        String paymentId = request.getPaymentId();
        String authorisationId = request.getAuthorisationId();
        PsuIdData psuData = request.getPsuData();

        if (!isPsuDataCorrect(paymentId, psuData)) {
            ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_401)
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.UNAUTHORIZED_NO_PSU))
                                          .build();
            writeErrorLog(authorisationProcessorRequest, psuData, errorHolder, "Apply Identification when update payment PSU data has failed. PSU credentials invalid.");
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, psuData);
        }

        return super.applyIdentification(authorisationProcessorRequest);
    }

    @Override
    SpiResponse<SpiAuthorizationCodeResult> requestAuthorisationCode(SpiPayment payment, String authenticationMethodId, SpiContextData spiContextData, SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return paymentCancellationSpi.requestAuthorisationCode(spiContextData, authenticationMethodId, payment, spiAspspConsentDataProvider);
    }

    @Override
    Xs2aUpdatePisCommonPaymentPsuDataResponse proceedDecoupledApproach(Xs2aUpdatePisCommonPaymentPsuDataRequest request, SpiPayment payment, String authenticationMethodId) {
        return pisCommonDecoupledService.proceedDecoupledCancellation(request, payment, authenticationMethodId);
    }

    private boolean isPsuDataCorrect(String paymentId, PsuIdData psuData) {
        List<PsuIdData> psuIdDataList = pisPsuDataService.getPsuDataByPaymentId(paymentId);

        return psuIdDataList.stream()
                   .anyMatch(psu -> psu.contentEquals(psuData));
    }
}
