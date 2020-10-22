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

package de.adorsys.psd2.xs2a.payment.common;

import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentExecutionResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.CommonPaymentSpi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PisExecutePaymentServiceCommonImplTest {
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";

    @Mock
    private CommonPaymentSpi commonPaymentSpi;
    @Mock
    private SpiAspspConsentDataProvider spiAspspConsentDataProvider;

    @InjectMocks
    private PisExecutePaymentServiceCommonImpl pisExecutePaymentServiceCommon;

    @Test
    void verifyScaAuthorisationAndExecutePayment() {
        // Given
        SpiContextData spiContextData = TestSpiDataProvider.defaultSpiContextData();
        SpiScaConfirmation spiScaConfirmation = new SpiScaConfirmation();
        SpiPaymentInfo payment = new SpiPaymentInfo(PAYMENT_PRODUCT);

        SpiResponse<SpiPaymentExecutionResponse> commonServiceResponse = SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                             .payload(new SpiPaymentExecutionResponse(TransactionStatus.ACSP))
                                                                             .build();
        when(commonPaymentSpi.verifyScaAuthorisationAndExecutePaymentWithPaymentResponse(spiContextData, spiScaConfirmation, payment, spiAspspConsentDataProvider))
            .thenReturn(commonServiceResponse);

        // When
        SpiResponse<SpiPaymentExecutionResponse> actualResponse =
            pisExecutePaymentServiceCommon.verifyScaAuthorisationAndExecutePaymentWithPaymentResponse(spiContextData, spiScaConfirmation, payment, spiAspspConsentDataProvider);

        // Then
        assertEquals(commonServiceResponse, actualResponse);

        verify(commonPaymentSpi).verifyScaAuthorisationAndExecutePaymentWithPaymentResponse(spiContextData, spiScaConfirmation, payment, spiAspspConsentDataProvider);
    }

    @Test
    void executePaymentWithoutSca() {
        // Given
        SpiContextData spiContextData = TestSpiDataProvider.defaultSpiContextData();
        SpiPaymentInfo payment = new SpiPaymentInfo(PAYMENT_PRODUCT);

        SpiResponse<SpiPaymentExecutionResponse> commonServiceResponse = SpiResponse.<SpiPaymentExecutionResponse>builder()
                                                                             .payload(new SpiPaymentExecutionResponse(TransactionStatus.ACSP))
                                                                             .build();
        when(commonPaymentSpi.executePaymentWithoutSca(spiContextData, payment, spiAspspConsentDataProvider))
            .thenReturn(commonServiceResponse);

        // When
        SpiResponse<SpiPaymentExecutionResponse> actualResponse =
            pisExecutePaymentServiceCommon.executePaymentWithoutSca(spiContextData, payment, spiAspspConsentDataProvider);

        // Then
        assertEquals(commonServiceResponse, actualResponse);

        verify(commonPaymentSpi).executePaymentWithoutSca(spiContextData, payment, spiAspspConsentDataProvider);
    }
}
