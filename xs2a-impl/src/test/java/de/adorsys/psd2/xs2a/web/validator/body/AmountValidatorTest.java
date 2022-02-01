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

import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.header.ErrorBuildingServiceMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;

class AmountValidatorTest {

    private static final String CORRECT_AMOUNT = "123.56";
    private static final String WRONG_AMOUNT_1 = "+123.56";
    private static final String WRONG_AMOUNT_2 = "123x56";
    private static final String WRONG_AMOUNT_3 = "123:56";
    private static final String WRONG_AMOUNT_4 = "+rt123..56";
    private static final String WRONG_AMOUNT_5 = "rt123..56";

    private AmountValidator validator;
    private MessageError messageError = new MessageError();

    @BeforeEach
    void setUp() {
        ErrorBuildingService errorBuildingServiceMock = new ErrorBuildingServiceMock(ErrorType.PIIS_400);
        validator = new AmountValidator(errorBuildingServiceMock);
    }

    @Test
    void doValidation_success() {
        validator.validateAmount(CORRECT_AMOUNT, messageError);
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    void doValidation_wrong_1() {
        validator.validateAmount(WRONG_AMOUNT_1, messageError);
        assertThatErrorIs(FORMAT_ERROR_WRONG_FORMAT_VALUE, "amount");
    }

    @Test
    void doValidation_wrong_2() {
        validator.validateAmount(WRONG_AMOUNT_2, messageError);
        assertThatErrorIs(FORMAT_ERROR_WRONG_FORMAT_VALUE, "amount");
    }

    @Test
    void doValidation_wrong_3() {
        validator.validateAmount(WRONG_AMOUNT_3, messageError);
        assertThatErrorIs(FORMAT_ERROR_WRONG_FORMAT_VALUE, "amount");
    }

    @Test
    void doValidation_wrong_4() {
        validator.validateAmount(WRONG_AMOUNT_4, messageError);
        assertThatErrorIs(FORMAT_ERROR_WRONG_FORMAT_VALUE, "amount");
    }

    @Test
    void doValidation_wrong_5() {
        validator.validateAmount(WRONG_AMOUNT_5, messageError);
        assertThatErrorIs(FORMAT_ERROR_WRONG_FORMAT_VALUE, "amount");
    }

    @Test
    void doValidation_empty() {
        validator.validateAmount("", messageError);
        assertThatErrorIs(FORMAT_ERROR_EMPTY_FIELD, "amount");
    }

    @Test
    void doValidation_null() {
        validator.validateAmount(null, messageError);
        assertThatErrorIs(FORMAT_ERROR_NULL_VALUE, "amount");
    }

    private void assertThatErrorIs(MessageErrorCode messageErrorCode, String... textParameters) {
        assertFalse(messageError.getTppMessages().isEmpty());
        assertEquals(messageErrorCode, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(textParameters, messageError.getTppMessage().getTextParameters());
    }
}
