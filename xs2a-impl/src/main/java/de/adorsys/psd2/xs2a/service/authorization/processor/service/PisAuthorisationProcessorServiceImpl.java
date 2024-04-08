/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.service.authorization.processor.service;

import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.pis.Xs2aCurrencyConversionInfo;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.authorisation.CommonAuthorisationParameters;
import de.adorsys.psd2.xs2a.domain.consent.CreatePaymentAuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreateAuthorisationRequest;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.pis.PaymentAuthorisationParameters;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisCommonDecoupledService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisExecutePaymentService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationServiceResolver;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.consent.PisAspspDataService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisCommonPaymentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aPisCommonPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.*;
import de.adorsys.psd2.xs2a.service.payment.Xs2aUpdatePaymentAfterSpiService;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.*;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentExecutionResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.sca.SpiScaApproach;
import de.adorsys.psd2.xs2a.spi.domain.sca.SpiScaStatus;
import de.adorsys.psd2.xs2a.spi.service.CurrencyConversionInfoSpi;
import de.adorsys.psd2.xs2a.spi.service.PaymentAuthorisationSpi;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.adorsys.psd2.xs2a.core.error.ErrorType.PIS_400;

@Service
@Slf4j
public class PisAuthorisationProcessorServiceImpl extends PaymentBaseAuthorisationProcessorService {
    private static final String AUTHORISATION_FOR_PAYMENT_HAS_FAILED = "Start authorisation for payment has failed.";

    private final PisExecutePaymentService pisExecutePaymentService;
    private final SpiErrorMapper spiErrorMapper;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final Xs2aUpdatePaymentAfterSpiService updatePaymentAfterSpiService;
    private final Xs2aPisCommonPaymentService xs2aPisCommonPaymentService;
    private final PaymentAuthorisationSpi paymentAuthorisationSpi;
    private final PisCommonDecoupledService pisCommonDecoupledService;
    private final SpiContextDataProvider spiContextDataProvider;
    private final CurrencyConversionInfoSpi currencyConversionInfoSpi;
    private final SpiToXs2aCurrencyConversionInfoMapper spiToXs2aCurrencyConversionInfoMapper;
    private final SpiToXs2aTransactionStatusMapper spiToXs2aTransactionStatusMapper;

