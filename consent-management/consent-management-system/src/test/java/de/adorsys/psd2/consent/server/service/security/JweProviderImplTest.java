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

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class JweProviderImplTest {
    @InjectMocks
    JweCryptoProviderImpl jweCryptoProvider;

    @Test
    public void encryptionDecryptionJwe() {
        // Given
        String secretKey = RandomStringUtils.random(16, true, true);

        String data = "secret data should be encrypted";

        // When
        Optional<EncryptedData> encryptData = jweCryptoProvider.encryptData(data.getBytes(), secretKey);

        // Then
        assertThat(encryptData.isPresent()).isTrue();
        assertThat(encryptData.get().getData().length > 0).isTrue();

        // When
        Optional<DecryptedData> decryptData = jweCryptoProvider.decryptData(encryptData.get().getData(), secretKey);

        assertThat(decryptData.isPresent()).isTrue();
        assertThat(decryptData.get().getData().length > 0).isTrue();
        assertThat(new String(decryptData.get().getData())).isEqualTo(data);
    }

    @Test
    public void encryptionDecryptionJwe_wrong_password() {
        // Given
        String secretKey = RandomStringUtils.random(16, true, true);
        String wrongSecretKey = RandomStringUtils.random(16, true, true);

        String data = "secret data should be encrypted";

        // When
        Optional<EncryptedData> encryptData = jweCryptoProvider.encryptData(data.getBytes(), secretKey);

        // Then
        assertThat(encryptData.isPresent()).isTrue();
        assertThat(encryptData.get().getData().length > 0).isTrue();

        // When
        Optional<DecryptedData> decryptData = jweCryptoProvider.decryptData(encryptData.get().getData(), wrongSecretKey);
        assertThat(decryptData.isPresent()).isFalse();
    }
}
