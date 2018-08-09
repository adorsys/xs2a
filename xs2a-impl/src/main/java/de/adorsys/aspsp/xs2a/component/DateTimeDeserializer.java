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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;

import static de.adorsys.aspsp.xs2a.web.util.ApiDateConstants.DATE_TIME_PATTERN_LOCAL;
import static de.adorsys.aspsp.xs2a.web.util.ApiDateConstants.DATE_TIME_PATTERN;
import static de.adorsys.aspsp.xs2a.web.util.ApiDateConstants.DATE_TIME_PATTERN_OFFSET;

@Slf4j
public class DateTimeDeserializer extends StdDeserializer<LocalDateTime> {
    private final DateTimeFormatter formatter;

    {
        formatter = new DateTimeFormatterBuilder()
            .appendOptional(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN_OFFSET))
            .appendOptional(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN_LOCAL))
            .appendOptional(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN))
            .toFormatter();
    }

    public DateTimeDeserializer(Class<LocalDateTime> localDateTimeClass) {
        super(localDateTimeClass);
    }

    @Override
    public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
        try {
            String date = jsonParser.getText();
            return LocalDateTime.parse(date, formatter);
        } catch (IOException | DateTimeParseException e) {
            log.error("Unsupported dateTime format!");
        }
        return null;
    }
}
