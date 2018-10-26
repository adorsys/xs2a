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

package de.adorsys.psd2.consent.server.service.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.AESDecrypter;
import com.nimbusds.jose.crypto.AESEncrypter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.Optional;

@Slf4j
@Service
public class JweCryptoProviderImpl implements CryptoProvider {
    private static final EncryptionMethod METHOD = EncryptionMethod.A256GCM;
    private static final JWEAlgorithm ALGORITHM = JWEAlgorithm.A256GCMKW;

    @Override
    public Optional<EncryptedData> encryptData(byte[] data, String password) {
        try {
            Payload payload = new Payload(data);
            SecretKey key = getSecretKey(password);

            JWEHeader header = new JWEHeader(ALGORITHM, METHOD);
            JWEObject jweObject = new JWEObject(header, payload);
            JWEEncrypter encrypter = new AESEncrypter(key.getEncoded());

            jweObject.encrypt(encrypter);
            String encryptedData = jweObject.serialize();

            return Optional.of(new EncryptedData(encryptedData.getBytes()));

        } catch (GeneralSecurityException | JOSEException e) {
            log.error("Error encryption data: {}", e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<DecryptedData> decryptData(byte[] data, String password) {
        try {
            SecretKey key = getSecretKey(password);

            JWEObject jweObject = JWEObject.parse(new String(data));
            JWEDecrypter decrypter = new AESDecrypter(key.getEncoded());
            jweObject.decrypt(decrypter);

            return Optional.of(new DecryptedData(jweObject.getPayload().toBytes()));
        } catch (ParseException e) {
            log.error("Error encryption data. Data can't be parsed : {}", e);
        } catch (GeneralSecurityException | JOSEException e) {
            if (e.getMessage().contains("Tag mismatch!")) {
                log.error("Error decryption data. Wrong password");
            } else {
                log.error("Error encryption data: {}", e);
            }
        }

        return Optional.empty();
    }

    @Override
    public CryptoProviderAlgorithmVersion getAlgorithmVersion() {
        return new CryptoProviderAlgorithmVersion("gQ8wkMeo93", "JWE/GCM/256");
    }
}
