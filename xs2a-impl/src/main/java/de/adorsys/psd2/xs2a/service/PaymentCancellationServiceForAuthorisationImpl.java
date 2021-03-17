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
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.PaymentScaStatus;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaStatusResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PaymentCancellationSpi;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
public class PaymentCancellationServiceForAuthorisationImpl extends PaymentServiceForAuthorisation {
    private final PaymentCancellationSpi paymentCancellationSpi;
    private final PaymentCancellationAuthorisationService paymentCancellationAuthorisationService;

    public PaymentCancellationServiceForAuthorisationImpl(SpiContextDataProvider spiContextDataProvider,
                                                          SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory,
                                                          SpiErrorMapper spiErrorMapper, PaymentCancellationSpi paymentCancellationSpi,
                                                          PaymentCancellationAuthorisationService paymentCancellationAuthorisationService,
                                                          RequestProviderService requestProviderService, Xs2aAuthorisationService xs2aAuthorisationService) {
        super(spiContextDataProvider, aspspConsentDataProviderFactory, spiErrorMapper, requestProviderService, xs2aAuthorisationService);
        this.paymentCancellationSpi = paymentCancellationSpi;
        this.paymentCancellationAuthorisationService = paymentCancellationAuthorisationService;
    }

    @Override
    ResponseObject<PaymentScaStatus> getCMSScaStatus(String paymentId, String authorisationId, PaymentType paymentType,
                                                     String paymentProduct) {
        return paymentCancellationAuthorisationService.getPaymentCancellationAuthorisationScaStatus(paymentId, authorisationId,
                                                                                                    paymentType, paymentProduct);
    }

    @Override
    SpiResponse<SpiScaStatusResponse> getScaStatus(@NotNull ScaStatus scaStatus, @NotNull SpiContextData contextData,
                                                   @NotNull String authorisationId,
                                                   @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return paymentCancellationSpi.getScaStatus(scaStatus, contextData, authorisationId, aspspConsentDataProvider);
    }
}
