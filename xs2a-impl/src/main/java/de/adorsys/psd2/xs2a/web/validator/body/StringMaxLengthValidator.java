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
import de.adorsys.psd2.xs2a.web.validator.ObjectValidator;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StringMaxLengthValidator implements ObjectValidator<StringMaxLengthValidator.MaxLengthRequirement> {

    private final ErrorBuildingService errorBuildingService;

    @Override
    public void validate(@NotNull MaxLengthRequirement object, @NotNull MessageError messageError) {
        if (object.getField() != null && object.getField().length() > object.getMaxLength()) {
            String text = String.format("Value '%s' should not be more than %s symbols", object.getFieldName(), object.getMaxLength());
            errorBuildingService.enrichMessageError(messageError, text);
        }
    }

    @Value
    public static class MaxLengthRequirement {
        private String field;
        private String fieldName;
        private int maxLength;
    }
}
