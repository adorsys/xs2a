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

package de.adorsys.psd2.xs2a.web.validator.header;

import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AcceptHeaderValidatorImplTest {
    private static final String[] ACCEPT_HEADER_NAME = {"Accept"};

    private AcceptHeaderValidatorImpl validator;
    private MessageError messageError;
    private Map<String, String> headers;

    @BeforeEach
    void setUp() {
        validator = new AcceptHeaderValidatorImpl(new ErrorBuildingServiceMock(ErrorType.AIS_400));
        messageError = new MessageError();
        headers = new HashMap<>();
    }

    @Test
    void validate_headerIsNotPresented_success() {
        validator.validate(headers, messageError);
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    void validate_headerIsPresented_success() {
        headers.put(validator.getHeaderName(), MediaType.APPLICATION_JSON_VALUE);
        validator.validate(headers, messageError);
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    void validate_manyHeaders_success() {
        headers.put(validator.getHeaderName(), "application/json, application/xml");
        validator.validate(headers, messageError);
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    void validate_blankHeader_error() {
        headers.put(validator.getHeaderName(), "");
        validator.validate(headers, messageError);

        assertEquals(MessageErrorCode.FORMAT_ERROR_BLANK_HEADER, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(ACCEPT_HEADER_NAME, messageError.getTppMessage().getTextParameters());
    }
}
