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

package de.adorsys.psd2.consent.service.security.provider.jwe;

import de.adorsys.psd2.consent.service.security.provider.AbstractInstanceFactory;
import de.adorsys.psd2.consent.service.security.provider.CryptoInstanceFactory;
import de.adorsys.psd2.consent.service.security.provider.CryptoProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class JweGsmInstanceFactoryImpl extends AbstractInstanceFactory implements CryptoInstanceFactory {
    private static final String SEPARATOR = "_#_";

    @Override
    public CryptoProvider initProvider(String cryptoProviderId, String parameters) {
        String[] paramsArr = StringUtils.split(parameters, SEPARATOR);

        int keyLength = getIntegerValueByIndex(paramsArr, 2, 256);
        int hashIterations = getIntegerValueByIndex(paramsArr, 3, 65536);
        String skfAlgorithm = getStringValueByIndex(paramsArr, 4, "PBKDF2WithHmacSHA256");

        return new JweCryptoProviderImpl(cryptoProviderId, keyLength, hashIterations, skfAlgorithm);
    }
}
