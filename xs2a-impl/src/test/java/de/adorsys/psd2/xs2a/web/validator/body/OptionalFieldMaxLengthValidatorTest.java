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

import de.adorsys.psd2.xs2a.core.domain.MessageCategory;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.header.ErrorBuildingServiceMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static de.adorsys.psd2.xs2a.web.validator.body.StringMaxLengthValidator.MaxLengthRequirement;
import static org.junit.jupiter.api.Assertions.*;

class OptionalFieldMaxLengthValidatorTest {
    private OptionalFieldMaxLengthValidator optionalFieldMaxLengthValidator;
    private MessageError messageError;

    @BeforeEach
    void init() {
        messageError = new MessageError();
        ErrorBuildingService errorBuildingService = new ErrorBuildingServiceMock(ErrorType.AIS_400);
        StringMaxLengthValidator stringMaxLengthValidator = new StringMaxLengthValidator(errorBuildingService);
        optionalFieldMaxLengthValidator = new OptionalFieldMaxLengthValidator(stringMaxLengthValidator);
    }

    @Test
    void validate_success() {
        //Given
        MaxLengthRequirement panField = new MaxLengthRequirement("1234567890", "PAN", 35);

        //When
        optionalFieldMaxLengthValidator.validate(panField, messageError);

        //Then
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    void validate_account_empty_field() {
        //Given
        MaxLengthRequirement panField = new MaxLengthRequirement(null, "PAN", 35);

        //When
        optionalFieldMaxLengthValidator.validate(panField, messageError);

        //Then
        assertTrue(messageError.getTppMessages().isEmpty());
    }


    @Test
    void validate_long_field_name() {
        //Given
        MaxLengthRequirement panField = new MaxLengthRequirement("1234567890", "PAN", 7);

        //When
        optionalFieldMaxLengthValidator.validate(panField, messageError);

        //Then
        assertFalse(messageError.getTppMessages().isEmpty());
        assertEquals(MessageCategory.ERROR, messageError.getTppMessage().getCategory());
        assertEquals(MessageErrorCode.FORMAT_ERROR_OVERSIZE_FIELD, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[] {"PAN", 7}, messageError.getTppMessage().getTextParameters());
    }
}
