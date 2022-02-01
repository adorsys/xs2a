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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.ActionStatus;
import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.ais.AisConsentActionRequest;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.ais.UpdateAisConsentResponse;
import de.adorsys.psd2.consent.config.AisConsentRemoteUrls;
import de.adorsys.psd2.consent.config.CmsRestException;
import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.xs2a.core.profile.AdditionalInformationAccess;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

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
        AccountAccess accountAccess = buildEmptyAccountAccess();
        CmsConsent updatedConsent = new CmsConsent();
        when(consentRestTemplate.exchange(URL, HttpMethod.PUT, new HttpEntity<>(accountAccess), UpdateAisConsentResponse.class, CONSENT_ID))
            .thenReturn(ResponseEntity.ok(new UpdateAisConsentResponse(updatedConsent)));

        CmsResponse<CmsConsent> response = aisConsentServiceRemote.updateAspspAccountAccess(CONSENT_ID, accountAccess);

        assertTrue(response.isSuccessful());
        assertEquals(updatedConsent, response.getPayload());
    }

    @Test
    void updateAspspAccountAccess_nullBody() {
        when(aisConsentRemoteUrls.updateAisAccountAccess()).thenReturn(URL);
        AccountAccess accountAccess = buildEmptyAccountAccess();
        when(consentRestTemplate.exchange(URL, HttpMethod.PUT, new HttpEntity<>(accountAccess), UpdateAisConsentResponse.class, CONSENT_ID))
            .thenReturn(ResponseEntity.ok().build());

        CmsResponse<CmsConsent> response = aisConsentServiceRemote.updateAspspAccountAccess(CONSENT_ID, accountAccess);

        assertTrue(response.hasError());
        assertEquals(CmsError.TECHNICAL_ERROR, response.getError());
    }

    @Test
    void updateAspspAccountAccess_cmsRestException() {
        when(aisConsentRemoteUrls.updateAisAccountAccess()).thenReturn(URL);
        AccountAccess accountAccess = buildEmptyAccountAccess();
        when(consentRestTemplate.exchange(URL, HttpMethod.PUT, new HttpEntity<>(accountAccess), UpdateAisConsentResponse.class, CONSENT_ID))
            .thenThrow(CmsRestException.class);

        CmsResponse<CmsConsent> response = aisConsentServiceRemote.updateAspspAccountAccess(CONSENT_ID, accountAccess);

        assertTrue(response.hasError());
        assertEquals(CmsError.TECHNICAL_ERROR, response.getError());
    }

    private AccountAccess buildEmptyAccountAccess() {
        return new AccountAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), buildEmptyAdditionalInformationAccess());
    }

    private AdditionalInformationAccess buildEmptyAdditionalInformationAccess() {
        return new AdditionalInformationAccess(Collections.emptyList(), Collections.emptyList());
    }
}
