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
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OneOffConsentExpirationService {

    private final AisConsentUsageRepository aisConsentUsageRepository;
    private final AisConsentTransactionRepository aisConsentTransactionRepository;

    /**
     * Checks, should the one-off consent be expired after using its all GET endpoints (accounts, balances, transactions)
     * in all possible combinations depending on the consent type.
     *
     * @param consent the {@link AisConsent} to check.
     * @return true if the consent should be expired, false otherwise.
     */
    public boolean isConsentExpired(AisConsent consent) {

        // We omit all bank offered consents until they are not populated with accounts.
        if (consent.getAisConsentRequestType() == AisConsentRequestType.BANK_OFFERED) {
            return false;
        }

        // All available account consent support only one call - readAccountList.
        if (consent.getAisConsentRequestType() == AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS) {
            return true;
        }

        List<String> consentResourceIds = consent.getAspspAccountAccesses()
                                              .stream()
                                              .map(AspspAccountAccess::getResourceId)
                                              .distinct()
                                              .collect(Collectors.toList());

        boolean isExpired = true;
        for (String resourceId : consentResourceIds) {
            Optional<AisConsentTransaction> transactionOptional = aisConsentTransactionRepository.findByConsentIdAndResourceId(consent, resourceId);

            int transactions = transactionOptional
                                    .map(AisConsentTransaction::getNumberOfTransactions)
                                    .orElse(0);

            int maximumNumberOfGetRequestsForConsent = getMaximumNumberOfGetRequestsForConsentsAccount(consent, transactions);
            int numberOfUsedGetRequestsForConsent = aisConsentUsageRepository.countByConsentIdAndResourceId(consent.getId(), resourceId);

            // There are some available not used get requests - omit all other iterations.
            if (numberOfUsedGetRequestsForConsent < maximumNumberOfGetRequestsForConsent) {
                isExpired = false;
                break;
            }
        }

        return isExpired;
    }

    /**
     * This method returns maximum number of possible get requests for the definite consent for ONE account
     * except the main get call - readAccountList.
     */
    private int getMaximumNumberOfGetRequestsForConsentsAccount(AisConsent consent, int numberOfTransactions) {
        switch (consent.getAisConsentRequestType()) {
            case DEDICATED_ACCOUNTS:
                List<AspspAccountAccess> aspspAccountAccesses = consent.getAspspAccountAccesses();

                // Consent was given only for accounts: readAccountDetails for each account.
                if (aspspAccountAccesses.size() == 1) {
                    return 1;
                }

                // Consent was given for accounts, balances and transactions.
                if (aspspAccountAccesses.size() == 3) {
                    return 3 + numberOfTransactions;
                }

                // Consent was given for accounts and balances.
                if (aspspAccountAccesses
                        .stream()
                        .noneMatch(aspspAccountAccess -> aspspAccountAccess.getTypeAccess() == TypeAccess.TRANSACTION)) {

                    // Value 2 corresponds to the readAccountDetails and readBalances.
                    return 2;
                }

                // Consent was given for accounts and transactions.
                if (aspspAccountAccesses
                        .stream()
                        .noneMatch(aspspAccountAccess -> aspspAccountAccess.getTypeAccess() == TypeAccess.BALANCE)) {

                    // Value 2 corresponds to the readAccountDetails and readTransactions. Plus each account's transactions.
                    return 2 + numberOfTransactions;
                }

            case GLOBAL:
                // Value 3 corresponds to the number of static get requests in scope of each account: readAccountDetails,
                // readBalances, readTransactionList.
                return 3 + numberOfTransactions;
            default:
                throw new UnsupportedOperationException("Not supported AIS consent type.");
        }
    }
}
