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

package de.adorsys.psd2.consent.service.security;


import de.adorsys.psd2.consent.service.security.provider.aes.AesEcbCryptoProviderImpl;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class AesEcbCryptoProviderImplTest {
    private final String SERVER_KEY_16 = "mvLBiZsiTbGwrfJB";
    private final String SERVER_KEY_3 = "mvL";
    private final String SERVER_KEY_80 = "mvLBiZsiTbGwrfJBmvLBiZsiTbGwrfJBmvLBiZsiTbGwrfJBmvLBiZsiTbGwrfJBmvLBiZsiTbGwrfJB";

    AesEcbCryptoProviderImpl aesGcmCryptoProvider = new AesEcbCryptoProviderImpl("bS6p6XvTWI", "AES/ECB/PKCS5Padding", 256, 65536, "PBKDF2WithHmacSHA256");

    @Test
    void encryptionDecryptionTest_password_16char() {
        encryptionDecryptionTest(SERVER_KEY_16);
    }

    @Test
    void encryptionDecryptionTest_password_3char() {
        encryptionDecryptionTest(SERVER_KEY_3);
    }

    @Test
    void encryptionDecryptionTest_password_80char() {
        encryptionDecryptionTest(SERVER_KEY_80);
    }

    @Test
    void encryptionDecryptionTest_wrong_password() {
        // Given
        String consentKey = RandomStringUtils.random(16, true, true);
        String externalId = UUID.randomUUID().toString();
        String data = externalId + "_" + consentKey;
        String correctPassword = "correct_password";
        String wrongPassword = "wrong_password";

        // When
        Optional<EncryptedData> encryptData = aesGcmCryptoProvider.encryptData(data.getBytes(), correctPassword);

        // Then
        assertTrue(encryptData.isPresent());
        assertTrue(encryptData.get().getData().length > 0);

        // When
        Optional<DecryptedData> decryptData = aesGcmCryptoProvider.decryptData(encryptData.get().getData(), wrongPassword);
        assertFalse(decryptData.isPresent());
    }

    private void encryptionDecryptionTest(String password) {
        // Given
        String consentKey = RandomStringUtils.random(16, true, true);
        String externalId = UUID.randomUUID().toString();
        String data = externalId + "_" + consentKey;

        // When
        Optional<EncryptedData> encryptData = aesGcmCryptoProvider.encryptData(data.getBytes(), password);

        // Then
        assertTrue(encryptData.isPresent());
        assertTrue(encryptData.get().getData().length > 0);

        // When
        Optional<DecryptedData> decryptData = aesGcmCryptoProvider.decryptData(encryptData.get().getData(), password);

        assertTrue(decryptData.isPresent());
        assertTrue(decryptData.get().getData().length > 0);
        assertEquals(data, new String(decryptData.get().getData()));
    }
}
