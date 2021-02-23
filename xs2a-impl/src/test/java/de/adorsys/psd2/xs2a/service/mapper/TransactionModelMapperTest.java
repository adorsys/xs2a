/*
 * Copyright 2018-2021 adorsys GmbH & Co KG
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
import de.adorsys.psd2.core.payment.model.PurposeCode;
import de.adorsys.psd2.model.Amount;
import de.adorsys.psd2.model.InlineResponse2001;
import de.adorsys.psd2.model.TransactionsResponse200Json;
import de.adorsys.psd2.xs2a.core.pis.Xs2aAmount;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.Transactions;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTransactionsReport;
import de.adorsys.psd2.xs2a.web.mapper.*;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TransactionModelMapperImpl.class,
    AccountModelMapperTest.TestConfiguration.class,
    BalanceMapperImpl.class, ReportExchangeMapperImpl.class,
    DayOfExecutionMapper.class, OffsetDateTimeMapper.class})
class TransactionModelMapperTest {
    private static final OffsetDateTime OFFSET_DATE_TIME = OffsetDateTime.now();
    private static final String BYTE_ARRAY_IN_STRING = "000000000000000=";
    private static final String XS2A_LINKS_JSON_PATH = "json/service/mapper/account-model-mapper/AccountModelMapper-xs2a-links.json";
    private static final String LINKS_JSON_PATH = "json/service/mapper/account-model-mapper/AccountModelMapper-links.json";
    private static final String XS2A_AMOUNT_JSON_PATH = "json/service/mapper/account-model-mapper/AccountModelMapper-xs2a-amount.json";
    private static final String XS2A_AMOUNT_ENTRY_JSON_PATH = "json/service/mapper/account-model-mapper/AccountModelMapper-xs2a-amount-entry.json";
    private static final String AMOUNT_JSON_PATH = "json/service/mapper/account-model-mapper/AccountModelMapper-amount.json";
    private static final String AMOUNT_ENTRY_JSON_PATH = "json/service/mapper/account-model-mapper/AccountModelMapper-amount-entry.json";

    @Autowired
    private TransactionModelMapper mapper;
    @Autowired
    private HrefLinkMapper mockedHrefLinkMapper;
    @Autowired
    private AmountModelMapper mockedAmountModelMapper;
    @Autowired
    private PurposeCodeMapper mockedPurposeCodeMapper;
    @Autowired
    protected BalanceMapper balanceMapper;

    private final JsonReader jsonReader = new JsonReader();

    @AfterEach
    void resetMocks() {
        // Resetting is necessary because these mocks are injected into the mapper as singleton beans
        // and are not being recreated after each test
        Mockito.reset(mockedHrefLinkMapper, mockedAmountModelMapper, mockedPurposeCodeMapper);
    }

    @Test
    void mapToTransaction_success() {
        Xs2aAmount xs2aAmount = jsonReader.getObjectFromFile(XS2A_AMOUNT_JSON_PATH, Xs2aAmount.class);
        Amount amount = jsonReader.getObjectFromFile(AMOUNT_JSON_PATH, Amount.class);
        when(mockedAmountModelMapper.mapToAmount(xs2aAmount)).thenReturn(amount);

        Xs2aAmount xs2aEntryAmount = jsonReader.getObjectFromFile(XS2A_AMOUNT_ENTRY_JSON_PATH, Xs2aAmount.class);
        Amount amountEntry = jsonReader.getObjectFromFile(AMOUNT_ENTRY_JSON_PATH, Amount.class);
        when(mockedAmountModelMapper.mapToAmount(xs2aEntryAmount)).thenReturn(amountEntry);

        when(mockedPurposeCodeMapper.mapToPurposeCode(PurposeCode.BKDF)).thenReturn(de.adorsys.psd2.model.PurposeCode.BKDF);
        when(mockedPurposeCodeMapper.mapToPurposeCode(PurposeCode.CDCB)).thenReturn(de.adorsys.psd2.model.PurposeCode.CDCB);

        Transactions transactions = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-transactions.json", Transactions.class);
        de.adorsys.psd2.model.Transactions actualTransactionDetails = mapper.mapToTransactions(transactions);

        de.adorsys.psd2.model.Transactions expectedReportTransactionDetails = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-transaction-details-expected.json",
                                                                                                           de.adorsys.psd2.model.Transactions.class);
        assertEquals(expectedReportTransactionDetails, actualTransactionDetails);
    }

    @Test
    void mapToTransactionDetails_success() {
        Xs2aAmount xs2aAmount = jsonReader.getObjectFromFile(XS2A_AMOUNT_JSON_PATH, Xs2aAmount.class);
        Amount amount = jsonReader.getObjectFromFile(AMOUNT_JSON_PATH, Amount.class);
        when(mockedAmountModelMapper.mapToAmount(xs2aAmount)).thenReturn(amount);
        when(mockedPurposeCodeMapper.mapToPurposeCode(PurposeCode.BKDF)).thenReturn(de.adorsys.psd2.model.PurposeCode.BKDF);

        Xs2aAmount xs2aEntryAmount = jsonReader.getObjectFromFile(XS2A_AMOUNT_ENTRY_JSON_PATH, Xs2aAmount.class);
        Amount amountEntry = jsonReader.getObjectFromFile(AMOUNT_ENTRY_JSON_PATH, Amount.class);
        when(mockedAmountModelMapper.mapToAmount(xs2aEntryAmount)).thenReturn(amountEntry);

        when(mockedPurposeCodeMapper.mapToPurposeCode(PurposeCode.BKDF)).thenReturn(de.adorsys.psd2.model.PurposeCode.BKDF);
        when(mockedPurposeCodeMapper.mapToPurposeCode(PurposeCode.CDCB)).thenReturn(de.adorsys.psd2.model.PurposeCode.CDCB);
        Transactions transactions = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-transactions.json", Transactions.class);

        InlineResponse2001 actualInlineResponse2001 = mapper.mapToTransactionDetails(transactions);

        de.adorsys.psd2.model.Transactions expectedTransactionDetails = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-transaction-details-expected.json",
                                                                                                     de.adorsys.psd2.model.Transactions.class);

        assertNotNull(actualInlineResponse2001);
        assertEquals(expectedTransactionDetails, actualInlineResponse2001.getTransactionsDetails().getTransactionDetails());
    }

    @Test
    void mapToTransactionsResponseRaw_success() {
        Xs2aTransactionsReport xs2aTransactionsReport = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-xs2a-transactions-report.json", Xs2aTransactionsReport.class);

        byte[] actualByteArray = mapper.mapToTransactionsResponseRaw(xs2aTransactionsReport);

        assertEquals(BYTE_ARRAY_IN_STRING, Base64.getEncoder().encodeToString(actualByteArray));
    }

    @Test
    void mapToTransactionsResponse200Json_success() {
        // Given
        Map<String, HrefType> links = jsonReader.getObjectFromFile(LINKS_JSON_PATH, new TypeReference<Map<String, HrefType>>() {
        });
        Links xs2aLinks = jsonReader.getObjectFromFile(XS2A_LINKS_JSON_PATH, Links.class);
        when(mockedHrefLinkMapper.mapToLinksMap(xs2aLinks)).thenReturn(links);
        Xs2aAmount xs2aAmount = jsonReader.getObjectFromFile(XS2A_AMOUNT_JSON_PATH, Xs2aAmount.class);
        Amount amount = jsonReader.getObjectFromFile(AMOUNT_JSON_PATH, Amount.class);
        when(mockedAmountModelMapper.mapToAmount(xs2aAmount)).thenReturn(amount);

        Xs2aTransactionsReport xs2aTransactionsReport = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-xs2a-transactions-report.json", Xs2aTransactionsReport.class);

        // When
        TransactionsResponse200Json actual = mapper.mapToTransactionsResponse200Json(xs2aTransactionsReport);

        actual.getBalances().get(0).setLastChangeDateTime(OFFSET_DATE_TIME);

        TransactionsResponse200Json expected = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-transactionsResponse200.json",
                                                                            TransactionsResponse200Json.class);
        expected.getBalances().get(0).setLastChangeDateTime(OFFSET_DATE_TIME);

        // Then
        assertLinks(expected.getLinks(), actual.getLinks());
        expected.getTransactions().setLinks(null);
        actual.getTransactions().setLinks(null);

        expected.setLinks(actual.getLinks());
        assertEquals(expected, actual);

    }

    private void assertLinks(Map<?, ?> expectedLinks, Map<?, ?> actualLinks) {
        assertNotNull(actualLinks);
        assertFalse(actualLinks.isEmpty());
        assertEquals(expectedLinks.size(), actualLinks.size());
        for (Object linkKey : actualLinks.keySet()) {
            HrefType actualHrefType = (HrefType) actualLinks.get(linkKey);
            assertEquals(String.valueOf(((Map) expectedLinks.get(linkKey)).get("href")), actualHrefType.getHref());
        }
    }

    @Configuration
    static class TestConfiguration {
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
