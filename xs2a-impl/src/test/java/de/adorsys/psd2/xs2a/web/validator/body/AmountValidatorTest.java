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

import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.header.ErrorBuildingServiceMock;
import org.junit.Before;
import org.junit.Test;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static org.junit.Assert.*;

public class AmountValidatorTest {

    private static final String CORRECT_AMOUNT = "123.56";
    private static final String WRONG_AMOUNT_1 = "+123.56";
    private static final String WRONG_AMOUNT_2 = "123x56";
    private static final String WRONG_AMOUNT_3 = "123:56";
    private static final String WRONG_AMOUNT_4 = "+rt123..56";
    private static final String WRONG_AMOUNT_5 = "rt123..56";

    private AmountValidator validator;
    private MessageError messageError = new MessageError();

    @Before
    public void setUp() {
        ErrorBuildingService errorBuildingServiceMock = new ErrorBuildingServiceMock(ErrorType.PIIS_400);
        validator = new AmountValidator(errorBuildingServiceMock);
    }

    @Test
    public void doValidation_success() {
        validator.validateAmount(CORRECT_AMOUNT, messageError);
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    public void doValidation_wrong_1() {
        validator.validateAmount(WRONG_AMOUNT_1, messageError);
        assertThatErrorIs(FORMAT_ERROR_WRONG_FORMAT_VALUE, "amount");
    }

    @Test
    public void doValidation_wrong_2() {
        validator.validateAmount(WRONG_AMOUNT_2, messageError);
        assertThatErrorIs(FORMAT_ERROR_WRONG_FORMAT_VALUE, "amount");
    }

    @Test
    public void doValidation_wrong_3() {
        validator.validateAmount(WRONG_AMOUNT_3, messageError);
        assertThatErrorIs(FORMAT_ERROR_WRONG_FORMAT_VALUE, "amount");
    }

    @Test
    public void doValidation_wrong_4() {
        validator.validateAmount(WRONG_AMOUNT_4, messageError);
        assertThatErrorIs(FORMAT_ERROR_WRONG_FORMAT_VALUE, "amount");
    }

    @Test
    public void doValidation_wrong_5() {
        validator.validateAmount(WRONG_AMOUNT_5, messageError);
        assertThatErrorIs(FORMAT_ERROR_WRONG_FORMAT_VALUE, "amount");
    }

    @Test
    public void doValidation_empty() {
        validator.validateAmount("", messageError);
        assertThatErrorIs(FORMAT_ERROR_EMPTY_FIELD, "amount");
    }

    @Test
    public void doValidation_null() {
        validator.validateAmount(null, messageError);
        assertThatErrorIs(FORMAT_ERROR_NULL_VALUE, "amount");
    }

    private void assertThatErrorIs(MessageErrorCode messageErrorCode, String... textParameters) {
        assertFalse(messageError.getTppMessages().isEmpty());
        assertEquals(messageErrorCode, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(textParameters, messageError.getTppMessage().getTextParameters());
    }
}
