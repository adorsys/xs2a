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

package de.adorsys.psd2.xs2a.service.consent;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import de.adorsys.psd2.xs2a.core.profile.AdditionalInformationAccess;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountDetails;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCardAccountDetails;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This service is designed to update AccountReferences in AisConsent. Also for bank offered consent and global consent
 * AccountReferences needed to be saved in AisConsent in order to be used in next GET calls.
 */
@Service
@RequiredArgsConstructor
public class AccountReferenceInConsentUpdater {
    private final Xs2aAisConsentService aisConsentService;
    private final Xs2aAisConsentMapper consentMapper;
    private final CardAccountHandler cardAccountHandler;

    /**
     * Overwrites existing account access with the new one. To be used with caution.
     * Only allowed when you get new AccountAccess from ASPSP side,
     * NOT ALLOWED to update consent by smth coming from TPP side!
     *
     * @param consentId        an external ID of consent, where account access to be stored
     * @param newAccountAccess new object with account accesses
     */
    public void rewriteAccountAccess(@NotNull String consentId, @NotNull AccountAccess newAccountAccess) {
        aisConsentService.updateAspspAccountAccess(consentId, newAccountAccess);
    }

    /**
     * Updates account resourceIDs in existing account access. If account is not found in consent's account access,
     * will be ignored.
     *
     * @param consentId      an external ID of consent, where account access to be stored
     * @param aisConsent     consent for which references are being updated
     * @param accountDetails list of account details with referenceId set
     * @return Response containing AIS Consent
     */
    public CmsResponse<AisConsent> updateAccountReferences(@NotNull String consentId, @NotNull AisConsent aisConsent, @NotNull List<Xs2aAccountDetails> accountDetails) {
        List<AccountReference> accounts = new ArrayList<>();
        List<AccountReference> transactions = new ArrayList<>();
        List<AccountReference> balances = new ArrayList<>();
        List<AccountReference> ownerName = new ArrayList<>();
        AccountAccess existingAccess = aisConsent.getAccess();
        AdditionalInformationAccess additionalInformationAccess = existingAccess.getAdditionalInformationAccess();

        if (aisConsent.getConsentData().getAllPsd2() == AccountAccessType.ALL_ACCOUNTS) {
            accounts.addAll(enrichAccountReferencesGlobal(accountDetails));
            transactions.addAll(enrichAccountReferencesGlobal(accountDetails));
            balances.addAll(enrichAccountReferencesGlobal(accountDetails));
        } else {
            for (Xs2aAccountDetails accountDetail : accountDetails) {
                accounts.addAll(enrichAccountReferences(accountDetail, existingAccess.getAccounts()));
                balances.addAll(enrichAccountReferences(accountDetail, existingAccess.getBalances()));
                transactions.addAll(enrichAccountReferences(accountDetail, existingAccess.getTransactions()));
            }
        }

        for (Xs2aAccountDetails accountDetail : accountDetails) {
            if (additionalInformationAccess != null && additionalInformationAccess.getOwnerName() != null) {
                ownerName.addAll(enrichAccountReferences(accountDetail, additionalInformationAccess.getOwnerName()));
            }
        }

        AccountAccess accountAccess =
            getXs2aAccountAccess(accounts, transactions, balances, ownerName, additionalInformationAccess);

        return aisConsentService.updateAspspAccountAccess(consentId, accountAccess);
    }

