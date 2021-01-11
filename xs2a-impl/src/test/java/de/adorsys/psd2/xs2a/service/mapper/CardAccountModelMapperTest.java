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
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.account.AccountStatus;
import de.adorsys.psd2.xs2a.domain.account.*;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.mapper.HrefLinkMapper;
import de.adorsys.psd2.xs2a.web.mapper.PurposeCodeMapper;
import de.adorsys.psd2.xs2a.web.mapper.Xs2aAddressMapperImpl;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.AfterEach;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CardAccountModelMapperImpl.class, Xs2aAddressMapperImpl.class, CardAccountModelMapperTest.TestConfiguration.class})
class CardAccountModelMapperTest {

    private static final OffsetDateTime OFFSET_DATE_TIME = OffsetDateTime.now();
    private static final String XS2A_LINKS_JSON_PATH = "json/service/mapper/account-model-mapper/AccountModelMapper-xs2a-links.json";
    private static final String LINKS_JSON_PATH = "json/service/mapper/account-model-mapper/AccountModelMapper-links.json";
    private static final String XS2A_AMOUNT_JSON_PATH = "json/service/mapper/account-model-mapper/AccountModelMapper-xs2a-amount.json";
    private static final String AMOUNT_JSON_PATH = "json/service/mapper/account-model-mapper/AccountModelMapper-amount.json";

    @Autowired
    private CardAccountModelMapper mapper;
    @Autowired
    private HrefLinkMapper mockedHrefLinkMapper;
    @Autowired
    private AmountModelMapper mockedAmountModelMapper;
    @Autowired
    private PurposeCodeMapper mockedPurposeCodeMapper;
    @MockBean
    private AspspProfileServiceWrapper aspspProfileService;

    private final JsonReader jsonReader = new JsonReader();

    @AfterEach
    void resetMocks() {
        // Resetting is necessary because these mocks are injected into the mapper as singleton beans
        // and are not being recreated after each test
        Mockito.reset(mockedHrefLinkMapper, mockedAmountModelMapper, mockedPurposeCodeMapper);
    }

    @Test
    void mapToCardAccountList() {
        // Given
        Map<String, HrefType> links = jsonReader.getObjectFromFile(LINKS_JSON_PATH, new TypeReference<Map<String, HrefType>>() {
        });
        Links xs2aLinks = jsonReader.getObjectFromFile(XS2A_LINKS_JSON_PATH, Links.class);
        when(mockedHrefLinkMapper.mapToLinksMap(xs2aLinks)).thenReturn(links);

        Xs2aAmount xs2aAmount = jsonReader.getObjectFromFile(XS2A_AMOUNT_JSON_PATH, Xs2aAmount.class);
        Amount amount = jsonReader.getObjectFromFile(AMOUNT_JSON_PATH, Amount.class);
        when(mockedAmountModelMapper.mapToAmount(xs2aAmount)).thenReturn(amount);

        Xs2aCardAccountListHolder xs2aAccountListHolder = jsonReader.getObjectFromFile("json/service/mapper/card-account-model-mapper/CardAccountModelMapper-xs2a-card-account-list-holder.json", Xs2aCardAccountListHolder.class);

        // When
        CardAccountList actualAccountList = mapper.mapToCardAccountList(xs2aAccountListHolder);
        actualAccountList.getCardAccounts().get(0).getBalances().get(0).setLastChangeDateTime(OFFSET_DATE_TIME);

        CardAccountList expectedAccountList = jsonReader.getObjectFromFile("json/service/mapper/card-account-model-mapper/CardAccountModelMapper-card-account-list-expected.json", CardAccountList.class);
        expectedAccountList.getCardAccounts().get(0).getBalances().get(0).setLastChangeDateTime(OFFSET_DATE_TIME);

        // Then
        assertLinks(expectedAccountList.getCardAccounts().get(0).getLinks(), actualAccountList.getCardAccounts().get(0).getLinks());

        expectedAccountList.getCardAccounts().get(0).setLinks(actualAccountList.getCardAccounts().get(0).getLinks());
        assertEquals(expectedAccountList, actualAccountList);
    }

    @Test
    void mapToCardAccountDetails() {
        // Given
        Map<String, HrefType> links = jsonReader.getObjectFromFile(LINKS_JSON_PATH, new TypeReference<Map<String, HrefType>>() {
        });
        Links xs2aLinks = jsonReader.getObjectFromFile(XS2A_LINKS_JSON_PATH, Links.class);
        when(mockedHrefLinkMapper.mapToLinksMap(xs2aLinks)).thenReturn(links);

        Xs2aCardAccountDetailsHolder xs2aAccountDetailsHolder = jsonReader.getObjectFromFile("json/service/mapper/card-account-model-mapper/CardAccountModelMapper-xs2a-card-account-details-holder.json", Xs2aCardAccountDetailsHolder.class);

        // When
        InlineResponse2002 actualInlineResponse2002 = mapper.mapToInlineResponse202(xs2aAccountDetailsHolder);
        actualInlineResponse2002.getCardAccount().getBalances().get(0).setLastChangeDateTime(OFFSET_DATE_TIME);

        CardAccountDetails expectedAccountDetails = jsonReader.getObjectFromFile("json/service/mapper/card-account-model-mapper/CardAccountModelMapper-card-account-details-expected.json", CardAccountDetails.class);
        expectedAccountDetails.getBalances().get(0).setLastChangeDateTime(OFFSET_DATE_TIME);

        // Then
        assertLinks(expectedAccountDetails.getLinks(), actualInlineResponse2002.getCardAccount().getLinks());

        expectedAccountDetails.setLinks(actualInlineResponse2002.getCardAccount().getLinks());
        assertEquals(expectedAccountDetails, actualInlineResponse2002.getCardAccount());
    }

