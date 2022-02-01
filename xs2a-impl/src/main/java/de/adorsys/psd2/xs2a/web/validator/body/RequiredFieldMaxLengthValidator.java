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

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageError;
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
