/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

import lombok.AllArgsConstructor;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@AllArgsConstructor
public abstract class AbstractCryptoProvider {
    private final int keyLength;
    private final int hashIterations;
    private final String skfAlgorithm;
    private final String cryptoProviderId;

    protected SecretKey getSecretKey(String password) throws InvalidKeySpecException, NoSuchAlgorithmException {
        byte[] salt = new byte[16];
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, hashIterations, keyLength);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(skfAlgorithm);
            SecretKey secretKey = factory.generateSecret(keySpec);
            return new SecretKeySpec(secretKey.getEncoded(), "AES");
        } finally {
            keySpec.clearPassword();
        }
    }

    public String getCryptoProviderId() {
        return cryptoProviderId;
    }
}