    public PisAuthorisationProcessorServiceImpl(List<PisScaAuthorisationService> services,
                                                Xs2aToSpiPaymentMapper xs2aToSpiPaymentMapper, PisExecutePaymentService pisExecutePaymentService,
                                                PisAspspDataService pisAspspDataService, Xs2aPisCommonPaymentMapper xs2aPisCommonPaymentMapper,
                                                SpiContextDataProvider spiContextDataProvider, SpiErrorMapper spiErrorMapper,
                                                SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory,
                                                Xs2aUpdatePaymentAfterSpiService updatePaymentAfterSpiService,
                                                Xs2aAuthorisationService xs2aAuthorisationService,
                                                Xs2aPisCommonPaymentService xs2aPisCommonPaymentService,
                                                PaymentAuthorisationSpi paymentAuthorisationSpi,
                                                PisCommonDecoupledService pisCommonDecoupledService,
                                                Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper,
                                                CurrencyConversionInfoSpi currencyConversionInfoSpi,
                                                SpiToXs2aCurrencyConversionInfoMapper spiToXs2aCurrencyConversionInfoMapper,
                                                SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper,
                                                SpiToXs2aChallengeDataMapper spiToXs2aChallengeDataMapper,
                                                SpiToXs2aScaStatusMapper spiToXs2aScaStatusMapper,
                                                SpiToXs2aScaApproachMapper spiToXs2aScaApproachMapper,
                                                SpiToXs2aTppMessageInformationMapper spiToXs2aTppMessageInformationMapper,
                                                SpiToXs2aPaymentTypeMapper spiToXs2aPaymentTypeMapper,
                                                SpiToXs2aTransactionStatusMapper spiToXs2aTransactionStatusMapper,
                                                PisScaAuthorisationServiceResolver pisScaAuthorisationServiceResolver) {
        super(services, xs2aAuthorisationService, xs2aPisCommonPaymentService, xs2aToSpiPaymentMapper,
              spiContextDataProvider, aspspConsentDataProviderFactory, spiErrorMapper,
              pisAspspDataService, xs2aPisCommonPaymentMapper, xs2aToSpiPsuDataMapper,
              spiToXs2aAuthenticationObjectMapper, spiToXs2aChallengeDataMapper, spiToXs2aScaStatusMapper,
              spiToXs2aScaApproachMapper, spiToXs2aTppMessageInformationMapper, spiToXs2aPaymentTypeMapper, pisScaAuthorisationServiceResolver);
        this.pisExecutePaymentService = pisExecutePaymentService;
        this.spiErrorMapper = spiErrorMapper;
        this.aspspConsentDataProviderFactory = aspspConsentDataProviderFactory;
        this.updatePaymentAfterSpiService = updatePaymentAfterSpiService;
        this.xs2aPisCommonPaymentService = xs2aPisCommonPaymentService;
        this.paymentAuthorisationSpi = paymentAuthorisationSpi;
        this.pisCommonDecoupledService = pisCommonDecoupledService;
        this.spiContextDataProvider = spiContextDataProvider;
        this.currencyConversionInfoSpi = currencyConversionInfoSpi;
        this.spiToXs2aCurrencyConversionInfoMapper = spiToXs2aCurrencyConversionInfoMapper;
        this.spiToXs2aTransactionStatusMapper = spiToXs2aTransactionStatusMapper;
    }

