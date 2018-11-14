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

import de.adorsys.psd2.consent.domain.CryptoAlgorithm;
import de.adorsys.psd2.consent.repository.CryptoAlgorithmRepository;
import de.adorsys.psd2.consent.service.security.provider.CryptoProvider;
import de.adorsys.psd2.consent.service.security.provider.CryptoProviderFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CryptoProviderFactoryTest {
    private static final String ALGORITHM_ID = "bS6p6XvTWI";
    private static final String ALGORITHM_ID_CONSENT_DATA = "gQ8wkMeo93";
    private static final String NON_EXISTING_ALGORITHM_ID = "OtherId";
    private static final String ALGORITHM_NAME = "AES/ECB/PKCS5Padding";
    private static final String ALGORITHM_NAME_CONSENT_DATA = "JWE/GCM/256";

    private final CryptoProvider cryptoProviderId = getCryptoProvider(ALGORITHM_ID, ALGORITHM_NAME);
    private final CryptoProvider cryptoProviderConsentData = getCryptoProvider(ALGORITHM_ID_CONSENT_DATA,
                                                                               ALGORITHM_NAME_CONSENT_DATA);

    @Mock
    private CryptoAlgorithmRepository cryptoAlgorithmRepository;
    private CryptoProviderFactory cryptoProviderFactory;

    @Before
    public void setUp() {
        cryptoProviderFactory = new CryptoProviderFactory(cryptoAlgorithmRepository);


        when(cryptoAlgorithmRepository.findByExternalId(eq(ALGORITHM_ID)))
            .thenReturn(Optional.of(getMockCryptoAlgorithm(ALGORITHM_ID, ALGORITHM_NAME)));
        when(cryptoAlgorithmRepository.findByExternalId(eq(ALGORITHM_ID_CONSENT_DATA)))
            .thenReturn(Optional.of(getMockCryptoAlgorithm(ALGORITHM_ID_CONSENT_DATA, ALGORITHM_NAME_CONSENT_DATA)));
        when(cryptoAlgorithmRepository.findByExternalId(eq(NON_EXISTING_ALGORITHM_ID)))
            .thenReturn(Optional.empty());
    }

    @Test
    public void getCryptoProviderByAlgorithmVersion_Success_IdProvider() {
        Optional<CryptoProvider> actual = cryptoProviderFactory.getCryptoProviderByAlgorithmVersion(ALGORITHM_ID);
        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get().getAlgorithmVersion()).isEqualTo(cryptoProviderId.getAlgorithmVersion());
    }

    @Test
    public void getCryptoProviderByAlgorithmVersion_Success_ConsentDataProvider() {
        Optional<CryptoProvider> actual =
            cryptoProviderFactory.getCryptoProviderByAlgorithmVersion(ALGORITHM_ID_CONSENT_DATA);
        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get().getAlgorithmVersion()).isEqualTo(cryptoProviderConsentData.getAlgorithmVersion());
    }

    @Test
    public void getCryptoProviderByAlgorithmVersion_Failure_NonExistingAlgorithmVersion() {
        Optional<CryptoProvider> actual =
            cryptoProviderFactory.getCryptoProviderByAlgorithmVersion(NON_EXISTING_ALGORITHM_ID);
        assertThat(actual.isPresent()).isFalse();
    }

    private CryptoAlgorithm getMockCryptoAlgorithm(String id, String name) {
        CryptoAlgorithm cryptoAlgorithm = new CryptoAlgorithm();
        cryptoAlgorithm.setExternalId(id);
        cryptoAlgorithm.setAlgorithm(name);
        return cryptoAlgorithm;
    }

    private CryptoProvider getCryptoProvider(String id, String algorithmName) {
        return new MockCryptoProvider(id, algorithmName, false);
    }
}
