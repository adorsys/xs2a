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

package de.adorsys.psd2.consent.web.psu.controller;

import de.adorsys.psd2.consent.api.ais.AisAccountConsent;
import de.adorsys.psd2.consent.psu.api.CmsPsuAisService;
import de.adorsys.psd2.consent.api.ais.CmsAisConsentResponse;
import de.adorsys.psd2.xs2a.core.exception.AuthorisationIsExpiredException;
import de.adorsys.psd2.xs2a.core.exception.RedirectUrlIsExpiredException;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CmsPsuAisControllerTest {

    private static final String CONSENT_ID = "consent id";
    private static final String AUTHORISATION_ID = "authorisation id";
    private static final String PSU_ID = "psu id";
    private static final String PSU_ID_TYPE = "psu id type";
    private static final String PSU_CORPORATE_ID = "psu corporate id";
    private static final String PSU_CORPORATE_ID_TYPE = "psu corporate id type";
    private static final String INSTANCE_ID = "instance id";
    private static final String SCA_STATUS_RECEIVED = "RECEIVED";
    private static final String NOK_REDIRECT_URI = "http://everything_is_bad.html";

    private PsuIdData psuIdData;

    @InjectMocks
    private CmsPsuAisController cmsPsuAisController;

    @Mock
    private CmsPsuAisService cmsPsuAisService;

    @Before
    public void init() {
        psuIdData = new PsuIdData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE);
    }

    @Test
    public void updatePsuDataInConsent_withValidRequest_shouldReturnOk() throws AuthorisationIsExpiredException {
        // Given
        when(cmsPsuAisService.updatePsuDataInConsent(psuIdData, AUTHORISATION_ID, INSTANCE_ID))
            .thenReturn(true);

        // When
        ResponseEntity actualResponse = cmsPsuAisController.updatePsuDataInConsent(CONSENT_ID, AUTHORISATION_ID, INSTANCE_ID, psuIdData);

        // Then
        verify(cmsPsuAisService).updatePsuDataInConsent(psuIdData, AUTHORISATION_ID, INSTANCE_ID);

        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertNull(actualResponse.getBody());
    }

    @Test
    public void updatePsuDataInConsent_withWrongRequest_shouldReturnBadRequest() throws AuthorisationIsExpiredException {
        // Given
        when(cmsPsuAisService.updatePsuDataInConsent(psuIdData, AUTHORISATION_ID, INSTANCE_ID))
            .thenReturn(false);

        // When
        ResponseEntity actualResponse = cmsPsuAisController.updatePsuDataInConsent(CONSENT_ID, AUTHORISATION_ID, INSTANCE_ID, psuIdData);

        // Then
        verify(cmsPsuAisService).updatePsuDataInConsent(psuIdData, AUTHORISATION_ID, INSTANCE_ID);

        assertEquals(HttpStatus.BAD_REQUEST, actualResponse.getStatusCode());
        assertNull(actualResponse.getBody());
    }

    @Test
    public void updatePsuDataInConsent_withExpiredAuthorisation_shouldReturnRequestTimeout() throws AuthorisationIsExpiredException {
        // Given
        when(cmsPsuAisService.updatePsuDataInConsent(psuIdData, AUTHORISATION_ID, INSTANCE_ID))
            .thenThrow(new AuthorisationIsExpiredException(NOK_REDIRECT_URI));

        // When
        ResponseEntity<CmsAisConsentResponse> actualResponse = cmsPsuAisController.updatePsuDataInConsent(CONSENT_ID, AUTHORISATION_ID, INSTANCE_ID, psuIdData);

        // Then
        verify(cmsPsuAisService).updatePsuDataInConsent(psuIdData, AUTHORISATION_ID, INSTANCE_ID);

        assertEquals(HttpStatus.REQUEST_TIMEOUT, actualResponse.getStatusCode());
        assertNotNull(actualResponse.getBody());
        assertEquals(NOK_REDIRECT_URI, actualResponse.getBody().getTppNokRedirectUri());
    }

    @Test
    public void getConsent_withValidRequest_shouldReturnOk() {
        // Given
        when(cmsPsuAisService.getConsent(psuIdData, CONSENT_ID, INSTANCE_ID))
            .thenReturn(Optional.of(new AisAccountConsent()));

        // When
        ResponseEntity<AisAccountConsent> actualResponse = cmsPsuAisController.getConsentByConsentId(CONSENT_ID, PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, INSTANCE_ID);

        // Then
        verify(cmsPsuAisService).getConsent(psuIdData, CONSENT_ID, INSTANCE_ID);

        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertNotNull(actualResponse.getBody());
    }

    @Test
    public void getConsent_withNonExistingConsent_shouldReturnNotFound() {
        // Given
        when(cmsPsuAisService.getConsent(psuIdData, CONSENT_ID, INSTANCE_ID))
            .thenReturn(Optional.empty());

        // When
        ResponseEntity<AisAccountConsent> actualResponse = cmsPsuAisController.getConsentByConsentId(CONSENT_ID, PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, INSTANCE_ID);

        // Then
        verify(cmsPsuAisService).getConsent(psuIdData, CONSENT_ID, INSTANCE_ID);

        assertEquals(HttpStatus.NOT_FOUND, actualResponse.getStatusCode());
        assertNull(actualResponse.getBody());
    }

    @Test
    public void updateAuthorisationStatus_withValidRequest_shouldReturnOk() throws AuthorisationIsExpiredException {
        // Given
        when(cmsPsuAisService.updateAuthorisationStatus(psuIdData, CONSENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID))
            .thenReturn(true);

        // When
        ResponseEntity actualResponse = cmsPsuAisController.updateAuthorisationStatus(CONSENT_ID, SCA_STATUS_RECEIVED, AUTHORISATION_ID, PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, INSTANCE_ID);

        // Then
        verify(cmsPsuAisService).updateAuthorisationStatus(psuIdData, CONSENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID);

        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertNull(actualResponse.getBody());
    }

    @Test
    public void updateAuthorisationStatus_withValidRequestAndLowercaseScaStatus_shouldReturnOk() throws AuthorisationIsExpiredException {
        // Given
        when(cmsPsuAisService.updateAuthorisationStatus(psuIdData, CONSENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID))
            .thenReturn(true);
        String lowercaseScaStatus = SCA_STATUS_RECEIVED.toLowerCase();

        // When
        ResponseEntity actualResponse = cmsPsuAisController.updateAuthorisationStatus(CONSENT_ID, lowercaseScaStatus, AUTHORISATION_ID, PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, INSTANCE_ID);

        // Then
        verify(cmsPsuAisService).updateAuthorisationStatus(psuIdData, CONSENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID);

        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertNull(actualResponse.getBody());
    }

    @Test
    public void updateAuthorisationStatus_withFalseFromService_shouldReturnBadRequest() throws AuthorisationIsExpiredException {
        // Given
        when(cmsPsuAisService.updateAuthorisationStatus(psuIdData, CONSENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID))
            .thenReturn(false);

        // When
        ResponseEntity actualResponse = cmsPsuAisController.updateAuthorisationStatus(CONSENT_ID, SCA_STATUS_RECEIVED, AUTHORISATION_ID, PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, INSTANCE_ID);

        // Then
        verify(cmsPsuAisService).updateAuthorisationStatus(psuIdData, CONSENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID);

        assertEquals(HttpStatus.BAD_REQUEST, actualResponse.getStatusCode());
        assertNull(actualResponse.getBody());
    }

    @Test
    public void updateAuthorisationStatus_withInvalidScaStatus_shouldReturnBadRequest() throws AuthorisationIsExpiredException {
        // Given
        String invalidScaStatus = "invalid SCA status";

        // When
        ResponseEntity actualResponse = cmsPsuAisController.updateAuthorisationStatus(CONSENT_ID, invalidScaStatus, AUTHORISATION_ID, PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, INSTANCE_ID);

        // Then
        verify(cmsPsuAisService, never()).updateAuthorisationStatus(any(), anyString(), anyString(), any(), anyString());

        assertEquals(HttpStatus.BAD_REQUEST, actualResponse.getStatusCode());
        assertNull(actualResponse.getBody());
    }

    @Test
    public void updateAuthorisationStatus_withExpiredAuthorisation_shouldReturnRequestTimeout() throws AuthorisationIsExpiredException {
        // Given
        when(cmsPsuAisService.updateAuthorisationStatus(psuIdData, CONSENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID))
            .thenThrow(new AuthorisationIsExpiredException(NOK_REDIRECT_URI));

        // When
        ResponseEntity<CmsAisConsentResponse> actualResponse = cmsPsuAisController.updateAuthorisationStatus(CONSENT_ID, SCA_STATUS_RECEIVED, AUTHORISATION_ID, PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, INSTANCE_ID);

        // Then
        verify(cmsPsuAisService).updateAuthorisationStatus(psuIdData, CONSENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID);

        assertEquals(HttpStatus.REQUEST_TIMEOUT, actualResponse.getStatusCode());
        CmsAisConsentResponse body = actualResponse.getBody();
        assertNotNull(body);
        assertNotNull(body.getTppNokRedirectUri());
        assertEquals(NOK_REDIRECT_URI, body.getTppNokRedirectUri());
    }

    @Test
    public void confirmConsent_withTrueRequest_shouldReturnOk() {
        // Given
        when(cmsPsuAisService.confirmConsent(CONSENT_ID, INSTANCE_ID))
            .thenReturn(true);

        // When
        ResponseEntity<Boolean> actualResponse = cmsPsuAisController.confirmConsent(CONSENT_ID, PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, INSTANCE_ID);

        // Then
        verify(cmsPsuAisService).confirmConsent(CONSENT_ID, INSTANCE_ID);

        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertNotNull(actualResponse.getBody());
        assertTrue(actualResponse.getBody());
    }

    @Test
    public void confirmConsent_withFalseRequest_shouldReturnOk() {
        // Given
        when(cmsPsuAisService.confirmConsent(CONSENT_ID, INSTANCE_ID))
            .thenReturn(false);

        // When
        ResponseEntity<Boolean> actualResponse = cmsPsuAisController.confirmConsent(CONSENT_ID, PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, INSTANCE_ID);

        // Then
        verify(cmsPsuAisService).confirmConsent(CONSENT_ID, INSTANCE_ID);

        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertNotNull(actualResponse.getBody());
        assertFalse(actualResponse.getBody());
    }

    @Test
    public void rejectConsent_withTrueRequest_shouldReturnOk() {
        // Given
        when(cmsPsuAisService.rejectConsent(CONSENT_ID, INSTANCE_ID))
            .thenReturn(true);

        // When
        ResponseEntity<Boolean> actualResponse = cmsPsuAisController.rejectConsent(CONSENT_ID, PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, INSTANCE_ID);

        // Then
        verify(cmsPsuAisService).rejectConsent(CONSENT_ID, INSTANCE_ID);

        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertNotNull(actualResponse.getBody());
        assertTrue(actualResponse.getBody());
    }

    @Test
    public void rejectConsent_withFalseRequest_shouldReturnOk() {
        // Given
        when(cmsPsuAisService.rejectConsent(CONSENT_ID, INSTANCE_ID))
            .thenReturn(false);

        // When
        ResponseEntity<Boolean> actualResponse = cmsPsuAisController.rejectConsent(CONSENT_ID, PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, INSTANCE_ID);

        // Then
        verify(cmsPsuAisService).rejectConsent(CONSENT_ID, INSTANCE_ID);

        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertNotNull(actualResponse.getBody());
        assertFalse(actualResponse.getBody());
    }

    @Test
    public void getConsentsForPsu_withTrueRequest_shouldReturnOk() {
        // Given
        when(cmsPsuAisService.getConsentsForPsu(psuIdData, INSTANCE_ID))
            .thenReturn(Collections.singletonList(new AisAccountConsent()));

        // When
        ResponseEntity<List<AisAccountConsent>> actualResponse = cmsPsuAisController.getConsentsForPsu(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, INSTANCE_ID);

        // Then
        verify(cmsPsuAisService).getConsentsForPsu(psuIdData, INSTANCE_ID);

        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertNotNull(actualResponse.getBody());
    }

    @Test
    public void getConsentsForPsu_withFalseRequest_shouldReturnOk() {
        // Given
        when(cmsPsuAisService.getConsentsForPsu(psuIdData, INSTANCE_ID))
            .thenReturn(Collections.emptyList());

        // When
        ResponseEntity<List<AisAccountConsent>> actualResponse = cmsPsuAisController.getConsentsForPsu(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, INSTANCE_ID);

        // Then
        verify(cmsPsuAisService).getConsentsForPsu(psuIdData, INSTANCE_ID);

        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertNotNull(actualResponse.getBody());
        assertTrue(actualResponse.getBody().isEmpty());
    }

    @Test
    public void revokeConsent_withTrueRequest_shouldReturnOk() {
        // Given
        when(cmsPsuAisService.revokeConsent(CONSENT_ID, INSTANCE_ID))
            .thenReturn(true);

        // When
        ResponseEntity<Boolean> actualResponse = cmsPsuAisController.revokeConsent(CONSENT_ID, PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, INSTANCE_ID);

        // Then
        verify(cmsPsuAisService).revokeConsent(CONSENT_ID, INSTANCE_ID);

        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertNotNull(actualResponse.getBody());
        assertTrue(actualResponse.getBody());
    }

    @Test
    public void revokeConsent_withFalseRequest_shouldReturnOk() {
        // Given
        when(cmsPsuAisService.revokeConsent(CONSENT_ID, INSTANCE_ID))
            .thenReturn(false);

        // When
        ResponseEntity<Boolean> actualResponse = cmsPsuAisController.revokeConsent(CONSENT_ID, PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, INSTANCE_ID);

        // Then
        verify(cmsPsuAisService).revokeConsent(CONSENT_ID, INSTANCE_ID);

        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertNotNull(actualResponse.getBody());
        assertFalse(actualResponse.getBody());
    }

    @Test
    public void getConsentIdByRedirectId_withValidRequest_shouldReturnOk() throws RedirectUrlIsExpiredException {
        // Given
        when(cmsPsuAisService.checkRedirectAndGetConsent(AUTHORISATION_ID, INSTANCE_ID))
            .thenReturn(Optional.of(new CmsAisConsentResponse(new AisAccountConsent(), null, null, null)));

        // When
        ResponseEntity actualResponse = cmsPsuAisController.getConsentIdByRedirectId(AUTHORISATION_ID, INSTANCE_ID);

        // Then
        verify(cmsPsuAisService).checkRedirectAndGetConsent(AUTHORISATION_ID, INSTANCE_ID);

        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertNotNull(actualResponse.getBody());
    }

    @Test
    public void getConsentIdByRedirectId_withWrongRequest_shouldReturnNotFound() throws RedirectUrlIsExpiredException {
        // Given
        when(cmsPsuAisService.checkRedirectAndGetConsent(AUTHORISATION_ID, INSTANCE_ID))
            .thenReturn(Optional.empty());

        // When
        ResponseEntity actualResponse = cmsPsuAisController.getConsentIdByRedirectId(AUTHORISATION_ID, INSTANCE_ID);

        // Then
        verify(cmsPsuAisService).checkRedirectAndGetConsent(AUTHORISATION_ID, INSTANCE_ID);

        assertEquals(HttpStatus.NOT_FOUND, actualResponse.getStatusCode());
        assertNull(actualResponse.getBody());
    }

    @Test
    public void getConsentIdByRedirectId_withExpiredAuthorisation_shouldReturnRequestTimeout() throws RedirectUrlIsExpiredException {
        // Given
        when(cmsPsuAisService.checkRedirectAndGetConsent(AUTHORISATION_ID, INSTANCE_ID))
            .thenThrow(new RedirectUrlIsExpiredException(NOK_REDIRECT_URI));

        // When
        ResponseEntity<CmsAisConsentResponse> actualResponse = cmsPsuAisController.getConsentIdByRedirectId(AUTHORISATION_ID, INSTANCE_ID);

        // Then
        verify(cmsPsuAisService).checkRedirectAndGetConsent(AUTHORISATION_ID, INSTANCE_ID);

        assertEquals(HttpStatus.REQUEST_TIMEOUT, actualResponse.getStatusCode());
        assertNotNull(actualResponse.getBody());
        assertEquals(NOK_REDIRECT_URI, actualResponse.getBody().getTppNokRedirectUri());
    }
}
