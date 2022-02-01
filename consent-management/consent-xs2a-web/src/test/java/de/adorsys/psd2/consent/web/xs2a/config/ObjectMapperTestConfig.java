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

package de.adorsys.psd2.consent.web.xs2a.config;

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
