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
