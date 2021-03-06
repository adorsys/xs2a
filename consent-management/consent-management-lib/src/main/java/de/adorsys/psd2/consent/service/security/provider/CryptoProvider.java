/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

import de.adorsys.psd2.consent.service.security.DecryptedData;
import de.adorsys.psd2.consent.service.security.EncryptedData;

import java.util.Optional;

public interface CryptoProvider {
    /**
     * Encrypts data with some crypto algorithm using password
     *
     * @param data     Raw data for encryption
     * @param password Secret key to encrypt the data with
     * @return encrypted data
     */
    Optional<EncryptedData> encryptData(byte[] data, String password);

    /**
     * Decrypts data with some crypto algorithm using password
     *
     * @param data     Encrypted data for decryption
     * @param password Secret key to decrypt the data with
     * @return Raw data
     */
    Optional<DecryptedData> decryptData(byte[] data, String password);

    /**
     * Returns crypto provider's id
     *
     * @return Id of crypto provider
     */
    String getCryptoProviderId();
}
