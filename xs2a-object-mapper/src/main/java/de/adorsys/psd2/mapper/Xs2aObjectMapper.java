/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
public class Xs2aObjectMapper extends ObjectMapper {

    public Xs2aObjectMapper(Xs2aObjectMapper src) {
        super(src);
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
            JsonNode jsonNode = readTree(stream);
            JsonNode fieldNode = jsonNode.get(fieldName);

            if (fieldNode == null) {
                log.info("Couldn't extract field from json, because there is no this field {} at json.", fieldName);
                return Optional.empty();
            }

            T value = readValue(treeAsTokens(fieldNode), typeReference);
            return Optional.ofNullable(value);

        } catch (IOException e) {
            log.info("Couldn't extract field {} from json: {}", fieldName, e.getMessage());
        }

        return Optional.empty();
    }

    public List<String> toJsonGetValuesForField(InputStream stream, String fieldName) {
        List<String> values = new ArrayList<>();
        try {
            JsonNode jsonNode = readTree(stream);
            values.addAll(jsonNode.findValuesAsText(fieldName));
        } catch (IOException e) {
            log.info("Couldn't extract field {} from json: {}", fieldName, e.getMessage());
        }
        return values;
    }

    @Override
    public ObjectMapper copy() {
        _checkInvalidCopy(Xs2aObjectMapper.class);
        return new Xs2aObjectMapper(this);
    }
}
