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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityDataServiceTest {
    private static final String SERVER_KEY = "Some secret key";
    private static final String SEPARATOR = "_=_";

    private static final String CONSENT_ID = "fa6e687b-1ac9-4b1a-9c74-357c35c82ba1";
    private static final String CONSENT_KEY = "rvIgmcYarc8eMbqk";
    private static final int CONSENT_KEY_LENGTH = 16;
    private static final byte[] CONSENT_DATA = "Consent data".getBytes();

    private static final String CRYPTO_PROVIDER_ID = "mock";
    private static final String CRYPTO_PROVIDER_DATA = "mock_CP_for_data";
    private static final String FAILING_CRYPTO_PROVIDER_ID = "failing";
    private static final String NON_EXISTING_CRYPT_PROVIDER_ID = "nonExisting";
    private static final CryptoProvider CRYPTO_PROVIDER = new MockCryptoProvider(CRYPTO_PROVIDER_ID, false);
    private static final CryptoProvider FAILING_CRYPTO_PROVIDER = new MockCryptoProvider(FAILING_CRYPTO_PROVIDER_ID,
                                                                                                 true);

    @Mock
    private CryptoProviderHolder cryptoProviderHolder;
    private SecurityDataService securityDataService;

    @Mock
    private Environment environment;

    @BeforeEach
    void setUp() {
        environment = mock(Environment.class);

        when(environment.getProperty("server_key")).thenReturn(SERVER_KEY);

        securityDataService = new SecurityDataService(environment, cryptoProviderHolder);
    }

    @Test
    void getEncryptedId_Success() {
        // Given
        when(cryptoProviderHolder.getDefaultIdProvider())
            .thenReturn(CRYPTO_PROVIDER);
        when(cryptoProviderHolder.getDefaultDataProvider())
            .thenReturn(CRYPTO_PROVIDER);

        // When
        Optional<String> actual = securityDataService.encryptId(CONSENT_ID);

        // Then
        assertTrue(actual.isPresent());

        // When
        String encryptedId = actual.get();

        // Then
        assertTrue(encryptedId.endsWith(SEPARATOR + CRYPTO_PROVIDER_ID));

        // When
        String[] splitId = decryptAndSplitConsentId(encryptedId);
        String consentId = splitId[0];
        String consentKey = splitId[1];

        // Then
        assertEquals(CONSENT_ID, consentId);
        assertEquals(CONSENT_KEY_LENGTH, consentKey.length());
    }

    @Test
    void getEncryptedId_Failure_EncryptionError() {
        // Given
        when(cryptoProviderHolder.getDefaultIdProvider())
            .thenReturn(FAILING_CRYPTO_PROVIDER);

        when(cryptoProviderHolder.getDefaultDataProvider())
            .thenReturn(FAILING_CRYPTO_PROVIDER);

        // When
        Optional<String> actual = securityDataService.encryptId(CONSENT_ID);

        // Then
        assertFalse(actual.isPresent());
    }

    @Test
    void getConsentId_Success() {
        when(cryptoProviderHolder.getProviderById(CRYPTO_PROVIDER_ID)).thenReturn(Optional.of(CRYPTO_PROVIDER));

        // Given
        String encrypted = getEncryptedConsentId(CRYPTO_PROVIDER_ID);

        // When
        Optional<String> actual = securityDataService.decryptId(encrypted);

        // Then
        assertTrue(actual.isPresent());
        assertEquals(CONSENT_ID, actual.get());
    }

    @Test
    void getConsentId_Failure_WrongExternalIdFormat() {
        // When
        Optional<String> actual = securityDataService.decryptId(CONSENT_ID);

        // Then
        assertFalse(actual.isPresent());
    }

    @Test
    void getConsentId_Failure_NonExistingAlgorithm() {
        // Given
        String encrypted = getEncryptedConsentId(NON_EXISTING_CRYPT_PROVIDER_ID);
        when(cryptoProviderHolder.getProviderById(NON_EXISTING_CRYPT_PROVIDER_ID)).thenReturn(Optional.empty());

        // When
        Optional<String> actual = securityDataService.decryptId(encrypted);

        // Then
        assertFalse(actual.isPresent());
    }

    @Test
    void getConsentId_Failure_DecryptionError() {
        // Given
        String encrypted = getEncryptedConsentId(FAILING_CRYPTO_PROVIDER_ID);
        when(cryptoProviderHolder.getProviderById(FAILING_CRYPTO_PROVIDER_ID)).thenReturn(Optional.of(FAILING_CRYPTO_PROVIDER));

        // When
        Optional<String> actual = securityDataService.decryptId(encrypted);

        // Then
        assertFalse(actual.isPresent());
    }

    @Test
    void encryptConsentData_Success() {
        // Given
        String encryptedId = getEncryptedConsentId(CRYPTO_PROVIDER_ID);
        byte[] dataWithKey = ArrayUtils.addAll(CONSENT_DATA, CONSENT_KEY.getBytes());
        when(cryptoProviderHolder.getProviderById(CRYPTO_PROVIDER_ID)).thenReturn(Optional.of(CRYPTO_PROVIDER));
        when(cryptoProviderHolder.getProviderById(CRYPTO_PROVIDER_DATA)).thenReturn(Optional.of(CRYPTO_PROVIDER));

        EncryptedData expected = new EncryptedData(dataWithKey);

        // When
        Optional<EncryptedData> actual = securityDataService.encryptConsentData(encryptedId, CONSENT_DATA);

        // Then
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
    }

    @Test
    void encryptConsentData_Failure_NonExistingAlgorithm() {
        // Given
        String encryptedId = getEncryptedConsentId(NON_EXISTING_CRYPT_PROVIDER_ID);
        when(cryptoProviderHolder.getProviderById(NON_EXISTING_CRYPT_PROVIDER_ID)).thenReturn(Optional.empty());

        // When
        Optional<EncryptedData> actual = securityDataService.encryptConsentData(encryptedId, CONSENT_DATA);

        // Then
        assertFalse(actual.isPresent());
    }

    @Test
    void encryptConsentData_Failure_DecryptionError() {
        // Given
        String encryptedId = getEncryptedConsentId(FAILING_CRYPTO_PROVIDER_ID);
        when(cryptoProviderHolder.getProviderById(FAILING_CRYPTO_PROVIDER_ID)).thenReturn(Optional.of(FAILING_CRYPTO_PROVIDER));

        // When
        Optional<EncryptedData> actual = securityDataService.encryptConsentData(encryptedId, CONSENT_DATA);

        // Then
        assertFalse(actual.isPresent());
    }

    @Test
    void decryptConsentData_Success() {
        // Given
        String encryptedId = getEncryptedConsentId(CRYPTO_PROVIDER_ID);
        byte[] encryptedConsentData = CRYPTO_PROVIDER.encryptData(CONSENT_DATA, CONSENT_KEY).get().getData();
        DecryptedData expected = new DecryptedData(CONSENT_DATA);
        when(cryptoProviderHolder.getProviderById(CRYPTO_PROVIDER_ID)).thenReturn(Optional.of(CRYPTO_PROVIDER));
        when(cryptoProviderHolder.getProviderById(CRYPTO_PROVIDER_DATA)).thenReturn(Optional.of(CRYPTO_PROVIDER));

        // When
        Optional<DecryptedData> actual = securityDataService.decryptConsentData(encryptedId, encryptedConsentData);

        // Then
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
    }

    @Test
    void decryptConsentData_Failure_NonExistingAlgorithm() {
        when(cryptoProviderHolder.getProviderById(NON_EXISTING_CRYPT_PROVIDER_ID)).thenReturn(Optional.empty());

        // Given
        String encryptedId = getEncryptedConsentId(NON_EXISTING_CRYPT_PROVIDER_ID);
        byte[] encryptedConsentData = CRYPTO_PROVIDER.encryptData(CONSENT_DATA, CONSENT_KEY).get().getData();

        // When
        Optional<DecryptedData> actual = securityDataService.decryptConsentData(encryptedId, encryptedConsentData);

        // Then
        assertFalse(actual.isPresent());
    }

    @Test
    void decryptConsentData_Failure_DecryptionError() {
        // Given
        String encryptedId = getEncryptedConsentId(FAILING_CRYPTO_PROVIDER_ID);
        byte[] encryptedConsentData = CRYPTO_PROVIDER.encryptData(CONSENT_DATA, CONSENT_KEY).get().getData();
        when(cryptoProviderHolder.getProviderById(FAILING_CRYPTO_PROVIDER_ID)).thenReturn(Optional.of(FAILING_CRYPTO_PROVIDER));

        // When
        Optional<DecryptedData> actual = securityDataService.decryptConsentData(encryptedId, encryptedConsentData);

        // Then
        assertFalse(actual.isPresent());
    }

    private String getEncryptedConsentId(String cryptoProviderId) {
        String compositeId = CONSENT_ID + SEPARATOR + CONSENT_KEY+SEPARATOR +CRYPTO_PROVIDER_DATA;
        String encodedCompositeId = encodeToBase64(compositeId);
        return encodedCompositeId + SEPARATOR + cryptoProviderId;
    }

    private String[] decryptAndSplitConsentId(String encryptedId) {
        String withoutSeparator = StringUtils.removeEnd(encryptedId, SEPARATOR + CRYPTO_PROVIDER_ID);
        String encryptedDecoded = new String(Base64.getDecoder().decode(withoutSeparator));
        String decrypted = new String(CRYPTO_PROVIDER
                                          .decryptData(encryptedDecoded.getBytes(), SERVER_KEY)
                                          .get()
                                          .getData());
        return StringUtils.splitByWholeSeparator(decrypted, SEPARATOR);
    }

    private String encodeToBase64(String raw) {
        return Base64.getEncoder().encodeToString(raw.getBytes());
    }
}
