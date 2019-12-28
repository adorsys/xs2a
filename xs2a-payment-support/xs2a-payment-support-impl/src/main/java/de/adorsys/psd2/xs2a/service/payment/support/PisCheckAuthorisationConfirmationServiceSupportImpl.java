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

package de.adorsys.psd2.xs2a.service.payment.support;

import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisCheckAuthorisationConfirmationService;
import de.adorsys.psd2.xs2a.service.payment.support.mapper.spi.SpiPaymentMapper;
import de.adorsys.psd2.xs2a.service.profile.StandardPaymentProductsResolver;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiConfirmationCode;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiConfirmationCodeCheckingResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PisCheckAuthorisationConfirmationServiceSupportImpl implements PisCheckAuthorisationConfirmationService {
    private final StandardPaymentProductsResolver standardPaymentProductsResolver;
    private final CommonPaymentSpi commonPaymentSpi;
    private final SinglePaymentSpi singlePaymentSpi;
    private final PeriodicPaymentSpi periodicPaymentSpi;
    private final BulkPaymentSpi bulkPaymentSpi;
    private final SpiPaymentMapper spiPaymentMapper;

    @Override
    public SpiResponse<SpiConfirmationCodeCheckingResponse> checkConfirmationCode(SpiContextData contextData, SpiConfirmationCode spiConfirmationCode, SpiPayment payment, SpiAspspConsentDataProvider aspspConsentDataProvider) {
        if (standardPaymentProductsResolver.isRawPaymentProduct(payment.getPaymentProduct())) {
            return executeSpi(commonPaymentSpi, (SpiPaymentInfo) payment, contextData, spiConfirmationCode, aspspConsentDataProvider);
        }
        PaymentType paymentType = payment.getPaymentType();
        if (PaymentType.SINGLE == paymentType) {
            return executeSpi(singlePaymentSpi, spiPaymentMapper.mapToSpiSinglePayment(payment), contextData, spiConfirmationCode, aspspConsentDataProvider);
        } else if (PaymentType.PERIODIC == paymentType) {
            return executeSpi(periodicPaymentSpi, spiPaymentMapper.mapToSpiPeriodicPayment(payment), contextData, spiConfirmationCode, aspspConsentDataProvider);
        } else {
            return executeSpi(bulkPaymentSpi, spiPaymentMapper.mapToSpiBulkPayment(payment), contextData, spiConfirmationCode, aspspConsentDataProvider);
        }

    }

    private <T extends SpiPayment> SpiResponse<SpiConfirmationCodeCheckingResponse> executeSpi(PaymentSpi<T, ? extends SpiPaymentInitiationResponse> paymentSpi,
                                                                                               T payment,
                                                                                               SpiContextData contextData,
                                                                                               SpiConfirmationCode spiConfirmationCode,
                                                                                               SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return paymentSpi.checkConfirmationCode(contextData, spiConfirmationCode, payment, aspspConsentDataProvider);
    }
}
