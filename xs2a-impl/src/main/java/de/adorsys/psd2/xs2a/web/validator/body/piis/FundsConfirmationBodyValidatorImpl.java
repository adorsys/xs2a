/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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
