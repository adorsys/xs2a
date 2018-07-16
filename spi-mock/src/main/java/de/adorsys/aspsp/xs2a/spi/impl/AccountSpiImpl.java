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
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiTransaction;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
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

@Component
@AllArgsConstructor
@Profile("mockspi")
public class AccountSpiImpl implements AccountSpi {
    private final AspspRemoteUrls remoteSpiUrls;
    @Qualifier("aspspRestTemplate")
    private final RestTemplate aspspRestTemplate;

    /**
     * For detailed description see {@link AccountSpi#readAccountDetailsByIban(String)}
     */
    @Override
    public List<SpiAccountDetails> readAccountDetailsByIban(String iban) {
        return Optional.ofNullable(aspspRestTemplate.exchange(
            remoteSpiUrls.getAccountDetailsByIban(), HttpMethod.GET, new HttpEntity<>(null), new ParameterizedTypeReference<List<SpiAccountDetails>>() {
            }, iban).getBody())
                   .orElse(Collections.emptyList());
    }

    /**
     * For detailed description see {@link AccountSpi#readTransactionsByPeriod(String, LocalDate, LocalDate)}
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
     * For detailed description see {@link AccountSpi#readTransactionById(String, String)}
     */
    @Override
    public Optional<SpiTransaction> readTransactionById(String transactionId, String accountId) {
        return Optional.ofNullable(aspspRestTemplate.getForObject(remoteSpiUrls.readTransactionById(), SpiTransaction.class, transactionId, accountId));
    }

    /**
     * For detailed description see {@link AccountSpi#readAccountDetails(String)}
     */
    @Override
    public SpiAccountDetails readAccountDetails(String accountId) {
        return aspspRestTemplate.getForObject(remoteSpiUrls.getAccountDetailsById(), SpiAccountDetails.class, accountId);
    }

    /**
     * For detailed description see {@link AccountSpi#readAccountsByPsuId(String)}
     */
    @Override
    public List<SpiAccountDetails> readAccountsByPsuId(String psuId) {
        return Optional.ofNullable(aspspRestTemplate.exchange(
            remoteSpiUrls.getAccountDetailsByPsuId(), HttpMethod.GET, null, new ParameterizedTypeReference<List<SpiAccountDetails>>() {
            }, psuId).getBody())
                   .orElse(Collections.emptyList());
    }

    /**
     * For detailed description see {@link AccountSpi#readAccountDetailsByIbans(Collection)}
     */
    @Override
    public List<SpiAccountDetails> readAccountDetailsByIbans(Collection<String> ibans) {
        List<SpiAccountDetails> accountDetails = new ArrayList<>();
        for (String iban : ibans) {
            List<SpiAccountDetails> det = readAccountDetailsByIban(iban);
            if (CollectionUtils.isEmpty(det)) {
                return Collections.emptyList();
            }
            accountDetails.addAll(det);
        }

        return accountDetails;
    }

    /**
     * For detailed description see {@link AccountSpi#readPsuAllowedPaymentProductList(SpiAccountReference)}
     */
    @Override
    public List<String> readPsuAllowedPaymentProductList(SpiAccountReference reference) {
        return Optional.ofNullable(aspspRestTemplate.exchange(remoteSpiUrls.getAllowedPaymentProducts(), HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {
        }, reference.getIban()).getBody())
                   .orElse(Collections.emptyList());
    }
}
