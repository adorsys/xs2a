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

package de.adorsys.psd2.xs2a.service.validator.pis.payment;

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.TppUriHeaderValidator;
import de.adorsys.psd2.xs2a.service.validator.pis.AbstractPisValidator;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Validator to be used for validating cancel payment request according to some business rules
 */
@Component
@RequiredArgsConstructor
public class CancelPaymentValidator extends AbstractPisValidator<CancelPaymentPO> {
    private final TppUriHeaderValidator tppUriHeaderValidator;

    /**
     * Validates cancel payment request by checking whether:
     * <ul>
     * <li>given payment's type and product are valid for the payment</li>
     * </ul>
     *
     * @param paymentObject payment information object
     * @return valid result if the payment is valid, invalid result with appropriate error otherwise
     */
    @Override
    protected ValidationResult executeBusinessValidation(CancelPaymentPO paymentObject) {
        return ValidationResult.valid();
    }

    @Override
    public @NotNull Set<TppMessageInformation> buildWarningMessages(@NotNull CancelPaymentPO cancelPaymentPO) {
        return tppUriHeaderValidator.buildWarningMessages(cancelPaymentPO.getTppRedirectUri());
    }
}
