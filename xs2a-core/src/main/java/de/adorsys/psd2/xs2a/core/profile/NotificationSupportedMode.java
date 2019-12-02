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

package de.adorsys.psd2.xs2a.core.profile;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;

public enum NotificationSupportedMode {

    SCA("sca"),
    PROCESS("process"),
    LAST("last"),
    NONE("none");

    private String value;

    NotificationSupportedMode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @JsonCreator
    public static NotificationSupportedMode getByValue(String value) {
        return Arrays.stream(values()).filter(mode -> mode.getValue().equalsIgnoreCase(value)).findFirst().orElse(null);
    }

}
