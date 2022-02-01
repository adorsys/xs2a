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

package de.adorsys.psd2.xs2a.web.validator.path;

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.PARAMETER_NOT_SUPPORTED_WRONG_PAYMENT_TYPE;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.PRODUCT_UNKNOWN_WRONG_PAYMENT_PRODUCT;

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
            return ValidationResult.invalid(ErrorType.PIS_404, TppMessageInformation.of(PRODUCT_UNKNOWN_WRONG_PAYMENT_PRODUCT, paymentProduct));
        }

        // Case when URL contains correct type "/v1/payments/", but it is not supported by ASPSP. Bad type.
        return ValidationResult.invalid(ErrorType.PIS_400, TppMessageInformation.of(PARAMETER_NOT_SUPPORTED_WRONG_PAYMENT_TYPE, paymentType.getValue()));
    }
}