    @Test
    void mapToBalance_ReadAccountBalanceResponse200() {
        // Given
        Xs2aAmount xs2aAmount = jsonReader.getObjectFromFile(XS2A_AMOUNT_JSON_PATH, Xs2aAmount.class);
        Amount amount = jsonReader.getObjectFromFile(AMOUNT_JSON_PATH, Amount.class);
        when(mockedAmountModelMapper.mapToAmount(xs2aAmount)).thenReturn(amount);
        Xs2aBalancesReport xs2aBalancesReport = jsonReader.getObjectFromFile("json/service/mapper/card-account-model-mapper/CardAccountModelMapper-xs2a-card-balances-report.json", Xs2aBalancesReport.class);

        LocalDateTime lastChangeDateTime = LocalDateTime.parse("2018-03-31T15:16:16.374");
        ZoneOffset zoneOffset = ZoneId.systemDefault().getRules().getOffset(lastChangeDateTime);
        OffsetDateTime expectedLastChangeDateTime = lastChangeDateTime.atOffset(zoneOffset);

        // When
        ReadCardAccountBalanceResponse200 actualReadAccountBalanceResponse200 = mapper.mapToBalance(xs2aBalancesReport);

        ReadCardAccountBalanceResponse200 expectedReadAccountBalanceResponse200 = jsonReader.getObjectFromFile("json/service/mapper/card-account-model-mapper/CardAccountModelMapper-read-card-account-balance-expected.json", ReadCardAccountBalanceResponse200.class);

        Balance actualBalance = actualReadAccountBalanceResponse200.getBalances().get(0);
        assertEquals(expectedLastChangeDateTime, actualBalance.getLastChangeDateTime());

        actualBalance.setLastChangeDateTime(OFFSET_DATE_TIME);
        expectedReadAccountBalanceResponse200.getBalances().get(0).setLastChangeDateTime(OFFSET_DATE_TIME);

        // Then
        assertEquals(expectedReadAccountBalanceResponse200, actualReadAccountBalanceResponse200);
    }

    @Test
    void mapToCardTransaction_success() {
        // Given
        Xs2aAmount xs2aAmount = jsonReader.getObjectFromFile(XS2A_AMOUNT_JSON_PATH, Xs2aAmount.class);
        Amount amount = jsonReader.getObjectFromFile(AMOUNT_JSON_PATH, Amount.class);
        when(mockedAmountModelMapper.mapToAmount(xs2aAmount)).thenReturn(amount);
        when(mockedPurposeCodeMapper.mapToPurposeCode(PurposeCode.BKDF)).thenReturn(de.adorsys.psd2.model.PurposeCode.BKDF);

        de.adorsys.psd2.xs2a.domain.CardTransaction transaction = jsonReader.getObjectFromFile("json/service/mapper/card-account-model-mapper/CardAccountModelMapper-card-transaction.json", de.adorsys.psd2.xs2a.domain.CardTransaction.class);

        // When
        CardTransaction actualCardTransaction = mapper.mapToCardTransaction(transaction);

        CardTransaction expectedReportTransactionDetails = jsonReader.getObjectFromFile("json/service/mapper/card-account-model-mapper/CardAccountModelMapper-card-transaction-expected.json",
                                                                                        CardTransaction.class);
        // Then
        assertEquals(expectedReportTransactionDetails, actualCardTransaction);
    }

    @Test
    void mapToCardTransactionsResponse200Json_success() {
        // Given
        Map<String, HrefType> links = jsonReader.getObjectFromFile(LINKS_JSON_PATH, new TypeReference<Map<String, HrefType>>() {
        });
        Links xs2aLinks = jsonReader.getObjectFromFile(XS2A_LINKS_JSON_PATH, Links.class);
        when(mockedHrefLinkMapper.mapToLinksMap(xs2aLinks)).thenReturn(links);
        Xs2aAmount xs2aAmount = jsonReader.getObjectFromFile(XS2A_AMOUNT_JSON_PATH, Xs2aAmount.class);
        Amount amount = jsonReader.getObjectFromFile(AMOUNT_JSON_PATH, Amount.class);
        when(mockedAmountModelMapper.mapToAmount(xs2aAmount)).thenReturn(amount);

        Xs2aCardTransactionsReport xs2aTransactionsReport = jsonReader.getObjectFromFile("json/service/mapper/card-account-model-mapper/CardAccountModelMapper-xs2a-card-transactions-report.json", Xs2aCardTransactionsReport.class);

        // When
        CardAccountsTransactionsResponse200 actual = mapper.mapToTransactionsResponse200Json(xs2aTransactionsReport);

        actual.getBalances().get(0).setLastChangeDateTime(OFFSET_DATE_TIME);

        CardAccountsTransactionsResponse200 expected = jsonReader.getObjectFromFile("json/service/mapper/card-account-model-mapper/CardAccountModelMapper-card-transactionsResponse200.json",
                                                                                    CardAccountsTransactionsResponse200.class);
        expected.getBalances().get(0).setLastChangeDateTime(OFFSET_DATE_TIME);

        // Then
        assertLinks(expected.getLinks(), actual.getLinks());
        expected.getCardTransactions().setLinks(null);
        actual.getCardTransactions().setLinks(null);

        expected.setLinks(actual.getLinks());
        assertEquals(expected, actual);
    }

