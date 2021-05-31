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

class ConsentRemoteUrlsTest {
    private static final String BASE_URL = "http://base.url";

    private final ConsentRemoteUrls consentRemoteUrls = new ConsentRemoteUrls();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(consentRemoteUrls, "consentServiceBaseUrl", BASE_URL);
    }

    @Test
    void createConsent() {
        String expected = "http://base.url/consent/";
        assertEquals(expected, consentRemoteUrls.createConsent());
    }

    @Test
    void getConsentStatusById() {
        String expected = "http://base.url/consent/{encrypted-consent-id}/status";
        assertEquals(expected, consentRemoteUrls.getConsentStatusById());
    }

    @Test
    void getConsentById() {
        String expected = "http://base.url/consent/{encrypted-consent-id}";
        assertEquals(expected, consentRemoteUrls.getConsentById());
    }

    @Test
    void updateConsentStatusById() {
        String expected = "http://base.url/consent/{encrypted-consent-id}/status/{status}";
        assertEquals(expected, consentRemoteUrls.updateConsentStatusById());
    }

    @Test
    void findAndTerminateOldConsentsByNewConsentId() {
        String expected = "http://base.url/consent/{encrypted-consent-id}/old-consents";
        assertEquals(expected, consentRemoteUrls.findAndTerminateOldConsentsByNewConsentId());
    }

    @Test
    void findAndTerminateOldConsents() {
        String expected = "http://base.url/consent/{encrypted-consent-id}/old-consents";
        assertEquals(expected, consentRemoteUrls.findAndTerminateOldConsents());
    }

    @Test
    void getPsuDataByConsentId() {
        String expected = "http://base.url/consent/{encrypted-consent-id}/psu-data";
        assertEquals(expected, consentRemoteUrls.getPsuDataByConsentId());
    }

    @Test
    void updateMultilevelScaRequired() {
        String expected = "http://base.url/consent/{encrypted-consent-id}/multilevel-sca?multilevel-sca={multilevel-sca}";
        assertEquals(expected, consentRemoteUrls.updateMultilevelScaRequired());
    }
}
