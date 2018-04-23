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

package de.adorsys.aspsp.xs2a.util;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


/**
 * GSON custom adapter for java.util.Date class that enables GSON to parse dates into UTC TimeZone
 * and not local one.
 * Also supports both date and datetime formats for parsing.
 */
public class GsonUtcDateAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {

    private final DateFormat dateTimeFormat;
    private final DateFormat dateFormat;

    public GsonUtcDateAdapter() {
        dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public synchronized JsonElement serialize(Date date, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(dateTimeFormat.format(date));
    }

    @Override
    public synchronized Date deserialize(JsonElement jsonElement, Type type,
                                         JsonDeserializationContext jsonDeserializationContext
                                        ) {
        try {
            String asString = jsonElement.getAsString();
            if (asString.matches("[0-9]{2,4}-[0-9]{1,2}-[0-9]{1,2}")) {
                return dateFormat.parse(asString);
            }
            return dateTimeFormat.parse(asString);
        } catch (ParseException e) {
            throw new JsonParseException(e);
        }
    }
}
