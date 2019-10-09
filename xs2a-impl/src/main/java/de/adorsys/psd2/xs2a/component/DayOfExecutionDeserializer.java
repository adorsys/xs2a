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

package de.adorsys.psd2.xs2a.component;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import de.adorsys.psd2.model.DayOfExecution;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.format.DateTimeParseException;

@Slf4j
public class DayOfExecutionDeserializer extends StdDeserializer<DayOfExecution> {

    public DayOfExecutionDeserializer() {
        super(DayOfExecution.class);
    }

    @Override
    public DayOfExecution deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
        try {
            String date = jsonParser.getText();
            // Remove leading zeroes
            return DayOfExecution.fromValue(date.replaceFirst("^0+(?!$)", ""));
        } catch (IOException | DateTimeParseException e) {
            log.error("Unsupported dayOfExecution format!");
        }
        return null;
    }
}
