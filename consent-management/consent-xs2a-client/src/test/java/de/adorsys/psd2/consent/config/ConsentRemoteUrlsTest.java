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
