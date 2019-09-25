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
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
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
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PaymentAuthorisationSpi;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import static de.adorsys.psd2.xs2a.core.sca.ScaStatus.PSUIDENTIFIED;

@Slf4j
@Service("PIS_DECOUPLED_RECEIVED")
public class PisDecoupledScaReceivedAuthorisationStage extends PisScaStage<Xs2aUpdatePisCommonPaymentPsuDataRequest, GetPisAuthorisationResponse, Xs2aUpdatePisCommonPaymentPsuDataResponse> {
    private final PaymentAuthorisationSpi paymentAuthorisationSpi;
    private final RequestProviderService requestProviderService;
    private final PisCommonDecoupledService pisCommonDecoupledService;
    private final SpiContextDataProvider spiContextDataProvider;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper;
    private final SpiErrorMapper spiErrorMapper;

    public PisDecoupledScaReceivedAuthorisationStage(CmsToXs2aPaymentMapper cmsToXs2aPaymentMapper, Xs2aToSpiPeriodicPaymentMapper xs2aToSpiPeriodicPaymentMapper, Xs2aToSpiSinglePaymentMapper xs2aToSpiSinglePaymentMapper, Xs2aToSpiBulkPaymentMapper xs2aToSpiBulkPaymentMapper, PisCommonPaymentServiceEncrypted pisCommonPaymentServiceEncrypted, ApplicationContext applicationContext, PaymentAuthorisationSpi paymentAuthorisationSpi, RequestProviderService requestProviderService, PisCommonDecoupledService pisCommonDecoupledService, SpiContextDataProvider spiContextDataProvider, SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory, Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper, SpiErrorMapper spiErrorMapper) {
        super(cmsToXs2aPaymentMapper, xs2aToSpiPeriodicPaymentMapper, xs2aToSpiSinglePaymentMapper, xs2aToSpiBulkPaymentMapper, pisCommonPaymentServiceEncrypted, applicationContext, xs2aToSpiPsuDataMapper);
        this.paymentAuthorisationSpi = paymentAuthorisationSpi;
        this.pisCommonDecoupledService = pisCommonDecoupledService;
        this.spiContextDataProvider = spiContextDataProvider;
        this.aspspConsentDataProviderFactory = aspspConsentDataProviderFactory;
        this.xs2aToSpiPsuDataMapper = xs2aToSpiPsuDataMapper;
        this.spiErrorMapper = spiErrorMapper;
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
        request.setPsuData(psuData);
        PaymentType paymentType = pisAuthorisationResponse.getPaymentType();
        String paymentProduct = pisAuthorisationResponse.getPaymentProduct();
        SpiPayment payment = mapToSpiPayment(pisAuthorisationResponse, paymentType, paymentProduct);
        String paymentId = request.getPaymentId();

        SpiAspspConsentDataProvider aspspConsentDataProvider = aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(paymentId);

        SpiPsuData spiPsuData = xs2aToSpiPsuDataMapper.mapToSpiPsuData(psuData);
        SpiContextData contextData = spiContextDataProvider.provideWithPsuIdData(psuData);

        SpiResponse<SpiAuthorisationStatus> authPsuResponse = paymentAuthorisationSpi.authorisePsu(contextData, spiPsuData, request.getPassword(), payment, aspspConsentDataProvider);

        String authenticationId = request.getAuthorisationId();
        if (authPsuResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(authPsuResponse, ServiceType.PIS);
            log.warn("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}], Authorisation-ID [{}], PSU-ID [{}]. PIS_DECOUPLED_RECEIVED stage. Authorise PSU when apply payment authorisation has failed. Error msg: [{}].",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), paymentId, authenticationId, psuData.getPsuId(), errorHolder);
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authenticationId, psuData);
        }

        return pisCommonDecoupledService.proceedDecoupledInitiation(request, payment);
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse applyIdentification(Xs2aUpdatePisCommonPaymentPsuDataRequest request) {
        if (!isPsuExist(request.getPsuData())) {
            ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_400)
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_NO_PSU))
                                          .build();
            log.warn("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}], Authorisation-ID [{}], PSU-ID [{}]. PIS_DECOUPLED_RECEIVED stage. Apply identification when update payment PSU data has failed. No PSU data available in request.",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), request.getPaymentId(), request.getAuthorisationId(), request.getPsuData().getPsuId());
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, request.getPaymentId(), request.getAuthorisationId(), request.getPsuData());
        }

        return new Xs2aUpdatePisCommonPaymentPsuDataResponse(PSUIDENTIFIED, request.getPaymentId(), request.getAuthorisationId(), request.getPsuData());
    }
}
