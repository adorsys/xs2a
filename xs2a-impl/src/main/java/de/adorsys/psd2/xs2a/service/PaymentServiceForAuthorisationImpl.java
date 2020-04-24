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

import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.PaymentScaStatus;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.payment.SpiPaymentFactory;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PaymentAuthorisationSpi;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceForAuthorisationImpl extends PaymentServiceForAuthorisation{
    private final PaymentAuthorisationSpi paymentAuthorisationSpi;
    private final PaymentAuthorisationService paymentAuthorisationService;

    public PaymentServiceForAuthorisationImpl(SpiContextDataProvider spiContextDataProvider, SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory, SpiErrorMapper spiErrorMapper, SpiPaymentFactory spiPaymentFactory, PaymentAuthorisationSpi paymentAuthorisationSpi, PaymentAuthorisationService paymentAuthorisationService) {
        super(spiContextDataProvider, aspspConsentDataProviderFactory, spiErrorMapper, spiPaymentFactory);
        this.paymentAuthorisationSpi = paymentAuthorisationSpi;
        this.paymentAuthorisationService = paymentAuthorisationService;
    }

    @Override
    ResponseObject<PaymentScaStatus> getPaymentScaStatus(String paymentId, String authorisationId, PaymentType paymentType, String paymentProduct) {
        return paymentAuthorisationService.getPaymentInitiationAuthorisationScaStatus(paymentId, authorisationId, paymentType, paymentProduct);
    }

    @Override
    SpiResponse<Boolean> getTrustedBeneficiaryFlagFromSpi(SpiContextData contextData, SpiPayment spiPayment, String authorisationId, SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return  paymentAuthorisationSpi.requestTrustedBeneficiaryFlag(contextData, spiPayment, authorisationId, aspspConsentDataProvider);
    }
}
