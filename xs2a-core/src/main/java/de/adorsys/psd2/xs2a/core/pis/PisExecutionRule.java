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

package de.adorsys.psd2.xs2a.core.pis;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Optional;

/**
 * \"following\" or \"preceding\" supported as values. This data attribute defines the behavior when recurring
 * payment dates falls on a weekend or bank holiday. The payment is then executed either the \"preceding\" or
 * \"following\" working day. ASPSP might reject the request due to the communicated value, if rules in
 * Online-Banking are not supporting this execution rule.
 */
public enum PisExecutionRule {
    FOLLOWING("following"), PRECEDING("preceding");

    private String value;

    @JsonCreator
    PisExecutionRule(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static Optional<PisExecutionRule> getByValue(String value) {
        return Arrays.stream(values()).filter(doe -> doe.getValue().equals(value)).findAny();
    }

    @Override
    public String toString() {
        return value;
    }
}

