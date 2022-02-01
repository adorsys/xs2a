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
