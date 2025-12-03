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

package de.adorsys.psd2.xs2a.core.vop;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum VopBulkStatus {

    RECEIVED("recieved"),
    PENDING("pending"),
    REJECTED("rejected"),
    COMPLETED("completed");

    private static Map<String, VopBulkStatus> container = new HashMap<>();

    static {
        Arrays.stream(values())
                .forEach(status -> container.put(status.getValue(), status));
    }

    private String value;

    VopBulkStatus(String value) {
        this.value = value;
    }

    public static VopBulkStatus getByValue(String value) {
        return container.get(value);
    }

    public String getValue() {
        return value;
    }
}