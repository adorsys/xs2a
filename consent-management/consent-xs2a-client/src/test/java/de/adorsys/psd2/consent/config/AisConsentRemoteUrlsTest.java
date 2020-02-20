/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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
