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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class AesCbcCryptoProviderImplTest {
    private final String SERVER_KEY = "mvLBiZsiTbGwrfJB";

    @InjectMocks
    AesCbcCryptoProviderImpl aesGcmCryptoProvider;

    @Test
    public void encryptionDecryptionTest() {

        // Given
        String consentKey = RandomStringUtils.random(16, true, true);
        String externalId = UUID.randomUUID().toString();
        String data = externalId + "_" + consentKey;

        // When
        Optional<String> encryptData = aesGcmCryptoProvider.encryptText(data, SERVER_KEY);

        // Then
        assertThat(encryptData.isPresent()).isTrue();
        assertThat(encryptData.get()).isNotBlank();

        // When
        Optional<String> decryptData = aesGcmCryptoProvider.decryptText(encryptData.get(), SERVER_KEY);

        assertThat(decryptData.isPresent()).isTrue();
        assertThat(decryptData.get()).isNotBlank();
        assertThat(decryptData.get()).isEqualTo(data);

    }
}
