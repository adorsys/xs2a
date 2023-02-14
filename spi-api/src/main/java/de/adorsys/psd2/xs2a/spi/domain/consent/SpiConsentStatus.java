/*
 * Copyright 2018-2023 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.spi.domain.consent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum SpiConsentStatus {
    RECEIVED("received", false),
    REJECTED("rejected", true),
    VALID("valid", false),
    REVOKED_BY_PSU("revokedByPsu", true),
    EXPIRED("expired", true),
    TERMINATED_BY_TPP("terminatedByTpp", true),
    TERMINATED_BY_ASPSP("terminatedByAspsp", true),
    PARTIALLY_AUTHORISED("partiallyAuthorised", false);

    private static final Map<String, SpiConsentStatus> CONTAINER = new HashMap<>();

    static {
        for (SpiConsentStatus status : values()) {
            CONTAINER.put(status.getValue(), status);
        }
    }

    private String value;
    private boolean finalisedStatus;

    public boolean isFinalisedStatus() {
        return finalisedStatus;
    }

    SpiConsentStatus(String value, boolean finalisedStatus) {
        this.value = value;
        this.finalisedStatus = finalisedStatus;
    }

    public static Optional<SpiConsentStatus> fromValue(String text) {
        if (text != null) {
            return Optional.ofNullable(CONTAINER.get(text.trim()));
        }
        return Optional.empty();
    }

    public String getValue() {
        return String.valueOf(value);
    }
}
