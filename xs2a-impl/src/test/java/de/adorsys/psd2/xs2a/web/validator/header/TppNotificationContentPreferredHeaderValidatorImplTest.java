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
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
