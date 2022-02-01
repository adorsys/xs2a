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

package de.adorsys.psd2.xs2a.web.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

class OffsetDateTimeMapperTest {

    private OffsetDateTimeMapper offsetDateTimeMapper;

    @BeforeEach
    void setUp() {
        offsetDateTimeMapper = new OffsetDateTimeMapper();
    }

    @Test
    void mapToOffsetDateTime_withNull_shouldReturnNull() {
        //When
        OffsetDateTime offsetDateTime = offsetDateTimeMapper.mapToOffsetDateTime(null);
        //Then
        assertNull(offsetDateTime);
    }

    @Test
    void mapToOffsetDateTime_Ok() {
        LocalDateTime localDateTime = LocalDateTime.of(2021, 10, 10, 15, 10);

        OffsetDateTime actual = offsetDateTimeMapper.mapToOffsetDateTime(localDateTime);

        OffsetDateTime expected = OffsetDateTime.from(ZonedDateTime.parse("2021-10-10T15:10", DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.systemDefault())));

        assertThat(actual).isNotNull().isEqualTo(expected);
    }
}
