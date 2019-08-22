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

import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Currency;

@Component
@RequiredArgsConstructor
public class CurrencyValidator {

    private final ErrorBuildingService errorBuildingService;

    public void validateCurrency(String currency, MessageError messageError) {
        if (StringUtils.isEmpty(currency)) {
            errorBuildingService.enrichMessageError(messageError, "Value 'currency' should not be null");
        } else if (!isValidCurrency(currency)) {
            errorBuildingService.enrichMessageError(messageError, "Invalid currency code format");
        }
    }

    private boolean isValidCurrency(String currency) {
        try {
            Currency.getInstance(currency);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }
}
