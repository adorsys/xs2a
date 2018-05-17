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
import de.adorsys.aspsp.xs2a.spi.test.data.AccountMockData;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.stream.Collectors;


@Component
@AllArgsConstructor
@Profile("mockspi")
public class AccountSpiImpl implements AccountSpi {
    private final RemoteSpiUrls remoteSpiUrls;
    private final RestTemplate restTemplate;

    @Override
    public List<SpiAccountDetails> readAccounts(String consentId, boolean withBalance, boolean psuInvolved) {
        String url = remoteSpiUrls.getUrl("getAllAccounts");

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        builder.queryParam("consent-id", consentId);
        builder.queryParam("withBalance", withBalance);

        ResponseEntity<SpiAccountDetails[]> response = restTemplate.getForEntity(builder.build().encode().toUri(), SpiAccountDetails[].class);
        return Arrays.asList(response.getBody());
    }

    @Override
    public List<SpiBalances> readBalances(String accountId, boolean psuInvolved) {
        String getBalanceUrl = remoteSpiUrls.getUrl("getAccountBalances");
        ResponseEntity<List<SpiBalances>> response = restTemplate.exchange(getBalanceUrl, HttpMethod.GET, null,
            new ParameterizedTypeReference<List<SpiBalances>>() {
            }, accountId);

        return response.getBody();
    }

    @Override
    public List<SpiTransaction> readTransactionsByPeriod(String accountId, Date dateFrom, Date dateTo, boolean psuInvolved) {
        List<SpiTransaction> spiTransactions = AccountMockData.getSpiTransactions();

        List<SpiTransaction> validSpiTransactions = filterValidTransactionsByAccountId(spiTransactions, accountId);
        List<SpiTransaction> transactionsFilteredByPeriod = filterTransactionsByPeriod(validSpiTransactions, dateFrom, dateTo);

        return Collections.unmodifiableList(transactionsFilteredByPeriod);
    }

    @Override
    public List<SpiTransaction> readTransactionsById(String accountId, String transactionId, boolean psuInvolved) {
        List<SpiTransaction> spiTransactions = AccountMockData.getSpiTransactions();

        List<SpiTransaction> validSpiTransactions = filterValidTransactionsByAccountId(spiTransactions, accountId);
        List<SpiTransaction> filteredSpiTransactions = filterValidTransactionsByTransactionId(validSpiTransactions, transactionId);

        return Collections.unmodifiableList(filteredSpiTransactions);
    }

    @Override
    public SpiAccountDetails readAccountDetails(String accountId, boolean withBalance, boolean psuInvolved) {
        String url = remoteSpiUrls.getUrl("getAccountById");
        SpiAccountDetails spiAccountDetails = restTemplate.getForObject(url, SpiAccountDetails.class, accountId);

        return Optional.ofNullable(spiAccountDetails)
                   .map(ad -> new SpiAccountDetails(
                       ad.getId(), ad.getIban(), ad.getBban(),
                       ad.getMaskedPan(), ad.getMaskedPan(), ad.getMsisdn(),
                       ad.getCurrency(), ad.getName(), ad.getAccountType(),
                       ad.getCashSpiAccountType(), ad.getBic(),
                       withBalance ? ad.getBalances() : null)
                   ).orElse(null);
    }

    @Override
    public SpiAccountDetails readAccountDetailsByIbanAndCurrency(String iban, Currency currency) {
        return restTemplate.getForObject(remoteSpiUrls.getUrl("getAccountByIban"), SpiAccountDetails.class, iban, currency);
    }

    private SpiTransaction[] getFilteredPendingTransactions(List<SpiTransaction> spiTransactions) { //NOPMD TODO review and check PMD assertion
        return spiTransactions.parallelStream()
                   .filter(this::isPendingTransaction)
                   .toArray(SpiTransaction[]::new);
    }

    private SpiTransaction[] getFilteredBookedTransactions(List<SpiTransaction> spiTransactions) { //NOPMD TODO review and check PMD assertion
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
