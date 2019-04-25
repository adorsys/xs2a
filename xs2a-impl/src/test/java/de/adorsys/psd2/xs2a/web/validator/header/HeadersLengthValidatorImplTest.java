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

import de.adorsys.psd2.xs2a.domain.ContentType;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aHeaderConstant.HEADERS_MAP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HeadersLengthValidatorImplTest {

    private static final String CORRECT_HEADER_VALUE = "correct_value";
    private static final String TOO_LONG_HEADER_VALUE = "DSFDGHJKLKJHGFCVBNMDSFDGHJKLLKJHGFCVBNMDSFDGHJKLKJHGFCVBNM";

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
        headers.put(validator.getHeaderName(), ContentType.JSON.getType());
        validator.validate(headers, messageError);
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    public void validate_allLengthsAreCorrect() {
        HEADERS_MAP.forEach((length, listOfHeaders) -> {
            Arrays.stream(listOfHeaders)
                .forEach(h -> headers.put(h, CORRECT_HEADER_VALUE));
            validator.validate(headers, messageError);
            assertTrue(messageError.getTppMessages().isEmpty());
        });
    }

    @Test
    public void validate_lengthHasErrors() {
        HEADERS_MAP.forEach((length, listOfHeaders) -> {
            Arrays.stream(listOfHeaders)
                .forEach(h -> headers.put(h, TOO_LONG_HEADER_VALUE));
            validator.validate(headers, messageError);
            assertEquals(listOfHeaders.length, messageError.getTppMessages().size());
        });
    }
}
