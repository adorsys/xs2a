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
