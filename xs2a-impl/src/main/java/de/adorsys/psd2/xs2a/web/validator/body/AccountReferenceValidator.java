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

package de.adorsys.psd2.xs2a.web.validator.body;

import de.adorsys.psd2.model.AccountReference;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.ObjectValidator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_INVALID_FIELD;
import static de.adorsys.psd2.xs2a.web.validator.body.StringMaxLengthValidator.MaxLengthRequirement;

@Component
@RequiredArgsConstructor
public class AccountReferenceValidator implements ObjectValidator<AccountReference> {

    private final ErrorBuildingService errorBuildingService;
    private final OptionalFieldMaxLengthValidator optionalFieldMaxLengthValidator;
    private final CurrencyValidator currencyValidator;
    private final IbanValidator ibanValidator;

    @Override
    public void validate(@NotNull AccountReference accountReference, @NotNull MessageError messageError) {
        ibanValidator.validate(accountReference.getIban(), messageError);

        if (StringUtils.isNotBlank(accountReference.getBban()) && !isValidBban(accountReference.getBban())) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_INVALID_FIELD, "BBAN"));
        }
        optionalFieldMaxLengthValidator.validate(new MaxLengthRequirement(accountReference.getPan(),
                                                                          "PAN", 35), messageError);
        optionalFieldMaxLengthValidator.validate(new MaxLengthRequirement(accountReference.getMaskedPan(),
                                                                          "Masked PAN", 35), messageError);
        optionalFieldMaxLengthValidator.validate(new MaxLengthRequirement(accountReference.getMsisdn(),
                                                                          "MSISDN", 35), messageError);

        if (Objects.nonNull(accountReference.getCurrency())) {
            currencyValidator.validateCurrency(accountReference.getCurrency(), messageError);
        }
    }

    private boolean isValidBban(String bban) {
        return normalizeString(bban).length() >= 11
                   && normalizeString(bban).length() <= 28; // Can be extended with aprox 50 country specific masks
    }

    private String normalizeString(String string) {
        return string.replaceAll("[^a-zA-Z0-9]", "");
    }
}
