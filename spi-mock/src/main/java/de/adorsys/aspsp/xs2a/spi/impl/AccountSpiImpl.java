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
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.exception.RestException;
import de.adorsys.psd2.xs2a.spi.domain.account.*;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiAccountAccess;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
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
    public SpiResponse<List<SpiAccountDetails>> requestAccountList(boolean withBalance, @NotNull SpiAccountConsent accountConsent, @NotNull AspspConsentData aspspConsentData) {
        try {
            List<SpiAccountDetails> accountDetailsList;

            if (isBankOfferedConsent(accountConsent.getAccess())) {
                accountDetailsList = getAccountDetailsByConsentId(accountConsent);
            } else {
                accountDetailsList = getAccountDetailsFromReferences(withBalance, accountConsent);
            }

            return SpiResponse.<List<SpiAccountDetails>>builder()
                .payload(filterAccountDetailsByWithBalance(withBalance, accountDetailsList))
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
    public SpiResponse<SpiAccountDetails> requestAccountDetailForAccount(boolean withBalance, @NotNull SpiAccountReference accountReference, @NotNull SpiAccountConsent spiAccountConsent, @NotNull AspspConsentData aspspConsentData) {
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
    public SpiResponse<SpiTransactionReport> requestTransactionsForAccount(String acceptMediaType, boolean withBalance, @NotNull LocalDate dateFrom, @NotNull LocalDate dateTo, @NotNull SpiAccountReference accountReference, @NotNull SpiAccountConsent spiAccountConsent, @NotNull AspspConsentData aspspConsentData) {
        try {
            SpiAccountDetails accountDetails = aspspRestTemplate.getForObject(remoteSpiUrls.getAccountDetailsById(), SpiAccountDetails.class, accountReference.getResourceId());

            Map<String, String> uriParams = new HashMap<>();
            uriParams.put("account-id", accountReference.getResourceId());

            UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(remoteSpiUrls.readTransactionsByPeriod())
                .queryParam("dateFrom", dateFrom)
                .queryParam("dateTo", dateTo)
                .buildAndExpand(uriParams);

            Optional<List<SpiTransaction>> transactionsOptional = Optional.ofNullable(aspspRestTemplate.exchange(
                uriComponents.toUriString(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<SpiTransaction>>() {
                }
            ).getBody());

            List<SpiTransaction> transactions = transactionsOptional.orElseGet(ArrayList::new);
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
            } else {
                String plainTextResponse = transactions.stream()
                                     .map(SpiTransaction::toString)
                                     .collect(Collectors.joining(",\n"));
                SpiTransactionReport transactionReport = new SpiTransactionReport(Collections.emptyList(),
                                                                                  Collections.emptyList(),
                                                                                  "text/plain",
                                                                                  plainTextResponse.getBytes(UTF_8)
                );
                responseBuilder = responseBuilder.payload(transactionReport);
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
    public SpiResponse<SpiTransaction> requestTransactionForAccountByTransactionId(@NotNull String transactionId, @NotNull SpiAccountReference accountReference, @NotNull SpiAccountConsent accountConsent, @NotNull AspspConsentData aspspConsentData) {
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
    public SpiResponse<List<SpiAccountBalance>> requestBalancesForAccount(@NotNull SpiAccountReference accountReference, @NotNull SpiAccountConsent spiAccountConsent, @NotNull AspspConsentData aspspConsentData) {
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

    private boolean isBankOfferedConsent(SpiAccountAccess accountAccess) {
        return CollectionUtils.isEmpty(accountAccess.getBalances())
            && CollectionUtils.isEmpty(accountAccess.getTransactions())
            && CollectionUtils.isEmpty(accountAccess.getAccounts());
    }

    private List<SpiAccountDetails> getAccountDetailsByConsentId(SpiAccountConsent accountConsent) {
        return Optional.ofNullable(aspspRestTemplate.exchange(
            remoteSpiUrls.getAccountDetailsByPsuId(),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<SpiAccountDetails>>() {
            },
            Optional.ofNullable(accountConsent.getPsuData()).map(SpiPsuData::getPsuId).orElse(null)
        ).getBody())
            .orElseGet(Collections::emptyList);
    }

    private List<SpiAccountDetails> getAccountDetailsFromReferences(boolean withBalance, SpiAccountConsent accountConsent) { // TODO remove consentId param, when SpiAccountConsent contains it https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/430
        SpiAccountAccess accountAccess = accountConsent.getAccess();

        List<SpiAccountReference> references = withBalance
            ? accountAccess.getBalances()
            : accountAccess.getAccounts();

        return getAccountDetailsFromReferences(references);
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

    private List<SpiAccountDetails> filterAccountDetailsByWithBalance(boolean withBalance, List<SpiAccountDetails> details) {
        if (!withBalance) {
            details.forEach(SpiAccountDetails::emptyBalances);
        }

        return details;
    }
}
