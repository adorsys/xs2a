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
