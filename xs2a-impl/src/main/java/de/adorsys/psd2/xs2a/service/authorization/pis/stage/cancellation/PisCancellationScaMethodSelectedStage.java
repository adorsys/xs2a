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
import de.adorsys.psd2.consent.api.service.PisAuthorisationServiceEncrypted;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.authorization.pis.stage.PisScaStage;
import de.adorsys.psd2.xs2a.service.consent.PisAspspDataService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisCommonPaymentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.consent.CmsToXs2aPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aPisCommonPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.*;
import de.adorsys.psd2.xs2a.service.payment.Xs2aUpdatePaymentAfterSpiService;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PaymentCancellationSpi;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.sca.ScaStatus.FINALISED;

@Slf4j
@Service("PIS_CANCELLATION_EMBEDDED_SCAMETHODSELECTED")
public class PisCancellationScaMethodSelectedStage extends PisScaStage<Xs2aUpdatePisCommonPaymentPsuDataRequest, GetPisAuthorisationResponse, Xs2aUpdatePisCommonPaymentPsuDataResponse> {
    private final PaymentCancellationSpi paymentCancellationSpi;
    private final PisAspspDataService pisAspspDataService;
    private final Xs2aUpdatePaymentAfterSpiService updatePaymentAfterSpiService;
    private final SpiContextDataProvider spiContextDataProvider;
    private final SpiErrorMapper spiErrorMapper;
    private final Xs2aPisCommonPaymentMapper xs2aPisCommonPaymentMapper;
    private final RequestProviderService requestProviderService;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final Xs2aPisCommonPaymentService xs2aPisCommonPaymentService;

    public PisCancellationScaMethodSelectedStage(CmsToXs2aPaymentMapper cmsToXs2aPaymentMapper, Xs2aToSpiPeriodicPaymentMapper xs2aToSpiPeriodicPaymentMapper, Xs2aToSpiSinglePaymentMapper xs2aToSpiSinglePaymentMapper, Xs2aToSpiBulkPaymentMapper xs2aToSpiBulkPaymentMapper, PisAuthorisationServiceEncrypted pisAuthorisationServiceEncrypted, ApplicationContext applicationContext, Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper, PaymentCancellationSpi paymentCancellationSpi, PisAspspDataService pisAspspDataService, Xs2aUpdatePaymentAfterSpiService updatePaymentAfterSpiService, SpiContextDataProvider spiContextDataProvider, SpiErrorMapper spiErrorMapper, Xs2aPisCommonPaymentMapper xs2aPisCommonPaymentMapper, RequestProviderService requestProviderService, SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory, Xs2aPisCommonPaymentService xs2aPisCommonPaymentService) {
        super(cmsToXs2aPaymentMapper, xs2aToSpiPeriodicPaymentMapper, xs2aToSpiSinglePaymentMapper, xs2aToSpiBulkPaymentMapper, pisAuthorisationServiceEncrypted, applicationContext, xs2aToSpiPsuDataMapper);
        this.paymentCancellationSpi = paymentCancellationSpi;
        this.pisAspspDataService = pisAspspDataService;
        this.updatePaymentAfterSpiService = updatePaymentAfterSpiService;
        this.spiContextDataProvider = spiContextDataProvider;
        this.spiErrorMapper = spiErrorMapper;
        this.xs2aPisCommonPaymentMapper = xs2aPisCommonPaymentMapper;
        this.requestProviderService = requestProviderService;
        this.aspspConsentDataProviderFactory = aspspConsentDataProviderFactory;
        this.xs2aPisCommonPaymentService = xs2aPisCommonPaymentService;
    }

    @Override
    public Xs2aUpdatePisCommonPaymentPsuDataResponse apply(Xs2aUpdatePisCommonPaymentPsuDataRequest request, GetPisAuthorisationResponse pisAuthorisationResponse) {
        PaymentType paymentType = pisAuthorisationResponse.getPaymentType();
        String paymentProduct = pisAuthorisationResponse.getPaymentProduct();
        SpiPayment payment = mapToSpiPayment(pisAuthorisationResponse, paymentType, paymentProduct);
        PsuIdData psuData = extractPsuIdData(request, true);
        String authorisationId = request.getAuthorisationId();
        String paymentId = request.getPaymentId();

        SpiAspspConsentDataProvider spiAspspConsentDataProvider = aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(request.getPaymentId());

        String internalId = pisAspspDataService.getInternalPaymentIdByEncryptedString(request.getPaymentId());
        SpiScaConfirmation spiScaConfirmation = xs2aPisCommonPaymentMapper.buildSpiScaConfirmation(request, pisAuthorisationResponse.getPaymentId(), internalId, psuData);

        SpiResponse<SpiResponse.VoidResponse> spiResponse = paymentCancellationSpi.verifyScaAuthorisationAndCancelPayment(spiContextDataProvider.provideWithPsuIdData(psuData), spiScaConfirmation, payment, spiAspspConsentDataProvider);

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS);
            log.warn("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}], Authorisation-ID [{}]. PIS_CANCELLATION_EMBEDDED_SCAMETHODSELECTED stage. Verify SCA authorisation and cancel payment has failed. Error msg: [{}]",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), paymentId, authorisationId, errorHolder);
            Optional<MessageErrorCode> messageInformation = errorHolder.getFirstErrorCode();
            if (messageInformation.isPresent() && messageInformation.get() == MessageErrorCode.PSU_CREDENTIALS_INVALID) {
                xs2aPisCommonPaymentService.updatePisAuthorisationStatus(authorisationId, ScaStatus.FAILED);
            }
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS), request.getPaymentId(), request.getAuthorisationId(), psuData);
        }

        updatePaymentAfterSpiService.updatePaymentStatus(request.getPaymentId(), TransactionStatus.CANC);

        return new Xs2aUpdatePisCommonPaymentPsuDataResponse(FINALISED, request.getPaymentId(), request.getAuthorisationId(), psuData);
    }
}
