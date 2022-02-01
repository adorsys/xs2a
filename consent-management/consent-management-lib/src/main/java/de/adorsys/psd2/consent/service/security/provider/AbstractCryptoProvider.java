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
