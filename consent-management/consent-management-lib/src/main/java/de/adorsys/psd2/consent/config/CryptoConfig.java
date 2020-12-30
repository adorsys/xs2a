/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.config;

import de.adorsys.psd2.consent.repository.CryptoAlgorithmRepository;
import de.adorsys.psd2.consent.service.security.provider.CryptoInstanceFactory;
import de.adorsys.psd2.consent.service.security.provider.CryptoProvider;
import de.adorsys.psd2.consent.service.security.provider.CryptoProviderHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Configuration
public class CryptoConfig {
    @Value("${xs2a.cms.encryption.defaultProvider.dataProvider:JcHZwvJMuc}")
    private String defaultDataProviderId;

    @Value("${xs2a.cms.encryption.defaultProvider.idProvider:psGLvQpt9Q}")
    private String defaultIdProviderId;

    @Bean
    public CryptoProviderHolder initCryptoProviders(CryptoAlgorithmRepository cryptoAlgorithmRepository) {
        Map<String, CryptoProvider> providerMap = getInitializedProviderMap(cryptoAlgorithmRepository);
        CryptoProviderHolder cryptoProviderHolder = new CryptoProviderHolder(providerMap, defaultDataProviderId, defaultIdProviderId);

        validateDefaultProviders(cryptoProviderHolder.getDefaultDataProvider(), cryptoProviderHolder.getDefaultIdProvider());

        log.info("Crypto providers are initialized: {}", cryptoProviderHolder.getInitializedProviders());
        log.info("Provider for Data encryption by default: {}", cryptoProviderHolder.getDefaultDataProvider());
        log.info("Provider for ID encryption by default: {}", cryptoProviderHolder.getDefaultIdProvider());

        return cryptoProviderHolder;
    }

    private Map<String, CryptoProvider> getInitializedProviderMap(CryptoAlgorithmRepository cryptoAlgorithmRepository) {
        return StreamSupport.stream(cryptoAlgorithmRepository.findAll().spliterator(), false)
                   .filter(crp -> StringUtils.isNotBlank(crp.getEncryptorClass())
                                      && !crp.getEncryptorClass().equals("UNDEFINED"))
                   .map(crp -> getCryptoProviderInstance(crp.getEncryptorClass(), crp.getCryptoProviderId(), crp.getEncryptorParams()))
                   .filter(Objects::nonNull)
                   .collect(Collectors.toMap(CryptoProvider::getCryptoProviderId, crp -> crp));
    }

    private CryptoProvider getCryptoProviderInstance(String factoryClassName, String cryptoProviderId, String params) {
        try {
            CryptoInstanceFactory factoryImpl = (CryptoInstanceFactory) Class.forName(factoryClassName)
                                                                            .getConstructor()
                                                                            .newInstance();

            return factoryImpl.initProvider(cryptoProviderId, params);
        } catch (Exception ex) {
            log.info("Error creation factory class from name: {}", factoryClassName, ex);
        }
        return null;
    }

    private void validateDefaultProviders(CryptoProvider defaultDataProvider, CryptoProvider defaultIdProvider) {
        if (Objects.isNull(defaultDataProvider)
                || Objects.isNull(defaultIdProvider)) {
            log.error("Default providers are not initialized! DefaultDataProvider : {} , DefaultIdProvider {}", defaultDataProvider, defaultIdProvider);
            throw new IllegalArgumentException("Default providers are not initialized!");
        }
    }
}
