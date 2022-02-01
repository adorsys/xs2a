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

package de.adorsys.psd2.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeDeserializerTest {

    private Xs2aObjectMapper xs2aObjectMapper;

    @BeforeEach
    void init() {
        xs2aObjectMapper = new Xs2aObjectMapper();
        xs2aObjectMapper.findAndRegisterModules();
    }

    @Test
    void canDeserialize() {
        assertTrue(xs2aObjectMapper.canDeserialize(xs2aObjectMapper.constructType(LocalDateTime.class)));
    }

    @Test
    void parseDateTime() throws JsonProcessingException {
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
