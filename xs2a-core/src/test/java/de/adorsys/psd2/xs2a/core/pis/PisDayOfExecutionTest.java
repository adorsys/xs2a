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

package de.adorsys.psd2.xs2a.core.pis;

import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public class PisDayOfExecutionTest {

    @Test
    public void getByValue() {
        assertEquals(Optional.of(PisDayOfExecution._14), PisDayOfExecution.getByValue("14"));
        assertEquals(Optional.of(PisDayOfExecution._1), PisDayOfExecution.getByValue("1"));

        assertEquals(Optional.empty(), PisDayOfExecution.getByValue("234567890"));
        assertEquals(Optional.empty(), PisDayOfExecution.getByValue(""));
        assertEquals(Optional.empty(), PisDayOfExecution.getByValue(null));
    }

    @Test
    public void fromValue() {
        assertEquals(PisDayOfExecution._14, PisDayOfExecution.fromValue("14"));
        assertEquals(PisDayOfExecution._1, PisDayOfExecution.fromValue("1"));

        assertNull(PisDayOfExecution.fromValue("234567890"));
        assertNull(PisDayOfExecution.fromValue(""));
        assertNull(PisDayOfExecution.fromValue(null));
    }
}
