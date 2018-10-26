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
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum AspspOtpFormat {
    CHARACTERS("characters"),
    INTEGER("integer");

    private static final Map<String, AspspOtpFormat> container = new HashMap<>();

    static {
        for (AspspOtpFormat otpFormat : values()) {
            container.put(otpFormat.getValue(), otpFormat);
        }
    }

    private String value;

    @JsonCreator
    AspspOtpFormat(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @JsonIgnore
    public static Optional<AspspOtpFormat> getByValue(String value) {
        return Optional.ofNullable(container.get(value));
    }
}
