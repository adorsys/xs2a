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

import de.adorsys.aspsp.xs2a.spi.config.rest.AspspRemoteUrls;
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
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType.BANK_OFFERED;
import static de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType.GLOBAL;
import static java.nio.charset.StandardCharsets.UTF_8;

@Component
@RequiredArgsConstructor
public class AccountSpiImpl implements AccountSpi {
    // Test data is used there for testing purposes to have the possibility to see if AccountSpiImpl is being invoked from xs2a.
    // TODO remove if some requirements will be received https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/394
    private static final String TEST_ASPSP_DATA = "Test aspsp data";

    private final AspspRemoteUrls remoteSpiUrls;
    @Qualifier("aspspRestTemplate")
    private final RestTemplate aspspRestTemplate;

    @Override
    public SpiResponse<List<SpiAccountDetails>> requestAccountList(@NotNull SpiContextData contextData, boolean withBalance, @NotNull SpiAccountConsent accountConsent, @NotNull AspspConsentData aspspConsentData) {
        try {
            List<SpiAccountDetails> accountDetailsList;

            if (EnumSet.of(GLOBAL, BANK_OFFERED).contains(accountConsent.getAisConsentRequestType())) {
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
            SpiAccountDetails accountDetails = aspspRestTemplate.getForObject(remoteSpiUrls.getAccountDetailsById(), SpiAccountDetails.class, accountReference.getResourceId());

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
            SpiAccountDetails accountDetails = aspspRestTemplate.getForObject(remoteSpiUrls.getAccountDetailsById(), SpiAccountDetails.class, accountReference.getResourceId());

            Map<String, String> uriParams = new HashMap<>();
            uriParams.put("account-id", accountReference.getResourceId());

            UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(remoteSpiUrls.readTransactionsByPeriod())
                                              .queryParam("dateFrom", dateFrom)
                                              .queryParam("dateTo", dateTo)
                                              .buildAndExpand(uriParams);

            List<SpiTransaction> transactions = getFilteredTransactions(uriComponents, bookingStatus);
            List<SpiAccountBalance> balances = null;

            if (withBalance) {
                balances = accountDetails.getBalances();
            }

            SpiResponse.SpiResponseBuilder<SpiTransactionReport> responseBuilder =
                SpiResponse.<SpiTransactionReport>builder()
                    .aspspConsentData(aspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()));
            if (acceptMediaType.contains(SpiTransactionReport.RESPONSE_TYPE_JSON)) {
                SpiTransactionReport transactionReport = new SpiTransactionReport(transactions,
                                                                                  balances,
                                                                                  SpiTransactionReport.RESPONSE_TYPE_JSON,
                                                                                  null
                );
                responseBuilder = responseBuilder.payload(transactionReport);
            } else if (acceptMediaType.contains(SpiTransactionReport.RESPONSE_TYPE_TEXT)) {

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

    @Override
    public SpiResponse<SpiTransaction> requestTransactionForAccountByTransactionId(@NotNull SpiContextData contextData, @NotNull String transactionId, @NotNull SpiAccountReference accountReference, @NotNull SpiAccountConsent accountConsent, @NotNull AspspConsentData aspspConsentData) {
        try {
            SpiTransaction transaction = aspspRestTemplate.getForObject(remoteSpiUrls.readTransactionById(), SpiTransaction.class, transactionId, accountReference.getResourceId());
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
            List<SpiAccountBalance> accountBalances = aspspRestTemplate.exchange(
                remoteSpiUrls.getBalancesByAccountId(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<SpiAccountBalance>>() {
                },
                accountReference.getResourceId()
            ).getBody();

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

    private List<SpiTransaction> getFilteredTransactions(UriComponents uriComponents, BookingStatus bookingStatus) {
        return Optional.ofNullable(getTransactionsFromAspsp(uriComponents))
                   .map(t -> filterByBookingStatus(t, bookingStatus))
                   .orElse(Collections.emptyList());
    }

    private List<SpiTransaction> getTransactionsFromAspsp(UriComponents uriComponents) {
        return aspspRestTemplate.exchange(
            uriComponents.toUriString(),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<SpiTransaction>>() {
            }
        ).getBody();
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

        return Optional.ofNullable(aspspRestTemplate.exchange(
            remoteSpiUrls.getAccountDetailsByPsuId(),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<SpiAccountDetails>>() {
            },
            psuId
        ).getBody())
                   .orElseGet(Collections::emptyList);
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
            aspspRestTemplate.exchange(
                remoteSpiUrls.getAccountDetailsByIban(),
                HttpMethod.GET,
                new HttpEntity<>(null), new ParameterizedTypeReference<List<SpiAccountDetails>>() {
                },
                reference.getIban()
            ).getBody()
        )
                                                     .orElseGet(Collections::emptyList);

        // TODO don't use currency as an account identifier https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/440
        return accountDetails.stream()
                   .filter(acc -> acc.getCurrency() == reference.getCurrency())
                   .findFirst();
    }

    private List<SpiAccountDetails> filterAccountDetailsByWithBalance(boolean withBalance, List<SpiAccountDetails> details,
                                                                      SpiAccountAccess spiAccountAccess) {

        List<SpiAccountReference> balanceReferences = spiAccountAccess.getBalances();

        for (SpiAccountDetails spiAccountDetails : details) {
            if (!withBalance || !isValidAccountByAccess(spiAccountDetails.getResourceId(), balanceReferences)) {
                spiAccountDetails.emptyBalances();
            }
        }

        return details;
    }

    private boolean isValidAccountByAccess(String accountId, List<SpiAccountReference> allowedAccountData) {
        return CollectionUtils.isNotEmpty(allowedAccountData)
                   && allowedAccountData.stream()
                          .anyMatch(a -> accountId.equals(a.getResourceId()));
    }

}
