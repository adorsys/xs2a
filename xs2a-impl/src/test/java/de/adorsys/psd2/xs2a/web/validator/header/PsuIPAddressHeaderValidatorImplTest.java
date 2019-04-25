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

import static de.adorsys.psd2.xs2a.web.validator.header.AbstractHeaderValidatorImpl.*;
import static de.adorsys.psd2.xs2a.web.validator.header.PsuIPAddressHeaderValidatorImpl.ERROR_TEXT_WRONG_IP_ADDRESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PsuIPAddressHeaderValidatorImplTest {

    private static final String CORRECT_IP_ADDRESS = "192.168.7.40";
    private static final String CORRECT_V6_IP_ADDRESS = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";
    private static final String WRONG_IP_ADDRESS = "683.168.11.56";
    private static final String WRONG_V6_IP_ADDRESS = "2001:0db8:85a3:0000:0n0k:8a2e:0370:7334";

    private PsuIPAddressHeaderValidatorImpl validator;
    private MessageError messageError;
    private Map<String, String> headers;

    @Before
    public void setUp() {
        validator = new PsuIPAddressHeaderValidatorImpl(new ErrorBuildingServiceMock(ErrorType.AIS_400));
        messageError = new MessageError();
        headers = new HashMap<>();
    }

    @Test
    public void validate_success() {
        headers.put(validator.getHeaderName(), CORRECT_IP_ADDRESS);
        validator.validate(headers, messageError);
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    public void validate_ipAddressV6_success() {
        headers.put(validator.getHeaderName(), CORRECT_V6_IP_ADDRESS);
        validator.validate(headers, messageError);
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    public void validate_absentHeader_error() {
        validator.validate(headers, messageError);

        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals(String.format(ERROR_TEXT_ABSENT_HEADER, validator.getHeaderName()), messageError.getTppMessage().getText());
    }

    @Test
    public void validate_nullHeader_error() {
        headers.put(validator.getHeaderName(), null);
        validator.validate(headers, messageError);

        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals(String.format(ERROR_TEXT_NULL_HEADER, validator.getHeaderName()), messageError.getTppMessage().getText());
    }

    @Test
    public void validate_blankHeader_error() {
        headers.put(validator.getHeaderName(), "");
        validator.validate(headers, messageError);

        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals(String.format(ERROR_TEXT_BLANK_HEADER, validator.getHeaderName()), messageError.getTppMessage().getText());
    }

    @Test
    public void validate_ipAddress_error() {
        headers.put(validator.getHeaderName(), WRONG_IP_ADDRESS);
        validator.validate(headers, messageError);

        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals(String.format(ERROR_TEXT_WRONG_IP_ADDRESS, validator.getHeaderName()), messageError.getTppMessage().getText());
    }

    @Test
    public void validate_ipAddressV6_error() {
        headers.put(validator.getHeaderName(), WRONG_V6_IP_ADDRESS);
        validator.validate(headers, messageError);

        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals(String.format(ERROR_TEXT_WRONG_IP_ADDRESS, validator.getHeaderName()), messageError.getTppMessage().getText());
    }
}
