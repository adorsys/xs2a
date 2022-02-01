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

import de.adorsys.psd2.xs2a.core.domain.MessageCategory;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.ContentType;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HeadersLengthValidatorImplTest {

    private static final String ONE_CHAR = "A";

    private HeadersLengthValidatorImpl validator;
    private MessageError messageError;
    private Map<String, String> headers;

    @BeforeEach
    void setUp() {
        validator = new HeadersLengthValidatorImpl(new ErrorBuildingServiceMock(ErrorType.AIS_400));
        messageError = new MessageError();
        headers = new HashMap<>();
    }

    @Test
    void validate_success() {
        // Given
        headers.put(validator.getHeaderName(), ContentType.JSON.getType());

        // When
        validator.validate(headers, messageError);

        // Then
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    void validate_allLengthsAreCorrect() {
        // Given
        HeadersLengthValidatorImpl.headerMaxLengths.forEach((header, length) -> headers.put(header, StringUtils.repeat(ONE_CHAR, length)));

        // When
        validator.validate(headers, messageError);

        // Then
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    void validate_allLengthsAreExceeded() {
        // Given
        HeadersLengthValidatorImpl.headerMaxLengths.forEach((header, length) -> headers.put(header, StringUtils.repeat(ONE_CHAR, length + 1)));

        // When
        validator.validate(headers, messageError);

        // Then
        assertFalse(messageError.getTppMessages().isEmpty());
        assertEquals(headers.size(), messageError.getTppMessages().size());
        messageError.getTppMessages().forEach(message -> assertEquals(MessageCategory.ERROR, message.getCategory()));
        messageError.getTppMessages().forEach(message -> assertEquals(MessageErrorCode.FORMAT_ERROR_OVERSIZE_HEADER, message.getMessageErrorCode()));
    }

    @Test
    void check_that_headersList_contains_only_lowercase() {
        for (String header : HeadersLengthValidatorImpl.headerMaxLengths.keySet()) {
            assertEquals(header, header.toLowerCase());
        }
    }

}
