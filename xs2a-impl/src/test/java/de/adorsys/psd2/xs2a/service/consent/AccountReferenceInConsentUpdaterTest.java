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

package de.adorsys.psd2.xs2a.service.consent;

import de.adorsys.psd2.consent.api.ais.AisAccountAccessInfo;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.domain.CashAccountType;
import de.adorsys.psd2.xs2a.domain.account.AccountStatus;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountDetails;
import de.adorsys.psd2.xs2a.domain.account.Xs2aUsageType;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccountReferenceInConsentUpdaterTest {
    private static final String CONSENT_ID = "consent id";
    private static final String ASPSP_ACCOUNT_ID_1 = "aspsp account id 1";
    private static final String ASPSP_ACCOUNT_ID_2 = "aspsp account id 2";
    private static final String RESOURCE_ID_1 = "resource id 1";
    private static final String RESOURCE_ID_2 = "resource id 2";
    private static final String IBAN_1 = "iban 1";
    private static final String IBAN_2 = "iban 2";
    private static final Currency CURRENCY = Currency.getInstance("EUR");
    private static final AisAccountAccessInfo AIS_ACCOUNT_ACCESS_INFO = new AisAccountAccessInfo();

    @Mock
    private Xs2aAisConsentService aisConsentService;
    @Mock
    private Xs2aAisConsentMapper consentMapper;

    @InjectMocks
    private AccountReferenceInConsentUpdater accountReferenceInConsentUpdater;

    @Before
    public void setUp() {
        when(consentMapper.mapToAisAccountAccessInfo(any()))
            .thenReturn(AIS_ACCOUNT_ACCESS_INFO);
    }

    @Test
    public void updateAccountReferences_shouldSetResourceId() {
        // Given
        ArgumentCaptor<Xs2aAccountAccess> xs2aAccountAccessArgumentCaptor = ArgumentCaptor.forClass(Xs2aAccountAccess.class);

        Xs2aAccountAccess xs2aAccountAccess = buildXs2aAccountAccess(Collections.singletonList(buildAccountReference(ASPSP_ACCOUNT_ID_1, IBAN_1)),
                                                                     Collections.singletonList(buildAccountReference(ASPSP_ACCOUNT_ID_1, IBAN_1)),
                                                                     Collections.singletonList(buildAccountReference(ASPSP_ACCOUNT_ID_1, IBAN_1)));
        Xs2aAccountDetails xs2aAccountDetails = buildXs2aAccountDetails(ASPSP_ACCOUNT_ID_1, RESOURCE_ID_1, IBAN_1);

        // When
        accountReferenceInConsentUpdater.updateAccountReferences(CONSENT_ID, xs2aAccountAccess, Collections.singletonList(xs2aAccountDetails));

        // Then
        verify(consentMapper).mapToAisAccountAccessInfo(xs2aAccountAccessArgumentCaptor.capture());
        Xs2aAccountAccess value = xs2aAccountAccessArgumentCaptor.getValue();

        List<AccountReference> accounts = value.getAccounts();
        assertEquals(1, accounts.size());

        AccountReference accountReference = accounts.get(0);
        assertEquals(IBAN_1, accountReference.getIban());
        assertEquals(RESOURCE_ID_1, accountReference.getResourceId());

        List<AccountReference> balances = value.getBalances();
        assertEquals(1, balances.size());

        AccountReference balanceReference = balances.get(0);
        assertEquals(IBAN_1, balanceReference.getIban());
        assertEquals(RESOURCE_ID_1, balanceReference.getResourceId());

        List<AccountReference> transactions = value.getTransactions();
        assertEquals(1, transactions.size());

        AccountReference transactionReference = transactions.get(0);
        assertEquals(IBAN_1, transactionReference.getIban());
        assertEquals(RESOURCE_ID_1, transactionReference.getResourceId());

        verify(aisConsentService).updateAspspAccountAccess(CONSENT_ID, AIS_ACCOUNT_ACCESS_INFO);
    }

    @Test
    public void updateAccountReferences_withMultipleAccountReferences_shouldProperlyUpdateResourceId() {
        ArgumentCaptor<Xs2aAccountAccess> xs2aAccountAccessArgumentCaptor = ArgumentCaptor.forClass(Xs2aAccountAccess.class);

        // Given
        Xs2aAccountAccess xs2aAccountAccess = buildXs2aAccountAccess(Arrays.asList(buildAccountReference(ASPSP_ACCOUNT_ID_1, IBAN_1),
                                                                                   buildAccountReference(ASPSP_ACCOUNT_ID_2, IBAN_2)),
                                                                     Collections.emptyList(),
                                                                     Collections.singletonList(buildAccountReference(ASPSP_ACCOUNT_ID_1, IBAN_1)));
        Xs2aAccountDetails xs2aAccountDetails = buildXs2aAccountDetails(ASPSP_ACCOUNT_ID_1, RESOURCE_ID_1, IBAN_1);
        Xs2aAccountDetails xs2aAccountDetails2 = buildXs2aAccountDetails(ASPSP_ACCOUNT_ID_2, RESOURCE_ID_2, IBAN_2);

        // When
        accountReferenceInConsentUpdater.updateAccountReferences(CONSENT_ID, xs2aAccountAccess, Arrays.asList(xs2aAccountDetails, xs2aAccountDetails2));

        // Then
        verify(consentMapper).mapToAisAccountAccessInfo(xs2aAccountAccessArgumentCaptor.capture());
        Xs2aAccountAccess value = xs2aAccountAccessArgumentCaptor.getValue();

        List<AccountReference> accounts = value.getAccounts();
        assertEquals(2, accounts.size());

        AccountReference firstAccountReference = accounts.get(0);
        assertEquals(IBAN_1, firstAccountReference.getIban());
        assertEquals(RESOURCE_ID_1, firstAccountReference.getResourceId());

        AccountReference secondAccountReference = accounts.get(1);
        assertEquals(IBAN_2, secondAccountReference.getIban());
        assertEquals(RESOURCE_ID_2, secondAccountReference.getResourceId());

        List<AccountReference> balances = value.getBalances();
        assertTrue(balances.isEmpty());

        List<AccountReference> transactions = value.getTransactions();
        assertEquals(1, transactions.size());

        AccountReference transactionReference = transactions.get(0);
        assertEquals(IBAN_1, transactionReference.getIban());
        assertEquals(RESOURCE_ID_1, transactionReference.getResourceId());

        verify(aisConsentService).updateAspspAccountAccess(CONSENT_ID, AIS_ACCOUNT_ACCESS_INFO);
    }

    @Test
    public void updateAccountReferences_withResourceIdInAccounts_shouldNotUpdateBalancesOrTransactions() {
        ArgumentCaptor<Xs2aAccountAccess> xs2aAccountAccessArgumentCaptor = ArgumentCaptor.forClass(Xs2aAccountAccess.class);

        // Given
        Xs2aAccountAccess xs2aAccountAccess = buildXs2aAccountAccess(Collections.singletonList(buildAccountReference(ASPSP_ACCOUNT_ID_1, IBAN_1)),
                                                                     Collections.emptyList(),
                                                                     Collections.emptyList());
        Xs2aAccountDetails xs2aAccountDetails = buildXs2aAccountDetails(ASPSP_ACCOUNT_ID_1, RESOURCE_ID_1, IBAN_1);

        // When
        accountReferenceInConsentUpdater.updateAccountReferences(CONSENT_ID, xs2aAccountAccess, Collections.singletonList(xs2aAccountDetails));

        // Then
        verify(consentMapper).mapToAisAccountAccessInfo(xs2aAccountAccessArgumentCaptor.capture());
        Xs2aAccountAccess value = xs2aAccountAccessArgumentCaptor.getValue();

        List<AccountReference> accounts = value.getAccounts();
        assertEquals(1, accounts.size());

        AccountReference accountReference = accounts.get(0);
        assertEquals(IBAN_1, accountReference.getIban());
        assertEquals(RESOURCE_ID_1, accountReference.getResourceId());

        List<AccountReference> balances = value.getBalances();
        assertTrue(balances.isEmpty());

        List<AccountReference> transactions = value.getTransactions();
        assertTrue(transactions.isEmpty());

        verify(aisConsentService).updateAspspAccountAccess(CONSENT_ID, AIS_ACCOUNT_ACCESS_INFO);
    }

    private Xs2aAccountAccess buildXs2aAccountAccess(List<AccountReference> accounts,
                                                     List<AccountReference> balances,
                                                     List<AccountReference> transactions) {
        return new Xs2aAccountAccess(accounts, balances, transactions, AccountAccessType.ALL_ACCOUNTS_WITH_BALANCES, AccountAccessType.ALL_ACCOUNTS);
    }

    private Xs2aAccountDetails buildXs2aAccountDetails(String aspspAccountId, String resourceId, String iban) {
        return new Xs2aAccountDetails(aspspAccountId, resourceId, iban, null, null, null, null,
                                      CURRENCY, null, null, null,
                                      null, null, null, null,
                                      null, Collections.emptyList());
    }

    private AccountReference buildAccountReference(String aspspAccountId, String iban) {
        return new AccountReference(aspspAccountId, null, iban, null, null, null, null, Currency.getInstance("EUR"));
    }
}
