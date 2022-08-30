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

package de.adorsys.psd2.xs2a.service.payment.support;

import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisExecutePaymentService;
import de.adorsys.psd2.xs2a.service.payment.support.mapper.spi.SpiPaymentMapper;
import de.adorsys.psd2.xs2a.service.profile.StandardPaymentProductsResolver;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentExecutionResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PisExecutePaymentServiceSupportImpl implements PisExecutePaymentService {
    private final StandardPaymentProductsResolver standardPaymentProductsResolver;
    private final CommonPaymentSpi commonPaymentSpi;
    private final SinglePaymentSpi singlePaymentSpi;
    private final PeriodicPaymentSpi periodicPaymentSpi;
    private final BulkPaymentSpi bulkPaymentSpi;
    private final SpiPaymentMapper spiPaymentMapper;

    @Override
    public SpiResponse<SpiPaymentExecutionResponse> verifyScaAuthorisationAndExecutePaymentWithPaymentResponse(SpiContextData contextData,
                                                                                                      SpiScaConfirmation spiScaConfirmation,
                                                                                                      SpiPayment payment,
                                                                                                      SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        if (standardPaymentProductsResolver.isRawPaymentProduct(payment.getPaymentProduct())) {
            return verifyScaAndExecutePaymentWithPaymentResponse(commonPaymentSpi, (SpiPaymentInfo) payment, spiScaConfirmation, contextData, spiAspspConsentDataProvider);
        }

        PaymentType paymentType = payment.getPaymentType();
        if (PaymentType.SINGLE == paymentType) {
            return verifyScaAndExecutePaymentWithPaymentResponse(singlePaymentSpi, spiPaymentMapper.mapToSpiSinglePayment(payment), spiScaConfirmation, contextData, spiAspspConsentDataProvider);
        } else if (PaymentType.PERIODIC == paymentType) {
            return verifyScaAndExecutePaymentWithPaymentResponse(periodicPaymentSpi, spiPaymentMapper.mapToSpiPeriodicPayment(payment), spiScaConfirmation, contextData, spiAspspConsentDataProvider);
        } else {
            return verifyScaAndExecutePaymentWithPaymentResponse(bulkPaymentSpi, spiPaymentMapper.mapToSpiBulkPayment(payment), spiScaConfirmation, contextData, spiAspspConsentDataProvider);
        }
    }

    @Override
    public SpiResponse<SpiPaymentExecutionResponse> executePaymentWithoutSca(SpiContextData contextData, SpiPayment payment, SpiAspspConsentDataProvider aspspConsentDataProvider) {
        if (standardPaymentProductsResolver.isRawPaymentProduct(payment.getPaymentProduct())) {
            return executeWithoutSca(commonPaymentSpi, (SpiPaymentInfo) payment, contextData, aspspConsentDataProvider);
        }

        PaymentType paymentType = payment.getPaymentType();
        if (PaymentType.SINGLE == paymentType) {
            return executeWithoutSca(singlePaymentSpi, spiPaymentMapper.mapToSpiSinglePayment(payment), contextData, aspspConsentDataProvider);
        } else if (PaymentType.PERIODIC == paymentType) {
            return executeWithoutSca(periodicPaymentSpi, spiPaymentMapper.mapToSpiPeriodicPayment(payment), contextData, aspspConsentDataProvider);
        } else {
            return executeWithoutSca(bulkPaymentSpi, spiPaymentMapper.mapToSpiBulkPayment(payment), contextData, aspspConsentDataProvider);
        }
    }

    private <T extends SpiPayment> SpiResponse<SpiPaymentExecutionResponse> verifyScaAndExecutePaymentWithPaymentResponse(PaymentSpi<T, ? extends SpiPaymentInitiationResponse> paymentSpi,
                                                                                              T payment,
                                                                                              SpiScaConfirmation spiScaConfirmation,
                                                                                              SpiContextData contextData,
                                                                                              SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return paymentSpi.verifyScaAuthorisationAndExecutePaymentWithPaymentResponse(contextData, spiScaConfirmation, payment, spiAspspConsentDataProvider);
    }

    private <T extends SpiPayment> SpiResponse<SpiPaymentExecutionResponse> executeWithoutSca(PaymentSpi<T, ? extends SpiPaymentInitiationResponse> paymentSpi,
                                                                                              T payment,
                                                                                              SpiContextData contextData,
                                                                                              SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return paymentSpi.executePaymentWithoutSca(contextData, payment, spiAspspConsentDataProvider);
    }
}
