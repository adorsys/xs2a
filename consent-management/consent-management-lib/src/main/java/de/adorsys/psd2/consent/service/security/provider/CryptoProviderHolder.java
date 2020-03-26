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

package de.adorsys.psd2.consent.service.security.provider;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Data
public class CryptoProviderHolder {
    private final Map<String, CryptoProvider> initializedProviders;
    private final CryptoProvider defaultDataProvider;
    private final CryptoProvider defaultIdProvider;

    public CryptoProviderHolder(Map<String, CryptoProvider> initializedProviders, String defaultDataProviderId, String defaultIdProviderId) {
        this.initializedProviders = initializedProviders;
        this.defaultDataProvider = initializedProviders.get(defaultDataProviderId);
        this.defaultIdProvider = initializedProviders.get(defaultIdProviderId);
    }

    /**
     * Returns primary crypto provider which corresponds to security requirements for aspsp data encryption
     *
     * @return Crypto provider for aspsp data encryption
     */
    public CryptoProvider getDefaultDataProvider() {
        return defaultDataProvider;
    }

    /**
     * Returns primary crypto provider which corresponds to security requirements for identifier encryption
     *
     * @return Crypto provider for identifier encryption
     */
    public CryptoProvider getDefaultIdProvider() {
        return defaultIdProvider;
    }

    /**
     * Gets crypto provider from holder by its ID
     *
     * @param cryptoProviderId Id of Crypto provider
     *
     * @return Crypto provider
     */
    public Optional<CryptoProvider> getProviderById(String cryptoProviderId) {
        return Optional.ofNullable(initializedProviders.get(cryptoProviderId));
    }
}
