/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.mapper.config;

import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ObjectMapperConfigTest {

    @Test
    void init() {
        ObjectMapperConfig config = new ObjectMapperConfig();
        Xs2aObjectMapper xs2aObjectMapper = config.xs2aObjectMapper();

        assertNotNull(xs2aObjectMapper);

        assertEquals(3, xs2aObjectMapper.getRegisteredModuleIds().size());
        assertTrue(xs2aObjectMapper.getRegisteredModuleIds().contains(Jdk8Module.class.getName()));
        assertTrue(xs2aObjectMapper.getRegisteredModuleIds().contains(JavaTimeModule.class.getName()));
        assertTrue(xs2aObjectMapper.getRegisteredModuleIds().contains(ParameterNamesModule.class.getName()));
    }
}
