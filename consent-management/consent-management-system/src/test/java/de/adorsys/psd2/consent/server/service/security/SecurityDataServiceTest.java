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

import de.adorsys.psd2.consent.service.security.DecryptedData;
import de.adorsys.psd2.consent.service.security.EncryptedData;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.consent.service.security.provider.CryptoProvider;
import de.adorsys.psd2.consent.service.security.provider.CryptoProviderFactory;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SecurityDataServiceTest {
    private static final String SERVER_KEY = "Some secret key";
    private static final String SEPARATOR = "_=_";

    private static final String CONSENT_ID = "fa6e687b-1ac9-4b1a-9c74-357c35c82ba1";
    private static final String CONSENT_KEY = "rvIgmcYarc8eMbqk";
    private static final int CONSENT_KEY_LENGTH = 16;
    private static final byte[] CONSENT_DATA = "Consent data".getBytes();
    private static final String CONSENT_DATA_BASE64 = Base64.getEncoder().encodeToString(CONSENT_DATA);

    private static final String CRYPTO_PROVIDER_ID = "mock";
    private static final String FAILING_CRYPTO_PROVIDER_ID = "failing";
    private static final String NON_EXISTING_CRYPT_PROVIDER_ID = "nonExisting";
    private static final CryptoProvider CRYPTO_PROVIDER = new MockCryptoProvider(CRYPTO_PROVIDER_ID, false);
    private static final CryptoProvider FAILING_CRYPTO_PROVIDER = new MockCryptoProvider(FAILING_CRYPTO_PROVIDER_ID,
                                                                                         true);

    @Mock
    private CryptoProviderFactory cryptoProviderFactory;
    private SecurityDataService securityDataService;

    @Mock
    private Environment environment;

    @Before
    public void setUp() {
        environment = mock(Environment.class);

        when(environment.getProperty("server_key")).thenReturn(SERVER_KEY);

        securityDataService = new SecurityDataService(environment, cryptoProviderFactory);

        when(cryptoProviderFactory.getCryptoProviderByAlgorithmVersion(CRYPTO_PROVIDER_ID))
            .thenReturn(Optional.of(CRYPTO_PROVIDER));

        when(cryptoProviderFactory.getCryptoProviderByAlgorithmVersion(FAILING_CRYPTO_PROVIDER_ID))
            .thenReturn(Optional.of(FAILING_CRYPTO_PROVIDER));
        when(cryptoProviderFactory.getCryptoProviderByAlgorithmVersion(NON_EXISTING_CRYPT_PROVIDER_ID))
            .thenReturn(Optional.empty());
    }

    @Test
    public void getEncryptedId_Success() throws Exception {
        // Given
        when(cryptoProviderFactory.actualIdentifierCryptoProvider())
            .thenReturn(CRYPTO_PROVIDER);

        // When
        Optional<String> actual = securityDataService.encryptId(CONSENT_ID);

        // Then
        assertThat(actual.isPresent()).isTrue();

        // When
        String encryptedId = actual.get();

        // Then
        assertThat(encryptedId).endsWith(SEPARATOR + CRYPTO_PROVIDER_ID);

        // When
        String[] splitId = decryptAndSplitConsentId(encryptedId);
        String consentId = splitId[0];
        String consentKey = splitId[1];

        // Then
        assertThat(consentId).isEqualTo(CONSENT_ID);
        assertThat(consentKey.length()).isEqualTo(CONSENT_KEY_LENGTH);
    }

    @Test
    public void getEncryptedId_Failure_EncryptionError() {
        // Given
        when(cryptoProviderFactory.actualIdentifierCryptoProvider())
            .thenReturn(FAILING_CRYPTO_PROVIDER);

        // When
        Optional<String> actual = securityDataService.encryptId(CONSENT_ID);

        // Then
        assertThat(actual.isPresent()).isFalse();
    }

    @Test
    public void getConsentId_Success() {
        // Given
        String encrypted = getEncryptedConsentId(CRYPTO_PROVIDER_ID);

        // When
        Optional<String> actual = securityDataService.decryptId(encrypted);

        // Then
        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(CONSENT_ID);
    }

    @Test
    public void getConsentId_Failure_WrongExternalIdFormat() {
        // When
        Optional<String> actual = securityDataService.decryptId(CONSENT_ID);

        // Then
        assertThat(actual.isPresent()).isFalse();
    }

    @Test
    public void getConsentId_Failure_NonExistingAlgorithm() {
        // Given
        String encrypted = getEncryptedConsentId(NON_EXISTING_CRYPT_PROVIDER_ID);

        // When
        Optional<String> actual = securityDataService.decryptId(encrypted);

        // Then
        assertThat(actual.isPresent()).isFalse();
    }

    @Test
    public void getConsentId_Failure_DecryptionError() {
        // Given
        String encrypted = getEncryptedConsentId(FAILING_CRYPTO_PROVIDER_ID);

        // When
        Optional<String> actual = securityDataService.decryptId(encrypted);

        // Then
        assertThat(actual.isPresent()).isFalse();
    }

    @Test
    public void encryptConsentData_Success() {
        when(cryptoProviderFactory.actualConsentDataCryptoProvider())
            .thenReturn(CRYPTO_PROVIDER);

        // Given
        String encryptedId = getEncryptedConsentId(CRYPTO_PROVIDER_ID);
        byte[] dataWithKey = ArrayUtils.addAll(CONSENT_DATA, CONSENT_KEY.getBytes());
        ;
        EncryptedData expected = new EncryptedData(dataWithKey);

        // When
        Optional<EncryptedData> actual = securityDataService.encryptConsentData(encryptedId, CONSENT_DATA_BASE64);

        // Then
        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    public void encryptConsentData_Failure_NonExistingAlgorithm() {
        when(cryptoProviderFactory.actualConsentDataCryptoProvider())
            .thenReturn(CRYPTO_PROVIDER);

        // Given
        String encryptedId = getEncryptedConsentId(NON_EXISTING_CRYPT_PROVIDER_ID);

        // When
        Optional<EncryptedData> actual = securityDataService.encryptConsentData(encryptedId, CONSENT_DATA_BASE64);

        // Then
        assertThat(actual.isPresent()).isFalse();
    }

    @Test
    public void encryptConsentData_Failure_DecryptionError() {
        when(cryptoProviderFactory.actualConsentDataCryptoProvider())
            .thenReturn(FAILING_CRYPTO_PROVIDER);

        // Given
        String encryptedId = getEncryptedConsentId(CRYPTO_PROVIDER_ID);

        // When
        Optional<EncryptedData> actual = securityDataService.encryptConsentData(encryptedId, CONSENT_DATA_BASE64);

        // Then
        assertThat(actual.isPresent()).isFalse();
    }

    @Test
    public void decryptConsentData_Success() throws Exception {
        when(cryptoProviderFactory.actualConsentDataCryptoProvider())
            .thenReturn(CRYPTO_PROVIDER);

        // Given
        String encryptedId = getEncryptedConsentId(CRYPTO_PROVIDER_ID);
        byte[] encryptedConsentData = CRYPTO_PROVIDER.encryptData(CONSENT_DATA, CONSENT_KEY).get().getData();
        DecryptedData expected = new DecryptedData(CONSENT_DATA);

        // When
        Optional<DecryptedData> actual = securityDataService.decryptConsentData(encryptedId, encryptedConsentData);

        // Then
        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    public void decryptConsentData_Failure_NonExistingAlgorithm() throws Exception {
        // Given
        String encryptedId = getEncryptedConsentId(NON_EXISTING_CRYPT_PROVIDER_ID);
        byte[] encryptedConsentData = CRYPTO_PROVIDER.encryptData(CONSENT_DATA, CONSENT_KEY).get().getData();

        // When
        Optional<DecryptedData> actual = securityDataService.decryptConsentData(encryptedId, encryptedConsentData);

        // Then
        assertThat(actual.isPresent()).isFalse();
    }

    @Test
    public void decryptConsentData_Failure_DecryptionError() throws Exception {
        when(cryptoProviderFactory.actualConsentDataCryptoProvider())
            .thenReturn(FAILING_CRYPTO_PROVIDER);

        // Given
        String encryptedId = getEncryptedConsentId(CRYPTO_PROVIDER_ID);
        byte[] encryptedConsentData = CRYPTO_PROVIDER.encryptData(CONSENT_DATA, CONSENT_KEY).get().getData();

        // When
        Optional<DecryptedData> actual = securityDataService.decryptConsentData(encryptedId, encryptedConsentData);

        // Then
        assertThat(actual.isPresent()).isFalse();
    }

    private String getEncryptedConsentId(String cryptoProviderId) {
        String compositeId = CONSENT_ID + SEPARATOR + CONSENT_KEY;
        String encodedCompositeId = encodeToBase64(compositeId);
        return encodedCompositeId + SEPARATOR + cryptoProviderId;
    }

    private String[] decryptAndSplitConsentId(String encryptedId) throws Exception {
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
