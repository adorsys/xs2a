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

package de.adorsys.psd2.xs2a.domain.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum Xs2aUsageType {
    PRIV("PRIV"),
    ORGA("ORGA");

    private final static Map<String, Xs2aUsageType> container = new HashMap<>();

    static {
        for (Xs2aUsageType usageType : values()) {
            container.put(usageType.getValue(), usageType);
        }
    }

    private String value;

    Xs2aUsageType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @JsonIgnore
    public static Optional<Xs2aUsageType> getByValue(String name) {
        return Optional.ofNullable(container.get(name));
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }
}
