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

package de.adorsys.psd2.consent.server.data.test;

import de.adorsys.psd2.consent.domain.CryptoAlgorithm;
import de.adorsys.psd2.consent.repository.CryptoAlgorithmRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * CryptoAlgorithmMockData is used to create test data in the database.
 * To fill the DB with test data 'consent-management-system' app should be running with profile "data_test"
 * <p>
 * THIS CLASS HAS TO BE DELETED AFTER TESTING TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/447
 */
@Component
@Profile("data_test")
public class CryptoAlgorithmMockData {
    private final CryptoAlgorithmRepository cryptoAlgorithmRepository;

    public CryptoAlgorithmMockData(CryptoAlgorithmRepository cryptoAlgorithmRepository) {
        this.cryptoAlgorithmRepository = cryptoAlgorithmRepository;
        fillCryptoAlgorithms();
    }

    private void fillCryptoAlgorithms() {
        if (cryptoAlgorithmRepository.count() == 0) {
            cryptoAlgorithmRepository.save(getCryptoAlgorithm("AES/GCM/NoPadding", "1", "nML0IXWdMa"));
            cryptoAlgorithmRepository.save(getCryptoAlgorithm("AES/ECB/PKCS5Padding", "2", "bS6p6XvTWI"));
            cryptoAlgorithmRepository.save(getCryptoAlgorithm("JWE/GCM/256", "3", "gQ8wkMeo93"));
        }
    }

    private CryptoAlgorithm getCryptoAlgorithm(String algorithm, String version, String externalId) {
        CryptoAlgorithm cryptoAlgorithm = new CryptoAlgorithm();
        cryptoAlgorithm.setAlgorithm(algorithm);
        cryptoAlgorithm.setVersion(version);
        cryptoAlgorithm.setExternalId(externalId);
        return cryptoAlgorithm;
    }
}
