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

package de.adorsys.psd2.consent.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AisConsentRemoteUrlsTest {
    private static final String BASE_URL = "http://base.url";

    private AisConsentRemoteUrls aisConsentRemoteUrls;

    @BeforeEach
    void setUp() {
        aisConsentRemoteUrls = new AisConsentRemoteUrls();
        ReflectionTestUtils.setField(aisConsentRemoteUrls, "consentServiceBaseUrl", BASE_URL);
    }

    @Test
    void consentActionLog() {
        assertEquals("http://base.url/ais/consent/action",
                     aisConsentRemoteUrls.consentActionLog());
    }

    @Test
    void updateAisAccountAccess() {
        assertEquals("http://base.url/ais/consent/{consent-id}/access",
                     aisConsentRemoteUrls.updateAisAccountAccess());
    }
}
