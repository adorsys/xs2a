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
