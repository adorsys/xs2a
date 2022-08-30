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

package de.adorsys.psd2.xs2a.service.authorization.pis;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.service.AuthorisationServiceEncrypted;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.pis.PaymentAuthorisationParameters;
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
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentConfirmationCodeValidationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.CurrencyConversionInfoSpi;
import de.adorsys.psd2.xs2a.util.reader.TestSpiDataProvider;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Currency;

import static de.adorsys.psd2.consent.api.CmsError.TECHNICAL_ERROR;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.PSU_CREDENTIALS_INVALID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PisAuthorisationConfirmationServiceTest {
    private static final String PAYMENT_ID = "c713a32c-15ff-4f90-afa0-34a500359844";
    private static final String AUTHORISATION_ID = "ad746cb3-a01b-4196-a6b9-40b0e4cd2350";
    private static final String CONFIRMATION_CODE = "123456";
    private static final String SCA_AUTHENTICATION_DATA = "54321";
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final SpiSinglePayment SPI_SINGLE_PAYMENT = new SpiSinglePayment(PAYMENT_PRODUCT);
    private final static JsonReader jsonReader = new JsonReader();

    @InjectMocks
    private PisAuthorisationConfirmationService pisAuthorisationConfirmationService;
    @Mock
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;
    @Mock
    private Xs2aPisCommonPaymentMapper pisCommonPaymentMapper;
    @Mock
    private Xs2aToSpiPaymentMapper xs2aToSpiPaymentMapper;
    @Mock
    private AuthorisationServiceEncrypted authorisationServiceEncrypted;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    @Mock
    private PisCheckAuthorisationConfirmationService pisCheckAuthorisationConfirmationService;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private SpiAspspConsentDataProvider aspspConsentDataProvider;
    @Mock
    private Xs2aUpdatePaymentAfterSpiService xs2aUpdatePaymentAfterSpiService;
    @Mock
    private PisCommonPaymentServiceEncrypted pisCommonPaymentServiceEncrypted;
    @Mock
    private CurrencyConversionInfoSpi currencyConversionInfoSpi;
    @Mock
    private SpiToXs2aCurrencyConversionInfoMapper spiToXs2aCurrencyConversionInfoMapper;

    @Test
    void processAuthorisationConfirmation_success_checkOnSpi() {
        // given
        PsuIdData psuIdData = buildPsuIdData();
        PaymentAuthorisationParameters request = buildUpdatePisCommonPaymentPsuDataRequest();
        TransactionStatus transactionStatus = TransactionStatus.ACSP;
        Xs2aUpdatePisCommonPaymentPsuDataResponse expectedResult = new Xs2aUpdatePisCommonPaymentPsuDataResponse(
            ScaStatus.FINALISED, PAYMENT_ID, AUTHORISATION_ID, psuIdData, null);
        Authorisation authorisationResponse = buildGetPisAuthorisationResponse();

        SpiCheckConfirmationCodeRequest spiCheckConfirmationCodeRequest = new SpiCheckConfirmationCodeRequest(request.getConfirmationCode(), AUTHORISATION_ID);
        SpiContextData contextData = getSpiContextData();

        when(aspspProfileServiceWrapper.isAuthorisationConfirmationCheckByXs2a())
            .thenReturn(false);
        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<Authorisation>builder()
                            .payload(authorisationResponse)
                            .build());
        PisCommonPaymentResponse commonPaymentResponse = new PisCommonPaymentResponse();
        when(pisCommonPaymentServiceEncrypted.getCommonPaymentById(PAYMENT_ID))
            .thenReturn(CmsResponse.<PisCommonPaymentResponse>builder()
                            .payload(commonPaymentResponse)
                            .build());
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(commonPaymentResponse))
            .thenReturn(SPI_SINGLE_PAYMENT);
        when(spiContextDataProvider.provideWithPsuIdData(psuIdData))
            .thenReturn(contextData);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(PAYMENT_ID))
            .thenReturn(aspspConsentDataProvider);
        when(pisCheckAuthorisationConfirmationService.checkConfirmationCode(contextData, spiCheckConfirmationCodeRequest, SPI_SINGLE_PAYMENT, aspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiPaymentConfirmationCodeValidationResponse>builder()
                            .payload(new SpiPaymentConfirmationCodeValidationResponse(ScaStatus.FINALISED, transactionStatus))
                            .build());

        SpiAmount spiAmount = new SpiAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(34));
        SpiCurrencyConversionInfo spiCurrencyConversionInfo = new SpiCurrencyConversionInfo(spiAmount, spiAmount, spiAmount, spiAmount);
        when(currencyConversionInfoSpi.getCurrencyConversionInfo(contextData, SPI_SINGLE_PAYMENT, authorisationResponse.getAuthorisationId(), aspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiCurrencyConversionInfo>builder()
                            .payload(spiCurrencyConversionInfo)
                            .build());

        // when
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResult = pisAuthorisationConfirmationService.processAuthorisationConfirmation(request);

        // then
        assertThat(actualResult).isEqualTo(expectedResult);
        verify(xs2aUpdatePaymentAfterSpiService).updatePaymentStatus(PAYMENT_ID, transactionStatus);
    }

    @Test
    void processAuthorisationConfirmation_success_checkOnXs2a() {
        // given
        PsuIdData psuIdData = buildPsuIdData();
        PaymentAuthorisationParameters request = buildUpdatePisCommonPaymentPsuDataRequest();
        Xs2aUpdatePisCommonPaymentPsuDataResponse expectedResult = new Xs2aUpdatePisCommonPaymentPsuDataResponse(
            ScaStatus.FINALISED, PAYMENT_ID, AUTHORISATION_ID, psuIdData, null);
        Authorisation authorisationResponse = buildGetPisAuthorisationResponse();

        when(aspspProfileServiceWrapper.isAuthorisationConfirmationCheckByXs2a()).thenReturn(true);
        when(pisCheckAuthorisationConfirmationService.checkConfirmationCodeInternally(AUTHORISATION_ID, CONFIRMATION_CODE, SCA_AUTHENTICATION_DATA, aspspConsentDataProvider)).thenReturn(true);
        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID)).thenReturn(CmsResponse.<Authorisation>builder()
                                                                                                  .payload(authorisationResponse)
                                                                                                  .build());

        PisCommonPaymentResponse commonPaymentResponse = new PisCommonPaymentResponse();
        when(pisCommonPaymentServiceEncrypted.getCommonPaymentById(PAYMENT_ID)).thenReturn(CmsResponse.<PisCommonPaymentResponse>builder()
                                                                                               .payload(commonPaymentResponse)
                                                                                               .build());
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(commonPaymentResponse))
            .thenReturn(SPI_SINGLE_PAYMENT);

        SpiContextData contextData = getSpiContextData();
        SpiPaymentConfirmationCodeValidationResponse response = new SpiPaymentConfirmationCodeValidationResponse(ScaStatus.FINALISED, TransactionStatus.ACSP);
        SpiResponse<SpiPaymentConfirmationCodeValidationResponse> spiResponse = SpiResponse.<SpiPaymentConfirmationCodeValidationResponse>builder().payload(response).build();
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(PAYMENT_ID)).thenReturn(aspspConsentDataProvider);
        when(spiContextDataProvider.provideWithPsuIdData(psuIdData)).thenReturn(contextData);
        when(pisCheckAuthorisationConfirmationService.notifyConfirmationCodeValidation(contextData, true, SPI_SINGLE_PAYMENT, false, aspspConsentDataProvider)).thenReturn(spiResponse);
        SpiAmount spiAmount = new SpiAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(34));
        SpiCurrencyConversionInfo spiCurrencyConversionInfo = new SpiCurrencyConversionInfo(spiAmount, spiAmount, spiAmount, spiAmount);
        when(currencyConversionInfoSpi.getCurrencyConversionInfo(contextData, SPI_SINGLE_PAYMENT, authorisationResponse.getAuthorisationId(), aspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiCurrencyConversionInfo>builder()
                            .payload(spiCurrencyConversionInfo)
                            .build());

        // when
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResult = pisAuthorisationConfirmationService.processAuthorisationConfirmation(request);

        // then
        assertThat(actualResult).isEqualTo(expectedResult);

        verify(xs2aUpdatePaymentAfterSpiService, times(1)).updatePaymentStatus(PAYMENT_ID, response.getTransactionStatus());
        verify(pisCheckAuthorisationConfirmationService, times(1)).checkConfirmationCodeInternally(AUTHORISATION_ID, CONFIRMATION_CODE, SCA_AUTHENTICATION_DATA, aspspConsentDataProvider);
    }

    @Test
    void processAuthorisationConfirmation_failed_NoAuthorisation() {
        // given
        PaymentAuthorisationParameters request = buildUpdatePisCommonPaymentPsuDataRequest();

        ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_404)
                                      .tppMessages(TppMessageInformation.of(MessageErrorCode.RESOURCE_UNKNOWN_404_NO_AUTHORISATION))
                                      .build();
        Xs2aUpdatePisCommonPaymentPsuDataResponse expectedResult = new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, request.getPaymentId(), request.getAuthorisationId(), request.getPsuData());


        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID)).thenReturn(CmsResponse.<Authorisation>builder()
                                                                                                  .error(TECHNICAL_ERROR)
                                                                                                  .build());

        // when
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResult = pisAuthorisationConfirmationService.processAuthorisationConfirmation(request);

        // then
        assertThat(actualResult.hasError()).isTrue();
        assertThat(actualResult.getErrorHolder()).isEqualToComparingFieldByField(expectedResult.getErrorHolder());
    }

    @Test
    void processAuthorisationConfirmation_failed_WrongScaStatus() {
        // given
        PaymentAuthorisationParameters request = buildUpdatePisCommonPaymentPsuDataRequest();

        ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_400)
                                      .tppMessages(TppMessageInformation.of(MessageErrorCode.SCA_INVALID))
                                      .build();
        Xs2aUpdatePisCommonPaymentPsuDataResponse expectedResult = new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, request.getPaymentId(), request.getAuthorisationId(), request.getPsuData());


        Authorisation authorisationResponse = buildGetPisAuthorisationResponse();
        authorisationResponse.setScaStatus(ScaStatus.FAILED);

        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID)).thenReturn(CmsResponse.<Authorisation>builder()
                                                                                                  .payload(authorisationResponse)
                                                                                                  .build());

        // when
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResult = pisAuthorisationConfirmationService.processAuthorisationConfirmation(request);

        // then
        assertThat(actualResult.hasError()).isTrue();
        assertThat(actualResult.getErrorHolder()).isEqualToComparingFieldByField(expectedResult.getErrorHolder());
    }

    @Test
    void processAuthorisationConfirmation_failed_wrongCode() {
        // given
        PsuIdData psuIdData = buildPsuIdData();
        PaymentAuthorisationParameters request = buildUpdatePisCommonPaymentPsuDataRequest();
        request.setConfirmationCode("wrong_code");

        ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_400)
                                      .tppMessages(TppMessageInformation.of(MessageErrorCode.SCA_INVALID))
                                      .build();
        Xs2aUpdatePisCommonPaymentPsuDataResponse expectedResult = new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, request.getPaymentId(), request.getAuthorisationId(), request.getPsuData());

        Authorisation authorisationResponse = buildGetPisAuthorisationResponse();

        when(aspspProfileServiceWrapper.isAuthorisationConfirmationCheckByXs2a()).thenReturn(true);
        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID)).thenReturn(CmsResponse.<Authorisation>builder()
                                                                                                  .payload(authorisationResponse)
                                                                                                  .build());

        PisCommonPaymentResponse commonPaymentResponse = new PisCommonPaymentResponse();
        when(pisCommonPaymentServiceEncrypted.getCommonPaymentById(PAYMENT_ID)).thenReturn(CmsResponse.<PisCommonPaymentResponse>builder()
                                                                                               .payload(commonPaymentResponse)
                                                                                               .build());
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(commonPaymentResponse))
            .thenReturn(SPI_SINGLE_PAYMENT);

        SpiContextData contextData = getSpiContextData();
        SpiPaymentConfirmationCodeValidationResponse response = new SpiPaymentConfirmationCodeValidationResponse(ScaStatus.FAILED, TransactionStatus.RJCT);
        SpiResponse<SpiPaymentConfirmationCodeValidationResponse> spiResponse = SpiResponse.<SpiPaymentConfirmationCodeValidationResponse>builder().payload(response).build();
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(PAYMENT_ID)).thenReturn(aspspConsentDataProvider);
        when(spiContextDataProvider.provideWithPsuIdData(psuIdData)).thenReturn(contextData);
        when(pisCheckAuthorisationConfirmationService.notifyConfirmationCodeValidation(contextData, false, SPI_SINGLE_PAYMENT, false, aspspConsentDataProvider)).thenReturn(spiResponse);
        SpiAmount spiAmount = new SpiAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(34));
        SpiCurrencyConversionInfo spiCurrencyConversionInfo = new SpiCurrencyConversionInfo(spiAmount, spiAmount, spiAmount, spiAmount);
        when(currencyConversionInfoSpi.getCurrencyConversionInfo(contextData, SPI_SINGLE_PAYMENT, authorisationResponse.getAuthorisationId(), aspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiCurrencyConversionInfo>builder()
                            .payload(spiCurrencyConversionInfo)
                            .build());

        // when
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResult = pisAuthorisationConfirmationService.processAuthorisationConfirmation(request);

        // then
        assertThat(actualResult.hasError()).isTrue();
        assertThat(actualResult.getErrorHolder()).isEqualToComparingFieldByField(expectedResult.getErrorHolder());
        verify(xs2aUpdatePaymentAfterSpiService, times(1)).updatePaymentStatus(PAYMENT_ID, response.getTransactionStatus());
    }

    @Test
    void processAuthorisationConfirmation_checkOnSpi_spiError() {
        // given
        PsuIdData psuIdData = buildPsuIdData();
        PaymentAuthorisationParameters request = buildUpdatePisCommonPaymentPsuDataRequest();

        ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_400)
                                      .tppMessages(TppMessageInformation.of(MessageErrorCode.SCA_INVALID))
                                      .build();
        Xs2aUpdatePisCommonPaymentPsuDataResponse expectedResult = new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, request.getPaymentId(), request.getAuthorisationId(), request.getPsuData());

        Authorisation authorisationResponse = buildGetPisAuthorisationResponse();

        when(aspspProfileServiceWrapper.isAuthorisationConfirmationCheckByXs2a()).thenReturn(true);
        when(pisCheckAuthorisationConfirmationService.checkConfirmationCodeInternally(AUTHORISATION_ID, CONFIRMATION_CODE, SCA_AUTHENTICATION_DATA, aspspConsentDataProvider)).thenReturn(true);
        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID)).thenReturn(CmsResponse.<Authorisation>builder()
                                                                                                  .payload(authorisationResponse)
                                                                                                  .build());

        PisCommonPaymentResponse commonPaymentResponse = new PisCommonPaymentResponse();
        when(pisCommonPaymentServiceEncrypted.getCommonPaymentById(PAYMENT_ID)).thenReturn(CmsResponse.<PisCommonPaymentResponse>builder()
                                                                                               .payload(commonPaymentResponse)
                                                                                               .build());
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(commonPaymentResponse))
            .thenReturn(SPI_SINGLE_PAYMENT);

        SpiContextData contextData = getSpiContextData();
        TppMessage spiErrorMessage = new TppMessage(MessageErrorCode.SCA_INVALID);
        SpiResponse<SpiPaymentConfirmationCodeValidationResponse> spiResponse = SpiResponse.<SpiPaymentConfirmationCodeValidationResponse>builder().error(spiErrorMessage).build();
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(PAYMENT_ID)).thenReturn(aspspConsentDataProvider);
        when(spiContextDataProvider.provideWithPsuIdData(psuIdData)).thenReturn(contextData);
        when(pisCheckAuthorisationConfirmationService.notifyConfirmationCodeValidation(contextData, true, SPI_SINGLE_PAYMENT, false, aspspConsentDataProvider)).thenReturn(spiResponse);
        when(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS)).thenReturn(errorHolder);

        // when
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResult = pisAuthorisationConfirmationService.processAuthorisationConfirmation(request);

        // then
        assertThat(actualResult.hasError()).isTrue();
        assertThat(actualResult.getErrorHolder()).isEqualToComparingFieldByField(expectedResult.getErrorHolder());
        verify(xs2aUpdatePaymentAfterSpiService, never()).updatePaymentStatus(any(), any());
        verify(pisCheckAuthorisationConfirmationService, times(1)).checkConfirmationCodeInternally(AUTHORISATION_ID, CONFIRMATION_CODE, SCA_AUTHENTICATION_DATA, aspspConsentDataProvider);
    }

    @Test
    void processAuthorisationConfirmation_failed_errorOnSpi() {
        // given
        PsuIdData psuIdData = buildPsuIdData();
        PaymentAuthorisationParameters request = buildUpdatePisCommonPaymentPsuDataRequest();
        SpiResponse<SpiPaymentConfirmationCodeValidationResponse> spiResponse = SpiResponse.<SpiPaymentConfirmationCodeValidationResponse>builder()
                                                                                    .error(new TppMessage(PSU_CREDENTIALS_INVALID))
                                                                                    .build();

        ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_400)
                                      .tppMessages(TppMessageInformation.of(MessageErrorCode.SCA_INVALID))
                                      .build();
        Xs2aUpdatePisCommonPaymentPsuDataResponse expectedResult = new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, request.getPaymentId(), request.getAuthorisationId(), request.getPsuData());
        Authorisation authorisationResponse = buildGetPisAuthorisationResponse();

        SpiCheckConfirmationCodeRequest spiCheckConfirmationCodeRequest = new SpiCheckConfirmationCodeRequest(request.getConfirmationCode(), AUTHORISATION_ID);
        SpiContextData contextData = getSpiContextData();

        when(aspspProfileServiceWrapper.isAuthorisationConfirmationCheckByXs2a()).thenReturn(false);
        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID)).thenReturn(CmsResponse.<Authorisation>builder()
                                                                                                  .payload(authorisationResponse)
                                                                                                  .build());
        PisCommonPaymentResponse commonPaymentResponse = new PisCommonPaymentResponse();
        when(pisCommonPaymentServiceEncrypted.getCommonPaymentById(PAYMENT_ID)).thenReturn(CmsResponse.<PisCommonPaymentResponse>builder()
                                                                                               .payload(commonPaymentResponse)
                                                                                               .build());
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(commonPaymentResponse))
            .thenReturn(SPI_SINGLE_PAYMENT);

        when(spiContextDataProvider.provideWithPsuIdData(psuIdData))
            .thenReturn(contextData);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(PAYMENT_ID))
            .thenReturn(aspspConsentDataProvider);
        when(pisCheckAuthorisationConfirmationService.checkConfirmationCode(contextData, spiCheckConfirmationCodeRequest, SPI_SINGLE_PAYMENT, aspspConsentDataProvider))
            .thenReturn(spiResponse);
        when(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS)).thenReturn(errorHolder);

        // when
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResult = pisAuthorisationConfirmationService.processAuthorisationConfirmation(request);

        // then
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    private SpiContextData getSpiContextData() {
        return TestSpiDataProvider.defaultSpiContextData();
    }

    private Authorisation buildGetPisAuthorisationResponse() {
        Authorisation response = new Authorisation();
        response.setAuthorisationId(AUTHORISATION_ID);
        response.setParentId(PAYMENT_ID);
        response.setScaStatus(ScaStatus.UNCONFIRMED);
        response.setScaAuthenticationData(CONFIRMATION_CODE);
        response.setAuthorisationType(AuthorisationType.PIS_CREATION);
        response.setScaAuthenticationData(SCA_AUTHENTICATION_DATA);
        return response;
    }

    private PaymentAuthorisationParameters buildUpdatePisCommonPaymentPsuDataRequest() {
        PaymentAuthorisationParameters request =
            jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/xs2a-update-pis-common-payment-psu-data-request.json",
                                         PaymentAuthorisationParameters.class);

        request.setConfirmationCode(CONFIRMATION_CODE);
        request.setPaymentId(PAYMENT_ID);
        request.setAuthorisationId(AUTHORISATION_ID);
        request.setScaStatus(ScaStatus.UNCONFIRMED);
        request.setPsuData(buildPsuIdData());
        request.setPaymentProduct(PAYMENT_PRODUCT);
        request.setPaymentService(PaymentType.SINGLE);

        return request;
    }

    private PsuIdData buildPsuIdData() {
        return new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType", "psuIpAddress");
    }

}
