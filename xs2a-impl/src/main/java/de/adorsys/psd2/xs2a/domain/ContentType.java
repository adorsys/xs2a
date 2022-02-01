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

package de.adorsys.psd2.xs2a.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public enum ContentType {

    XML("application/xml"),
    JSON("application/json"),
    TXT("text/plain"),
    EMPTY("*/*");

    private static final Map<String, ContentType> CONTAINER = new HashMap<>();

    static {
        for (ContentType t : values()) {
            CONTAINER.put(t.getType(), t);
        }
    }

    private String type;

    ContentType(String type) {
        this.type = type;
    }

    @JsonValue
    public String getType() {
        return type;
    }

    /**
     * Extracts content-type by deserialization from strings such as "content/type; encoding=utf-8"
     * For us encoding, boundary etc. are not relevant in this Enum
     *
     * @param type input string with full content type (i.e. "text/plain; boundary=-------")
     * @return mapped enum value. Null if not found
     */
    @JsonCreator
    public static ContentType extract(String type) {
        if (StringUtils.isNotBlank(type)) {
            String[] parts = type.split(";");
            return CONTAINER.get(parts[0]);
        }
        return null;
    }
}
