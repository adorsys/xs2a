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

package de.adorsys.psd2.consent.service.security;


import de.adorsys.psd2.consent.service.security.provider.CryptoProvider;
import de.adorsys.psd2.consent.service.security.provider.CryptoProviderFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityDataService {
    private static final String SEPARATOR = "_=_";
    private String serverKey;
    private final CryptoProviderFactory cryptoProviderFactory;

    @Autowired
    public SecurityDataService(Environment environment, CryptoProviderFactory cryptoProviderFactory) {
        this.cryptoProviderFactory = cryptoProviderFactory;
        serverKey = environment.getProperty("server_key");
        if (StringUtils.isBlank(serverKey)) {
            log.warn("The 'server_key' must be specified at CMS start");
            throw new IllegalArgumentException("CMS_SERVER_KEY_MISSING");
        }
    }

    /**
     * Encrypts external consent ID with secret consent key via configuration server key
     *
     * @param originalId
     * @return String encrypted external consent ID
     */
    public Optional<String> encryptId(String originalId) {
        String consentKey = RandomStringUtils.random(16, true, true);

        String compositeConsentId = concatWithSeparator(originalId, consentKey);
        byte[] bytesCompositeConsentId = compositeConsentId.getBytes();
        return identifierCP()
                   .encryptData(bytesCompositeConsentId, serverKey)
                   .map(EncryptedData::getData)
                   .map(raw -> Base64.getUrlEncoder().encodeToString(raw))
                   .map(this::addVersionToEncryptedId);
    }

    /**
     * Decrypts encrypted external ID
     *
     * @param encryptedId
     * @return String original ID
     */
    public Optional<String> decryptId(String encryptedId) {
        if (!encryptedId.contains(SEPARATOR)) {
            return Optional.empty();
        }

        return decryptCompositeId(encryptedId)
                   .map(cmst -> cmst.split(SEPARATOR)[0]);
    }

    /**
     * Encrypts ASPSP consent data
     *
     * @param encryptedId
     * @param aspspConsentDataBase64 original data encoded in Base64 to be encrypted
     * @return response contains encrypted data
     */
    public Optional<EncryptedData> encryptConsentData(String encryptedId, String aspspConsentDataBase64) {
        byte[] aspspConsentData = decode64(aspspConsentDataBase64);

        if (aspspConsentData == null) {
            return Optional.empty();
        }

        return getConsentKeyByEncryptedId(encryptedId)
                   .flatMap(consentKey -> consentDataCP().encryptData(aspspConsentData, consentKey));
    }

    /**
     * Decrypt ASPSP consent data
     *
     * @param encryptedId
     * @param aspspConsentData encrypted data to be decrypted
     * @return response contains decrypted data
     */
    public Optional<DecryptedData> decryptConsentData(String encryptedId, byte[] aspspConsentData) {
        return getConsentKeyByEncryptedId(encryptedId)
                   .flatMap(consentKey -> consentDataCP().decryptData(aspspConsentData, consentKey));
    }

    private Optional<String> decryptCompositeId(String encryptedId) {
        String encryptedCompositeId = encryptedId.substring(0, encryptedId.indexOf(SEPARATOR));

        byte[] bytesCompositeId = decode64(encryptedCompositeId);
        if (bytesCompositeId == null) {
            return Optional.empty();
        }

        String algorithmVersion = encryptedId.substring(encryptedId.indexOf(SEPARATOR) + SEPARATOR.length());
        Optional<CryptoProvider> provider = cryptoProviderFactory.getCryptoProviderByAlgorithmVersion(algorithmVersion);

        return provider
                   .flatMap(prd -> prd.decryptData(bytesCompositeId, serverKey))
                   .map(ed -> new String(ed.getData()))
                   .filter(StringUtils::isAsciiPrintable);
    }

    private Optional<String> getConsentKeyByEncryptedId(String encryptedId) {
        return decryptCompositeId(encryptedId)
                   .map(comst -> comst.split(SEPARATOR)[1]);
    }

    private byte[] decode64(String raw) {
        try {
            return Base64.getUrlDecoder().decode(raw);
        } catch (IllegalArgumentException ex) {
            log.warn("Input id has wrong format: {}", raw);
            return null;
        }
    }

    private String addVersionToEncryptedId(String encryptedId) {
        // external Id is identifier of crypto method
        String algorithmVersion = identifierCP().getAlgorithmVersion().getExternalId();
        return concatWithSeparator(encryptedId, algorithmVersion);
    }

    private CryptoProvider consentDataCP() {
        return cryptoProviderFactory.actualConsentDataCryptoProvider();
    }

    private CryptoProvider identifierCP() {
        return cryptoProviderFactory.actualIdentifierCryptoProvider();
    }

    private String concatWithSeparator(String leftPart, String rightPart) {
        StringBuilder sb = new StringBuilder();
        sb.append(leftPart);
        sb.append(SEPARATOR);
        sb.append(rightPart);
        return sb.toString();
    }
}
