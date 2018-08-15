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

package de.adorsys.aspsp.xs2a.integtest.util;

import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.consent.AccountAccess;
import de.adorsys.aspsp.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.aspsp.xs2a.integtest.config.rest.consent.AisConsentRemoteUrls;
import de.adorsys.aspsp.xs2a.integtest.config.rest.consent.ConsentMapper;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

@Component
public class ConsentTestData {


    @Qualifier("consentRestTemplate")
    private final RestTemplate consentRestTemplate;
    private final AisConsentRemoteUrls remoteAisConsentUrls;
    private final ConsentMapper consentMapper;

    @Autowired
    public ConsentTestData(RestTemplate consentRestTemplate, AisConsentRemoteUrls remoteAisConsentUrls, ConsentMapper consentMapper){
        this.consentRestTemplate = consentRestTemplate;
        this.remoteAisConsentUrls = remoteAisConsentUrls;
        this.consentMapper = consentMapper;
    }

    private CreateConsentReq getConsentReq(int tppFrequency, LocalDate expire, boolean recurring, AccountAccess accountAccess) {
            CreateConsentReq consent = new CreateConsentReq();

            consent.setFrequencyPerDay(tppFrequency);
            consent.setValidUntil(expire);
            consent.setAccess(accountAccess);
            consent.setRecurringIndicator(recurring);
            consent.setCombinedServiceIndicator(false);
            return consent;
        }

    private String createConsent(CreateConsentReq request, String psuId, String tppId) {
        return consentRestTemplate.postForEntity(remoteAisConsentUrls.createAisConsent(), consentMapper.mapToAisConsentRequest(request, psuId, tppId), String.class).getBody();
    }


    private AccountAccess createAccountAccessTestData(List<AccountReference> accounts, List<AccountReference> balances, List<AccountReference> transactions){
        return new AccountAccess(accounts, balances, transactions, null, null);

    }

    private List<AccountReference> createAccountReferenceListTestData () {
        AccountReference accountReference1 = new AccountReference();
        accountReference1.setIban("DE52500105173911841934");
        accountReference1.setCurrency(Currency.getInstance("EUR"));

        return Collections.singletonList(accountReference1);
    }

    public String createConsentTestData () {
        List <AccountReference> accounts = createAccountReferenceListTestData();
        List <AccountReference> balances = createAccountReferenceListTestData();
        List <AccountReference> transactions = createAccountReferenceListTestData();
        AccountAccess accountAccess = createAccountAccessTestData(accounts, balances, transactions);
        CreateConsentReq consentReq = getConsentReq(1, LocalDate.parse("2020-11-10"),false, accountAccess);
        return createConsent(consentReq,"d9e71419-24e4-4c5a-8d93-fcc23153aaff", "tpp01" );
    }

    public void changeAccountConsentStatus (String consentId, SpiConsentStatus consentStatus) {
        consentRestTemplate.put(remoteAisConsentUrls.updateAisConsentStatus(), null, consentId, consentStatus.name());
    }
}

