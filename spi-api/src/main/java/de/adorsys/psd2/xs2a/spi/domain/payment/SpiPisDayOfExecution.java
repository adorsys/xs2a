/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.spi.domain.payment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public enum SpiPisDayOfExecution {
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

    SpiPisDayOfExecution(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Optional<SpiPisDayOfExecution> getByValue(String value) {
        return Optional.ofNullable(fromValue(value));
    }

    @JsonCreator
    public static SpiPisDayOfExecution fromValue(Object value) {
        return Arrays.stream(values())
                   .filter(Objects::nonNull)
                   .filter(doe -> doe.getValue().equals(String.valueOf(value))).findFirst().orElse(null);
    }

    @Override
    @JsonValue
    public String toString() {
        return value;
    }

}
