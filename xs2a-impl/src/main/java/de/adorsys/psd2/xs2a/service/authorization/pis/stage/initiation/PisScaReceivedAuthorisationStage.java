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

package de.adorsys.psd2.xs2a.service.authorization.pis.stage.initiation;


import de.adorsys.psd2.consent.api.pis.authorisation.GetPisAuthorisationResponse;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ChallengeData;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisCommonDecoupledService;
import de.adorsys.psd2.xs2a.service.authorization.pis.stage.PisScaStage;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisCommonPaymentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.consent.CmsToXs2aPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.*;
import de.adorsys.psd2.xs2a.service.payment.Xs2aUpdatePaymentAfterSpiService;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthenticationObject;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentExecutionResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PaymentAuthorisationSpi;
import de.adorsys.psd2.xs2a.spi.service.PaymentSpi;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;

import static de.adorsys.psd2.xs2a.core.sca.ScaStatus.*;

@Slf4j
@Service("PIS_EMBEDDED_RECEIVED")
public class PisScaReceivedAuthorisationStage extends PisScaStage<Xs2aUpdatePisCommonPaymentPsuDataRequest, GetPisAuthorisationResponse, Xs2aUpdatePisCommonPaymentPsuDataResponse> {
    private final PaymentAuthorisationSpi paymentAuthorisationSpi;
    private final Xs2aUpdatePaymentAfterSpiService updatePaymentAfterSpiService;
    private final Xs2aPisCommonPaymentService xs2aPisCommonPaymentService;
    private final PisCommonDecoupledService pisCommonDecoupledService;
    private final SpiContextDataProvider spiContextDataProvider;
    private final SpiErrorMapper spiErrorMapper;
    private final Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper;
    private final SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final RequestProviderService requestProviderService;

    public PisScaReceivedAuthorisationStage(CmsToXs2aPaymentMapper cmsToXs2aPaymentMapper, Xs2aToSpiPeriodicPaymentMapper xs2aToSpiPeriodicPaymentMapper, Xs2aToSpiSinglePaymentMapper xs2aToSpiSinglePaymentMapper, Xs2aToSpiBulkPaymentMapper xs2aToSpiBulkPaymentMapper, PisCommonPaymentServiceEncrypted pisCommonPaymentServiceEncrypted, ApplicationContext applicationContext, PaymentAuthorisationSpi paymentAuthorisationSpi, Xs2aUpdatePaymentAfterSpiService updatePaymentAfterSpiService, Xs2aPisCommonPaymentService xs2aPisCommonPaymentService, PisCommonDecoupledService pisCommonDecoupledService, SpiContextDataProvider spiContextDataProvider, SpiErrorMapper spiErrorMapper, Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper, SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper, SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory, RequestProviderService requestProviderService) {
        super(cmsToXs2aPaymentMapper, xs2aToSpiPeriodicPaymentMapper, xs2aToSpiSinglePaymentMapper, xs2aToSpiBulkPaymentMapper, pisCommonPaymentServiceEncrypted, applicationContext, xs2aToSpiPsuDataMapper);
        this.paymentAuthorisationSpi = paymentAuthorisationSpi;
        this.updatePaymentAfterSpiService = updatePaymentAfterSpiService;
        this.xs2aPisCommonPaymentService = xs2aPisCommonPaymentService;
        this.pisCommonDecoupledService = pisCommonDecoupledService;
        this.spiContextDataProvider = spiContextDataProvider;
        this.spiErrorMapper = spiErrorMapper;
        this.xs2aToSpiPsuDataMapper = xs2aToSpiPsuDataMapper;
        this.spiToXs2aAuthenticationObjectMapper = spiToXs2aAuthenticationObjectMapper;
        this.aspspConsentDataProviderFactory = aspspConsentDataProviderFactory;
        this.requestProviderService = requestProviderService;
    }

    @Override
    public Xs2aUpdatePisCommonPaymentPsuDataResponse apply(Xs2aUpdatePisCommonPaymentPsuDataRequest request, GetPisAuthorisationResponse pisAuthorisationResponse) {
        return request.isUpdatePsuIdentification()
                   ? applyIdentification(request)
                   : applyAuthorisation(request, pisAuthorisationResponse);
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse applyAuthorisation(Xs2aUpdatePisCommonPaymentPsuDataRequest request, GetPisAuthorisationResponse pisAuthorisationResponse) {
        PsuIdData psuData = extractPsuIdData(request, false);
        PaymentType paymentType = pisAuthorisationResponse.getPaymentType();
        String paymentProduct = pisAuthorisationResponse.getPaymentProduct();
        SpiPayment payment = mapToSpiPayment(pisAuthorisationResponse, paymentType, paymentProduct);
        String authenticationId = request.getAuthorisationId();
        String paymentId = request.getPaymentId();

        SpiAspspConsentDataProvider aspspConsentDataProvider = aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(request.getPaymentId());

        SpiPsuData spiPsuData = xs2aToSpiPsuDataMapper.mapToSpiPsuData(psuData);
        SpiContextData contextData = spiContextDataProvider.provideWithPsuIdData(psuData);

        SpiResponse<SpiAuthorisationStatus> authPsuResponse = paymentAuthorisationSpi.authorisePsu(contextData, spiPsuData, request.getPassword(), payment, aspspConsentDataProvider);

        if (authPsuResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(authPsuResponse, ServiceType.PIS);
            log.warn("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}], Authorisation-ID [{}], PSU-ID [{}]. PIS_EMBEDDED_RECEIVED stage. Authorise PSU when apply authorisation has failed. Error msg: [{}]",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), paymentId, authenticationId, psuData.getPsuId(), errorHolder);
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authenticationId, psuData);
        }

