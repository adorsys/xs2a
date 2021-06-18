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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class PsuDeviceIdHeaderValidatorImplTest {
    private static final String CORRECT_PSU_DEVICE_ID = "99391c7e-ad88-49ec-a2ad-99ddcb1f7723";
    private static final String WRONG_PSU_DEVICE_ID_1 = "99391ce-ad88-49ec-a2ad-99ddcb1f7723";
    private static final String WRONG_PSU_DEVICE_ID_2 = "99391c7e-ad884-49ec-a2ad-99ddcb1f7723";
    private static final String WRONG_PSU_DEVICE_ID_3 = "99391c7e-ad88-49e-a2ad-99ddcb1f7723";
    private static final String WRONG_PSU_DEVICE_ID_4 = "99391c7e-ad88-49ec-a2ad4-99ddcb1f7723";
    private static final String WRONG_PSU_DEVICE_ID_5 = "99391c7e-ad88-49ec-c2ad-99ddcb1f7723";

    private PsuDeviceIdHeaderValidatorImpl validator;
    private Map<String, String> headers;

    @BeforeEach
    void setUp() {
        validator = new PsuDeviceIdHeaderValidatorImpl(new ErrorBuildingServiceMock(ErrorType.AIS_400));
        headers = new HashMap<>();
    }

    @Test
    void validate_success() {
        //Given
        headers.put(validator.getHeaderName(), CORRECT_PSU_DEVICE_ID);

        //When
        ValidationResult result = validator.validate(headers);

        //Then
        assertTrue(result.isValid());
    }

    @ParameterizedTest
    @ValueSource(strings = {WRONG_PSU_DEVICE_ID_1, WRONG_PSU_DEVICE_ID_2, WRONG_PSU_DEVICE_ID_3, WRONG_PSU_DEVICE_ID_4,
        WRONG_PSU_DEVICE_ID_5})
    void validate_error(String value) {
        //Given
        headers.put(validator.getHeaderName(), value);

        //When
        ValidationResult result = validator.validate(headers);

        //Then
        assertTrue(result.isNotValid());
        assertEquals(MessageErrorCode.FORMAT_ERROR_WRONG_HEADER, result.getMessageError().getTppMessage().getMessageErrorCode());
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
