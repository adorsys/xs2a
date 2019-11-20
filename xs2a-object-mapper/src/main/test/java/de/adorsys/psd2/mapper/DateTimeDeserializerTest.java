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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

public class DateTimeDeserializerTest {

    private Xs2aObjectMapper xs2aObjectMapper;

    @Before
    public void init() {
        xs2aObjectMapper = new Xs2aObjectMapper();
        xs2aObjectMapper.findAndRegisterModules();
    }

    @Test
    public void canDeserialize() {
        assertTrue(xs2aObjectMapper.canDeserialize(xs2aObjectMapper.constructType(LocalDateTime.class)));
    }

    @Test
    public void parseDateTime() throws JsonProcessingException {
        TestLocalDateTimeObject localDateTimeObject = xs2aObjectMapper.readValue("{\"field1\": \"2018-10-03T23:40:40.324Z\"}", TestLocalDateTimeObject.class);

        assertNotNull(localDateTimeObject);
        assertEquals(LocalDateTime.parse("2018-10-03T23:40:40.324"), localDateTimeObject.getField1());
    }

    private static class TestLocalDateTimeObject {
        private LocalDateTime field1;

        public LocalDateTime getField1() {
            return field1;
        }

    }
}
