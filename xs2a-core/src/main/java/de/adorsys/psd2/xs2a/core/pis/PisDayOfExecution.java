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

import java.util.Arrays;
import java.util.Optional;

/**
 * Day of execution as string in the form DD.  This string consists always of two characters.  31 is ultimo of the
 * month.
 */
public enum PisDayOfExecution {
    DAY_1("1"),

    DAY_2("2"),

    DAY_3("3"),

    DAY_4("4"),

    DAY_5("5"),

    DAY_6("6"),

    DAY_7("7"),

    DAY_8("8"),

    DAY_9("9"),

    DAY_10("10"),

    DAY_11("11"),

    DAY_12("12"),

    DAY_13("13"),

    DAY_14("14"),

    DAY_15("15"),

    DAY_16("16"),

    DAY_17("17"),

    DAY_18("18"),

    DAY_19("19"),

    DAY_20("20"),

    DAY_21("21"),

    DAY_22("22"),

    DAY_23("23"),

    DAY_24("24"),

    DAY_25("25"),

    DAY_26("26"),

    DAY_27("27"),

    DAY_28("28"),

    DAY_29("29"),

    DAY_30("30"),

    DAY_31("31");

    private String value;

    PisDayOfExecution(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Optional<PisDayOfExecution> getByValue(String value) {
        return Optional.ofNullable(fromValue(value));
    }

    @JsonCreator
    public static PisDayOfExecution fromValue(String value) {
        return Arrays.stream(values()).filter(doe -> doe.getValue().equals(value)).findFirst().orElse(null);
    }

    @Override
    @JsonValue
    public String toString() {
        return value;
    }
}

