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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.ActionStatus;
import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.ais.AisAccountAccessInfo;
import de.adorsys.psd2.consent.api.ais.AisConsentActionRequest;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.ais.UpdateAisConsentResponse;
import de.adorsys.psd2.consent.config.AisConsentRemoteUrls;
import de.adorsys.psd2.consent.config.CmsRestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AisConsentServiceRemoteTest {
    private static final String URL = "http://some.url";
    private static final String CONSENT_ID = "consent ID";
    private static final String TPP_ID = "TPP ID";
    private static final String ACTION_LOG_REQUEST_URI = "http://xs2a-uri.com/v1/accounts";

    @Mock
    private RestTemplate consentRestTemplate;
    @Mock
    private AisConsentRemoteUrls aisConsentRemoteUrls;

    @InjectMocks
    private AisConsentServiceRemote aisConsentServiceRemote;

    @Test
    void checkConsentAndSaveActionLog() {
        when(aisConsentRemoteUrls.consentActionLog()).thenReturn(URL);
        AisConsentActionRequest aisConsentActionRequest = new AisConsentActionRequest(TPP_ID, CONSENT_ID, ActionStatus.SUCCESS, ACTION_LOG_REQUEST_URI, true, null, null);

        CmsResponse<CmsResponse.VoidResponse> response = aisConsentServiceRemote.checkConsentAndSaveActionLog(aisConsentActionRequest);

        assertTrue(response.isSuccessful());
        verify(consentRestTemplate).postForEntity(URL, aisConsentActionRequest, Void.class);
    }

    @Test
    void checkConsentAndSaveActionLog_cmsRestException() {
        when(aisConsentRemoteUrls.consentActionLog()).thenReturn(URL);
        AisConsentActionRequest aisConsentActionRequest = new AisConsentActionRequest(TPP_ID, CONSENT_ID, ActionStatus.SUCCESS, ACTION_LOG_REQUEST_URI, true, null, null);
        doThrow(CmsRestException.class).when(consentRestTemplate).postForEntity(URL, aisConsentActionRequest, Void.class);

        CmsResponse<CmsResponse.VoidResponse> response = aisConsentServiceRemote.checkConsentAndSaveActionLog(aisConsentActionRequest);

        assertTrue(response.hasError());
        assertEquals(CmsError.TECHNICAL_ERROR, response.getError());
        verify(consentRestTemplate).postForEntity(URL, aisConsentActionRequest, Void.class);
    }

    @Test
    void updateAspspAccountAccess() {
        when(aisConsentRemoteUrls.updateAisAccountAccess()).thenReturn(URL);
        AisAccountAccessInfo aisAccountAccessInfo = new AisAccountAccessInfo();
        CmsConsent updatedConsent = new CmsConsent();
        when(consentRestTemplate.exchange(URL, HttpMethod.PUT, new HttpEntity<>(aisAccountAccessInfo), UpdateAisConsentResponse.class, CONSENT_ID))
            .thenReturn(ResponseEntity.ok(new UpdateAisConsentResponse(updatedConsent)));

        CmsResponse<CmsConsent> response = aisConsentServiceRemote.updateAspspAccountAccess(CONSENT_ID, aisAccountAccessInfo);

        assertTrue(response.isSuccessful());
        assertEquals(updatedConsent, response.getPayload());
    }

    @Test
    void updateAspspAccountAccess_nullBody() {
        when(aisConsentRemoteUrls.updateAisAccountAccess()).thenReturn(URL);
        AisAccountAccessInfo aisAccountAccessInfo = new AisAccountAccessInfo();
        when(consentRestTemplate.exchange(URL, HttpMethod.PUT, new HttpEntity<>(aisAccountAccessInfo), UpdateAisConsentResponse.class, CONSENT_ID))
            .thenReturn(ResponseEntity.ok().build());

        CmsResponse<CmsConsent> response = aisConsentServiceRemote.updateAspspAccountAccess(CONSENT_ID, aisAccountAccessInfo);

        assertTrue(response.hasError());
        assertEquals(CmsError.TECHNICAL_ERROR, response.getError());
    }

    @Test
    void updateAspspAccountAccess_cmsRestException() {
        when(aisConsentRemoteUrls.updateAisAccountAccess()).thenReturn(URL);
        AisAccountAccessInfo aisAccountAccessInfo = new AisAccountAccessInfo();
        when(consentRestTemplate.exchange(URL, HttpMethod.PUT, new HttpEntity<>(aisAccountAccessInfo), UpdateAisConsentResponse.class, CONSENT_ID))
            .thenThrow(CmsRestException.class);

        CmsResponse<CmsConsent> response = aisConsentServiceRemote.updateAspspAccountAccess(CONSENT_ID, aisAccountAccessInfo);

        assertTrue(response.hasError());
        assertEquals(CmsError.TECHNICAL_ERROR, response.getError());
    }
}
