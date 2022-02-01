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

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import de.adorsys.psd2.xs2a.core.profile.AdditionalInformationAccess;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountDetails;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCardAccountDetails;
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
    private final CardAccountHandler cardAccountHandler;
    private final Xs2aPiisConsentService xs2aPiisConsentService;

    /**
     * Overwrites existing account access with the new one. To be used with caution.
     * Only allowed when you get new AccountAccess from ASPSP side,
     * NOT ALLOWED to update consent by something coming from TPP side!
     *
     * @param consentId        an external ID of consent, where account access to be stored
     * @param newAccountAccess new object with account accesses
     * @param consentType      type of the consent
     */
    public void rewriteAccountAccess(@NotNull String consentId, @NotNull AccountAccess newAccountAccess, ConsentType consentType) {
        if (consentType == ConsentType.AIS) {
            aisConsentService.updateAspspAccountAccess(consentId, newAccountAccess);
        } else {
            xs2aPiisConsentService.updateAspspAccountAccess(consentId, newAccountAccess);
        }
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
        List<AccountReference> trustedBeneficiaries = new ArrayList<>();
        AccountAccess existingAccess = aisConsent.getAccess();
        AdditionalInformationAccess additionalInformationAccess = existingAccess.getAdditionalInformationAccess();

        if (aisConsent.isGlobalConsent()) {
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

            if (additionalInformationAccess != null && additionalInformationAccess.getTrustedBeneficiaries() != null) {
                trustedBeneficiaries.addAll(enrichAccountReferences(accountDetail, additionalInformationAccess.getTrustedBeneficiaries()));
            }
        }

        AccountAccess accountAccess =
            getXs2aAccountAccess(accounts, transactions, balances, ownerName, trustedBeneficiaries, additionalInformationAccess);

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
        List<AccountReference> trustedBeneficiaries = new ArrayList<>();
        AccountAccess existingAccess = aisConsent.getAccess();
        AdditionalInformationAccess additionalInformationAccess = existingAccess.getAdditionalInformationAccess();

        if (aisConsent.isGlobalConsent()) {
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
            if (additionalInformationAccess != null && additionalInformationAccess.getTrustedBeneficiaries() != null) {
                trustedBeneficiaries.addAll(enrichCardAccountReferences(accountDetail, additionalInformationAccess.getTrustedBeneficiaries()));
            }
        }

        AccountAccess accountAccess =
            getXs2aAccountAccess(accounts, transactions, balances, ownerName, trustedBeneficiaries, additionalInformationAccess);

        return aisConsentService.updateAspspAccountAccess(consentId, accountAccess);
    }

    private AccountAccess getXs2aAccountAccess(List<AccountReference> accounts,
                                               List<AccountReference> transactions, List<AccountReference> balances,
                                               List<AccountReference> ownerName, List<AccountReference> trustedBeneficiaries,
                                               AdditionalInformationAccess additionalInformationAccess) {

        return new AccountAccess(accounts, balances, transactions,
                                 Optional.ofNullable(additionalInformationAccess)
                                     .map(info -> new AdditionalInformationAccess(ownerName, trustedBeneficiaries))
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
