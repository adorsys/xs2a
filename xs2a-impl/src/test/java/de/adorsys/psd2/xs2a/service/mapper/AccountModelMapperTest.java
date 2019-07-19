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

package de.adorsys.psd2.xs2a.service.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.pis.PurposeCode;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.*;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountDetailsHolder;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountListHolder;
import de.adorsys.psd2.xs2a.domain.account.Xs2aBalancesReport;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTransactionsReport;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import de.adorsys.psd2.xs2a.web.mapper.HrefLinkMapper;
import de.adorsys.psd2.xs2a.web.mapper.PurposeCodeMapper;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {AccountModelMapperImpl.class, AccountModelMapperTest.TestConfiguration.class})
public class AccountModelMapperTest {

    private static final OffsetDateTime OFFSET_DATE_TIME = OffsetDateTime.now();
    private static final String BYTE_ARRAY_IN_STRING = "000000000000000=";
    private static final String XS2A_LINKS_JSON_PATH = "json/service/mapper/AccountModelMapper-xs2a-links.json";
    private static final String LINKS_JSON_PATH = "json/service/mapper/AccountModelMapper-links.json";
    private static final String XS2A_AMOUNT_JSON_PATH = "json/service/mapper/AccountModelMapper-xs2a-amount.json";
    private static final String AMOUNT_JSON_PATH = "json/service/mapper/AccountModelMapper-amount.json";

    @Autowired
    private AccountModelMapper mapper;

    @Autowired
    private HrefLinkMapper mockedHrefLinkMapper;
    @Autowired
    private AmountModelMapper mockedAmountModelMapper;
    @Autowired
    private PurposeCodeMapper mockedPurposeCodeMapper;

    private JsonReader jsonReader = new JsonReader();

    @After
    public void resetMocks() {
        // Resetting is necessary because these mocks are injected into the mapper as singleton beans
        // and are not being recreated after each test
        Mockito.reset(mockedHrefLinkMapper, mockedAmountModelMapper, mockedPurposeCodeMapper);
    }

    @Test
    public void mapToAccountList() {
        Map<String, HrefType> links = jsonReader.getObjectFromFile(LINKS_JSON_PATH, new TypeReference<Map<String, HrefType>>() {
        });
        Links xs2aLinks = jsonReader.getObjectFromFile(XS2A_LINKS_JSON_PATH, Links.class);
        when(mockedHrefLinkMapper.mapToLinksMap(xs2aLinks)).thenReturn(links);

        Xs2aAccountListHolder xs2aAccountListHolder = jsonReader.getObjectFromFile("json/service/mapper/AccountModelMapper-xs2a-account-list-holder.json", Xs2aAccountListHolder.class);
        AccountList actualAccountList = mapper.mapToAccountList(xs2aAccountListHolder);
        actualAccountList.getAccounts().get(0).getBalances().get(0).setLastChangeDateTime(OFFSET_DATE_TIME);

        AccountList expectedAccountList = jsonReader.getObjectFromFile("json/service/mapper/AccountModelMapper-account-list-expected.json", AccountList.class);
        expectedAccountList.getAccounts().get(0).getBalances().get(0).setLastChangeDateTime(OFFSET_DATE_TIME);

        assertLinks(expectedAccountList.getAccounts().get(0).getLinks(), actualAccountList.getAccounts().get(0).getLinks());

        expectedAccountList.getAccounts().get(0).setLinks(actualAccountList.getAccounts().get(0).getLinks());
        assertEquals(expectedAccountList, actualAccountList);
    }

    @Test
    public void mapToAccountDetails() {
        Map<String, HrefType> links = jsonReader.getObjectFromFile(LINKS_JSON_PATH, new TypeReference<Map<String, HrefType>>() {
        });
        Links xs2aLinks = jsonReader.getObjectFromFile(XS2A_LINKS_JSON_PATH, Links.class);
        when(mockedHrefLinkMapper.mapToLinksMap(xs2aLinks)).thenReturn(links);

        Xs2aAccountDetailsHolder xs2aAccountDetailsHolder = jsonReader.getObjectFromFile("json/service/mapper/AccountModelMapper-xs2a-account-details-holder.json", Xs2aAccountDetailsHolder.class);
        AccountDetails actualAccountDetails = mapper.mapToAccountDetails(xs2aAccountDetailsHolder);

        AccountDetails expectedAccountDetails = jsonReader.getObjectFromFile("json/service/mapper/AccountModelMapper-account-details-expected.json", AccountDetails.class);

        assertLinks(expectedAccountDetails.getLinks(), actualAccountDetails.getLinks());

        expectedAccountDetails.setLinks(actualAccountDetails.getLinks());
        assertEquals(expectedAccountDetails, actualAccountDetails);
    }

