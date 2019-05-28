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

package de.adorsys.psd2.xs2a.service.validator.pis;

import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.PARAMETER_NOT_SUPPORTED;
import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.PRODUCT_UNKNOWN;

@Component
@RequiredArgsConstructor
public class PaymentTypeAndProductValidator {
    private final AspspProfileServiceWrapper aspspProfileServiceWrapper;

    /**
     * Validates payment type and payment product by checking whether it's supported by the ASPSP profile
     *
     * @param paymentType    payment type
     * @param paymentProduct payment product
     * @return valid result if the payment type and product are supported by the ASPSP, invalid result with an
     * appropriate error otherwise
     */
    public @NotNull ValidationResult validateTypeAndProduct(@NotNull PaymentType paymentType, String paymentProduct) {
        Map<PaymentType, Set<String>> supportedPaymentTypeAndProductMatrix = aspspProfileServiceWrapper.getSupportedPaymentTypeAndProductMatrix();

        if (supportedPaymentTypeAndProductMatrix.containsKey(paymentType)) {
            if (supportedPaymentTypeAndProductMatrix.get(paymentType).contains(paymentProduct)) {
                return ValidationResult.valid();
            }

            // Case when URL contains something like "/sepa-credit-transfers111/". Bad product.
            return ValidationResult.invalid(ErrorType.PIS_404, TppMessageInformation.of(PRODUCT_UNKNOWN, "Wrong payment product: " + paymentProduct));
        }

        // Case when URL contains correct type "/v1/payments/", but it is not supported by ASPSP. Bad type.
        return ValidationResult.invalid(ErrorType.PIS_400, TppMessageInformation.of(PARAMETER_NOT_SUPPORTED, "Wrong payment type: " + paymentType));
    }
}