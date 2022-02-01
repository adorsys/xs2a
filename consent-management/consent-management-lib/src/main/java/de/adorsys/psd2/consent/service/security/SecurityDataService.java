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

package de.adorsys.psd2.consent.service.security;


import de.adorsys.psd2.consent.service.security.provider.CryptoProvider;
import de.adorsys.psd2.consent.service.security.provider.CryptoProviderHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityDataService {
    private static final String SEPARATOR = "_=_";
    private String serverKey;

    private final CryptoProviderHolder cryptoProviderHolder;
    private final SecureRandom random = new SecureRandom();

    @Autowired
    public SecurityDataService(Environment environment, CryptoProviderHolder cryptoProviderHolder) {
        this.cryptoProviderHolder = cryptoProviderHolder;
        serverKey = environment.getProperty("server_key");
        if (StringUtils.isBlank(serverKey)) {
            log.info("The 'server_key' missing - must be specified at CMS start");
            throw new IllegalArgumentException("CMS_SERVER_KEY_MISSING");
        }
    }

    /**
     * Encrypts external consent ID with secret consent key via configuration server key
     *
     * @param originalId external consent ID to encrypt
     * @return String encrypted external consent ID
     */
    public Optional<String> encryptId(String originalId) {
        String consentKey = RandomStringUtils.random(16, 0, 0, true, true, null, random); //NOSONAR

        String compositeConsentId = concatWithSeparator(originalId, consentKey, cryptoProviderHolder.getDefaultDataProvider().getCryptoProviderId());

        byte[] bytesCompositeConsentId = compositeConsentId.getBytes();

        Optional<String> encryptedId = cryptoProviderHolder.getDefaultIdProvider()
                                           .encryptData(bytesCompositeConsentId, serverKey)
                                           .map(EncryptedData::getData)
                                           .map(raw -> Base64.getUrlEncoder().encodeToString(raw))
                                           .map(this::addVersionToEncryptedId);

        if (encryptedId.isEmpty()) {
            log.info("ID: [{}]. Couldn't encrypt ID", originalId);
        }

        return encryptedId;
    }

    /**
     * Decrypts encrypted external ID
     *
     * @param encryptedId encrypted ID for the input
     * @return String original ID
     */
    public Optional<String> decryptId(String encryptedId) {
        if (!encryptedId.contains(SEPARATOR)) {
            log.info("ID: [{}]. Couldn't decrypt, because id does not contain separator [{}]", encryptedId, SEPARATOR);
            return Optional.empty();
        }

        Optional<String> decryptedId = decryptCompositeId(encryptedId)
                                           .map(cmst -> cmst.split(SEPARATOR)[0]);

        if (decryptedId.isEmpty()) {
            log.info("ID: [{}]. Couldn't decrypt ID", encryptedId);
        }

        return decryptedId;
    }

    /**
     * Encrypts ASPSP consent data
     *
     * @param encryptedId      encrypted consent ID
     * @param aspspConsentData original data to be encrypted
     * @return response contains encrypted data
     */
    public Optional<EncryptedData> encryptConsentData(String encryptedId, byte[] aspspConsentData) {
        return getDecryptedIdSetByEncryptedId(encryptedId)
                   .flatMap(dta -> getEncryptedData(dta, aspspConsentData));
    }

    /**
     * Decrypt ASPSP consent data
     *
     * @param encryptedId      encrypted consent ID
     * @param aspspConsentData encrypted data to be decrypted
     * @return response contains decrypted data
     */
    public Optional<DecryptedData> decryptConsentData(String encryptedId, byte[] aspspConsentData) {
        return getDecryptedIdSetByEncryptedId(encryptedId)
                   .flatMap(dta -> getDecryptedData(dta, aspspConsentData));
    }

    /**
     * Checks whether paymentId is encrypted or not
     *
     * @param consentId id of consent
     * @return <code>true</code> if paymentId is encrypted. <code>false</code> otherwise.
     */
    public boolean isConsentIdEncrypted(String consentId) {
        return consentId.contains(SEPARATOR);
    }

    private Optional<DecryptedData> getDecryptedData(DecryptedIdSet decryptedIdSet, byte[] aspspConsentData) {
        return cryptoProviderHolder.getProviderById(decryptedIdSet.getDataEncryptionProviderId())
                   .flatMap(provider -> provider.decryptData(aspspConsentData, decryptedIdSet.getRandomSecretKey()));
    }

    private Optional<EncryptedData> getEncryptedData(DecryptedIdSet decryptedIdSet, byte[] aspspConsentData) {
        return cryptoProviderHolder.getProviderById(decryptedIdSet.getDataEncryptionProviderId())
                   .flatMap(provider -> provider.encryptData(aspspConsentData, decryptedIdSet.getRandomSecretKey()));
    }

    private Optional<DecryptedIdSet> getDecryptedIdSetByEncryptedId(String encryptedId) {
        return decryptCompositeId(encryptedId)
                   .map(cmpid -> new DecryptedIdSet(cmpid.split(SEPARATOR)));
    }

    private Optional<String> decryptCompositeId(String encryptedId) {
        String encryptedCompositeId = encryptedId.substring(0, encryptedId.indexOf(SEPARATOR));

        byte[] bytesCompositeId = decode64(encryptedCompositeId);
        if (bytesCompositeId == null) {
            log.info("ID: [{}]. Couldn't decrypt composite id", encryptedId);
            return Optional.empty();
        }

        String algorithmVersion = encryptedId.substring(encryptedId.indexOf(SEPARATOR) + SEPARATOR.length());
        Optional<CryptoProvider> provider = cryptoProviderHolder.getProviderById(algorithmVersion);

        return provider
                   .flatMap(prd -> prd.decryptData(bytesCompositeId, serverKey))
                   .map(ed -> new String(ed.getData()))
                   .filter(StringUtils::isAsciiPrintable);
    }

    private byte[] decode64(String raw) {
        try {
            return Base64.getUrlDecoder().decode(raw);
        } catch (IllegalArgumentException ex) {
            log.info("ID: [{}]. Input id has wrong format", raw);
            return new byte[0];
        }
    }

    private String addVersionToEncryptedId(String encryptedId) {
        // external Id is identifier of crypto method
        String algorithmVersion = cryptoProviderHolder.getDefaultIdProvider().getCryptoProviderId();
        return concatWithSeparator(encryptedId, algorithmVersion);
    }

    private String concatWithSeparator(String... parts) {
        return StringUtils.join(parts, SEPARATOR);
    }
}