    /**
     * Updates card account resource IDs in existing account access. If account is not found in consent's account access,
     * will be ignored. Masked PAN and PAN corresponding is implemented here.
     *
     * @param consentId      an external ID of consent, where account access to be stored
     * @param aisConsent     consent for which references are being updated
     * @param accountDetails list of account details with referenceId set
     * @return Response containing AIS Consent
     */
    public CmsResponse<AisConsent> updateCardAccountReferences(String consentId, AisConsent aisConsent, List<Xs2aCardAccountDetails> accountDetails) {
        List<AccountReference> accounts = new ArrayList<>();
        List<AccountReference> transactions = new ArrayList<>();
        List<AccountReference> balances = new ArrayList<>();
        List<AccountReference> ownerName = new ArrayList<>();
        AccountAccess existingAccess = aisConsent.getAccess();
        AdditionalInformationAccess additionalInformationAccess = existingAccess.getAdditionalInformationAccess();

        if (aisConsent.getConsentData().getAllPsd2() == AccountAccessType.ALL_ACCOUNTS) {
            accounts.addAll(enrichCardAccountReferencesGlobal(accountDetails));
            transactions.addAll(enrichCardAccountReferencesGlobal(accountDetails));
            balances.addAll(enrichCardAccountReferencesGlobal(accountDetails));
        } else {
            for (Xs2aCardAccountDetails accountDetail : accountDetails) {
                accounts.addAll(enrichCardAccountReferences(accountDetail, existingAccess.getAccounts()));
                balances.addAll(enrichCardAccountReferences(accountDetail, existingAccess.getBalances()));
                transactions.addAll(enrichCardAccountReferences(accountDetail, existingAccess.getTransactions()));
            }
        }

        for (Xs2aCardAccountDetails accountDetail : accountDetails) {
            if (additionalInformationAccess != null && additionalInformationAccess.getOwnerName() != null) {
                ownerName.addAll(enrichCardAccountReferences(accountDetail, additionalInformationAccess.getOwnerName()));
            }
        }

        AccountAccess accountAccess =
            getXs2aAccountAccess(accounts, transactions, balances, ownerName, additionalInformationAccess);

        return aisConsentService.updateAspspAccountAccess(consentId, accountAccess);
    }

    private AccountAccess getXs2aAccountAccess(List<AccountReference> accounts,
                                               List<AccountReference> transactions, List<AccountReference> balances,
                                               List<AccountReference> ownerName, AdditionalInformationAccess additionalInformationAccess) {

        return new AccountAccess(accounts, balances, transactions,
                                 Optional.ofNullable(additionalInformationAccess)
                                     .map(info -> new AdditionalInformationAccess(ownerName))
                                     .orElse(null));
    }

    /**
     * Enriches given list of account references with resource ID and ASPSP account ID from the given account details
     * and returns it
     *
     * <p>
     * If given list doesn't contain account reference that matches passed account details, an empty list will be
     * returned instead.
     *
     * @param xs2aAccountDetails   accounts details with resourceId and aspspAccountId set
     * @param accountReferenceList list of account references through which the search will be made
     * @return list of account references with resourceId and aspspAccountId set
     */
    private List<AccountReference> enrichAccountReferences(Xs2aAccountDetails xs2aAccountDetails, List<AccountReference> accountReferenceList) {
        return accountReferenceList.stream()
                   .filter(ar -> ar.getUsedAccountReferenceSelector().equals(xs2aAccountDetails.getAccountSelector()))
                   .map(ar -> new AccountReference(ar.getUsedAccountReferenceSelector().getAccountReferenceType(),
                                                   ar.getUsedAccountReferenceSelector().getAccountValue(),
                                                   ar.getCurrency(),
                                                   xs2aAccountDetails.getResourceId(),
                                                   xs2aAccountDetails.getAspspAccountId()))
                   .collect(Collectors.toList());
    }

    private List<AccountReference> enrichCardAccountReferences(Xs2aCardAccountDetails xs2aAccountDetails, List<AccountReference> accountReferenceList) {
        return accountReferenceList.stream()
                   .filter(ar -> cardAccountHandler.areAccountsEqual(xs2aAccountDetails, ar))
                   .map(ar -> new AccountReference(ar.getUsedAccountReferenceSelector().getAccountReferenceType(),
                                                   ar.getUsedAccountReferenceSelector().getAccountValue(),
                                                   ar.getCurrency(),
                                                   xs2aAccountDetails.getResourceId(),
                                                   xs2aAccountDetails.getAspspAccountId()))
                   .collect(Collectors.toList());
    }

    private List<AccountReference> enrichAccountReferencesGlobal(List<Xs2aAccountDetails> xs2aAccountDetails) {
        return xs2aAccountDetails.stream()
                   .map(ad -> new AccountReference(ad.getAccountSelector().getAccountReferenceType(),
                                                   ad.getAccountSelector().getAccountValue(),
                                                   ad.getCurrency(),
                                                   ad.getResourceId(),
                                                   ad.getAspspAccountId()))
                   .collect(Collectors.toList());
    }

    private List<AccountReference> enrichCardAccountReferencesGlobal(List<Xs2aCardAccountDetails> xs2aAccountDetails) {
        return xs2aAccountDetails.stream()
                   .map(ad -> new AccountReference(AccountReferenceType.MASKED_PAN,
                                                   ad.getMaskedPan(),
                                                   ad.getCurrency(),
                                                   ad.getResourceId(),
                                                   ad.getAspspAccountId()))
                   .collect(Collectors.toList());
    }

}
