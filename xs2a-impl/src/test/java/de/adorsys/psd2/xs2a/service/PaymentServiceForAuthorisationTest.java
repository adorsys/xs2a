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
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PaymentAuthorisationSpi;
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
class PaymentServiceForAuthorisationTest {
    private static final String AUTHORISATION_ID = "3278921mxl-n2131-13nw";
    private static final String PAYMENT_ID = "3278921mxl-n2131-13nw";
    private static final String CORRECT_PSU_ID = "marion.mueller";
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(CORRECT_PSU_ID, null, null, null, null);

    @InjectMocks
    private PaymentServiceForAuthorisationImpl paymentServiceForAuthorisation;

    @Mock
    private PaymentAuthorisationSpi paymentAuthorisationSpi;
    @Mock
    private PaymentAuthorisationService paymentAuthorisationService;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Spy
    private Xs2aToSpiPaymentMapper xs2aToSpiPaymentMapper = new Xs2aToSpiPaymentMapper(new Xs2aToSpiPsuDataMapper());

    private JsonReader jsonReader = new JsonReader();
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

        when(paymentAuthorisationService.getPaymentInitiationAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID, PaymentType.SINGLE, PAYMENT_PRODUCT))
            .thenReturn(paymentScaStatusResponse);

        // When
        ResponseObject<Xs2aScaStatusResponse> actual = paymentServiceForAuthorisation.getAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID, PaymentType.SINGLE, PAYMENT_PRODUCT);

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual.hasError()).isTrue();
    }

    @Test
    void getPaymentAuthorisationScaStatus_successNotFinalised() {
        // Given
        Xs2aScaStatusResponse expected = new Xs2aScaStatusResponse(ScaStatus.RECEIVED, null);

        PaymentScaStatus paymentScaStatus = new PaymentScaStatus(PSU_ID_DATA, pisCommonPaymentResponse, ScaStatus.RECEIVED);
        ResponseObject<PaymentScaStatus> paymentScaStatusResponse = ResponseObject.<PaymentScaStatus>builder()
                                                                        .body(paymentScaStatus)
                                                                        .build();

        when(paymentAuthorisationService.getPaymentInitiationAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID, PaymentType.SINGLE, PAYMENT_PRODUCT))
            .thenReturn(paymentScaStatusResponse);

        // When
        ResponseObject<Xs2aScaStatusResponse> actual = paymentServiceForAuthorisation.getAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID, PaymentType.SINGLE, PAYMENT_PRODUCT);

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

        when(paymentAuthorisationService.getPaymentInitiationAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID, PaymentType.SINGLE, PAYMENT_PRODUCT))
            .thenReturn(paymentScaStatusResponse);

        SpiResponse<Boolean> spiResponse = SpiResponse.<Boolean>builder()
                                               .error(new TppMessage(MessageErrorCode.FORMAT_ERROR))
                                               .build();
        when(paymentAuthorisationSpi.requestTrustedBeneficiaryFlag(any(), any(), any(), any())).thenReturn(spiResponse);
        when(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS)).thenReturn(ErrorHolder.builder(ErrorType.PIS_400).build());

        // When
        ResponseObject<Xs2aScaStatusResponse> actual = paymentServiceForAuthorisation.getAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID, PaymentType.SINGLE, PAYMENT_PRODUCT);

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual.hasError()).isTrue();
        verify(paymentAuthorisationSpi, times(1)).requestTrustedBeneficiaryFlag(any(), any(), any(), any());
        verify(spiErrorMapper, times(1)).mapToErrorHolder(spiResponse, ServiceType.PIS);
    }

    @Test
    void getPaymentAuthorisationScaStatus_success() {
        // Given
        Xs2aScaStatusResponse expected = new Xs2aScaStatusResponse(ScaStatus.FINALISED, true);

        PaymentScaStatus paymentScaStatus = new PaymentScaStatus(PSU_ID_DATA, pisCommonPaymentResponse, ScaStatus.FINALISED);
        ResponseObject<PaymentScaStatus> paymentScaStatusResponse = ResponseObject.<PaymentScaStatus>builder()
                                                                        .body(paymentScaStatus)
                                                                        .build();

        when(paymentAuthorisationService.getPaymentInitiationAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID, PaymentType.SINGLE, PAYMENT_PRODUCT))
            .thenReturn(paymentScaStatusResponse);

        SpiResponse<Boolean> spiResponse = SpiResponse.<Boolean>builder()
                                               .payload(true)
                                               .build();
        when(paymentAuthorisationSpi.requestTrustedBeneficiaryFlag(any(), any(), any(), any())).thenReturn(spiResponse);

        // When
        ResponseObject<Xs2aScaStatusResponse> actual = paymentServiceForAuthorisation.getAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID, PaymentType.SINGLE, PAYMENT_PRODUCT);

        // Then
        assertThat(actual).isNotNull();
        assertThat(actual.hasError()).isFalse();
        assertThat(actual.getBody()).isEqualTo(expected);

        verify(paymentAuthorisationSpi, times(1)).requestTrustedBeneficiaryFlag(any(), any(), any(), any());
        verify(spiErrorMapper, never()).mapToErrorHolder(any(), any());
    }
}
