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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.PaymentScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aScaStatusResponse;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aLinksMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaStatusResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PaymentCancellationSpi;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import de.adorsys.psd2.xs2a.util.reader.TestSpiDataProvider;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentCancellationServiceForAuthorisationTest {
    private static final String AUTHORISATION_ID = "3278921mxl-n2131-13nw";
    private static final String PAYMENT_ID = "3278921mxl-n2131-13nw";
    private static final String CORRECT_PSU_ID = "marion.mueller";
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(CORRECT_PSU_ID, null, null, null, null);
    private static final SpiContextData SPI_CONTEXT_DATA = TestSpiDataProvider.getSpiContextData();

    @InjectMocks
    private PaymentCancellationServiceForAuthorisationImpl paymentCancellationServiceForAuthorisation;

    @Mock
    private PaymentCancellationSpi paymentCancellationSpi;
    @Mock
    private PaymentCancellationAuthorisationService paymentCancellationAuthorisationService;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private RequestProviderService requestProviderService;
    @Mock
    private SpiAspspConsentDataProvider spiAspspConsentDataProvider;
    @Mock
    private Xs2aAuthorisationService xs2aAuthorisationService;
    @Mock
    private SpiToXs2aLinksMapper spiToXs2aLinksMapper;
    @Spy
    private final Xs2aToSpiPaymentMapper xs2aToSpiPaymentMapper = new Xs2aToSpiPaymentMapper(new Xs2aToSpiPsuDataMapper());

    private final JsonReader jsonReader = new JsonReader();
    private PisCommonPaymentResponse pisCommonPaymentResponse;

    @BeforeEach
    void setUp() {
        pisCommonPaymentResponse = jsonReader.getObjectFromFile("json/service/payment/pis-common-payment-response.json", PisCommonPaymentResponse.class);
    }

    @Test
    void getPaymentAuthorisationScaStatus_errorFromAuthorisation() {
        // Given
        ResponseObject<PaymentScaStatus> paymentScaStatusResponse = ResponseObject.<PaymentScaStatus>builder()
                                                                        .fail(new MessageError())
                                                                        .build();

        when(paymentCancellationAuthorisationService.getPaymentCancellationAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID, PaymentType.SINGLE, PAYMENT_PRODUCT))
            .thenReturn(paymentScaStatusResponse);

        // When
        ResponseObject<Xs2aScaStatusResponse> actual = paymentCancellationServiceForAuthorisation.getAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID, PaymentType.SINGLE, PAYMENT_PRODUCT);

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual.hasError()).isTrue();
    }

    @Test
    void getPaymentAuthorisationScaStatus_successNotFinalised() {
        // Given
        Xs2aScaStatusResponse expected = new Xs2aScaStatusResponse(ScaStatus.FINALISED, true, "psu message", null, null);

        PaymentScaStatus paymentScaStatus = new PaymentScaStatus(PSU_ID_DATA, pisCommonPaymentResponse, ScaStatus.RECEIVED);
        ResponseObject<PaymentScaStatus> paymentScaStatusResponse = ResponseObject.<PaymentScaStatus>builder()
                                                                        .body(paymentScaStatus)
                                                                        .build();

        when(paymentCancellationAuthorisationService.getPaymentCancellationAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID, PaymentType.SINGLE, PAYMENT_PRODUCT))
            .thenReturn(paymentScaStatusResponse);
        when(requestProviderService.getPsuIdData()).thenReturn(PSU_ID_DATA);
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(PAYMENT_ID))
            .thenReturn(spiAspspConsentDataProvider);

        SpiResponse<SpiScaStatusResponse> spiResponse = SpiResponse.<SpiScaStatusResponse>builder()
                                                            .payload(new SpiScaStatusResponse(ScaStatus.FINALISED, true, "psu message", null, null))
                                                            .build();
        when(paymentCancellationSpi.getScaStatus(ScaStatus.RECEIVED, SPI_CONTEXT_DATA, AUTHORISATION_ID,
                                                 xs2aToSpiPaymentMapper.mapToSpiPayment(pisCommonPaymentResponse), spiAspspConsentDataProvider)).thenReturn(spiResponse);

        // When
        ResponseObject<Xs2aScaStatusResponse> actual = paymentCancellationServiceForAuthorisation.getAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID, PaymentType.SINGLE, PAYMENT_PRODUCT);

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual.hasError()).isFalse();
        assertThat(actual.getBody()).isEqualTo(expected);
    }

    @Test
    void getPaymentAuthorisationScaStatus_spiError() {
        // Given
        PaymentScaStatus paymentScaStatus = new PaymentScaStatus(PSU_ID_DATA, pisCommonPaymentResponse, ScaStatus.FINALISED);
        ResponseObject<PaymentScaStatus> paymentScaStatusResponse = ResponseObject.<PaymentScaStatus>builder()
                                                                        .body(paymentScaStatus)
                                                                        .build();

        when(paymentCancellationAuthorisationService.getPaymentCancellationAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID, PaymentType.SINGLE, PAYMENT_PRODUCT))
            .thenReturn(paymentScaStatusResponse);
        when(requestProviderService.getPsuIdData()).thenReturn(PSU_ID_DATA);
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(PAYMENT_ID))
            .thenReturn(spiAspspConsentDataProvider);

        SpiResponse<SpiScaStatusResponse> spiResponse = SpiResponse.<SpiScaStatusResponse>builder()
                                               .error(new TppMessage(MessageErrorCode.FORMAT_ERROR))
                                               .build();
        SpiPayment businessObject = xs2aToSpiPaymentMapper.mapToSpiPayment(pisCommonPaymentResponse);
        when(paymentCancellationSpi.getScaStatus(ScaStatus.FINALISED, SPI_CONTEXT_DATA, AUTHORISATION_ID, businessObject, spiAspspConsentDataProvider)).thenReturn(spiResponse);
        when(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS)).thenReturn(ErrorHolder.builder(ErrorType.PIS_400).build());

        // When
        ResponseObject<Xs2aScaStatusResponse> actual = paymentCancellationServiceForAuthorisation.getAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID, PaymentType.SINGLE, PAYMENT_PRODUCT);

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual.hasError()).isTrue();
        verify(paymentCancellationSpi, times(1)).getScaStatus(ScaStatus.FINALISED, SPI_CONTEXT_DATA, AUTHORISATION_ID, businessObject, spiAspspConsentDataProvider);
        verify(spiErrorMapper, times(1)).mapToErrorHolder(spiResponse, ServiceType.PIS);
        verifyNoInteractions(xs2aAuthorisationService);
    }

    @Test
    void getPaymentAuthorisationScaStatus_success() {
        // Given
        Xs2aScaStatusResponse expected = new Xs2aScaStatusResponse(ScaStatus.FINALISED, true, "psu message", null, null);

        PaymentScaStatus paymentScaStatus = new PaymentScaStatus(PSU_ID_DATA, pisCommonPaymentResponse, ScaStatus.RECEIVED);
        ResponseObject<PaymentScaStatus> paymentScaStatusResponse = ResponseObject.<PaymentScaStatus>builder()
                                                                        .body(paymentScaStatus)
                                                                        .build();
        SpiPayment spiPayment = xs2aToSpiPaymentMapper.mapToSpiPayment(pisCommonPaymentResponse);

        when(paymentCancellationAuthorisationService.getPaymentCancellationAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID, PaymentType.SINGLE, PAYMENT_PRODUCT))
            .thenReturn(paymentScaStatusResponse);
        when(requestProviderService.getPsuIdData()).thenReturn(PSU_ID_DATA);
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(PAYMENT_ID))
            .thenReturn(spiAspspConsentDataProvider);

        SpiResponse<SpiScaStatusResponse> spiResponse = SpiResponse.<SpiScaStatusResponse>builder()
                                               .payload(new SpiScaStatusResponse(ScaStatus.FINALISED, true, "psu message", null, null))
                                               .build();
        when(paymentCancellationSpi.getScaStatus(ScaStatus.RECEIVED, SPI_CONTEXT_DATA, AUTHORISATION_ID, spiPayment, spiAspspConsentDataProvider)).thenReturn(spiResponse);

        // When
        ResponseObject<Xs2aScaStatusResponse> actual = paymentCancellationServiceForAuthorisation.getAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID, PaymentType.SINGLE, PAYMENT_PRODUCT);

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual.hasError()).isFalse();
        assertThat(actual.getBody()).isEqualTo(expected);

        verify(paymentCancellationSpi, times(1)).getScaStatus(ScaStatus.RECEIVED, SPI_CONTEXT_DATA, AUTHORISATION_ID, spiPayment, spiAspspConsentDataProvider);
        verify(spiErrorMapper, never()).mapToErrorHolder(any(), any());
        verify(xs2aAuthorisationService).updateAuthorisationStatus(AUTHORISATION_ID, ScaStatus.FINALISED);
    }
}
