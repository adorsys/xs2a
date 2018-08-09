/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.aspsp.xs2a.component;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;

import static de.adorsys.aspsp.xs2a.web.util.ApiDateConstants.*;
import static org.junit.Assert.assertEquals;

public class DateTimeDeserializerTest {

    private final String UTC_DATETIME = "2018-10-03T23:40:40.324Z";
    private final String WRONG_UTC_DATETIME = "2018-10-03T23:40:40.324";
    private final String OFFSET_DATETIME = "2018-10-03T23:40:40.324-04:00";
    private final String WRONG_OFFSET_DATETIME = "2018-1003T23:40:40.324-04:00";
    private final String LOCAL_DATETIME = "2018-1003T23:40:40.324";
    private final String WRONG_LOCAL_DATETIME = "2018-1003T23:40:40.32454";

    private final LocalDateTime EXPECTED_RESULT = LocalDateTime.parse("2018-10-03T23:40:40.324");
    private DateTimeFormatter FORMATTER;

    @Before
    public void init() {
        FORMATTER = new DateTimeFormatterBuilder()
            .appendOptional(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN))
            .appendOptional(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN_LOCAL))
            .appendOptional(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN_OFFSET))
            .toFormatter();
    }

    @Test
    public void deserialize_Success_UTC_Format() {
        LocalDateTime actualResault = LocalDateTime.parse(UTC_DATETIME, FORMATTER);
        assertEquals(EXPECTED_RESULT, actualResault);
    }

    @Test
    public void deserialize_Success_Offset_Format() {
        LocalDateTime actualResault = LocalDateTime.parse(OFFSET_DATETIME, FORMATTER);
        assertEquals(EXPECTED_RESULT, actualResault);
    }

    @Test
    public void deserialize_Success_Local_Format() {
        LocalDateTime actualResault = LocalDateTime.parse(LOCAL_DATETIME, FORMATTER);
        assertEquals(EXPECTED_RESULT, actualResault);
    }

    @Test(expected = DateTimeParseException.class)
    public void deserialize_Failure_WrongUTCFormat() {
        LocalDateTime.parse(WRONG_UTC_DATETIME, FORMATTER);
    }

    @Test(expected = DateTimeParseException.class)
    public void deserialize_Failure_WrongOffsetFormat() {
        LocalDateTime.parse(WRONG_OFFSET_DATETIME, FORMATTER);
    }

    @Test(expected = DateTimeParseException.class)
    public void deserialize_Failure_WrongLocalFormat() {
        LocalDateTime.parse(WRONG_LOCAL_DATETIME, FORMATTER);
    }
}