        if (authPsuResponse.getPayload() == SpiAuthorisationStatus.FAILURE) {
            ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_401)
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.PSU_CREDENTIALS_INVALID))
                                          .build();
            log.warn("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}], Authorisation-ID [{}], PSU-ID [{}]. PIS_EMBEDDED_RECEIVED stage. PSU authorisation failed due to incorrect credentials. Error msg: [{}].",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), paymentId, authenticationId, psuData.getPsuId(), errorHolder);
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authenticationId, psuData);
        }

        SpiResponse<List<SpiAuthenticationObject>> availableScaMethodsResponse = paymentAuthorisationSpi.requestAvailableScaMethods(contextData, payment, aspspConsentDataProvider);

        if (availableScaMethodsResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(availableScaMethodsResponse, ServiceType.PIS);
            log.warn("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}], Authorisation-ID [{}], PSU-ID [{}]. PIS_EMBEDDED_RECEIVED stage. Request available SCA methods has failed. Error msg: [{}]",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), paymentId, authenticationId, psuData.getPsuId(), errorHolder);
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authenticationId, psuData);
        }

        List<SpiAuthenticationObject> spiScaMethods = availableScaMethodsResponse.getPayload();

        if (CollectionUtils.isEmpty(spiScaMethods)) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}], Authorisation-ID [{}], PSU-ID [{}]. PIS_EMBEDDED_RECEIVED stage. Available SCA methods is empty.",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), paymentId, authenticationId, psuData.getPsuId());
            return buildUpdateResponseWhenScaMethodsAreEmpty(request, pisAuthorisationResponse, psuData, paymentType, payment, contextData);
        } else if (isSingleScaMethod(spiScaMethods)) {

            return buildUpdateResponseWhenScaMethodIsSingle(request, psuData, payment, aspspConsentDataProvider, contextData, spiScaMethods);
        } else if (isMultipleScaMethods(spiScaMethods)) {

            return buildUpdateResponseWhenScaMethodsAreMultiple(request, psuData, spiScaMethods);
        }

        log.info("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}], Authorisation-ID [{}]. PIS_EMBEDDED_RECEIVED stage. Apply authorisation when update payment PSU data set SCA status failed.",
                 requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), paymentId, authenticationId);
        return new Xs2aUpdatePisCommonPaymentPsuDataResponse(FAILED, request.getPaymentId(), request.getAuthorisationId(), psuData);
    }

    @NotNull
    private Xs2aUpdatePisCommonPaymentPsuDataResponse buildUpdateResponseWhenScaMethodsAreMultiple(Xs2aUpdatePisCommonPaymentPsuDataRequest request, PsuIdData psuData, List<SpiAuthenticationObject> spiScaMethods) {
        xs2aPisCommonPaymentService.saveAuthenticationMethods(request.getAuthorisationId(), spiToXs2aAuthenticationObjectMapper.mapToXs2aListAuthenticationObject(spiScaMethods));
        Xs2aUpdatePisCommonPaymentPsuDataResponse response = new Xs2aUpdatePisCommonPaymentPsuDataResponse(PSUAUTHENTICATED, request.getPaymentId(), request.getAuthorisationId(), psuData);
        response.setAvailableScaMethods(spiToXs2aAuthenticationObjectMapper.mapToXs2aListAuthenticationObject(spiScaMethods));
        response.setPsuData(psuData);
        return response;
    }

    @NotNull
    private Xs2aUpdatePisCommonPaymentPsuDataResponse buildUpdateResponseWhenScaMethodIsSingle(Xs2aUpdatePisCommonPaymentPsuDataRequest request, PsuIdData psuData, SpiPayment payment, SpiAspspConsentDataProvider aspspConsentDataProvider, SpiContextData contextData, List<SpiAuthenticationObject> spiScaMethods) {
        xs2aPisCommonPaymentService.saveAuthenticationMethods(request.getAuthorisationId(), spiToXs2aAuthenticationObjectMapper.mapToXs2aListAuthenticationObject(spiScaMethods));
        SpiAuthenticationObject chosenMethod = spiScaMethods.get(0);

        if (chosenMethod.isDecoupled()) {
            xs2aPisCommonPaymentService.updateScaApproach(request.getAuthorisationId(), ScaApproach.DECOUPLED);
            return pisCommonDecoupledService.proceedDecoupledInitiation(request, payment, chosenMethod.getAuthenticationMethodId());
        }

        return proceedSingleScaEmbeddedApproach(payment, chosenMethod, contextData, aspspConsentDataProvider, request, psuData);
    }

    @NotNull
    private Xs2aUpdatePisCommonPaymentPsuDataResponse buildUpdateResponseWhenScaMethodsAreEmpty(Xs2aUpdatePisCommonPaymentPsuDataRequest request, GetPisAuthorisationResponse pisAuthorisationResponse, PsuIdData psuData, PaymentType paymentType, SpiPayment payment, SpiContextData contextData) {
        String authorisationId = request.getAuthorisationId();
        String paymentId = request.getPaymentId();

        final SpiAspspConsentDataProvider aspspConsentDataProvider =
            aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(request.getPaymentId());

        PaymentSpi paymentSpi = getPaymentService(pisAuthorisationResponse, paymentType);
        SpiResponse<SpiPaymentExecutionResponse> spiResponse = paymentSpi.executePaymentWithoutSca(contextData, payment, aspspConsentDataProvider);

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS);
            log.warn("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}], Authorisation-ID [{}], PSU-ID [{}]. PIS_EMBEDDED_RECEIVED stage. Execute payment without SCA has failed. Error msg: [{}]",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), paymentId, authorisationId, psuData.getPsuId(), errorHolder);
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, psuData);
        }

        TransactionStatus paymentStatus = spiResponse.getPayload().getTransactionStatus();

        if (paymentStatus == TransactionStatus.PATC) {
            xs2aPisCommonPaymentService.updateMultilevelSca(paymentId, true);
        }

        updatePaymentAfterSpiService.updatePaymentStatus(request.getPaymentId(), paymentStatus);
        return new Xs2aUpdatePisCommonPaymentPsuDataResponse(FINALISED, paymentId, authorisationId, psuData);
    }

    @NotNull
    private Xs2aUpdatePisCommonPaymentPsuDataResponse proceedSingleScaEmbeddedApproach(SpiPayment payment, SpiAuthenticationObject chosenMethod, SpiContextData contextData, SpiAspspConsentDataProvider aspspConsentDataProvider, Xs2aUpdatePisCommonPaymentPsuDataRequest request, PsuIdData psuData) {
        String authorisationId = request.getAuthorisationId();
        String paymentId = request.getPaymentId();

        SpiResponse<SpiAuthorizationCodeResult> authCodeResponse = paymentAuthorisationSpi.requestAuthorisationCode(contextData, chosenMethod.getAuthenticationMethodId(), payment, aspspConsentDataProvider);

        if (authCodeResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(authCodeResponse, ServiceType.PIS);
            log.warn("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}], Authorisation-ID [{}], PSU-ID [{}]. PIS_EMBEDDED_RECEIVED stage. Proceed single SCA embedded approach when performs authorisation has failed. Error msg: [{}]",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), paymentId, authorisationId, psuData.getPsuId(), errorHolder);
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, psuData);
        }

        Xs2aUpdatePisCommonPaymentPsuDataResponse response = new Xs2aUpdatePisCommonPaymentPsuDataResponse(SCAMETHODSELECTED, paymentId, authorisationId, psuData);
        response.setChosenScaMethod(spiToXs2aAuthenticationObjectMapper.mapToXs2aAuthenticationObject(chosenMethod));
        response.setChallengeData(mapToChallengeData(authCodeResponse.getPayload()));
        return response;
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse applyIdentification(Xs2aUpdatePisCommonPaymentPsuDataRequest request) {
        if (!isPsuExist(request.getPsuData())) {
            ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_400)
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_NO_PSU))
                                          .build();
            log.warn("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}], Authorisation-ID [{}]. PIS_EMBEDDED_RECEIVED stage. Apply identification when update payment PSU data has failed. No PSU data available in request.",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), request.getPaymentId(), request.getAuthorisationId());
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, request.getPaymentId(), request.getAuthorisationId(), request.getPsuData());
        }

        return new Xs2aUpdatePisCommonPaymentPsuDataResponse(PSUIDENTIFIED, request.getPaymentId(), request.getAuthorisationId(), request.getPsuData());
    }

    private ChallengeData mapToChallengeData(SpiAuthorizationCodeResult authorizationCodeResult) {
        if (authorizationCodeResult != null && !authorizationCodeResult.isEmpty()) {
            return authorizationCodeResult.getChallengeData();
        }
        return null;
    }

    private boolean isSingleScaMethod(List<SpiAuthenticationObject> spiScaMethods) {
        return spiScaMethods.size() == 1;
    }

    private boolean isMultipleScaMethods(List<SpiAuthenticationObject> spiScaMethods) {
        return spiScaMethods.size() > 1;
    }
}
