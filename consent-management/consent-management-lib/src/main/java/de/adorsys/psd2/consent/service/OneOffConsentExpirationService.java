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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.domain.account.AisConsentTransaction;
import de.adorsys.psd2.consent.repository.AisConsentTransactionRepository;
import de.adorsys.psd2.consent.repository.AisConsentUsageRepository;
import de.adorsys.psd2.consent.service.mapper.CmsAisConsentMapper;
import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AdditionalInformationAccess;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class OneOffConsentExpirationService {
    public static final String BENEFICIARIES_URI = "/v1/trusted-beneficiaries";

    public static final int READ_ONLY_ACCOUNT_DETAILS_COUNT = 1;
    public static final int READ_ACCOUNT_DETAILS_AND_BALANCES_COUNT = 2;
    public static final int READ_ALL_DETAILS_AND_BENEFICIARIES = 3;

    private final AisConsentUsageRepository aisConsentUsageRepository;
    private final AisConsentTransactionRepository aisConsentTransactionRepository;
    private final CmsAisConsentMapper cmsAisConsentMapper;
    private final AspspProfileService aspspProfileService;

    /**
     * Checks, should the one-off consent be expired after using its all GET endpoints (accounts, balances, transactions)
     * in all possible combinations depending on the consent type.
     *
     * @param consentId  consentId to check.
     * @param cmsConsent the {@link CmsConsent} to check.
     * @return true if the consent should be expired, false otherwise.
     */
    public boolean isConsentExpired(CmsConsent cmsConsent, Long consentId) {
        AisConsentRequestType consentRequestType = cmsAisConsentMapper.mapToAisConsent(cmsConsent).getConsentRequestType();

        // We omit all bank offered consents until they are not populated with accounts.
        if (consentRequestType == AisConsentRequestType.BANK_OFFERED) {
            return false;
        }

        // All available account consent support only one call - readAccountList.
        if (consentRequestType == AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS) {
            return true;
        }

        AccountAccess aspspAccountAccesses = cmsConsent.getAspspAccountAccesses();
        List<AccountReference> references = Stream.of(aspspAccountAccesses.getAccounts(), aspspAccountAccesses.getBalances(), aspspAccountAccesses.getTransactions())
                                                .flatMap(Collection::stream).collect(Collectors.toList());

        List<String> consentResourceIds = references.stream()
                                              .map(AccountReference::getResourceId)
                                              .distinct()
                                              .collect(Collectors.toList());

        boolean isExpired = true;
        for (String resourceId : consentResourceIds) {
            List<AisConsentTransaction> consentTransactions = aisConsentTransactionRepository.findByConsentIdAndResourceId(consentId,
                                                                                                                           resourceId,
                                                                                                                           PageRequest.of(0, 1));

            int numberOfTransactions =
                CollectionUtils.isNotEmpty(consentTransactions) ? consentTransactions.get(0).getNumberOfTransactions() : 0;
            boolean isConsentGlobal = consentRequestType == AisConsentRequestType.GLOBAL;
            int bookingStatusesAvailable = aspspProfileService.getAspspSettings(cmsConsent.getInstanceId())
                                               .getAis().getTransactionParameters().getAvailableBookingStatuses().size();
            int maximumNumberOfGetRequestsForConsent =
                getMaximumNumberOfGetRequestsForConsentsAccount(aspspAccountAccesses, resourceId, numberOfTransactions,
                                                                isConsentGlobal, cmsConsent.getInstanceId(), bookingStatusesAvailable);
            int numberOfUsedGetRequestsForConsent = getNumberOfUsedGetRequestsForConsent(consentId, resourceId);

            // There are some available not used get requests - omit all other iterations.
            if (numberOfUsedGetRequestsForConsent < maximumNumberOfGetRequestsForConsent) {
                isExpired = false;
                break;
            }
        }

        return isExpired;
    }

    /**
     * This method returns number of already used get requests for the definite consent for ONE account
     * plus trusted beneficiaries get request.
     */
    private int getNumberOfUsedGetRequestsForConsent(Long consentId, String resourceId) {
        int numberOfUsedGetRequestsForConsent = aisConsentUsageRepository.countByConsentIdAndResourceId(consentId, resourceId);
        int numberOfUsedGetRequestsForBeneficiaries = aisConsentUsageRepository.countByConsentIdAndRequestUri(consentId, BENEFICIARIES_URI);
        return numberOfUsedGetRequestsForConsent + numberOfUsedGetRequestsForBeneficiaries;
    }

    /**
     * This method returns maximum number of possible get requests for the definite consent for ONE account
     * except the main get call - readAccountList.
     */
    private int getMaximumNumberOfGetRequestsForConsentsAccount(AccountAccess aspspAccountAccesses,
                                                                String resourceId,
                                                                int numberOfTransactions,
                                                                boolean isConsentGlobal,
                                                                String instanceId,
                                                                int bookingStatusesAvailable) {

        boolean accessesForAccountsEmpty = isAccessForAccountReferencesEmpty(aspspAccountAccesses.getAccounts(), resourceId);
        boolean accessesForBalanceEmpty = isAccessForAccountReferencesEmpty(aspspAccountAccesses.getBalances(), resourceId);
        boolean accessesForTransactionsEmpty = isAccessForAccountReferencesEmpty(aspspAccountAccesses.getTransactions(), resourceId);

        // Consent was given only for accounts: readAccountDetails for each account.
        if (!accessesForAccountsEmpty
                && accessesForBalanceEmpty
                && accessesForTransactionsEmpty) {
            return READ_ONLY_ACCOUNT_DETAILS_COUNT;
        }

        // Consent was given for accounts and balances.
        if (accessesForTransactionsEmpty) {
            // Value 2 corresponds to the readAccountDetails and readBalances.
            return READ_ACCOUNT_DETAILS_AND_BALANCES_COUNT;
        }

        // Consent was given for accounts and transactions lists for each booking status.
        if (accessesForBalanceEmpty) {
            // Value 1 corresponds to the readAccountDetails.
            // Plus quantity of transaction lists which is equal to the number ob available booking statuses.
            // Plus each account's transactions.
            return READ_ONLY_ACCOUNT_DETAILS_COUNT + bookingStatusesAvailable + numberOfTransactions;
        }

        // Consent was given for accounts, balances, transactions lists for each booking status and beneficiaries.
        if (isBeneficiariesEndpointAllowed(isConsentGlobal, aspspAccountAccesses, instanceId)) {
            return READ_ALL_DETAILS_AND_BENEFICIARIES + bookingStatusesAvailable + numberOfTransactions;
        }

        // Consent was given for accounts, balances and transactions lists for each booking status.
        return READ_ACCOUNT_DETAILS_AND_BALANCES_COUNT + bookingStatusesAvailable + numberOfTransactions;
    }

    private boolean isBeneficiariesEndpointAllowed(boolean isConsentGlobal, AccountAccess aspspAccountAccesses, String instanceId) {
        return isGlobalConsentWithBeneficiaries(isConsentGlobal, instanceId) || !isTrustedBeneficiariesNotAllowed(aspspAccountAccesses);
    }

    private boolean isGlobalConsentWithBeneficiaries(boolean isConsentGlobal, String instanceId) {
        return isConsentGlobal && isTrustedBeneficiariesSupported(instanceId);
    }

    private boolean isTrustedBeneficiariesNotAllowed(AccountAccess aspspAccountAccesses) {
        AdditionalInformationAccess additionalInformationAccess = aspspAccountAccesses.getAdditionalInformationAccess();
        return additionalInformationAccess == null || additionalInformationAccess.getTrustedBeneficiaries() == null;
    }

    private boolean isAccessForAccountReferencesEmpty(List<AccountReference> accounts, String resourceId) {
        return accounts.stream().noneMatch(access -> access.getResourceId().equals(resourceId));
    }

    public boolean isTrustedBeneficiariesSupported(String instanceId) {
        return aspspProfileService.getAspspSettings(instanceId).getAis().getConsentTypes().isTrustedBeneficiariesSupported();
    }
}
