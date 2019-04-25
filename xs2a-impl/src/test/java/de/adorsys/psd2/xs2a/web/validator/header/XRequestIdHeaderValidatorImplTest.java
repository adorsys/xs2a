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

package de.adorsys.psd2.xs2a.web.validator.header;

import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static de.adorsys.psd2.xs2a.web.validator.header.AbstractHeaderValidatorImpl.*;
import static de.adorsys.psd2.xs2a.web.validator.header.XRequestIdHeaderValidatorImpl.ERROR_TEXT_WRONG_HEADER;
import static org.junit.Assert.*;

public class XRequestIdHeaderValidatorImplTest {

    private static final String X_REQUEST_ID_HEADER = UUID.randomUUID().toString();

    private XRequestIdHeaderValidatorImpl validator;
    private MessageError messageError;
    private Map<String, String> headers;

    @Before
    public void setUp() {
        validator = new XRequestIdHeaderValidatorImpl(new ErrorBuildingServiceMock(ErrorType.AIS_400));
        messageError = new MessageError();
        headers = new HashMap<>();
    }

    @Test
    public void validate_success() {
        headers.put(validator.getHeaderName(), X_REQUEST_ID_HEADER);
        validator.validate(headers, messageError);
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    public void validate_absentHeaderError() {
        validator.validate(headers, messageError);

        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals(String.format(ERROR_TEXT_ABSENT_HEADER, validator.getHeaderName()), messageError.getTppMessage().getText());
    }

    @Test
    public void validate_nullHeaderError() {
        headers.put(validator.getHeaderName(), null);
        validator.validate(headers, messageError);

        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals(String.format(ERROR_TEXT_NULL_HEADER, validator.getHeaderName()), messageError.getTppMessage().getText());
    }

    @Test
    public void validate_blankHeaderError() {
        headers.put(validator.getHeaderName(), "");
        validator.validate(headers, messageError);

        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals(String.format(ERROR_TEXT_BLANK_HEADER, validator.getHeaderName()), messageError.getTppMessage().getText());
    }

    @Test
    public void validate_wrongFormatError() {
        headers.put(validator.getHeaderName(), "wrong_format");
        validator.validate(headers, messageError);

        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals(String.format(ERROR_TEXT_WRONG_HEADER, validator.getHeaderName()), messageError.getTppMessage().getText());
    }
}
