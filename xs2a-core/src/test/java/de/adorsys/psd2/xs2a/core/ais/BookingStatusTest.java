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

package de.adorsys.psd2.xs2a.core.ais;

import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public class BookingStatusTest {
    private static final BookingStatus BOOKING_STATUS = BookingStatus.BOOKED;
    private static final String BOOKING_STATUS_STRING = "booked";

    @Test
    public void getByValue_withValidValue_shouldReturnEnum() {
        Optional<BookingStatus> actualValue = BookingStatus.getByValue(BOOKING_STATUS_STRING);
        assertTrue(actualValue.isPresent());
        assertEquals(BOOKING_STATUS, actualValue.get());
    }

    @Test
    public void getByValue_withInvalidValue_shouldReturnEmpty() {
        Optional<BookingStatus> actualValue = BookingStatus.getByValue("invalid value");
        assertFalse(actualValue.isPresent());
    }

    @Test
    public void getByValue_withUpperCaseValue_shouldReturnEnum() {
        Optional<BookingStatus> actualValue = BookingStatus.getByValue(BOOKING_STATUS_STRING.toUpperCase());
        assertTrue(actualValue.isPresent());
        assertEquals(BOOKING_STATUS, actualValue.get());
    }
}
