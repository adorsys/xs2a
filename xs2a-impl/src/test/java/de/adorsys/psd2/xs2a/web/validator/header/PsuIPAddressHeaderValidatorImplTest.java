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

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PsuIPAddressHeaderValidatorImplTest {
    private static final String CORRECT_IP_ADDRESS = "192.168.7.40";
    private static final String CORRECT_V6_IP_ADDRESS = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";
    private static final String WRONG_IP_ADDRESS = "683.168.11.56";
    private static final String WRONG_V6_IP_ADDRESS = "2001:0db8:85a3:0000:0n0k:8a2e:0370:7334";

    private static final String[] PSU_IP_ADDRESS_HEADER_NAME = {"psu-ip-address"};

    private PsuIPAddressHeaderValidatorImpl validator;
    private MessageError messageError;
    private Map<String, String> headers;

    @BeforeEach
    void setUp() {
        validator = new PsuIPAddressHeaderValidatorImpl(new ErrorBuildingServiceMock(ErrorType.AIS_400));
        messageError = new MessageError();
        headers = new HashMap<>();
    }

    @Test
    void validate_success() {
        headers.put(validator.getHeaderName(), CORRECT_IP_ADDRESS);
        validator.validate(headers, messageError);
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    void validate_ipAddressV6_success() {
        headers.put(validator.getHeaderName(), CORRECT_V6_IP_ADDRESS);
        validator.validate(headers, messageError);
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    void validate_absentHeader_error() {
        validator.validate(headers, messageError);

        assertEquals(MessageErrorCode.FORMAT_ERROR_ABSENT_HEADER, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(PSU_IP_ADDRESS_HEADER_NAME, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void validate_nullHeader_error() {
        headers.put(validator.getHeaderName(), null);
        validator.validate(headers, messageError);

        assertEquals(MessageErrorCode.FORMAT_ERROR_NULL_HEADER, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(PSU_IP_ADDRESS_HEADER_NAME, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void validate_blankHeader_error() {
        headers.put(validator.getHeaderName(), "");
        validator.validate(headers, messageError);

        assertEquals(MessageErrorCode.FORMAT_ERROR_BLANK_HEADER, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(PSU_IP_ADDRESS_HEADER_NAME, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void validate_ipAddress_error() {
        headers.put(validator.getHeaderName(), WRONG_IP_ADDRESS);
        validator.validate(headers, messageError);

        assertEquals(MessageErrorCode.FORMAT_ERROR_WRONG_IP_ADDRESS, messageError.getTppMessage().getMessageErrorCode());
    }

    @Test
    void validate_ipAddressV6_error() {
        headers.put(validator.getHeaderName(), WRONG_V6_IP_ADDRESS);
        validator.validate(headers, messageError);

        assertEquals(MessageErrorCode.FORMAT_ERROR_WRONG_IP_ADDRESS, messageError.getTppMessage().getMessageErrorCode());
    }
}