    @Override
    public AuthorisationProcessorResponse proceedAuthorisation(Xs2aCreateAuthorisationRequest xs2aCreateAuthorisationRequest,
                                                               PisScaAuthorisationService pisScaAuthorisationService,
                                                               PaymentType paymentType,
                                                               SpiStartAuthorisationResponse startAuthorisationResponse) {
        PsuIdData psuIdData = xs2aCreateAuthorisationRequest.getPsuData();
        String paymentId = xs2aCreateAuthorisationRequest.getPaymentId();
        ScaApproach scaApproach = xs2aCreateAuthorisationRequest.getScaApproach();

        Optional<Xs2aCreatePisAuthorisationResponse> commonPaymentAuthorisationResponse =
            pisScaAuthorisationService.createCommonPaymentAuthorisation(xs2aCreateAuthorisationRequest, paymentType);

        if (commonPaymentAuthorisationResponse.isEmpty()) {
            ErrorHolder errorHolder = ErrorHolder.builder(PIS_400)
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.PAYMENT_FAILED))
                                          .build();
            writeErrorLog(xs2aCreateAuthorisationRequest, errorHolder, AUTHORISATION_FOR_PAYMENT_HAS_FAILED);
            return new CreatePaymentAuthorisationProcessorResponse(errorHolder, scaApproach, paymentId, psuIdData);
        }

        Xs2aCreatePisAuthorisationResponse createPaymentAuthorisationResponse = commonPaymentAuthorisationResponse.get();

        Set<TppMessageInformation> tppMessages = spiToXs2aTppMessageInformationMapper.toTppMessageInformationSet(startAuthorisationResponse.getTppMessages());
        String psuMessage = startAuthorisationResponse.getPsuMessage();

        return new CreatePaymentAuthorisationProcessorResponse(createPaymentAuthorisationResponse.getScaStatus(),
                                                               createPaymentAuthorisationResponse.getScaApproach(),
                                                               psuMessage, tppMessages,
                                                               paymentId, xs2aCreateAuthorisationRequest.getAuthorisationId(),
                                                               createPaymentAuthorisationResponse.getInternalRequestId(),
                                                               psuIdData);
    }

    @Override
    public void updateAuthorisation(AuthorisationProcessorRequest request, AuthorisationProcessorResponse response) {
        PisScaAuthorisationService authorizationService = getService(request.getScaApproach());
        authorizationService.updateAuthorisation(request.getUpdateAuthorisationRequest(), response);
    }

    @Override
    public AuthorisationProcessorResponse doScaReceived(AuthorisationProcessorRequest authorisationProcessorRequest) {
        PaymentAuthorisationParameters request = (PaymentAuthorisationParameters) authorisationProcessorRequest.getUpdateAuthorisationRequest();
        return request.isUpdatePsuIdentification()
                   ? applyIdentification(authorisationProcessorRequest)
                   : applyAuthorisation(authorisationProcessorRequest);
    }

    @Override
    SpiResponse<SpiPaymentExecutionResponse> verifyScaAuthorisationAndExecutePayment(Authorisation authorisation,
                                                                                     SpiPayment payment, SpiScaConfirmation spiScaConfirmation,
                                                                                     SpiContextData contextData, SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return pisExecutePaymentService.verifyScaAuthorisationAndExecutePaymentWithPaymentResponse(contextData,
                                                                                                   spiScaConfirmation,
                                                                                                   payment,
                                                                                                   spiAspspConsentDataProvider);
    }

    @Override
    protected SpiResponse<SpiStartAuthorisationResponse> getSpiStartAuthorisationResponse(SpiContextData spiContextData,
                                                                                          ScaApproach scaApproach,
                                                                                          ScaStatus scaStatus,
                                                                                          String authorisationId,
                                                                                          SpiPayment spiPayment,
                                                                                          SpiAspspConsentDataProvider spiAspspDataProviderFor) {
        return paymentAuthorisationSpi.startAuthorisation(spiContextData, SpiScaApproach.valueOf(scaApproach.name()), SpiScaStatus.valueOf(scaStatus.name()), authorisationId, spiPayment, spiAspspDataProviderFor);
    }

    @Override
    void updatePaymentDataByPaymentResponse(String paymentId, SpiResponse<SpiPaymentExecutionResponse> spiResponse) {
        SpiPaymentExecutionResponse payload = spiResponse.getPayload();
        TransactionStatus paymentStatus = spiToXs2aTransactionStatusMapper.mapToXs2aTransactionStatus(payload.getTransactionStatus());

        if (paymentStatus == TransactionStatus.PATC) {
            xs2aPisCommonPaymentService.updateMultilevelSca(paymentId, true);
        }

        updatePaymentAfterSpiService.updatePaymentStatus(paymentId, paymentStatus);
    }

    @Override
    public AuthorisationProcessorResponse doScaExempted(AuthorisationProcessorRequest authorisationProcessorRequest) {
        CommonAuthorisationParameters request = authorisationProcessorRequest.getUpdateAuthorisationRequest();

        Authorisation authorisation = authorisationProcessorRequest.getAuthorisation();
        PsuIdData psuData = extractPsuIdData(request, authorisation);
        SpiPayment payment = getSpiPayment(request.getBusinessObjectId());
        SpiContextData contextData = spiContextDataProvider.provideWithPsuIdData(psuData);
        SpiAspspConsentDataProvider aspspConsentDataProvider = aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(request.getBusinessObjectId());

        Xs2aCurrencyConversionInfo xs2aCurrencyConversionInfo = getCurrencyConversionInfo(contextData, payment,
                                                                                          request.getAuthorisationId(), aspspConsentDataProvider);


        return new Xs2aUpdatePisCommonPaymentPsuDataResponse(ScaStatus.EXEMPTED, request.getBusinessObjectId(),
                                                             request.getAuthorisationId(), request.getPsuData(),
                                                             xs2aCurrencyConversionInfo);
    }

    @Override
    boolean needProcessExemptedSca(PaymentType paymentType, boolean isScaExempted) {
        return isScaExempted && paymentType != PaymentType.PERIODIC;
    }

    @Override
    protected Xs2aCurrencyConversionInfo getCurrencyConversionInfo(SpiContextData contextData, SpiPayment payment,
                                                                   String authorisationId, SpiAspspConsentDataProvider aspspConsentDataProvider) {
        SpiResponse<SpiCurrencyConversionInfo> conversionInfoSpiResponse =
            currencyConversionInfoSpi.getCurrencyConversionInfo(contextData, payment, authorisationId, aspspConsentDataProvider);

        SpiCurrencyConversionInfo spiCurrencyConversionInfo = conversionInfoSpiResponse.getPayload();

        return spiToXs2aCurrencyConversionInfoMapper.toXs2aCurrencyConversionInfo(spiCurrencyConversionInfo);
    }

    @Override
    SpiResponse<SpiAvailableScaMethodsResponse> requestAvailableScaMethods(SpiPayment payment, SpiAspspConsentDataProvider aspspConsentDataProvider, SpiContextData contextData) {
        return paymentAuthorisationSpi.requestAvailableScaMethods(contextData, payment, aspspConsentDataProvider);
    }

    @Override
    SpiResponse<SpiPsuAuthorisationResponse> authorisePsu(PaymentAuthorisationParameters request, SpiPayment payment,
                                                          SpiAspspConsentDataProvider aspspConsentDataProvider, SpiPsuData spiPsuData,
                                                          SpiContextData contextData, String authorisationId) {
        return paymentAuthorisationSpi.authorisePsu(contextData, authorisationId, spiPsuData, request.getPassword(), payment, aspspConsentDataProvider);
    }

    @Override
    SpiResponse<SpiAuthorizationCodeResult> requestAuthorisationCode(SpiPayment payment, String authenticationMethodId,
                                                                     SpiContextData spiContextData, SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return paymentAuthorisationSpi.requestAuthorisationCode(spiContextData, authenticationMethodId, payment, spiAspspConsentDataProvider);
    }

    @Override
    Xs2aUpdatePisCommonPaymentPsuDataResponse proceedDecoupledApproach(PaymentAuthorisationParameters request, SpiPayment payment, String authenticationMethodId) {
        return pisCommonDecoupledService.proceedDecoupledInitiation(request, payment, authenticationMethodId);
    }

    @Override
    Xs2aUpdatePisCommonPaymentPsuDataResponse executePaymentWithoutSca(AuthorisationProcessorRequest authorisationProcessorRequest, PsuIdData psuData, PaymentType paymentType, SpiPayment payment, SpiContextData contextData, ScaStatus resultScaStatus,
                                                                       Xs2aCurrencyConversionInfo xs2aCurrencyConversionInfo) {
        PaymentAuthorisationParameters request = (PaymentAuthorisationParameters) authorisationProcessorRequest.getUpdateAuthorisationRequest();
        String authorisationId = request.getAuthorisationId();
        String paymentId = request.getPaymentId();

        final SpiAspspConsentDataProvider aspspConsentDataProvider = aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(paymentId);

        SpiResponse<SpiPaymentExecutionResponse> spiResponse = pisExecutePaymentService.executePaymentWithoutSca(contextData, payment, aspspConsentDataProvider);

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS);
            writeErrorLog(authorisationProcessorRequest, psuData, errorHolder, "Execute payment without SCA has failed.");
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, psuData);
        }

        TransactionStatus paymentStatus = spiToXs2aTransactionStatusMapper.mapToXs2aTransactionStatus(spiResponse.getPayload().getTransactionStatus());

        if (paymentStatus == TransactionStatus.PATC) {
            xs2aPisCommonPaymentService.updateMultilevelSca(paymentId, true);
        }

        updatePaymentAfterSpiService.updatePaymentStatus(paymentId, paymentStatus);

        return new Xs2aUpdatePisCommonPaymentPsuDataResponse(resultScaStatus, paymentId, authorisationId, psuData, xs2aCurrencyConversionInfo);
    }
}
