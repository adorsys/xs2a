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

package de.adorsys.aspsp.xs2a.spi.impl;

import de.adorsys.aspsp.xs2a.spi.rest.client.AccountRestClient;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.exception.RestException;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.*;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiAccountAccess;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus;
import de.adorsys.psd2.xs2a.spi.service.AccountSpi;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS;
import static de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType.BANK_OFFERED;
import static de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType.GLOBAL;
import static java.nio.charset.StandardCharsets.UTF_8;

@Component
@RequiredArgsConstructor
public class AccountSpiImpl implements AccountSpi {
    // Test data is used there for testing purposes to have the possibility to see if AccountSpiImpl is being invoked from xs2a.
    // TODO remove if some requirements will be received https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/394
    private static final String TEST_ASPSP_DATA = "Test aspsp data";
    private static final String DEFAULT_ACCEPT_MEDIA_TYPE = MediaType.APPLICATION_JSON_VALUE;
    private static final String WILDCARD_ACCEPT_HEADER = "*/*";

    private final AccountRestClient accountRestClient;

    @Override
    public SpiResponse<List<SpiAccountDetails>> requestAccountList(@NotNull SpiContextData contextData, boolean withBalance, @NotNull SpiAccountConsent accountConsent, @NotNull AspspConsentData aspspConsentData) {
        try {
            List<SpiAccountDetails> accountDetailsList;

            if (EnumSet.of(GLOBAL, BANK_OFFERED, ALL_AVAILABLE_ACCOUNTS).contains(accountConsent.getAisConsentRequestType())) {
                accountDetailsList = getAccountDetailsByPsuId(accountConsent);
            } else {
                accountDetailsList = getAccountDetailsFromReferences(accountConsent);
            }

            return SpiResponse.<List<SpiAccountDetails>>builder()
                       .payload(filterAccountDetailsByWithBalance(withBalance, accountDetailsList, accountConsent.getAccess()))
                       .aspspConsentData(aspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))
                       .success();
        } catch (RestException e) {
            if (e.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {
                return SpiResponse.<List<SpiAccountDetails>>builder()
                           .fail(SpiResponseStatus.TECHNICAL_FAILURE);
            }

            return SpiResponse.<List<SpiAccountDetails>>builder()
                       .fail(SpiResponseStatus.LOGICAL_FAILURE);
        }
    }

    @Override
    public SpiResponse<SpiAccountDetails> requestAccountDetailForAccount(@NotNull SpiContextData contextData, boolean withBalance, @NotNull SpiAccountReference accountReference, @NotNull SpiAccountConsent spiAccountConsent, @NotNull AspspConsentData aspspConsentData) {
        try {
            SpiAccountDetails accountDetails = accountRestClient.getAccountDetailsById(accountReference.getResourceId());

            if (!withBalance) {
                accountDetails.emptyBalances();
            }

            return SpiResponse.<SpiAccountDetails>builder()
                       .payload(accountDetails)
                       .aspspConsentData(aspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))
                       .success();
        } catch (RestException e) {
            if (e.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {
                return SpiResponse.<SpiAccountDetails>builder()
                           .fail(SpiResponseStatus.TECHNICAL_FAILURE);
            }

            return SpiResponse.<SpiAccountDetails>builder()
                       .fail(SpiResponseStatus.LOGICAL_FAILURE);
        }
    }

