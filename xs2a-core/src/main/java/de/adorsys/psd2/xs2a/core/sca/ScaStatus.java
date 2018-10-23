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

package de.adorsys.psd2.xs2a.core.sca;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

/**
 * This data element is containing information about the status of the SCA method applied.
 */
public enum ScaStatus {
    /**
     * An authorisation or cancellation-authorisation resource has been created
     * successfully.
     */
    RECEIVED("received"),
    /**
     * The PSU related to the authorisation or cancellation-authorisation resource has
     * been identified.
     */
    PSUIDENTIFIED("psuIdentified"),
    /**
     * The PSU related to the authorisation or cancellation-authorisation resource
     * has been identified and authenticated e.g. by a password or by an access token.
     */
    PSUAUTHENTICATED("psuAuthenticated"),
    /**
     * The PSU/TPP has selected the related SCA routine.
     * If the SCA method is chosen implicitly since only one SCA method is available,
     * then this is the first status to be reported instead of {@link #RECEIVED}
     */
    SCAMETHODSELECTED("scaMethodSelected"),
    /**
     * The addressed SCA routine has been started.
     */
    STARTED("started"),
    /**
     * The SCA routine has been finalised successfully.
     */
    FINALISED("finalised"),
    /**
     * The SCA routine failed.
     */
    FAILED("failed"),
    /**
     * SCA was exempted for the related transaction,
     * the related authorisation is successful.
     */
    EXEMPTED("exempted");

    private static final Map<String, ScaStatus> HOLDER = new HashMap<>();

    static {
        for (ScaStatus status : ScaStatus.values()) {
            HOLDER.put(status.value.toLowerCase(), status);
        }
    }

    private String value;

    ScaStatus(String value) {
        this.value = value;
    }

    /**
     * Maps textual representation to ScaStatus enum-value.
     * Mapping is peformed case-insensetive.
     *
     * @param text - text to be mapped.
     * @return Enum value mapped. Null otherwise.
     */
    @JsonCreator
    public static ScaStatus fromValue(String text) {
        return HOLDER.get(text.trim().toLowerCase());
    }

    /**
     * Provides a textual representation to be used i.e. in JSON-Serialization.
     * @return a textual representation according to Berlin Group Implementation Guidelines
     */
    @JsonValue
    public String getValue() {
        return value;
    }
}

