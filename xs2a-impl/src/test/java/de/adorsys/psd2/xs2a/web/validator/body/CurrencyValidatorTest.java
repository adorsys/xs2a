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
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.header.ErrorBuildingServiceMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyValidatorTest {

    private static final String CORRECT_CURRECNY = "UAH";
    private static final String WRONG_CURRENCY = "UZD";
    private static final String[] PARAMETER_TEXT = {"currency"};
    private CurrencyValidator validator;
    private MessageError messageError = new MessageError();

    @BeforeEach
    void setUp() {
        ErrorBuildingService errorBuildingServiceMock = new ErrorBuildingServiceMock(ErrorType.AIS_400);
        validator = new CurrencyValidator(errorBuildingServiceMock);
    }

    @Test
    void doValidation_success() {
        validator.validateCurrency(CORRECT_CURRECNY, messageError);
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    void doValidation_wrong() {
        validator.validateCurrency(WRONG_CURRENCY, messageError);
        assertFalse(messageError.getTppMessages().isEmpty());
        assertEquals(MessageErrorCode.FORMAT_ERROR_WRONG_FORMAT_VALUE, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(PARAMETER_TEXT, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void doValidation_empty() {
        validator.validateCurrency(null, messageError);
        assertFalse(messageError.getTppMessages().isEmpty());
        assertEquals(MessageErrorCode.FORMAT_ERROR_EMPTY_FIELD, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(PARAMETER_TEXT, messageError.getTppMessage().getTextParameters());
    }
}
