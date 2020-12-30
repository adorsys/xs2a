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

package de.adorsys.psd2.consent.service.security.provider.aes;

import de.adorsys.psd2.consent.service.security.DecryptedData;
import de.adorsys.psd2.consent.service.security.EncryptedData;
import de.adorsys.psd2.consent.service.security.provider.AbstractCryptoProvider;
import de.adorsys.psd2.consent.service.security.provider.CryptoProvider;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.Optional;

@Slf4j
public class AesEcbCryptoProviderImpl extends AbstractCryptoProvider implements CryptoProvider {
    private final String algorithm;

    public AesEcbCryptoProviderImpl(String cryptoProviderId, String algorithm, int keyLength, int hashIterations, String skfAlgorithm) {
        super(keyLength, hashIterations, skfAlgorithm, cryptoProviderId);
        this.algorithm = algorithm;
    }

    @Override
    public Optional<EncryptedData> encryptData(byte[] data, String password) {
        try {
            Key secretKey = getSecretKey(password);

            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedData = cipher.doFinal(data);

            return Optional.of(new EncryptedData(encryptedData));

        } catch (GeneralSecurityException e) {
            log.info("Error encryption data: ", e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<DecryptedData> decryptData(byte[] data, String password) {
        try {
            Key secretKey = getSecretKey(password);

            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedData = cipher.doFinal(data);

            return Optional.of(new DecryptedData(decryptedData));
        } catch (BadPaddingException e) {
            log.info("Error decryption data. Wrong password");
        } catch (GeneralSecurityException e) {
            log.info("Error decryption data: ", e);
        }

        return Optional.empty();
    }
}
