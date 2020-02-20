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
import de.adorsys.psd2.consent.api.ais.AisAccountAccessInfo;
import de.adorsys.psd2.consent.api.ais.AisConsentActionRequest;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.service.AisConsentService;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AisConsentServiceInternalEncryptedTest {
    private static final String ENCRYPTED_CONSENT_ID = "DfLtDOgo1tTK6WQlHlb-TMPL2pkxRlhZ4feMa5F4tOWwNN45XLNAVfWwoZUKlQwb_=_bS6p6XvTWI";
    private static final String UNDECRYPTABLE_CONSENT_ID = "0000000000TK6WQlHlb-TMPL2pkxRlhZ4feMa5F4tOWwNN45XLNAVfWwoZUKlQwb_=_bS6p6XvTWI";
    private static final String DECRYPTED_CONSENT_ID = "255574b2-f115-4f3c-8d77-c1897749c060";

    @InjectMocks
    private AisConsentServiceInternalEncrypted aisConsentServiceInternalEncrypted;
    @Mock
    private AisConsentService aisConsentService;
    @Mock
    private SecurityDataService securityDataService;

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
        CmsConsent cmsConsent = buildCmsConsent();
        AisAccountAccessInfo accountAccessInfo = buildAisAccountAccessInfo();
        when(securityDataService.decryptId(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(DECRYPTED_CONSENT_ID));
        when(aisConsentService.updateAspspAccountAccess(eq(DECRYPTED_CONSENT_ID), any()))
            .thenReturn(CmsResponse.<CmsConsent>builder()
                            .payload(cmsConsent)
                            .build());

        // When
        CmsResponse<CmsConsent> actual = aisConsentServiceInternalEncrypted.updateAspspAccountAccess(ENCRYPTED_CONSENT_ID, accountAccessInfo);

        // Then
        assertTrue(actual.isSuccessful());

        assertEquals(DECRYPTED_CONSENT_ID, actual.getPayload().getId());
        verify(aisConsentService, times(1)).updateAspspAccountAccess(DECRYPTED_CONSENT_ID, accountAccessInfo);
    }

    @Test
    void updateAccountAccess_internalServiceFailed() throws WrongChecksumException {
        when(securityDataService.decryptId(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(DECRYPTED_CONSENT_ID));
        when(aisConsentService.updateAspspAccountAccess(any(), any()))
            .thenReturn(CmsResponse.<CmsConsent>builder()
                            .error(CmsError.LOGICAL_ERROR)
                            .build());

        // Given
        AisAccountAccessInfo accountAccessInfo = buildAisAccountAccessInfo();

        // When
        CmsResponse<CmsConsent> actual = aisConsentServiceInternalEncrypted.updateAspspAccountAccess(ENCRYPTED_CONSENT_ID, accountAccessInfo);

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
        CmsResponse<CmsConsent> actual = aisConsentServiceInternalEncrypted.updateAspspAccountAccess(UNDECRYPTABLE_CONSENT_ID, accountAccessInfo);

        // Then
        assertTrue(actual.hasError());

        assertEquals(CmsError.TECHNICAL_ERROR, actual.getError());
        verify(aisConsentService, never()).updateAspspAccountAccess(any(), any());
    }

    private CmsConsent buildCmsConsent(){
        CmsConsent cmsConsent = new CmsConsent();
        cmsConsent.setId(DECRYPTED_CONSENT_ID);
        return cmsConsent;
    }

    private AisConsentActionRequest buildAisActionRequest(String consentId) {
        return new AisConsentActionRequest("tpp id", consentId, ActionStatus.SUCCESS, "request/uri", true, null, null);
    }

    private AisAccountAccessInfo buildAisAccountAccessInfo() {
        return new AisAccountAccessInfo();
    }
}

