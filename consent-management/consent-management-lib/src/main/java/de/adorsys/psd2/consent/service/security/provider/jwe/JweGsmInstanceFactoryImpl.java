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

package de.adorsys.psd2.consent.service.security.provider.jwe;

import de.adorsys.psd2.consent.service.security.provider.CryptoInstanceFactory;
import de.adorsys.psd2.consent.service.security.provider.CryptoProvider;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@Data
public class JweGsmInstanceFactoryImpl implements CryptoInstanceFactory {
    private static final String SEPARATOR = "_#_";

    @Override
    public CryptoProvider initProvider(String cryptoProviderId, String parameters) throws IllegalArgumentException {
        String[] paramsArr = StringUtils.split(parameters, SEPARATOR);

        String algorithm = getStringValueByIndex(paramsArr, 0, "JWE/GCM/256");
        String version = getStringValueByIndex(paramsArr, 1, "1");
        int keyLength = getIntegerValueByIndex(paramsArr, 2, 256);
        int hashIterations = getIntegerValueByIndex(paramsArr, 3, 65536);
        String skfAlgorithm = getStringValueByIndex(paramsArr, 4, "PBKDF2WithHmacSHA256");

        return new JweCryptoProviderImpl(cryptoProviderId, algorithm, version, keyLength, hashIterations, skfAlgorithm);
    }

    private String getStringValueByIndex(String[] paramsArr, int index, String defaultValue) {
        if (paramsArr == null) {
            return defaultValue;
        }

        if (paramsArr.length > index) {
            return paramsArr[index];
        }
        return defaultValue;
    }

    private Integer getIntegerValueByIndex(String[] paramsArr, int index, Integer defaultValue) {
        if (paramsArr == null) {
            return defaultValue;
        }

        if (paramsArr.length > index) {
            return Integer.valueOf(paramsArr[index]);
        }
        return defaultValue;
    }
}
