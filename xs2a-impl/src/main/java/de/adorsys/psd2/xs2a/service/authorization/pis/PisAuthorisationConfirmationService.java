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

package de.adorsys.psd2.xs2a.service.authorization.pis;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.service.AuthorisationServiceEncrypted;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aPisCommonPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aCurrencyConversionInfoMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPaymentMapper;
import de.adorsys.psd2.xs2a.service.payment.Xs2aUpdatePaymentAfterSpiService;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiCheckConfirmationCodeRequest;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiCurrencyConversionInfo;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentConfirmationCodeValidationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.CurrencyConversionInfoSpi;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PisAuthorisationConfirmationService {

    private final AspspProfileServiceWrapper aspspProfileServiceWrapper;
    private final Xs2aPisCommonPaymentMapper pisCommonPaymentMapper;
    private final Xs2aToSpiPaymentMapper xs2aToSpiPaymentMapper;
    private final AuthorisationServiceEncrypted authorisationServiceEncrypted;
    private final SpiContextDataProvider spiContextDataProvider;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final SpiErrorMapper spiErrorMapper;
    private final PisCheckAuthorisationConfirmationService pisCheckAuthorisationConfirmationService;
    private final PisCommonPaymentServiceEncrypted pisCommonPaymentServiceEncrypted;
    private final Xs2aUpdatePaymentAfterSpiService xs2aUpdatePaymentAfterSpiService;
    private final CurrencyConversionInfoSpi currencyConversionInfoSpi;
    private final SpiToXs2aCurrencyConversionInfoMapper spiToXs2aCurrencyConversionInfoMapper;

    /**
     * Checks authorisation confirmation data. Has two possible flows:
     * - data is checked at XS2A side, we compare the data from DB with the incoming data;
     * - data is transferred to SPI level and checking should be implemented at ASPSP side.
     *
     * @param request {@link Xs2aUpdatePisCommonPaymentPsuDataRequest} with all payment information.
     * @return {@link Xs2aUpdatePisCommonPaymentPsuDataResponse} with new authorisation status.
     */
    public Xs2aUpdatePisCommonPaymentPsuDataResponse processAuthorisationConfirmation(Xs2aUpdatePisCommonPaymentPsuDataRequest request) {
        String paymentId = request.getPaymentId();
        String authorisationId = request.getAuthorisationId();

        CmsResponse<Authorisation> pisAuthorisationResponse = authorisationServiceEncrypted.getAuthorisationById(authorisationId);

        if (pisAuthorisationResponse.hasError()) {
            ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_404)
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.RESOURCE_UNKNOWN_404_NO_AUTHORISATION))
                                          .build();
            log.info("Payment-ID [{}], Authorisation-ID [{}]. Updating PIS authorisation PSU Data has failed: authorisation is not found by id.",
                     paymentId, authorisationId);
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, request.getPsuData());

        }

        Authorisation authorisationResponse = pisAuthorisationResponse.getPayload();

        ScaStatus status = authorisationResponse.getScaStatus();

        boolean processIsAllowed = status == ScaStatus.UNCONFIRMED;

        return processIsAllowed
                   ? processAuthorisationConfirmationInternal(request, authorisationResponse)
                   : buildScaConfirmationCodeErrorResponse(paymentId, authorisationId, request.getPsuData());
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse processAuthorisationConfirmationInternal(Xs2aUpdatePisCommonPaymentPsuDataRequest request,
                                                                                               Authorisation authorisationResponse) {
        return aspspProfileServiceWrapper.isAuthorisationConfirmationCheckByXs2a()
                   ? checkAuthorisationConfirmationXs2a(request, authorisationResponse)
                   : checkAuthorisationConfirmationOnSpi(request, authorisationResponse);
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse checkAuthorisationConfirmationXs2a(Xs2aUpdatePisCommonPaymentPsuDataRequest request, Authorisation authorisation) {
        boolean codeCorrect = StringUtils.equals(request.getConfirmationCode(), authorisation.getScaAuthenticationData());

        CmsResponse<PisCommonPaymentResponse> pisCommonPaymentResponseCmsResponse = pisCommonPaymentServiceEncrypted.getCommonPaymentById(request.getPaymentId());
        SpiPayment payment = xs2aToSpiPaymentMapper.mapToSpiPayment(pisCommonPaymentResponseCmsResponse.getPayload());

        SpiContextData contextData = spiContextDataProvider.provideWithPsuIdData(request.getPsuData());
        SpiAspspConsentDataProvider aspspConsentDataProvider = aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(request.getPaymentId());

        AuthorisationType authorisationType = authorisation.getAuthorisationType();
        boolean isCancellation = AuthorisationType.PIS_CANCELLATION == authorisationType;
        SpiResponse<SpiPaymentConfirmationCodeValidationResponse> spiResponse = pisCheckAuthorisationConfirmationService.notifyConfirmationCodeValidation(contextData, codeCorrect, payment, isCancellation, aspspConsentDataProvider);

        if (spiResponse.hasError()) {
            return buildConfirmationCodeSpiErrorResponse(spiResponse, request.getPaymentId(), request.getAuthorisationId(), request.getPsuData());
        }

        SpiResponse<SpiCurrencyConversionInfo> conversionSpiResponse = currencyConversionInfoSpi.getCurrencyConversionInfo(contextData, payment, authorisation.getAuthorisationId(), aspspConsentDataProvider);

        Xs2aUpdatePisCommonPaymentPsuDataResponse response = resolveResponse(request, codeCorrect, spiResponse.getPayload(), conversionSpiResponse.getPayload());

        UpdateAuthorisationRequest updatePaymentRequest = pisCommonPaymentMapper.mapToUpdateAuthorisationRequest(response, authorisationType);

        if (spiResponse.isSuccessful()) {
            SpiPaymentConfirmationCodeValidationResponse payload = spiResponse.getPayload();
            authorisationServiceEncrypted.updateAuthorisation(request.getAuthorisationId(), updatePaymentRequest);
            xs2aUpdatePaymentAfterSpiService.updatePaymentStatus(request.getPaymentId(), payload.getTransactionStatus());
        }

        return response;
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse resolveResponse(Xs2aUpdatePisCommonPaymentPsuDataRequest request,
                                                                      boolean codeCorrect,
                                                                      SpiPaymentConfirmationCodeValidationResponse spiResponse,
                                                                      SpiCurrencyConversionInfo conversionSpiResponse) {
        return codeCorrect
                   ? buildResponseWithCurrencyConversionInfo(request, spiResponse, conversionSpiResponse)
                   : buildScaConfirmationCodeErrorResponse(request.getPaymentId(), request.getAuthorisationId(), request.getPsuData());
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse buildResponseWithCurrencyConversionInfo(Xs2aUpdatePisCommonPaymentPsuDataRequest request,
                                                                                              SpiPaymentConfirmationCodeValidationResponse spiConfirmationResponse,
                                                                                              SpiCurrencyConversionInfo conversionSpiResponse) {
        return Xs2aUpdatePisCommonPaymentPsuDataResponse
                   .buildWithCurrencyConversionInfo(spiConfirmationResponse.getScaStatus(),
                                                    request.getPaymentId(),
                                                    request.getAuthorisationId(),
                                                    request.getPsuData(),
                                                    spiToXs2aCurrencyConversionInfoMapper
                                                        .toXs2aCurrencyConversionInfo(conversionSpiResponse)
                   );
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse checkAuthorisationConfirmationOnSpi(Xs2aUpdatePisCommonPaymentPsuDataRequest request,
                                                                                          Authorisation authorisationResponse) {
        SpiContextData contextData = spiContextDataProvider.provideWithPsuIdData(request.getPsuData());

        CmsResponse<PisCommonPaymentResponse> pisCommonPaymentResponseCmsResponse = pisCommonPaymentServiceEncrypted.getCommonPaymentById(request.getPaymentId());
        SpiPayment payment = xs2aToSpiPaymentMapper.mapToSpiPayment(pisCommonPaymentResponseCmsResponse.getPayload());

        SpiAspspConsentDataProvider aspspConsentDataProvider = aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(request.getPaymentId());
        SpiCheckConfirmationCodeRequest spiCheckConfirmationCodeRequest = new SpiCheckConfirmationCodeRequest(request.getConfirmationCode(), authorisationResponse.getAuthorisationId());

        SpiResponse<SpiPaymentConfirmationCodeValidationResponse> spiResponse = pisCheckAuthorisationConfirmationService.checkConfirmationCode(contextData, spiCheckConfirmationCodeRequest, payment, aspspConsentDataProvider);

        Xs2aUpdatePisCommonPaymentPsuDataResponse xs2aUpdatePisCommonPaymentPsuDataResponse;
        if (spiResponse.hasError()) {
            xs2aUpdatePaymentAfterSpiService.updatePaymentStatus(request.getPaymentId(), TransactionStatus.RJCT);
            xs2aUpdatePisCommonPaymentPsuDataResponse = buildConfirmationCodeSpiErrorResponse(spiResponse, request.getPaymentId(), request.getAuthorisationId(), request.getPsuData());
        } else {
            SpiPaymentConfirmationCodeValidationResponse codeValidationResponse = spiResponse.getPayload();
            xs2aUpdatePaymentAfterSpiService.updatePaymentStatus(request.getPaymentId(), codeValidationResponse.getTransactionStatus());
            SpiResponse<SpiCurrencyConversionInfo> conversionInfoSpiResponse = currencyConversionInfoSpi.getCurrencyConversionInfo(contextData, payment, authorisationResponse.getAuthorisationId(), aspspConsentDataProvider);
            SpiCurrencyConversionInfo spiCurrencyConversionInfo = conversionInfoSpiResponse.getPayload();
            xs2aUpdatePisCommonPaymentPsuDataResponse = Xs2aUpdatePisCommonPaymentPsuDataResponse.buildWithCurrencyConversionInfo(
                codeValidationResponse.getScaStatus(),
                request.getPaymentId(),
                request.getAuthorisationId(),
                request.getPsuData(),
                spiToXs2aCurrencyConversionInfoMapper.toXs2aCurrencyConversionInfo(spiCurrencyConversionInfo)
            );
        }

        UpdateAuthorisationRequest updatePaymentRequest = pisCommonPaymentMapper.mapToUpdateAuthorisationRequest(xs2aUpdatePisCommonPaymentPsuDataResponse, authorisationResponse.getAuthorisationType());
        authorisationServiceEncrypted.updateAuthorisation(request.getAuthorisationId(), updatePaymentRequest);

        return xs2aUpdatePisCommonPaymentPsuDataResponse;
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse buildConfirmationCodeSpiErrorResponse(SpiResponse<SpiPaymentConfirmationCodeValidationResponse> spiResponse, String paymentId, String authorisationId, PsuIdData psuData) {
        ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS);

        log.info("Authorisation-ID: [{}]. Update payment PSU data failed: error occurred at SPI.", authorisationId);

        return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, psuData);
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse buildScaConfirmationCodeErrorResponse(String paymentId, String authorisationId, PsuIdData psuData) {
        ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_400)
                                      .tppMessages(TppMessageInformation.of(MessageErrorCode.SCA_INVALID))
                                      .build();
        log.info("Payment-ID [{}], Authorisation-ID [{}]. Updating PIS authorisation PSU Data has failed: confirmation code is wrong or has been provided more than once.",
                 paymentId, authorisationId);

        return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, psuData);
    }

}
