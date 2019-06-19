/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.aspsp.xs2a.spi.rest.client;

import de.adorsys.aspsp.xs2a.spi.config.rest.AspspRemoteUrls;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountBalance;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiTransaction;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountRestClient {

    private final AspspRemoteUrls remoteSpiUrls;
    @Qualifier("aspspRestTemplate")
    private final RestTemplate aspspRestTemplate;

    public SpiAccountDetails getAccountDetailsById(String resourceId) {
        return aspspRestTemplate.getForObject(remoteSpiUrls.getAccountDetailsById(), SpiAccountDetails.class, resourceId);
    }

    public List<SpiAccountDetails> getAccountDetailsByPsuId(String psuId) {
        return aspspRestTemplate.exchange(
            remoteSpiUrls.getAccountDetailsByPsuId(),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<SpiAccountDetails>>() {
            },
            psuId
        ).getBody();
    }

    // TODO don't use IBAN as an account identifier https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/440
    public List<SpiAccountDetails> getAccountDetailsByIban(String iban) {
        return aspspRestTemplate.exchange(
            remoteSpiUrls.getAccountDetailsByIban(),
            HttpMethod.GET,
            new HttpEntity<>(null), new ParameterizedTypeReference<List<SpiAccountDetails>>() {
            },
            iban
        ).getBody();
    }

    public SpiTransaction getTransactionByIdAndResourceId(String transactionId, String resourceId) {
        return aspspRestTemplate.getForObject(remoteSpiUrls.readTransactionById(), SpiTransaction.class, transactionId, resourceId);
    }

    public List<SpiTransaction> getTransactionsByResourceIdAndPeriod(String resourceId, LocalDate dateFrom, LocalDate dateTo) {
        return aspspRestTemplate.exchange(
            remoteSpiUrls.readTransactionsByPeriod(resourceId, dateFrom, dateTo),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<SpiTransaction>>() {
            }
        ).getBody();
    }

    public List<SpiAccountBalance> getBalancesByResourceId(String resourceId) {
        return aspspRestTemplate.exchange(
            remoteSpiUrls.getBalancesByAccountId(),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<SpiAccountBalance>>() {
            },
            resourceId
        ).getBody();
    }

}
