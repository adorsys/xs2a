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
