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
        for (ContentType t: values()) {
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
