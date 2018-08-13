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

package de.adorsys.aspsp.cmsclient.core.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.Optional;

public final class ObjectMapperUtil {
    private static final Log logger = LogFactory.getLog(ObjectMapperUtil.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private ObjectMapperUtil() {
    }

    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new ParameterNamesModule());
    }

    public static <T> Optional<String> toJson(final T object) {
        try {
            return object == null
                       ? Optional.empty()
                       : Optional.ofNullable(mapper.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            logger.error("Can't convert object to json");
        }
        return Optional.empty();
    }

    public static <T> Optional<T> toObject(final String json, final Class<T> target) {
        try {
            return json == null
                       ? Optional.empty()
                       : Optional.ofNullable(mapper.readValue(json, target));
        } catch (IOException e) {
            logger.error("Can't convert json to object");
        }
        return Optional.empty();
    }
}
