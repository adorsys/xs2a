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

package de.adorsys.psd2.mapper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;


@Slf4j
public class DateTimeDeserializer extends StdDeserializer<LocalDateTime> {
    private final DateTimeFormatter formatter;

    public DateTimeDeserializer() {
        super(LocalDateTime.class);
        formatter = new DateTimeFormatterBuilder()
                        .append(DateTimeFormatter.ofPattern(ApiDateConstants.DATE_TIME_PATTERN_LOCAL))
                        .appendOptional(DateTimeFormatter.ofPattern(ApiDateConstants.ZONE_PART))
                        .appendOptional(DateTimeFormatter.ofPattern(ApiDateConstants.OFFSET_PART))
                        .toFormatter();
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
