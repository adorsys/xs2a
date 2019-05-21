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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CryptoProviderCode {
    AES_ECB_PKCS5_256_65K("bS6p6XvTWI"),

    AES_ECB_NO_256_65K("nML0IXWdMa"),

    JWE_GCM_256_65K("gQ8wkMeo93"),

    AES_ECB_PKCS5_256_1K("psGLvQpt9Q"),

    JWE_GCM_256_1K("JcHZwvJMuc"),

    AES_ECB_NO_256_1K("Ad3lmz9DZY");

    private String value;

    CryptoProviderCode(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static CryptoProviderCode fromValue(String text) {
        for (CryptoProviderCode b : CryptoProviderCode.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
