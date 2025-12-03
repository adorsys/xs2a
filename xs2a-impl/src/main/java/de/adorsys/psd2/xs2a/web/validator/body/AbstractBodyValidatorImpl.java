/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.web.validator.body;

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.validator.payment.config.ValidationObject;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import org.apache.commons.collections4.CollectionUtils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Class with common functionality (AIS and PIS) for bodies validating.
 */
public abstract class AbstractBodyValidatorImpl implements BodyValidator {

    /**
     * Service for building error messages.
     */
    protected ErrorBuildingService errorBuildingService;
    /**
     * Mapper for XS2A objects.
     */
    protected Xs2aObjectMapper xs2aObjectMapper;
    private final FieldLengthValidator fieldLengthValidator;

    /**
     * Constructs a new AbstractBodyValidatorImpl.
     *
     * @param errorBuildingService service for building error messages
     * @param xs2aObjectMapper mapper for XS2A objects
     * @param fieldLengthValidator validator for field length
     */
    protected AbstractBodyValidatorImpl(ErrorBuildingService errorBuildingService, Xs2aObjectMapper xs2aObjectMapper,
                                        FieldLengthValidator fieldLengthValidator) {
        this.errorBuildingService = errorBuildingService;
        this.xs2aObjectMapper = xs2aObjectMapper;
        this.fieldLengthValidator = fieldLengthValidator;
    }

    /**
     * Validates body fields from the HTTP request.
     *
     * @param request the HTTP servlet request
     * @param messageError the message error to populate
     * @return the updated message error
     */
    protected MessageError validateBodyFields(HttpServletRequest request, MessageError messageError) {
        return messageError;
    }

    /**
     * Validates raw data from the HTTP request.
     *
     * @param request the HTTP servlet request
     * @param messageError the message error to populate
     * @return the updated message error
     */
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

    /**
     * Checks if a field exceeds the maximum allowed length.
     *
     * @param fieldToCheck the field value to check
     * @param fieldName the name of the field
     * @param validationObject the validation object containing length constraints
     * @param messageError the message error to populate if validation fails
     */
    protected void checkFieldForMaxLength(String fieldToCheck, String fieldName, ValidationObject validationObject, MessageError messageError) {
        fieldLengthValidator.checkFieldForMaxLength(fieldToCheck, fieldName, validationObject, messageError);
    }

    /**
     * Extracts the error field name from an error message.
     *
     * @param message the error message
     * @return the extracted field name
     */
    protected String extractErrorField(String message) {
        return message.split("\"")[1];
    }
}
