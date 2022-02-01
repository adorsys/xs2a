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

package de.adorsys.psd2.xs2a.web.converter;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalDateConverterTest {
    private static final String ISO_DATE_STRING = "2020-04-02";
    private static final LocalDate EXPECTED_LOCAL_DATE = LocalDate.of(2020, 4, 2);
    private static final String MALFORMED_STRING = "malformed body";

    private LocalDateConverter localDateConverter = new LocalDateConverter();

    @Test
    void convert_withCorrectString_shouldReturnObject() {
        // When
        LocalDate actualResult = localDateConverter.convert(ISO_DATE_STRING);

        // Then
        assertEquals(EXPECTED_LOCAL_DATE, actualResult);
    }

    @Test
    void convert_withMalformedString_shouldThrowDateTimeParseException() {
        assertThrows(DateTimeParseException.class, () -> localDateConverter.convert(MALFORMED_STRING));
    }

    @Test
    void convert_DateTimeFormatter() {
        LocalDate convertedDate = localDateConverter.convert("2020-04-02", DateTimeFormatter.ISO_LOCAL_DATE);
        assertEquals(EXPECTED_LOCAL_DATE, convertedDate);

        convertedDate = localDateConverter.convert("20200402", DateTimeFormatter.BASIC_ISO_DATE);
        assertEquals(EXPECTED_LOCAL_DATE, convertedDate);
    }
}
