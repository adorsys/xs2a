/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.exception.model.error415;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

// TODO remove, when specification provide a class for such error code https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/634

/**
 * Message codes defined for AIS for HTTP Error code 415 (INTERNAL_SERVER_ERROR).
 */
public enum MessageCode415AIS {

    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR");

    private String value;

    MessageCode415AIS(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static MessageCode415AIS fromValue(String text) {
        for (MessageCode415AIS b : MessageCode415AIS.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}

