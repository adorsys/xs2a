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
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiCheckConfirmationCodeRequest;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentConfirmationCodeValidationResponse;
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
    public SpiResponse<SpiPaymentConfirmationCodeValidationResponse> checkConfirmationCode(SpiContextData contextData, SpiCheckConfirmationCodeRequest spiCheckConfirmationCodeRequest, SpiPayment payment, SpiAspspConsentDataProvider aspspConsentDataProvider) {
        if (standardPaymentProductsResolver.isRawPaymentProduct(payment.getPaymentProduct())) {
            return checkConfirmationCode(commonPaymentSpi, contextData, spiCheckConfirmationCodeRequest, aspspConsentDataProvider);
        }
        PaymentType paymentType = payment.getPaymentType();
        if (PaymentType.SINGLE == paymentType) {
            return checkConfirmationCode(singlePaymentSpi, contextData, spiCheckConfirmationCodeRequest, aspspConsentDataProvider);
        } else if (PaymentType.PERIODIC == paymentType) {
            return checkConfirmationCode(periodicPaymentSpi, contextData, spiCheckConfirmationCodeRequest, aspspConsentDataProvider);
        } else {
            return checkConfirmationCode(bulkPaymentSpi, contextData, spiCheckConfirmationCodeRequest, aspspConsentDataProvider);
        }
    }

    @Override
    public SpiResponse<SpiPaymentConfirmationCodeValidationResponse> notifyConfirmationCodeValidation(SpiContextData contextData, boolean confirmationCodeValidationResult, SpiPayment payment, boolean isCancellation, SpiAspspConsentDataProvider aspspConsentDataProvider) {
        if (standardPaymentProductsResolver.isRawPaymentProduct(payment.getPaymentProduct())) {
            return notifyConfirmationCodeValidation(commonPaymentSpi, (SpiPaymentInfo) payment, isCancellation, contextData, confirmationCodeValidationResult, aspspConsentDataProvider);
        }
        PaymentType paymentType = payment.getPaymentType();
        if (PaymentType.SINGLE == paymentType) {
            return notifyConfirmationCodeValidation(singlePaymentSpi, spiPaymentMapper.mapToSpiSinglePayment(payment), isCancellation, contextData, confirmationCodeValidationResult, aspspConsentDataProvider);
        } else if (PaymentType.PERIODIC == paymentType) {
            return notifyConfirmationCodeValidation(periodicPaymentSpi, spiPaymentMapper.mapToSpiPeriodicPayment(payment), isCancellation, contextData, confirmationCodeValidationResult, aspspConsentDataProvider);
        } else {
            return notifyConfirmationCodeValidation(bulkPaymentSpi, spiPaymentMapper.mapToSpiBulkPayment(payment), isCancellation, contextData, confirmationCodeValidationResult, aspspConsentDataProvider);
        }
    }

    private <T extends SpiPayment> SpiResponse<SpiPaymentConfirmationCodeValidationResponse> notifyConfirmationCodeValidation(PaymentSpi<T, ? extends SpiPaymentInitiationResponse> paymentSpi,
                                                                                                                              T payment,
                                                                                                                              boolean isCancellation,
                                                                                                                              SpiContextData contextData,
                                                                                                                              boolean confirmationCodeValidationResult,
                                                                                                                              SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return paymentSpi.notifyConfirmationCodeValidation(contextData, confirmationCodeValidationResult, payment, isCancellation, aspspConsentDataProvider);
    }

    private <T extends SpiPayment> SpiResponse<SpiPaymentConfirmationCodeValidationResponse> checkConfirmationCode(PaymentSpi<T, ? extends SpiPaymentInitiationResponse> paymentSpi,
                                                                                                                   SpiContextData contextData,
                                                                                                                   SpiCheckConfirmationCodeRequest spiCheckConfirmationCodeRequest,
                                                                                                                   SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return paymentSpi.checkConfirmationCode(contextData, spiCheckConfirmationCodeRequest, aspspConsentDataProvider);
    }

    @Override
    public boolean checkConfirmationCodeInternally(String authorisationId, String confirmationCode, String scaAuthenticationData, SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return commonPaymentSpi.checkConfirmationCodeInternally(authorisationId, confirmationCode, scaAuthenticationData, aspspConsentDataProvider);
    }
}
