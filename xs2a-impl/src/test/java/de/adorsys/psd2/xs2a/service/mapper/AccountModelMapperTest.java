/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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
import de.adorsys.psd2.aspsp.profile.domain.MulticurrencyAccountLevel;
import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.pis.PurposeCode;
import de.adorsys.psd2.xs2a.core.pis.Xs2aAmount;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.domain.BalanceType;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.*;
import de.adorsys.psd2.xs2a.domain.Transactions;
import de.adorsys.psd2.xs2a.domain.account.*;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.mapper.HrefLinkMapper;
import de.adorsys.psd2.xs2a.web.mapper.PurposeCodeMapper;
import de.adorsys.psd2.xs2a.web.mapper.Xs2aAddressMapper;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AccountModelMapperImpl.class, AccountModelMapperTest.TestConfiguration.class})
class AccountModelMapperTest {

    private static final OffsetDateTime OFFSET_DATE_TIME = OffsetDateTime.now();
    private static final String BYTE_ARRAY_IN_STRING = "000000000000000=";
    private static final String XS2A_LINKS_JSON_PATH = "json/service/mapper/account-model-mapper/AccountModelMapper-xs2a-links.json";
    private static final String LINKS_JSON_PATH = "json/service/mapper/account-model-mapper/AccountModelMapper-links.json";
    private static final String XS2A_AMOUNT_JSON_PATH = "json/service/mapper/account-model-mapper/AccountModelMapper-xs2a-amount.json";
    private static final String AMOUNT_JSON_PATH = "json/service/mapper/account-model-mapper/AccountModelMapper-amount.json";

    @Autowired
    private AccountModelMapper mapper;
    @Autowired
    private HrefLinkMapper mockedHrefLinkMapper;
    @Autowired
    private AmountModelMapper mockedAmountModelMapper;
    @Autowired
    private PurposeCodeMapper mockedPurposeCodeMapper;
    @MockBean
    private AspspProfileServiceWrapper aspspProfileService;

    private JsonReader jsonReader = new JsonReader();

    @AfterEach
    void resetMocks() {
        // Resetting is necessary because these mocks are injected into the mapper as singleton beans
        // and are not being recreated after each test
        Mockito.reset(mockedHrefLinkMapper, mockedAmountModelMapper, mockedPurposeCodeMapper);
    }

    @Test
    void mapToAccountList() {
        // Given
        Map<String, HrefType> links = jsonReader.getObjectFromFile(LINKS_JSON_PATH, new TypeReference<Map<String, HrefType>>() {
        });
        Links xs2aLinks = jsonReader.getObjectFromFile(XS2A_LINKS_JSON_PATH, Links.class);
        when(mockedHrefLinkMapper.mapToLinksMap(xs2aLinks)).thenReturn(links);

        Xs2aAmount xs2aAmount = jsonReader.getObjectFromFile(XS2A_AMOUNT_JSON_PATH, Xs2aAmount.class);
        Amount amount = jsonReader.getObjectFromFile(AMOUNT_JSON_PATH, Amount.class);
        when(mockedAmountModelMapper.mapToAmount(xs2aAmount)).thenReturn(amount);

        Xs2aAccountListHolder xs2aAccountListHolder = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-xs2a-account-list-holder.json", Xs2aAccountListHolder.class);

        // When
        AccountList actualAccountList = mapper.mapToAccountList(xs2aAccountListHolder);


        actualAccountList.getAccounts().get(0).getBalances().get(0).setLastChangeDateTime(OFFSET_DATE_TIME);

        AccountList expectedAccountList = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-account-list-expected.json", AccountList.class);
        expectedAccountList.getAccounts().get(0).getBalances().get(0).setLastChangeDateTime(OFFSET_DATE_TIME);

        // Then
        assertLinks(expectedAccountList.getAccounts().get(0).getLinks(), actualAccountList.getAccounts().get(0).getLinks());

        expectedAccountList.getAccounts().get(0).setLinks(actualAccountList.getAccounts().get(0).getLinks());
        assertEquals(expectedAccountList, actualAccountList);
    }

    @Test
    void mapToAccountDetails() {
        Map<String, HrefType> links = jsonReader.getObjectFromFile(LINKS_JSON_PATH, new TypeReference<Map<String, HrefType>>() {
        });
        Links xs2aLinks = jsonReader.getObjectFromFile(XS2A_LINKS_JSON_PATH, Links.class);
        when(mockedHrefLinkMapper.mapToLinksMap(xs2aLinks)).thenReturn(links);

        Xs2aAccountDetailsHolder xs2aAccountDetailsHolder = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-xs2a-account-details-holder.json", Xs2aAccountDetailsHolder.class);
        InlineResponse200 actualInlineResponse200 = mapper.mapToInlineResponse200(xs2aAccountDetailsHolder);

        AccountDetails expectedAccountDetails = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-account-details-expected.json", AccountDetails.class);

        assertLinks(expectedAccountDetails.getLinks(), actualInlineResponse200.getAccount().getLinks());

        expectedAccountDetails.setLinks(actualInlineResponse200.getAccount().getLinks());
        assertEquals(expectedAccountDetails, actualInlineResponse200.getAccount());
    }

