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

package de.adorsys.psd2.xs2a.service.authorization.processor.service;

import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.CreatePaymentAuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.domain.consent.pis.PaymentAuthorisationParameters;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.PisAuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisCommonPaymentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPaymentMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiStartAuthorisationResponse;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PaymentCancellationSpi;
import de.adorsys.psd2.xs2a.util.reader.TestSpiDataProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Currency;
import java.util.Optional;
import java.util.Set;

import static de.adorsys.psd2.xs2a.core.error.ErrorType.PIS_400;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PisCancellationAuthorisationProcessorServiceImplTest {
    private static final String TEST_PAYMENT_ID = "12345676";
    private static final String TEST_AUTHORISATION_ID = "assddsff";
    private static final PsuIdData TEST_PSU_DATA = new PsuIdData("test-user", null, null, null, null);
    private static final ScaApproach TEST_SCA_APPROACH = ScaApproach.EMBEDDED;
    private static final ScaStatus TEST_SCA_STATUS = ScaStatus.RECEIVED;
    private static final SpiContextData SPI_CONTEXT_DATA = TestSpiDataProvider.getSpiContextData();
    private static final Set<TppMessageInformation> TEST_TPP_MESSAGES = buildTppMessageInformationSet();
    private static final String TEST_PSU_MESSAGE = "psu message";
    private static final String TEST_PAYMENT_PRODUCT = "sepa- credit-transfers";
    private static final SpiSinglePayment TEST_SPI_SINGLE_PAYMENT = new SpiSinglePayment(TEST_PAYMENT_PRODUCT);
    private static final ErrorType TEST_ERROR_TYPE_400 = PIS_400;

    @InjectMocks
    private PisCancellationAuthorisationProcessorServiceImpl pisCancellationAuthorisationProcessorService;
    @Mock
    private Xs2aPisCommonPaymentService xs2aPisCommonPaymentService;
    @Mock
    private Xs2aToSpiPaymentMapper xs2aToSpiPaymentMapper;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    @Mock
    private PaymentCancellationSpi paymentCancellationSpi;
    @Mock
    private SpiAspspConsentDataProvider spiAspspConsentDataProvider;
    @Mock
    private SpiErrorMapper spiErrorMapper;

    private PisCommonPaymentResponse commonPaymentResponse;

    @BeforeEach
    void setUp() {
        commonPaymentResponse = new PisCommonPaymentResponse();
    }

    @Test
    void doScaExempted_success() {
        // When
        AuthorisationProcessorRequest authorisationProcessorRequest = buildAuthorisationProcessorRequest();
        assertThrows(UnsupportedOperationException.class, () -> pisCancellationAuthorisationProcessorService.doScaExempted(authorisationProcessorRequest));
    }


    @Test
    void doScaStarted_success() {
        // Given
        AuthorisationProcessorRequest authorisationProcessorRequest = buildAuthorisationProcessorRequest();
        when(spiContextDataProvider.provideWithPsuIdData(TEST_PSU_DATA)).thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(TEST_PAYMENT_ID)).thenReturn(spiAspspConsentDataProvider);
        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(TEST_PAYMENT_ID)).thenReturn(Optional.of(commonPaymentResponse));
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(any(PisCommonPaymentResponse.class))).thenReturn(TEST_SPI_SINGLE_PAYMENT);
        SpiResponse<SpiStartAuthorisationResponse> spiResponse = SpiResponse.<SpiStartAuthorisationResponse>builder()
                                                                     .payload(new SpiStartAuthorisationResponse(TEST_SCA_APPROACH, TEST_SCA_STATUS, TEST_PSU_MESSAGE, TEST_TPP_MESSAGES))
                                                                     .build();
        when(paymentCancellationSpi.startAuthorisation(SPI_CONTEXT_DATA, TEST_SCA_APPROACH, TEST_SCA_STATUS, TEST_AUTHORISATION_ID, TEST_SPI_SINGLE_PAYMENT, spiAspspConsentDataProvider))
            .thenReturn(spiResponse);

        // When
        AuthorisationProcessorResponse actual = pisCancellationAuthorisationProcessorService.doScaStarted(authorisationProcessorRequest);
        CreatePaymentAuthorisationProcessorResponse expected = buildCreatePaymentAuthorisationProcessorResponse();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void doScaStarted_spiError() {
        // Given
        AuthorisationProcessorRequest authorisationProcessorRequest = buildAuthorisationProcessorRequest();
        when(spiContextDataProvider.provideWithPsuIdData(TEST_PSU_DATA)).thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(TEST_PAYMENT_ID)).thenReturn(spiAspspConsentDataProvider);
        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(TEST_PAYMENT_ID)).thenReturn(Optional.of(commonPaymentResponse));
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(any(PisCommonPaymentResponse.class))).thenReturn(TEST_SPI_SINGLE_PAYMENT);
        SpiResponse<SpiStartAuthorisationResponse> spiResponse = SpiResponse.<SpiStartAuthorisationResponse>builder()
                                                                     .error(new TppMessage(MessageErrorCode.FORMAT_ERROR_ABSENT_HEADER))
                                                                     .build();
        when(paymentCancellationSpi.startAuthorisation(SPI_CONTEXT_DATA, TEST_SCA_APPROACH, TEST_SCA_STATUS, TEST_AUTHORISATION_ID, TEST_SPI_SINGLE_PAYMENT, spiAspspConsentDataProvider))
            .thenReturn(spiResponse);
        ErrorHolder errorHolder = ErrorHolder.builder(TEST_ERROR_TYPE_400).build();
        when(spiErrorMapper.mapToErrorHolder(eq(spiResponse), any())).thenReturn(errorHolder);

        // When
        AuthorisationProcessorResponse actual = pisCancellationAuthorisationProcessorService.doScaStarted(authorisationProcessorRequest);
        CreatePaymentAuthorisationProcessorResponse expected = buildCreatePaymentAuthorisationProcessorResponseWithError(errorHolder);

        // Then
        assertThat(actual.hasError()).isTrue();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void doScaFinalised_success() {
        // Given
        PisCommonPaymentResponse pisCommonPaymentResponse = new PisCommonPaymentResponse();
        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(TEST_PAYMENT_ID)).thenReturn(Optional.of(pisCommonPaymentResponse));
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(pisCommonPaymentResponse)).thenReturn(new SpiSinglePayment("sepa-credit-transfers"));

        SpiAmount spiAmount = new SpiAmount(Currency.getInstance("EUR"), BigDecimal.valueOf(34));
        // When
        AuthorisationProcessorResponse actual = pisCancellationAuthorisationProcessorService.doScaFinalised(buildAuthorisationProcessorRequest());

        // Then
        assertNotNull(actual);
        assertNull(actual.getErrorHolder());
        assertTrue(actual instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse);
        assertThat(actual.getScaStatus()).isEqualTo(ScaStatus.FINALISED);
        assertThat(actual.getAuthorisationId()).isEqualTo(TEST_AUTHORISATION_ID);
        assertThat(actual.getPaymentId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat((actual).getPsuData()).isEqualTo(TEST_PSU_DATA);
    }

    @Test
    void doScaFailed_success() {
        // When
        AuthorisationProcessorRequest authorisationProcessorRequest = buildEmptyAuthorisationProcessorRequest();
        assertThrows(UnsupportedOperationException.class, () -> pisCancellationAuthorisationProcessorService.doScaFailed(authorisationProcessorRequest));
    }

    private AuthorisationProcessorRequest buildEmptyAuthorisationProcessorRequest() {
        return new PisAuthorisationProcessorRequest(null,
                                                    null,
                                                    null,
                                                    null);
    }

    private AuthorisationProcessorRequest buildAuthorisationProcessorRequest() {
        PaymentAuthorisationParameters request = new PaymentAuthorisationParameters();
        request.setPaymentId(TEST_PAYMENT_ID);
        request.setAuthorisationId(TEST_AUTHORISATION_ID);
        request.setPsuData(TEST_PSU_DATA);
        return new PisAuthorisationProcessorRequest(TEST_SCA_APPROACH,
                                                    TEST_SCA_STATUS,
                                                    request,
                                                    new Authorisation());
    }

    private static Set<TppMessageInformation> buildTppMessageInformationSet() {
        return Collections.singleton(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR));
    }

    private CreatePaymentAuthorisationProcessorResponse buildCreatePaymentAuthorisationProcessorResponse() {
        return new CreatePaymentAuthorisationProcessorResponse(TEST_SCA_STATUS, TEST_SCA_APPROACH, TEST_PSU_MESSAGE, TEST_TPP_MESSAGES, TEST_PAYMENT_ID, TEST_PSU_DATA);
    }

    private CreatePaymentAuthorisationProcessorResponse buildCreatePaymentAuthorisationProcessorResponseWithError(ErrorHolder errorHolder) {
        return new CreatePaymentAuthorisationProcessorResponse(errorHolder, TEST_SCA_APPROACH, TEST_PAYMENT_ID, TEST_PSU_DATA);
    }
}
