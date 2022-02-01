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

package de.adorsys.psd2.consent.service.account;

import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AdditionalInformationAccess;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AccountAccessUpdater {
    @NotNull
    public AccountAccess updateAccountReferencesInAccess(@NotNull AccountAccess existingAccess,
                                                         @NotNull AccountAccess newAccess) {
        if (hasNoAccountReferences(existingAccess)) {
            return new AccountAccess(newAccess.getAccounts(), newAccess.getBalances(), newAccess.getTransactions(), newAccess.getAdditionalInformationAccess());
        }

        List<AccountReference> updatedAccounts = existingAccess.getAccounts().stream()
                                                     .map(ref -> updateAccountReference(ref, newAccess.getAccounts()))
                                                     .collect(Collectors.toList());
        List<AccountReference> updatedBalances = existingAccess.getBalances().stream()
                                                     .map(ref -> updateAccountReference(ref, newAccess.getBalances()))
                                                     .collect(Collectors.toList());
        List<AccountReference> updatedTransactions = existingAccess.getTransactions().stream()
                                                         .map(ref -> updateAccountReference(ref, newAccess.getTransactions()))
                                                         .collect(Collectors.toList());

        AdditionalInformationAccess updatedAdditionalInformation = updateAccountReferencesInAdditionalInformation(existingAccess.getAdditionalInformationAccess(),
                                                                                                                  newAccess.getAdditionalInformationAccess());

        return new AccountAccess(updatedAccounts, updatedBalances, updatedTransactions, updatedAdditionalInformation);
    }

    private boolean hasNoAccountReferences(AccountAccess accountAccess) {
        AdditionalInformationAccess additionalInformationAccess = accountAccess.getAdditionalInformationAccess();
        boolean hasNoAdditionalInformationReferences = additionalInformationAccess == null
                                                           || CollectionUtils.isEmpty(additionalInformationAccess.getOwnerName());

        return CollectionUtils.isEmpty(accountAccess.getAccounts())
                   && CollectionUtils.isEmpty(accountAccess.getBalances())
                   && CollectionUtils.isEmpty(accountAccess.getTransactions())
                   && hasNoAdditionalInformationReferences;
    }

    private AdditionalInformationAccess updateAccountReferencesInAdditionalInformation(AdditionalInformationAccess existingAccess,
                                                                                       AdditionalInformationAccess requestedAccess) {
        if (isAdditionalInformationAbsent(existingAccess) || isAdditionalInformationAbsent(requestedAccess)) {
            return existingAccess;
        }
        return new AdditionalInformationAccess(getAccountReferences(existingAccess.getOwnerName(), requestedAccess.getOwnerName()),
                                               getAccountReferences(existingAccess.getTrustedBeneficiaries(), requestedAccess.getTrustedBeneficiaries()));
    }

    private List<AccountReference> getAccountReferences(List<AccountReference> existing, List<AccountReference> requested){
        if (existing != null && requested != null) {
            return existing.stream()
                       .map(ref -> updateAccountReference(ref, requested))
                       .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private boolean isAdditionalInformationAbsent(AdditionalInformationAccess additionalInformationAccess) {
        return additionalInformationAccess == null || isAdditionalInformationEmpty(additionalInformationAccess);
    }

    private boolean isAdditionalInformationEmpty(AdditionalInformationAccess additionalInformationAccess) {
        return isOwnerNameAbsent(additionalInformationAccess) && isTrustedBeneficiariesAbsent(additionalInformationAccess);
    }

    private boolean isOwnerNameAbsent(AdditionalInformationAccess additionalInformationAccess) {
        return additionalInformationAccess.getOwnerName() == null;
    }

    private boolean isTrustedBeneficiariesAbsent(AdditionalInformationAccess additionalInformationAccess) {
        return additionalInformationAccess.getTrustedBeneficiaries() == null;
    }

    private AccountReference updateAccountReference(AccountReference existingReference, List<AccountReference> requestedAspspReferences) {
        AccountReference reference = requestedAspspReferences.stream()
                   .filter(aspsp -> aspsp.getUsedAccountReferenceSelector().equals(existingReference.getUsedAccountReferenceSelector()))
                   .filter(aspsp -> Objects.equals(aspsp.getCurrency(), existingReference.getCurrency()))
                   .findFirst()
                   .orElse(existingReference);
        reference.setId(existingReference.getId());
        return reference;
    }
}
