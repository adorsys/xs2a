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
import de.adorsys.psd2.validator.payment.config.ValidationObject;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import org.apache.commons.collections.CollectionUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * Class with common functionality (AIS and PIS) for bodies validating.
 */
public abstract class AbstractBodyValidatorImpl implements BodyValidator {

    protected ErrorBuildingService errorBuildingService;
    protected Xs2aObjectMapper xs2aObjectMapper;
    private final FieldLengthValidator fieldLengthValidator;

    protected AbstractBodyValidatorImpl(ErrorBuildingService errorBuildingService, Xs2aObjectMapper xs2aObjectMapper,
                                        FieldLengthValidator fieldLengthValidator) {
        this.errorBuildingService = errorBuildingService;
        this.xs2aObjectMapper = xs2aObjectMapper;
        this.fieldLengthValidator = fieldLengthValidator;
    }

    protected MessageError validateBodyFields(HttpServletRequest request, MessageError messageError) {
        return messageError;
    }

    protected MessageError validateRawData(HttpServletRequest request, MessageError messageError) {
        return messageError;
    }

    @Override
    public MessageError validate(HttpServletRequest request, MessageError messageError) {
        MessageError result = validateRawData(request, messageError);
        if (CollectionUtils.isEmpty(result.getTppMessages())) {
            result = validateBodyFields(request, result);
        }

        return result;
    }

    protected void checkFieldForMaxLength(String fieldToCheck, String fieldName, ValidationObject validationObject, MessageError messageError) {
        fieldLengthValidator.checkFieldForMaxLength(fieldToCheck, fieldName, validationObject, messageError);
    }

    protected String extractErrorField(String message) {
        return message.split("\"")[1];
    }
}
