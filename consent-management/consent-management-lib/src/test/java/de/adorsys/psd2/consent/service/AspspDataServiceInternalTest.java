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

import de.adorsys.psd2.consent.domain.AspspConsentDataEntity;
import de.adorsys.psd2.consent.repository.AspspConsentDataRepository;
import de.adorsys.psd2.consent.service.security.DecryptedData;
import de.adorsys.psd2.consent.service.security.EncryptedData;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.verification.VerificationMode;

import java.util.Base64;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AspspDataServiceInternalTest {
    @InjectMocks
    AspspDataServiceInternal aspspDataServiceInternal;
    @Mock
    private SecurityDataService securityDataService;
    @Mock
    private AspspConsentDataRepository aspspConsentDataRepository;

    private AspspConsentDataEntity aspspConsentDataEntity;
    private final String EXTERNAL_CONSENT_ID = "UjXVCgkxoLmfueAWXkT4dZrAMdaZMBMUFckGwTgY4sV_ZSG9MeQUuGwve3cT85V5sAD6jPgqqholJCbEQmjZIQ==_=_bS6p6XvTWI";
    private final String EXTERNAL_CONSENT_ID_NOT_EXIST = "5o5Wldx-YSWcc2l9iWBmTFQA5W5RGQocLAiyj2W6dRwuTkoi-tFw54Sv5qXjLeHd0gdfoYGRQSQPzXco-i5-YQ==_=_bS6p6XvTWI";
    private final String CONSENT_ID_NOT_ENCRYPTED = "4b112130-6a96-4941-a220-2da8a4af2c63";
    private static final String CONSENT_DATA = "test data";
    private static final byte[] ENCRYPTED_CONSENT_DATA = CONSENT_DATA.getBytes();
    private static final String ENCRYPTED_CONSENT_DATA_BASE64_ENCODED = Base64.getEncoder().encodeToString(ENCRYPTED_CONSENT_DATA);
    VerificationMode once = times(1);

    @Before
    public void setUp() {
        aspspConsentDataEntity = buildApspConsentDataEntity();
        when(securityDataService.decryptId(EXTERNAL_CONSENT_ID)).thenReturn(Optional.of(EXTERNAL_CONSENT_ID));
        when(securityDataService.decryptId(EXTERNAL_CONSENT_ID_NOT_EXIST)).thenReturn(Optional.of(EXTERNAL_CONSENT_ID_NOT_EXIST));
        when(securityDataService.encryptConsentData(EXTERNAL_CONSENT_ID, ENCRYPTED_CONSENT_DATA_BASE64_ENCODED)).thenReturn(Optional.of(new EncryptedData(ENCRYPTED_CONSENT_DATA)));
        when(securityDataService.decryptConsentData(EXTERNAL_CONSENT_ID, ENCRYPTED_CONSENT_DATA)).thenReturn(Optional.of(new DecryptedData(ENCRYPTED_CONSENT_DATA)));
        when(aspspConsentDataRepository.findByConsentId(EXTERNAL_CONSENT_ID)).thenReturn(Optional.of(aspspConsentDataEntity));
        when(aspspConsentDataRepository.findByConsentId(EXTERNAL_CONSENT_ID_NOT_EXIST)).thenReturn(Optional.empty());
        when(aspspConsentDataRepository.findByConsentId(CONSENT_ID_NOT_ENCRYPTED)).thenReturn(Optional.of(aspspConsentDataEntity));
        when(aspspConsentDataRepository.save(aspspConsentDataEntity)).thenReturn(aspspConsentDataEntity);
        when(securityDataService.isConsentIdEncrypted(EXTERNAL_CONSENT_ID)).thenReturn(true);
        when(securityDataService.isConsentIdEncrypted(EXTERNAL_CONSENT_ID_NOT_EXIST)).thenReturn(true);
        when(securityDataService.isConsentIdEncrypted(CONSENT_ID_NOT_ENCRYPTED)).thenReturn(false);
    }

    @Test
    public void checkIsConsentIdEncryptedRead() {
        aspspDataServiceInternal.readAspspConsentData(EXTERNAL_CONSENT_ID);
        verify(securityDataService, once).decryptConsentData(EXTERNAL_CONSENT_ID, ENCRYPTED_CONSENT_DATA);
        verify(securityDataService, once).decryptId(EXTERNAL_CONSENT_ID);

        aspspDataServiceInternal.readAspspConsentData(CONSENT_ID_NOT_ENCRYPTED);
        verify(securityDataService, never()).decryptConsentData(CONSENT_ID_NOT_ENCRYPTED, ENCRYPTED_CONSENT_DATA);
        verify(securityDataService, never()).decryptId(CONSENT_ID_NOT_ENCRYPTED);
    }

    @Test
    public void checkIsConsentIdEncryptedUpdate() {
        aspspDataServiceInternal.updateAspspConsentData(buildAspspConsentData(ENCRYPTED_CONSENT_DATA, EXTERNAL_CONSENT_ID));
        verify(securityDataService, once).decryptId(EXTERNAL_CONSENT_ID);
        verify(securityDataService, once).encryptConsentData(EXTERNAL_CONSENT_ID, ENCRYPTED_CONSENT_DATA_BASE64_ENCODED);

        aspspDataServiceInternal.updateAspspConsentData(buildAspspConsentData(ENCRYPTED_CONSENT_DATA, CONSENT_ID_NOT_ENCRYPTED));
        verify(securityDataService, never()).decryptId(CONSENT_ID_NOT_ENCRYPTED);
        verify(securityDataService, never()).encryptConsentData(CONSENT_ID_NOT_ENCRYPTED, ENCRYPTED_CONSENT_DATA_BASE64_ENCODED);
    }

    @Test
    public void readAspspConsentDataSuccess() {
        // When
        // Then
        Optional<AspspConsentData> aspspConsentData = aspspDataServiceInternal.readAspspConsentData(EXTERNAL_CONSENT_ID);
        // Assert
        assertTrue(aspspConsentData.isPresent());
        assertEquals(aspspConsentData.get().getConsentId(), EXTERNAL_CONSENT_ID);
        assertEquals(aspspConsentData.get().getAspspConsentData(), ENCRYPTED_CONSENT_DATA);
    }

    @Test
    public void readAspspConsentDataFail() {
        // When
        // Then
        Optional<AspspConsentData> aspspConsentData = aspspDataServiceInternal.readAspspConsentData(EXTERNAL_CONSENT_ID_NOT_EXIST);
        // Assert
        assertFalse(aspspConsentData.isPresent());
    }

    @Test
    public void updateAspspConsentDataSuccess() {
        // When
        // Then
        boolean updated = aspspDataServiceInternal.updateAspspConsentData(buildAspspConsentData(ENCRYPTED_CONSENT_DATA, EXTERNAL_CONSENT_ID));
        // Assert
        assertTrue(updated);
    }

    @Test
    public void updateAspspConsentDataFail() {
        // When
        // Then
        boolean updated = aspspDataServiceInternal.updateAspspConsentData(buildAspspConsentData(null, EXTERNAL_CONSENT_ID));
        // Assert
        assertFalse(updated);
    }

    private AspspConsentDataEntity buildApspConsentDataEntity() {
        AspspConsentDataEntity aspspConsentDataEntity = new AspspConsentDataEntity();
        aspspConsentDataEntity.setConsentId(EXTERNAL_CONSENT_ID);
        aspspConsentDataEntity.setData(ENCRYPTED_CONSENT_DATA);
        return aspspConsentDataEntity;
    }

    private AspspConsentData buildAspspConsentData(byte[] aspspConsentData, String consentId) {
        return new AspspConsentData(aspspConsentData, consentId);
    }
}
