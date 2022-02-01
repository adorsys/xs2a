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
