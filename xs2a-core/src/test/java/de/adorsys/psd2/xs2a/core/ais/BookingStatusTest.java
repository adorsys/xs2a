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

package de.adorsys.psd2.xs2a.core.ais;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BookingStatusTest {
    private static final BookingStatus BOOKING_STATUS = BookingStatus.BOOKED;
    private static final String BOOKING_STATUS_STRING = "booked";

    @Test
    void getByValue_withValidValue_shouldReturnEnum() {
        Optional<BookingStatus> actualValue = BookingStatus.getByValue(BOOKING_STATUS_STRING);
        assertTrue(actualValue.isPresent());
        assertEquals(BOOKING_STATUS, actualValue.get());
    }

    @Test
    void getByValue_withInvalidValue_shouldReturnEmpty() {
        Optional<BookingStatus> actualValue = BookingStatus.getByValue("invalid value");
        assertFalse(actualValue.isPresent());
    }

    @Test
    void getByValue_withUpperCaseValue_shouldReturnEnum() {
        Optional<BookingStatus> actualValue = BookingStatus.getByValue(BOOKING_STATUS_STRING.toUpperCase());
        assertTrue(actualValue.isPresent());
        assertEquals(BOOKING_STATUS, actualValue.get());
    }
}
