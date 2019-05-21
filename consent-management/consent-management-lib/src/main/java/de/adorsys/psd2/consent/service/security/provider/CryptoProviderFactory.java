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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class CryptoProviderFactory {
    private final CryptoAlgorithmRepository cryptoAlgorithmRepository;
    private final Map<CryptoProviderCode, AbstractCryptoProvider> algorithmMap;

    public CryptoProviderFactory(CryptoAlgorithmRepository cryptoAlgorithmRepository) {
        this.cryptoAlgorithmRepository = cryptoAlgorithmRepository;
        this.algorithmMap = generateAlgorithmMap();
    }

    public Optional<AbstractCryptoProvider> getCryptoProviderByAlgorithmVersion(String algorithmVersion) {
        Optional<AbstractCryptoProvider> provider = cryptoAlgorithmRepository.findByExternalId(algorithmVersion)
                                                        .map(this::mapCryptoProviderByAlgorithmName);

        if (!provider.isPresent()) {
            log.info("Crypto Algorithm ID: {{}}. Crypto provider can not be identify by id", algorithmVersion);
        }
        return provider;
    }

    public AbstractCryptoProvider actualIdentifierCryptoProvider() {
        return algorithmMap.get(CryptoProviderCode.AES_ECB_PKCS5_256_1K);
    }

    public AbstractCryptoProvider actualConsentDataCryptoProvider() {
        return algorithmMap.get(CryptoProviderCode.JWE_GCM_256_1K);
    }

    public AbstractCryptoProvider oldDefaultVersionDataCryptoProvider() {
        return algorithmMap.get(CryptoProviderCode.JWE_GCM_256_65K);
    }

    private AbstractCryptoProvider mapCryptoProviderByAlgorithmName(CryptoAlgorithm cryptoAlgorithm) {
        return algorithmMap.get(CryptoProviderCode.fromValue(cryptoAlgorithm.getExternalId()));
    }

    private Map<CryptoProviderCode, AbstractCryptoProvider> generateAlgorithmMap() {
        Map<CryptoProviderCode, AbstractCryptoProvider> generatedAlgorithmMap = new HashMap<>();

        // 65536 hashIterations
        generatedAlgorithmMap.put(CryptoProviderCode.AES_ECB_PKCS5_256_65K, new AesEcbCryptoProviderImpl("bS6p6XvTWI", "AES/ECB/PKCS5Padding", "2", 256, 65536, "PBKDF2WithHmacSHA256"));
        generatedAlgorithmMap.put(CryptoProviderCode.JWE_GCM_256_65K, new JweCryptoProviderImpl("gQ8wkMeo93", "JWE/GCM/256", "3", 256, 65536, "PBKDF2WithHmacSHA256"));

        // 1024 hashIterations
        generatedAlgorithmMap.put(CryptoProviderCode.AES_ECB_PKCS5_256_1K, new AesEcbCryptoProviderImpl("psGLvQpt9Q", "AES/ECB/PKCS5Padding", "5", 256, 1024, "PBKDF2WithHmacSHA256"));
        generatedAlgorithmMap.put(CryptoProviderCode.JWE_GCM_256_1K, new JweCryptoProviderImpl("JcHZwvJMuc", "JWE/GCM/256", "6", 256, 1024, "PBKDF2WithHmacSHA256"));

        return generatedAlgorithmMap;
    }
}
