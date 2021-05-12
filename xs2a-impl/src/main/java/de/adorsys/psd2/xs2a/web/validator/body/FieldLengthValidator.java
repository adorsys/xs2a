/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

import de.adorsys.psd2.validator.payment.config.ValidationObject;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.Objects;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;

@Component
@AllArgsConstructor
public class FieldLengthValidator {
    private final ErrorBuildingService errorBuildingService;

    public void checkFieldForMaxLength(String fieldToCheck, String fieldName, ValidationObject validationObject,
                                       MessageError messageError) {
        if (isFieldExtra(validationObject, fieldToCheck)) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_EXTRA_FIELD, fieldName));
            return;
        }

        if (isFieldMissing(validationObject, fieldToCheck)) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_EMPTY_FIELD, fieldName));
            return;
        }

        if (isFieldPresent(validationObject, fieldToCheck)) {
            checkFieldForMaxLength(fieldToCheck, fieldName, validationObject.getMaxLength(), messageError);
        }
    }

    private void checkFieldForMaxLength(@NotNull String fieldToCheck, String fieldName, int maxLength, MessageError messageError) {
        if (fieldToCheck.length() > maxLength) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_OVERSIZE_FIELD, fieldName, maxLength));
        }
    }

    private boolean isFieldExtra(ValidationObject validationObject, String fieldToCheck) {
        return validationObject.isNone() && Objects.nonNull(fieldToCheck);
    }

    private boolean isFieldMissing(ValidationObject validationObject, String fieldToCheck) {
        return validationObject.isRequired() && StringUtils.isBlank(fieldToCheck);
    }

    private boolean isFieldPresent(ValidationObject validationObject, String fieldToCheck) {
        boolean isNotExtra = validationObject.isRequired() || validationObject.isOptional();
        return isNotExtra && StringUtils.isNotBlank(fieldToCheck);
    }
}
