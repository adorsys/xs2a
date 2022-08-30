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

import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.consent.pis.PaymentAuthorisationParameters;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aCurrencyConversionInfoMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationDecoupledScaResponse;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiCurrencyConversionInfo;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.CurrencyConversionInfoSpi;
import de.adorsys.psd2.xs2a.spi.service.PaymentAuthorisationSpi;
import de.adorsys.psd2.xs2a.spi.service.PaymentCancellationSpi;
import de.adorsys.psd2.xs2a.util.reader.TestSpiDataProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Currency;

import static de.adorsys.psd2.xs2a.core.sca.ScaStatus.SCAMETHODSELECTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PisCommonDecoupledServiceTest {
    private static final String PRODUCT = "sepa-credit-transfers";
    private static final String PAYMENT_ID = "c713a32c-15ff-4f90-afa0-34a500359844";
    private static final String AUTHORISATION_ID = "ad746cb3-a01b-4196-a6b9-40b0e4cd2350";
    private static final String AUTHENTICATION_METHOD_ID = "dd5d766f-eeb7-4efe-b730-24d5ed53f537";
    private static final String DECOUPLED_PSU_MESSAGE = "Please use your BankApp for transaction Authorisation";
    private static final SpiContextData SPI_CONTEXT_DATA = TestSpiDataProvider.getSpiContextData();
    private static final SpiSinglePayment SPI_SINGLE_PAYMENT = new SpiSinglePayment(PRODUCT);
    private static final SpiResponse<SpiAuthorisationDecoupledScaResponse> AUTH_DECOUPLED_RESPONSE = buildSpiResponse();
    private static final SpiResponse<SpiAuthorisationDecoupledScaResponse> AUTH_DECOUPLED_RESPONSE_FAIL = buildSpiResponseFail();
    private static final PaymentAuthorisationParameters UPDATE_PIS_COMMON_PAYMENT_REQUEST = buildUpdatePisCommonPaymentPsuDataRequest(null);
    private static final PaymentAuthorisationParameters UPDATE_PIS_COMMON_PAYMENT_REQUEST_AUTH_METHOD_ID = buildUpdatePisCommonPaymentPsuDataRequest(AUTHENTICATION_METHOD_ID);
    private static final PsuIdData PSU_DATA = buildPsuIdData();
    private static final Xs2aUpdatePisCommonPaymentPsuDataResponse UPDATE_PIS_COMMON_PAYMENT_RESPONSE = buildUpdatePisCommonPaymentPsuDataResponse();
    private static final ErrorHolder EXPECTED_ERROR = ErrorHolder.builder(ErrorType.PIS_404)
                                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.RESOURCE_UNKNOWN_404_NO_PAYMENT))
                                                          .build();
    @InjectMocks
    private PisCommonDecoupledService pisCommonDecoupledService;

    @Mock
    private PaymentAuthorisationSpi paymentAuthorisationSpi;
    @Mock
    private PaymentCancellationSpi paymentCancellationSpi;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    @Mock
    private SpiAspspConsentDataProvider spiAspspConsentDataProvider;
    @Mock
    private CurrencyConversionInfoSpi currencyConversionInfoSpi;
    @Mock
    private SpiToXs2aCurrencyConversionInfoMapper spiToXs2aCurrencyConversionInfoMapper;

    @BeforeEach
    void init() {
        when(spiContextDataProvider.provideWithPsuIdData(PSU_DATA))
            .thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(PAYMENT_ID)).thenReturn(spiAspspConsentDataProvider);
    }

    @Test
    void proceedDecoupledInitiation_success() {
        // Given
        when(paymentAuthorisationSpi.startScaDecoupled(SPI_CONTEXT_DATA, AUTHORISATION_ID, null, SPI_SINGLE_PAYMENT, spiAspspConsentDataProvider))
            .thenReturn(AUTH_DECOUPLED_RESPONSE);

        SpiAmount spiAmount = new SpiAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(34));
        SpiCurrencyConversionInfo spiCurrencyConversionInfo = new SpiCurrencyConversionInfo(spiAmount, spiAmount, spiAmount, spiAmount);
        when(currencyConversionInfoSpi.getCurrencyConversionInfo(SPI_CONTEXT_DATA, SPI_SINGLE_PAYMENT, AUTHORISATION_ID, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiCurrencyConversionInfo>builder()
                            .payload(spiCurrencyConversionInfo)
                            .build());

        // When
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = pisCommonDecoupledService.proceedDecoupledInitiation(UPDATE_PIS_COMMON_PAYMENT_REQUEST, SPI_SINGLE_PAYMENT, null);

        // Then
        assertThat(actualResponse.hasError()).isFalse();
        assertThat(actualResponse).isEqualTo(UPDATE_PIS_COMMON_PAYMENT_RESPONSE);
    }

    @Test
    void proceedDecoupledInitiation_failed() {
        // Given
        when(paymentAuthorisationSpi.startScaDecoupled(SPI_CONTEXT_DATA, AUTHORISATION_ID, null, SPI_SINGLE_PAYMENT, spiAspspConsentDataProvider))
            .thenReturn(AUTH_DECOUPLED_RESPONSE_FAIL);
        when(spiErrorMapper.mapToErrorHolder(AUTH_DECOUPLED_RESPONSE_FAIL, ServiceType.PIS))
            .thenReturn(EXPECTED_ERROR);
        SpiAmount spiAmount = new SpiAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(34));
        SpiCurrencyConversionInfo spiCurrencyConversionInfo = new SpiCurrencyConversionInfo(spiAmount, spiAmount, spiAmount, spiAmount);
        when(currencyConversionInfoSpi.getCurrencyConversionInfo(SPI_CONTEXT_DATA, SPI_SINGLE_PAYMENT, AUTHORISATION_ID, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiCurrencyConversionInfo>builder()
                            .payload(spiCurrencyConversionInfo)
                            .build());

        // When
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = pisCommonDecoupledService.proceedDecoupledInitiation(UPDATE_PIS_COMMON_PAYMENT_REQUEST, SPI_SINGLE_PAYMENT, null);

        // Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getErrorHolder()).isEqualToComparingFieldByField(EXPECTED_ERROR);
    }

    @Test
    void proceedDecoupledInitiation_authenticationMethodId_success() {
        // Given
        when(paymentAuthorisationSpi.startScaDecoupled(SPI_CONTEXT_DATA, AUTHORISATION_ID, AUTHENTICATION_METHOD_ID, SPI_SINGLE_PAYMENT, spiAspspConsentDataProvider))
            .thenReturn(AUTH_DECOUPLED_RESPONSE);
        SpiAmount spiAmount = new SpiAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(34));
        SpiCurrencyConversionInfo spiCurrencyConversionInfo = new SpiCurrencyConversionInfo(spiAmount, spiAmount, spiAmount, spiAmount);
        when(currencyConversionInfoSpi.getCurrencyConversionInfo(SPI_CONTEXT_DATA, SPI_SINGLE_PAYMENT, AUTHORISATION_ID, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiCurrencyConversionInfo>builder()
                            .payload(spiCurrencyConversionInfo)
                            .build());

        // When
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = pisCommonDecoupledService.proceedDecoupledInitiation(UPDATE_PIS_COMMON_PAYMENT_REQUEST_AUTH_METHOD_ID, SPI_SINGLE_PAYMENT, AUTHENTICATION_METHOD_ID);

        // Then
        assertThat(actualResponse.hasError()).isFalse();
        assertThat(actualResponse).isEqualTo(UPDATE_PIS_COMMON_PAYMENT_RESPONSE);
    }

    @Test
    void proceedDecoupledInitiation_authenticationMethodId_failed() {
        // Given
        when(paymentAuthorisationSpi.startScaDecoupled(SPI_CONTEXT_DATA, AUTHORISATION_ID, AUTHENTICATION_METHOD_ID, SPI_SINGLE_PAYMENT, spiAspspConsentDataProvider))
            .thenReturn(AUTH_DECOUPLED_RESPONSE_FAIL);
        when(spiErrorMapper.mapToErrorHolder(AUTH_DECOUPLED_RESPONSE_FAIL, ServiceType.PIS))
            .thenReturn(EXPECTED_ERROR);
        SpiAmount spiAmount = new SpiAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(34));
        SpiCurrencyConversionInfo spiCurrencyConversionInfo = new SpiCurrencyConversionInfo(spiAmount, spiAmount, spiAmount, spiAmount);
        when(currencyConversionInfoSpi.getCurrencyConversionInfo(SPI_CONTEXT_DATA, SPI_SINGLE_PAYMENT, AUTHORISATION_ID, spiAspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiCurrencyConversionInfo>builder()
                            .payload(spiCurrencyConversionInfo)
                            .build());

        // When
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = pisCommonDecoupledService.proceedDecoupledInitiation(UPDATE_PIS_COMMON_PAYMENT_REQUEST_AUTH_METHOD_ID, SPI_SINGLE_PAYMENT, AUTHENTICATION_METHOD_ID);

        // Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getErrorHolder()).isEqualToComparingFieldByField(EXPECTED_ERROR);
    }

    @Test
    void proceedDecoupledCancellation_authenticationMethodId_success() {
        // Given
        when(paymentCancellationSpi.startScaDecoupled(SPI_CONTEXT_DATA, AUTHORISATION_ID, AUTHENTICATION_METHOD_ID, SPI_SINGLE_PAYMENT, spiAspspConsentDataProvider))
            .thenReturn(AUTH_DECOUPLED_RESPONSE);

        // When
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = pisCommonDecoupledService.proceedDecoupledCancellation(UPDATE_PIS_COMMON_PAYMENT_REQUEST_AUTH_METHOD_ID, SPI_SINGLE_PAYMENT, AUTHENTICATION_METHOD_ID);

        // Then
        assertThat(actualResponse.hasError()).isFalse();
        assertThat(actualResponse).isEqualTo(UPDATE_PIS_COMMON_PAYMENT_RESPONSE);
    }

    @Test
    void proceedDecoupledCancellation_authenticationMethodId_failed() {
        // Given
        when(paymentCancellationSpi.startScaDecoupled(SPI_CONTEXT_DATA, AUTHORISATION_ID, AUTHENTICATION_METHOD_ID, SPI_SINGLE_PAYMENT, spiAspspConsentDataProvider))
            .thenReturn(AUTH_DECOUPLED_RESPONSE_FAIL);
        when(spiErrorMapper.mapToErrorHolder(AUTH_DECOUPLED_RESPONSE_FAIL, ServiceType.PIS))
            .thenReturn(EXPECTED_ERROR);

        // When
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = pisCommonDecoupledService.proceedDecoupledCancellation(UPDATE_PIS_COMMON_PAYMENT_REQUEST_AUTH_METHOD_ID, SPI_SINGLE_PAYMENT, AUTHENTICATION_METHOD_ID);

        // Then
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getErrorHolder()).isEqualToComparingFieldByField(EXPECTED_ERROR);
    }

    private static PsuIdData buildPsuIdData() {
        return new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType", "psuIpAddress");
    }

    private static PaymentAuthorisationParameters buildUpdatePisCommonPaymentPsuDataRequest(String authenticationMethodId) {
        PaymentAuthorisationParameters request = new PaymentAuthorisationParameters();
        request.setPaymentId(PAYMENT_ID);
        request.setAuthorisationId(AUTHORISATION_ID);
        request.setAuthenticationMethodId(authenticationMethodId);
        request.setPsuData(buildPsuIdData());
        return request;
    }

    private static SpiResponse<SpiAuthorisationDecoupledScaResponse> buildSpiResponse() {
        SpiAuthorisationDecoupledScaResponse response = new SpiAuthorisationDecoupledScaResponse(SCAMETHODSELECTED, DECOUPLED_PSU_MESSAGE);
        return SpiResponse.<SpiAuthorisationDecoupledScaResponse>builder()
                   .payload(response)
                   .build();
    }

    private static SpiResponse<SpiAuthorisationDecoupledScaResponse> buildSpiResponseFail() {
        return SpiResponse.<SpiAuthorisationDecoupledScaResponse>builder()
                   .error(new TppMessage(MessageErrorCode.SERVICE_NOT_SUPPORTED))
                   .build();
    }

    private static Xs2aUpdatePisCommonPaymentPsuDataResponse buildUpdatePisCommonPaymentPsuDataResponse() {
        Xs2aUpdatePisCommonPaymentPsuDataResponse response = new Xs2aUpdatePisCommonPaymentPsuDataResponse(
            SCAMETHODSELECTED, PAYMENT_ID, AUTHORISATION_ID, PSU_DATA, null);
        response.setPsuMessage(AUTH_DECOUPLED_RESPONSE.getPayload().getPsuMessage());
        return response;
    }

}
