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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.TypeAccess;
import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.domain.account.AisConsentTransaction;
import de.adorsys.psd2.consent.domain.account.AspspAccountAccess;
import de.adorsys.psd2.consent.repository.AisConsentTransactionRepository;
import de.adorsys.psd2.consent.repository.AisConsentUsageRepository;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OneOffConsentExpirationServiceTest {
    private static final String ACCOUNT_RESOURCE_ID_1 = "account 1";
    private static final String ACCOUNT_RESOURCE_ID_2 = "account 2";
    private static final String ACCOUNT_RESOURCE_ID_3 = "account 3";

    @InjectMocks
    private OneOffConsentExpirationService oneOffConsentExpirationService;

    @Mock
    private AisConsentUsageRepository aisConsentUsageRepository;
    @Mock
    private AisConsentTransactionRepository aisConsentTransactionRepository;

    @Test
    void isConsentExpired_multipleAccounts_partiallyUsed_shouldReturnFalse() {
        // Given
        AisConsentTransaction aisConsentTransaction = new AisConsentTransaction();
        aisConsentTransaction.setNumberOfTransactions(1);

        when(aisConsentTransactionRepository.findByConsentIdAndResourceId(any(AisConsent.class), eq(ACCOUNT_RESOURCE_ID_1)))
            .thenReturn(Optional.empty());
        when(aisConsentTransactionRepository.findByConsentIdAndResourceId(any(AisConsent.class), eq(ACCOUNT_RESOURCE_ID_2)))
            .thenReturn(Optional.empty());
        when(aisConsentTransactionRepository.findByConsentIdAndResourceId(any(AisConsent.class), eq(ACCOUNT_RESOURCE_ID_3)))
            .thenReturn(Optional.of(aisConsentTransaction));

        when(aisConsentUsageRepository.countByConsentIdAndResourceId(anyLong(), eq(ACCOUNT_RESOURCE_ID_1)))
            .thenReturn(1);
        when(aisConsentUsageRepository.countByConsentIdAndResourceId(anyLong(), eq(ACCOUNT_RESOURCE_ID_2)))
            .thenReturn(2);
        when(aisConsentUsageRepository.countByConsentIdAndResourceId(anyLong(), eq(ACCOUNT_RESOURCE_ID_3)))
            .thenReturn(2);

        AisConsent aisConsent = new AisConsent();
        aisConsent.setId(1L);
        aisConsent.setAspspAccountAccesses(createListOfAccountAccesses());

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(aisConsent);

        // Then
        assertFalse(isExpired);
    }

    @Test
    void isConsentExpired_multipleAccounts_fullyUsed_shouldReturnTrue() {
        // Given
        AisConsentTransaction aisConsentTransaction = new AisConsentTransaction();
        aisConsentTransaction.setNumberOfTransactions(1);

        when(aisConsentTransactionRepository.findByConsentIdAndResourceId(any(AisConsent.class), eq(ACCOUNT_RESOURCE_ID_1)))
            .thenReturn(Optional.empty());
        when(aisConsentTransactionRepository.findByConsentIdAndResourceId(any(AisConsent.class), eq(ACCOUNT_RESOURCE_ID_2)))
            .thenReturn(Optional.empty());
        when(aisConsentTransactionRepository.findByConsentIdAndResourceId(any(AisConsent.class), eq(ACCOUNT_RESOURCE_ID_3)))
            .thenReturn(Optional.of(aisConsentTransaction));

        when(aisConsentUsageRepository.countByConsentIdAndResourceId(anyLong(), eq(ACCOUNT_RESOURCE_ID_1)))
            .thenReturn(1);
        when(aisConsentUsageRepository.countByConsentIdAndResourceId(anyLong(), eq(ACCOUNT_RESOURCE_ID_2)))
            .thenReturn(2);
        when(aisConsentUsageRepository.countByConsentIdAndResourceId(anyLong(), eq(ACCOUNT_RESOURCE_ID_3)))
            .thenReturn(3);

        AisConsent aisConsent = new AisConsent();
        aisConsent.setId(1L);
        aisConsent.setAspspAccountAccesses(createListOfAccountAccesses());

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(aisConsent);

        // Then
        assertTrue(isExpired);
    }

    @Test
    void isConsentExpired_allAvailableAccounts_shouldReturnTrue() {
        // Given
        AisConsent aisConsent = new AisConsent();
        aisConsent.setAisConsentRequestType(AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS);

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(aisConsent);

        // Then
        assertTrue(isExpired);
    }

    @Test
    void isConsentExpired_bankOffered_shouldReturnFalse() {
        // Given
        AisConsent aisConsent = new AisConsent();
        aisConsent.setAisConsentRequestType(AisConsentRequestType.BANK_OFFERED);

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(aisConsent);

        // Then
        assertFalse(isExpired);
    }

    @Test
    void isConsentExpired_globalFullAccesses_notUsed_shouldReturnFalse() {
        // Given
        AisConsent aisConsent = new AisConsent();
        aisConsent.setAisConsentRequestType(AisConsentRequestType.GLOBAL);
        aisConsent.setAspspAccountAccesses(createListOfAccountAccesses());

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(aisConsent);

        // Then
        assertFalse(isExpired);
    }

    @Test
    void isConsentExpired_globalFullAccesses_fullyUsed_shouldReturnTrue() {
        // Given
        AisConsentTransaction aisConsentTransaction = new AisConsentTransaction();
        aisConsentTransaction.setNumberOfTransactions(2);

        when(aisConsentTransactionRepository.findByConsentIdAndResourceId(any(AisConsent.class), eq(ACCOUNT_RESOURCE_ID_1)))
            .thenReturn(Optional.of(aisConsentTransaction));
        when(aisConsentUsageRepository.countByConsentIdAndResourceId(anyLong(), eq(ACCOUNT_RESOURCE_ID_1)))
            .thenReturn(5);

        AisConsent aisConsent = new AisConsent();
        aisConsent.setId(1L);
        aisConsent.setAisConsentRequestType(AisConsentRequestType.GLOBAL);
        aisConsent.setAllPsd2(AccountAccessType.ALL_ACCOUNTS);

        aisConsent.setAspspAccountAccesses(Arrays.asList(getAspspAccountAccess(TypeAccess.ACCOUNT, ACCOUNT_RESOURCE_ID_1),
                                                         getAspspAccountAccess(TypeAccess.BALANCE, ACCOUNT_RESOURCE_ID_1),
                                                         getAspspAccountAccess(TypeAccess.TRANSACTION, ACCOUNT_RESOURCE_ID_1)));

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(aisConsent);

        // Then
        assertTrue(isExpired);
    }

    @Test
    void isConsentExpired_dedicatedFullAccesses_notUsed_shouldReturnFalse() {
        // Given
        AisConsent aisConsent = new AisConsent();
        aisConsent.setAisConsentRequestType(AisConsentRequestType.DEDICATED_ACCOUNTS);
        aisConsent.setAspspAccountAccesses(createListOfAccountAccesses());

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(aisConsent);

        // Then
        assertFalse(isExpired);
    }

    @Test
    void isConsentExpired_dedicatedWithoutBalances_partiallyUsed_shouldReturnFalse() {
        // Given
        AisConsentTransaction aisConsentTransaction = new AisConsentTransaction();
        aisConsentTransaction.setNumberOfTransactions(2);

        when(aisConsentTransactionRepository.findByConsentIdAndResourceId(any(AisConsent.class), eq(ACCOUNT_RESOURCE_ID_1)))
            .thenReturn(Optional.empty());
        when(aisConsentTransactionRepository.findByConsentIdAndResourceId(any(AisConsent.class), eq(ACCOUNT_RESOURCE_ID_3)))
            .thenReturn(Optional.of(aisConsentTransaction));
        when(aisConsentUsageRepository.countByConsentIdAndResourceId(anyLong(), eq(ACCOUNT_RESOURCE_ID_1)))
            .thenReturn(1);
        when(aisConsentUsageRepository.countByConsentIdAndResourceId(anyLong(), eq(ACCOUNT_RESOURCE_ID_3)))
            .thenReturn(3);

        AisConsent aisConsent = new AisConsent();
        aisConsent.setId(1L);
        aisConsent.setAisConsentRequestType(AisConsentRequestType.DEDICATED_ACCOUNTS);
        aisConsent.setAspspAccountAccesses(createListOfAccountAccessesWithTransactions());

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(aisConsent);

        // Then
        assertFalse(isExpired);
    }

    @Test
    void isConsentExpired_dedicatedWithoutBalances_fullyUsed_shouldReturnTrue() {
        // Given
        AisConsentTransaction aisConsentTransaction = new AisConsentTransaction();
        aisConsentTransaction.setNumberOfTransactions(2);

        when(aisConsentTransactionRepository.findByConsentIdAndResourceId(any(AisConsent.class), eq(ACCOUNT_RESOURCE_ID_1)))
            .thenReturn(Optional.empty());
        when(aisConsentTransactionRepository.findByConsentIdAndResourceId(any(AisConsent.class), eq(ACCOUNT_RESOURCE_ID_3)))
            .thenReturn(Optional.of(aisConsentTransaction));
        when(aisConsentUsageRepository.countByConsentIdAndResourceId(anyLong(), eq(ACCOUNT_RESOURCE_ID_1)))
            .thenReturn(1);
        when(aisConsentUsageRepository.countByConsentIdAndResourceId(anyLong(), eq(ACCOUNT_RESOURCE_ID_3)))
            .thenReturn(4);

        AisConsent aisConsent = new AisConsent();
        aisConsent.setId(1L);
        aisConsent.setAisConsentRequestType(AisConsentRequestType.DEDICATED_ACCOUNTS);
        aisConsent.setAspspAccountAccesses(createListOfAccountAccessesWithTransactions());

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(aisConsent);

        // Then
        assertTrue(isExpired);
    }

    @Test
    void isConsentExpired_dedicatedWithoutTransactions_partiallyUsed_shouldReturnFalse() {
        // Given
        AisConsent aisConsent = new AisConsent();
        aisConsent.setAisConsentRequestType(AisConsentRequestType.DEDICATED_ACCOUNTS);
        aisConsent.setAspspAccountAccesses(createListOfAccountAccessesWithBalances());

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(aisConsent);

        assertFalse(isExpired);
    }

    @Test
    void isConsentExpired_dedicatedWithoutTransactions_fullyUsed_shouldReturnTrue() {
        // Given
        when(aisConsentUsageRepository.countByConsentIdAndResourceId(anyLong(), eq(ACCOUNT_RESOURCE_ID_2)))
            .thenReturn(2);
        when(aisConsentUsageRepository.countByConsentIdAndResourceId(anyLong(), eq(ACCOUNT_RESOURCE_ID_1)))
            .thenReturn(1);

        AisConsent aisConsent = new AisConsent();
        aisConsent.setId(1L);
        aisConsent.setAisConsentRequestType(AisConsentRequestType.DEDICATED_ACCOUNTS);
        aisConsent.setAspspAccountAccesses(createListOfAccountAccessesWithBalances());

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(aisConsent);

        // Then
        assertTrue(isExpired);
    }

    private List<AspspAccountAccess> createListOfAccountAccesses() {
        List<AspspAccountAccess> result = new ArrayList<>();

        AspspAccountAccess accounts = getAspspAccountAccess(TypeAccess.ACCOUNT, ACCOUNT_RESOURCE_ID_1);
        AspspAccountAccess balances = getAspspAccountAccess(TypeAccess.BALANCE, ACCOUNT_RESOURCE_ID_2);
        AspspAccountAccess transactions = getAspspAccountAccess(TypeAccess.TRANSACTION, ACCOUNT_RESOURCE_ID_3);

        result.add(accounts);
        result.add(balances);
        result.add(transactions);

        return result;
    }

    private List<AspspAccountAccess> createListOfAccountAccessesWithTransactions() {
        List<AspspAccountAccess> result = new ArrayList<>();

        AspspAccountAccess accounts = getAspspAccountAccess(TypeAccess.ACCOUNT, ACCOUNT_RESOURCE_ID_1);
        AspspAccountAccess transactions = getAspspAccountAccess(TypeAccess.TRANSACTION, ACCOUNT_RESOURCE_ID_3);

        result.add(accounts);
        result.add(transactions);

        return result;
    }

    private List<AspspAccountAccess> createListOfAccountAccessesWithBalances() {
        List<AspspAccountAccess> result = new ArrayList<>();

        AspspAccountAccess accounts = getAspspAccountAccess(TypeAccess.ACCOUNT, ACCOUNT_RESOURCE_ID_1);
        AspspAccountAccess balances = getAspspAccountAccess(TypeAccess.BALANCE, ACCOUNT_RESOURCE_ID_2);

        result.add(accounts);
        result.add(balances);
        return result;
    }

    private AspspAccountAccess getAspspAccountAccess(TypeAccess account, String accountResourceId) {
        AspspAccountAccess accounts = new AspspAccountAccess();
        accounts.setTypeAccess(account);
        accounts.setResourceId(accountResourceId);
        return accounts;
    }
}
