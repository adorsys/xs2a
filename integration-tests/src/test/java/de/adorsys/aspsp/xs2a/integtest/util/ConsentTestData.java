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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountReference;
import de.adorsys.aspsp.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.aspsp.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.aspsp.xs2a.integtest.config.rest.consent.AisConsentRemoteUrls;
import de.adorsys.aspsp.xs2a.integtest.config.rest.consent.ConsentMapper;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiConsentStatus;
import de.adorsys.psd2.consent.api.ais.CreateAisConsentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.NotNull;
import java.io.IOException;
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
    private ObjectMapper mapper;

    @Autowired
    public ConsentTestData(RestTemplate consentRestTemplate, AisConsentRemoteUrls remoteAisConsentUrls, ConsentMapper consentMapper){
        this.consentRestTemplate = consentRestTemplate;
        this.remoteAisConsentUrls = remoteAisConsentUrls;
        this.consentMapper = consentMapper;
    }

    private CreateConsentReq getConsentReq(int tppFrequency, LocalDate expire, boolean recurring, Xs2aAccountAccess accountAccess) {
            CreateConsentReq consent = new CreateConsentReq();

            consent.setFrequencyPerDay(tppFrequency);
            consent.setValidUntil(expire);
            consent.setAccess(accountAccess);
            consent.setRecurringIndicator(recurring);
            consent.setCombinedServiceIndicator(false);
            return consent;
        }

    private String createConsent(CreateConsentReq request, String psuId, String tppId) throws IOException {
        String consentId = consentRestTemplate.postForEntity(remoteAisConsentUrls.createAisConsent(), consentMapper.mapToAisConsentRequest(request, psuId, tppId), String.class).getBody();

        CreateAisConsentResponse consentResponse;
        consentResponse = mapper.readValue(consentId, CreateAisConsentResponse.class);

        return consentResponse.getConsentId();
    }

    private Xs2aAccountAccess createAccountAccessTestData(List<Xs2aAccountReference> accounts, List<Xs2aAccountReference> balances, List<Xs2aAccountReference> transactions){
        return new Xs2aAccountAccess(accounts, balances, transactions, null, null);
    }

    private List<Xs2aAccountReference> createAccountReferenceListTestData () {
        final String IBAN = "DE52500105173911841934";
        final String CURRENCY = "EUR";

        Xs2aAccountReference xs2aAccountReference1 = new Xs2aAccountReference();
        xs2aAccountReference1.setIban(IBAN);
        xs2aAccountReference1.setCurrency(Currency.getInstance(CURRENCY));

        return Collections.singletonList(xs2aAccountReference1);
    }

    public String createConsentTestData () throws IOException {
        final String PSU_ID =  "d9e71419-24e4-4c5a-8d93-fcc23153aaff";
        final String TTP_ID = "tpp01";
        final int VALID_UNTIL = 30;

        List <Xs2aAccountReference> accounts = createAccountReferenceListTestData();
        List <Xs2aAccountReference> balances = createAccountReferenceListTestData();
        List <Xs2aAccountReference> transactions = createAccountReferenceListTestData();
        Xs2aAccountAccess accountAccess = createAccountAccessTestData(accounts, balances, transactions);
        CreateConsentReq consentReq = getConsentReq(1, LocalDate.now().plusDays(VALID_UNTIL),false, accountAccess);
        return createConsent(consentReq,PSU_ID, TTP_ID ) ;
    }

    public void changeAccountConsentStatus (@NotNull String consentId, SpiConsentStatus consentStatus) {
        consentRestTemplate.put(remoteAisConsentUrls.updateAisConsentStatus(), null, consentId, consentStatus.name());
    }
}

