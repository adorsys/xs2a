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

package de.adorsys.psd2.consent.service.security;

import lombok.Value;

/**
 * Container for holding crypto information, extracted in the process of decrypting the ID
 */
@Value
public class DecryptedIdSet {
    // Do not delete this constant! It is used in order to decrypt old aspsp consent data with previous algorithm (JWE/GCM/256 65536)
    private static final String PREVIOUS_DATA_PROVIDER_ID = "gQ8wkMeo93";
    private String decryptedId;
    private String randomSecretKey;
    private String dataEncryptionProviderId;

    /**
     * Constructs new instance of DecryptedIdSet from given parameters:
     * <ul>
     * <li>decrypted ID</li>
     * <li>random secret key, used for encrypting data</li>
     * <li>ID of the crypto provider, used for encrypting ASPSP consent data (if the value is omitted, it's assumed that
     * old provider was used)</li>
     * </ul>
     *
     * @param idDataValues values, extracted from the ID during decryption
     */
    public DecryptedIdSet(String... idDataValues) {
        if (idDataValues.length < 2) {
            throw new IllegalArgumentException("At least decrypted ID and secret key must be present");
        }

        this.decryptedId = idDataValues[0];
        this.randomSecretKey = idDataValues[1];

        if (idDataValues.length > 2) {
            this.dataEncryptionProviderId = idDataValues[2];
        } else {
            this.dataEncryptionProviderId = PREVIOUS_DATA_PROVIDER_ID;
        }
    }
}
