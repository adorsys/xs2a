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
