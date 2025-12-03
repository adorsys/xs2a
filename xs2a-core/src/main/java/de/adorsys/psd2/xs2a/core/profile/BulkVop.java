/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.core.profile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Enumeration representing bulk Verification of Payee (VOP) types.
 */
public enum BulkVop {
    /**
     * Bulk payee verification type.
     */
    BULK_PAYEE("bulk-payee-verifications"),

    /**
     * Bulk payer verification type.
     */
    BULK_PAYER("bulk-payer-verifications"),

    /**
     * Bulk party verification type.
     */
    BULK_PARTY("bulk-party-verifications");

    private static final Map<String, BulkVop> container = new HashMap<>();

    private String value;

    static {
        for (BulkVop type : values()) {
            container.put(type.getValue(), type);
        }
    }

    /**
     * Constructor for BulkVop enum.
     *
     * @param value the string value associated with this enum constant
     */
    @JsonCreator
    BulkVop(String value) {
        this.value = value;
    }

    /**
     * Gets the string value associated with this enum constant.
     *
     * @return the string value
     */
    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * Retrieves a BulkVop enum constant by its string value.
     *
     * @param name the string value to look up
     * @return an Optional containing the matching BulkVop, or empty if not found
     */
    public static Optional<BulkVop> getByValue(String name) {
        return Optional.ofNullable(container.get(name));
    }
}
