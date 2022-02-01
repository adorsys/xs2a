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
