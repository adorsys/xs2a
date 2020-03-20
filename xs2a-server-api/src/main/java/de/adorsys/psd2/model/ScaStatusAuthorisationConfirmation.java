/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * This data element is containing information about the status of the SCA method in an authorisation confirmation response.   The following codes are defined for this data type.    * 'finalised': if the transaction authorisation and confirmation was successfule.   * 'failed': if the transaction authorisation or confirmation was not successful.
 */
public enum ScaStatusAuthorisationConfirmation {

    FINALISED("finalised"),

    FAILED("failed");

    private String value;

    ScaStatusAuthorisationConfirmation(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static ScaStatusAuthorisationConfirmation fromValue(String text) {
        for (ScaStatusAuthorisationConfirmation b : ScaStatusAuthorisationConfirmation.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}

