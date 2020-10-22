/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

import de.adorsys.psd2.xs2a.service.authorization.pis.PisExecutePaymentService;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentExecutionResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.CommonPaymentSpi;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PisExecutePaymentServiceCommonImpl implements PisExecutePaymentService {
    private final CommonPaymentSpi commonPaymentSpi;

    @Override
    public SpiResponse<SpiPaymentExecutionResponse> verifyScaAuthorisationAndExecutePaymentWithPaymentResponse(SpiContextData contextData,
                                                                                                      SpiScaConfirmation spiScaConfirmation,
                                                                                                      SpiPayment payment,
                                                                                                      SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return commonPaymentSpi.verifyScaAuthorisationAndExecutePaymentWithPaymentResponse(contextData,
                                                                                           spiScaConfirmation,
                                                                                           (SpiPaymentInfo) payment,
                                                                                           spiAspspConsentDataProvider);
    }

    @Override
    public SpiResponse<SpiPaymentExecutionResponse> executePaymentWithoutSca(SpiContextData contextData, SpiPayment payment, SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return commonPaymentSpi.executePaymentWithoutSca(contextData, (SpiPaymentInfo) payment, aspspConsentDataProvider);
    }
}
