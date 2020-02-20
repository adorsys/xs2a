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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.api.AccountInfo;
import de.adorsys.psd2.consent.api.TypeAccess;
import de.adorsys.psd2.consent.api.ais.AccountAdditionalInformationAccess;
import de.adorsys.psd2.consent.api.ais.AdditionalAccountInformationType;
import de.adorsys.psd2.consent.api.ais.AisAccountAccessInfo;
import de.adorsys.psd2.consent.domain.account.AspspAccountAccess;
import de.adorsys.psd2.consent.domain.account.TppAccountAccess;
import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AdditionalInformationAccess;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class AccessMapper {

    public AccountAccess mapTppAccessesToAccountAccess(List<TppAccountAccess> tppAccountAccesses, AdditionalAccountInformationType additionalAccountInformationType) {
        AccountAccessListHolder holder = new AccountAccessListHolder();
        tppAccountAccesses.forEach(a -> {
            AccountReference accountReference = new AccountReference(a.getAccountReferenceType(),
                                                                     a.getAccountIdentifier(),
                                                                     a.getCurrency());
            holder.addAccountReference(accountReference, a.getTypeAccess());
        });
        return new AccountAccess(holder.getAccounts(), holder.getBalances(), holder.getTransactions(),
                                 new AdditionalInformationAccess(additionalAccountInformationType.getReferencesByType(holder.getOwnerNames())));
    }

    public AccountAccess mapAspspAccessesToAccountAccess(List<AspspAccountAccess> aspspAccountAccesses, AdditionalAccountInformationType additionalAccountInformationType) {
        AccountAccessListHolder holder = new AccountAccessListHolder();
        aspspAccountAccesses.forEach(a -> {
            AccountReference accountReference = new AccountReference(a.getAccountReferenceType(),
                                                                     a.getAccountIdentifier(),
                                                                     a.getCurrency(),
                                                                     a.getResourceId(),
                                                                     a.getAspspAccountId());
            holder.addAccountReference(accountReference, a.getTypeAccess());
        });
        return new AccountAccess(holder.getAccounts(), holder.getBalances(), holder.getTransactions(),
                                 new AdditionalInformationAccess(additionalAccountInformationType.getReferencesByType(holder.getOwnerNames())));
    }

    public List<TppAccountAccess> mapToTppAccountAccess(AccountAccess accountAccess) {
        List<TppAccountAccess> tppAccountAccesses = new ArrayList<>();
        tppAccountAccesses.addAll(accountAccess.getAccounts().stream().map(a -> new TppAccountAccess(a.getUsedAccountReferenceSelector().getAccountValue(),
                                                                                                     TypeAccess.ACCOUNT,
                                                                                                     a.getUsedAccountReferenceSelector().getAccountReferenceType(),
                                                                                                     a.getCurrency())).collect(Collectors.toList()));
        tppAccountAccesses.addAll(accountAccess.getBalances().stream().map(a -> new TppAccountAccess(a.getUsedAccountReferenceSelector().getAccountValue(),
                                                                                                     TypeAccess.BALANCE,
                                                                                                     a.getUsedAccountReferenceSelector().getAccountReferenceType(),
                                                                                                     a.getCurrency())).collect(Collectors.toList()));
        tppAccountAccesses.addAll(accountAccess.getTransactions().stream().map(a -> new TppAccountAccess(a.getUsedAccountReferenceSelector().getAccountValue(),
                                                                                                         TypeAccess.TRANSACTION,
                                                                                                         a.getUsedAccountReferenceSelector().getAccountReferenceType(),
                                                                                                         a.getCurrency())).collect(Collectors.toList()));
        AdditionalInformationAccess additionalInformationAccess = accountAccess.getAdditionalInformationAccess();
        if (additionalInformationAccess != null && CollectionUtils.isNotEmpty(additionalInformationAccess.getOwnerName())) {
            tppAccountAccesses.addAll(additionalInformationAccess.getOwnerName().stream().map(a -> new TppAccountAccess(a.getUsedAccountReferenceSelector().getAccountValue(),
                                                                                                                        TypeAccess.OWNER_NAME,
                                                                                                                        a.getUsedAccountReferenceSelector().getAccountReferenceType(),
                                                                                                                        a.getCurrency())).collect(Collectors.toList()));
        }
        return tppAccountAccesses;
    }

    public List<AspspAccountAccess> mapToAspspAccountAccess(AccountAccess accountAccess) {
        List<AspspAccountAccess> aspspAccountAccesses = new ArrayList<>();
        aspspAccountAccesses.addAll(accountAccess.getAccounts().stream().map(a -> new AspspAccountAccess(a.getUsedAccountReferenceSelector().getAccountValue(),
                                                                                                         TypeAccess.ACCOUNT,
                                                                                                         a.getUsedAccountReferenceSelector().getAccountReferenceType(),
                                                                                                         a.getCurrency(),
                                                                                                         a.getResourceId(),
                                                                                                         a.getAspspAccountId())).collect(Collectors.toList()));
        aspspAccountAccesses.addAll(accountAccess.getBalances().stream().map(a -> new AspspAccountAccess(a.getUsedAccountReferenceSelector().getAccountValue(),
                                                                                                         TypeAccess.BALANCE,
                                                                                                         a.getUsedAccountReferenceSelector().getAccountReferenceType(),
                                                                                                         a.getCurrency(),
                                                                                                         a.getResourceId(),
                                                                                                         a.getAspspAccountId())).collect(Collectors.toList()));
        aspspAccountAccesses.addAll(accountAccess.getTransactions().stream().map(a -> new AspspAccountAccess(a.getUsedAccountReferenceSelector().getAccountValue(),
                                                                                                             TypeAccess.TRANSACTION,
                                                                                                             a.getUsedAccountReferenceSelector().getAccountReferenceType(),
                                                                                                             a.getCurrency(),
                                                                                                             a.getResourceId(),
                                                                                                             a.getAspspAccountId())).collect(Collectors.toList()));
        AdditionalInformationAccess additionalInformationAccess = accountAccess.getAdditionalInformationAccess();
        if (additionalInformationAccess != null && CollectionUtils.isNotEmpty(additionalInformationAccess.getOwnerName())) {
            aspspAccountAccesses.addAll(additionalInformationAccess.getOwnerName().stream().map(a -> new AspspAccountAccess(a.getUsedAccountReferenceSelector().getAccountValue(),
                                                                                                                            TypeAccess.OWNER_NAME,
                                                                                                                            a.getUsedAccountReferenceSelector().getAccountReferenceType(),
                                                                                                                            a.getCurrency(),
                                                                                                                            a.getResourceId(),
                                                                                                                            a.getAspspAccountId())).collect(Collectors.toList()));
        }
        return aspspAccountAccesses;
    }

    public AccountAccess mapToAccountAccess(AisAccountAccessInfo aisAccountAccessInfo) {
        Set<AccountReference> accounts = mapToAccountReferences(aisAccountAccessInfo.getAccounts());
        Set<AccountReference> balances = mapToAccountReferences(aisAccountAccessInfo.getBalances());
        Set<AccountReference> transactions = mapToAccountReferences(aisAccountAccessInfo.getTransactions());

        Set<AccountReference> allAccounts = addReferencesToAccounts(accounts, balances, transactions);
        return new AccountAccess(new ArrayList<>(allAccounts),
                                 new ArrayList<>(balances),
                                 new ArrayList<>(transactions),
                                 mapToAdditionalInformationAccess(aisAccountAccessInfo.getAccountAdditionalInformationAccess()));
    }

    private AdditionalInformationAccess mapToAdditionalInformationAccess(AccountAdditionalInformationAccess accountAdditionalInformationAccess) {
        if (accountAdditionalInformationAccess == null) {
            return null;
        }

        List<AccountInfo> ownerNameAccountInfo = accountAdditionalInformationAccess.getOwnerName();
        if (ownerNameAccountInfo == null) {
            return new AdditionalInformationAccess(null);
        }

        List<AccountReference> ownerNameAccountReferences = ownerNameAccountInfo.stream().map(this::mapToAccountReference).collect(Collectors.toList());
        return new AdditionalInformationAccess(ownerNameAccountReferences);
    }

    private Set<AccountReference> mapToAccountReferences(@NotNull List<AccountInfo> accountInfoList) {
        return accountInfoList.stream()
                   .map(this::mapToAccountReference)
                   .collect(Collectors.toSet());
    }

    private AccountReference mapToAccountReference(AccountInfo accountInfo) {
        return new AccountReference(accountInfo.getAccountType(),
                                    accountInfo.getAccountIdentifier(),
                                    getCurrencyByString(accountInfo.getCurrency()),
                                    accountInfo.getResourceId(),
                                    accountInfo.getAspspAccountId());
    }

    private Currency getCurrencyByString(String currency) {
        return Optional.ofNullable(currency)
                   .map(Currency::getInstance)
                   .orElse(null);
    }

    private Set<AccountReference> addReferencesToAccounts(Set<AccountReference> accounts,
                                                          Set<AccountReference> balances,
                                                          Set<AccountReference> transactions) {
        return Stream.of(accounts, balances, transactions)
                   .flatMap(Collection::stream)
                   .collect(Collectors.toSet());
    }

    @Getter
    private static class AccountAccessListHolder {
        List<AccountReference> accounts = new ArrayList<>();
        List<AccountReference> balances = new ArrayList<>();
        List<AccountReference> transactions = new ArrayList<>();
        List<AccountReference> ownerNames = new ArrayList<>();

        void addAccountReference(AccountReference accountReference, TypeAccess typeAccess) {
            if (TypeAccess.ACCOUNT == typeAccess) {
                accounts.add(accountReference);
            } else if (TypeAccess.BALANCE == typeAccess) {
                balances.add(accountReference);
            } else if (TypeAccess.TRANSACTION == typeAccess) {
                transactions.add(accountReference);
            } else if (TypeAccess.OWNER_NAME == typeAccess) {
                ownerNames.add(accountReference);
            }
        }
    }
}
