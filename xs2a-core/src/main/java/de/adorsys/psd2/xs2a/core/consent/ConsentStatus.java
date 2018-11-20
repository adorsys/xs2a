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

package de.adorsys.psd2.xs2a.core.consent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This is the overall lifecycle status of the consent.  Valid values are:   - 'received': The consent data have been
 * received and are technically correct.     The data is not authorised yet.   - 'rejected': The consent data have been
 * rejected e.g. since no successful authorisation has taken place.   - 'valid': The consent is accepted and valid for
 * GET account data calls and others as specified in the consent object.   - 'revokedByPsu': The consent has been
 * revoked by the PSU towards the ASPSP.   - 'expired': The consent expired.   - 'terminatedByTpp': The corresponding
 * TPP has terminated the consent by applying the DELETE method to the consent resource.  The ASPSP might add further
 * codes. These codes then shall be contained in the ASPSP's documentation of the XS2A interface and has to be added to
 * this API definition as well.
 */
public enum ConsentStatus {
    RECEIVED("received", false),
    REJECTED("rejected", true),
    VALID("valid", false),
    REVOKED_BY_PSU("revokedByPsu", true),
    EXPIRED("expired", true),
    TERMINATED_BY_TPP("terminatedByTpp", true),
    TERMINATED_BY_ASPSP("terminatedByAspsp", true);

    private static final Map<String, ConsentStatus> CONTAINER = new HashMap<>();

    static {
        for (ConsentStatus status : values()) {
            CONTAINER.put(status.getValue(), status);
        }
    }

    private String value;
    private boolean finalisedStatus;

    public boolean isFinalisedStatus() {
        return finalisedStatus;
    }

    ConsentStatus(String value, boolean finalisedStatus) {
        this.value = value;
        this.finalisedStatus = finalisedStatus;
    }

    public static Optional<ConsentStatus> fromValue(String text) {
        if (text != null) {
            return Optional.ofNullable(CONTAINER.get(text.trim()));
        }
        return Optional.empty();
    }

    public String getValue() {
        return String.valueOf(value);
    }
}

