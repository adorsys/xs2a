/*
 * Copyright 2018-2021 adorsys GmbH & Co KG
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

import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TppNotificationContentPreferredHeaderValidatorImplTest {
    private static final String CORRECT_HEADER = "status=sca, process, last, none";
    private static final String WRONG_HEADER_1 = "status=sca,process,last,unknown";
    private static final String WRONG_HEADER_2 = "status=sca,process,last,no ne";
    private static final String WRONG_HEADER_3 = "status=";

    private TppNotificationContentPreferredHeaderValidatorImpl validator;
    private Map<String, String> headers;

    @BeforeEach
    void setUp() {
        validator = new TppNotificationContentPreferredHeaderValidatorImpl(new ErrorBuildingServiceMock(ErrorType.AIS_400));
        headers = new HashMap<>();
    }

    @Test
    void validate_success() {
        //Given
        headers.put(validator.getHeaderName(), CORRECT_HEADER);

        //When
        ValidationResult result = validator.validate(headers);

        //Then
        assertTrue(result.isValid());
    }

    @ParameterizedTest
    @ValueSource(strings = {WRONG_HEADER_1, WRONG_HEADER_2, WRONG_HEADER_3, StringUtils.EMPTY})
    void validate_error(String value) {
        //Given
        headers.put(validator.getHeaderName(), value);

        //When
        ValidationResult result = validator.validate(headers);

        //Then
        assertTrue(result.isNotValid());
        assertEquals(MessageErrorCode.FORMAT_ERROR_INVALID_NOTIFICATION_MODE, result.getMessageError().getTppMessage().getMessageErrorCode());
    }

    @Test
    void validate_absentHeader() {
        //When
        ValidationResult result = validator.validate(headers);

        //Then
        assertTrue(result.isValid());
    }

    @Test
    void validate_nullHeader() {
        //Given
        headers.put(validator.getHeaderName(), null);

        //When
        ValidationResult result = validator.validate(headers);

        //Then
        assertTrue(result.isValid());
    }

}
