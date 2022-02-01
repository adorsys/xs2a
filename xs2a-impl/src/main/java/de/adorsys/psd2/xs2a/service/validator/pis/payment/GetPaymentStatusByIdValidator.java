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

import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.validator.OauthPaymentValidator;
import de.adorsys.psd2.xs2a.service.validator.pis.AbstractPisValidator;
import org.springframework.stereotype.Component;

/**
 * Validator to be used for validating get payment status by ID request according to some business rules
 */
@Component
public class GetPaymentStatusByIdValidator extends AbstractPisValidator<GetPaymentStatusByIdPO> {
    private final RequestProviderService requestProviderService;
    private final OauthPaymentValidator oauthPaymentValidator;
    private final TransactionStatusAcceptHeaderValidator transactionStatusAcceptHeaderValidator;

    public GetPaymentStatusByIdValidator(RequestProviderService requestProviderService,
                                         OauthPaymentValidator oauthPaymentValidator,
                                         TransactionStatusAcceptHeaderValidator transactionStatusAcceptHeaderValidator) {
        this.requestProviderService = requestProviderService;
        this.oauthPaymentValidator = oauthPaymentValidator;
        this.transactionStatusAcceptHeaderValidator = transactionStatusAcceptHeaderValidator;
    }

    /**
     * Validates get payment status by ID request by checking whether:
     * <ul>
     * <li>accept header in the request is supported by the ASPSP</li>
     * <li>oauth request is valid for given payment</li>
     * </ul>
     *
     * @param paymentObject payment information object
     * @return valid result if the payment is valid, invalid result with appropriate error otherwise
     */
    @Override
    protected ValidationResult executeBusinessValidation(GetPaymentStatusByIdPO paymentObject) {
        ValidationResult transactionStatusFormatValidationResult = transactionStatusAcceptHeaderValidator.validate(requestProviderService.getAcceptHeader());
        if (transactionStatusFormatValidationResult.isNotValid()) {
            return transactionStatusFormatValidationResult;
        }

        ValidationResult oauthPaymentValidationResult = oauthPaymentValidator.validate(paymentObject.getPisCommonPaymentResponse());

        if (oauthPaymentValidationResult.isNotValid()) {
            return oauthPaymentValidationResult;
        }

        return ValidationResult.valid();
    }
}