    @Test
    public void mapToAccountReference_success() {
        AccountReference accountReference = jsonReader.getObjectFromFile("json/service/mapper/AccountModelMapper-account-reference.json", AccountReference.class);
        de.adorsys.psd2.model.AccountReference actualAccountReference = mapper.mapToAccountReference(accountReference);

        de.adorsys.psd2.model.AccountReference expectedAccountReference = jsonReader.getObjectFromFile("json/service/mapper/AccountModelMapper-account-reference-expected.json",
                                                                                                       de.adorsys.psd2.model.AccountReference.class);
        assertEquals(expectedAccountReference, actualAccountReference);
    }

    @Test
    public void mapToAccountReference_nullValue() {
        de.adorsys.psd2.model.AccountReference accountReference = mapper.mapToAccountReference(null);
        assertNull(accountReference);
    }

    @Test
    public void mapToAccountReferences() {
        AccountReference accountReference = jsonReader.getObjectFromFile("json/service/mapper/AccountModelMapper-account-reference.json", AccountReference.class);
        List<de.adorsys.psd2.model.AccountReference> actualAccountReferences = mapper.mapToAccountReferences(Collections.singletonList(accountReference));

        de.adorsys.psd2.model.AccountReference expectedAccountReference = jsonReader.getObjectFromFile("json/service/mapper/AccountModelMapper-account-reference-expected.json",
                                                                                                       de.adorsys.psd2.model.AccountReference.class);

        assertEquals(1, actualAccountReferences.size());
        assertEquals(expectedAccountReference, actualAccountReferences.get(0));
    }

    @Test
    public void mapToBalance_ReadAccountBalanceResponse200() {
        Xs2aBalancesReport xs2aBalancesReport = jsonReader.getObjectFromFile("json/service/mapper/AccountModelMapper-xs2a-balances-report.json", Xs2aBalancesReport.class);
        ReadAccountBalanceResponse200 actualReadAccountBalanceResponse200 = mapper.mapToBalance(xs2aBalancesReport);

        ReadAccountBalanceResponse200 expectedReadAccountBalanceResponse200 = jsonReader.getObjectFromFile("json/service/mapper/AccountModelMapper-read-account-balance-expected.json", ReadAccountBalanceResponse200.class);
        assertEquals(expectedReadAccountBalanceResponse200, actualReadAccountBalanceResponse200);
    }

    @Test
    public void mapToReportExchangeRate_success() {
        Xs2aExchangeRate xs2aExchangeRate = jsonReader.getObjectFromFile("json/service/mapper/AccountModelMapper-xs2a-exchange-rate.json", Xs2aExchangeRate.class);
        ReportExchangeRate reportExchangeRate = mapper.mapToReportExchangeRate(xs2aExchangeRate);

        ReportExchangeRate expectedReportExchangeRate = jsonReader.getObjectFromFile("json/service/mapper/AccountModelMapper-report-exchange-rate-expected.json",
                                                                                     ReportExchangeRate.class);
        assertEquals(expectedReportExchangeRate, reportExchangeRate);

    }

    @Test
    public void mapToReportExchangeRate_nullValue() {
        ReportExchangeRate reportExchangeRate = mapper.mapToReportExchangeRate(null);
        assertNull(reportExchangeRate);
    }

    @Test
    public void mapToTransaction_success() {
        Xs2aAmount xs2aAmount = jsonReader.getObjectFromFile(XS2A_AMOUNT_JSON_PATH, Xs2aAmount.class);
        Amount amount = jsonReader.getObjectFromFile(AMOUNT_JSON_PATH, Amount.class);
        when(mockedAmountModelMapper.mapToAmount(xs2aAmount)).thenReturn(amount);
        when(mockedPurposeCodeMapper.mapToPurposeCode(PurposeCode.BKDF)).thenReturn(de.adorsys.psd2.model.PurposeCode.BKDF);

        Transactions transactions = jsonReader.getObjectFromFile("json/service/mapper/AccountModelMapper-transactions.json", Transactions.class);
        TransactionDetails actualTransactionDetails = mapper.mapToTransaction(transactions);

        TransactionDetails expectedReportTransactionDetails = jsonReader.getObjectFromFile("json/service/mapper/AccountModelMapper-transaction-details-expected.json",
                                                                                           TransactionDetails.class);
        assertEquals(expectedReportTransactionDetails, actualTransactionDetails);

    }

