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

package de.adorsys.psd2.aspsp.mock.api.payment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * \"following\" or \"preceeding\" supported as values. This data attribute defines the behavior when recurring
 * payment dates falls on a weekend or bank holiday. The payment is then executed either the \"preceeding\" or
 * \"following\" working day. ASPSP might reject the request due to the communicated value, if rules in
 * Online-Banking are not supporting this execution rule.
 */
public enum AspspExecutionRule {
    FOLLOWING("following"), PRECEEDING("preceeding");
    private String value;

    private final static Map<String, AspspExecutionRule> container = new HashMap<>();

    static {
        for (AspspExecutionRule rule : values()) {
            container.put(rule.getValue(), rule);
        }
    }

    @JsonCreator
    AspspExecutionRule(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static Optional<AspspExecutionRule> getByValue(String name) {
        return Optional.ofNullable(container.get(name));
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}

