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
import de.adorsys.psd2.xs2a.spi.service.PaymentCancellationSpi;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
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
                                                          RequestProviderService requestProviderService, Xs2aAuthorisationService xs2aAuthorisationService,
                                                          Xs2aToSpiPaymentMapper xs2aToSpiPaymentMapper,
                                                          SpiToXs2aLinksMapper spiToXs2aLinksMapper) {
        super(spiContextDataProvider, aspspConsentDataProviderFactory, spiErrorMapper, requestProviderService, xs2aAuthorisationService, xs2aToSpiPaymentMapper, spiToXs2aLinksMapper);
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
                                                   @NotNull SpiPayment businessObject,
                                                   @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return paymentCancellationSpi.getScaStatus(scaStatus, contextData, authorisationId, businessObject, aspspConsentDataProvider);
    }
}
