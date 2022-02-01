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

package de.adorsys.psd2.xs2a.web.validator.body.piis;

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.Amount;
import de.adorsys.psd2.model.ConfirmationOfFunds;
import de.adorsys.psd2.validator.payment.config.ValidationObject;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.body.*;
import de.adorsys.psd2.xs2a.web.validator.body.raw.FieldExtractor;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_NULL_VALUE;

@Component
public class FundsConfirmationBodyValidatorImpl extends AbstractBodyValidatorImpl implements FundsConfirmationBodyValidator {

    private final AccountReferenceValidator accountReferenceValidator;
    private final AmountValidator amountValidator;
    private final CurrencyValidator currencyValidator;
    private final FieldExtractor fieldExtractor;

    public FundsConfirmationBodyValidatorImpl(ErrorBuildingService errorBuildingService, Xs2aObjectMapper xs2aObjectMapper,
                                              AccountReferenceValidator accountReferenceValidator, AmountValidator amountValidator,
                                              CurrencyValidator currencyValidator, FieldExtractor fieldExtractor,
                                              FieldLengthValidator fieldLengthValidator) {
        super(errorBuildingService, xs2aObjectMapper, fieldLengthValidator);
        this.accountReferenceValidator = accountReferenceValidator;
        this.amountValidator = amountValidator;
        this.currencyValidator = currencyValidator;
        this.fieldExtractor = fieldExtractor;
    }

    @Override
    public MessageError validate(HttpServletRequest request, MessageError messageError) {
        Optional<ConfirmationOfFunds> confirmationOfFundsOptional = fieldExtractor.mapBodyToInstance(request, messageError, ConfirmationOfFunds.class);

        // In case of wrong JSON - we don't proceed to the inner fields validation.
        if (confirmationOfFundsOptional.isEmpty()) {
            return messageError;
        }

        ConfirmationOfFunds confirmationOfFunds = confirmationOfFundsOptional.get();

        checkFieldForMaxLength(confirmationOfFunds.getCardNumber(), "cardNumber", new ValidationObject(35), messageError);

        if (confirmationOfFunds.getAccount() == null) { //NOSONAR
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_NULL_VALUE, "access"));
        } else {
            accountReferenceValidator.validate(confirmationOfFunds.getAccount(), messageError);
        }

        if (confirmationOfFunds.getInstructedAmount() == null) { //NOSONAR
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_NULL_VALUE, "instructedAmount"));
        } else {
            validateAmount(confirmationOfFunds.getInstructedAmount(), messageError);
        }

        return messageError;
    }

    private void validateAmount(Amount instructedAmount, MessageError messageError) {
        currencyValidator.validateCurrency(instructedAmount.getCurrency(), messageError);
        amountValidator.validateAmount(instructedAmount.getAmount(), messageError);
    }
}
