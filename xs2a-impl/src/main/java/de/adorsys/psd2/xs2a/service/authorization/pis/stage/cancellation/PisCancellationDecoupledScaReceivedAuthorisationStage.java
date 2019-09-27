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
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisCommonDecoupledService;
import de.adorsys.psd2.xs2a.service.authorization.pis.stage.PisScaStage;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.consent.CmsToXs2aPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.*;
import de.adorsys.psd2.xs2a.service.payment.Xs2aUpdatePaymentAfterSpiService;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiPsuAuthorisationResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentExecutionResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PaymentCancellationSpi;
import de.adorsys.psd2.xs2a.spi.service.PaymentSpi;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import static de.adorsys.psd2.xs2a.core.sca.ScaStatus.EXEMPTED;

@Slf4j
@Service("PIS_CANCELLATION_DECOUPLED_RECEIVED")
public class PisCancellationDecoupledScaReceivedAuthorisationStage extends PisScaStage<Xs2aUpdatePisCommonPaymentPsuDataRequest, GetPisAuthorisationResponse, Xs2aUpdatePisCommonPaymentPsuDataResponse> {
    private final PaymentCancellationSpi paymentCancellationSpi;
    private final PisCommonDecoupledService pisCommonDecoupledService;
    private final Xs2aUpdatePaymentAfterSpiService updatePaymentAfterSpiService;
    private final SpiContextDataProvider spiContextDataProvider;
    private final Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper;
    private final SpiErrorMapper spiErrorMapper;
    private final RequestProviderService requestProviderService;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;

    public PisCancellationDecoupledScaReceivedAuthorisationStage(CmsToXs2aPaymentMapper cmsToXs2aPaymentMapper, Xs2aToSpiPeriodicPaymentMapper xs2aToSpiPeriodicPaymentMapper, Xs2aToSpiSinglePaymentMapper xs2aToSpiSinglePaymentMapper, Xs2aToSpiBulkPaymentMapper xs2aToSpiBulkPaymentMapper, PisCommonPaymentServiceEncrypted pisCommonPaymentServiceEncrypted, ApplicationContext applicationContext, Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper, PaymentCancellationSpi paymentCancellationSpi, PisCommonDecoupledService pisCommonDecoupledService, Xs2aUpdatePaymentAfterSpiService updatePaymentAfterSpiService, SpiContextDataProvider spiContextDataProvider, Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper1, SpiErrorMapper spiErrorMapper, RequestProviderService requestProviderService, SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory) {
        super(cmsToXs2aPaymentMapper, xs2aToSpiPeriodicPaymentMapper, xs2aToSpiSinglePaymentMapper, xs2aToSpiBulkPaymentMapper, pisCommonPaymentServiceEncrypted, applicationContext, xs2aToSpiPsuDataMapper);
        this.paymentCancellationSpi = paymentCancellationSpi;
        this.pisCommonDecoupledService = pisCommonDecoupledService;
        this.updatePaymentAfterSpiService = updatePaymentAfterSpiService;
        this.spiContextDataProvider = spiContextDataProvider;
        this.xs2aToSpiPsuDataMapper = xs2aToSpiPsuDataMapper1;
        this.spiErrorMapper = spiErrorMapper;
        this.requestProviderService = requestProviderService;
        this.aspspConsentDataProviderFactory = aspspConsentDataProviderFactory;
    }

    @Override
    public Xs2aUpdatePisCommonPaymentPsuDataResponse apply(Xs2aUpdatePisCommonPaymentPsuDataRequest request, GetPisAuthorisationResponse pisAuthorisationResponse) {
        PsuIdData psuData = extractPsuIdData(request, true);
        request.setPsuData(psuData);
        PaymentType paymentType = pisAuthorisationResponse.getPaymentType();
        String paymentProduct = pisAuthorisationResponse.getPaymentProduct();
        SpiPayment payment = mapToSpiPayment(pisAuthorisationResponse, paymentType, paymentProduct);
        String authorisationId = request.getAuthorisationId();
        String paymentId = request.getPaymentId();

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(request.getPaymentId());

        SpiPsuData spiPsuData = xs2aToSpiPsuDataMapper.mapToSpiPsuData(psuData);
        SpiContextData contextData = spiContextDataProvider.provideWithPsuIdData(psuData);

        SpiResponse<SpiPsuAuthorisationResponse> authPsuResponse = paymentCancellationSpi.authorisePsu(contextData, spiPsuData, request.getPassword(), payment, spiAspspConsentDataProvider);

        if (authPsuResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(authPsuResponse, ServiceType.PIS);
            log.warn("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}], Authorisation-ID [{}], PSU-ID [{}]. PIS_CANCELLATION_DECOUPLED_RECEIVED stage. Authorise PSU has failed. Error msg: {}.",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), paymentId, authorisationId, psuData.getPsuId(), errorHolder);
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, psuData);
        }

        SpiPsuAuthorisationResponse psuAuthorisationResponse = authPsuResponse.getPayload();

        if (psuAuthorisationResponse.getSpiAuthorisationStatus() == SpiAuthorisationStatus.FAILURE) {
            ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_401)
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.PSU_CREDENTIALS_INVALID))
                                          .build();
            log.warn("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}], Authorisation-ID [{}], PSU-ID [{}]. PIS_CANCELLATION_DECOUPLED_RECEIVED stage. PSU authorisation failed due to incorrect credentials. Error msg: [{}].",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), paymentId, authorisationId, psuData.getPsuId(), errorHolder);
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, psuData);
        }

        if (psuAuthorisationResponse.isScaExempted() && paymentType != PaymentType.PERIODIC) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}], Authorisation-ID [{}], PSU-ID [{}]. PIS_CANCELLATION_DECOUPLED_RECEIVED stage. SCA was exempted for the payment after AuthorisationSpi#authorisePsu.",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), paymentId, authorisationId, psuData.getPsuId());
            return executePaymentWithoutSca(request, pisAuthorisationResponse, psuData, paymentType, payment, contextData, EXEMPTED);
        }


        return pisCommonDecoupledService.proceedDecoupledCancellation(request, payment);
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse executePaymentWithoutSca(Xs2aUpdatePisCommonPaymentPsuDataRequest request, GetPisAuthorisationResponse pisAuthorisationResponse, PsuIdData psuData, PaymentType paymentType, SpiPayment payment, SpiContextData contextData, ScaStatus resultScaStatus) {
        String authorisationId = request.getAuthorisationId();
        String paymentId = request.getPaymentId();

        final SpiAspspConsentDataProvider aspspConsentDataProvider =
            aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(request.getPaymentId());

        PaymentSpi paymentSpi = getPaymentService(pisAuthorisationResponse, paymentType);
        SpiResponse<SpiPaymentExecutionResponse> spiResponse = paymentSpi.executePaymentWithoutSca(contextData, payment, aspspConsentDataProvider);

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS);
            log.warn("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}], Authorisation-ID [{}], PSU-ID [{}]. PIS_CANCELLATION_DECOUPLED_RECEIVED stage. Execute payment without SCA has failed. Error msg: [{}]",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), paymentId, authorisationId, psuData.getPsuId(), errorHolder);
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, psuData);
        }

        TransactionStatus paymentStatus = spiResponse.getPayload().getTransactionStatus();
        updatePaymentAfterSpiService.updatePaymentStatus(request.getPaymentId(), paymentStatus);
        return new Xs2aUpdatePisCommonPaymentPsuDataResponse(resultScaStatus, paymentId, authorisationId, psuData);
    }
}

