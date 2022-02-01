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

package de.adorsys.psd2.xs2a.core.pis;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PisDayOfExecutionTest {

    @Test
    void getByValue() {
        assertEquals(Optional.of(PisDayOfExecution.DAY_14), PisDayOfExecution.getByValue("14"));
        assertEquals(Optional.of(PisDayOfExecution.DAY_1), PisDayOfExecution.getByValue("1"));

        assertEquals(Optional.empty(), PisDayOfExecution.getByValue("234567890"));
        assertEquals(Optional.empty(), PisDayOfExecution.getByValue(""));
        assertEquals(Optional.empty(), PisDayOfExecution.getByValue(null));
    }

    @Test
    void fromValue() {
        assertEquals(PisDayOfExecution.DAY_14, PisDayOfExecution.fromValue("14"));
        assertEquals(PisDayOfExecution.DAY_1, PisDayOfExecution.fromValue("1"));

        assertNull(PisDayOfExecution.fromValue("234567890"));
        assertNull(PisDayOfExecution.fromValue(""));
        assertNull(PisDayOfExecution.fromValue(null));
    }
}
