/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.core.consent;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ConsentStatusTest {
    private static final ConsentStatus CONSENT_STATUS = ConsentStatus.RECEIVED;
    private static final String CONSENT_STATUS_STRING = "received";

    @Test
    void getByValue_withValidValue_shouldReturnEnum() {
        Optional<ConsentStatus> actualValue = ConsentStatus.fromValue(CONSENT_STATUS_STRING);
        assertTrue(actualValue.isPresent());
        assertEquals(CONSENT_STATUS, actualValue.get());
    }

    @Test
    void forValue_withValidValue_shouldReturnEnum() {
        Optional<ConsentStatus> actualValue = ConsentStatus.fromValue(CONSENT_STATUS_STRING);
        assertNotNull(actualValue);
        assertEquals(CONSENT_STATUS, actualValue.get());
    }

    @Test
    void getByValue_withInvalidValue_shouldReturnEmpty() {
        Optional<ConsentStatus> actualValue = ConsentStatus.fromValue("invalid value");
        assertFalse(actualValue.isPresent());
    }

    @Test
    void getByValue_withUpperCaseValue_shouldReturnEnum() {
        Optional<ConsentStatus> actualValue = ConsentStatus.fromValue("   " + CONSENT_STATUS_STRING + "   ");
        assertTrue(actualValue.isPresent());
        assertEquals(CONSENT_STATUS, actualValue.get());
    }

}
