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

package de.adorsys.psd2.xs2a.service.consent;

import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountDetails;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCardAccountDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountReferenceInConsentUpdaterTest {
    private static final String CONSENT_ID = "consent id";
    private static final String ASPSP_ACCOUNT_ID_1 = "aspsp account id 1";
    private static final String ASPSP_ACCOUNT_ID_2 = "aspsp account id 2";
    private static final String RESOURCE_ID_1 = "resource id 1";
    private static final String RESOURCE_ID_2 = "resource id 2";
    private static final String IBAN_1 = "iban 1";
    private static final String IBAN_2 = "iban 2";
    private static final String MASKED_PAN_1 = "123456xxxxxx1234";
    private static final String MASKED_PAN_2 = "525412******3241";
    private static final Currency CURRENCY = Currency.getInstance("EUR");

    @Mock
    private Xs2aAisConsentService aisConsentService;
    @Mock
    private CardAccountHandler cardAccountHandler;
    @Mock
    private Xs2aPiisConsentService xs2aPiisConsentService;

    @InjectMocks
    private AccountReferenceInConsentUpdater accountReferenceInConsentUpdater;


    @Test
    void updateAccountReferences_withGlobalConsent_shouldSetResourceId() {
        // Given
        ArgumentCaptor<AccountAccess> xs2aAccountAccessArgumentCaptor = ArgumentCaptor.forClass(AccountAccess.class);

        AccountAccess accountAccess = buildXs2aAccountAccess(Collections.emptyList(),
                                                             Collections.emptyList(),
                                                             Collections.emptyList());
        AisConsent aisConsent = buildAisConsent(accountAccess, AccountAccessType.ALL_ACCOUNTS);
        Xs2aAccountDetails xs2aAccountDetails = buildXs2aAccountDetails(ASPSP_ACCOUNT_ID_1, RESOURCE_ID_1, IBAN_1);

        // When
        accountReferenceInConsentUpdater.updateAccountReferences(CONSENT_ID, aisConsent, Collections.singletonList(xs2aAccountDetails));

        // Then
        verify(aisConsentService).updateAspspAccountAccess(eq(CONSENT_ID), xs2aAccountAccessArgumentCaptor.capture());

        AccountAccess value = xs2aAccountAccessArgumentCaptor.getValue();

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
    }

    @Test
    void updateAccountReferences_withGlobalConsentForOwnerName_shouldSetResourceId() {
        // Given
        ArgumentCaptor<AccountAccess> xs2aAccountAccessArgumentCaptor = ArgumentCaptor.forClass(AccountAccess.class);

        AccountAccess accountAccess = buildXs2aAccountAccess(Collections.emptyList(),
                                                             Collections.emptyList(),
                                                             Collections.emptyList());
        AisConsent aisConsent = buildAisConsent(accountAccess, AccountAccessType.ALL_ACCOUNTS_WITH_OWNER_NAME);
        Xs2aAccountDetails xs2aAccountDetails = buildXs2aAccountDetails(ASPSP_ACCOUNT_ID_1, RESOURCE_ID_1, IBAN_1);

        // When
        accountReferenceInConsentUpdater.updateAccountReferences(CONSENT_ID, aisConsent, Collections.singletonList(xs2aAccountDetails));

        // Then
        verify(aisConsentService).updateAspspAccountAccess(eq(CONSENT_ID), xs2aAccountAccessArgumentCaptor.capture());
        AccountAccess value = xs2aAccountAccessArgumentCaptor.getValue();

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

    }

    @Test
    void updateAccountReferences_shouldSetResourceId() {
        // Given
        ArgumentCaptor<AccountAccess> xs2aAccountAccessArgumentCaptor = ArgumentCaptor.forClass(AccountAccess.class);

        AccountAccess accountAccess = buildXs2aAccountAccess(Collections.singletonList(buildAccountReferenceWithIban(IBAN_1)),
                                                             Collections.singletonList(buildAccountReferenceWithIban(IBAN_1)),
                                                             Collections.singletonList(buildAccountReferenceWithIban(IBAN_1)));
        AisConsent aisConsent = buildAisConsent(accountAccess, null);
        Xs2aAccountDetails xs2aAccountDetails = buildXs2aAccountDetails(ASPSP_ACCOUNT_ID_1, RESOURCE_ID_1, IBAN_1);

        // When
        accountReferenceInConsentUpdater.updateAccountReferences(CONSENT_ID, aisConsent, Collections.singletonList(xs2aAccountDetails));

        // Then
        verify(aisConsentService).updateAspspAccountAccess(eq(CONSENT_ID), xs2aAccountAccessArgumentCaptor.capture());

        AccountAccess value = xs2aAccountAccessArgumentCaptor.getValue();

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
    }

    @Test
    void updateAccountReferences_withMultipleAccountReferences_shouldProperlyUpdateResourceId() {
        ArgumentCaptor<AccountAccess> xs2aAccountAccessArgumentCaptor = ArgumentCaptor.forClass(AccountAccess.class);

        // Given
        AccountAccess accountAccess = buildXs2aAccountAccess(Arrays.asList(buildAccountReferenceWithIban(IBAN_1),
                                                                           buildAccountReferenceWithIban(IBAN_2)),
                                                             Collections.emptyList(),
                                                             Collections.singletonList(buildAccountReferenceWithIban(IBAN_1)));
        AisConsent aisConsent = buildAisConsent(accountAccess, null);
        Xs2aAccountDetails xs2aAccountDetails = buildXs2aAccountDetails(ASPSP_ACCOUNT_ID_1, RESOURCE_ID_1, IBAN_1);
        Xs2aAccountDetails xs2aAccountDetails2 = buildXs2aAccountDetails(ASPSP_ACCOUNT_ID_2, RESOURCE_ID_2, IBAN_2);

        // When
        accountReferenceInConsentUpdater.updateAccountReferences(CONSENT_ID, aisConsent, Arrays.asList(xs2aAccountDetails, xs2aAccountDetails2));

        // Then
        verify(aisConsentService).updateAspspAccountAccess(eq(CONSENT_ID), xs2aAccountAccessArgumentCaptor.capture());

        AccountAccess value = xs2aAccountAccessArgumentCaptor.getValue();

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
    }

    @Test
    void updateAccountReferences_withResourceIdInAccounts_shouldNotUpdateBalancesOrTransactions() {
        ArgumentCaptor<AccountAccess> xs2aAccountAccessArgumentCaptor = ArgumentCaptor.forClass(AccountAccess.class);

        // Given
        AccountAccess accountAccess = buildXs2aAccountAccess(Collections.singletonList(buildAccountReferenceWithIban(IBAN_1)),
                                                             Collections.emptyList(),
                                                             Collections.emptyList());
        AisConsent aisConsent = buildAisConsent(accountAccess, null);
        Xs2aAccountDetails xs2aAccountDetails = buildXs2aAccountDetails(ASPSP_ACCOUNT_ID_1, RESOURCE_ID_1, IBAN_1);

        // When
        accountReferenceInConsentUpdater.updateAccountReferences(CONSENT_ID, aisConsent, Collections.singletonList(xs2aAccountDetails));

        // Then
        verify(aisConsentService).updateAspspAccountAccess(eq(CONSENT_ID), xs2aAccountAccessArgumentCaptor.capture());

        AccountAccess value = xs2aAccountAccessArgumentCaptor.getValue();

        List<AccountReference> accounts = value.getAccounts();
        assertEquals(1, accounts.size());

        AccountReference accountReference = accounts.get(0);
        assertEquals(IBAN_1, accountReference.getIban());
        assertEquals(RESOURCE_ID_1, accountReference.getResourceId());

        List<AccountReference> balances = value.getBalances();
        assertTrue(balances.isEmpty());

        List<AccountReference> transactions = value.getTransactions();
        assertTrue(transactions.isEmpty());
    }

    @Test
    void updateCardAccountReferences_withGlobalConsent_shouldSetResourceId() {
        // Given
        ArgumentCaptor<AccountAccess> xs2aAccountAccessArgumentCaptor = ArgumentCaptor.forClass(AccountAccess.class);

        AccountAccess accountAccess = buildXs2aAccountAccess(Collections.emptyList(),
                                                             Collections.emptyList(),
                                                             Collections.emptyList());
        AisConsent aisConsent = buildAisConsent(accountAccess, AccountAccessType.ALL_ACCOUNTS);
        Xs2aCardAccountDetails xs2aCardAccountDetails = buildXs2aCardAccountDetails(ASPSP_ACCOUNT_ID_1, RESOURCE_ID_1, MASKED_PAN_1);

        // When
        accountReferenceInConsentUpdater.updateCardAccountReferences(CONSENT_ID, aisConsent, Collections.singletonList(xs2aCardAccountDetails));

        // Then
        verify(aisConsentService).updateAspspAccountAccess(eq(CONSENT_ID), xs2aAccountAccessArgumentCaptor.capture());
        AccountAccess value = xs2aAccountAccessArgumentCaptor.getValue();

        List<AccountReference> accounts = value.getAccounts();
        assertEquals(1, accounts.size());

        AccountReference accountReference = accounts.get(0);
        assertEquals(MASKED_PAN_1, accountReference.getMaskedPan());
        assertEquals(RESOURCE_ID_1, accountReference.getResourceId());

        List<AccountReference> balances = value.getBalances();
        assertEquals(1, balances.size());

        AccountReference balanceReference = balances.get(0);
        assertEquals(MASKED_PAN_1, balanceReference.getMaskedPan());
        assertEquals(RESOURCE_ID_1, balanceReference.getResourceId());

        List<AccountReference> transactions = value.getTransactions();
        assertEquals(1, transactions.size());

        AccountReference transactionReference = transactions.get(0);
        assertEquals(MASKED_PAN_1, transactionReference.getMaskedPan());
        assertEquals(RESOURCE_ID_1, transactionReference.getResourceId());
    }

    @Test
    void updateCardAccountReferences_withGlobalConsentForOwnerName_shouldSetResourceId() {
        // Given
        ArgumentCaptor<AccountAccess> xs2aAccountAccessArgumentCaptor = ArgumentCaptor.forClass(AccountAccess.class);

        AccountAccess accountAccess = buildXs2aAccountAccess(Collections.emptyList(),
                                                             Collections.emptyList(),
                                                             Collections.emptyList());
        AisConsent aisConsent = buildAisConsent(accountAccess, AccountAccessType.ALL_ACCOUNTS_WITH_OWNER_NAME);
        Xs2aCardAccountDetails xs2aCardAccountDetails = buildXs2aCardAccountDetails(ASPSP_ACCOUNT_ID_1, RESOURCE_ID_1, MASKED_PAN_1);

        // When
        accountReferenceInConsentUpdater.updateCardAccountReferences(CONSENT_ID, aisConsent, Collections.singletonList(xs2aCardAccountDetails));

        // Then
        verify(aisConsentService).updateAspspAccountAccess(eq(CONSENT_ID), xs2aAccountAccessArgumentCaptor.capture());
        AccountAccess value = xs2aAccountAccessArgumentCaptor.getValue();

        List<AccountReference> accounts = value.getAccounts();
        assertEquals(1, accounts.size());

        AccountReference accountReference = accounts.get(0);
        assertEquals(MASKED_PAN_1, accountReference.getMaskedPan());
        assertEquals(RESOURCE_ID_1, accountReference.getResourceId());

        List<AccountReference> balances = value.getBalances();
        assertEquals(1, balances.size());

        AccountReference balanceReference = balances.get(0);
        assertEquals(MASKED_PAN_1, balanceReference.getMaskedPan());
        assertEquals(RESOURCE_ID_1, balanceReference.getResourceId());

        List<AccountReference> transactions = value.getTransactions();
        assertEquals(1, transactions.size());

        AccountReference transactionReference = transactions.get(0);
        assertEquals(MASKED_PAN_1, transactionReference.getMaskedPan());
        assertEquals(RESOURCE_ID_1, transactionReference.getResourceId());

    }

    @Test
    void updateCardAccountReferences_shouldSetResourceId() {
        // Given
        ArgumentCaptor<AccountAccess> xs2aAccountAccessArgumentCaptor = ArgumentCaptor.forClass(AccountAccess.class);

        AccountReference accountReferenceWithMaskedPan = buildAccountReferenceWithMaskedPan(MASKED_PAN_1);
        AccountAccess accountAccess = buildXs2aAccountAccess(Collections.singletonList(accountReferenceWithMaskedPan),
                                                             Collections.singletonList(accountReferenceWithMaskedPan),
                                                             Collections.singletonList(accountReferenceWithMaskedPan));
        AisConsent aisConsent = buildAisConsent(accountAccess, null);
        Xs2aCardAccountDetails xs2aCardAccountDetails = buildXs2aCardAccountDetails(ASPSP_ACCOUNT_ID_1, RESOURCE_ID_1, MASKED_PAN_1);
        when(cardAccountHandler.areAccountsEqual(xs2aCardAccountDetails, accountReferenceWithMaskedPan)).thenReturn(true);

        // When
        accountReferenceInConsentUpdater.updateCardAccountReferences(CONSENT_ID, aisConsent, Collections.singletonList(xs2aCardAccountDetails));

        // Then
        verify(aisConsentService).updateAspspAccountAccess(eq(CONSENT_ID), xs2aAccountAccessArgumentCaptor.capture());
        AccountAccess value = xs2aAccountAccessArgumentCaptor.getValue();

        List<AccountReference> accounts = value.getAccounts();
        assertEquals(1, accounts.size());

        AccountReference accountReference = accounts.get(0);
        assertEquals(MASKED_PAN_1, accountReference.getMaskedPan());
        assertEquals(RESOURCE_ID_1, accountReference.getResourceId());

        List<AccountReference> balances = value.getBalances();
        assertEquals(1, balances.size());

        AccountReference balanceReference = balances.get(0);
        assertEquals(MASKED_PAN_1, balanceReference.getMaskedPan());
        assertEquals(RESOURCE_ID_1, balanceReference.getResourceId());

        List<AccountReference> transactions = value.getTransactions();
        assertEquals(1, transactions.size());

        AccountReference transactionReference = transactions.get(0);
        assertEquals(MASKED_PAN_1, transactionReference.getMaskedPan());
        assertEquals(RESOURCE_ID_1, transactionReference.getResourceId());
    }

    @Test
    void updateCardAccountReferences_withMultipleAccountReferences_shouldProperlyUpdateResourceId() {
        ArgumentCaptor<AccountAccess> xs2aAccountAccessArgumentCaptor = ArgumentCaptor.forClass(AccountAccess.class);

        // Given
        AccountReference firstAccountReference = buildAccountReferenceWithMaskedPan(MASKED_PAN_1);
        AccountReference secondAccountReference = buildAccountReferenceWithMaskedPan(MASKED_PAN_2);
        AccountAccess accountAccess = buildXs2aAccountAccess(Arrays.asList(firstAccountReference,
                                                                           secondAccountReference),
                                                             Collections.emptyList(),
                                                             Collections.singletonList(firstAccountReference));
        AisConsent aisConsent = buildAisConsent(accountAccess, null);
        Xs2aCardAccountDetails firstXs2aCardAccountDetails = buildXs2aCardAccountDetails(ASPSP_ACCOUNT_ID_1, RESOURCE_ID_1, MASKED_PAN_1);
        Xs2aCardAccountDetails secondXs2aCardAccountDetails = buildXs2aCardAccountDetails(ASPSP_ACCOUNT_ID_2, RESOURCE_ID_2, MASKED_PAN_2);
        when(cardAccountHandler.areAccountsEqual(firstXs2aCardAccountDetails, firstAccountReference)).thenReturn(true);
        when(cardAccountHandler.areAccountsEqual(firstXs2aCardAccountDetails, secondAccountReference)).thenReturn(false);
        when(cardAccountHandler.areAccountsEqual(secondXs2aCardAccountDetails, firstAccountReference)).thenReturn(false);
        when(cardAccountHandler.areAccountsEqual(secondXs2aCardAccountDetails, secondAccountReference)).thenReturn(true);

        // When
        accountReferenceInConsentUpdater.updateCardAccountReferences(CONSENT_ID, aisConsent, Arrays.asList(firstXs2aCardAccountDetails, secondXs2aCardAccountDetails));

        // Then
        verify(aisConsentService).updateAspspAccountAccess(eq(CONSENT_ID), xs2aAccountAccessArgumentCaptor.capture());
        AccountAccess value = xs2aAccountAccessArgumentCaptor.getValue();

        List<AccountReference> accounts = value.getAccounts();
        assertEquals(2, accounts.size());

        AccountReference firstMappedAccountReference = accounts.get(0);
        assertEquals(MASKED_PAN_1, firstMappedAccountReference.getMaskedPan());
        assertEquals(RESOURCE_ID_1, firstMappedAccountReference.getResourceId());

        AccountReference secondMappedAccountReference = accounts.get(1);
        assertEquals(MASKED_PAN_2, secondMappedAccountReference.getMaskedPan());
        assertEquals(RESOURCE_ID_2, secondMappedAccountReference.getResourceId());

        List<AccountReference> balances = value.getBalances();
        assertTrue(balances.isEmpty());

        List<AccountReference> transactions = value.getTransactions();
        assertEquals(1, transactions.size());

        AccountReference transactionReference = transactions.get(0);
        assertEquals(MASKED_PAN_1, transactionReference.getMaskedPan());
        assertEquals(RESOURCE_ID_1, transactionReference.getResourceId());

    }

    @Test
    void updateCardAccountReferences_withResourceIdInAccounts_shouldNotUpdateBalancesOrTransactions() {
        ArgumentCaptor<AccountAccess> xs2aAccountAccessArgumentCaptor = ArgumentCaptor.forClass(AccountAccess.class);

        // Given
        AccountReference accountReferenceWithMaskedPan = buildAccountReferenceWithMaskedPan(MASKED_PAN_1);
        AccountAccess accountAccess = buildXs2aAccountAccess(Collections.singletonList(accountReferenceWithMaskedPan),
                                                             Collections.emptyList(),
                                                             Collections.emptyList());
        AisConsent aisConsent = buildAisConsent(accountAccess, null);
        Xs2aCardAccountDetails xs2aCardAccountDetails = buildXs2aCardAccountDetails(ASPSP_ACCOUNT_ID_1, RESOURCE_ID_1, MASKED_PAN_1);
        when(cardAccountHandler.areAccountsEqual(xs2aCardAccountDetails, accountReferenceWithMaskedPan)).thenReturn(true);

        // When
        accountReferenceInConsentUpdater.updateCardAccountReferences(CONSENT_ID, aisConsent, Collections.singletonList(xs2aCardAccountDetails));

        // Then
        verify(aisConsentService).updateAspspAccountAccess(eq(CONSENT_ID), xs2aAccountAccessArgumentCaptor.capture());
        AccountAccess value = xs2aAccountAccessArgumentCaptor.getValue();

        List<AccountReference> accounts = value.getAccounts();
        assertEquals(1, accounts.size());

        AccountReference accountReference = accounts.get(0);
        assertEquals(MASKED_PAN_1, accountReference.getMaskedPan());
        assertEquals(RESOURCE_ID_1, accountReference.getResourceId());

        List<AccountReference> balances = value.getBalances();
        assertTrue(balances.isEmpty());

        List<AccountReference> transactions = value.getTransactions();
        assertTrue(transactions.isEmpty());
    }

    @Test
    void rewriteAccountAccess_AIS() {
        //Given
        AccountAccess accountAccess = buildXs2aAccountAccess(Collections.emptyList(),
                                                             Collections.emptyList(),
                                                             Collections.emptyList());
        //When
        accountReferenceInConsentUpdater.rewriteAccountAccess(CONSENT_ID, accountAccess, ConsentType.AIS);
        //Then
        verify(aisConsentService, atLeastOnce()).updateAspspAccountAccess(CONSENT_ID, accountAccess);
        verify(xs2aPiisConsentService, never()).updateAspspAccountAccess(CONSENT_ID, accountAccess);
    }

    @Test
    void rewriteAccountAccess_PIIS() {
        //Given
        AccountAccess accountAccess = buildXs2aAccountAccess(Collections.emptyList(),
                                                             Collections.emptyList(),
                                                             Collections.emptyList());
        //When
        accountReferenceInConsentUpdater.rewriteAccountAccess(CONSENT_ID, accountAccess, ConsentType.PIIS_TPP);
        //Then
        verify(xs2aPiisConsentService, atLeastOnce()).updateAspspAccountAccess(CONSENT_ID, accountAccess);
        verify(aisConsentService, never()).updateAspspAccountAccess(CONSENT_ID, accountAccess);
    }

    private AisConsent buildAisConsent(AccountAccess accountAccess, AccountAccessType globalAccessType) {
        AisConsent aisConsent = new AisConsent();
        aisConsent.setTppAccountAccesses(accountAccess);
        aisConsent.setAspspAccountAccesses(AccountAccess.EMPTY_ACCESS);
        aisConsent.setConsentData(new AisConsentData(globalAccessType, globalAccessType, globalAccessType, false));
        return aisConsent;
    }

    private AccountAccess buildXs2aAccountAccess(List<AccountReference> accounts,
                                                 List<AccountReference> balances,
                                                 List<AccountReference> transactions) {
        return new AccountAccess(accounts, balances, transactions, null);
    }

    private Xs2aAccountDetails buildXs2aAccountDetails(String aspspAccountId, String resourceId, String iban) {
        return new Xs2aAccountDetails(aspspAccountId, resourceId, iban, null, null, null, null,
                                      CURRENCY, null, null, null, null,
                                      null, null, null, null,
                                      null, Collections.emptyList(), null, null);
    }

    private Xs2aCardAccountDetails buildXs2aCardAccountDetails(String aspspAccountId, String resourceId, String maskedPan) {
        return new Xs2aCardAccountDetails(aspspAccountId, resourceId, maskedPan,
                                          CURRENCY, null, null, null, null,
                                          null, null, null, Collections.emptyList(),
                                          null, null, null);
    }

    private AccountReference buildAccountReferenceWithIban(String iban) {
        return new AccountReference(null, null, iban, null, null, null, null, CURRENCY, null);
    }

    private AccountReference buildAccountReferenceWithMaskedPan(String maskedPan) {
        return new AccountReference(null, null, null, null, null, maskedPan, null, CURRENCY, null);
    }
}