    @Override
    public SpiResponse<SpiTransactionReport> requestTransactionsForAccount(@NotNull SpiContextData contextData, String acceptMediaType, boolean withBalance, @NotNull LocalDate dateFrom, @NotNull LocalDate dateTo, BookingStatus bookingStatus, @NotNull SpiAccountReference accountReference, @NotNull SpiAccountConsent spiAccountConsent, @NotNull AspspConsentData aspspConsentData) {
        try {
            SpiAccountDetails accountDetails = accountRestClient.getAccountDetailsById(accountReference.getResourceId());

            List<SpiTransaction> transactions = getFilteredTransactions(accountReference.getResourceId(), dateFrom, dateTo, bookingStatus);
            List<SpiAccountBalance> balances = getBalances(withBalance, accountDetails);

            SpiResponse.SpiResponseBuilder<SpiTransactionReport> responseBuilder =
                SpiResponse.<SpiTransactionReport>builder()
                    .aspspConsentData(aspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()));
            String responseMediaType = processAcceptMediaType(acceptMediaType);
            if (responseMediaType.contains(SpiTransactionReport.RESPONSE_TYPE_JSON)) {
                SpiTransactionReport transactionReport = new SpiTransactionReport(transactions,
                                                                                  balances,
                                                                                  SpiTransactionReport.RESPONSE_TYPE_JSON,
                                                                                  null
                );
                responseBuilder = responseBuilder.payload(transactionReport);
            } else if (responseMediaType.contains(SpiTransactionReport.RESPONSE_TYPE_TEXT)) {

                StringBuilder textResponseBuilder = new StringBuilder();
                int transactionsCount = transactions.size();
                textResponseBuilder
                    .append("Transactions report in plain text format.\n")
                    .append("=========================================\n")
                    .append("Transactions count: ").append(transactionsCount).append("\n\n");
                if (transactionsCount > 0) {
                    textResponseBuilder.append("Transactions:\n");
                    for (SpiTransaction transaction : transactions) {
                        textResponseBuilder.append(transaction).append("\n");
                    }
                }
                textResponseBuilder.append("\nEnd of report.");

                SpiTransactionReport transactionReport = new SpiTransactionReport(Collections.emptyList(),
                                                                                  Collections.emptyList(),
                                                                                  SpiTransactionReport.RESPONSE_TYPE_TEXT,
                                                                                  textResponseBuilder.toString().getBytes(UTF_8)
                );

                responseBuilder = responseBuilder.payload(transactionReport);
            } else {
                return responseBuilder
                           .fail(SpiResponseStatus.NOT_SUPPORTED);
            }

            return responseBuilder
                       .success();
        } catch (RestException e) {
            if (e.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {
                return SpiResponse.<SpiTransactionReport>builder()
                           .fail(SpiResponseStatus.TECHNICAL_FAILURE);
            }

            return SpiResponse.<SpiTransactionReport>builder()
                       .fail(SpiResponseStatus.LOGICAL_FAILURE);
        }
    }

    private String processAcceptMediaType(String acceptMediaType) {
        return StringUtils.isBlank(acceptMediaType) || WILDCARD_ACCEPT_HEADER.equals(acceptMediaType) ?
                   DEFAULT_ACCEPT_MEDIA_TYPE : acceptMediaType;
    }

    private List<SpiAccountBalance> getBalances(boolean withBalance, SpiAccountDetails accountDetails) {
        if (withBalance) {
            return accountDetails.getBalances();
        }
        return null;
    }

    @Override
    public SpiResponse<SpiTransaction> requestTransactionForAccountByTransactionId(@NotNull SpiContextData contextData, @NotNull String transactionId, @NotNull SpiAccountReference accountReference, @NotNull SpiAccountConsent accountConsent, @NotNull AspspConsentData aspspConsentData) {
        try {
            SpiTransaction transaction = accountRestClient.getTransactionByIdAndResourceId(transactionId, accountReference.getResourceId());
            return SpiResponse.<SpiTransaction>builder()
                       .payload(transaction)
                       .aspspConsentData(aspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))
                       .success();
        } catch (RestException e) {
            if (e.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {
                return SpiResponse.<SpiTransaction>builder()
                           .fail(SpiResponseStatus.TECHNICAL_FAILURE);
            }

            return SpiResponse.<SpiTransaction>builder()
                       .fail(SpiResponseStatus.LOGICAL_FAILURE);
        }
    }

    @Override
    public SpiResponse<List<SpiAccountBalance>> requestBalancesForAccount(@NotNull SpiContextData contextData, @NotNull SpiAccountReference accountReference, @NotNull SpiAccountConsent spiAccountConsent, @NotNull AspspConsentData aspspConsentData) {
        try {
            List<SpiAccountBalance> accountBalances = accountRestClient.getBalancesByResourceId(accountReference.getResourceId());

            return SpiResponse.<List<SpiAccountBalance>>builder()
                       .payload(accountBalances)
                       .aspspConsentData(aspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))
                       .success();
        } catch (RestException e) {
            if (e.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {
                return SpiResponse.<List<SpiAccountBalance>>builder()
                           .fail(SpiResponseStatus.TECHNICAL_FAILURE);
            }

            return SpiResponse.<List<SpiAccountBalance>>builder()
                       .fail(SpiResponseStatus.LOGICAL_FAILURE);
        }
    }

