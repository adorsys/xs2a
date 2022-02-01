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

package de.adorsys.psd2.xs2a.service.validator.pis.authorisation;

import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationServiceType;
import de.adorsys.psd2.xs2a.domain.consent.pis.PaymentAuthorisationParameters;
import de.adorsys.psd2.xs2a.service.validator.PisEndpointAccessCheckerService;
import de.adorsys.psd2.xs2a.service.validator.PisPsuDataUpdateAuthorisationCheckerValidator;
import de.adorsys.psd2.xs2a.service.validator.authorisation.AuthorisationStageCheckValidator;
import de.adorsys.psd2.xs2a.service.validator.pis.AbstractPisValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.RESOURCE_UNKNOWN_403;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.SERVICE_BLOCKED;

/**
 * Common validator for validating update PSU data in payments and executing request-specific business validation afterwards.
 * Should be used for all update PIS PSU data requests.
 *
 * @param <T> type of object to be checked
 */
@Slf4j
@Component
public abstract class AbstractUpdatePisPsuDataValidator<T extends UpdatePisPsuDataPO> extends AbstractPisValidator<T> {
    private final PisEndpointAccessCheckerService pisEndpointAccessCheckerService;
    private final PisAuthorisationValidator pisAuthorisationValidator;
    private final PisAuthorisationStatusValidator pisAuthorisationStatusValidator;
    private final PisPsuDataUpdateAuthorisationCheckerValidator pisPsuDataUpdateAuthorisationCheckerValidator;
    private final AuthorisationStageCheckValidator authorisationStageCheckValidator;

    protected AbstractUpdatePisPsuDataValidator(PisEndpointAccessCheckerService pisEndpointAccessCheckerService,
                                             PisAuthorisationValidator pisAuthorisationValidator,
                                             PisAuthorisationStatusValidator pisAuthorisationStatusValidator,
                                             PisPsuDataUpdateAuthorisationCheckerValidator pisPsuDataUpdateAuthorisationCheckerValidator,
                                             AuthorisationStageCheckValidator authorisationStageCheckValidator) {
        this.pisEndpointAccessCheckerService = pisEndpointAccessCheckerService;
        this.pisAuthorisationValidator = pisAuthorisationValidator;
        this.pisAuthorisationStatusValidator = pisAuthorisationStatusValidator;
        this.pisPsuDataUpdateAuthorisationCheckerValidator = pisPsuDataUpdateAuthorisationCheckerValidator;
        this.authorisationStageCheckValidator = authorisationStageCheckValidator;
    }

    /**
     * Validates update PSU Data in payment authorisation request by checking whether:
     * <ul>
     * <li>endpoint is accessible for given authorisation</li>
     * <li>payment is not expired</li>
     * </ul>
     *
     * @param paymentObject payment information object
     * @return valid result if the payment is valid, invalid result with appropriate error otherwise
     */
    @Override
    protected ValidationResult executeBusinessValidation(UpdatePisPsuDataPO paymentObject) {
        PaymentAuthorisationParameters request = paymentObject.getUpdateRequest();
        String authorisationId = request.getAuthorisationId();
        boolean confirmationCodeReceived = StringUtils.isNotBlank(request.getConfirmationCode());

        if (!pisEndpointAccessCheckerService.isEndpointAccessible(authorisationId, confirmationCodeReceived)) {
            log.info("Authorisation ID: [{}]. Updating PIS initiation authorisation PSU Data  has failed: endpoint is not accessible for authorisation", authorisationId);
            return ValidationResult.invalid(ErrorType.PIS_403, SERVICE_BLOCKED);
        }

        ValidationResult transactionStatusValidationResult = validateTransactionStatus(paymentObject);
        if (transactionStatusValidationResult.isNotValid()) {
            return transactionStatusValidationResult;
        }

        PisCommonPaymentResponse pisCommonPaymentResponse = paymentObject.getPisCommonPaymentResponse();

        ValidationResult authorisationValidationResult = pisAuthorisationValidator.validate(authorisationId, pisCommonPaymentResponse);
        if (authorisationValidationResult.isNotValid()) {
            return authorisationValidationResult;
        }

        Optional<Authorisation> authorisationOptional = pisCommonPaymentResponse.findAuthorisationInPayment(authorisationId);

        if (authorisationOptional.isEmpty()) {
            return ValidationResult.invalid(ErrorType.PIS_403, RESOURCE_UNKNOWN_403);
        }

        Authorisation authorisation = authorisationOptional.get();

        ValidationResult validationResult = pisPsuDataUpdateAuthorisationCheckerValidator
                                                .validate(request.getPsuData(), authorisation.getPsuIdData());

        if (validationResult.isNotValid()) {
            return validationResult;
        }

        ValidationResult authorisationStatusValidationResult = pisAuthorisationStatusValidator.validate(authorisation.getScaStatus(), confirmationCodeReceived);
        if (authorisationStatusValidationResult.isNotValid()) {
            return authorisationStatusValidationResult;
        }

        ValidationResult authorisationStageCheckValidatorResult = authorisationStageCheckValidator.validate(request, authorisation.getScaStatus(), getAuthorisationServiceType());
        if (authorisationStageCheckValidatorResult.isNotValid()) {
            return authorisationStageCheckValidatorResult;
        }

        return ValidationResult.valid();
    }

    protected abstract ValidationResult validateTransactionStatus(UpdatePisPsuDataPO paymentObject);

    protected abstract AuthorisationServiceType getAuthorisationServiceType();
}
