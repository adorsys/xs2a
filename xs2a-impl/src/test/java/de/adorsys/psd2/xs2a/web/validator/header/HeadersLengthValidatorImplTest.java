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

import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.ContentType;
import de.adorsys.psd2.xs2a.exception.MessageCategory;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aHeaderConstant.HEADERS_MAX_LENGTHS;
import static org.junit.Assert.*;

public class HeadersLengthValidatorImplTest {

    private static final String ONE_CHAR = "A";

    private HeadersLengthValidatorImpl validator;
    private MessageError messageError;
    private Map<String, String> headers;

    @Before
    public void setUp() {
        validator = new HeadersLengthValidatorImpl(new ErrorBuildingServiceMock(ErrorType.AIS_400));
        messageError = new MessageError();
        headers = new HashMap<>();
    }

    @Test
    public void validate_success() {
        // Given
        headers.put(validator.getHeaderName(), ContentType.JSON.getType());

        // When
        validator.validate(headers, messageError);

        // Then
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    public void validate_allLengthsAreCorrect() {
        // Given
        HEADERS_MAX_LENGTHS.forEach((header, length) -> headers.put(header, StringUtils.repeat(ONE_CHAR, length)));

        // When
        validator.validate(headers, messageError);

        // Then
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    public void validate_allLengthsAreExceeded() {
        // Given
        HEADERS_MAX_LENGTHS.forEach((header, length) -> headers.put(header, StringUtils.repeat(ONE_CHAR, length + 1)));

        // When
        validator.validate(headers, messageError);

        // Then
        assertFalse(messageError.getTppMessages().isEmpty());
        assertEquals(headers.size(), messageError.getTppMessages().size());
        messageError.getTppMessages().forEach(message -> assertEquals(MessageCategory.ERROR, message.getCategory()));
        messageError.getTppMessages().forEach(message -> assertEquals(MessageErrorCode.FORMAT_ERROR, message.getMessageErrorCode()));
    }

    @Test
    public void check_that_headersList_contains_only_lowercase() {
        for (String header : HEADERS_MAX_LENGTHS.keySet()) {
            assertEquals(header, header.toLowerCase());
        }
    }

}
