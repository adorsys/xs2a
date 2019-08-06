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

package de.adorsys.psd2.xs2a.service.authorization.pis.stage.cancellation;

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
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisCommonDecoupledService;
import de.adorsys.psd2.xs2a.service.authorization.pis.stage.PisScaStage;
import de.adorsys.psd2.xs2a.service.consent.PisPsuDataService;
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
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PaymentCancellationSpi;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;

import static de.adorsys.psd2.xs2a.core.sca.ScaStatus.*;

@Slf4j
@Service("PIS_CANCELLATION_EMBEDDED_RECEIVED")
public class PisCancellationScaReceivedAuthorisationStage extends PisScaStage<Xs2aUpdatePisCommonPaymentPsuDataRequest, GetPisAuthorisationResponse, Xs2aUpdatePisCommonPaymentPsuDataResponse> {
    private final PaymentCancellationSpi paymentCancellationSpi;
    private final Xs2aPisCommonPaymentService xs2aPisCommonPaymentService;
    private final PisPsuDataService pisPsuDataService;
    private final PisCommonDecoupledService pisCommonDecoupledService;
    private final Xs2aUpdatePaymentAfterSpiService updatePaymentAfterSpiService;
    private final SpiContextDataProvider spiContextDataProvider;
    private final ScaApproachResolver scaApproachResolver;
    private final SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper;
    private final SpiErrorMapper spiErrorMapper;
    private final Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper;
    private final RequestProviderService requestProviderService;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;

    private static final String MESSAGE_ERROR_NO_PSU = "Please provide the PSU identification data";

    public PisCancellationScaReceivedAuthorisationStage(CmsToXs2aPaymentMapper cmsToXs2aPaymentMapper, Xs2aToSpiPeriodicPaymentMapper xs2aToSpiPeriodicPaymentMapper, Xs2aToSpiSinglePaymentMapper xs2aToSpiSinglePaymentMapper, Xs2aToSpiBulkPaymentMapper xs2aToSpiBulkPaymentMapper, PisCommonPaymentServiceEncrypted pisCommonPaymentServiceEncrypted, ApplicationContext applicationContext, Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper, PaymentCancellationSpi paymentCancellationSpi, Xs2aPisCommonPaymentService xs2aPisCommonPaymentService, PisPsuDataService pisPsuDataService, PisCommonDecoupledService pisCommonDecoupledService, Xs2aUpdatePaymentAfterSpiService updatePaymentAfterSpiService, SpiContextDataProvider spiContextDataProvider, ScaApproachResolver scaApproachResolver, SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper, SpiErrorMapper spiErrorMapper, RequestProviderService requestProviderService, SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory) {
        super(cmsToXs2aPaymentMapper, xs2aToSpiPeriodicPaymentMapper, xs2aToSpiSinglePaymentMapper, xs2aToSpiBulkPaymentMapper, pisCommonPaymentServiceEncrypted, applicationContext, xs2aToSpiPsuDataMapper);
        this.paymentCancellationSpi = paymentCancellationSpi;
        this.xs2aPisCommonPaymentService = xs2aPisCommonPaymentService;
        this.pisPsuDataService = pisPsuDataService;
        this.pisCommonDecoupledService = pisCommonDecoupledService;
        this.updatePaymentAfterSpiService = updatePaymentAfterSpiService;
        this.spiContextDataProvider = spiContextDataProvider;
        this.scaApproachResolver = scaApproachResolver;
        this.spiToXs2aAuthenticationObjectMapper = spiToXs2aAuthenticationObjectMapper;
        this.spiErrorMapper = spiErrorMapper;
        this.xs2aToSpiPsuDataMapper = xs2aToSpiPsuDataMapper;
        this.requestProviderService = requestProviderService;
        this.aspspConsentDataProviderFactory = aspspConsentDataProviderFactory;
    }

