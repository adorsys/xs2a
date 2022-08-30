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
