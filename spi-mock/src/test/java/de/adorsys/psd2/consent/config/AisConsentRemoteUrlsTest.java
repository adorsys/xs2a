/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.*;

public class AisConsentRemoteUrlsTest {

    private static final String BASE_URL = "http://base.url";

    private AisConsentRemoteUrls aisConsentRemoteUrls;

    @Before
    public void setUp() {
        aisConsentRemoteUrls = new AisConsentRemoteUrls();
        ReflectionTestUtils.setField(aisConsentRemoteUrls, "consentServiceBaseUrl", BASE_URL);
    }

    @Test
    public void createAisConsent() {
        assertEquals("http://base.url/ais/consent/",
            aisConsentRemoteUrls.createAisConsent());
    }

    @Test
    public void getAisConsentById() {
        assertEquals("http://base.url/ais/consent/{consent-id}",
            aisConsentRemoteUrls.getAisConsentById());
    }

    @Test
    public void getInitialAisConsentById() {
        assertEquals("http://base.url/ais/consent/initial/{consent-id}",
            aisConsentRemoteUrls.getInitialAisConsentById());
    }

    @Test
    public void getAisConsentStatusById() {
        assertEquals("http://base.url/ais/consent/{consent-id}/status",
            aisConsentRemoteUrls.getAisConsentStatusById());
    }

    @Test
    public void updateAisConsentStatus() {
        assertEquals("http://base.url/ais/consent/{consent-id}/status/{status}",
            aisConsentRemoteUrls.updateAisConsentStatus());
    }

    @Test
    public void findAndTerminateOldConsentsByNewConsentId() {
        assertEquals("http://base.url/ais/consent/{consent-id}/old-consents",
            aisConsentRemoteUrls.findAndTerminateOldConsentsByNewConsentId());
    }

    @Test
    public void consentActionLog() {
        assertEquals("http://base.url/ais/consent/action",
            aisConsentRemoteUrls.consentActionLog());
    }

    @Test
    public void createAisConsentAuthorization() {
        assertEquals("http://base.url/ais/consent/{consent-id}/authorizations",
            aisConsentRemoteUrls.createAisConsentAuthorization());
    }

    @Test
    public void updateAisConsentAuthorization() {
        assertEquals("http://base.url/ais/consent/authorizations/{authorization-id}",
            aisConsentRemoteUrls.updateAisConsentAuthorization());
    }

    @Test
    public void getAisConsentAuthorizationById() {
        assertEquals("http://base.url/ais/consent/{consent-id}/authorizations/{authorization-id}",
            aisConsentRemoteUrls.getAisConsentAuthorizationById());
    }

    @Test
    public void updateAisAccountAccess() {
        assertEquals("http://base.url/ais/consent/{consent-id}/access",
            aisConsentRemoteUrls.updateAisAccountAccess());
    }

    @Test
    public void getPsuDataByConsentId() {
        assertEquals("http://base.url/ais/consent/{consent-id}/psu-data",
            aisConsentRemoteUrls.getPsuDataByConsentId());
    }

    @Test
    public void getAuthorisationSubResources() {
        assertEquals("http://base.url/ais/consent/{consent-id}/authorisations",
            aisConsentRemoteUrls.getAuthorisationSubResources());
    }

    @Test
    public void getAuthorisationScaStatus() {
        assertEquals("http://base.url/ais/consent/{consent-id}/authorisations/{authorisation-id}/status",
            aisConsentRemoteUrls.getAuthorisationScaStatus());
    }

    @Test
    public void isAuthenticationMethodDecoupled() {
        assertEquals("http://base.url/ais/consent/authorisations/{authorisation-id}/authentication-methods/{authentication-method-id}",
            aisConsentRemoteUrls.isAuthenticationMethodDecoupled());
    }

    @Test
    public void saveAuthenticationMethods() {
        assertEquals("http://base.url/ais/consent/authorisations/{authorisation-id}/authentication-methods",
            aisConsentRemoteUrls.saveAuthenticationMethods());
    }

    @Test
    public void updateScaApproach() {
        assertEquals("http://base.url/ais/consent/authorisations/{authorisation-id}/sca-approach/{sca-approach}",
            aisConsentRemoteUrls.updateScaApproach());
    }

    @Test
    public void updateMultilevelScaRequired() {
        assertEquals("http://base.url/ais/consent/{consent-id}/multilevel-sca?multilevel-sca={multilevel-sca}",
            aisConsentRemoteUrls.updateMultilevelScaRequired());
    }

    @Test
    public void getAuthorisationScaApproach() {
        assertEquals("http://base.url/ais/consent/authorisations/{authorisation-id}/sca-approach",
            aisConsentRemoteUrls.getAuthorisationScaApproach());
    }
}
