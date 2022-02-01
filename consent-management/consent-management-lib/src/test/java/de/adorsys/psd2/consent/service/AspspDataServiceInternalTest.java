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

import de.adorsys.psd2.consent.domain.AspspConsentDataEntity;
import de.adorsys.psd2.consent.repository.AspspConsentDataRepository;
import de.adorsys.psd2.consent.service.security.DecryptedData;
import de.adorsys.psd2.consent.service.security.EncryptedData;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AspspDataServiceInternalTest {
    @InjectMocks
    AspspDataServiceInternal aspspDataServiceInternal;
    @Mock
    private SecurityDataService securityDataService;
    @Mock
    private AspspConsentDataRepository aspspConsentDataRepository;

    private static final String EXTERNAL_CONSENT_ID = "UjXVCgkxoLmfueAWXkT4dZrAMdaZMBMUFckGwTgY4sV_ZSG9MeQUuGwve3cT85V5sAD6jPgqqholJCbEQmjZIQ==_=_bS6p6XvTWI";
    private static final String EXTERNAL_CONSENT_ID_NOT_EXIST = "5o5Wldx-YSWcc2l9iWBmTFQA5W5RGQocLAiyj2W6dRwuTkoi-tFw54Sv5qXjLeHd0gdfoYGRQSQPzXco-i5-YQ==_=_bS6p6XvTWI";
    private static final String CONSENT_ID_NOT_ENCRYPTED = "4b112130-6a96-4941-a220-2da8a4af2c63";
    private static final String CONSENT_DATA = "test data";
    private static final byte[] ENCRYPTED_CONSENT_DATA = CONSENT_DATA.getBytes();
    private VerificationMode once = times(1);
    private AspspConsentDataEntity aspspConsentDataEntity;

    @BeforeEach
    void setUp() {
        aspspConsentDataEntity = buildApspConsentDataEntity();
    }

    @Test
    void checkIsConsentIdEncryptedRead() {
        // Given
        when(securityDataService.isConsentIdEncrypted(EXTERNAL_CONSENT_ID)).thenReturn(true);
        when(securityDataService.decryptId(EXTERNAL_CONSENT_ID)).thenReturn(Optional.of(EXTERNAL_CONSENT_ID));
        when(aspspConsentDataRepository.findByConsentId(EXTERNAL_CONSENT_ID)).thenReturn(Optional.of(aspspConsentDataEntity));
        when(securityDataService.decryptConsentData(EXTERNAL_CONSENT_ID, ENCRYPTED_CONSENT_DATA)).thenReturn(Optional.of(new DecryptedData(ENCRYPTED_CONSENT_DATA)));

        aspspDataServiceInternal.readAspspConsentData(EXTERNAL_CONSENT_ID);
        verify(securityDataService, once).decryptConsentData(EXTERNAL_CONSENT_ID, ENCRYPTED_CONSENT_DATA);
        verify(securityDataService, once).decryptId(EXTERNAL_CONSENT_ID);
        reset(securityDataService, aspspConsentDataRepository);

        when(securityDataService.isConsentIdEncrypted(CONSENT_ID_NOT_ENCRYPTED)).thenReturn(false);
        when(aspspConsentDataRepository.findByConsentId(CONSENT_ID_NOT_ENCRYPTED)).thenReturn(Optional.of(aspspConsentDataEntity));

        // When
        aspspDataServiceInternal.readAspspConsentData(CONSENT_ID_NOT_ENCRYPTED);

        // Then
        verify(securityDataService, never()).decryptConsentData(CONSENT_ID_NOT_ENCRYPTED, ENCRYPTED_CONSENT_DATA);
        verify(securityDataService, never()).decryptId(CONSENT_ID_NOT_ENCRYPTED);
    }

    @Test
    void checkIsConsentIdEncryptedUpdate() {
        // Given
        when(securityDataService.isConsentIdEncrypted(EXTERNAL_CONSENT_ID)).thenReturn(true);
        when(securityDataService.decryptId(EXTERNAL_CONSENT_ID)).thenReturn(Optional.of(EXTERNAL_CONSENT_ID));
        when(securityDataService.encryptConsentData(EXTERNAL_CONSENT_ID, ENCRYPTED_CONSENT_DATA)).thenReturn(Optional.of(new EncryptedData(ENCRYPTED_CONSENT_DATA)));

        aspspDataServiceInternal.updateAspspConsentData(buildAspspConsentData(ENCRYPTED_CONSENT_DATA, EXTERNAL_CONSENT_ID));
        verify(securityDataService, once).decryptId(EXTERNAL_CONSENT_ID);
        verify(securityDataService, once).encryptConsentData(EXTERNAL_CONSENT_ID, ENCRYPTED_CONSENT_DATA);
        reset(securityDataService, aspspConsentDataRepository);

        when(securityDataService.isConsentIdEncrypted(CONSENT_ID_NOT_ENCRYPTED)).thenReturn(false);
        when(aspspConsentDataRepository.findByConsentId(CONSENT_ID_NOT_ENCRYPTED)).thenReturn(Optional.of(aspspConsentDataEntity));
        when(aspspConsentDataRepository.save(aspspConsentDataEntity)).thenReturn(aspspConsentDataEntity);

        // When
        aspspDataServiceInternal.updateAspspConsentData(buildAspspConsentData(ENCRYPTED_CONSENT_DATA, CONSENT_ID_NOT_ENCRYPTED));

        // Then
        verify(securityDataService, never()).decryptId(CONSENT_ID_NOT_ENCRYPTED);
        verify(securityDataService, never()).encryptConsentData(CONSENT_ID_NOT_ENCRYPTED, ENCRYPTED_CONSENT_DATA);
    }

    @Test
    void readAspspConsentDataSuccess() {
        when(securityDataService.decryptId(EXTERNAL_CONSENT_ID)).thenReturn(Optional.of(EXTERNAL_CONSENT_ID));
        when(securityDataService.decryptConsentData(EXTERNAL_CONSENT_ID, ENCRYPTED_CONSENT_DATA)).thenReturn(Optional.of(new DecryptedData(ENCRYPTED_CONSENT_DATA)));
        when(aspspConsentDataRepository.findByConsentId(EXTERNAL_CONSENT_ID)).thenReturn(Optional.of(aspspConsentDataEntity));
        when(securityDataService.isConsentIdEncrypted(EXTERNAL_CONSENT_ID)).thenReturn(true);

        // When
        Optional<AspspConsentData> aspspConsentData = aspspDataServiceInternal.readAspspConsentData(EXTERNAL_CONSENT_ID);

        // Then
        assertTrue(aspspConsentData.isPresent());
        assertEquals(EXTERNAL_CONSENT_ID, aspspConsentData.get().getConsentId());
        assertEquals(ENCRYPTED_CONSENT_DATA, aspspConsentData.get().getAspspConsentDataBytes());
    }

    @Test
    void readAspspConsentDataFail() {
        // When
        Optional<AspspConsentData> aspspConsentData = aspspDataServiceInternal.readAspspConsentData(EXTERNAL_CONSENT_ID_NOT_EXIST);

        // Then
        assertFalse(aspspConsentData.isPresent());
    }

    @Test
    void updateAspspConsentDataSuccess() {
        when(securityDataService.decryptId(EXTERNAL_CONSENT_ID)).thenReturn(Optional.of(EXTERNAL_CONSENT_ID));
        when(securityDataService.encryptConsentData(EXTERNAL_CONSENT_ID, ENCRYPTED_CONSENT_DATA)).thenReturn(Optional.of(new EncryptedData(ENCRYPTED_CONSENT_DATA)));
        when(aspspConsentDataRepository.findByConsentId(EXTERNAL_CONSENT_ID)).thenReturn(Optional.of(aspspConsentDataEntity));
        when(aspspConsentDataRepository.save(aspspConsentDataEntity)).thenReturn(aspspConsentDataEntity);
        when(securityDataService.isConsentIdEncrypted(EXTERNAL_CONSENT_ID)).thenReturn(true);

        // When
        boolean updated = aspspDataServiceInternal.updateAspspConsentData(buildAspspConsentData(ENCRYPTED_CONSENT_DATA, EXTERNAL_CONSENT_ID));

        // Then
        assertTrue(updated);
    }

    @Test
    void updateAspspConsentDataFail() {
        // When
        boolean updated = aspspDataServiceInternal.updateAspspConsentData(buildAspspConsentData(null, EXTERNAL_CONSENT_ID));

        // Then
        assertFalse(updated);
    }

    @Test
    void updateAspspConsentDataFail_emptyData() {
        // When
        boolean updated = aspspDataServiceInternal.updateAspspConsentData(buildAspspConsentData(null, ""));

        // Then
        assertFalse(updated);
    }

    @Test
    void updateAspspConsentDataFail_cant_decrypt_consent_id() {
        when(securityDataService.decryptId(EXTERNAL_CONSENT_ID)).thenReturn(Optional.of(EXTERNAL_CONSENT_ID));
        when(securityDataService.isConsentIdEncrypted(EXTERNAL_CONSENT_ID)).thenReturn(true);

        // Given
        when(securityDataService.decryptId(EXTERNAL_CONSENT_ID)).thenReturn(Optional.empty());

        // When
        boolean updated = aspspDataServiceInternal.updateAspspConsentData(buildAspspConsentData(ENCRYPTED_CONSENT_DATA, EXTERNAL_CONSENT_ID));

        // Then
        assertFalse(updated);
    }

    @Test
    void updateAspspConsentDataFail_cant_encrypt_aspspConsentData() {
        when(securityDataService.decryptId(EXTERNAL_CONSENT_ID)).thenReturn(Optional.of(EXTERNAL_CONSENT_ID));
        when(securityDataService.encryptConsentData(EXTERNAL_CONSENT_ID, ENCRYPTED_CONSENT_DATA)).thenReturn(Optional.of(new EncryptedData(ENCRYPTED_CONSENT_DATA)));
        when(securityDataService.isConsentIdEncrypted(EXTERNAL_CONSENT_ID)).thenReturn(true);

        // Given
        when(securityDataService.encryptConsentData(EXTERNAL_CONSENT_ID, ENCRYPTED_CONSENT_DATA)).thenReturn(Optional.empty());

        // When
        boolean updated = aspspDataServiceInternal.updateAspspConsentData(buildAspspConsentData(ENCRYPTED_CONSENT_DATA, EXTERNAL_CONSENT_ID));

        // Then
        assertFalse(updated);
    }

    @Test
    void deleteAspspConsentDataSuccess() {
        // Given
        when(aspspConsentDataRepository.existsById(EXTERNAL_CONSENT_ID)).thenReturn(true);

        // When
        boolean deleted = aspspDataServiceInternal.deleteAspspConsentData(EXTERNAL_CONSENT_ID);

        // Then
        assertTrue(deleted);
    }

    @Test
    void deleteAspspConsentDataFail_no_aspspConsentData() {
        // When
        boolean deleted = aspspDataServiceInternal.deleteAspspConsentData(EXTERNAL_CONSENT_ID);

        // Then
        assertFalse(deleted);
    }


    @Test
    void deleteAspspConsentDataFail_no_decrypted_id() {
        when(securityDataService.decryptId(EXTERNAL_CONSENT_ID)).thenReturn(Optional.of(EXTERNAL_CONSENT_ID));
        when(securityDataService.isConsentIdEncrypted(EXTERNAL_CONSENT_ID)).thenReturn(true);

        // Given
        when(securityDataService.decryptId(EXTERNAL_CONSENT_ID)).thenReturn(Optional.empty());

        // When
        boolean deleted = aspspDataServiceInternal.deleteAspspConsentData(EXTERNAL_CONSENT_ID);

        // Then
        assertFalse(deleted);
    }

    @Test
    void deleteAspspConsentDataFail_consentId_not_encrypted() {
        // Given
        when(securityDataService.isConsentIdEncrypted(EXTERNAL_CONSENT_ID)).thenReturn(false);

        // When
        boolean deleted = aspspDataServiceInternal.deleteAspspConsentData(EXTERNAL_CONSENT_ID);

        // Then
        assertFalse(deleted);
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
