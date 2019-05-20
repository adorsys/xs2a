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
    private final Map<String, AbstractCryptoProvider> algorithmMap;

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
        return algorithmMap.get("psGLvQpt9Q"); // AES/ECB/PKCS5Padding 256 1024
    }

    public AbstractCryptoProvider actualConsentDataCryptoProvider() {
        return algorithmMap.get("JcHZwvJMuc"); // JWE/GCM/256	256	1024
    }

    private AbstractCryptoProvider mapCryptoProviderByAlgorithmName(CryptoAlgorithm cryptoAlgorithm) {
        return algorithmMap.get(cryptoAlgorithm.getExternalId());
    }

    private Map<String, AbstractCryptoProvider> generateAlgorithmMap() {
        Map<String, AbstractCryptoProvider> algorithmMap = new HashMap<>();

        // 65536 hashIterations
        algorithmMap.put("bS6p6XvTWI", new AesEcbCryptoProviderImpl("bS6p6XvTWI", "AES/ECB/PKCS5Padding", "2", 256, 65536, "PBKDF2WithHmacSHA256"));
        algorithmMap.put("gQ8wkMeo93", new JweCryptoProviderImpl("gQ8wkMeo93", "JWE/GCM/256", "3", 256, 65536, "PBKDF2WithHmacSHA256"));

        // 1024 hashIterations
        algorithmMap.put("psGLvQpt9Q", new AesEcbCryptoProviderImpl("psGLvQpt9Q", "AES/ECB/PKCS5Padding", "5", 256, 1024, "PBKDF2WithHmacSHA256"));
        algorithmMap.put("JcHZwvJMuc", new JweCryptoProviderImpl("JcHZwvJMuc", "JWE/GCM/256", "6", 256, 1024, "PBKDF2WithHmacSHA256"));

        return algorithmMap;
    }
}
