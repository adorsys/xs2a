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
