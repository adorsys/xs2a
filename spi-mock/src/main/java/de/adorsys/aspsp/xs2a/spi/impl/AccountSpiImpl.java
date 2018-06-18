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
import de.adorsys.aspsp.xs2a.spi.domain.ObjectHolder;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiBalances;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiBookingStatus;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiTransaction;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
@Profile("mockspi")
public class AccountSpiImpl implements AccountSpi {
    private final RemoteSpiUrls remoteSpiUrls;
    private final RestTemplate restTemplate;

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
    public String saveTransaction(SpiTransaction transaction) {
        return restTemplate.postForEntity(remoteSpiUrls.createTransaction(), transaction, String.class).getBody();
    }

    @Override
    public List<SpiTransaction> readTransactionsByPeriod(String iban, Currency currency, LocalDate dateFrom, LocalDate dateTo, SpiBookingStatus bookingStatus) {
        return getTransactionsByPeriod(iban, currency, dateFrom, dateTo, bookingStatus);
    }

    private List<SpiTransaction> getTransactionsByPeriod(String iban, Currency currency, LocalDate dateFrom, LocalDate dateTo, SpiBookingStatus bookingStatus) {
        Map<String, String> uriParams = new ObjectHolder<String, String>()
                                            .addValue("iban", iban)
                                            .addValue("currency", currency.toString())
                                            .getValues();

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(remoteSpiUrls.readTransactionsByPeriod())
                                           .queryParam("dateFrom", dateFrom)
                                           .queryParam("dateTo", dateTo);

        List<SpiTransaction> spiTransactions = restTemplate.exchange(
            builder.buildAndExpand(uriParams).toUriString(), HttpMethod.GET, null, new ParameterizedTypeReference<List<SpiTransaction>>() {
            }).getBody();

        Predicate<SpiTransaction> pendingTransactionPredicate = SpiTransaction::isPendingTransaction;
        if (SpiBookingStatus.PENDING == bookingStatus) {
            return getFilteredTransactions(spiTransactions, pendingTransactionPredicate);
        } else if (SpiBookingStatus.BOOKED == bookingStatus) {
            return getFilteredTransactions(spiTransactions, pendingTransactionPredicate.negate());
        }
        return spiTransactions;
    }

    @Override
    public SpiTransaction readTransactionsById(String transactionId) {
        return restTemplate.getForObject(remoteSpiUrls.readTransactionById(), SpiTransaction.class, transactionId);
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

    private List<SpiTransaction> getFilteredTransactions(List<SpiTransaction> spiTransactions, Predicate<SpiTransaction> predicate) {
        return Optional.ofNullable(spiTransactions)
                   .map(t->t.stream()
                   .filter(predicate)
                   .collect(Collectors.toList()))
            .orElse(Collections.emptyList());
    }
}
