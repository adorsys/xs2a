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

package de.adorsys.psd2.xs2a.service.payment.support;

import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.service.payment.support.mapper.spi.SpiPaymentMapper;
import de.adorsys.psd2.xs2a.service.profile.StandardPaymentProductsResolver;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiCheckConfirmationCodeRequest;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentConfirmationCodeValidationResponse;
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
    private static final String AUTHORISATION_ID = "a8fc1f02-3639-4528-bd19-3eacf1c67038";
    private static final String RAW_PAYMENT_PRODUCT = "raw-product";
    private static final String CONFIRMATION_CODE = "12345";
    private static final String SCA_AUTHENTICATION_DATA = "54321";
    private static final SpiContextData SPI_CONTEXT_DATA = TestSpiDataProvider.defaultSpiContextData();
    private static final SpiCheckConfirmationCodeRequest SPI_CONFIRMATION_CODE_REQ = new SpiCheckConfirmationCodeRequest(CONFIRMATION_CODE, AUTHORISATION_ID);

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

        SpiPaymentConfirmationCodeValidationResponse spiConfirmationCodeCheckingResponse = new SpiPaymentConfirmationCodeValidationResponse(ScaStatus.PSUIDENTIFIED, TransactionStatus.ACSP);
        SpiResponse<SpiPaymentConfirmationCodeValidationResponse> expectedResponse = SpiResponse.<SpiPaymentConfirmationCodeValidationResponse>builder()
                                                                                         .payload(spiConfirmationCodeCheckingResponse)
                                                                                         .build();
        when(commonPaymentSpi.checkConfirmationCode(SPI_CONTEXT_DATA, SPI_CONFIRMATION_CODE_REQ, mockSpiAspspConsentDataProvider))
            .thenReturn(expectedResponse);

        // When
        SpiResponse<SpiPaymentConfirmationCodeValidationResponse> spiConfirmationCodeCheckingResponseSpiResponse =
            pisCheckAuthorisationConfirmationServiceSupport.checkConfirmationCode(SPI_CONTEXT_DATA, SPI_CONFIRMATION_CODE_REQ, rawSpiPayment, mockSpiAspspConsentDataProvider);

        // Then
        assertEquals(expectedResponse, spiConfirmationCodeCheckingResponseSpiResponse);

        verify(singlePaymentSpi, never()).checkConfirmationCode(any(), any(), any());
        verify(periodicPaymentSpi, never()).checkConfirmationCode(any(), any(), any());
        verify(bulkPaymentSpi, never()).checkConfirmationCode(any(), any(), any());
    }

    @Test
    void checkConfirmationCode_single() {
        // Given
        SpiPaymentInfo standardSpiPayment = buildSpiPaymentInfo(STANDARD_PAYMENT_PRODUCT, PaymentType.SINGLE);

        SpiPaymentConfirmationCodeValidationResponse spiConfirmationCodeCheckingResponse = new SpiPaymentConfirmationCodeValidationResponse(ScaStatus.PSUIDENTIFIED, TransactionStatus.ACSP);
        SpiResponse<SpiPaymentConfirmationCodeValidationResponse> expectedResponse = SpiResponse.<SpiPaymentConfirmationCodeValidationResponse>builder()
                                                                                         .payload(spiConfirmationCodeCheckingResponse)
                                                                                         .build();
        when(singlePaymentSpi.checkConfirmationCode(SPI_CONTEXT_DATA, SPI_CONFIRMATION_CODE_REQ, mockSpiAspspConsentDataProvider))
            .thenReturn(expectedResponse);

        // When
        SpiResponse<SpiPaymentConfirmationCodeValidationResponse> spiConfirmationCodeCheckingResponseSpiResponse =
            pisCheckAuthorisationConfirmationServiceSupport.checkConfirmationCode(SPI_CONTEXT_DATA, SPI_CONFIRMATION_CODE_REQ, standardSpiPayment, mockSpiAspspConsentDataProvider);

        // Then
        assertEquals(expectedResponse, spiConfirmationCodeCheckingResponseSpiResponse);

        verify(commonPaymentSpi, never()).checkConfirmationCode(any(), any(), any());
        verify(periodicPaymentSpi, never()).checkConfirmationCode(any(), any(), any());
        verify(bulkPaymentSpi, never()).checkConfirmationCode(any(), any(), any());
    }

    @Test
    void checkConfirmationCode_periodic() {
        // Given
        SpiPaymentInfo standardSpiPayment = buildSpiPaymentInfo(STANDARD_PAYMENT_PRODUCT, PaymentType.PERIODIC);

        SpiPaymentConfirmationCodeValidationResponse spiConfirmationCodeCheckingResponse = new SpiPaymentConfirmationCodeValidationResponse(ScaStatus.PSUIDENTIFIED, TransactionStatus.ACSP);
        SpiResponse<SpiPaymentConfirmationCodeValidationResponse> expectedResponse = SpiResponse.<SpiPaymentConfirmationCodeValidationResponse>builder()
                                                                                         .payload(spiConfirmationCodeCheckingResponse)
                                                                                         .build();
        when(periodicPaymentSpi.checkConfirmationCode(SPI_CONTEXT_DATA, SPI_CONFIRMATION_CODE_REQ, mockSpiAspspConsentDataProvider))
            .thenReturn(expectedResponse);

        // When
        SpiResponse<SpiPaymentConfirmationCodeValidationResponse> spiConfirmationCodeCheckingResponseSpiResponse =
            pisCheckAuthorisationConfirmationServiceSupport.checkConfirmationCode(SPI_CONTEXT_DATA, SPI_CONFIRMATION_CODE_REQ, standardSpiPayment, mockSpiAspspConsentDataProvider);

        // Then
        assertEquals(expectedResponse, spiConfirmationCodeCheckingResponseSpiResponse);

        verify(commonPaymentSpi, never()).checkConfirmationCode(any(), any(), any());
        verify(singlePaymentSpi, never()).checkConfirmationCode(any(), any(), any());
        verify(bulkPaymentSpi, never()).checkConfirmationCode(any(), any(), any());
    }

    @Test
    void checkConfirmationCode_bulk() {
        // Given
        SpiPaymentInfo standardSpiPayment = buildSpiPaymentInfo(STANDARD_PAYMENT_PRODUCT, PaymentType.BULK);

        SpiPaymentConfirmationCodeValidationResponse spiConfirmationCodeCheckingResponse = new SpiPaymentConfirmationCodeValidationResponse(ScaStatus.PSUIDENTIFIED, TransactionStatus.ACSP);
        SpiResponse<SpiPaymentConfirmationCodeValidationResponse> expectedResponse = SpiResponse.<SpiPaymentConfirmationCodeValidationResponse>builder()
                                                                                         .payload(spiConfirmationCodeCheckingResponse)
                                                                                         .build();
        when(bulkPaymentSpi.checkConfirmationCode(SPI_CONTEXT_DATA, SPI_CONFIRMATION_CODE_REQ, mockSpiAspspConsentDataProvider))
            .thenReturn(expectedResponse);

        // When
        SpiResponse<SpiPaymentConfirmationCodeValidationResponse> spiConfirmationCodeCheckingResponseSpiResponse =
            pisCheckAuthorisationConfirmationServiceSupport.checkConfirmationCode(SPI_CONTEXT_DATA, SPI_CONFIRMATION_CODE_REQ, standardSpiPayment, mockSpiAspspConsentDataProvider);

        // Then
        assertEquals(expectedResponse, spiConfirmationCodeCheckingResponseSpiResponse);

        verify(commonPaymentSpi, never()).checkConfirmationCode(any(), any(), any());
        verify(singlePaymentSpi, never()).checkConfirmationCode(any(), any(), any());
        verify(periodicPaymentSpi, never()).checkConfirmationCode(any(), any(), any());
    }

    @Test
    void checkConfirmationCodeInternally() {
        pisCheckAuthorisationConfirmationServiceSupport.checkConfirmationCodeInternally(AUTHORISATION_ID, CONFIRMATION_CODE, SCA_AUTHENTICATION_DATA, mockSpiAspspConsentDataProvider);
        verify(commonPaymentSpi, times(1)).checkConfirmationCodeInternally(AUTHORISATION_ID, CONFIRMATION_CODE, SCA_AUTHENTICATION_DATA, mockSpiAspspConsentDataProvider);
    }

    @NotNull
    private SpiPaymentInfo buildSpiPaymentInfo(String paymentProduct, PaymentType paymentType) {
        SpiPaymentInfo spiPaymentInfo = new SpiPaymentInfo(paymentProduct);
        spiPaymentInfo.setPaymentType(paymentType);
        return spiPaymentInfo;
    }
}
