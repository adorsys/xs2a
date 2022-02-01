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

import de.adorsys.psd2.validator.payment.config.Occurrence;
import de.adorsys.psd2.validator.payment.config.ValidationObject;
import de.adorsys.psd2.xs2a.core.domain.MessageCategory;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.header.ErrorBuildingServiceMock;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FieldLengthValidatorTest {
    public static final String FIELD_VALUE = "fieldValue";
    public static final String FIELD_NAME = "fieldName";
    private FieldLengthValidator validator;
    private MessageError messageError;

    @BeforeEach
    void setUp() {
        messageError = new MessageError();
        ErrorBuildingService errorBuildingService = new ErrorBuildingServiceMock(ErrorType.AIS_400);
        validator = new FieldLengthValidator(errorBuildingService);
    }

    @Test
    void checkFieldForMaxLength_extraField() {
        // Given
        ValidationObject validationObject = new ValidationObject(Occurrence.NONE, 0);

        // When
        validator.checkFieldForMaxLength(FIELD_VALUE, FIELD_NAME, validationObject, messageError);

        // Then
        assertFalse(messageError.getTppMessages().isEmpty());
        assertEquals(MessageCategory.ERROR, messageError.getTppMessage().getCategory());
        assertEquals(MessageErrorCode.FORMAT_ERROR_EXTRA_FIELD, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[] {FIELD_NAME}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void checkFieldForMaxLength_emptyField() {
        // Given
        ValidationObject validationObject = new ValidationObject(Occurrence.REQUIRED, 50);

        // When
        validator.checkFieldForMaxLength(StringUtils.EMPTY, FIELD_NAME, validationObject, messageError);

        // Then
        assertFalse(messageError.getTppMessages().isEmpty());
        assertEquals(MessageCategory.ERROR, messageError.getTppMessage().getCategory());
        assertEquals(MessageErrorCode.FORMAT_ERROR_EMPTY_FIELD, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[] {FIELD_NAME}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void checkFieldForMaxLength_oversizedField() {
        // Given
        ValidationObject validationObject = new ValidationObject(Occurrence.REQUIRED, 1);

        // When
        validator.checkFieldForMaxLength(FIELD_VALUE, FIELD_NAME, validationObject, messageError);

        // Then
        assertFalse(messageError.getTppMessages().isEmpty());
        assertEquals(MessageCategory.ERROR, messageError.getTppMessage().getCategory());
        assertEquals(MessageErrorCode.FORMAT_ERROR_OVERSIZE_FIELD, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[] {FIELD_NAME, 1}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void checkFieldForMaxLength_requiredOk() {
        // Given
        ValidationObject validationObject = new ValidationObject(Occurrence.REQUIRED, 19);

        // When
        validator.checkFieldForMaxLength(FIELD_VALUE, FIELD_NAME, validationObject, messageError);

        // Then
        assertTrue(messageError.getTppMessages().isEmpty());

    }

    @Test
    void checkFieldForMaxLength_optionalOk() {
        // Given
        ValidationObject validationObject = new ValidationObject(Occurrence.OPTIONAL, 19);

        // When
        validator.checkFieldForMaxLength(FIELD_VALUE, FIELD_NAME, validationObject, messageError);

        // Then
        assertTrue(messageError.getTppMessages().isEmpty());

    }

    @Test
    void checkFieldForMaxLength_optionalBlank() {
        // Given
        ValidationObject validationObject = new ValidationObject(Occurrence.OPTIONAL, 19);

        // When
        validator.checkFieldForMaxLength(StringUtils.EMPTY, FIELD_NAME, validationObject, messageError);

        // Then
        assertTrue(messageError.getTppMessages().isEmpty());

    }

    @Test
    void checkFieldForMaxLength_oversizedOptionalField() {
        // Given
        ValidationObject validationObject = new ValidationObject(Occurrence.OPTIONAL, 1);

        // When
        validator.checkFieldForMaxLength(FIELD_VALUE, FIELD_NAME, validationObject, messageError);

        // Then
        assertFalse(messageError.getTppMessages().isEmpty());
        assertEquals(MessageCategory.ERROR, messageError.getTppMessage().getCategory());
        assertEquals(MessageErrorCode.FORMAT_ERROR_OVERSIZE_FIELD, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[] {FIELD_NAME, 1}, messageError.getTppMessage().getTextParameters());
    }
}
