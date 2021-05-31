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

import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.ais.ConsentStatusResponse;
import de.adorsys.psd2.consent.api.consent.CmsCreateConsentResponse;
import de.adorsys.psd2.consent.config.CmsRestException;
import de.adorsys.psd2.consent.config.ConsentRemoteUrls;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.TerminateOldConsentsRequest;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsentServiceRemoteTest {
    private static final String URL = "http://some.url";
    private static final String CONSENT_ID = "encrypted consent id";

    @Mock
    private RestTemplate consentRestTemplate;
    @Mock
    private ConsentRemoteUrls consentRemoteUrls;

    @InjectMocks
    private ConsentServiceRemote consentServiceRemote;

    @Test
    void createConsent() {
        CmsConsent consentToCreate = new CmsConsent();
        CmsConsent createdConsent = new CmsConsent();
        CmsCreateConsentResponse createConsentResponse = new CmsCreateConsentResponse(CONSENT_ID, createdConsent);
        when(consentRemoteUrls.createConsent()).thenReturn(URL);
        when(consentRestTemplate.postForEntity(URL, consentToCreate, CmsCreateConsentResponse.class))
            .thenReturn(new ResponseEntity<>(createConsentResponse, HttpStatus.CREATED));

        CmsResponse<CmsCreateConsentResponse> actualResponse = consentServiceRemote.createConsent(consentToCreate);

        assertTrue(actualResponse.isSuccessful());
        assertEquals(createConsentResponse, actualResponse.getPayload());
    }

    @Test
    void createConsent_cmsRestException() {
        CmsConsent consentToCreate = new CmsConsent();
        when(consentRemoteUrls.createConsent()).thenReturn(URL);
        when(consentRestTemplate.postForEntity(URL, consentToCreate, CmsCreateConsentResponse.class))
            .thenThrow(CmsRestException.class);

        CmsResponse<CmsCreateConsentResponse> actualResponse = consentServiceRemote.createConsent(consentToCreate);

        assertTrue(actualResponse.hasError());
        assertEquals(CmsError.TECHNICAL_ERROR, actualResponse.getError());
    }

    @Test
    void getConsentStatusById() {
        when(consentRemoteUrls.getConsentStatusById()).thenReturn(URL);
        ConsentStatus consentStatus = ConsentStatus.RECEIVED;
        when(consentRestTemplate.getForEntity(URL, ConsentStatusResponse.class, CONSENT_ID))
            .thenReturn(ResponseEntity.ok(new ConsentStatusResponse(consentStatus)));

        CmsResponse<ConsentStatus> actualResponse = consentServiceRemote.getConsentStatusById(CONSENT_ID);

        assertTrue(actualResponse.isSuccessful());
        assertEquals(consentStatus, actualResponse.getPayload());
    }

    @Test
    void getConsentStatusById_nullResponse() {
        when(consentRemoteUrls.getConsentStatusById()).thenReturn(URL);
        ConsentStatus consentStatus = ConsentStatus.RECEIVED;
        when(consentRestTemplate.getForEntity(URL, ConsentStatusResponse.class, CONSENT_ID))
            .thenReturn(ResponseEntity.ok().build());

        CmsResponse<ConsentStatus> actualResponse = consentServiceRemote.getConsentStatusById(CONSENT_ID);

        assertTrue(actualResponse.hasError());
        assertEquals(CmsError.TECHNICAL_ERROR, actualResponse.getError());
    }

    @Test
    void getConsentStatusById_cmsRestException() {
        when(consentRemoteUrls.getConsentStatusById()).thenReturn(URL);
        when(consentRestTemplate.getForEntity(URL, ConsentStatusResponse.class, CONSENT_ID))
            .thenThrow(CmsRestException.class);

        CmsResponse<ConsentStatus> actualResponse = consentServiceRemote.getConsentStatusById(CONSENT_ID);

        assertTrue(actualResponse.hasError());
        assertEquals(CmsError.TECHNICAL_ERROR, actualResponse.getError());
    }

    @Test
    void updateConsentStatusById() {
        when(consentRemoteUrls.updateConsentStatusById()).thenReturn(URL);
        ConsentStatus newConsentStatus = ConsentStatus.RECEIVED;

        CmsResponse<Boolean> actualResponse = consentServiceRemote.updateConsentStatusById(CONSENT_ID, newConsentStatus);

        assertTrue(actualResponse.isSuccessful());
        assertTrue(actualResponse.getPayload());
        verify(consentRestTemplate).put(URL, null, CONSENT_ID, newConsentStatus);
    }

    @Test
    void updateConsentStatusById_cmsRestException() {
        when(consentRemoteUrls.updateConsentStatusById()).thenReturn(URL);
        ConsentStatus newConsentStatus = ConsentStatus.RECEIVED;
        doThrow(CmsRestException.class).when(consentRestTemplate).put(URL, null, CONSENT_ID, newConsentStatus);

        CmsResponse<Boolean> actualResponse = consentServiceRemote.updateConsentStatusById(CONSENT_ID, newConsentStatus);

        assertTrue(actualResponse.isSuccessful());
        assertFalse(actualResponse.getPayload());
        verify(consentRestTemplate).put(URL, null, CONSENT_ID, newConsentStatus);
    }

    @Test
    void getConsentById() {
        when(consentRemoteUrls.getConsentById()).thenReturn(URL);
        CmsConsent cmsConsent = new CmsConsent();
        when(consentRestTemplate.getForEntity(URL, CmsConsent.class, CONSENT_ID))
            .thenReturn(ResponseEntity.ok(cmsConsent));

        CmsResponse<CmsConsent> actualResponse = consentServiceRemote.getConsentById(CONSENT_ID);

        assertTrue(actualResponse.isSuccessful());
        assertEquals(cmsConsent, actualResponse.getPayload());
    }

    @Test
    void getConsentById_noContent() {
        when(consentRemoteUrls.getConsentById()).thenReturn(URL);
        when(consentRestTemplate.getForEntity(URL, CmsConsent.class, CONSENT_ID))
            .thenReturn(ResponseEntity.noContent().build());

        CmsResponse<CmsConsent> actualResponse = consentServiceRemote.getConsentById(CONSENT_ID);

        assertTrue(actualResponse.hasError());
        assertEquals(CmsError.TECHNICAL_ERROR, actualResponse.getError());
    }

    @Test
    void getConsentById_cmsRestException() {
        when(consentRemoteUrls.getConsentById()).thenReturn(URL);
        when(consentRestTemplate.getForEntity(URL, CmsConsent.class, CONSENT_ID))
            .thenThrow(CmsRestException.class);

        CmsResponse<CmsConsent> actualResponse = consentServiceRemote.getConsentById(CONSENT_ID);

        assertTrue(actualResponse.hasError());
        assertEquals(CmsError.TECHNICAL_ERROR, actualResponse.getError());
    }

    @Test
    void findAndTerminateOldConsentsByNewConsentId() {
        when(consentRemoteUrls.findAndTerminateOldConsentsByNewConsentId()).thenReturn(URL);

        CmsResponse<Boolean> actualResponse = consentServiceRemote.findAndTerminateOldConsentsByNewConsentId(CONSENT_ID);

        assertTrue(actualResponse.isSuccessful());
        assertTrue(actualResponse.getPayload());
        verify(consentRestTemplate).delete(URL, CONSENT_ID);
    }

    @Test
    void findAndTerminateOldConsentsByNewConsentId_cmsRestException() {
        when(consentRemoteUrls.findAndTerminateOldConsentsByNewConsentId()).thenReturn(URL);
        doThrow(CmsRestException.class).when(consentRestTemplate).delete(URL, CONSENT_ID);

        CmsResponse<Boolean> actualResponse = consentServiceRemote.findAndTerminateOldConsentsByNewConsentId(CONSENT_ID);

        assertTrue(actualResponse.isSuccessful());
        assertFalse(actualResponse.getPayload());
        verify(consentRestTemplate).delete(URL, CONSENT_ID);
    }

    @Test
    void findAndTerminateOldConsents() {
        when(consentRemoteUrls.findAndTerminateOldConsents()).thenReturn(URL);
        TerminateOldConsentsRequest request = new TerminateOldConsentsRequest(false, false, null, null, null);

        CmsResponse<Boolean> actualResponse = consentServiceRemote.findAndTerminateOldConsents(CONSENT_ID, request);

        assertTrue(actualResponse.isSuccessful());
        assertTrue(actualResponse.getPayload());
        verify(consentRestTemplate).put(URL, request, CONSENT_ID);
    }

    @Test
    void findAndTerminateOldConsents_cmsRestException() {
        when(consentRemoteUrls.findAndTerminateOldConsents()).thenReturn(URL);
        TerminateOldConsentsRequest request = new TerminateOldConsentsRequest(false, false, null, null, null);
        doThrow(CmsRestException.class).when(consentRestTemplate).put(URL, request, CONSENT_ID);

        CmsResponse<Boolean> actualResponse = consentServiceRemote.findAndTerminateOldConsents(CONSENT_ID, request);

        assertTrue(actualResponse.isSuccessful());
        assertFalse(actualResponse.getPayload());
        verify(consentRestTemplate).put(URL, request, CONSENT_ID);
    }

    @Test
    void getPsuDataByConsentId() {
        when(consentRemoteUrls.getPsuDataByConsentId()).thenReturn(URL);
        List<PsuIdData> psuIdDataList = Collections.singletonList(new PsuIdData());
        // noinspection unchecked
        when(consentRestTemplate.exchange(eq(URL), eq(HttpMethod.GET), eq(null), any(ParameterizedTypeReference.class), eq(CONSENT_ID)))
            .thenReturn(ResponseEntity.ok(psuIdDataList));

        CmsResponse<List<PsuIdData>> actualResponse = consentServiceRemote.getPsuDataByConsentId(CONSENT_ID);

        assertTrue(actualResponse.isSuccessful());
        assertEquals(psuIdDataList, actualResponse.getPayload());
    }

    @Test
    void getPsuDataByConsentId_cmsRestException() {
        when(consentRemoteUrls.getPsuDataByConsentId()).thenReturn(URL);
        // noinspection unchecked
        when(consentRestTemplate.exchange(eq(URL), eq(HttpMethod.GET), eq(null), any(ParameterizedTypeReference.class), eq(CONSENT_ID)))
            .thenThrow(CmsRestException.class);

        CmsResponse<List<PsuIdData>> actualResponse = consentServiceRemote.getPsuDataByConsentId(CONSENT_ID);

        assertTrue(actualResponse.hasError());
        assertEquals(CmsError.TECHNICAL_ERROR, actualResponse.getError());
    }

    @Test
    void updateMultilevelScaRequired() {
        when(consentRemoteUrls.updateMultilevelScaRequired()).thenReturn(URL);
        when(consentRestTemplate.exchange(URL, HttpMethod.PUT, null, Boolean.class, CONSENT_ID, true))
            .thenReturn(ResponseEntity.ok(true));

        CmsResponse<Boolean> actualResponse = consentServiceRemote.updateMultilevelScaRequired(CONSENT_ID, true);

        assertTrue(actualResponse.isSuccessful());
        assertTrue(actualResponse.getPayload());
    }

    @Test
    void updateMultilevelScaRequired_cmsRestException() {
        when(consentRemoteUrls.updateMultilevelScaRequired()).thenReturn(URL);
        when(consentRestTemplate.exchange(URL, HttpMethod.PUT, null, Boolean.class, CONSENT_ID, true))
            .thenThrow(CmsRestException.class);

        CmsResponse<Boolean> actualResponse = consentServiceRemote.updateMultilevelScaRequired(CONSENT_ID, true);

        assertTrue(actualResponse.isSuccessful());
        assertFalse(actualResponse.getPayload());
    }
}
