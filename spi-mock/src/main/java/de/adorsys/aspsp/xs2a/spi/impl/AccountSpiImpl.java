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

import de.adorsys.aspsp.xs2a.spi.config.RemoteSpiUrls;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiBalances;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiTransaction;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;


@Component
@AllArgsConstructor
@Profile("mockspi")
public class AccountSpiImpl implements AccountSpi {
    private RemoteSpiUrls remoteSpiUrls;
    private RestTemplate restTemplate;

    @Override
    public List<SpiAccountDetails> readAccountDetailsByIban(String iban) {
        return Optional.ofNullable(restTemplate.exchange(
            remoteSpiUrls.getAccountDetailsByIban(), HttpMethod.GET, new HttpEntity<>(null), new ParameterizedTypeReference<List<SpiAccountDetails>>() {
            }, iban).getBody())
                   .orElse(Collections.emptyList());
    }

    @Override
    public List<SpiBalances> readBalances(String accountId) {
        return Optional.ofNullable(restTemplate.exchange(
            remoteSpiUrls.getBalancesByAccountId(), HttpMethod.GET, null, new ParameterizedTypeReference<List<SpiBalances>>() {
            }, accountId).getBody())
                   .orElse(Collections.emptyList());
    }

    @Override
    public List<SpiTransaction> readTransactionsByPeriod(String accountId, Date dateFrom, Date dateTo) {
        List<SpiTransaction> spiTransactions = new ArrayList<>();

        List<SpiTransaction> validSpiTransactions = filterValidTransactionsByAccountId(spiTransactions, accountId);
        List<SpiTransaction> transactionsFilteredByPeriod = filterTransactionsByPeriod(validSpiTransactions, dateFrom, dateTo);

        return Collections.unmodifiableList(transactionsFilteredByPeriod);
    }

    @Override
    public List<SpiTransaction> readTransactionsById(String accountId, String transactionId) {
        List<SpiTransaction> spiTransactions = new ArrayList<>();

        List<SpiTransaction> validSpiTransactions = filterValidTransactionsByAccountId(spiTransactions, accountId);
        List<SpiTransaction> filteredSpiTransactions = filterValidTransactionsByTransactionId(validSpiTransactions, transactionId);

        return Collections.unmodifiableList(filteredSpiTransactions);
    }

    @Override
    public SpiAccountDetails readAccountDetails(String accountId) {
        return restTemplate.getForObject(remoteSpiUrls.getAccountDetailsById(), SpiAccountDetails.class, accountId);
    }

    @Override
    public List<SpiAccountDetails> readAccountsByPsuId(String psuId) {
        return Optional.ofNullable(restTemplate.exchange(
            remoteSpiUrls.getAccountDetailsByPsuId(), HttpMethod.GET, null, new ParameterizedTypeReference<List<SpiAccountDetails>>() {
            }, psuId).getBody())
                   .orElse(Collections.emptyList());
    }

    @Override
    public List<SpiAccountDetails> readAccountDetailsByIbans(Collection<String> ibans) {
        return ibans.stream()
                   .map(this::readAccountDetailsByIban)
                   .flatMap(Collection::stream)
                   .collect(Collectors.toList());
    }

    private SpiTransaction[] getFilteredPendingTransactions(List<SpiTransaction> spiTransactions) { //NOPMD TODO review and check PMD assertion https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/74
        return spiTransactions.parallelStream()
                   .filter(this::isPendingTransaction)
                   .toArray(SpiTransaction[]::new);
    }

    private SpiTransaction[] getFilteredBookedTransactions(List<SpiTransaction> spiTransactions) { //NOPMD TODO review and check PMD assertion https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/74
        return spiTransactions.parallelStream()
                   .filter(transaction -> !isPendingTransaction(transaction))
                   .toArray(SpiTransaction[]::new);
    }

    private boolean isPendingTransaction(SpiTransaction spiTransaction) {
        return spiTransaction.getBookingDate() == null;
    }

    private List<SpiTransaction> filterTransactionsByPeriod(List<SpiTransaction> spiTransactions, Date dateFrom, Date dateTo) {
        return spiTransactions.parallelStream()
                   .filter(transaction -> isDateInTimeFrame(transaction.getBookingDate(), dateFrom, dateTo))
                   .collect(Collectors.toList());
    }

    private static boolean isDateInTimeFrame(Date currentDate, Date dateFrom, Date dateTo) {
        return currentDate != null && currentDate.after(dateFrom) && currentDate.before(dateTo);
    }

    private List<SpiTransaction> filterValidTransactionsByAccountId(List<SpiTransaction> spiTransactions, String accountId) {
        return spiTransactions.parallelStream()
                   .filter(transaction -> transactionIsValid(transaction, accountId))
                   .collect(Collectors.toList());
    }

    private List<SpiTransaction> filterValidTransactionsByTransactionId(List<SpiTransaction> spiTransactions, String transactionId) {
        return spiTransactions.parallelStream()
                   .filter(transaction -> transactionId.equals(transaction.getTransactionId()))
                   .collect(Collectors.toList());
    }

    private boolean transactionIsValid(SpiTransaction spiTransaction, String accountId) {

        boolean isCreditorAccountValid = Optional.ofNullable(spiTransaction.getCreditorAccount())
                                             .map(creditorAccount -> creditorAccount.getIban().trim().equals(accountId))
                                             .orElse(false);

        boolean isDebtorAccountValid = Optional.ofNullable(spiTransaction.getDebtorAccount())
                                           .map(debtorAccount -> debtorAccount.getIban().trim().equals(accountId))
                                           .orElse(false);

        return isCreditorAccountValid || isDebtorAccountValid;
    }
}
