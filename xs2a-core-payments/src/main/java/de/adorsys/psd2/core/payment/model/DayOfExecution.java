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

package de.adorsys.psd2.core.payment.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DayOfExecution {
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

    private final String value;

    DayOfExecution(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static DayOfExecution fromValue(String text) {
        for (DayOfExecution b : DayOfExecution.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}

