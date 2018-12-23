/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountDetails;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * This service is designed to update AccountReferences in AisConsent. Also for bank offered consent and global consent
 * AccountReferences needed to be saved in AisConsent in order to be used in next GET calls.
 */
@Service
@RequiredArgsConstructor
public class AccountReferenceInConsentUpdater {
    private final Xs2aAisConsentService aisConsentService;

    private final Xs2aAisConsentMapper consentMapper;

    /**
     * Overwrites existing account access with the new one. To be used with caution.
     * Only allowed when you get new AccountAccess from ASPSP side,
     * NOT ALLOWED to update consent by smth coming from TPP side!
     *
     * @param consentId             an external ID of consent, where account access to be stored
     * @param newAccountAccess      new object with account accesses
     */
    public void rewriteAccountAccess(@NotNull String consentId, @NotNull Xs2aAccountAccess newAccountAccess) {
        Optional<Xs2aAccountAccess> existingAccess = loadExistingAccountReferences(consentId);
        if (!existingAccess.isPresent()) {
            return; //Consent not found
        }

        aisConsentService.updateAccountAccess(consentId, consentMapper.mapToAisAccountAccessInfo(newAccountAccess));
    }

    /**
     * Updates account resourceIDs in existing account access. If account is not found in consent's account access,
     * will be ignored.
     *
     * @param consentId         an external ID of consent, where account access to be stored
     * @param accountDetails    list of account details with referenceId set
     */
    public void updateAccountReferences(@NotNull String consentId, @NotNull List<Xs2aAccountDetails> accountDetails) {
        Optional<Xs2aAccountAccess> existingAccess = loadExistingAccountReferences(consentId);

        if (!existingAccess.isPresent()) {
            return; //Consent not found
        }

        // update existing account references
        Xs2aAccountAccess updatedAccountAccess = updateResourceId(existingAccess.get(), accountDetails);

        aisConsentService.updateAccountAccess(consentId, consentMapper.mapToAisAccountAccessInfo(updatedAccountAccess));
    }

    private Optional<Xs2aAccountAccess> loadExistingAccountReferences(@NotNull String consentId) {
        return Optional.ofNullable(aisConsentService.getAccountConsentById(consentId))
            .map(AccountConsent::getAccess);
    }

    //TODO migrate to new structure of accountReferences in consent (determine requested account access and keep additionally real account references
    //     https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/491
    private Xs2aAccountAccess updateResourceId(Xs2aAccountAccess accountAccess, List<Xs2aAccountDetails> accountDetailsList) {
        for (Xs2aAccountDetails accountDetails : accountDetailsList) {
            if (CollectionUtils.isNotEmpty(accountAccess.getAccounts())) {
                updateResourceId(accountAccess.getAccounts(), accountDetails, accountDetails.getResourceId());
            }
            if (CollectionUtils.isNotEmpty(accountAccess.getBalances())) {
                updateResourceId(accountAccess.getBalances(), accountDetails, accountDetails.getResourceId());
            }
            if (CollectionUtils.isNotEmpty(accountAccess.getTransactions())) {
                updateResourceId(accountAccess.getTransactions(), accountDetails,
                                 accountDetails.getResourceId());
            }
        }
        return accountAccess;
    }

    private void updateResourceId(List<AccountReference> accountReferences, Xs2aAccountDetails xs2aAccountDetails, String resourceId) {
        accountReferences.stream()
            .filter(accountReference -> isSameAccountReference(accountReference, xs2aAccountDetails))
            .findFirst()
            .ifPresent(xs2aAccountReference -> xs2aAccountReference.setResourceId(resourceId));
    }

    private boolean isSameAccountReference(AccountReference accountReference, Xs2aAccountDetails accountDetails) {
        boolean same = Optional.ofNullable(accountReference.getIban())
                           .map(iban -> StringUtils.equals(iban, accountDetails.getIban()))
                           .orElse(false);

        if (!same) {
            same = Optional.ofNullable(accountReference.getBban())
                       .map(bban -> StringUtils.equals(bban, accountDetails.getBban()))
                       .orElse(false);
        }
        if (!same) {
            same = Optional.ofNullable(accountReference.getMaskedPan())
                       .map(maskedpan -> StringUtils.equals(maskedpan, accountDetails.getMaskedPan()))
                       .orElse(false);
        }
        if (!same) {
            same = Optional.ofNullable(accountReference.getMsisdn())
                       .map(msisdn -> StringUtils.equals(msisdn, accountDetails.getMsisdn()))
                       .orElse(false);
        }
        if (!same) {
            same = Optional.ofNullable(accountReference.getPan())
                       .map(pan -> StringUtils.equals(pan, accountDetails.getPan()))
                       .orElse(false);
        }
        return same;
    }


}
