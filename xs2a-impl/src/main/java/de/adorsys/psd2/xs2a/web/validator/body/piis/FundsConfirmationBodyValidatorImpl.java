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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.model.Amount;
import de.adorsys.psd2.model.ConfirmationOfFunds;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.body.AbstractBodyValidatorImpl;
import de.adorsys.psd2.xs2a.web.validator.body.AccountReferenceValidator;
import de.adorsys.psd2.xs2a.web.validator.body.AmountValidator;
import de.adorsys.psd2.xs2a.web.validator.body.CurrencyValidator;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Component
public class FundsConfirmationBodyValidatorImpl extends AbstractBodyValidatorImpl implements FundsConfirmationBodyValidator {

    private final AccountReferenceValidator accountReferenceValidator;
    private final AmountValidator amountValidator;
    private final CurrencyValidator currencyValidator;

    public FundsConfirmationBodyValidatorImpl(ErrorBuildingService errorBuildingService, ObjectMapper objectMapper, AccountReferenceValidator accountReferenceValidator, AmountValidator amountValidator, CurrencyValidator currencyValidator) {
        super(errorBuildingService, objectMapper);
        this.accountReferenceValidator = accountReferenceValidator;
        this.amountValidator = amountValidator;
        this.currencyValidator = currencyValidator;
    }

    @Override
    public void validate(HttpServletRequest request, MessageError messageError) {
        Optional<ConfirmationOfFunds> confirmationOfFundsOptional = mapBodyToInstance(request, messageError, ConfirmationOfFunds.class);

        // In case of wrong JSON - we don't proceed the inner fields validation.
        if (!confirmationOfFundsOptional.isPresent()) {
            return;
        }

        ConfirmationOfFunds confirmationOfFunds = confirmationOfFundsOptional.get();

        if (confirmationOfFunds.getAccount() == null) {
            errorBuildingService.enrichMessageError(messageError, "Value 'access' should not be null");
        } else {
            accountReferenceValidator.validate(confirmationOfFunds.getAccount(), messageError);
        }

        if (confirmationOfFunds.getInstructedAmount() == null) {
            errorBuildingService.enrichMessageError(messageError, "Value 'instructedAmount' should not be null");
        } else {
            validateAmount(confirmationOfFunds.getInstructedAmount(), messageError);
        }
    }

    private void validateAmount(Amount instructedAmount, MessageError messageError) {
        currencyValidator.validateCurrency(instructedAmount.getCurrency(), messageError);
        amountValidator.validateAmount(instructedAmount.getAmount(), messageError);
    }
}
