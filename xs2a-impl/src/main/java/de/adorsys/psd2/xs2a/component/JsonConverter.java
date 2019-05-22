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

package de.adorsys.psd2.xs2a.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Slf4j
@Component
@AllArgsConstructor
public class JsonConverter {
    private final ObjectMapper objectMapper;

    public <T> Optional<String> toJson(final T object) {
        try {
            return Optional.ofNullable(objectMapper.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            log.error("Can't convert object to json: {}", e.getMessage(), e);
        }
        return Optional.empty();
    }

    public <T> Optional<T> toObject(final String json, final Class<T> target) {
        try {
            return Optional.ofNullable(objectMapper.readValue(json, target));
        } catch (IOException e) {
            log.error("Can't convert json to object: {}", e.getMessage(), e);
        }
        return Optional.empty();
    }

    public <T> Optional<T> toObject(final byte[] bytes, final TypeReference valueTypeRef) {
        try {
            return Optional.ofNullable(objectMapper.readValue(IOUtils.toString(bytes, "UTF-8"), valueTypeRef));
        } catch (IOException e) {
            log.error("Can't convert json to object: {}", e.getMessage(), e);
        }
        return Optional.empty();
    }

    /**
     * Converts value from input stream into JSON and extracts its field by given name
     *
     * @param stream        input stream used to read JSON content
     * @param fieldName     name of the JSON field to be extracted
     * @param typeReference type reference of the field
     * @param <T>           type of the field to be extracted
     * @return value of the extracted field, if it was found in the valid JSON
     */
    public <T> Optional<T> toJsonField(InputStream stream, String fieldName, TypeReference<T> typeReference) {
        try {
            JsonNode jsonNode = objectMapper.readTree(stream);
            JsonNode fieldNode = jsonNode.get(fieldName);

            if (fieldNode == null) {
                return Optional.empty();
            }

            T value = objectMapper.readValue(objectMapper.treeAsTokens(fieldNode), typeReference);
            return Optional.ofNullable(value);

        } catch (IOException e) {
            log.info("Couldn't extract field {} from json: {}", fieldName, e.getMessage(), e);
        }

        return Optional.empty();
    }
}
