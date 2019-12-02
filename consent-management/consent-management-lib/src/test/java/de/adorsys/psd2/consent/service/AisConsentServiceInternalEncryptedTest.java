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


package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.ActionStatus;
import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.ais.*;
import de.adorsys.psd2.consent.api.service.AisConsentService;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AisConsentServiceInternalEncryptedTest {
    private static final String ENCRYPTED_CONSENT_ID = "encrypted consent id";
    private static final String UNDECRYPTABLE_CONSENT_ID = "undecryptable consent id";
    private static final String DECRYPTED_CONSENT_ID = "255574b2-f115-4f3c-8d77-c1897749c060";
    private static final String UNENCRYPTABLE_CONSENT_ID = "unencryptable consent id";
    private static final ConsentStatus CONSENT_STATUS = ConsentStatus.RECEIVED;
    private static final List<NotificationSupportedMode> MODES = Arrays.asList(NotificationSupportedMode.LAST, NotificationSupportedMode.SCA);

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
        when(securityDataService.encryptId(UNENCRYPTABLE_CONSENT_ID))
            .thenReturn(Optional.empty());

        when(aisConsentService.createConsent(any()))
            .thenReturn(CmsResponse.<CreateAisConsentResponse>builder()
                            .payload(new CreateAisConsentResponse(DECRYPTED_CONSENT_ID, buildAisAccountConsent(), MODES))
                            .build());
        when(aisConsentService.getConsentStatusById(DECRYPTED_CONSENT_ID))
            .thenReturn(CmsResponse.<ConsentStatus>builder()
                            .payload(CONSENT_STATUS)
                            .build());
        when(aisConsentService.updateConsentStatusById(DECRYPTED_CONSENT_ID, CONSENT_STATUS))
            .thenReturn(CmsResponse.<Boolean>builder()
                            .payload(true)
                            .build());
        when(aisConsentService.updateAspspAccountAccess(eq(DECRYPTED_CONSENT_ID), any()))
            .thenReturn(CmsResponse.<String>builder()
                            .payload(DECRYPTED_CONSENT_ID)
                            .build());
        when(aisConsentService.getAisAccountConsentById(DECRYPTED_CONSENT_ID))
            .thenReturn(CmsResponse.<AisAccountConsent>builder()
                            .payload(buildAisAccountConsent())
                            .build());

        when(aisConsentService.getPsuDataByConsentId(DECRYPTED_CONSENT_ID))
            .thenReturn(CmsResponse.<List<PsuIdData>>builder()
                            .payload(Collections.singletonList(buildPsuIdData()))
                            .build());
    }

    @Test
    public void createConsent_success() {
        // Given
        CreateAisConsentRequest request = buildCreateAisConsentRequest();
        CreateAisConsentResponse expected = new CreateAisConsentResponse(ENCRYPTED_CONSENT_ID, buildAisAccountConsent(), MODES);

        // When
        CmsResponse<CreateAisConsentResponse> actualResponse = aisConsentServiceInternalEncrypted.createConsent(request);

        // Then
        assertTrue(actualResponse.isSuccessful());

        CreateAisConsentResponse actual = actualResponse.getPayload();
        assertEquals(expected, actual);
        verify(aisConsentService).createConsent(request);
    }

    @Test
    public void createConsent_failure_internalServiceFailed() {
        when(aisConsentService.createConsent(any()))
            .thenReturn(CmsResponse.<CreateAisConsentResponse>builder()
                            .error(CmsError.LOGICAL_ERROR)
                            .build());

        // Given
        CreateAisConsentRequest request = buildCreateAisConsentRequest();

        // When
        CmsResponse<CreateAisConsentResponse> actual = aisConsentServiceInternalEncrypted.createConsent(request);

        // Then
        assertTrue(actual.hasError());

        assertEquals(CmsError.LOGICAL_ERROR, actual.getError());
        verify(aisConsentService).createConsent(request);
    }

    @Test
    public void createConsent_failure_encryptionFailed() {
        // Given
        CreateAisConsentRequest request = buildCreateAisConsentRequest();
        when(aisConsentService.createConsent(any()))
            .thenReturn(CmsResponse.<CreateAisConsentResponse>builder()
                            .payload(new CreateAisConsentResponse(UNENCRYPTABLE_CONSENT_ID, buildAisAccountConsent(), MODES))
                            .build());

        // When
        CmsResponse<CreateAisConsentResponse> actual = aisConsentServiceInternalEncrypted.createConsent(request);

        // Then
        assertTrue(actual.hasError());

        assertEquals(CmsError.TECHNICAL_ERROR, actual.getError());
        verify(aisConsentService).createConsent(request);
    }

    @Test
    public void getConsentStatusById_success() {
        // When
        CmsResponse<ConsentStatus> actual = aisConsentServiceInternalEncrypted.getConsentStatusById(ENCRYPTED_CONSENT_ID);

        // Then
        assertTrue(actual.isSuccessful());

        assertEquals(CONSENT_STATUS, actual.getPayload());
        verify(aisConsentService, times(1)).getConsentStatusById(DECRYPTED_CONSENT_ID);
    }

    @Test
    public void getConsentStatusById_internalServiceFailed() {
        when(aisConsentService.getConsentStatusById(any()))
            .thenReturn(CmsResponse.<ConsentStatus>builder()
                            .error(CmsError.LOGICAL_ERROR)
                            .build());

        // When
        CmsResponse<ConsentStatus> actual = aisConsentServiceInternalEncrypted.getConsentStatusById(ENCRYPTED_CONSENT_ID);

        // Then
        assertTrue(actual.hasError());

        assertEquals(CmsError.LOGICAL_ERROR, actual.getError());
        verify(aisConsentService, times(1)).getConsentStatusById(DECRYPTED_CONSENT_ID);
    }

    @Test
    public void getConsentStatusById_decryptionFailed() {
        // When
        CmsResponse<ConsentStatus> actual = aisConsentServiceInternalEncrypted.getConsentStatusById(UNDECRYPTABLE_CONSENT_ID);

        // Then
        assertTrue(actual.hasError());

        assertEquals(CmsError.TECHNICAL_ERROR, actual.getError());
        verify(aisConsentService, never()).getConsentStatusById(any());
    }

    @Test
    public void updateConsentStatusById_success() {
        // When
        CmsResponse<Boolean> actual = aisConsentServiceInternalEncrypted.updateConsentStatusById(ENCRYPTED_CONSENT_ID, CONSENT_STATUS);

        // Then
        assertTrue(actual.isSuccessful());

        assertTrue(actual.getPayload());
        verify(aisConsentService, times(1)).updateConsentStatusById(DECRYPTED_CONSENT_ID, CONSENT_STATUS);
    }

    @Test
    public void updateConsentStatusById_internalServiceFailed() {
        when(aisConsentService.updateConsentStatusById(any(), any()))
            .thenReturn(CmsResponse.<Boolean>builder()
                            .error(CmsError.LOGICAL_ERROR)
                            .build());

        // When
        CmsResponse<Boolean> actual = aisConsentServiceInternalEncrypted.updateConsentStatusById(ENCRYPTED_CONSENT_ID, CONSENT_STATUS);

        // Then
        assertTrue(actual.hasError());

        assertEquals(CmsError.LOGICAL_ERROR, actual.getError());
        verify(aisConsentService, times(1)).updateConsentStatusById(DECRYPTED_CONSENT_ID, CONSENT_STATUS);
    }

    @Test
    public void updateConsentStatusById_decryptionFailed() {
        // When
        CmsResponse<Boolean> actual = aisConsentServiceInternalEncrypted.updateConsentStatusById(UNDECRYPTABLE_CONSENT_ID, CONSENT_STATUS);

        // Then
        assertTrue(actual.hasError());

        assertEquals(CmsError.TECHNICAL_ERROR, actual.getError());
        verify(aisConsentService, never()).updateConsentStatusById(any(), any());
    }

    @Test
    public void getAisAccountConsentById_success() {
        // Given
        AisAccountConsent expected = buildAisAccountConsent();

        // When
        CmsResponse<AisAccountConsent> actual = aisConsentServiceInternalEncrypted.getAisAccountConsentById(ENCRYPTED_CONSENT_ID);

        // Then
        assertTrue(actual.isSuccessful());

        assertEquals(expected, actual.getPayload());
        verify(aisConsentService, times(1)).getAisAccountConsentById(DECRYPTED_CONSENT_ID);
    }

    @Test
    public void getAisAccountConsentById_internalServiceFailed() {
        when(aisConsentService.getAisAccountConsentById(any()))
            .thenReturn(CmsResponse.<AisAccountConsent>builder()
                            .error(CmsError.LOGICAL_ERROR)
                            .build());

        // When
        CmsResponse<AisAccountConsent> actual = aisConsentServiceInternalEncrypted.getAisAccountConsentById(ENCRYPTED_CONSENT_ID);

        // Then
        assertTrue(actual.hasError());

        assertEquals(CmsError.LOGICAL_ERROR, actual.getError());
        verify(aisConsentService, times(1)).getAisAccountConsentById(DECRYPTED_CONSENT_ID);
    }

    @Test
    public void getAisAccountConsentById_decryptionFailed() {
        // When
        CmsResponse<AisAccountConsent> actual = aisConsentServiceInternalEncrypted.getAisAccountConsentById(UNDECRYPTABLE_CONSENT_ID);

        // Then
        assertTrue(actual.hasError());

        assertEquals(CmsError.TECHNICAL_ERROR, actual.getError());
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
        CmsResponse<String> actual = aisConsentServiceInternalEncrypted.updateAspspAccountAccess(ENCRYPTED_CONSENT_ID, accountAccessInfo);

        // Then
        assertTrue(actual.isSuccessful());

        assertEquals(ENCRYPTED_CONSENT_ID, actual.getPayload());
        verify(aisConsentService, times(1)).updateAspspAccountAccess(DECRYPTED_CONSENT_ID, accountAccessInfo);
    }

    @Test
    public void updateAccountAccess_internalServiceFailed() {
        when(aisConsentService.updateAspspAccountAccess(any(), any()))
            .thenReturn(CmsResponse.<String>builder()
                            .error(CmsError.LOGICAL_ERROR)
                            .build());

        // Given
        AisAccountAccessInfo accountAccessInfo = buildAisAccountAccessInfo();

        // When
        CmsResponse<String> actual = aisConsentServiceInternalEncrypted.updateAspspAccountAccess(ENCRYPTED_CONSENT_ID, accountAccessInfo);

        // Then
        assertTrue(actual.hasError());

        assertEquals(CmsError.LOGICAL_ERROR, actual.getError());
        verify(aisConsentService, times(1)).updateAspspAccountAccess(DECRYPTED_CONSENT_ID, accountAccessInfo);
    }

    @Test
    public void updateAccountAccess_decryptionFailed() {
        // Given
        AisAccountAccessInfo accountAccessInfo = buildAisAccountAccessInfo();

        // When
        CmsResponse<String> actual = aisConsentServiceInternalEncrypted.updateAspspAccountAccess(UNDECRYPTABLE_CONSENT_ID, accountAccessInfo);

        // Then
        assertTrue(actual.hasError());

        assertEquals(CmsError.TECHNICAL_ERROR, actual.getError());
        verify(aisConsentService, never()).updateAspspAccountAccess(any(), any());
    }

    @Test
    public void getPsuDataByConsentId_success() {
        // Given

        List<PsuIdData> expected = Collections.singletonList(buildPsuIdData());

        // When
        CmsResponse<List<PsuIdData>> actual = aisConsentServiceInternalEncrypted.getPsuDataByConsentId(ENCRYPTED_CONSENT_ID);

        // Then
        assertTrue(actual.isSuccessful());

        assertEquals(expected, actual.getPayload());
        verify(aisConsentService, times(1)).getPsuDataByConsentId(DECRYPTED_CONSENT_ID);
    }

    @Test
    public void getPsuDataByConsentId_internalServiceFailed() {
        when(aisConsentService.getPsuDataByConsentId(any()))
            .thenReturn(CmsResponse.<List<PsuIdData>>builder()
                            .error(CmsError.LOGICAL_ERROR)
                            .build());

        // When
        CmsResponse<List<PsuIdData>> actual = aisConsentServiceInternalEncrypted.getPsuDataByConsentId(ENCRYPTED_CONSENT_ID);

        // Then
        assertTrue(actual.hasError());

        assertEquals(CmsError.LOGICAL_ERROR, actual.getError());
        verify(aisConsentService, times(1)).getPsuDataByConsentId(DECRYPTED_CONSENT_ID);
    }

    @Test
    public void getPsuDataByConsentId_decryptionFailed() {
        // When
        CmsResponse<List<PsuIdData>> actual = aisConsentServiceInternalEncrypted.getPsuDataByConsentId(UNDECRYPTABLE_CONSENT_ID);

        // Then
        assertTrue(actual.hasError());

        assertEquals(CmsError.TECHNICAL_ERROR, actual.getError());
        verify(aisConsentService, never()).getPsuDataByConsentId(any());
    }

    private CreateAisConsentRequest buildCreateAisConsentRequest() {
        return new CreateAisConsentRequest();
    }

    private AisAccountConsent buildAisAccountConsent() {
        return new AisAccountConsent();
    }

    private AisConsentActionRequest buildAisActionRequest(String consentId) {
        return new AisConsentActionRequest("tpp id", consentId, ActionStatus.SUCCESS, "request/uri", true, null, null);
    }

    private AisAccountAccessInfo buildAisAccountAccessInfo() {
        return new AisAccountAccessInfo();
    }

    private PsuIdData buildPsuIdData() {
        return new PsuIdData(null, null, null, null);
    }
}

