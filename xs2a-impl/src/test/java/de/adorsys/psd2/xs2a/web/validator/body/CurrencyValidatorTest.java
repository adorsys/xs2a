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
