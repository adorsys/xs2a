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


package de.adorsys.psd2.consent.integration.config;

import de.adorsys.psd2.consent.config.CryptoConfig;
import de.adorsys.psd2.consent.domain.CryptoAlgorithm;
import de.adorsys.psd2.consent.repository.CryptoAlgorithmRepository;
import de.adorsys.psd2.consent.service.security.provider.CryptoProviderHolder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class CryptoConfigTest {
    @InjectMocks
    CryptoConfig cryptoConfig;
    private CryptoAlgorithmRepositoryImpl cryptoAlgorithmRepository = new CryptoAlgorithmRepositoryImpl();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(cryptoConfig, "defaultDataProviderId", "gQ8wkMeo93");
        ReflectionTestUtils.setField(cryptoConfig, "defaultIdProviderId", "nML0IXWdMa");
    }

    @Test
    void initCryptoProviders() {
        //Given
        cryptoAlgorithmRepository.save(buildCryptoAlgorithm("de.adorsys.psd2.consent.service.security.provider.jwe.JweGsmInstanceFactoryImpl",
                                                            "JWE/GCM/256_#_3_#_256_#_65536_#_PBKDF2WithHmacSHA256",
                                                            "gQ8wkMeo93"));
        cryptoAlgorithmRepository.save(buildCryptoAlgorithm("de.adorsys.psd2.consent.service.security.provider.aes.AesEcbInstanceFactoryImpl",
                                                            "AES/GCM/NoPadding_#_1_#_256_#_65536_#_PBKDF2WithHmacSHA256",
                                                            "nML0IXWdMa"));

        //When
        CryptoProviderHolder cryptoProviderHolder = cryptoConfig.initCryptoProviders(cryptoAlgorithmRepository);
        //Then
        assertNotNull(cryptoProviderHolder.getDefaultIdProvider());
        assertNotNull(cryptoProviderHolder.getDefaultDataProvider());
        assertEquals(cryptoProviderHolder.getInitializedProviders().size(), cryptoAlgorithmRepository.count());
    }

    @NotNull
    private CryptoAlgorithm buildCryptoAlgorithm(String encryptorClass, String encryptorParams, String cryptoProviderId) {
        CryptoAlgorithm cryptoAlgorithm = new CryptoAlgorithm();
        cryptoAlgorithm.setCryptoProviderId(cryptoProviderId);
        cryptoAlgorithm.setEncryptorClass(encryptorClass);
        cryptoAlgorithm.setEncryptorParams(encryptorParams);
        return cryptoAlgorithm;
    }

    static class CryptoAlgorithmRepositoryImpl implements CryptoAlgorithmRepository {
        private final static List<CryptoAlgorithm> cryptoAlgorithmList = new ArrayList<>();

        @Override
        public <S extends CryptoAlgorithm> S save(S s) {
            cryptoAlgorithmList.add(s);
            return null;
        }

        @Override
        public Iterable<CryptoAlgorithm> findAll() {
            return cryptoAlgorithmList;
        }

        @Override
        public Iterable<CryptoAlgorithm> findAllById(Iterable<Long> iterable) {
            return null;
        }

        @Override
        public long count() {
            return cryptoAlgorithmList.size();
        }

        @Override
        public void deleteById(Long aLong) {

        }

        @Override
        public void delete(CryptoAlgorithm cryptoAlgorithm) {

        }

        @Override
        public void deleteAll(Iterable<? extends CryptoAlgorithm> iterable) {

        }

        @Override
        public void deleteAll() {

        }

        @Override
        public <S extends CryptoAlgorithm> Iterable<S> saveAll(Iterable<S> iterable) {
            iterable.forEach(cryptoAlgorithmList::add);
            return null;
        }

        @Override
        public Optional<CryptoAlgorithm> findById(Long aLong) {
            return Optional.empty();
        }

        @Override
        public boolean existsById(Long aLong) {
            return false;
        }
    }
}
