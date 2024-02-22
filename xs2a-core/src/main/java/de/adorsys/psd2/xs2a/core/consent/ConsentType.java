/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.core.consent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum ConsentType {
    AIS,
    PIIS_ASPSP,
    PIIS_TPP;

    private static Map<String, ConsentType> container = new HashMap<>();

    static {
        Arrays.stream(values())
            .forEach(type -> container.put(type.getName(), type));
    }

    public String getName() {
        return this.name();
    }

    public static ConsentType getByValue(String consentType) {
        return container.get(consentType);
    }
}
