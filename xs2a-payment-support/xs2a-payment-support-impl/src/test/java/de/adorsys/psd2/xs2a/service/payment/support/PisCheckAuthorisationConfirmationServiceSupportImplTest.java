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

package de.adorsys.psd2.xs2a.service.payment.support;

import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.service.payment.support.mapper.spi.SpiPaymentMapper;
import de.adorsys.psd2.xs2a.service.profile.StandardPaymentProductsResolver;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiConfirmationCode;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiBulkPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiConfirmationCodeCheckingResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.BulkPaymentSpi;
import de.adorsys.psd2.xs2a.spi.service.CommonPaymentSpi;
import de.adorsys.psd2.xs2a.spi.service.PeriodicPaymentSpi;
import de.adorsys.psd2.xs2a.spi.service.SinglePaymentSpi;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PisCheckAuthorisationConfirmationServiceSupportImplTest {
    private static final String STANDARD_PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String RAW_PAYMENT_PRODUCT = "raw-product";
    private static final SpiContextData SPI_CONTEXT_DATA = new SpiContextData(null, null, null, null, null);
    private static final SpiConfirmationCode SPI_CONFIRMATION_CODE = new SpiConfirmationCode(null);
    private static final SpiSinglePayment SPI_SINGLE_PAYMENT = new SpiSinglePayment(STANDARD_PAYMENT_PRODUCT);
    private static final SpiPeriodicPayment SPI_PERIODIC_PAYMENT = new SpiPeriodicPayment(STANDARD_PAYMENT_PRODUCT);
    private static final SpiBulkPayment SPI_BULK_PAYMENT = new SpiBulkPayment();

    @Mock
    private StandardPaymentProductsResolver standardPaymentProductsResolver;
    @Mock
    private CommonPaymentSpi commonPaymentSpi;
    @Mock
    private SinglePaymentSpi singlePaymentSpi;
    @Mock
    private PeriodicPaymentSpi periodicPaymentSpi;
    @Mock
    private BulkPaymentSpi bulkPaymentSpi;
    @Mock
    private SpiPaymentMapper spiPaymentMapper;

    @Mock
    private SpiAspspConsentDataProvider mockSpiAspspConsentDataProvider;

    @InjectMocks
    private PisCheckAuthorisationConfirmationServiceSupportImpl pisCheckAuthorisationConfirmationServiceSupport;

    @Test
    void checkConfirmationCode_raw() {
        // Given
        when(standardPaymentProductsResolver.isRawPaymentProduct(RAW_PAYMENT_PRODUCT)).thenReturn(true);

        SpiPaymentInfo rawSpiPayment = buildSpiPaymentInfo(RAW_PAYMENT_PRODUCT, PaymentType.SINGLE);

        SpiConfirmationCodeCheckingResponse spiConfirmationCodeCheckingResponse = new SpiConfirmationCodeCheckingResponse(ScaStatus.PSUIDENTIFIED);
        SpiResponse<SpiConfirmationCodeCheckingResponse> expectedResponse = SpiResponse.<SpiConfirmationCodeCheckingResponse>builder()
                                                                                .payload(spiConfirmationCodeCheckingResponse)
                                                                                .build();
        when(commonPaymentSpi.checkConfirmationCode(SPI_CONTEXT_DATA, SPI_CONFIRMATION_CODE, rawSpiPayment, mockSpiAspspConsentDataProvider))
            .thenReturn(expectedResponse);

        // When
        SpiResponse<SpiConfirmationCodeCheckingResponse> spiConfirmationCodeCheckingResponseSpiResponse =
            pisCheckAuthorisationConfirmationServiceSupport.checkConfirmationCode(SPI_CONTEXT_DATA, SPI_CONFIRMATION_CODE, rawSpiPayment, mockSpiAspspConsentDataProvider);

        // Then
        assertEquals(expectedResponse, spiConfirmationCodeCheckingResponseSpiResponse);

        verify(singlePaymentSpi, never()).checkConfirmationCode(any(), any(), any(), any());
        verify(periodicPaymentSpi, never()).checkConfirmationCode(any(), any(), any(), any());
        verify(bulkPaymentSpi, never()).checkConfirmationCode(any(), any(), any(), any());
    }

    @Test
    void checkConfirmationCode_single() {
        // Given
        SpiPaymentInfo standardSpiPayment = buildSpiPaymentInfo(STANDARD_PAYMENT_PRODUCT, PaymentType.SINGLE);

        when(spiPaymentMapper.mapToSpiSinglePayment(standardSpiPayment)).thenReturn(SPI_SINGLE_PAYMENT);

        SpiConfirmationCodeCheckingResponse spiConfirmationCodeCheckingResponse = new SpiConfirmationCodeCheckingResponse(ScaStatus.PSUIDENTIFIED);
        SpiResponse<SpiConfirmationCodeCheckingResponse> expectedResponse = SpiResponse.<SpiConfirmationCodeCheckingResponse>builder()
                                                                                .payload(spiConfirmationCodeCheckingResponse)
                                                                                .build();
        when(singlePaymentSpi.checkConfirmationCode(SPI_CONTEXT_DATA, SPI_CONFIRMATION_CODE, SPI_SINGLE_PAYMENT, mockSpiAspspConsentDataProvider))
            .thenReturn(expectedResponse);

        // When
        SpiResponse<SpiConfirmationCodeCheckingResponse> spiConfirmationCodeCheckingResponseSpiResponse =
            pisCheckAuthorisationConfirmationServiceSupport.checkConfirmationCode(SPI_CONTEXT_DATA, SPI_CONFIRMATION_CODE, standardSpiPayment, mockSpiAspspConsentDataProvider);

        // Then
        assertEquals(expectedResponse, spiConfirmationCodeCheckingResponseSpiResponse);

        verify(commonPaymentSpi, never()).checkConfirmationCode(any(), any(), any(), any());
        verify(periodicPaymentSpi, never()).checkConfirmationCode(any(), any(), any(), any());
        verify(bulkPaymentSpi, never()).checkConfirmationCode(any(), any(), any(), any());
    }

    @Test
    void checkConfirmationCode_periodic() {
        // Given
        SpiPaymentInfo standardSpiPayment = buildSpiPaymentInfo(STANDARD_PAYMENT_PRODUCT, PaymentType.PERIODIC);

        when(spiPaymentMapper.mapToSpiPeriodicPayment(standardSpiPayment)).thenReturn(SPI_PERIODIC_PAYMENT);

        SpiConfirmationCodeCheckingResponse spiConfirmationCodeCheckingResponse = new SpiConfirmationCodeCheckingResponse(ScaStatus.PSUIDENTIFIED);
        SpiResponse<SpiConfirmationCodeCheckingResponse> expectedResponse = SpiResponse.<SpiConfirmationCodeCheckingResponse>builder()
                                                                                .payload(spiConfirmationCodeCheckingResponse)
                                                                                .build();
        when(periodicPaymentSpi.checkConfirmationCode(SPI_CONTEXT_DATA, SPI_CONFIRMATION_CODE, SPI_PERIODIC_PAYMENT, mockSpiAspspConsentDataProvider))
            .thenReturn(expectedResponse);

        // When
        SpiResponse<SpiConfirmationCodeCheckingResponse> spiConfirmationCodeCheckingResponseSpiResponse =
            pisCheckAuthorisationConfirmationServiceSupport.checkConfirmationCode(SPI_CONTEXT_DATA, SPI_CONFIRMATION_CODE, standardSpiPayment, mockSpiAspspConsentDataProvider);

        // Then
        assertEquals(expectedResponse, spiConfirmationCodeCheckingResponseSpiResponse);

        verify(commonPaymentSpi, never()).checkConfirmationCode(any(), any(), any(), any());
        verify(singlePaymentSpi, never()).checkConfirmationCode(any(), any(), any(), any());
        verify(bulkPaymentSpi, never()).checkConfirmationCode(any(), any(), any(), any());
    }

    @Test
    void checkConfirmationCode_bulk() {
        // Given
        SpiPaymentInfo standardSpiPayment = buildSpiPaymentInfo(STANDARD_PAYMENT_PRODUCT, PaymentType.BULK);

        when(spiPaymentMapper.mapToSpiBulkPayment(standardSpiPayment)).thenReturn(SPI_BULK_PAYMENT);

        SpiConfirmationCodeCheckingResponse spiConfirmationCodeCheckingResponse = new SpiConfirmationCodeCheckingResponse(ScaStatus.PSUIDENTIFIED);
        SpiResponse<SpiConfirmationCodeCheckingResponse> expectedResponse = SpiResponse.<SpiConfirmationCodeCheckingResponse>builder()
                                                                                .payload(spiConfirmationCodeCheckingResponse)
                                                                                .build();
        when(bulkPaymentSpi.checkConfirmationCode(SPI_CONTEXT_DATA, SPI_CONFIRMATION_CODE, SPI_BULK_PAYMENT, mockSpiAspspConsentDataProvider))
            .thenReturn(expectedResponse);

        // When
        SpiResponse<SpiConfirmationCodeCheckingResponse> spiConfirmationCodeCheckingResponseSpiResponse =
            pisCheckAuthorisationConfirmationServiceSupport.checkConfirmationCode(SPI_CONTEXT_DATA, SPI_CONFIRMATION_CODE, standardSpiPayment, mockSpiAspspConsentDataProvider);

        // Then
        assertEquals(expectedResponse, spiConfirmationCodeCheckingResponseSpiResponse);

        verify(commonPaymentSpi, never()).checkConfirmationCode(any(), any(), any(), any());
        verify(singlePaymentSpi, never()).checkConfirmationCode(any(), any(), any(), any());
        verify(periodicPaymentSpi, never()).checkConfirmationCode(any(), any(), any(), any());
    }

    @NotNull
    private SpiPaymentInfo buildSpiPaymentInfo(String paymentProduct, PaymentType paymentType) {
        SpiPaymentInfo spiPaymentInfo = new SpiPaymentInfo(paymentProduct);
        spiPaymentInfo.setPaymentType(paymentType);
        return spiPaymentInfo;
    }
}
