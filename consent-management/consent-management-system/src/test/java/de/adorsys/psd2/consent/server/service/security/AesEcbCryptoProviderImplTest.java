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

package de.adorsys.psd2.consent.server.service.security;

import de.adorsys.psd2.consent.server.service.security.provider.AesEcbCryptoProviderImpl;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class AesEcbCryptoProviderImplTest {
    private final String SERVER_KEY_16 = "mvLBiZsiTbGwrfJB";
    private final String SERVER_KEY_3 = "mvL";
    private final String SERVER_KEY_80 = "mvLBiZsiTbGwrfJBmvLBiZsiTbGwrfJBmvLBiZsiTbGwrfJBmvLBiZsiTbGwrfJBmvLBiZsiTbGwrfJB";

    @InjectMocks
    AesEcbCryptoProviderImpl aesGcmCryptoProvider;

    @Test
    public void encryptionDecryptionTest_password_16char() {
        encryptionDecryptionTest(SERVER_KEY_16);
    }

    @Test
    public void encryptionDecryptionTest_password_3char() {
        encryptionDecryptionTest(SERVER_KEY_3);
    }

    @Test
    public void encryptionDecryptionTest_password_80char() {
        encryptionDecryptionTest(SERVER_KEY_80);
    }

    @Test
    public void encryptionDecryptionTest_wrong_password() {
        // Given
        String consentKey = RandomStringUtils.random(16, true, true);
        String externalId = UUID.randomUUID().toString();
        String data = externalId + "_" + consentKey;
        String correctPassword = "correct_password";
        String wrongPassword = "wrong_password";

        // When
        Optional<EncryptedData> encryptData = aesGcmCryptoProvider.encryptData(data.getBytes(), correctPassword);

        // Then
        assertThat(encryptData.isPresent()).isTrue();
        assertThat(encryptData.get().getData().length > 0).isTrue();

        // When
        Optional<DecryptedData> decryptData = aesGcmCryptoProvider.decryptData(encryptData.get().getData(), wrongPassword);
        assertThat(decryptData.isPresent()).isFalse();
    }

    private void encryptionDecryptionTest(String password) {
        // Given
        String consentKey = RandomStringUtils.random(16, true, true);
        String externalId = UUID.randomUUID().toString();
        String data = externalId + "_" + consentKey;

        // When
        Optional<EncryptedData> encryptData = aesGcmCryptoProvider.encryptData(data.getBytes(), password);

        // Then
        assertThat(encryptData.isPresent()).isTrue();
        assertThat(encryptData.get().getData().length > 0).isTrue();

        // When
        Optional<DecryptedData> decryptData = aesGcmCryptoProvider.decryptData(encryptData.get().getData(), password);

        assertThat(decryptData.isPresent()).isTrue();
        assertThat(decryptData.get().getData().length > 0).isTrue();
        assertThat(new String(decryptData.get().getData())).isEqualTo(data);
    }
}
