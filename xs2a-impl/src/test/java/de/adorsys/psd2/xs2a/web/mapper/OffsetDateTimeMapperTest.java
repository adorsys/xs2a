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

package de.adorsys.psd2.xs2a.web.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
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

    @Test
    void mapToOffsetDateTime_withStandardTime_shouldUseCorrectOffset() {
        LocalDateTime winterTime = LocalDateTime.of(2021, 1, 15, 12, 30);

        OffsetDateTime actual = offsetDateTimeMapper.mapToOffsetDateTime(winterTime);
        assertThat(actual).isNotNull();
        assertThat(actual.toLocalDateTime()).isEqualTo(winterTime);
        ZoneId systemZone = ZoneId.systemDefault();
        ZoneOffset expectedOffset = systemZone.getRules().getValidOffsets(winterTime).get(0);
        assertThat(actual.getOffset()).isEqualTo(expectedOffset);
    }

    @Test
    void mapToOffsetDateTime_withSummerTime_shouldUseCorrectOffset() {
        LocalDateTime summerTime = LocalDateTime.of(2021, 7, 15, 12, 30);
        OffsetDateTime actual = offsetDateTimeMapper.mapToOffsetDateTime(summerTime);
        assertThat(actual).isNotNull();
        assertThat(actual.toLocalDateTime()).isEqualTo(summerTime);
        ZoneId systemZone = ZoneId.systemDefault();
        ZoneOffset expectedOffset = systemZone.getRules().getValidOffsets(summerTime).get(0);
        assertThat(actual.getOffset()).isEqualTo(expectedOffset);
    }

    @Test
    void mapToOffsetDateTime_withMinDateTime_shouldHandleEdgeCase() {
        LocalDateTime minDateTime = LocalDateTime.MIN;
        OffsetDateTime actual = offsetDateTimeMapper.mapToOffsetDateTime(minDateTime);
        assertThat(actual).isNotNull();
        assertThat(actual.toLocalDateTime()).isEqualTo(minDateTime);
    }

    @Test
    void mapToOffsetDateTime_withMaxDateTime_shouldHandleEdgeCase() {
        LocalDateTime maxDateTime = LocalDateTime.MAX;
        OffsetDateTime actual = offsetDateTimeMapper.mapToOffsetDateTime(maxDateTime);
        assertThat(actual).isNotNull();
        assertThat(actual.toLocalDateTime()).isEqualTo(maxDateTime);
    }

    @Test
    void mapToOffsetDateTime_shouldPreserveExactTime() {
        LocalDateTime preciseTime = LocalDateTime.of(2025, 11, 17, 14, 30, 45, 123456789);
        OffsetDateTime actual = offsetDateTimeMapper.mapToOffsetDateTime(preciseTime);
        assertThat(actual).isNotNull();
        assertThat(actual.toLocalDateTime()).isEqualTo(preciseTime);
        assertThat(actual.getNano()).isEqualTo(123456789);
    }

    @Test
    void mapToOffsetDateTime_shouldAlwaysReturnFirstValidOffset() {
        LocalDateTime testTime = LocalDateTime.of(2021, 3, 28, 2, 30); // Common DST transition time in Europe
        OffsetDateTime actual = offsetDateTimeMapper.mapToOffsetDateTime(testTime);
        assertThat(actual).isNotNull();
        ZoneId systemZone = ZoneId.systemDefault();
        ZoneOffset expectedOffset = systemZone.getRules().getOffset(actual.toLocalDateTime());
        assertThat(actual.getOffset()).isEqualTo(expectedOffset);
    }

    @Test
    void mapToOffsetDateTime_shouldBeConsistentAcrossMultipleCalls() {
        LocalDateTime testTime = LocalDateTime.of(2021, 6, 15, 10, 30);
        OffsetDateTime first = offsetDateTimeMapper.mapToOffsetDateTime(testTime);
        OffsetDateTime second = offsetDateTimeMapper.mapToOffsetDateTime(testTime);
        OffsetDateTime third = offsetDateTimeMapper.mapToOffsetDateTime(testTime);
        assertThat(first).isEqualTo(second).isEqualTo(third);
        assertThat(first.getOffset()).isEqualTo(second.getOffset()).isEqualTo(third.getOffset());
    }
}
