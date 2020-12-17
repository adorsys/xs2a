/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.domain.account.AisConsentTransaction;
import de.adorsys.psd2.consent.repository.AisConsentTransactionRepository;
import de.adorsys.psd2.consent.repository.AisConsentUsageRepository;
import de.adorsys.psd2.consent.service.mapper.CmsAisConsentMapper;
import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.core.mapper.ConsentDataMapper;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.ais.AccountAccessType.ALL_ACCOUNTS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OneOffConsentExpirationServiceTest {
    private static final Long CONSENT_ID = 123358L;
    private static final String RESOURCE_ID = "LGCGDC4KTx0tgnpZGYTTr8";
    public static final String BENEFICIARIES_URI = "/v1/trusted-beneficiaries";

    @InjectMocks
    private OneOffConsentExpirationService oneOffConsentExpirationService;

    @Mock
    private AisConsentUsageRepository aisConsentUsageRepository;
    @Mock
    private AisConsentTransactionRepository aisConsentTransactionRepository;
    @Mock
    private CmsAisConsentMapper cmsAisConsentMapper;
    @Mock
    private AspspProfileService aspspProfileService;

    @Spy
    private ConsentDataMapper consentDataMapper = new ConsentDataMapper();

    private JsonReader jsonReader = new JsonReader();
    private AisConsentTransaction aisConsentTransaction;
    private CmsConsent cmsConsent;
    private AccountReference accountReference;

    @BeforeEach
    void setUp() {
        accountReference = jsonReader.getObjectFromFile("json/service/account-reference.json", AccountReference.class);
        aisConsentTransaction = new AisConsentTransaction();
        cmsConsent = new CmsConsent();
        cmsConsent.setTppAccountAccesses(AccountAccess.EMPTY_ACCESS);
    }

    @Test
    void isConsentExpired_multipleAccounts_partiallyUsed_shouldReturnFalse() {
        // Given
        cmsConsent.setConsentData(consentDataMapper.getBytesFromConsentData(AisConsentData.buildDefaultAisConsentData()));
        aisConsentTransaction.setNumberOfTransactions(1);

        when(aisConsentTransactionRepository.findByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID, PageRequest.of(0, 1)))
            .thenReturn(Collections.singletonList(aisConsentTransaction));
        when(aisConsentUsageRepository.countByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID))
            .thenReturn(0);

        AisConsent aisConsent = buildAisConsent(accountReference, null, null, null, null);
        cmsConsent.setAspspAccountAccesses(aisConsent.getAspspAccountAccesses());
        when(cmsAisConsentMapper.mapToAisConsent(cmsConsent))
            .thenReturn(aisConsent);

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(cmsConsent, CONSENT_ID);

        // Then
        assertFalse(isExpired);
    }

    @Test
    void isConsentExpired_multipleAccounts_fullyUsed_shouldReturnTrue() {
        // Given
        cmsConsent.setConsentData(consentDataMapper.getBytesFromConsentData(AisConsentData.buildDefaultAisConsentData()));
        aisConsentTransaction.setNumberOfTransactions(1);

        when(aisConsentTransactionRepository.findByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID, PageRequest.of(0, 1)))
            .thenReturn(Collections.singletonList(aisConsentTransaction));
        when(aisConsentUsageRepository.countByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID))
            .thenReturn(1);

        AisConsent aisConsent = buildAisConsent(accountReference, null, null, null, null);
        when(cmsAisConsentMapper.mapToAisConsent(cmsConsent))
            .thenReturn(aisConsent);
        cmsConsent.setAspspAccountAccesses(aisConsent.getAspspAccountAccesses());

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(cmsConsent, CONSENT_ID);

        // Then
        assertTrue(isExpired);
    }

    @Test
    void isConsentExpired_allAvailableAccounts_shouldReturnTrue() {
        // Given
        AisConsent aisConsent = buildAisConsent(accountReference, null, null, ALL_ACCOUNTS, null);
        cmsConsent.setAspspAccountAccesses(aisConsent.getAspspAccountAccesses());
        when(cmsAisConsentMapper.mapToAisConsent(cmsConsent))
            .thenReturn(aisConsent);

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(cmsConsent, CONSENT_ID);

        // Then
        assertTrue(isExpired);
    }

    @Test
    void isConsentExpired_bankOffered_shouldReturnFalse() {
        // Given
        AisConsent aisConsent = buildAisConsent(null, null, null, null, null);
        cmsConsent.setAspspAccountAccesses(aisConsent.getAspspAccountAccesses());
        when(cmsAisConsentMapper.mapToAisConsent(cmsConsent))
            .thenReturn(aisConsent);

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(cmsConsent, CONSENT_ID);

        // Then
        assertFalse(isExpired);
    }

    @Test
    void isConsentExpired_globalFullAccesses_notUsed_shouldReturnFalse() {
        // Given
        aisConsentTransaction.setNumberOfTransactions(1);

        when(aisConsentTransactionRepository.findByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID, PageRequest.of(0, 1)))
            .thenReturn(Collections.singletonList(aisConsentTransaction));
        when(aisConsentUsageRepository.countByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID))
            .thenReturn(0);

        AisConsent aisConsent = buildAisConsent(accountReference, null, null, null, ALL_ACCOUNTS);
        cmsConsent.setAspspAccountAccesses(aisConsent.getAspspAccountAccesses());
        when(cmsAisConsentMapper.mapToAisConsent(cmsConsent))
            .thenReturn(aisConsent);

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(cmsConsent, CONSENT_ID);

        // Then
        assertFalse(isExpired);
    }

    @Test
    void isConsentExpired_globalFullAccesses_fullyUsed_shouldReturnTrue() {
        // Given
        aisConsentTransaction.setNumberOfTransactions(1);

        when(aisConsentTransactionRepository.findByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID, PageRequest.of(0, 1)))
            .thenReturn(Collections.singletonList(aisConsentTransaction));
        when(aisConsentUsageRepository.countByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID))
            .thenReturn(1);

        AisConsent aisConsent = buildAisConsent(accountReference, null, null, null, ALL_ACCOUNTS);
        cmsConsent.setAspspAccountAccesses(aisConsent.getAspspAccountAccesses());
        when(cmsAisConsentMapper.mapToAisConsent(cmsConsent))
            .thenReturn(aisConsent);

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(cmsConsent, CONSENT_ID);

        // Then
        assertTrue(isExpired);
    }

    @Test
    void isConsentExpired_dedicatedWithBalances_partiallyUsed_shouldReturnFalse() {
        // Given
        aisConsentTransaction.setNumberOfTransactions(2);

        when(aisConsentTransactionRepository.findByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID, PageRequest.of(0, 1)))
            .thenReturn(Collections.singletonList(aisConsentTransaction));
        when(aisConsentUsageRepository.countByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID))
            .thenReturn(1);

        AisConsent aisConsent = buildAisConsent(null, accountReference, null, null, null);
        cmsConsent.setAspspAccountAccesses(aisConsent.getAspspAccountAccesses());
        when(cmsAisConsentMapper.mapToAisConsent(cmsConsent))
            .thenReturn(aisConsent);

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(cmsConsent, CONSENT_ID);

        // Then
        assertFalse(isExpired);
    }

    @Test
    void isConsentExpired_dedicatedWithBalances_fullyUsed_shouldReturnTrue() {
        // Given
        aisConsentTransaction.setNumberOfTransactions(2);

        when(aisConsentTransactionRepository.findByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID, PageRequest.of(0, 1)))
            .thenReturn(Collections.singletonList(aisConsentTransaction));
        when(aisConsentUsageRepository.countByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID))
            .thenReturn(2);

        AisConsent aisConsent = buildAisConsent(null, accountReference, null, null, null);
        cmsConsent.setAspspAccountAccesses(aisConsent.getAspspAccountAccesses());
        when(cmsAisConsentMapper.mapToAisConsent(cmsConsent))
            .thenReturn(aisConsent);

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(cmsConsent, CONSENT_ID);

        // Then
        assertTrue(isExpired);
    }

    @Test
    void isConsentExpired_dedicatedWithTransactions_partiallyUsed_shouldReturnFalse() {
        // Given
        aisConsentTransaction.setNumberOfTransactions(2);

        when(aisConsentTransactionRepository.findByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID, PageRequest.of(0, 1)))
            .thenReturn(Collections.singletonList(aisConsentTransaction));
        when(aisConsentUsageRepository.countByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID))
            .thenReturn(1);

        AisConsent aisConsent = buildAisConsent(null, null, accountReference, null, null);
        cmsConsent.setAspspAccountAccesses(aisConsent.getAspspAccountAccesses());
        when(cmsAisConsentMapper.mapToAisConsent(cmsConsent)).thenReturn(aisConsent);

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(cmsConsent, CONSENT_ID);

        // Then
        assertFalse(isExpired);
    }

    @Test
    void isConsentExpired_dedicatedWithTransactions_fullyUsed_shouldReturnTrue() {
        // Given
        aisConsentTransaction.setNumberOfTransactions(2);

        when(aisConsentTransactionRepository.findByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID, PageRequest.of(0, 1)))
            .thenReturn(Collections.singletonList(aisConsentTransaction));
        when(aisConsentUsageRepository.countByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID))
            .thenReturn(4);

        AisConsent aisConsent = buildAisConsent(null, null, accountReference, null, null);
        cmsConsent.setAspspAccountAccesses(aisConsent.getAspspAccountAccesses());
        when(cmsAisConsentMapper.mapToAisConsent(cmsConsent)).thenReturn(aisConsent);

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(cmsConsent, CONSENT_ID);

        // Then
        assertTrue(isExpired);
    }

    @Test
    void isConsentExpired_withBeneficiaries_shouldReturnTrue() {
        // Given
        aisConsentTransaction.setNumberOfTransactions(1);

        when(aisConsentTransactionRepository.findByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID, PageRequest.of(0, 1)))
            .thenReturn(Collections.singletonList(aisConsentTransaction));
        when(aisConsentUsageRepository.countByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID))
            .thenReturn(4);
        when(aisConsentUsageRepository.countByConsentIdAndRequestUri(CONSENT_ID, BENEFICIARIES_URI)).thenReturn(1);

        AisConsent aisConsent = jsonReader.getObjectFromFile("json/ais-consent-dedicated-with-beneficiaries.json", AisConsent.class);
        cmsConsent.setAspspAccountAccesses(aisConsent.getAspspAccountAccesses());
        when(cmsAisConsentMapper.mapToAisConsent(cmsConsent)).thenReturn(aisConsent);

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(cmsConsent, CONSENT_ID);

        // Then
        assertTrue(isExpired);
    }

    @Test
    void isConsentExpired_shouldReturnTrue() {
        // Given
        when(aisConsentUsageRepository.countByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID))
            .thenReturn(3);

        AisConsent aisConsent = buildAisConsent(accountReference, accountReference, accountReference, null, null);
        cmsConsent.setAspspAccountAccesses(aisConsent.getAspspAccountAccesses());
        when(cmsAisConsentMapper.mapToAisConsent(cmsConsent)).thenReturn(aisConsent);

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(cmsConsent, CONSENT_ID);

        // Then
        assertTrue(isExpired);
    }

    @Test
    void isConsentExpired_GlobalConsent_shouldReturnTrue() {
        // Given
        when(aisConsentUsageRepository.countByConsentIdAndResourceId(CONSENT_ID, RESOURCE_ID))
            .thenReturn(3);

        AisConsent aisConsent = buildAisConsent(accountReference, accountReference, accountReference, null, ALL_ACCOUNTS);
        cmsConsent.setAspspAccountAccesses(aisConsent.getAspspAccountAccesses());
        when(cmsAisConsentMapper.mapToAisConsent(cmsConsent)).thenReturn(aisConsent);

        AspspSettings aspspSettings = jsonReader.getObjectFromFile("json/aspsp-settings.json", AspspSettings.class);
        when(aspspProfileService.getAspspSettings(aisConsent.getInstanceId())).thenReturn(aspspSettings);

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(cmsConsent, CONSENT_ID);

        // Then
        assertTrue(isExpired);
    }

    private AisConsent buildAisConsent(AccountReference account,
                                       AccountReference balance,
                                       AccountReference transaction,
                                       AccountAccessType availableAccounts,
                                       AccountAccessType allPsd2){
        AisConsent aisConsent = new AisConsent();

        AccountAccess accountAccess = new AccountAccess(
            Optional.ofNullable(account).map(Collections::singletonList).orElseGet(Collections::emptyList),
            Optional.ofNullable(balance).map(Collections::singletonList).orElseGet(Collections::emptyList),
            Optional.ofNullable(transaction).map(Collections::singletonList).orElseGet(Collections::emptyList),
            null);

        aisConsent.setAspspAccountAccesses(accountAccess);
        aisConsent.setTppAccountAccesses(AccountAccess.EMPTY_ACCESS);

        AisConsentData consentData = new AisConsentData(
            Optional.ofNullable(availableAccounts).orElse(null),
            Optional.ofNullable(allPsd2).orElse(null),
            null,
            false);

        aisConsent.setConsentData(consentData);
        return aisConsent;
    }
}