    private List<SpiTransaction> getFilteredTransactions(String resourceId, LocalDate dateFrom, LocalDate dateTo, BookingStatus bookingStatus) {
        return Optional.ofNullable(accountRestClient.getTransactionsByResourceIdAndPeriod(resourceId, dateFrom, dateTo))
                   .map(t -> filterByBookingStatus(t, bookingStatus))
                   .orElse(Collections.emptyList());
    }

    private List<SpiTransaction> filterByBookingStatus(List<SpiTransaction> transactionList, BookingStatus bookingStatus) {
        switch (bookingStatus) {
            case BOOKED:
                return transactionList.stream()
                           .filter(SpiTransaction::isBookedTransaction)
                           .collect(Collectors.toList());
            case PENDING:
                return transactionList.stream()
                           .filter(SpiTransaction::isPendingTransaction)
                           .collect(Collectors.toList());
            case BOTH:
            default:
                return transactionList;
        }
    }

    private List<SpiAccountDetails> getAccountDetailsByPsuId(SpiAccountConsent accountConsent) {
        // TODO correctly handle multiple PSUs for multilevel SCA https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/516
        String psuId = CollectionUtils.isNotEmpty(accountConsent.getPsuData())
                           ? accountConsent.getPsuData().get(0).getPsuId()
                           : null;

        List<SpiAccountDetails> spiAccountDetails = new ArrayList<>();
        List<SpiAccountDetails> spiAccountDetailsByPsuId = Optional.ofNullable(accountRestClient.getAccountDetailsByPsuId(psuId)).orElseGet(Collections::emptyList);

        spiAccountDetailsByPsuId.forEach(a -> spiAccountDetails.add(accountRestClient.getAccountDetailsById(a.getResourceId())));
        return spiAccountDetails;
    }

    private List<SpiAccountDetails> getAccountDetailsFromReferences(SpiAccountConsent accountConsent) { // TODO remove consentId param, when SpiAccountConsent contains it https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/430
        SpiAccountAccess accountAccess = accountConsent.getAccess();
        return getAccountDetailsFromReferences(accountAccess.getAccounts());
    }

    private List<SpiAccountDetails> getAccountDetailsFromReferences(List<SpiAccountReference> references) {
        if (CollectionUtils.isEmpty(references)) {
            return Collections.emptyList();
        }

        return references.stream()
                   .map(this::getAccountDetailsByAccountReference)
                   .filter(Optional::isPresent)
                   .map(Optional::get)
                   .collect(Collectors.toList());
    }

    private Optional<SpiAccountDetails> getAccountDetailsByAccountReference(SpiAccountReference reference) {
        if (reference == null) {
            return Optional.empty();
        }

        // TODO don't use IBAN as an account identifier https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/440
        List<SpiAccountDetails> accountDetails = Optional.ofNullable(
            accountRestClient.getAccountDetailsByIban(reference.getIban())).orElseGet(Collections::emptyList);

        return accountDetails.stream()
                   .filter(acc -> acc.getResourceId().equals(reference.getResourceId()))
                   .findFirst();
    }

    private List<SpiAccountDetails> filterAccountDetailsByWithBalance(boolean withBalance, List<SpiAccountDetails> details,
                                                                      SpiAccountAccess spiAccountAccess) {

        if (withBalance && isConsentSupportedBalances(spiAccountAccess)) {
            return details;
        }

        List<SpiAccountReference> balanceReferences = spiAccountAccess.getBalances();

        for (SpiAccountDetails spiAccountDetails : details) {
            if (!withBalance || !isValidAccountByAccess(spiAccountDetails.getResourceId(), balanceReferences)) {
                spiAccountDetails.emptyBalances();
            }
        }

        return details;
    }

    private boolean isConsentSupportedBalances(SpiAccountAccess spiAccountAccess) {
        boolean isConsentGlobal = spiAccountAccess.getAllPsd2() != null;
        boolean isConsentForAvailableAccountsWithBalances = spiAccountAccess.getAvailableAccountsWithBalances() == AccountAccessType.ALL_ACCOUNTS;
        return isConsentGlobal || isConsentForAvailableAccountsWithBalances;
    }

    private boolean isValidAccountByAccess(String accountId, List<SpiAccountReference> allowedAccountData) {
        return CollectionUtils.isNotEmpty(allowedAccountData)
                   && allowedAccountData.stream()
                          .anyMatch(a -> accountId.equals(a.getResourceId()));
    }

}
