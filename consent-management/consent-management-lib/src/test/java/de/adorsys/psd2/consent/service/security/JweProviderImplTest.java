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

import de.adorsys.psd2.consent.service.security.provider.jwe.JweCryptoProviderImpl;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class JweProviderImplTest {

    private JweCryptoProviderImpl jweCryptoProvider = new JweCryptoProviderImpl("gQ8wkMeo93", 256, 32, "PBKDF2WithHmacSHA256");

    @Test
    void encryptionDecryptionJwe() {
        // Given
        String secretKey = RandomStringUtils.random(16, true, true);

        String data = "secret data should be encrypted";

        // When
        Optional<EncryptedData> encryptData = jweCryptoProvider.encryptData(data.getBytes(), secretKey);

        // Then
        assertTrue(encryptData.isPresent());
        assertTrue(encryptData.get().getData().length > 0);

        // When
        Optional<DecryptedData> decryptData = jweCryptoProvider.decryptData(encryptData.get().getData(), secretKey);

        assertTrue(decryptData.isPresent());
        assertTrue(decryptData.get().getData().length > 0);
        assertEquals(data, new String(decryptData.get().getData()));
    }

    @Test
    void encryptionDecryptionJwe_wrong_password() {
        // Given
        String secretKey = RandomStringUtils.random(16, true, true);
        String wrongSecretKey = RandomStringUtils.random(16, true, true);

        String data = "secret data should be encrypted";

        // When
        Optional<EncryptedData> encryptData = jweCryptoProvider.encryptData(data.getBytes(), secretKey);

        // Then
        assertTrue(encryptData.isPresent());
        assertTrue(encryptData.get().getData().length > 0);

        // When
        Optional<DecryptedData> decryptData = jweCryptoProvider.decryptData(encryptData.get().getData(), wrongSecretKey);
        assertFalse(decryptData.isPresent());
    }
}
