/*
 * Copyright 2018-2021 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.web.mapper;

import de.adorsys.psd2.model.DayOfExecution;
import de.adorsys.psd2.xs2a.core.pis.PisDayOfExecution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DayOfExecutionMapperTest {
    private final DayOfExecutionMapper mapper = new DayOfExecutionMapper();

    @Test
    void mapDayOfExecution_nullValue() {
        DayOfExecution actual = mapper.mapDayOfExecution(null);
        assertNull(actual);
    }

    @Test
    void mapDayOfExecution_nonNull() {
        DayOfExecution actual = mapper.mapDayOfExecution(PisDayOfExecution.DAY_4);
        assertEquals(DayOfExecution._4, actual);
    }
}
