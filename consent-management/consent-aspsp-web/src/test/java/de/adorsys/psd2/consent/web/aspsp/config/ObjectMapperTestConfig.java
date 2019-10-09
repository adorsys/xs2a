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

package de.adorsys.psd2.consent.web.aspsp.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;

public class ObjectMapperTestConfig {
    public Xs2aObjectMapper getXs2aObjectMapper() {
        Xs2aObjectMapper xs2aObjectMapper = new Xs2aObjectMapper();
        xs2aObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        xs2aObjectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        xs2aObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        xs2aObjectMapper.registerModule(new Jdk8Module()); // add support for Optionals
        xs2aObjectMapper.registerModule(new JavaTimeModule()); // add support for java.time types
        xs2aObjectMapper.registerModule(new ParameterNamesModule()); // support for multiargs constructors
        return xs2aObjectMapper;
    }
}
