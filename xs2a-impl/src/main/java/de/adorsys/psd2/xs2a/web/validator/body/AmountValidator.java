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

import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class AmountValidator {

    private static final String CORRECT_AMOUNT_REGEX = "-?[0-9]{1,14}([.]{1}[0-9]{1,3})?";
    private final ErrorBuildingService errorBuildingService;

    public void validateAmount(String amount, MessageError messageError) {
        if (amount == null) {
            errorBuildingService.enrichMessageError(messageError, "Value 'amount' should not be null");
        } else if (StringUtils.isBlank(amount)) {
            errorBuildingService.enrichMessageError(messageError, "Value 'amount' should not be empty");
        } else if (!Pattern.matches(CORRECT_AMOUNT_REGEX, amount)) {
            errorBuildingService.enrichMessageError(messageError, "Value 'amount' has wrong format");
        }
    }
}