    @Test
    public void mapToTransactionDetails_success() {
        Xs2aAmount xs2aAmount = jsonReader.getObjectFromFile(XS2A_AMOUNT_JSON_PATH, Xs2aAmount.class);
        Amount amount = jsonReader.getObjectFromFile(AMOUNT_JSON_PATH, Amount.class);
        when(mockedAmountModelMapper.mapToAmount(xs2aAmount)).thenReturn(amount);
        when(mockedPurposeCodeMapper.mapToPurposeCode(PurposeCode.BKDF)).thenReturn(de.adorsys.psd2.model.PurposeCode.BKDF);

        Transactions transactions = jsonReader.getObjectFromFile("json/service/mapper/AccountModelMapper-transactions.json", Transactions.class);

        Map<String, TransactionDetails> actualMap = mapper.mapToTransactionDetails(transactions);

        TransactionDetails expectedTransactionDetails = jsonReader.getObjectFromFile("json/service/mapper/AccountModelMapper-transaction-details-expected.json",
                                                                                     TransactionDetails.class);

        Map<String, TransactionDetails> expectedMap = new HashMap<>();
        expectedMap.put("transactionsDetails", expectedTransactionDetails);

        assertEquals(expectedMap, actualMap);
    }

    @Test
    public void mapToTransactionsResponseRaw_success() {
        Xs2aTransactionsReport xs2aTransactionsReport = jsonReader.getObjectFromFile("json/service/mapper/AccountModelMapper-xs2a-transactions-report.json", Xs2aTransactionsReport.class);

        byte[] actualByteArray = mapper.mapToTransactionsResponseRaw(xs2aTransactionsReport);

        assertEquals(BYTE_ARRAY_IN_STRING, Base64.getEncoder().encodeToString(actualByteArray));
    }

    @Test
    public void mapToTransactionsResponse200Json_success() {
        Map<String, HrefType> links = jsonReader.getObjectFromFile(LINKS_JSON_PATH, new TypeReference<Map<String, HrefType>>() {
        });
        Links xs2aLinks = jsonReader.getObjectFromFile(XS2A_LINKS_JSON_PATH, Links.class);
        when(mockedHrefLinkMapper.mapToLinksMap(xs2aLinks)).thenReturn(links);

        Xs2aTransactionsReport xs2aTransactionsReport = jsonReader.getObjectFromFile("json/service/mapper/AccountModelMapper-xs2a-transactions-report.json", Xs2aTransactionsReport.class);

        TransactionsResponse200Json actual = mapper.mapToTransactionsResponse200Json(xs2aTransactionsReport);
        actual.getBalances().get(0).setLastChangeDateTime(OFFSET_DATE_TIME);

        TransactionsResponse200Json expected = jsonReader.getObjectFromFile("json/service/mapper/AccountModelMapper-transactionsResponse200.json",
                                                                            TransactionsResponse200Json.class);
        expected.getBalances().get(0).setLastChangeDateTime(OFFSET_DATE_TIME);

        assertLinks(expected.getLinks(), actual.getLinks());
        expected.getTransactions().setLinks(null);
        actual.getTransactions().setLinks(null);

        expected.setLinks(actual.getLinks());
        assertEquals(expected, actual);

    }

    private void assertLinks(Map expectedLinks, Map actualLinks) {
        assertNotNull(actualLinks);
        assertFalse(actualLinks.isEmpty());
        assertEquals(expectedLinks.size(), actualLinks.size());
        for (Object linkKey : actualLinks.keySet()) {
            HrefType actualHrefType = (HrefType) actualLinks.get(linkKey);
            assertEquals(String.valueOf(((Map) expectedLinks.get(linkKey)).get("href")), actualHrefType.getHref());
        }
    }

    @Configuration
    public static class TestConfiguration {
        @Bean
        public HrefLinkMapper mockHrefLinkMapper() {
            return mock(HrefLinkMapper.class);
        }

        @Bean
        public AmountModelMapper mockAmountModelMapper() {
            return mock(AmountModelMapper.class);
        }

        @Bean
        public PurposeCodeMapper mockPurposeCodeMapper() {
            return mock(PurposeCodeMapper.class);
        }
    }
}
