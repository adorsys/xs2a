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
    RECEIVED("received", false),
    /**
     * The PSU related to the authorisation or cancellation-authorisation resource has
     * been identified.
     */
    PSUIDENTIFIED("psuIdentified", false),
    /**
     * The PSU related to the authorisation or cancellation-authorisation resource
     * has been identified and authenticated e.g. by a password or by an access token.
     */
    PSUAUTHENTICATED("psuAuthenticated", false),
    /**
     * The PSU/TPP has selected the related SCA routine.
     * If the SCA method is chosen implicitly since only one SCA method is available,
     * then this is the first status to be reported instead of {@link #RECEIVED}
     */
    SCAMETHODSELECTED("scaMethodSelected", false),
    /**
     * The addressed SCA routine has been started.
     */
    STARTED("started", false),
    /**
     * The SCA routine has been finalised successfully.
     */
    FINALISED("finalised", true),
    /**
     * The SCA routine failed.
     */
    FAILED("failed", true),
    /**
     * SCA was exempted for the related transaction,
     * the related authorisation is successful.
     */
    EXEMPTED("exempted", true),
    /**
     * Authorisation is technically successfully finalised by the PSU,
     * but the authorisation resource needs a confirmation command by the TPP yet
     */
    UNCONFIRMED("unconfirmed", false);

    private static final Map<String, ScaStatus> HOLDER = new HashMap<>();

    static {
        for (ScaStatus status : ScaStatus.values()) {
            HOLDER.put(status.value.toLowerCase(), status);
        }
    }

    private String value;
    private final boolean finalisedStatus;

    ScaStatus(String value, boolean finalisedStatus) {
        this.value = value;
        this.finalisedStatus = finalisedStatus;
    }

    /**
     * Maps textual representation to ScaStatus enum-value.
     * Mapping is performed case-insensitive.
     *
     * @param text - text to be mapped.
     * @return Enum value mapped. Null otherwise.
     */
    @JsonCreator
    public static ScaStatus fromValue(String text) {
        return HOLDER.get(text.trim().toLowerCase());
    }

    public boolean isFinalisedStatus() {
        return finalisedStatus;
    }

    public boolean isNotFinalisedStatus() {
        return !isFinalisedStatus();
    }

    /**
     * Provides a textual representation to be used i.e. in JSON-Serialization.
     *
     * @return a textual representation according to Berlin Group Implementation Guidelines
     */
    @JsonValue
    public String getValue() {
        return value;
    }
}

