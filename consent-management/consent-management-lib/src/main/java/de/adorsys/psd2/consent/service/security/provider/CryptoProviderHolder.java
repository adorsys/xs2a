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
