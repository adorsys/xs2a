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
    private CryptoProvider aesEcbCryptoProviderId = new AesEcbCryptoProviderImpl();
    private CryptoProvider jweCryptoProviderConsentData  = new JweCryptoProviderImpl();

    public Optional<CryptoProvider> getCryptoProviderByAlgorithmVersion(String algorithmVersion) {
        Optional<CryptoProvider> provider = cryptoAlgorithmRepository.findByExternalId(algorithmVersion)
                                                .map(CryptoAlgorithm::getAlgorithm)
                                                .flatMap(this::mapCryptoProviderByAlgorithmName);
        if (!provider.isPresent()) {
            log.error("Crypto provider can not be identify by id: {}", algorithmVersion);
        }
        return provider;
    }

    public CryptoProvider actualIdentifierCryptoProvider() {
        return aesEcbCryptoProviderId;
    }

    public CryptoProvider actualConsentDataCryptoProvider() {
        return jweCryptoProviderConsentData;
    }

    private Optional<CryptoProvider> mapCryptoProviderByAlgorithmName(String algorithm) {
        if (algorithm.equals(aesEcbCryptoProviderId.getAlgorithmVersion().getAlgorithmName())) {
            return Optional.of(aesEcbCryptoProviderId);
        } else if (algorithm.equals(jweCryptoProviderConsentData.getAlgorithmVersion().getAlgorithmName())) {
            return Optional.of(jweCryptoProviderConsentData);
        } else {
            return Optional.empty();
        }
    }
}
