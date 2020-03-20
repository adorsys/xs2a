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
 * This data element is containing information about the status of the SCA method applied.   The following codes are defined for this data type.    * 'received':     An authorisation or cancellation-authorisation resource has been created successfully.   * 'psuIdentified':     The PSU related to the authorisation or cancellation-authorisation resource has been identified.   * 'psuAuthenticated':     The PSU related to the authorisation or cancellation-authorisation resource has been identified and authenticated e.g. by a password or by an access token.   * 'scaMethodSelected':     The PSU/TPP has selected the related SCA routine.      If the SCA method is chosen implicitly since only one SCA method is available,      then this is the first status to be reported instead of 'received'.   * 'unconfirmed':     SCA is technically successfully finalised by the PSU, but the authorisation resource needs a confirmation command by the TPP yet.    * 'started':     The addressed SCA routine has been started.   * 'finalised':     The SCA routine has been finalised successfully (including a potential confirmation command).      This is a final status of the authorisation resource.   * 'failed':     The SCA routine failed.     This is a final status of the authorisation resource.   * 'exempted':     SCA was exempted for the related transaction, the related authorisation is successful.     This is a final status of the authorisation resource.
 */
public enum ScaStatus {

    RECEIVED("received"),

    PSUIDENTIFIED("psuIdentified"),

    PSUAUTHENTICATED("psuAuthenticated"),

    SCAMETHODSELECTED("scaMethodSelected"),

    STARTED("started"),

    UNCONFIRMED("unconfirmed"),

    FINALISED("finalised"),

    FAILED("failed"),

    EXEMPTED("exempted");

    private String value;

    ScaStatus(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static ScaStatus fromValue(String text) {
        for (ScaStatus b : ScaStatus.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}

