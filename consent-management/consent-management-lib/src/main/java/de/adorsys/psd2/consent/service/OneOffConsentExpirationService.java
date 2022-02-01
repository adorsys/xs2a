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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.domain.account.AisConsentTransaction;
import de.adorsys.psd2.consent.repository.AisConsentTransactionRepository;
import de.adorsys.psd2.consent.repository.AisConsentUsageRepository;
import de.adorsys.psd2.consent.service.mapper.CmsAisConsentMapper;
import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AdditionalInformationAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
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
            List<BookingStatus> bookingStatuses = aspspProfileService.getAspspSettings(cmsConsent.getInstanceId())
                                                      .getAis().getTransactionParameters().getAvailableBookingStatuses();
            List<AisConsentTransaction> consentTransactions = aisConsentTransactionRepository.findByConsentIdAndResourceId(consentId,
                                                                                                                           resourceId,
                                                                                                                           Pageable.unpaged())
                                                                  .stream()
                                                                  .filter(t -> bookingStatuses.contains(t.getBookingStatus()))
                                                                  .collect(Collectors.toList());

            int numberOfTransactions = getNumberOfTransactions(consentTransactions);
            boolean isConsentGlobal = consentRequestType == AisConsentRequestType.GLOBAL;

            // Total pages for all available booking statuses
            int totalPages = getTotalPages(consentTransactions, bookingStatuses);

            int maximumNumberOfGetRequestsForConsent =
                getMaximumNumberOfGetRequestsForConsentsAccount(aspspAccountAccesses, resourceId, numberOfTransactions,
                                                                isConsentGlobal, cmsConsent.getInstanceId(), totalPages);
            int numberOfUsedGetRequestsForConsent = getNumberOfUsedGetRequestsForConsent(consentId, resourceId);

            // There are some available not used get requests - omit all other iterations.
            if (numberOfUsedGetRequestsForConsent < maximumNumberOfGetRequestsForConsent) {
                isExpired = false;
                break;
            }
        }

        return isExpired;
    }

    private int getNumberOfTransactions(List<AisConsentTransaction> consentTransactions) {
        Set<BookingStatus> bookingStatuses = consentTransactions.stream()
                                                 .map(AisConsentTransaction::getBookingStatus)
                                                 .collect(Collectors.toSet());
        EnumSet<BookingStatus> filteredBookingStatuses;
        if (bookingStatuses.contains(BookingStatus.ALL)) {
            filteredBookingStatuses = EnumSet.of(BookingStatus.ALL);
        } else if (bookingStatuses.contains(BookingStatus.BOTH)) {
            filteredBookingStatuses = EnumSet.of(BookingStatus.BOTH);
        } else {
            filteredBookingStatuses = EnumSet.of(BookingStatus.BOOKED, BookingStatus.PENDING);
        }

        return consentTransactions.stream()
                   .filter(t -> filteredBookingStatuses.contains(t.getBookingStatus()))
                   .map(AisConsentTransaction::getNumberOfTransactions)
                   .mapToInt(Integer::intValue)
                   .sum();
    }

    private int getTotalPages(List<AisConsentTransaction> consentTransactions, List<BookingStatus> bookingStatuses) {
        Map<BookingStatus, Integer> map = consentTransactions.stream().collect(Collectors.toMap(AisConsentTransaction::getBookingStatus, AisConsentTransaction::getTotalPages, Math::max));
        int result = 0;

        for (BookingStatus bs : bookingStatuses) {
            result = result + map.getOrDefault(bs, 1);
        }

        return result;
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
                                                                int totalPages) {

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

        // Consent was given for accounts and transactions lists for each booking status with paging.
        if (accessesForBalanceEmpty) {
            // Value 1 corresponds to the readAccountDetails.
            // Plus quantity of transaction lists with paging which is equal to the number of available booking statuses.
            // Plus each account's transaction.
            return READ_ONLY_ACCOUNT_DETAILS_COUNT + totalPages + numberOfTransactions;
        }

        // Consent was given for accounts, balances, transactions lists for each booking status with paging and beneficiaries.
        if (isBeneficiariesEndpointAllowed(isConsentGlobal, aspspAccountAccesses, instanceId)) {
            return READ_ALL_DETAILS_AND_BENEFICIARIES + totalPages + numberOfTransactions;
        }

        // Consent was given for accounts, balances and transactions lists for each booking status with paging.
        return READ_ACCOUNT_DETAILS_AND_BALANCES_COUNT + totalPages + numberOfTransactions;
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
