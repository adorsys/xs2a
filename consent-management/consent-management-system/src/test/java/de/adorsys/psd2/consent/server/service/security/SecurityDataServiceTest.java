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

import de.adorsys.psd2.consent.server.domain.CryptoAlgorithm;
import de.adorsys.psd2.consent.server.repository.CryptoAlgorithmRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SecurityDataServiceTest {

    @Mock
    private CryptoProvider aes = new AesEcbCryptoProviderImpl();
    private CryptoProvider jwe = new JweCryptoProviderImpl();
    private String consentId = "9d8db308a-ad6e-4b0b-a2e1-cea6043eb080";
    private String encryptedConsentId = "JnWWElxC8zqRB3RTmCIYFmGMdpEdYEU4C75oWn5jXSwrk9LHx7qHMEqZwzRNkfQg3kqn0nJaPKAcmBKGQxkgUg==_=_v1";
    private String server_key = "mvLBiZsiTbGwrfJB";
    private String aspspConsentData = "VGVzdCBhc3BzcCBkYXRh";
    @Mock
    private CryptoAlgorithmRepository cryptoAlgorithmRepository;

    @InjectMocks
    CryptoProviderFactory cryptoProviderFactory = new CryptoProviderFactory(aes, jwe, cryptoAlgorithmRepository);

    @InjectMocks
    private SecurityDataService securityDataService = new SecurityDataService(server_key, cryptoProviderFactory);

    @Before
    public void setUp() {
        CryptoAlgorithm ca = new CryptoAlgorithm();
        ca.setAlgorithm("AES/GCM/NoPadding");
        ca.setVersion("2");
        ca.setExternalId("bS6p6XvTWI");
       /* when(cryptoProviderFactory.getActualConsentDataCryptoProvider()).thenReturn(jwe);
        when(cryptoProviderFactory.getActualIdentifierCryptoProvider()).thenReturn(aes);
        ;*/
        when(cryptoAlgorithmRepository.findByExternalId("v1")).thenReturn(Optional.of(ca));

    }

    @Test
    public void testEncryptDecryptConsentId() {
        // Then
        Optional<String> encryptedExternalConsentId = securityDataService.getEncryptedId(consentId);
        // Assert
        assertTrue(encryptedExternalConsentId.isPresent());

        // Then
        String encId = encryptedExternalConsentId.get();
        Optional<String> decryptedConsentId = securityDataService.getConsentId(encId);
        // Assert
        assertTrue(decryptedConsentId.isPresent());
        String decId = decryptedConsentId.get();
        assertEquals(consentId, decId);
    }

    @Test
    public void testEncryptDecryptConsentAspspConsentData() {
        // Then
        Optional<EncryptedData> encryptedData = securityDataService.encryptConsentData(encryptedConsentId, aspspConsentData);
        // Assert
        assertTrue(encryptedData.isPresent());
        // Then
        byte[] encryptedAspspConsentData = encryptedData.get().getData();
        Optional<DecryptedData> decryptedData = securityDataService.decryptConsentData(encryptedConsentId, encryptedAspspConsentData);
        // Assert
        assertTrue(decryptedData.isPresent());
        byte[] decAspspConsentData = decryptedData.get().getData();
        assertArrayEquals(aspspConsentData.getBytes(), decAspspConsentData);
    }

    public String encode(String raw) {
        return Base64.getUrlEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public String decode(String raw) {
        return new String(Base64.getDecoder().decode(raw), StandardCharsets.UTF_8);
    }
}
