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

package de.adorsys.psd2.xs2a.core.authorisation;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.HashMap;
import java.util.Map;

public enum AuthorisationType {
    CONSENT,
    PIS_CREATION,
    PIS_CANCELLATION,
    SIGNING_BASKET;

    private static final Map<String, AuthorisationType> HOLDER = new HashMap<>();

    static {
        for (AuthorisationType authorisationType : AuthorisationType.values()) {
            HOLDER.put(authorisationType.name().toLowerCase(), authorisationType);
        }
    }

    /**
     * Maps textual representation to AuthorisationType enum-value.
     * Mapping is performed case-insensitive.
     *
     * @param text - text to be mapped.
     * @return Enum value mapped. Null otherwise.
     */
    @JsonCreator
    public static AuthorisationType fromValue(String text) {
        return HOLDER.get(text.trim().toLowerCase());
    }
}
