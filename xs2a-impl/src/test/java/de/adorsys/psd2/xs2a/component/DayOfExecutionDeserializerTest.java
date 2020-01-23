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

package de.adorsys.psd2.xs2a.component;

import com.fasterxml.jackson.core.JsonParser;
import de.adorsys.psd2.model.DayOfExecution;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DayOfExecutionDeserializerTest {
    private static final String NORMAL_DAY_OF_MONTH_NUMBER = "5";
    private static final String DAY_OF_MONTH_NUMBER_WITH_ZERO = "05";
    private static final DayOfExecution DAY_OF_MONTH_ENUM = DayOfExecution._5;

    @InjectMocks
    private DayOfExecutionDeserializer dayOfExecutionDeserializer;

    @Mock
    private JsonParser jsonParser;


    @Test
    void deserialize_normal_value_success() throws IOException {
        when(jsonParser.getText()).thenReturn(NORMAL_DAY_OF_MONTH_NUMBER);
        DayOfExecution actual = dayOfExecutionDeserializer.deserialize(jsonParser, null);
        assertEquals(DAY_OF_MONTH_ENUM, actual);
    }

    @Test
    void deserialize_value_with_zero_success() throws IOException {
        when(jsonParser.getText()).thenReturn(DAY_OF_MONTH_NUMBER_WITH_ZERO);
        DayOfExecution actual = dayOfExecutionDeserializer.deserialize(jsonParser, null);
        assertEquals(DAY_OF_MONTH_ENUM, actual);
    }

    @Test
    void deserialize_fail_should_return_null() throws IOException {
        when(jsonParser.getText()).thenThrow(new IOException());
        DayOfExecution actual = dayOfExecutionDeserializer.deserialize(jsonParser, null);
        assertNull(actual);
    }
}
