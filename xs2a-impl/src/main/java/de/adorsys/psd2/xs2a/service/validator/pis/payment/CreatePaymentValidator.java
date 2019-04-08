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

package de.adorsys.psd2.xs2a.service.validator.pis.payment;

import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.BusinessValidator;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.FORMAT_ERROR;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.PIS_400;

/**
 * Validator to be used for validating create payment request according to some business rules
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreatePaymentValidator implements BusinessValidator<PaymentInitiationParameters> {
    private final AspspProfileServiceWrapper aspspProfileServiceWrapper;
    private final RequestProviderService requestProviderService;

    /**
     * Validates create payment request by checking whether:
     * <ul>
     * <li>PSU is present in the request if it's mandated by the profile</li>
     * </ul>
     *
     * @param paymentInitiationParameters payment initiation parameters, passed to the service method
     * @return valid result if the parameters are valid, invalid result with appropriate error otherwise
     */
    @NotNull
    @Override
    public ValidationResult validate(@NotNull PaymentInitiationParameters paymentInitiationParameters) {
        if (aspspProfileServiceWrapper.isPsuInInitialRequestMandated()
                && paymentInitiationParameters.getPsuData().isEmpty()) {
            log.info("X-Request-ID: [{}]. Payment initiation has failed: no PSU Data was provided in the request",
                     requestProviderService.getRequestId());
            return ValidationResult.invalid(PIS_400, TppMessageInformation.of(FORMAT_ERROR, "Please provide the PSU identification data"));
        }

        return ValidationResult.valid();
    }
}
