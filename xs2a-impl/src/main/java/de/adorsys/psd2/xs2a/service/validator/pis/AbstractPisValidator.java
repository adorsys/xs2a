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

package de.adorsys.psd2.xs2a.service.validator.pis;

import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.service.validator.BusinessValidator;
import de.adorsys.psd2.xs2a.service.validator.tpp.PisTppInfoValidator;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;

/**
 * Common validator for validating TPP in payments and executing request-specific business validation afterwards.
 * Should be used for all PIS-related requests after payment initiation.
 *
 * @param <T> type of object to be checked
 */
@Slf4j
@Component
public abstract class AbstractPisValidator<T extends PaymentTypeAndInfoProvider> implements BusinessValidator<T> {
    private PisTppInfoValidator pisTppInfoValidator;

    @NotNull
    @Override
    public ValidationResult validate(@NotNull T object) {
        ValidationResult paymentTypeAndProductValidationResult = validatePaymentTypeAndProduct(object);
        if (paymentTypeAndProductValidationResult.isNotValid()) {
            return paymentTypeAndProductValidationResult;
        }

        TppInfo tppInfoInPayment = object.getTppInfo();
        ValidationResult tppValidationResult = pisTppInfoValidator.validateTpp(tppInfoInPayment);
        if (tppValidationResult.isNotValid()) {
            return tppValidationResult;
        }

        return executeBusinessValidation(object);
    }

    private ValidationResult validatePaymentTypeAndProduct(T object) {
        PisCommonPaymentResponse paymentResponse = object.getPisCommonPaymentResponse();

        if (!object.getPaymentType().equals(paymentResponse.getPaymentType())) {
            log.info("Payment ID: [{}]. Payment validation has failed: payment type [{}] is incorrect", paymentResponse.getExternalId(), object.getPaymentType());
            return ValidationResult.invalid(ErrorType.PIS_405, of(MessageErrorCode.SERVICE_INVALID_405_FOR_PAYMENT));
        }

        if (!object.getPaymentProduct().equals(paymentResponse.getPaymentProduct())) {
            log.info("Payment ID: [{}]. Payment validation has failed: payment product [{}] is incorrect", paymentResponse.getExternalId(), object.getPaymentProduct());
            return ValidationResult.invalid(ErrorType.PIS_403, of(MessageErrorCode.PRODUCT_INVALID_FOR_PAYMENT));
        }

        return ValidationResult.valid();
    }

    /**
     * Executes request-specific business validation
     *
     * @param paymentObject payment object to be validated
     * @return valid result if the object is valid, invalid result with appropriate error otherwise
     */
    protected abstract ValidationResult executeBusinessValidation(T paymentObject);

    @Autowired
    public void setPisValidators(PisTppInfoValidator pisTppInfoValidator) {
        this.pisTppInfoValidator = pisTppInfoValidator;
    }
}