    @Test
    void mapToAccountDetailsCurrency_currencyPresent() {
        // Given
        Currency currency = Currency.getInstance("EUR");
        // When
        String currencyRepresentation = mapper.mapToAccountDetailsCurrency(currency);
        // Then
        assertEquals(currency.getCurrencyCode(), currencyRepresentation);
    }

    @Test
    void mapToAccountDetailsCurrency_currencyNull() {
        // When
        String currencyRepresentation = mapper.mapToAccountDetailsCurrency(null);
        // Then
        assertNull(currencyRepresentation);
    }

    @Test
    void mapToAccountDetailsCurrency_multicurrencySubaccount() {
        // Given
        when(aspspProfileService.getMulticurrencyAccountLevel()).thenReturn(MulticurrencyAccountLevel.SUBACCOUNT);
        // When
        String currencyRepresentation = mapper.mapToAccountDetailsCurrency(null);
        // Then
        assertNull(currencyRepresentation);
    }

    @Test
    void mapToAccountDetailsCurrency_multicurrencyAggregations() {
        Arrays.asList(MulticurrencyAccountLevel.AGGREGATION, MulticurrencyAccountLevel.AGGREGATION_AND_SUBACCOUNT).forEach(multicurrencyAccountLevel -> {
            // Given
            when(aspspProfileService.getMulticurrencyAccountLevel()).thenReturn(multicurrencyAccountLevel);
            // When
            String currencyRepresentation = mapper.mapToAccountDetailsCurrency(null);
            // Then
            assertEquals("XXX", currencyRepresentation);
        });
    }

    @Test
    void mapToCardAccountList_currencyPresent_multicurrencyLevelSubaccount() {
        // Given
        when(aspspProfileService.getMulticurrencyAccountLevel()).thenReturn(MulticurrencyAccountLevel.SUBACCOUNT);
        Currency currency = Currency.getInstance("EUR");
        Xs2aCardAccountDetails xs2aCardAccountDetails = buildXs2aCardAccountDetails(currency);
        Xs2aCardAccountListHolder xs2aCardAccountListHolder = new Xs2aCardAccountListHolder(Collections.singletonList(xs2aCardAccountDetails), null);

        // When
        CardAccountList actualAccountList = mapper.mapToCardAccountList(xs2aCardAccountListHolder);

        //Then
        CardAccountDetails cardAccountDetails = actualAccountList.getCardAccounts().get(0);
        assertEquals(currency.getCurrencyCode(), cardAccountDetails.getCurrency());
    }

    @Test
    void mapToAccountList_currencyNull_multicurrencyLevelSubaccount() {
        // Given
        when(aspspProfileService.getMulticurrencyAccountLevel()).thenReturn(MulticurrencyAccountLevel.SUBACCOUNT);

        Xs2aCardAccountDetails xs2aCardAccountDetails = buildXs2aCardAccountDetails(null);
        Xs2aCardAccountListHolder xs2aCardAccountListHolder = new Xs2aCardAccountListHolder(Collections.singletonList(xs2aCardAccountDetails), null);

        // When
        CardAccountList actualAccountList = mapper.mapToCardAccountList(xs2aCardAccountListHolder);

        // Then
        CardAccountDetails accountDetails = actualAccountList.getCardAccounts().get(0);
        assertNull(accountDetails.getCurrency());
    }

    private Xs2aCardAccountDetails buildXs2aCardAccountDetails(Currency currency) {
        return new Xs2aCardAccountDetails(null, null, null, currency,
                                          null, null, null, null, AccountStatus.ENABLED,
                                          null, null, null, null, null, null);
    }

    private void assertLinks(Map<?, ?> expectedLinks, Map<?, ?> actualLinks) {
        assertNotNull(actualLinks);
        assertFalse(actualLinks.isEmpty());
        assertEquals(expectedLinks.size(), actualLinks.size());
        for (Object linkKey : actualLinks.keySet()) {
            HrefType actualHrefType = (HrefType) actualLinks.get(linkKey);
            assertEquals(String.valueOf(((Map<?, ?>) expectedLinks.get(linkKey)).get("href")), actualHrefType.getHref());
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
