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

package de.adorsys.psd2.xs2a.service.validator.pis.authorisation.cancellation;

import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.authorisation.AuthorisationPsuDataChecker;
import de.adorsys.psd2.xs2a.service.validator.authorisation.AuthorisationStatusChecker;
import de.adorsys.psd2.xs2a.service.validator.pis.AbstractPisValidator;
import org.springframework.stereotype.Component;

import java.util.List;

import static de.adorsys.psd2.xs2a.core.error.ErrorType.PIS_401;
import static de.adorsys.psd2.xs2a.core.error.ErrorType.PIS_409;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.PSU_CREDENTIALS_INVALID;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.STATUS_INVALID;

/**
 * Validator to be used for validating create pis cancellation authorisation request according to some business rules
 */
@Component
public class CreatePisCancellationAuthorisationValidator extends AbstractPisValidator<CreatePisCancellationAuthorisationObject> {
    private final AuthorisationPsuDataChecker authorisationPsuDataChecker;
    private final AuthorisationStatusChecker authorisationStatusChecker;

    public CreatePisCancellationAuthorisationValidator(AuthorisationPsuDataChecker authorisationPsuDataChecker,
                                                       AuthorisationStatusChecker authorisationStatusChecker) {
        this.authorisationPsuDataChecker = authorisationPsuDataChecker;
        this.authorisationStatusChecker = authorisationStatusChecker;
    }

    /**
     * Validates create payment cancellation authorisation request
     *
     * @param createPisCancellationAuthorisationObject payment cancellation authorisation object
     * @return valid result if the payment cancellation authorisation is valid, invalid result with appropriate error otherwise
     */
    @Override
    protected ValidationResult executeBusinessValidation(CreatePisCancellationAuthorisationObject createPisCancellationAuthorisationObject) {

        PsuIdData psuDataFromRequest = createPisCancellationAuthorisationObject.getPsuData();
        PisCommonPaymentResponse pisCommonPaymentResponse = createPisCancellationAuthorisationObject.getPisCommonPaymentResponse();
        List<PsuIdData> psuDataFromDb = pisCommonPaymentResponse.getPsuData();

        if (authorisationPsuDataChecker.isPsuDataWrong(
            pisCommonPaymentResponse.isMultilevelScaRequired(),
            psuDataFromDb,
            psuDataFromRequest)) {

            return ValidationResult.invalid(PIS_401, PSU_CREDENTIALS_INVALID);
        }

        // If the cancellation authorisation for this payment ID and for this PSU ID has status FINALISED or EXEMPTED - return error.
        boolean isFinalised = authorisationStatusChecker.isFinalised(psuDataFromRequest, pisCommonPaymentResponse.getAuthorisations(), AuthorisationType.PIS_CANCELLATION);

        if (isFinalised) {
            return ValidationResult.invalid(PIS_409, STATUS_INVALID);
        }

        return ValidationResult.valid();
    }
}
