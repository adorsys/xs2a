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

package de.adorsys.psd2.xs2a.web.validator.header;

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Objects;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;

public abstract class AbstractHeaderValidatorImpl {

    protected ErrorBuildingService errorBuildingService;

    AbstractHeaderValidatorImpl(ErrorBuildingService errorBuildingService) {
        this.errorBuildingService = errorBuildingService;
    }

    protected abstract String getHeaderName();

    public MessageError validate(Map<String, String> headers, MessageError messageError) {
        ValidationResult validationResult = validate(headers);

        if (validationResult.isNotValid()) {
            errorBuildingService.enrichMessageError(messageError, validationResult.getMessageError());
        }

        return messageError;
    }

    protected ValidationResult validate(Map<String, String> headers) {
        return checkIfHeaderIsPresented(headers);
    }

    protected ValidationResult checkHeaderContent(Map<String, String> headers) {
        return ValidationResult.valid();
    }

    protected ValidationResult checkIfHeaderIsPresented(Map<String, String> headers) {
        if (!headers.containsKey(getHeaderName())) {
            return ValidationResult.invalid(
                errorBuildingService.buildErrorType(), TppMessageInformation.of(FORMAT_ERROR_ABSENT_HEADER, getHeaderName()));
        }

        String header = headers.get(getHeaderName());
        if (Objects.isNull(header)) {
            return ValidationResult.invalid(
                errorBuildingService.buildErrorType(), TppMessageInformation.of(FORMAT_ERROR_NULL_HEADER, getHeaderName()));
        }

        if (StringUtils.isBlank(header)) {
            return ValidationResult.invalid(
                errorBuildingService.buildErrorType(), TppMessageInformation.of(FORMAT_ERROR_BLANK_HEADER, getHeaderName()));
        }
        return checkHeaderContent(headers);
    }

    MessageError checkBooleanFormat(Map<String, String> headers, MessageError messageError) {
        String header = headers.get(getHeaderName());
        if (Objects.nonNull(header)) {
            Boolean checker = BooleanUtils.toBooleanObject(header);
            if (checker == null) {
                errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_BOOLEAN_VALUE, getHeaderName()));
            }
        }
        return messageError;
    }
}
