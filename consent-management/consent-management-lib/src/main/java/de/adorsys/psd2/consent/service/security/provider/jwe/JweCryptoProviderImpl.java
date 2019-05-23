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

package de.adorsys.psd2.consent.service.security.provider.jwe;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.AESDecrypter;
import com.nimbusds.jose.crypto.AESEncrypter;
import de.adorsys.psd2.consent.service.security.DecryptedData;
import de.adorsys.psd2.consent.service.security.EncryptedData;
import de.adorsys.psd2.consent.service.security.provider.CryptoProvider;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;

@Slf4j
@Value
public class JweCryptoProviderImpl implements CryptoProvider {
    private static final EncryptionMethod METHOD = EncryptionMethod.A256GCM;
    private static final JWEAlgorithm ALGORITHM = JWEAlgorithm.A256GCMKW;
    private final String cryptoProviderId;
    private final String algorithm;
    private final String version;
    private final int keyLength;
    private final int hashIterations;
    private final String skfAlgorithm;

    public JweCryptoProviderImpl(String cryptoProviderId, String algorithm, String version, int keyLength, int hashIterations, String skfAlgorithm) {
        this.cryptoProviderId = cryptoProviderId;
        this.algorithm = algorithm;
        this.version = version;
        this.keyLength = keyLength;
        this.hashIterations = hashIterations;
        this.skfAlgorithm = skfAlgorithm;
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
            log.info("Error encryption data: {}", e);
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
            log.info("Error encryption data. Data can't be parsed : {}", e);
        }

        return Optional.empty();
    }

    private SecretKey getSecretKey(String password) throws InvalidKeySpecException, NoSuchAlgorithmException {
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
}
