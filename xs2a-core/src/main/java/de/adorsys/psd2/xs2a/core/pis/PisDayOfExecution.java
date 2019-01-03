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

package de.adorsys.psd2.xs2a.core.pis;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Day of execution as string in the form DD.  This string consists always of two characters.  31 is ultimo of the
 * month.
 */
public enum PisDayOfExecution {
    _01("01"), _02("02"), _03("03"), _04("04"), _05("05"), _06("06"), _07("07"), _08("08"), _09("09"), _10("10"),
    _11("11"), _12("12"), _13("13"), _14("14"), _15("15"), _16("16"), _17("17"), _18("18"), _19("19"), _20("20"),
    _21("21"), _22("22"), _23("23"), _24("24"), _25("25"), _26("26"), _27("27"), _28("28"), _29("29"), _30("30"),
    _31("31");
    private String value;

    private final static Map<String, PisDayOfExecution> container = new HashMap<>();

    static {
        for (PisDayOfExecution day : values()) {
            container.put(day.getValue(), day);
        }
    }

    @JsonCreator
    PisDayOfExecution(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static Optional<PisDayOfExecution> getByValue(String name) {
        return Optional.ofNullable(container.get(name));
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}

