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

import de.adorsys.psd2.consent.api.TypeAccess;
import de.adorsys.psd2.consent.api.ais.AdditionalAccountInformationType;
import de.adorsys.psd2.consent.domain.account.AspspAccountAccess;
import de.adorsys.psd2.consent.domain.account.TppAccountAccess;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AdditionalInformationAccess;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AccessMapper {

    public AccountAccess mapTppAccessesToAccountAccess(List<TppAccountAccess> tppAccountAccesses,
                                                       AdditionalAccountInformationType ownerNameType,
                                                       AdditionalAccountInformationType trustedBeneficiariesType) {
        AccountAccessListHolder holder = new AccountAccessListHolder();
        tppAccountAccesses.forEach(a -> {
            AccountReference accountReference = new AccountReference(a.getAccountReferenceType(),
                                                                     a.getAccountIdentifier(),
                                                                     a.getCurrency());
            holder.addAccountReference(accountReference, a.getTypeAccess());
        });
        return buildAccountAccess(holder, ownerNameType, trustedBeneficiariesType);
    }

    public AccountAccess mapAspspAccessesToAccountAccess(List<AspspAccountAccess> aspspAccountAccesses,
                                                         AdditionalAccountInformationType ownerNameType,
                                                         AdditionalAccountInformationType trustedBeneficiariesType) {
        AccountAccessListHolder holder = new AccountAccessListHolder();
        aspspAccountAccesses.forEach(a -> {
            AccountReference accountReference = new AccountReference(a.getId(),
                                                                     a.getAccountReferenceType(),
                                                                     a.getAccountIdentifier(),
                                                                     a.getCurrency(),
                                                                     a.getResourceId(),
                                                                     a.getAspspAccountId());
            holder.addAccountReference(accountReference, a.getTypeAccess());
        });
        return buildAccountAccess(holder, ownerNameType, trustedBeneficiariesType);
    }

    public List<TppAccountAccess> mapToTppAccountAccess(ConsentEntity consent, AccountAccess accountAccess) {
        List<TppAccountAccess> tppAccountAccesses = new ArrayList<>();
        tppAccountAccesses.addAll(accountAccess.getAccounts().stream().map(a -> new TppAccountAccess(a.getId(),
                                                                                                     consent,a.getUsedAccountReferenceSelector().getAccountValue(),
                                                                                                     TypeAccess.ACCOUNT,
                                                                                                     a.getUsedAccountReferenceSelector().getAccountReferenceType(),
                                                                                                     a.getCurrency())).collect(Collectors.toList()));
        tppAccountAccesses.addAll(accountAccess.getBalances().stream().map(a -> new TppAccountAccess(a.getId(),
                                                                                                     consent,
                                                                                                     a.getUsedAccountReferenceSelector().getAccountValue(),
                                                                                                     TypeAccess.BALANCE,
                                                                                                     a.getUsedAccountReferenceSelector().getAccountReferenceType(),
                                                                                                     a.getCurrency())).collect(Collectors.toList()));
        tppAccountAccesses.addAll(accountAccess.getTransactions().stream().map(a -> new TppAccountAccess(a.getId(),
                                                                                                         consent,
                                                                                                         a.getUsedAccountReferenceSelector().getAccountValue(),
                                                                                                         TypeAccess.TRANSACTION,
                                                                                                         a.getUsedAccountReferenceSelector().getAccountReferenceType(),
                                                                                                         a.getCurrency())).collect(Collectors.toList()));
        AdditionalInformationAccess additionalInformationAccess = accountAccess.getAdditionalInformationAccess();
        if (additionalInformationAccess != null) {
            if (CollectionUtils.isNotEmpty(additionalInformationAccess.getOwnerName())) {
                tppAccountAccesses.addAll(additionalInformationAccess.getOwnerName().stream().map(a -> new TppAccountAccess(a.getId(), consent, a.getUsedAccountReferenceSelector().getAccountValue(),
                                                                                                                            TypeAccess.OWNER_NAME,
                                                                                                                            a.getUsedAccountReferenceSelector().getAccountReferenceType(),
                                                                                                                            a.getCurrency())).collect(Collectors.toList()));
            }
            if (CollectionUtils.isNotEmpty(additionalInformationAccess.getTrustedBeneficiaries())) {
                tppAccountAccesses.addAll(additionalInformationAccess.getTrustedBeneficiaries().stream().map(a -> new TppAccountAccess(a.getId(), consent, a.getUsedAccountReferenceSelector().getAccountValue(),
                                                                                                                                       TypeAccess.BENEFICIARIES,
                                                                                                                                       a.getUsedAccountReferenceSelector().getAccountReferenceType(),
                                                                                                                                       a.getCurrency())).collect(Collectors.toList()));
            }
        }

        return tppAccountAccesses;
    }

    public List<AspspAccountAccess> mapToAspspAccountAccess(ConsentEntity consent, AccountAccess accountAccess) {
        List<AspspAccountAccess> aspspAccountAccesses = new ArrayList<>();
        aspspAccountAccesses.addAll(accountAccess.getAccounts().stream().map(a -> new AspspAccountAccess(
            a.getId(), consent, a.getUsedAccountReferenceSelector().getAccountValue(),
                                                                                                         TypeAccess.ACCOUNT,
                                                                                                         a.getUsedAccountReferenceSelector().getAccountReferenceType(),
                                                                                                         a.getCurrency(),
                                                                                                         a.getResourceId(),
                                                                                                         a.getAspspAccountId())).collect(Collectors.toList()));
        aspspAccountAccesses.addAll(accountAccess.getBalances().stream().map(a -> new AspspAccountAccess(a.getId(), consent, a.getUsedAccountReferenceSelector().getAccountValue(),
                                                                                                         TypeAccess.BALANCE,
                                                                                                         a.getUsedAccountReferenceSelector().getAccountReferenceType(),
                                                                                                         a.getCurrency(),
                                                                                                         a.getResourceId(),
                                                                                                         a.getAspspAccountId())).collect(Collectors.toList()));
        aspspAccountAccesses.addAll(accountAccess.getTransactions().stream().map(a -> new AspspAccountAccess(a.getId(), consent, a.getUsedAccountReferenceSelector().getAccountValue(),
                                                                                                             TypeAccess.TRANSACTION,
                                                                                                             a.getUsedAccountReferenceSelector().getAccountReferenceType(),
                                                                                                             a.getCurrency(),
                                                                                                             a.getResourceId(),
                                                                                                             a.getAspspAccountId())).collect(Collectors.toList()));
        AdditionalInformationAccess additionalInformationAccess = accountAccess.getAdditionalInformationAccess();
        if (additionalInformationAccess != null) {
            if (CollectionUtils.isNotEmpty(additionalInformationAccess.getOwnerName())) {
                aspspAccountAccesses.addAll(additionalInformationAccess.getOwnerName().stream().map(a -> new AspspAccountAccess(a.getId(), consent, a.getUsedAccountReferenceSelector().getAccountValue(),
                                                                                                                                TypeAccess.OWNER_NAME,
                                                                                                                                a.getUsedAccountReferenceSelector().getAccountReferenceType(),
                                                                                                                                a.getCurrency(),
                                                                                                                                a.getResourceId(),
                                                                                                                                a.getAspspAccountId())).collect(Collectors.toList()));
            }
            if (CollectionUtils.isNotEmpty(additionalInformationAccess.getTrustedBeneficiaries())) {
                aspspAccountAccesses.addAll(additionalInformationAccess.getTrustedBeneficiaries().stream().map(a -> new AspspAccountAccess(a.getId(), consent, a.getUsedAccountReferenceSelector().getAccountValue(),
                                                                                                                                           TypeAccess.BENEFICIARIES,
                                                                                                                                           a.getUsedAccountReferenceSelector().getAccountReferenceType(),
                                                                                                                                           a.getCurrency(),
                                                                                                                                           a.getResourceId(),
                                                                                                                                           a.getAspspAccountId())).collect(Collectors.toList()));
            }
        }

        return aspspAccountAccesses;
    }

    public AspspAccountAccess mapToAspspAccountAccess(ConsentEntity consent, AccountReference accountReference) {
        return new AspspAccountAccess(consent, accountReference.getUsedAccountReferenceSelector().getAccountValue(),
                                      TypeAccess.ACCOUNT,
                                      accountReference.getUsedAccountReferenceSelector().getAccountReferenceType(),
                                      accountReference.getCurrency(),
                                      accountReference.getResourceId(),
                                      accountReference.getAspspAccountId());
    }

    public AccountReference mapToAccountReference(AspspAccountAccess aspspAccountAccess) {
        return new AccountReference(aspspAccountAccess.getAccountReferenceType(),
                                    aspspAccountAccess.getAccountIdentifier(),
                                    aspspAccountAccess.getCurrency(),
                                    aspspAccountAccess.getResourceId(),
                                    aspspAccountAccess.getAspspAccountId());
    }

    @Getter
    private static class AccountAccessListHolder {
        List<AccountReference> accounts = new ArrayList<>();
        List<AccountReference> balances = new ArrayList<>();
        List<AccountReference> transactions = new ArrayList<>();
        List<AccountReference> ownerNames = new ArrayList<>();
        List<AccountReference> trustedBeneficiaries = new ArrayList<>();

        void addAccountReference(AccountReference accountReference, TypeAccess typeAccess) {
            if (TypeAccess.ACCOUNT == typeAccess) {
                accounts.add(accountReference);
            } else if (TypeAccess.BALANCE == typeAccess) {
                balances.add(accountReference);
            } else if (TypeAccess.TRANSACTION == typeAccess) {
                transactions.add(accountReference);
            } else if (TypeAccess.OWNER_NAME == typeAccess) {
                ownerNames.add(accountReference);
            } else if (TypeAccess.BENEFICIARIES == typeAccess) {
                trustedBeneficiaries.add(accountReference);
            }
        }
    }

    private AccountAccess buildAccountAccess(AccountAccessListHolder holder,
                                             AdditionalAccountInformationType ownerNameType,
                                             AdditionalAccountInformationType trustedBeneficiariesType) {

        return new AccountAccess(holder.getAccounts(), holder.getBalances(), holder.getTransactions(),
                                 new AdditionalInformationAccess(resolveAdditionalAccountInformationType(ownerNameType).getReferencesByType(holder.getOwnerNames()),
                                                                 resolveAdditionalAccountInformationType(trustedBeneficiariesType).getReferencesByType(holder.getTrustedBeneficiaries())));
    }

    private AdditionalAccountInformationType resolveAdditionalAccountInformationType(AdditionalAccountInformationType additionalAccountInformationType) {
        return additionalAccountInformationType == null ? AdditionalAccountInformationType.NONE : additionalAccountInformationType;
    }
}
