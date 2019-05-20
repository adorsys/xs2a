/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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
import de.adorsys.psd2.consent.api.ais.AisAccountAccessInfo;
import de.adorsys.psd2.consent.api.ais.AisAccountConsent;
import de.adorsys.psd2.consent.api.ais.AisConsentActionRequest;
import de.adorsys.psd2.consent.api.ais.CreateAisConsentRequest;
import de.adorsys.psd2.consent.api.service.AisConsentService;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AisConsentServiceInternalEncryptedTest {
    private static final String ENCRYPTED_CONSENT_ID = "encrypted consent id";
    private static final String UNDECRYPTABLE_CONSENT_ID = "undecryptable consent id";
    private static final String DECRYPTED_CONSENT_ID = "255574b2-f115-4f3c-8d77-c1897749c060";
    private static final ConsentStatus CONSENT_STATUS = ConsentStatus.RECEIVED;

    @InjectMocks
    private AisConsentServiceInternalEncrypted aisConsentServiceInternalEncrypted;
    @Mock
    private AisConsentService aisConsentService;
    @Mock
    private SecurityDataService securityDataService;

    @Before
    public void setUp() {
        when(securityDataService.encryptId(DECRYPTED_CONSENT_ID))
            .thenReturn(Optional.of(ENCRYPTED_CONSENT_ID));
        when(securityDataService.decryptId(ENCRYPTED_CONSENT_ID))
            .thenReturn(Optional.of(DECRYPTED_CONSENT_ID));
        when(securityDataService.decryptId(UNDECRYPTABLE_CONSENT_ID))
            .thenReturn(Optional.empty());

        when(aisConsentService.createConsent(any()))
            .thenReturn(Optional.of(DECRYPTED_CONSENT_ID));
        when(aisConsentService.getConsentStatusById(DECRYPTED_CONSENT_ID))
            .thenReturn(Optional.of(CONSENT_STATUS));
        when(aisConsentService.updateConsentStatusById(DECRYPTED_CONSENT_ID, CONSENT_STATUS))
            .thenReturn(true);
        when(aisConsentService.updateAspspAccountAccess(eq(DECRYPTED_CONSENT_ID), any()))
            .thenReturn(Optional.of(DECRYPTED_CONSENT_ID));
        when(aisConsentService.getAisAccountConsentById(DECRYPTED_CONSENT_ID))
            .thenReturn(Optional.of(buildAisAccountConsent()));

        when(aisConsentService.getPsuDataByConsentId(DECRYPTED_CONSENT_ID))
            .thenReturn(Optional.of(Collections.singletonList(buildPsuIdData())));
    }

    @Test
    public void createConsent_success() {
        // Given
        CreateAisConsentRequest request = buildCreateAisConsentRequest();

        // When
        Optional<String> actual = aisConsentServiceInternalEncrypted.createConsent(request);

        // Then
        assertTrue(actual.isPresent());
        assertEquals(ENCRYPTED_CONSENT_ID, actual.get());
        verify(aisConsentService, times(1)).createConsent(request);
    }

    @Test
    public void createConsent_failure_internalServiceFailed() {
        when(aisConsentService.createConsent(any())).thenReturn(Optional.empty());

        // Given
        CreateAisConsentRequest request = buildCreateAisConsentRequest();

        // When
        Optional<String> actual = aisConsentServiceInternalEncrypted.createConsent(request);

        // Then
        assertFalse(actual.isPresent());
        verify(aisConsentService, times(1)).createConsent(request);
    }

    @Test
    public void createConsent_failure_encryptionFailed() {
        when(aisConsentService.createConsent(any())).thenReturn(Optional.empty());

        // Given
        CreateAisConsentRequest request = buildCreateAisConsentRequest();

        // When
        Optional<String> actual = aisConsentServiceInternalEncrypted.createConsent(request);

        // Then
        assertFalse(actual.isPresent());
        verify(aisConsentService, times(1)).createConsent(request);
    }

    @Test
    public void getConsentStatusById_success() {
        // When
        Optional<ConsentStatus> actual = aisConsentServiceInternalEncrypted.getConsentStatusById(ENCRYPTED_CONSENT_ID);

        // Then
        assertTrue(actual.isPresent());

        assertEquals(CONSENT_STATUS, actual.get());
        verify(aisConsentService, times(1)).getConsentStatusById(DECRYPTED_CONSENT_ID);
    }

    @Test
    public void getConsentStatusById_internalServiceFailed() {
        when(aisConsentService.getConsentStatusById(any())).thenReturn(Optional.empty());

        // When
        Optional<ConsentStatus> actual = aisConsentServiceInternalEncrypted.getConsentStatusById(ENCRYPTED_CONSENT_ID);

        // Then
        assertFalse(actual.isPresent());
        verify(aisConsentService, times(1)).getConsentStatusById(DECRYPTED_CONSENT_ID);
    }

    @Test
    public void getConsentStatusById_decryptionFailed() {
        // When
        Optional<ConsentStatus> actual = aisConsentServiceInternalEncrypted.getConsentStatusById(UNDECRYPTABLE_CONSENT_ID);

        // Then
        assertFalse(actual.isPresent());
        verify(aisConsentService, never()).getConsentStatusById(any());
    }

    @Test
    public void updateConsentStatusById_success() {
        // When
        boolean actual = aisConsentServiceInternalEncrypted.updateConsentStatusById(ENCRYPTED_CONSENT_ID, CONSENT_STATUS);

        // Then
        assertTrue(actual);
        verify(aisConsentService, times(1)).updateConsentStatusById(DECRYPTED_CONSENT_ID, CONSENT_STATUS);
    }

    @Test
    public void updateConsentStatusById_internalServiceFailed() {
        when(aisConsentService.updateConsentStatusById(any(), any())).thenReturn(false);

        // When
        boolean actual = aisConsentServiceInternalEncrypted.updateConsentStatusById(ENCRYPTED_CONSENT_ID, CONSENT_STATUS);

        // Then
        assertFalse(actual);
        verify(aisConsentService, times(1)).updateConsentStatusById(DECRYPTED_CONSENT_ID, CONSENT_STATUS);
    }

    @Test
    public void updateConsentStatusById_decryptionFailed() {
        // When
        boolean actual = aisConsentServiceInternalEncrypted.updateConsentStatusById(UNDECRYPTABLE_CONSENT_ID, CONSENT_STATUS);

        // Then
        assertFalse(actual);
        verify(aisConsentService, never()).updateConsentStatusById(any(), any());
    }

    @Test
    public void getAisAccountConsentById_success() {
        // Given
        AisAccountConsent expected = buildAisAccountConsent();

        // When
        Optional<AisAccountConsent> actual = aisConsentServiceInternalEncrypted.getAisAccountConsentById(ENCRYPTED_CONSENT_ID);

        // Then
        assertTrue(actual.isPresent());

        assertEquals(expected, actual.get());
        verify(aisConsentService, times(1)).getAisAccountConsentById(DECRYPTED_CONSENT_ID);
    }

    @Test
    public void getAisAccountConsentById_internalServiceFailed() {
        when(aisConsentService.getAisAccountConsentById(any())).thenReturn(Optional.empty());

        // When
        Optional<AisAccountConsent> actual = aisConsentServiceInternalEncrypted.getAisAccountConsentById(ENCRYPTED_CONSENT_ID);

        // Then
        assertFalse(actual.isPresent());
        verify(aisConsentService, times(1)).getAisAccountConsentById(DECRYPTED_CONSENT_ID);
    }

    @Test
    public void getAisAccountConsentById_decryptionFailed() {
        // When
        Optional<AisAccountConsent> actual = aisConsentServiceInternalEncrypted.getAisAccountConsentById(UNDECRYPTABLE_CONSENT_ID);

        // Then
        assertFalse(actual.isPresent());
        verify(aisConsentService, never()).getAisAccountConsentById(any());
    }

    @Test
    public void checkConsentAndSaveActionLog_success() {
        // Given
        AisConsentActionRequest request = buildAisActionRequest(ENCRYPTED_CONSENT_ID);

        // When
        aisConsentServiceInternalEncrypted.checkConsentAndSaveActionLog(request);

        // Then
        AisConsentActionRequest decryptedRequest = buildAisActionRequest(DECRYPTED_CONSENT_ID);
        verify(aisConsentService, times(1)).checkConsentAndSaveActionLog(decryptedRequest);
    }

    @Test
    public void checkConsentAndSaveActionLog_decryptionFailed() {
        // Given
        AisConsentActionRequest request = buildAisActionRequest(UNDECRYPTABLE_CONSENT_ID);

        // When
        aisConsentServiceInternalEncrypted.checkConsentAndSaveActionLog(request);

        // Then
        verify(aisConsentService, never()).checkConsentAndSaveActionLog(any());
    }

    @Test
    public void updateAccountAccess_success() {
        // Given
        AisAccountAccessInfo accountAccessInfo = buildAisAccountAccessInfo();

        // When
        Optional<String> actual = aisConsentServiceInternalEncrypted.updateAspspAccountAccess(ENCRYPTED_CONSENT_ID, accountAccessInfo);

        // Then
        assertTrue(actual.isPresent());

        assertEquals(ENCRYPTED_CONSENT_ID, actual.get());
        verify(aisConsentService, times(1)).updateAspspAccountAccess(DECRYPTED_CONSENT_ID, accountAccessInfo);
    }

    @Test
    public void updateAccountAccess_internalServiceFailed() {
        when(aisConsentService.updateAspspAccountAccess(any(), any())).thenReturn(Optional.empty());

        // Given
        AisAccountAccessInfo accountAccessInfo = buildAisAccountAccessInfo();

        // When
        Optional<String> actual = aisConsentServiceInternalEncrypted.updateAspspAccountAccess(ENCRYPTED_CONSENT_ID, accountAccessInfo);

        // Then
        assertFalse(actual.isPresent());
        verify(aisConsentService, times(1)).updateAspspAccountAccess(DECRYPTED_CONSENT_ID, accountAccessInfo);
    }

    @Test
    public void updateAccountAccess_decryptionFailed() {
        // Given
        AisAccountAccessInfo accountAccessInfo = buildAisAccountAccessInfo();

        // When
        Optional<String> actual = aisConsentServiceInternalEncrypted.updateAspspAccountAccess(UNDECRYPTABLE_CONSENT_ID, accountAccessInfo);

        // Then
        assertFalse(actual.isPresent());
        verify(aisConsentService, never()).updateAspspAccountAccess(any(), any());
    }

    @Test
    public void getPsuDataByConsentId_success() {
        // Given

        List<PsuIdData> expected = Collections.singletonList(buildPsuIdData());

        // When
        Optional<List<PsuIdData>> actual = aisConsentServiceInternalEncrypted.getPsuDataByConsentId(ENCRYPTED_CONSENT_ID);

        // Then
        assertTrue(actual.isPresent());

        assertEquals(expected, actual.get());
        verify(aisConsentService, times(1)).getPsuDataByConsentId(DECRYPTED_CONSENT_ID);
    }

    @Test
    public void getPsuDataByConsentId_internalServiceFailed() {
        when(aisConsentService.getPsuDataByConsentId(any())).thenReturn(Optional.empty());

        // When
        Optional<List<PsuIdData>> actual = aisConsentServiceInternalEncrypted.getPsuDataByConsentId(ENCRYPTED_CONSENT_ID);

        // Then
        assertFalse(actual.isPresent());
        verify(aisConsentService, times(1)).getPsuDataByConsentId(DECRYPTED_CONSENT_ID);
    }

    @Test
    public void getPsuDataByConsentId_decryptionFailed() {
        // When
        Optional<List<PsuIdData>> actual = aisConsentServiceInternalEncrypted.getPsuDataByConsentId(UNDECRYPTABLE_CONSENT_ID);

        // Then
        assertFalse(actual.isPresent());
        verify(aisConsentService, never()).getPsuDataByConsentId(any());
    }

    private CreateAisConsentRequest buildCreateAisConsentRequest() {
        return new CreateAisConsentRequest();
    }

    private AisAccountConsent buildAisAccountConsent() {
        return new AisAccountConsent();
    }

    private AisConsentActionRequest buildAisActionRequest(String consentId) {
        return new AisConsentActionRequest("tpp id", consentId, ActionStatus.SUCCESS, "request/uri", true);
    }

    private AisAccountAccessInfo buildAisAccountAccessInfo() {
        return new AisAccountAccessInfo();
    }

    private PsuIdData buildPsuIdData() {
        return new PsuIdData(null, null, null, null);
    }
}
