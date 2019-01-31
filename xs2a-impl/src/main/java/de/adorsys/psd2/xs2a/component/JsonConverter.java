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
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
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
}
