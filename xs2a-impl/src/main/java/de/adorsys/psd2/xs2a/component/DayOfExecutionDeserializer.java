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
