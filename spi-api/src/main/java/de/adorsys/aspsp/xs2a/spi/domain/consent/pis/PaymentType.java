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

package de.adorsys.aspsp.xs2a.spi.domain.consent.pis;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum PaymentType {
    BULK("bulk"),
    PERIODIC("periodic"),
    FUTURE_DATED("delayed");

    private static final Map<String, PaymentType> container = new HashMap<>();

    static {
        for (PaymentType type : values()) {
            container.put(type.getValue(), type);
        }
    }

    private String value;


    PaymentType(String value) {
        this.value = value;
    }

    public static Optional<PaymentType> getByValue(String value) {
        return Optional.ofNullable(container.get(value));
    }

    public String getValue() {
        return value;
    }
}
