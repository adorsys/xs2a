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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityDataService {
    @Qualifier(value = "serverKey")
    private final String serverKey;
    private static final String SEPARATOR = "_=_";

    private final CryptoProviderFactory cryptoProviderFactory;

    /**
     * Encrypts external consent ID with secret consent key via configuration server key
     *
     * @param consentId
     * @return String encrypted external consent ID
     */
    public Optional<String> getEncryptedId(String consentId) {
        String consent_key = getConsentKey();
        String compositeConsentId = concatWithSeparator(consentId, consent_key);
        byte[] bytesCompositeConsentId = compositeConsentId.getBytes();
        return identifierCP()
                   .encryptData(bytesCompositeConsentId, serverKey)
                   .map(EncryptedData::getData)
                   .map(this::encode64)
                   .map(this::addVersionToEncryptedId);
    }

    /**
     * Decrypts encrypted external ID
     *
     * @param encryptedId
     * @return String external ID
     */
    public Optional<String> getDecryptedId(String encryptedId) {
        if (!encryptedId.contains(SEPARATOR)) {
            return Optional.empty();
        }

        Optional<String> compositeId = decryptCompositeId(encryptedId);
        return compositeId.map(this::getOriginalIdFromCompositeId);
    }

    /**
     * Encrypts ASPSP consent data
     *
     * @param encryptedConsentId
     * @param aspspConsentDataBase64 original data encoded in Base64 to be encrypted
     * @return response contains encrypted data
     */
    public Optional<EncryptedData> encryptConsentData(String encryptedConsentId, String aspspConsentDataBase64) {
        byte[] aspspConsentData = decode64(aspspConsentDataBase64);

        if (aspspConsentData == null) {
            return Optional.empty();
        }

        return getConsentKeyFromEncryptedConsentId(encryptedConsentId)
                   .flatMap(consentKey -> consentDataCP().encryptData(aspspConsentData, consentKey));
    }

    /**
     * Decrypt ASPSP consent data
     *
     * @param encryptedConsentId
     * @param aspspConsentData   encrypted data to be decrypted
     * @return response contains decrypted data
     */
    public Optional<DecryptedData> decryptConsentData(String encryptedConsentId, byte[] aspspConsentData) {
        return getConsentKeyFromEncryptedConsentId(encryptedConsentId)
                   .flatMap(consentKey -> consentDataCP().decryptData(aspspConsentData, consentKey));
    }

    private Optional<String> decryptCompositeId(String encryptedId) {
        String encryptedCompositeId = readCompositeIdWithoutVersion(encryptedId);
        byte[] bytesCompositeId = decode64(encryptedCompositeId);
        if (bytesCompositeId == null) {
            return Optional.empty();
        }
        String algorithmVersion = readAlgorithmVersion(encryptedId);
        Optional<CryptoProvider> provider = cryptoProviderFactory.getCryptoProviderByAlgorithmVersion(algorithmVersion);

        return provider
                   .flatMap(prd -> prd.decryptData(bytesCompositeId, serverKey))
                   .map(ed -> new String(ed.getData()))
                   .filter(this::hasValidCharacters);
    }

    private boolean hasValidCharacters(String consentId) {
        return StringUtils.isAsciiPrintable(consentId);
    }

    private String getOriginalIdFromCompositeId(String compositeId) {
        return compositeId.split(SEPARATOR)[0];
    }

    private Optional<String> getConsentKeyFromEncryptedConsentId(String encryptedConsentId) {
        return decryptCompositeId(encryptedConsentId)
                   .map(this::getConsentKeyFromCompositeId);
    }

    private String getConsentKeyFromCompositeId(String compositeId) {
        return compositeId.split(SEPARATOR)[1];
    }

    private String getConsentKey() {
        return RandomStringUtils.random(16, true, true);
    }

    private String encode64(byte[] raw) {
        return Base64.getUrlEncoder().encodeToString(raw);
    }

    private byte[] decode64(String raw) {
        try {
            return Base64.getUrlDecoder().decode(raw);
        } catch (IllegalArgumentException ex) {
            log.warn("Input id has wrong format: {}", raw);
            return null;
        }
    }

    private String addVersionToEncryptedId(String encryptedConsentId) {
        // external Id is identifier of crypto method
        String algorithmVersion = identifierCP().getAlgorithmVersion().getExternalId();
        return concatWithSeparator(encryptedConsentId, algorithmVersion);
    }

    private CryptoProvider consentDataCP() {
        return cryptoProviderFactory.getActualConsentDataCryptoProvider();
    }

    private CryptoProvider identifierCP() {
        return cryptoProviderFactory.getActualIdentifierCryptoProvider();
    }

    private String concatWithSeparator(String leftPart, String rightPart) {
        StringBuilder sb = new StringBuilder();
        sb.append(leftPart);
        sb.append(SEPARATOR);
        sb.append(rightPart);
        return sb.toString();
    }

    private String readCompositeIdWithoutVersion(String compositeIdWithVersion) {
        return compositeIdWithVersion.substring(0, compositeIdWithVersion.indexOf(SEPARATOR));
    }

    private String readAlgorithmVersion(String compositeIdWithVersion) {
        return compositeIdWithVersion.substring(compositeIdWithVersion.indexOf(SEPARATOR) + SEPARATOR.length());
    }
}
