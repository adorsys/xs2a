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

package de.adorsys.psd2.xs2a.service.validator.pis.authorisation.initiation;

import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.OauthPaymentValidator;
import de.adorsys.psd2.xs2a.service.validator.pis.AbstractPisValidator;
import de.adorsys.psd2.xs2a.service.validator.pis.authorisation.PisAuthorisationValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Validator to be used for validating get payment initiation authorisation SCA status request according to some
 * business rules
 */
@Slf4j
@Component
public class GetPaymentInitiationAuthorisationScaStatusValidator extends AbstractPisValidator<GetPaymentInitiationAuthorisationScaStatusPO> {
    private final PisAuthorisationValidator pisAuthorisationValidator;
    private final OauthPaymentValidator oauthPaymentValidator;

    public GetPaymentInitiationAuthorisationScaStatusValidator(PisAuthorisationValidator pisAuthorisationValidator,
                                                               OauthPaymentValidator oauthPaymentValidator) {
        this.pisAuthorisationValidator = pisAuthorisationValidator;
        this.oauthPaymentValidator = oauthPaymentValidator;
    }

    /**
     * Validates get payment initiation authorisation SCA status request
     *
     * @param paymentObject payment information object
     * @return valid result if the payment is valid, invalid result with appropriate error otherwise
     */
    @Override
    protected ValidationResult executeBusinessValidation(GetPaymentInitiationAuthorisationScaStatusPO paymentObject) {
        PisCommonPaymentResponse response = paymentObject.getPisCommonPaymentResponse();
        String authorisationId = paymentObject.getAuthorisationId();

        ValidationResult authorisationValidationResult = pisAuthorisationValidator.validate(authorisationId, response);
        if (authorisationValidationResult.isNotValid()) {
            return authorisationValidationResult;
        }

        ValidationResult oauthPaymentValidationResult = oauthPaymentValidator.validate(paymentObject.getPisCommonPaymentResponse());

        if (oauthPaymentValidationResult.isNotValid()) {
            return oauthPaymentValidationResult;
        }

        return ValidationResult.valid();
    }
}
