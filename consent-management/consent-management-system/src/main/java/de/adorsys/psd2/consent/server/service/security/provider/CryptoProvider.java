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

package de.adorsys.psd2.consent.server.service.security.provider;

import de.adorsys.psd2.consent.server.service.security.DecryptedData;
import de.adorsys.psd2.consent.server.service.security.EncryptedData;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Optional;

public interface CryptoProvider {
    String SKF_ALGORITHM = "PBKDF2WithHmacSHA256";

    Optional<EncryptedData> encryptData(byte[] data, String password);

    Optional<DecryptedData> decryptData(byte[] data, String password);

    CryptoProviderAlgorithmVersion getAlgorithmVersion();

    default SecretKey getSecretKey(String password) throws InvalidKeySpecException, NoSuchAlgorithmException {
        byte[] salt = new byte[16];
        SecretKeyFactory factory = SecretKeyFactory.getInstance(SKF_ALGORITHM);
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        SecretKey secretKey = factory.generateSecret(keySpec);
        return new SecretKeySpec(secretKey.getEncoded(), "AES");
    }
}
