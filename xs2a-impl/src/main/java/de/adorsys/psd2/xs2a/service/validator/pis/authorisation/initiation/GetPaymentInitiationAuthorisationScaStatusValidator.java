/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.validator.pis.authorisation.initiation;

import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.service.validator.OauthPaymentValidator;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
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
