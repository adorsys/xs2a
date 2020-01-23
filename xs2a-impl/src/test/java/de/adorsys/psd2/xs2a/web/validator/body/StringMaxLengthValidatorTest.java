/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.exception.MessageCategory;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.header.ErrorBuildingServiceMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static de.adorsys.psd2.xs2a.web.validator.body.StringMaxLengthValidator.MaxLengthRequirement;
import static org.junit.jupiter.api.Assertions.*;

class StringMaxLengthValidatorTest {

    private StringMaxLengthValidator stringMaxLengthValidator;
    private MessageError messageError;


    @BeforeEach
    void init() {
        messageError = new MessageError();
        ErrorBuildingService errorBuildingService = new ErrorBuildingServiceMock(ErrorType.AIS_400);
        stringMaxLengthValidator = new StringMaxLengthValidator(errorBuildingService);
    }

    @Test
    void validate_success() {
        //Given
        MaxLengthRequirement panField = new MaxLengthRequirement("1234567890", "PAN", 35);

        //When
        stringMaxLengthValidator.validate(panField, messageError);

        //Then
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    void validate_account_empty_field() {
        //Given
        MaxLengthRequirement panField = new MaxLengthRequirement(null, "PAN", 35);

        //When
        stringMaxLengthValidator.validate(panField, messageError);

        //Then
        assertTrue(messageError.getTppMessages().isEmpty());
    }


    @Test
    void validate_long_field_name() {
        //Given
        MaxLengthRequirement panField = new MaxLengthRequirement("1234567890", "PAN", 8);

        //When
        stringMaxLengthValidator.validate(panField, messageError);

        //Then
        assertFalse(messageError.getTppMessages().isEmpty());
        assertEquals(MessageCategory.ERROR, messageError.getTppMessage().getCategory());
        assertEquals(MessageErrorCode.FORMAT_ERROR_OVERSIZE_FIELD, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[] {"PAN", 8}, messageError.getTppMessage().getTextParameters());
    }
}
