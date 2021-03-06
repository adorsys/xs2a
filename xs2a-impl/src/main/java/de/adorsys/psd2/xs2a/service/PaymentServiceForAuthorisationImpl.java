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
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aLinksMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPaymentMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaStatusResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PaymentAuthorisationSpi;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceForAuthorisationImpl extends PaymentServiceForAuthorisation {
    private final PaymentAuthorisationSpi paymentAuthorisationSpi;
    private final PaymentAuthorisationService paymentAuthorisationService;

    public PaymentServiceForAuthorisationImpl(SpiContextDataProvider spiContextDataProvider,
                                              SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory,
                                              SpiErrorMapper spiErrorMapper, PaymentAuthorisationSpi paymentAuthorisationSpi,
                                              PaymentAuthorisationService paymentAuthorisationService,
                                              RequestProviderService requestProviderService, Xs2aAuthorisationService xs2aAuthorisationService,
                                              Xs2aToSpiPaymentMapper xs2aToSpiPaymentMapper,
                                              SpiToXs2aLinksMapper spiToXs2aLinksMapper) {
        super(spiContextDataProvider, aspspConsentDataProviderFactory, spiErrorMapper, requestProviderService, xs2aAuthorisationService, xs2aToSpiPaymentMapper, spiToXs2aLinksMapper);
        this.paymentAuthorisationSpi = paymentAuthorisationSpi;
        this.paymentAuthorisationService = paymentAuthorisationService;
    }

    @Override
    ResponseObject<PaymentScaStatus> getCMSScaStatus(String paymentId, String authorisationId, PaymentType paymentType,
                                                     String paymentProduct) {
        return paymentAuthorisationService.getPaymentInitiationAuthorisationScaStatus(paymentId, authorisationId,
                                                                                      paymentType, paymentProduct);
    }

    @Override
    SpiResponse<SpiScaStatusResponse> getScaStatus(@NotNull ScaStatus scaStatus, @NotNull SpiContextData contextData,
                                                   @NotNull String authorisationId, @NotNull SpiPayment businessObject,
                                                   @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return paymentAuthorisationSpi.getScaStatus(scaStatus, contextData, authorisationId, businessObject,
                                                    aspspConsentDataProvider);
    }
}
