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

class AuthorisationRemoteUrlsTest {
    private static final String BASE_URL = "http://base.url";

    private AuthorisationRemoteUrls authorisationRemoteUrls = new AuthorisationRemoteUrls();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authorisationRemoteUrls, "consentServiceBaseUrl", BASE_URL);
    }

    @Test
    void createAuthorisation() {
        String expected = "http://base.url/{authorisation-type}/{parent-id}/authorisations";
        assertEquals(expected, authorisationRemoteUrls.createAuthorisation());
    }

    @Test
    void getAuthorisationById() {
        String expected = "http://base.url/authorisations/{authorisation-id}";
        assertEquals(expected, authorisationRemoteUrls.getAuthorisationById());
    }

    @Test
    void updateAuthorisation() {
        String expected = "http://base.url/authorisations/{authorisation-id}";
        assertEquals(expected, authorisationRemoteUrls.updateAuthorisation());
    }

    @Test
    void getAuthorisationsByParentId() {
        String expected = "http://base.url/{authorisation-type}/{parent-id}/authorisations";
        assertEquals(expected, authorisationRemoteUrls.getAuthorisationsByParentId());
    }

    @Test
    void getAuthorisationScaStatus() {
        String expected = "http://base.url/{authorisation-type}/{parent-id}/authorisations/{authorisation-id}/status";
        assertEquals(expected, authorisationRemoteUrls.getAuthorisationScaStatus());
    }

    @Test
    void updateAuthorisationStatus() {
        String expected = "http://base.url/authorisations/{authorisation-id}/status/{status}";
        assertEquals(expected, authorisationRemoteUrls.updateAuthorisationStatus());
    }

    @Test
    void isAuthenticationMethodDecoupled() {
        String expected = "http://base.url/authorisations/{authorisation-id}/authentication-methods/{authentication-method-id}";
        assertEquals(expected, authorisationRemoteUrls.isAuthenticationMethodDecoupled());
    }

    @Test
    void saveAuthenticationMethods() {
        String expected = "http://base.url/authorisations/{authorisation-id}/authentication-methods";
        assertEquals(expected, authorisationRemoteUrls.saveAuthenticationMethods());
    }

    @Test
    void updateScaApproach() {
        String expected = "http://base.url/authorisations/{authorisation-id}/sca-approach/{sca-approach}";
        assertEquals(expected, authorisationRemoteUrls.updateScaApproach());
    }

    @Test
    void getAuthorisationScaApproach() {
        String expected = "http://base.url/authorisations/{authorisation-id}/sca-approach";
        assertEquals(expected, authorisationRemoteUrls.getAuthorisationScaApproach());
    }
}
