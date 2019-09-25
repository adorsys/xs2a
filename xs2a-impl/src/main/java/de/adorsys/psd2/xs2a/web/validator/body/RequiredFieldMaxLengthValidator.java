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

import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.ObjectValidator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_EMPTY_FIELD;

@Component
@RequiredArgsConstructor
public class RequiredFieldMaxLengthValidator implements ObjectValidator<StringMaxLengthValidator.MaxLengthRequirement> {

    private final StringMaxLengthValidator stringMaxLengthValidator;
    private final ErrorBuildingService errorBuildingService;

    @Override
    public void validate(@NotNull StringMaxLengthValidator.MaxLengthRequirement object, @NotNull MessageError messageError) {
        if (StringUtils.isBlank(object.getField())) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_EMPTY_FIELD, object.getFieldName()));
        } else {
            stringMaxLengthValidator.validate(object, messageError);
        }
    }
}
