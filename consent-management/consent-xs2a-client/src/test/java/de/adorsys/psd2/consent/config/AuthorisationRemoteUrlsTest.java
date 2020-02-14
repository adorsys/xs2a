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
