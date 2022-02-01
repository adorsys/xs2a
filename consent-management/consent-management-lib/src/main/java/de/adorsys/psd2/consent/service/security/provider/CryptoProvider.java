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
