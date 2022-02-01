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

package de.adorsys.psd2.consent.service.security.provider.jwe;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.AESDecrypter;
import com.nimbusds.jose.crypto.AESEncrypter;
import de.adorsys.psd2.consent.service.security.DecryptedData;
import de.adorsys.psd2.consent.service.security.EncryptedData;
import de.adorsys.psd2.consent.service.security.provider.AbstractCryptoProvider;
import de.adorsys.psd2.consent.service.security.provider.CryptoProvider;
import lombok.extern.slf4j.Slf4j;

import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.Optional;

@Slf4j
public class JweCryptoProviderImpl extends AbstractCryptoProvider implements CryptoProvider {
    private static final EncryptionMethod METHOD = EncryptionMethod.A256GCM;
    private static final JWEAlgorithm ALGORITHM = JWEAlgorithm.A256GCMKW;


    public JweCryptoProviderImpl(String cryptoProviderId, int keyLength, int hashIterations, String skfAlgorithm) {
        super(keyLength, hashIterations, skfAlgorithm, cryptoProviderId);
    }

    @Override
    public Optional<EncryptedData> encryptData(byte[] data, String password) {
        try {
            Payload payload = new Payload(data);
            Key secretKey = getSecretKey(password);

            JWEHeader header = new JWEHeader(ALGORITHM, METHOD);
            JWEObject jweObject = new JWEObject(header, payload);
            JWEEncrypter encrypter = new AESEncrypter(secretKey.getEncoded());

            jweObject.encrypt(encrypter);
            String encryptedData = jweObject.serialize();

            return Optional.of(new EncryptedData(encryptedData.getBytes()));

        } catch (GeneralSecurityException | JOSEException e) {
            log.info("Error encryption data: ", e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<DecryptedData> decryptData(byte[] data, String password) {
        try {
            Key secretKey = getSecretKey(password);

            JWEObject jweObject = JWEObject.parse(new String(data));
            JWEDecrypter decrypter = new AESDecrypter(secretKey.getEncoded());
            jweObject.decrypt(decrypter);

            return Optional.of(new DecryptedData(jweObject.getPayload().toBytes()));
        } catch (Exception e) {
            log.info("Error encryption data. Data can't be parsed : ", e);
        }

        return Optional.empty();
    }
}
