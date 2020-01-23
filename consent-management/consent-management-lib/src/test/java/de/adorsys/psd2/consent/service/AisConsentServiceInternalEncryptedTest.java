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
import de.adorsys.psd2.consent.api.WrongChecksumException;
import de.adorsys.psd2.consent.api.ais.*;
import de.adorsys.psd2.consent.api.service.AisConsentService;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AisConsentServiceInternalEncryptedTest {
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

    @Test
    void createConsent_success() throws WrongChecksumException {
        // Given
        CreateAisConsentRequest request = buildCreateAisConsentRequest();
        CreateAisConsentResponse expected = new CreateAisConsentResponse(ENCRYPTED_CONSENT_ID, buildAisAccountConsent(), MODES);
        when(securityDataService.encryptId(DECRYPTED_CONSENT_ID)).thenReturn(Optional.of(ENCRYPTED_CONSENT_ID));
        when(aisConsentService.createConsent(any()))
            .thenReturn(CmsResponse.<CreateAisConsentResponse>builder()
                            .payload(new CreateAisConsentResponse(DECRYPTED_CONSENT_ID, buildAisAccountConsent(), MODES))
                            .build());

        // When
        CmsResponse<CreateAisConsentResponse> actualResponse = aisConsentServiceInternalEncrypted.createConsent(request);

        // Then
        assertTrue(actualResponse.isSuccessful());

        CreateAisConsentResponse actual = actualResponse.getPayload();
        assertEquals(expected, actual);
        verify(aisConsentService).createConsent(request);
    }

    @Test
    void createConsent_failure_internalServiceFailed() throws WrongChecksumException {
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
    void createConsent_failure_encryptionFailed() throws WrongChecksumException {
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
    void getConsentStatusById_success() {
        when(securityDataService.decryptId(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(DECRYPTED_CONSENT_ID));
        when(aisConsentService.getConsentStatusById(DECRYPTED_CONSENT_ID))
            .thenReturn(CmsResponse.<ConsentStatus>builder()
                            .payload(CONSENT_STATUS)
                            .build());

        // When
        CmsResponse<ConsentStatus> actual = aisConsentServiceInternalEncrypted.getConsentStatusById(ENCRYPTED_CONSENT_ID);

        // Then
        assertTrue(actual.isSuccessful());

        assertEquals(CONSENT_STATUS, actual.getPayload());
        verify(aisConsentService, times(1)).getConsentStatusById(DECRYPTED_CONSENT_ID);
    }

    @Test
    void getConsentStatusById_internalServiceFailed() {
        when(securityDataService.decryptId(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(DECRYPTED_CONSENT_ID));
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
    void getConsentStatusById_decryptionFailed() {
        // When
        CmsResponse<ConsentStatus> actual = aisConsentServiceInternalEncrypted.getConsentStatusById(UNDECRYPTABLE_CONSENT_ID);

        // Then
        assertTrue(actual.hasError());

        assertEquals(CmsError.TECHNICAL_ERROR, actual.getError());
        verify(aisConsentService, never()).getConsentStatusById(any());
    }

    @Test
    void updateConsentStatusById_success() throws WrongChecksumException {
        when(securityDataService.decryptId(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(DECRYPTED_CONSENT_ID));
        when(aisConsentService.updateConsentStatusById(DECRYPTED_CONSENT_ID, CONSENT_STATUS))
            .thenReturn(CmsResponse.<Boolean>builder()
                            .payload(true)
                            .build());
        // When
        CmsResponse<Boolean> actual = aisConsentServiceInternalEncrypted.updateConsentStatusById(ENCRYPTED_CONSENT_ID, CONSENT_STATUS);

        // Then
        assertTrue(actual.isSuccessful());

        assertTrue(actual.getPayload());
        verify(aisConsentService, times(1)).updateConsentStatusById(DECRYPTED_CONSENT_ID, CONSENT_STATUS);
    }

    @Test
    void updateConsentStatusById_internalServiceFailed() throws WrongChecksumException {
        when(securityDataService.decryptId(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(DECRYPTED_CONSENT_ID));
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
    void updateConsentStatusById_decryptionFailed() throws WrongChecksumException {
        // When
        CmsResponse<Boolean> actual = aisConsentServiceInternalEncrypted.updateConsentStatusById(UNDECRYPTABLE_CONSENT_ID, CONSENT_STATUS);

        // Then
        assertTrue(actual.hasError());

        assertEquals(CmsError.TECHNICAL_ERROR, actual.getError());
        verify(aisConsentService, never()).updateConsentStatusById(any(), any());
    }

    @Test
    void getAisAccountConsentById_success() {
        // Given
        AisAccountConsent expected = buildAisAccountConsent();
        when(securityDataService.decryptId(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(DECRYPTED_CONSENT_ID));
        when(aisConsentService.getAisAccountConsentById(DECRYPTED_CONSENT_ID))
            .thenReturn(CmsResponse.<AisAccountConsent>builder()
                            .payload(buildAisAccountConsent())
                            .build());

        // When
        CmsResponse<AisAccountConsent> actual = aisConsentServiceInternalEncrypted.getAisAccountConsentById(ENCRYPTED_CONSENT_ID);

        // Then
        assertTrue(actual.isSuccessful());

        assertEquals(expected, actual.getPayload());
        verify(aisConsentService, times(1)).getAisAccountConsentById(DECRYPTED_CONSENT_ID);
    }

    @Test
    void getAisAccountConsentById_internalServiceFailed() {
        when(securityDataService.decryptId(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(DECRYPTED_CONSENT_ID));
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
    void getAisAccountConsentById_decryptionFailed() {
        // When
        CmsResponse<AisAccountConsent> actual = aisConsentServiceInternalEncrypted.getAisAccountConsentById(UNDECRYPTABLE_CONSENT_ID);

        // Then
        assertTrue(actual.hasError());

        assertEquals(CmsError.TECHNICAL_ERROR, actual.getError());
        verify(aisConsentService, never()).getAisAccountConsentById(any());
    }

    @Test
    void checkConsentAndSaveActionLog_success() throws WrongChecksumException {
        // Given
        AisConsentActionRequest request = buildAisActionRequest(ENCRYPTED_CONSENT_ID);
        when(securityDataService.decryptId(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(DECRYPTED_CONSENT_ID));

        // When
        aisConsentServiceInternalEncrypted.checkConsentAndSaveActionLog(request);

        // Then
        AisConsentActionRequest decryptedRequest = buildAisActionRequest(DECRYPTED_CONSENT_ID);
        verify(aisConsentService, times(1)).checkConsentAndSaveActionLog(decryptedRequest);
    }

    @Test
    void checkConsentAndSaveActionLog_decryptionFailed() throws WrongChecksumException {
        // Given
        AisConsentActionRequest request = buildAisActionRequest(UNDECRYPTABLE_CONSENT_ID);

        // When
        aisConsentServiceInternalEncrypted.checkConsentAndSaveActionLog(request);

        // Then
        verify(aisConsentService, never()).checkConsentAndSaveActionLog(any());
    }

    @Test
    void updateAccountAccess_success() throws WrongChecksumException {
        // Given
        AisAccountAccessInfo accountAccessInfo = buildAisAccountAccessInfo();
        when(securityDataService.encryptId(DECRYPTED_CONSENT_ID)).thenReturn(Optional.of(ENCRYPTED_CONSENT_ID));
        when(securityDataService.decryptId(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(DECRYPTED_CONSENT_ID));
        when(aisConsentService.updateAspspAccountAccess(eq(DECRYPTED_CONSENT_ID), any()))
            .thenReturn(CmsResponse.<String>builder()
                            .payload(DECRYPTED_CONSENT_ID)
                            .build());

        // When
        CmsResponse<String> actual = aisConsentServiceInternalEncrypted.updateAspspAccountAccess(ENCRYPTED_CONSENT_ID, accountAccessInfo);

        // Then
        assertTrue(actual.isSuccessful());

        assertEquals(ENCRYPTED_CONSENT_ID, actual.getPayload());
        verify(aisConsentService, times(1)).updateAspspAccountAccess(DECRYPTED_CONSENT_ID, accountAccessInfo);
    }

    @Test
    void updateAccountAccess_internalServiceFailed() throws WrongChecksumException {
        when(securityDataService.decryptId(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(DECRYPTED_CONSENT_ID));
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
    void updateAccountAccess_decryptionFailed() throws WrongChecksumException {
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
    void getPsuDataByConsentId_success() {
        // Given
        List<PsuIdData> expected = Collections.singletonList(buildPsuIdData());
        when(securityDataService.decryptId(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(DECRYPTED_CONSENT_ID));
        when(aisConsentService.getPsuDataByConsentId(DECRYPTED_CONSENT_ID))
            .thenReturn(CmsResponse.<List<PsuIdData>>builder()
                            .payload(Collections.singletonList(buildPsuIdData()))
                            .build());

        // When
        CmsResponse<List<PsuIdData>> actual = aisConsentServiceInternalEncrypted.getPsuDataByConsentId(ENCRYPTED_CONSENT_ID);

        // Then
        assertTrue(actual.isSuccessful());

        assertEquals(expected, actual.getPayload());
        verify(aisConsentService, times(1)).getPsuDataByConsentId(DECRYPTED_CONSENT_ID);
    }

    @Test
    void getPsuDataByConsentId_internalServiceFailed() {
        when(securityDataService.decryptId(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(DECRYPTED_CONSENT_ID));
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
    void getPsuDataByConsentId_decryptionFailed() {
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
        return new PsuIdData(null, null, null, null, null);
    }
}

