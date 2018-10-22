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

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.Base64;
import java.util.Optional;

@Slf4j
@Service
public class AesCbcCryptoProviderImpl implements CryptoProvider {
    private static final String METHOD = "AES/CBC/PKCS5Padding";
    private static IvParameterSpec VECTOR = new IvParameterSpec("1234567812345678".getBytes());

    @Override
    public Optional<String> encryptText(String text, String password) {
        try {
            Key secretKey = new SecretKeySpec(password.getBytes(), "AES");

            Cipher cipher = Cipher.getInstance(METHOD);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, VECTOR);

            byte[] encryptedData = cipher.doFinal(text.getBytes());
            String encodedData = Base64.getEncoder().encodeToString(encryptedData);

            return Optional.of(encodedData);

        } catch (GeneralSecurityException e) {
            log.error("Error encryption data {}" + e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> decryptText(String encryptedText, String password) {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(encryptedText);

            Key key = new SecretKeySpec(password.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance(METHOD);
            cipher.init(Cipher.DECRYPT_MODE, key, VECTOR);
            byte[] decryptedData = cipher.doFinal(decodedKey);

            String encodedData = new String(decryptedData);

            return Optional.of(encodedData);

        } catch (GeneralSecurityException e) {
            log.error("Error decryption data {}" + e);
            return Optional.empty();
        }
    }
}

