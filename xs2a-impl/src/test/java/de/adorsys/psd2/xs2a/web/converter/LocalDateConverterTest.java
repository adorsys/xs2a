/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.web.converter;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalDateConverterTest {
    private static final String ISO_DATE_STRING = "2020-04-02";
    private static final String ISO_OFFSET_DATE_TIME_STRING = "2020-04-02T10:15:30+02:00";
    private static final LocalDate EXPECTED_LOCAL_DATE = LocalDate.of(2020, 4, 2);
    private static final String MALFORMED_STRING = "malformed body";

    private final LocalDateConverter localDateConverter = new LocalDateConverter();

    @Test
    void convert_withIsoDate_shouldReturnLocalDate() {
        // When
        LocalDate actualResult = localDateConverter.convert(ISO_DATE_STRING);

        // Then
        assertEquals(EXPECTED_LOCAL_DATE, actualResult);
    }

    @Test
    void convert_withIsoOffsetDateTime_shouldReturnLocalDate() {
        // When
        LocalDate actualResult = localDateConverter.convert(ISO_OFFSET_DATE_TIME_STRING);

        // Then
        assertEquals(EXPECTED_LOCAL_DATE, actualResult);
    }

    @Test
    void convert_withMalformedString_shouldThrowDateTimeParseException() {
        // Expect
        assertThrows(DateTimeParseException.class, () -> localDateConverter.convert(MALFORMED_STRING));
    }
}
