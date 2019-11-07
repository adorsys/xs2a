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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OneOffConsentExpirationServiceTest {

    private static final String ACCOUNT_RESOURCE_ID = "account 1";
    private static final String BALANCE_RESOURCE_ID = "balance 1";
    private static final String TRANSACTION_RESOURCE_ID = "transaction 1";

    @InjectMocks
    private OneOffConsentExpirationService oneOffConsentExpirationService;

    @Mock
    private AisConsentUsageRepository aisConsentUsageRepository;
    @Mock
    private AisConsentTransactionRepository aisConsentTransactionRepository;

    @Test
    public void isConsentExpired_allAvailableAccounts_shouldReturnTrue() {
        // Given
        AisConsent aisConsent = new AisConsent();
        aisConsent.setAisConsentRequestType(AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS);

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(aisConsent);

        // Then
        assertTrue(isExpired);
    }

    @Test
    public void isConsentExpired_bankOffered_shouldReturnFalse() {
        // Given
        AisConsent aisConsent = new AisConsent();
        aisConsent.setAisConsentRequestType(AisConsentRequestType.BANK_OFFERED);

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(aisConsent);

        // Then
        assertFalse(isExpired);
    }

    @Test
    public void isConsentExpired_globalFullAccesses_notUsed_shouldReturnFalse() {
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
    public void isConsentExpired_globalFullAccesses_fullyUsed_shouldReturnTrue() {
        // Given
        AisConsentTransaction aisConsentTransaction = new AisConsentTransaction();
        aisConsentTransaction.setNumberOfTransactions(2);

        when(aisConsentTransactionRepository.findByConsentIdAndResourceId(any(AisConsent.class), anyString()))
            .thenReturn(Optional.of(aisConsentTransaction));
        when(aisConsentUsageRepository.countByConsentIdAndResourceId(anyLong(), anyString()))
            .thenReturn(5);

        AisConsent aisConsent = new AisConsent();
        aisConsent.setId(1L);
        aisConsent.setAisConsentRequestType(AisConsentRequestType.GLOBAL);
        aisConsent.setAllPsd2(AccountAccessType.ALL_ACCOUNTS);
        aisConsent.setAspspAccountAccesses(createListOfAccountAccesses());

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(aisConsent);

        // Then
        assertTrue(isExpired);
    }

    @Test
    public void isConsentExpired_dedicatedFullAccesses_notUsed_shouldReturnFalse() {
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
    public void isConsentExpired_dedicatedWithoutBalances_partiallyUsed_shouldReturnFalse() {
        // Given
        AisConsentTransaction aisConsentTransaction = new AisConsentTransaction();
        aisConsentTransaction.setNumberOfTransactions(2);

        when(aisConsentTransactionRepository.findByConsentIdAndResourceId(any(AisConsent.class), anyString()))
            .thenReturn(Optional.of(aisConsentTransaction));

        AisConsent aisConsent = new AisConsent();
        aisConsent.setAisConsentRequestType(AisConsentRequestType.DEDICATED_ACCOUNTS);
        aisConsent.setAspspAccountAccesses(createListOfAccountAccessesWithTransactions());

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(aisConsent);

        // Then
        assertFalse(isExpired);
    }

    @Test
    public void isConsentExpired_dedicatedWithoutBalances_fullyUsed_shouldReturnTrue() {
        // Given
        AisConsentTransaction aisConsentTransaction = new AisConsentTransaction();
        aisConsentTransaction.setNumberOfTransactions(2);

        when(aisConsentTransactionRepository.findByConsentIdAndResourceId(any(AisConsent.class), anyString()))
            .thenReturn(Optional.of(aisConsentTransaction));
        when(aisConsentUsageRepository.countByConsentIdAndResourceId(anyLong(), anyString()))
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
    public void isConsentExpired_dedicatedWithoutTransactions_partiallyUsed_shouldReturnFalse() {
        // Given
        AisConsent aisConsent = new AisConsent();
        aisConsent.setAisConsentRequestType(AisConsentRequestType.DEDICATED_ACCOUNTS);
        aisConsent.setAspspAccountAccesses(createListOfAccountAccessesWithBalances());

        // When
        boolean isExpired = oneOffConsentExpirationService.isConsentExpired(aisConsent);

        assertFalse(isExpired);
    }

    @Test
    public void isConsentExpired_dedicatedWithoutTransactions_fullyUsed_shouldReturnTrue() {
        // Given
        when(aisConsentUsageRepository.countByConsentIdAndResourceId(anyLong(), anyString()))
            .thenReturn(2);

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

        AspspAccountAccess accounts = getAspspAccountAccess(TypeAccess.ACCOUNT, ACCOUNT_RESOURCE_ID);
        AspspAccountAccess balances = getAspspAccountAccess(TypeAccess.BALANCE, BALANCE_RESOURCE_ID);
        AspspAccountAccess transactions = getAspspAccountAccess(TypeAccess.TRANSACTION, TRANSACTION_RESOURCE_ID);

        result.add(accounts);
        result.add(balances);
        result.add(transactions);

        return result;
    }


    private List<AspspAccountAccess> createListOfAccountAccessesWithTransactions() {
        List<AspspAccountAccess> result = new ArrayList<>();

        AspspAccountAccess accounts = getAspspAccountAccess(TypeAccess.ACCOUNT, ACCOUNT_RESOURCE_ID);
        AspspAccountAccess transactions = getAspspAccountAccess(TypeAccess.TRANSACTION, TRANSACTION_RESOURCE_ID);

        result.add(accounts);
        result.add(transactions);

        return result;
    }

    private List<AspspAccountAccess> createListOfAccountAccessesWithBalances() {
        List<AspspAccountAccess> result = new ArrayList<>();

        AspspAccountAccess accounts = getAspspAccountAccess(TypeAccess.ACCOUNT, ACCOUNT_RESOURCE_ID);
        AspspAccountAccess balances = getAspspAccountAccess(TypeAccess.BALANCE, BALANCE_RESOURCE_ID);

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
