/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.payment.common;

import de.adorsys.psd2.xs2a.service.authorization.pis.PisCheckAuthorisationConfirmationService;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiCheckConfirmationCodeRequest;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentConfirmationCodeValidationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.CommonPaymentSpi;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PisCheckAuthorisationConfirmationServiceCommonImpl implements PisCheckAuthorisationConfirmationService {
    private final CommonPaymentSpi commonPaymentSpi;

    @Override
    public SpiResponse<SpiPaymentConfirmationCodeValidationResponse> checkConfirmationCode(SpiContextData contextData, SpiCheckConfirmationCodeRequest spiCheckConfirmationCodeRequest, SpiPayment payment, SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return commonPaymentSpi.checkConfirmationCode(contextData, spiCheckConfirmationCodeRequest, aspspConsentDataProvider);
    }

    @Override
    public SpiResponse<SpiPaymentConfirmationCodeValidationResponse> notifyConfirmationCodeValidation(SpiContextData contextData, boolean confirmationCodeValidationResult, SpiPayment payment, boolean isCancellation, SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return commonPaymentSpi.notifyConfirmationCodeValidation(contextData, confirmationCodeValidationResult, (SpiPaymentInfo) payment, isCancellation, aspspConsentDataProvider);
    }

    @Override
    public boolean checkConfirmationCodeInternally(String authorisationId, String confirmationCode, String scaAuthenticationData, SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return commonPaymentSpi.checkConfirmationCodeInternally(authorisationId, confirmationCode, scaAuthenticationData, aspspConsentDataProvider);
    }
}