    @Test
    void mapToAccountReference_success() {
        AccountReference accountReference = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-account-reference.json", AccountReference.class);
        de.adorsys.psd2.model.AccountReference actualAccountReference = mapper.mapToAccountReference(accountReference);

        de.adorsys.psd2.model.AccountReference expectedAccountReference = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-account-reference-expected.json",
                                                                                                       de.adorsys.psd2.model.AccountReference.class);
        assertEquals(expectedAccountReference, actualAccountReference);
    }

    @Test
    void mapToAccountReference_nullValue() {
        de.adorsys.psd2.model.AccountReference accountReference = mapper.mapToAccountReference(null);
        assertNull(accountReference);
    }

    @Test
    void mapToAccountReferences() {
        AccountReference accountReference = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-account-reference.json", AccountReference.class);
        List<de.adorsys.psd2.model.AccountReference> actualAccountReferences = mapper.mapToAccountReferences(Collections.singletonList(accountReference));

        de.adorsys.psd2.model.AccountReference expectedAccountReference = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-account-reference-expected.json",
                                                                                                       de.adorsys.psd2.model.AccountReference.class);

        assertEquals(1, actualAccountReferences.size());
        assertEquals(expectedAccountReference, actualAccountReferences.get(0));
    }

    @Test
    void mapToBalance_ReadAccountBalanceResponse200() {
        // Given
        Xs2aAmount xs2aAmount = jsonReader.getObjectFromFile(XS2A_AMOUNT_JSON_PATH, Xs2aAmount.class);
        Amount amount = jsonReader.getObjectFromFile(AMOUNT_JSON_PATH, Amount.class);
        when(mockedAmountModelMapper.mapToAmount(xs2aAmount)).thenReturn(amount);
        Xs2aBalancesReport xs2aBalancesReport = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-xs2a-balances-report.json", Xs2aBalancesReport.class);

        LocalDateTime lastChangeDateTime = LocalDateTime.parse("2018-03-31T15:16:16.374");
        ZoneOffset zoneOffset = ZoneId.systemDefault().getRules().getOffset(lastChangeDateTime);
        OffsetDateTime expectedLastChangeDateTime = lastChangeDateTime.atOffset(zoneOffset);

        // When
        ReadAccountBalanceResponse200 actualReadAccountBalanceResponse200 = mapper.mapToBalance(xs2aBalancesReport);

        ReadAccountBalanceResponse200 expectedReadAccountBalanceResponse200 = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-read-account-balance-expected.json", ReadAccountBalanceResponse200.class);

        // Then
        Balance actualBalance = actualReadAccountBalanceResponse200.getBalances().get(0);
        assertEquals(expectedLastChangeDateTime, actualBalance.getLastChangeDateTime());

        actualBalance.setLastChangeDateTime(OFFSET_DATE_TIME);
        expectedReadAccountBalanceResponse200.getBalances().get(0).setLastChangeDateTime(OFFSET_DATE_TIME);

        assertEquals(expectedReadAccountBalanceResponse200, actualReadAccountBalanceResponse200);
    }

    @Test
    void mapToReportExchangeRate_success() {
        Xs2aExchangeRate xs2aExchangeRate = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-xs2a-exchange-rate.json", Xs2aExchangeRate.class);
        ReportExchangeRate reportExchangeRate = mapper.mapToReportExchangeRate(xs2aExchangeRate);

        ReportExchangeRate expectedReportExchangeRate = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-report-exchange-rate-expected.json",
                                                                                     ReportExchangeRate.class);
        assertEquals(expectedReportExchangeRate, reportExchangeRate);

    }

    @Test
    void mapToReportExchangeRate_nullValue() {
        ReportExchangeRate reportExchangeRate = mapper.mapToReportExchangeRate(null);
        assertNull(reportExchangeRate);
    }

    @Test
    void mapToTransaction_success() {
        Xs2aAmount xs2aAmount = jsonReader.getObjectFromFile(XS2A_AMOUNT_JSON_PATH, Xs2aAmount.class);
        Amount amount = jsonReader.getObjectFromFile(AMOUNT_JSON_PATH, Amount.class);
        when(mockedAmountModelMapper.mapToAmount(xs2aAmount)).thenReturn(amount);
        when(mockedPurposeCodeMapper.mapToPurposeCode(PurposeCode.BKDF)).thenReturn(de.adorsys.psd2.model.PurposeCode.BKDF);

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

    @Test
    public void mapToAccountDetailsCurrency_currencyPresent() {
        //Given
        Currency currency = Currency.getInstance("EUR");
        //When
        String currencyRepresentation = mapper.mapToAccountDetailsCurrency(currency);
        //Then
        assertEquals(currency.getCurrencyCode(), currencyRepresentation);
    }

    @Test
    public void mapToAccountDetailsCurrency_currencyNull() {
        //Given
        //When
        String currencyRepresentation = mapper.mapToAccountDetailsCurrency(null);
        //Then
        assertNull(currencyRepresentation);
    }

    @Test
    public void mapToAccountDetailsCurrency_multicurrencySubaccount() {
        //Given
        when(aspspProfileService.getMulticurrencyAccountLevel()).thenReturn(MulticurrencyAccountLevel.SUBACCOUNT);
        //When
        String currencyRepresentation = mapper.mapToAccountDetailsCurrency(null);
        //Then
        assertNull(currencyRepresentation);
    }

    @Test
    public void mapToAccountDetailsCurrency_multicurrencyAggregations() {
        Arrays.asList(MulticurrencyAccountLevel.AGGREGATION, MulticurrencyAccountLevel.AGGREGATION_AND_SUBACCOUNT).forEach(multicurrencyAccountLevel -> {
            //Given
            when(aspspProfileService.getMulticurrencyAccountLevel()).thenReturn(multicurrencyAccountLevel);
            //When
            String currencyRepresentation = mapper.mapToAccountDetailsCurrency(null);
            //Then
            assertEquals("XXX", currencyRepresentation);
        });
    }

    @Test
    public void mapToAccountList_currencyPresent_multicurrencyLevelSubaccount() {
        //Given
        when(aspspProfileService.getMulticurrencyAccountLevel()).thenReturn(MulticurrencyAccountLevel.SUBACCOUNT);
        Currency currency = Currency.getInstance("EUR");
        Xs2aAccountDetails xs2aAccountDetails = buildXs2aAccountDetails(currency);
        Xs2aAccountListHolder xs2aAccountListHolder = new Xs2aAccountListHolder(Collections.singletonList(xs2aAccountDetails), null);
        //When
        AccountList actualAccountList = mapper.mapToAccountList(xs2aAccountListHolder);
        //Then
        AccountDetails accountDetails = actualAccountList.getAccounts().get(0);
        assertEquals(currency.getCurrencyCode(), accountDetails.getCurrency());
    }

    @Test
    public void mapToAccountList_currencyNull_multicurrencyLevelSubaccount() {
        //Given
        when(aspspProfileService.getMulticurrencyAccountLevel()).thenReturn(MulticurrencyAccountLevel.SUBACCOUNT);
        Currency currency = null;
        Xs2aAccountDetails xs2aAccountDetails = buildXs2aAccountDetails(currency);
        Xs2aAccountListHolder xs2aAccountListHolder = new Xs2aAccountListHolder(Collections.singletonList(xs2aAccountDetails), null);
        //When
        AccountList actualAccountList = mapper.mapToAccountList(xs2aAccountListHolder);
        //Then
        AccountDetails accountDetails = actualAccountList.getAccounts().get(0);
        assertNull(accountDetails.getCurrency());
    }

    @Test
    public void mapToAccountList_currencyPresent_multicurrencyLevelAggregation() {
        //Given
        Arrays.asList(MulticurrencyAccountLevel.AGGREGATION, MulticurrencyAccountLevel.AGGREGATION_AND_SUBACCOUNT).forEach(multicurrencyAccountLevel -> {
            //Given
            when(aspspProfileService.getMulticurrencyAccountLevel()).thenReturn(multicurrencyAccountLevel);
            Currency currency = Currency.getInstance("EUR");
            Xs2aAccountDetails xs2aAccountDetails = buildXs2aAccountDetails(currency);
            Xs2aAccountListHolder xs2aAccountListHolder = new Xs2aAccountListHolder(Collections.singletonList(xs2aAccountDetails), null);
            //When
            AccountList actualAccountList = mapper.mapToAccountList(xs2aAccountListHolder);
            //Then
            AccountDetails accountDetails = actualAccountList.getAccounts().get(0);
            assertEquals(currency.getCurrencyCode(), accountDetails.getCurrency());
        });
    }

    @Test
    public void mapToAccountList_currencyNull_multicurrencyLevelAggregation() {
        //Given
        Arrays.asList(MulticurrencyAccountLevel.AGGREGATION, MulticurrencyAccountLevel.AGGREGATION_AND_SUBACCOUNT).forEach(multicurrencyAccountLevel -> {
            //Given
            when(aspspProfileService.getMulticurrencyAccountLevel()).thenReturn(multicurrencyAccountLevel);
            Currency currency = null;
            Xs2aAccountDetails xs2aAccountDetails = buildXs2aAccountDetails(currency);
            Xs2aAccountListHolder xs2aAccountListHolder = new Xs2aAccountListHolder(Collections.singletonList(xs2aAccountDetails), null);
            //When
            AccountList actualAccountList = mapper.mapToAccountList(xs2aAccountListHolder);
            //Then
            AccountDetails accountDetails = actualAccountList.getAccounts().get(0);
            assertEquals("XXX", accountDetails.getCurrency());
        });
    }

    @Test
    void mapToBalanceType() {
        Stream.of(BalanceType.values()) //Given
            .map(mapper::mapToBalanceType) //When
            .forEach(Assertions::assertNotNull); //Then
    }

    private Xs2aAccountDetails buildXs2aAccountDetails(Currency currency) {
        return new Xs2aAccountDetails(null, null, null, null,
                                      null, null, null, currency,
                                      null, null, null, null,
                                      null, null, null, null,
                                      null, null, null, null);
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