    @Override
    public Xs2aUpdatePisCommonPaymentPsuDataResponse apply(Xs2aUpdatePisCommonPaymentPsuDataRequest request, GetPisAuthorisationResponse pisAuthorisationResponse) {
        return request.isUpdatePsuIdentification()
                   ? applyIdentification(request)
                   : applyAuthorisation(request, pisAuthorisationResponse);
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse applyIdentification(Xs2aUpdatePisCommonPaymentPsuDataRequest request) {
        if (!isPsuExist(request.getPsuData())) {
            ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_400)
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR, MESSAGE_ERROR_NO_PSU))
                                          .build();
            log.warn("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}], Authorisation-ID [{}]. PIS_CANCELLATION_EMBEDDED_RECEIVED stage. Apply identification when update payment PSU data has failed. No PSU data available in request.",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), request.getPaymentId(), request.getAuthorisationId());
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, request.getPaymentId(), request.getAuthorisationId(), request.getPsuData());
        }

        if (!isPsuDataCorrect(request.getPaymentId(), request.getPsuData())) {
            log.warn("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}], Authorisation-ID [{}], PSU-ID [{}]. PIS_CANCELLATION_EMBEDDED_RECEIVED stage. Apply Identification when update payment PSU data has failed. PSU credentials invalid.",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), request.getPaymentId(), request.getAuthorisationId(), request.getPsuData().getPsuId());
            ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_401)
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.UNAUTHORIZED, MESSAGE_ERROR_NO_PSU))
                                          .build();
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, request.getPaymentId(), request.getAuthorisationId(), request.getPsuData());
        }

        return new Xs2aUpdatePisCommonPaymentPsuDataResponse(PSUIDENTIFIED, request.getPaymentId(), request.getAuthorisationId(), request.getPsuData());
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse applyAuthorisation(Xs2aUpdatePisCommonPaymentPsuDataRequest request, GetPisAuthorisationResponse pisAuthorisationResponse) {
        PsuIdData psuData = extractPsuIdData(request, true);
        String authorisationId = request.getAuthorisationId();
        String paymentId = request.getPaymentId();
        PaymentType paymentType = pisAuthorisationResponse.getPaymentType();
        String paymentProduct = pisAuthorisationResponse.getPaymentProduct();
        SpiPayment payment = mapToSpiPayment(pisAuthorisationResponse, paymentType, paymentProduct);

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(request.getPaymentId());

        SpiContextData spiContextData = spiContextDataProvider.provideWithPsuIdData(psuData);
        SpiPsuData spiPsuData = xs2aToSpiPsuDataMapper.mapToSpiPsuData(psuData);

        SpiResponse<SpiAuthorisationStatus> authPsuResponse = paymentCancellationSpi.authorisePsu(spiContextData, spiPsuData, request.getPassword(), payment, spiAspspConsentDataProvider);

        if (authPsuResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(authPsuResponse, ServiceType.PIS);
            log.warn("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}], Authorisation-ID [{}], PSU-ID [{}]. PIS_CANCELLATION_EMBEDDED_RECEIVED stage. Authorise PSU when apply authorisation has failed. Error msg: [{}]",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), paymentId, authorisationId, psuData.getPsuId(), errorHolder);
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, request.getPaymentId(), request.getAuthorisationId(), psuData);
        }

        SpiResponse<List<SpiAuthenticationObject>> availableScaMethodsResponse = paymentCancellationSpi.requestAvailableScaMethods(spiContextData, payment, spiAspspConsentDataProvider);

        if (availableScaMethodsResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(availableScaMethodsResponse, ServiceType.PIS);
            log.warn("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}], Authorisation-ID [{}], PSU-ID [{}]. PIS_CANCELLATION_EMBEDDED_RECEIVED stage. Request available SCA methods has failed. Error msg: [{}]",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), paymentId, authorisationId, psuData.getPsuId(), errorHolder);
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, request.getPaymentId(), request.getAuthorisationId(), psuData);
        }

        List<SpiAuthenticationObject> spiScaMethods = availableScaMethodsResponse.getPayload();

        if (CollectionUtils.isEmpty(spiScaMethods)) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}], Authorisation-ID [{}], PSU-ID [{}]. PIS_CANCELLATION_EMBEDDED_RECEIVED stage. Available SCA methods is empty.",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), paymentId, authorisationId, psuData.getPsuId());

            SpiResponse<SpiResponse.VoidResponse> executePaymentResponse = paymentCancellationSpi.cancelPaymentWithoutSca(spiContextData, payment, spiAspspConsentDataProvider);

            if (executePaymentResponse.hasError()) {
                ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(executePaymentResponse, ServiceType.PIS);
                log.warn("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}], Authorisation-ID [{}], PSU-ID [{}]. PIS_CANCELLATION_EMBEDDED_RECEIVED stage. Cancel payment without SCA has failed. Error msg: [{}]",
                         requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), paymentId, authorisationId, psuData.getPsuId(), errorHolder);
                return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, request.getPaymentId(), request.getAuthorisationId(), psuData);
            }

            updatePaymentAfterSpiService.updatePaymentStatus(request.getPaymentId(), TransactionStatus.CANC);

            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(FINALISED, request.getPaymentId(), request.getAuthorisationId(), psuData);

        } else if (isSingleScaMethod(spiScaMethods)) {
            xs2aPisCommonPaymentService.saveAuthenticationMethods(request.getAuthorisationId(), spiToXs2aAuthenticationObjectMapper.mapToXs2aListAuthenticationObject(spiScaMethods));
            SpiAuthenticationObject chosenMethod = spiScaMethods.get(0);

            if (chosenMethod.isDecoupled()) {
                scaApproachResolver.forceDecoupledScaApproach();
                xs2aPisCommonPaymentService.updateScaApproach(request.getAuthorisationId(), ScaApproach.DECOUPLED);
                return pisCommonDecoupledService.proceedDecoupledCancellation(request, payment, chosenMethod.getAuthenticationMethodId());
            }

            return proceedSingleScaEmbeddedApproach(payment, chosenMethod, spiContextData, spiAspspConsentDataProvider, request, psuData);

        } else if (isMultipleScaMethods(spiScaMethods)) {
            xs2aPisCommonPaymentService.saveAuthenticationMethods(request.getAuthorisationId(), spiToXs2aAuthenticationObjectMapper.mapToXs2aListAuthenticationObject(spiScaMethods));
            Xs2aUpdatePisCommonPaymentPsuDataResponse response = new Xs2aUpdatePisCommonPaymentPsuDataResponse(PSUAUTHENTICATED, request.getPaymentId(), request.getAuthorisationId(), psuData);
            response.setAvailableScaMethods(spiToXs2aAuthenticationObjectMapper.mapToXs2aListAuthenticationObject(spiScaMethods));
            return response;
        }

        log.info("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}], Authorisation-ID [{}]. PIS_CANCELLATION_EMBEDDED_RECEIVED stage. Apply authorisation when update payment PSU data set SCA status failed.",
                 requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), paymentId, authorisationId);
        return new Xs2aUpdatePisCommonPaymentPsuDataResponse(FAILED, request.getPaymentId(), request.getAuthorisationId(), psuData);
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse proceedSingleScaEmbeddedApproach(SpiPayment payment,
                                                                                       SpiAuthenticationObject chosenMethod,
                                                                                       SpiContextData contextData,
                                                                                       SpiAspspConsentDataProvider spiAspspConsentDataProvider,
                                                                                       Xs2aUpdatePisCommonPaymentPsuDataRequest request,
                                                                                       PsuIdData psuData) {
        String authorisationId = request.getAuthorisationId();
        String paymentId = request.getPaymentId();

        SpiResponse<SpiAuthorizationCodeResult> authCodeResponse = paymentCancellationSpi.requestAuthorisationCode(contextData, chosenMethod.getAuthenticationMethodId(), payment, spiAspspConsentDataProvider);

        if (authCodeResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(authCodeResponse, ServiceType.PIS);
            log.warn("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}], Authorisation-ID [{}], PSU-ID [{}]. PIS_CANCELLATION_EMBEDDED_RECEIVED stage. Proceed single SCA embedded approach when performs authorisation has failed. Error msg: [{}]",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), paymentId, authorisationId, psuData.getPsuId(), errorHolder);
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, request.getPaymentId(), request.getAuthorisationId(), psuData);
        }

        ChallengeData challengeData = mapToChallengeData(authCodeResponse.getPayload());

        Xs2aUpdatePisCommonPaymentPsuDataResponse response = new Xs2aUpdatePisCommonPaymentPsuDataResponse(SCAMETHODSELECTED, request.getPaymentId(), request.getAuthorisationId(), psuData);
        response.setChosenScaMethod(spiToXs2aAuthenticationObjectMapper.mapToXs2aAuthenticationObject(chosenMethod));
        response.setChallengeData(challengeData);
        return response;
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

    private boolean isPsuDataCorrect(String paymentId, PsuIdData psuData) {
        List<PsuIdData> psuIdDataList = pisPsuDataService.getPsuDataByPaymentId(paymentId);

        return psuIdDataList.stream()
                   .anyMatch(psu -> psu.contentEquals(psuData));
    }
}
