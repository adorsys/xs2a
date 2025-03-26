/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.core.profile;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NotificationSupportedModeTest {

    @Test
    void getByValue_withValidValue_shouldReturnEnum() {
        NotificationSupportedMode actualValue = NotificationSupportedMode.getByValue("process");
        assertEquals(NotificationSupportedMode.PROCESS, actualValue);
    }

    @Test
    void getByValue_withValidValueUppercased_shouldReturnEnum() {
        NotificationSupportedMode actualValue = NotificationSupportedMode.getByValue("LAST");
        assertEquals(NotificationSupportedMode.LAST, actualValue);
    }

    @Test
    void getByValue_withInvalidValue_shouldReturnEnum() {
        NotificationSupportedMode actualValue = NotificationSupportedMode.getByValue("StepanBandera");
        assertNull(actualValue);
    }
}
