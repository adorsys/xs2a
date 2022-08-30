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
