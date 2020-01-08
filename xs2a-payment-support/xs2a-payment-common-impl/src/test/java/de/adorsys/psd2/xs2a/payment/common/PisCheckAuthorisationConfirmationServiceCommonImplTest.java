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

import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiConfirmationCode;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiConfirmationCodeCheckingResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.CommonPaymentSpi;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PisCheckAuthorisationConfirmationServiceCommonImplTest {
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";

    @Mock
    private CommonPaymentSpi commonPaymentSpi;
    @Mock
    private SpiAspspConsentDataProvider spiAspspConsentDataProvider;

    @InjectMocks
    private PisCheckAuthorisationConfirmationServiceCommonImpl pisCheckAuthorisationConfirmationServiceCommon;

    @Test
    public void checkConfirmationCode() {
        // Given
        SpiContextData spiContextData = new SpiContextData(null, null, null, null, null);
        SpiConfirmationCode spiConfirmationCode = new SpiConfirmationCode("some code");
        SpiPaymentInfo payment = new SpiPaymentInfo(PAYMENT_PRODUCT);

        SpiResponse<SpiConfirmationCodeCheckingResponse> commonServiceResponse = SpiResponse.<SpiConfirmationCodeCheckingResponse>builder()
                                                                                     .payload(new SpiConfirmationCodeCheckingResponse(ScaStatus.FINALISED))
                                                                                     .build();
        when(commonPaymentSpi.checkConfirmationCode(spiContextData, spiConfirmationCode, payment, spiAspspConsentDataProvider))
            .thenReturn(commonServiceResponse);

        // When
        SpiResponse<SpiConfirmationCodeCheckingResponse> actualResponse =
            pisCheckAuthorisationConfirmationServiceCommon.checkConfirmationCode(spiContextData, spiConfirmationCode, payment, spiAspspConsentDataProvider);

        // Then
        assertEquals(commonServiceResponse, actualResponse);
    }
}
