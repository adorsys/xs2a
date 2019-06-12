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

package de.adorsys.psd2.xs2a.domain;

import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class ErrorHolderTest {
    private static final ErrorType ERROR_TYPE = ErrorType.PIS_400;
    private static final MessageErrorCode MESSAGE_ERROR_CODE = MessageErrorCode.PAYMENT_FAILED;
    private static final MessageErrorCode ANOTHER_MESSAGE_ERROR_CODE = MessageErrorCode.EXECUTION_DATE_INVALID;
    private static final String ERROR_MESSAGE = "Some message";
    private static final String ANOTHER_ERROR_MESSAGE = "Another message";

    @Test
    public void build() {
        TppMessageInformation tppMessage = TppMessageInformation.of(MESSAGE_ERROR_CODE, ERROR_MESSAGE);
        TppMessageInformation anotherTppMessage = TppMessageInformation.of(ANOTHER_MESSAGE_ERROR_CODE, ANOTHER_ERROR_MESSAGE);

        ErrorHolder errorHolder = ErrorHolder.builder(ERROR_TYPE)
                                      .tppMessages(tppMessage, anotherTppMessage)
                                      .build();

        assertEquals(ERROR_TYPE, errorHolder.getErrorType());
        assertEquals(Arrays.asList(tppMessage, anotherTppMessage), errorHolder.getTppMessageInformationList());
        assertEquals(MESSAGE_ERROR_CODE, errorHolder.getErrorCode());

        String expectedMessage = String.join(", ", ERROR_MESSAGE, ANOTHER_ERROR_MESSAGE);
        assertEquals(expectedMessage, errorHolder.getMessage());
    }

    @Test
    public void build_withDeprecatedMethods() {
        TppMessageInformation tppMessage = TppMessageInformation.of(MESSAGE_ERROR_CODE, ERROR_MESSAGE);
        TppMessageInformation anotherTppMessage = TppMessageInformation.of(MESSAGE_ERROR_CODE, ANOTHER_ERROR_MESSAGE);

        ErrorHolder build = ErrorHolder.builder(MESSAGE_ERROR_CODE)
                                .errorType(ERROR_TYPE)
                                .messages(Arrays.asList(ERROR_MESSAGE, ANOTHER_ERROR_MESSAGE))
                                .build();

        assertEquals(ERROR_TYPE, build.getErrorType());

        assertEquals(Arrays.asList(tppMessage, anotherTppMessage), build.getTppMessageInformationList());
        assertEquals(MESSAGE_ERROR_CODE, build.getErrorCode());

        String expectedMessage = String.join(", ", ERROR_MESSAGE, ANOTHER_ERROR_MESSAGE);
        assertEquals(expectedMessage, build.getMessage());
    }

    @Test
    public void build_withMultipleTppMessages() {
        TppMessageInformation tppMessageInformation = TppMessageInformation.of(MESSAGE_ERROR_CODE, ERROR_MESSAGE);

        ErrorHolder build = ErrorHolder.builder(MESSAGE_ERROR_CODE)
                                .errorType(ERROR_TYPE)
                                .messages(Collections.singletonList(ERROR_MESSAGE))
                                .build();

        assertEquals(ERROR_TYPE, build.getErrorType());
        assertEquals(Collections.singletonList(tppMessageInformation), build.getTppMessageInformationList());
        assertEquals(MESSAGE_ERROR_CODE, build.getErrorCode());
        assertEquals(ERROR_MESSAGE, build.getMessage());
    }
}
