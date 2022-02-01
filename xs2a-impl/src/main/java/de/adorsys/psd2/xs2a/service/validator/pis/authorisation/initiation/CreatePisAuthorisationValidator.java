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
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.authorisation.AuthorisationPsuDataChecker;
import de.adorsys.psd2.xs2a.service.validator.authorisation.AuthorisationStatusChecker;
import de.adorsys.psd2.xs2a.service.validator.pis.AbstractPisValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static de.adorsys.psd2.xs2a.core.error.ErrorType.*;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;

/**
 * Validator to be used for validating create PIS authorisation request according to some business rules
 */
@Slf4j
@Component
public class CreatePisAuthorisationValidator extends AbstractPisValidator<CreatePisAuthorisationObject> {

    private final AuthorisationPsuDataChecker authorisationPsuDataChecker;
    private final AuthorisationStatusChecker authorisationStatusChecker;

    public CreatePisAuthorisationValidator(AuthorisationPsuDataChecker authorisationPsuDataChecker,
                                           AuthorisationStatusChecker authorisationStatusChecker) {
        this.authorisationPsuDataChecker = authorisationPsuDataChecker;
        this.authorisationStatusChecker = authorisationStatusChecker;
    }

    /**
     * Validates create PIS authorisation request by checking whether:
     * <ul>
     * <li>payment authorisation PSU data is the same as initial request PSU data</li>
     * <li>payment authorisation is already finalised for this payment and for this PSU ID</li>
     * <li>payment is not expired</li>
     * </ul>
     *
     * @param createPisAuthorisationObject create payment authorisation information object
     * @return valid result if the payment is valid, invalid result with appropriate error otherwise
     */
    @Override
    protected ValidationResult executeBusinessValidation(CreatePisAuthorisationObject createPisAuthorisationObject) {

        PisCommonPaymentResponse pisCommonPaymentResponse = createPisAuthorisationObject.getPisCommonPaymentResponse();
        if (pisCommonPaymentResponse.isSigningBasketBlocked()) {
            return ValidationResult.invalid(PIS_400, RESOURCE_BLOCKED_SB);
        }

        if (pisCommonPaymentResponse.isSigningBasketAuthorised()) {
            return ValidationResult.invalid(PIS_400, STATUS_INVALID);
        }

        PsuIdData psuDataFromRequest = createPisAuthorisationObject.getPsuDataFromRequest();
        List<PsuIdData> psuDataFromDb = createPisAuthorisationObject.getPisCommonPaymentResponse().getPsuData();
        if (authorisationPsuDataChecker.isPsuDataWrong(
            pisCommonPaymentResponse.isMultilevelScaRequired(),
            psuDataFromDb,
            psuDataFromRequest)) {

            return ValidationResult.invalid(PIS_401, PSU_CREDENTIALS_INVALID);
        }

        // If the authorisation for this payment ID and for this PSU ID has status FINALISED or EXEMPTED - return error.
        boolean isFinalised = authorisationStatusChecker.isFinalised(psuDataFromRequest, pisCommonPaymentResponse.getAuthorisations(), AuthorisationType.PIS_CREATION);

        if (isFinalised) {
            return ValidationResult.invalid(PIS_409, STATUS_INVALID);
        }

        if (pisCommonPaymentResponse.getTransactionStatus() == TransactionStatus.RJCT) {
            log.info("Payment ID: [{}]. Creation of PIS authorisation has failed: payment has been rejected", pisCommonPaymentResponse.getExternalId());
            return ValidationResult.invalid(PIS_403, RESOURCE_EXPIRED_403);
        }

        return ValidationResult.valid();
    }

}
