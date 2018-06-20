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

import de.adorsys.aspsp.xs2a.spi.config.AspspRemoteUrls;
import de.adorsys.aspsp.xs2a.spi.domain.ObjectHolder;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiBalances;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiTransaction;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
@Profile("mockspi")
public class AccountSpiImpl implements AccountSpi {
    private final AspspRemoteUrls remoteSpiUrls;
    @Qualifier("aspspRestTemplate")
    private final RestTemplate aspspRestTemplate;

    /**
     * Queries ASPSP to (GET) List of AccountDetails by IBAN
     *
     * @param iban String representation of Account IBAN
     * @return List of account details
     */
    @Override
    public List<SpiAccountDetails> readAccountDetailsByIban(String iban) {
        return Optional.ofNullable(aspspRestTemplate.exchange(
            remoteSpiUrls.getAccountDetailsByIban(), HttpMethod.GET, new HttpEntity<>(null), new ParameterizedTypeReference<List<SpiAccountDetails>>() {
            }, iban).getBody())
                   .orElse(Collections.emptyList());
    }

    /**
     * Queries ASPSP to (GET) a list of balances of a sertain account by its primary id
     *
     * @param accountId String representation of ASPSP account identifier
     * @return List of balances
     */
    @Override
    public List<SpiBalances> readBalances(String accountId) {
        return Optional.ofNullable(aspspRestTemplate.exchange(
            remoteSpiUrls.getBalancesByAccountId(), HttpMethod.GET, null, new ParameterizedTypeReference<List<SpiBalances>>() {
            }, accountId).getBody())
                   .orElse(Collections.emptyList());
    }

    /**
     * Queries (POST) ASPSP to save a new Transaction, as a response receives a string representing the ASPSP primary identifier of saved transaction
     *
     * @param transaction Prepared at xs2a transaction object
     * @return String transactionId
     */
    @Override
    public String saveTransaction(SpiTransaction transaction) {
        return aspspRestTemplate.postForEntity(remoteSpiUrls.createTransaction(), transaction, String.class).getBody();
    }

    /**
     * Queries ASPSP to get List of transactions dependant on period and accountId
     *
     * @param accountId String representation of ASPSP account primary identifier
     * @param dateFrom  Date representing the beginning of the search period
     * @param dateTo    Date representing the ending of the search period
     * @return List of transactions
     */
    @Override
    public List<SpiTransaction> readTransactionsByPeriod(String accountId, LocalDate dateFrom, LocalDate dateTo) {
        Map<String, String> uriParams = new ObjectHolder<String, String>()
                                            .addValue("account-id", accountId)
                                            .getValues();

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(remoteSpiUrls.readTransactionsByPeriod())
                                           .queryParam("dateFrom", dateFrom)
                                           .queryParam("dateTo", dateTo);

        return aspspRestTemplate.exchange(
            builder.buildAndExpand(uriParams).toUriString(), HttpMethod.GET, null, new ParameterizedTypeReference<List<SpiTransaction>>() {
            }).getBody();
    }

    /**
     * Queries ASPSP to (GET) transaction by its primary identifier and account identifier
     *
     * @param transactionId String representation of ASPSP primary identifier of transaction
     * @param accountId     String representation of ASPSP account primary identifier
     * @return Transaction
     */
    @Override
    public Optional<SpiTransaction> readTransactionsById(String transactionId, String accountId) {
        return Optional.ofNullable(aspspRestTemplate.getForObject(remoteSpiUrls.readTransactionById(), SpiTransaction.class, transactionId, accountId));
    }

    /**
     * Queries ASPSP to (GET) AccountDetails by primary ASPSP account identifier
     *
     * @param accountId String representation of ASPSP account primary identifier
     * @return Account details
     */
    @Override
    public SpiAccountDetails readAccountDetails(String accountId) {
        return aspspRestTemplate.getForObject(remoteSpiUrls.getAccountDetailsById(), SpiAccountDetails.class, accountId);
    }

    /**
     * Queries ASPSP to (GET) a list of account details of a certain PSU by identifier
     *
     * @param psuId String representing ASPSP`s primary identifier of PSU
     * @return List of account details
     */
    @Override
    public List<SpiAccountDetails> readAccountsByPsuId(String psuId) {
        return Optional.ofNullable(aspspRestTemplate.exchange(
            remoteSpiUrls.getAccountDetailsByPsuId(), HttpMethod.GET, null, new ParameterizedTypeReference<List<SpiAccountDetails>>() {
            }, psuId).getBody())
                   .orElse(Collections.emptyList());
    }

    /**
     * Queries ASPSP to (GET) list of account details with certain account IBANS
     *
     * @param ibans a collection of Strings representing account IBANS
     * @return List of account details
     */
    @Override
    public List<SpiAccountDetails> readAccountDetailsByIbans(Collection<String> ibans) {
        return ibans.stream()
                   .map(this::readAccountDetailsByIban)
                   .flatMap(Collection::stream)
                   .collect(Collectors.toList());
    }
}
