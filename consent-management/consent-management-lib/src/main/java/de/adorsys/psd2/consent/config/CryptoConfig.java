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
