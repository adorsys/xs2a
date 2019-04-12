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

package de.adorsys.psd2.xs2a.web.converter;

import org.junit.Test;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import static org.junit.Assert.assertEquals;

public class LocalDateConverterTest {
    private static final String ISO_DATE_STRING = "2020-04-02";
    private static final LocalDate EXPECTED_LOCAL_DATE = LocalDate.of(2020, 4, 2);
    private static final String MALFORMED_STRING = "malformed body";

    private LocalDateConverter localDateConverter = new LocalDateConverter();

    @Test
    public void convert_withCorrectString_shouldReturnObject() {
        // When
        LocalDate actualResult = localDateConverter.convert(ISO_DATE_STRING);

        // Then
        assertEquals(EXPECTED_LOCAL_DATE, actualResult);
    }

    @Test(expected = DateTimeParseException.class)
    public void convert_withMalformedString_shouldThrowDateTimeParseException() {
        // When
        localDateConverter.convert(MALFORMED_STRING);
    }
}
