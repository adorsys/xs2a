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

package de.adorsys.psd2.consent.service.security.provider;

import de.adorsys.psd2.consent.domain.CryptoAlgorithm;
import de.adorsys.psd2.consent.repository.CryptoAlgorithmRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CryptoProviderFactory {
    private final CryptoAlgorithmRepository cryptoAlgorithmRepository;
    private final AesEcbCryptoProviderImpl aesEcbCryptoProvider1024 = new AesEcbCryptoProviderImpl("psGLvQpt9Q", "AES/ECB/PKCS5Padding", "5", 256, 1024, "PBKDF2WithHmacSHA256");
    private final AesEcbCryptoProviderImpl aesEcbCryptoProvider65536 = new AesEcbCryptoProviderImpl("bS6p6XvTWI", "AES/ECB/PKCS5Padding", "2", 256, 65536, "PBKDF2WithHmacSHA256");
    private final JweCryptoProviderImpl jweCryptoProvider65536 = new JweCryptoProviderImpl("gQ8wkMeo93", "JWE/GCM/256", "3", 256, 65536, "PBKDF2WithHmacSHA256");

    public Optional<AbstractCryptoProvider> getCryptoProviderByAlgorithmVersion(String algorithmVersion) {
        Optional<AbstractCryptoProvider> provider = cryptoAlgorithmRepository.findByExternalId(algorithmVersion)
                                                        .map(this::mapCryptoProviderByAlgorithmName);
        if (!provider.isPresent()) {
            log.info("Crypto Algorithm ID: {{}}. Crypto provider can not be identify by id", algorithmVersion);
        }
        return provider;
    }

    public AbstractCryptoProvider actualIdentifierCryptoProvider() {
        return aesEcbCryptoProvider1024;
    }

    public AbstractCryptoProvider actualConsentDataCryptoProvider() {
        return jweCryptoProvider65536;
    }

    private AbstractCryptoProvider mapCryptoProviderByAlgorithmName(CryptoAlgorithm cryptoAlgorithm) {
        String externalId = cryptoAlgorithm.getExternalId();
        String algorithm = cryptoAlgorithm.getAlgorithm();

        if (externalId.equals("bS6p6XvTWI")) { // AES/ECB/PKCS5Padding 256 65536
            return aesEcbCryptoProvider65536;
        }
        if (externalId.equals("gQ8wkMeo93")) { // JWE/GCM/256	256	65536
            return jweCryptoProvider65536;
        }
        if (externalId.equals("psGLvQpt9Q")) { // AES/ECB/PKCS5Padding 256 1024
            return aesEcbCryptoProvider1024;
        }

        log.info("Crypto provider can not be identify by algorithm: {}", algorithm);
        return null;

    }
}
