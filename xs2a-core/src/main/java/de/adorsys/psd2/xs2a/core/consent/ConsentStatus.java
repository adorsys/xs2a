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
    TERMINATED_BY_ASPSP("terminatedByAspsp", true),
    PARTIALLY_AUTHORISED("partiallyAuthorised", false);

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

