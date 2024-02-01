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

package de.adorsys.psd2.consent.service.sha;

import de.adorsys.psd2.core.data.ais.AisConsent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NoProcessingChecksumServiceTest {

    private NoProcessingChecksumService service = new NoProcessingChecksumService();

    @Test
    void calculateChecksumForConsent() {
        assertEquals(0, service.calculateChecksumForConsent(new AisConsent()).length);

    }

    @Test
    void verifyConsentWithChecksum() {
        assertTrue(service.verifyConsentWithChecksum(new AisConsent(), new byte[0]));
    }

    @Test
    void getVersion() {
        assertEquals("000", service.getVersion());
    }
}
