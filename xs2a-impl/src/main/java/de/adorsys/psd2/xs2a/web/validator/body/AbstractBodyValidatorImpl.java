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

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.body.payment.config.ValidationObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;

/**
 * Class with common functionality (AIS and PIS) for bodies validating.
 */
public class AbstractBodyValidatorImpl implements BodyValidator {

    protected ErrorBuildingService errorBuildingService;
    protected Xs2aObjectMapper xs2aObjectMapper;

    protected AbstractBodyValidatorImpl(ErrorBuildingService errorBuildingService, Xs2aObjectMapper xs2aObjectMapper) {
        this.errorBuildingService = errorBuildingService;
        this.xs2aObjectMapper = xs2aObjectMapper;
    }

    protected void validateBodyFields(HttpServletRequest request, MessageError messageError) {
    }

    protected void validateRawData(HttpServletRequest request, MessageError messageError) {
    }

    @Override
    public void validate(HttpServletRequest request, MessageError messageError) {
        validateRawData(request, messageError);
        if (CollectionUtils.isEmpty(messageError.getTppMessages())) {
            validateBodyFields(request, messageError);
        }
    }

    protected void checkFieldForMaxLength(String fieldToCheck, String fieldName, ValidationObject validationObject, MessageError messageError) {
        if (validationObject.isNone() && StringUtils.isNotBlank(fieldToCheck)) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_EXTRA_FIELD, fieldName));
        } else if (validationObject.isRequired()) {
            if (StringUtils.isBlank(fieldToCheck)) {
                errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_EMPTY_FIELD, fieldName));
            } else {
                checkFieldForMaxLength(fieldToCheck, fieldName, validationObject.getMaxLength(), messageError);
            }
        } else if (validationObject.isOptional() && StringUtils.isNotBlank(fieldToCheck)) {
            checkFieldForMaxLength(fieldToCheck, fieldName, validationObject.getMaxLength(), messageError);
        }
    }

    private void checkFieldForMaxLength(@NotNull String fieldToCheck, String fieldName, int maxLength, MessageError messageError) {
        if (fieldToCheck.length() > maxLength) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_OVERSIZE_FIELD, fieldName, maxLength));
        }
    }

    protected <T> Optional<T> mapBodyToInstance(HttpServletRequest request, MessageError messageError, Class<T> clazz) {
        try {
            return Optional.of(xs2aObjectMapper.readValue(request.getInputStream(), clazz));
        } catch (IOException e) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_DESERIALIZATION_FAIL));
        }

        return Optional.empty();
    }

    protected String extractErrorField(String message) {
        return message.split("\"")[1];
    }
}
