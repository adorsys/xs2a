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

package de.adorsys.aspsp.xs2a.service.mapper;

import com.google.gson.Gson;
import de.adorsys.aspsp.xs2a.consent.api.AccountInfo;
import de.adorsys.aspsp.xs2a.consent.api.ais.AisAccountAccessInfo;
import de.adorsys.aspsp.xs2a.consent.api.ais.AisConsentRequest;
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.consent.AccountConsent;
import de.adorsys.aspsp.xs2a.domain.consent.ConsentStatus;
import de.adorsys.aspsp.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiCreateConsentRequest;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ConsentMapperTest {
    private final String CREATE_CONSENT_REQ_JSON_PATH = "/json/CreateAccountConsentReqTest.json";
    private final String SPI_ACCOUNT_CONSENT_REQ_JSON_PATH = "/json/MapGetAccountConsentTest.json";
    private final Charset UTF_8 = Charset.forName("utf-8");
    private final String PSU_ID = "12345";
    private final String TPP_ID = "This is a test TppId";

    @Autowired
    private ConsentMapper consentMapper;

    @Test
    public void mapToAisConsentRequest() throws IOException {
        //Given:
        String aicConRequestJson = IOUtils.resourceToString(CREATE_CONSENT_REQ_JSON_PATH, UTF_8);
        CreateConsentReq donorRequest = new Gson().fromJson(aicConRequestJson, CreateConsentReq.class);
        AisConsentRequest expectedResult = getAisConsentReq(donorRequest, PSU_ID, TPP_ID);
        //When:
        AisConsentRequest mapedResult = consentMapper.mapToAisConsentRequest(donorRequest, PSU_ID, TPP_ID);
        assertThat(mapedResult).isEqualTo(expectedResult);
    }

    private AisConsentRequest getAisConsentReq(CreateConsentReq consentReq, String psuId, String tppId) {
        AisConsentRequest req = new AisConsentRequest();
        req.setPsuId(psuId);
        req.setTppId(tppId);
        req.setCombinedServiceIndicator(consentReq.isCombinedServiceIndicator());
        req.setRecurringIndicator(consentReq.isRecurringIndicator());
        req.setValidUntil(consentReq.getValidUntil().toInstant());
        req.setTppRedirectPreferred(false);
        req.setFrequencyPerDay(consentReq.getFrequencyPerDay());
        AisAccountAccessInfo info = new AisAccountAccessInfo();

        info.setAccounts(getAccountInfo(consentReq.getAccess().getAccounts()));
        info.setBalances(getAccountInfo(consentReq.getAccess().getBalances()));
        info.setTransactions(getAccountInfo(consentReq.getAccess().getTransactions()));
        info.setAvailableAccounts(Optional.ofNullable(req.getAccess()).map(AisAccountAccessInfo::getAvailableAccounts).orElse(null));
        info.setAllPsd2(Optional.ofNullable(req.getAccess()).map(AisAccountAccessInfo::getAllPsd2).orElse(null));
        req.setAccess(info);

        return req;
    }

    private List<AccountInfo> getAccountInfo(List<AccountReference> references) {

        return Optional.ofNullable(references)
                   .map(ref -> ref.stream()
                                   .map(ar -> {
                                       AccountInfo ai = new AccountInfo();
                                       ai.setIban(ar.getIban());
                                       ai.setCurrency(getCurrency(ar));
                                       return ai;
                                   })
                                   .collect(Collectors.toList()))
                   .orElse(Collections.emptyList());
    }

    @Test
    public void mapGetAccountConsentStatusById() {
        //Given:
        SpiTransactionStatus expectedTransactionStatus = SpiTransactionStatus.ACCP;

        //When:
        TransactionStatus actualTransactionStatus = consentMapper.mapToTransactionStatus(expectedTransactionStatus);

        //Then:
        assertThat(expectedTransactionStatus.name()).isEqualTo(actualTransactionStatus.name());
    }

    @Test
    public void mapSpiCreateConsentRequest() throws IOException {
        //Given:
        String aicRequestJson = IOUtils.resourceToString(CREATE_CONSENT_REQ_JSON_PATH, UTF_8);
        CreateConsentReq donorRequest = new Gson().fromJson(aicRequestJson, CreateConsentReq.class);
        SpiCreateConsentRequest expectedRequest = new Gson().fromJson(aicRequestJson, SpiCreateConsentRequest.class);

        //When:
        SpiCreateConsentRequest actualRequest = consentMapper.mapToSpiCreateConsentRequest(donorRequest);

        //Then:
        assertThat(actualRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void mapGetAccountConsent() throws IOException {
        //Given:
        String accountConsentJson = IOUtils.resourceToString(SPI_ACCOUNT_CONSENT_REQ_JSON_PATH, UTF_8);
        SpiAccountConsent donorAccountConsent = new Gson().fromJson(accountConsentJson, SpiAccountConsent.class);

        //When:
        assertNotNull(donorAccountConsent);
        AccountConsent actualAccountConsent = consentMapper.mapToAccountConsent(donorAccountConsent);

        //Then:
        assertThat(actualAccountConsent.getId()).isEqualTo("3dc3d5b3-7023-4848-9853-f5400a64e80f");
        assertThat(actualAccountConsent.getAccess().getBalances().get(0).getIban()).isEqualTo("DE2310010010123456789");
        assertThat(actualAccountConsent.getAccess().getBalances().get(1).getIban()).isEqualTo("DE2310010010123456790");
        assertThat(actualAccountConsent.getAccess().getBalances().get(1).getCurrency().getCurrencyCode()).isEqualTo("USD");
        assertThat(actualAccountConsent.getAccess().getBalances().get(2).getIban()).isEqualTo("DE2310010010123456788");
        assertThat(actualAccountConsent.getAccess().getTransactions().get(0).getIban()).isEqualTo("DE2310010010123456789");
        assertThat(actualAccountConsent.getAccess().getTransactions().get(1).getMaskedPan()).isEqualTo("123456xxxxxx1234");
        assertThat(actualAccountConsent.isRecurringIndicator()).isTrue();
        assertThat(actualAccountConsent.getValidUntil()).isEqualTo("2017-11-01");
        assertThat(actualAccountConsent.getFrequencyPerDay()).isEqualTo(4);
        assertThat(actualAccountConsent.getLastActionDate()).isEqualTo("2017-11-01");
        assertThat(actualAccountConsent.getConsentStatus()).isEqualTo(ConsentStatus.VALID);
    }

    private String getCurrency(AccountReference reference) {
        return Optional.ofNullable(reference.getCurrency())
                   .map(Currency::getCurrencyCode)
                   .orElse(null);
    }
}
