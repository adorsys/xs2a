/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.xs2a.service.mapper;

import de.adorsys.psd2.aspsp.profile.domain.MulticurrencyAccountLevel;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.Xs2aBalance;
import de.adorsys.psd2.xs2a.domain.account.AccountStatus;
import de.adorsys.psd2.xs2a.domain.account.*;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.mapper.HrefLinkMapper;
import de.adorsys.psd2.xs2a.web.mapper.PurposeCodeMapperImpl;
import de.adorsys.psd2.xs2a.web.mapper.Xs2aAddressMapperImpl;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CardAccountModelMapperImpl.class, Xs2aAddressMapperImpl.class, TestMapperConfiguration.class,
    HrefLinkMapper.class, Xs2aObjectMapper.class, PurposeCodeMapperImpl.class, AmountModelMapper.class})
class CardAccountModelMapperTest {

    private static final OffsetDateTime OFFSET_DATE_TIME = OffsetDateTime.now();

    @Autowired
    private CardAccountModelMapper mapper;

    @MockBean
    private AspspProfileServiceWrapper aspspProfileService;

    private final JsonReader jsonReader = new JsonReader();

    @Test
    void mapToCardAccountList() {
        // Given
        Xs2aCardAccountListHolder xs2aAccountListHolder = jsonReader.getObjectFromFile("json/service/mapper/card-account-model-mapper/CardAccountModelMapper-xs2a-card-account-list-holder.json", Xs2aCardAccountListHolder.class);

        // When
        CardAccountList actualAccountList = mapper.mapToCardAccountList(xs2aAccountListHolder);
        actualAccountList.getCardAccounts().get(0).getBalances().get(0).setLastChangeDateTime(OFFSET_DATE_TIME);

        CardAccountList expectedAccountList = jsonReader.getObjectFromFile("json/service/mapper/card-account-model-mapper/CardAccountModelMapper-card-account-list-expected.json", CardAccountList.class);
        expectedAccountList.getCardAccounts().get(0).getBalances().get(0).setLastChangeDateTime(OFFSET_DATE_TIME);

        // Then
        assertLinks(expectedAccountList.getCardAccounts().get(0).getLinks(), actualAccountList.getCardAccounts().get(0).getLinks());

        expectedAccountList.getCardAccounts().get(0).setLinks(actualAccountList.getCardAccounts().get(0).getLinks());
        assertThat(actualAccountList).isEqualTo(expectedAccountList);
    }

    @Test
    void mapToCardAccountDetails() {
        // Given
        Xs2aCardAccountDetailsHolder xs2aAccountDetailsHolder = jsonReader.getObjectFromFile("json/service/mapper/card-account-model-mapper/CardAccountModelMapper-xs2a-card-account-details-holder.json", Xs2aCardAccountDetailsHolder.class);

        // When
        InlineResponse2002 actualInlineResponse2002 = mapper.mapToInlineResponse202(xs2aAccountDetailsHolder);
        actualInlineResponse2002.getCardAccount().getBalances().get(0).setLastChangeDateTime(OFFSET_DATE_TIME);

        CardAccountDetails expectedAccountDetails = jsonReader.getObjectFromFile("json/service/mapper/card-account-model-mapper/CardAccountModelMapper-card-account-details-expected.json", CardAccountDetails.class);
        expectedAccountDetails.getBalances().get(0).setLastChangeDateTime(OFFSET_DATE_TIME);

        // Then
        assertLinks(expectedAccountDetails.getLinks(), actualInlineResponse2002.getCardAccount().getLinks());

        expectedAccountDetails.setLinks(actualInlineResponse2002.getCardAccount().getLinks());
        assertThat(actualInlineResponse2002.getCardAccount()).isEqualTo(expectedAccountDetails);
    }

    @Test
    void mapToCardAccountDetails_null() {
        // When
        CardAccountDetails actual = mapper.mapToCardAccountDetails(null);
        // Then
        assertThat(actual).isNull();
    }

    @Test
    void mapToBalance_ReadAccountBalanceResponse200() {
        // Given
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
        assertThat(actualReadAccountBalanceResponse200).isEqualTo(expectedReadAccountBalanceResponse200);
    }

    @Test
    void mapToBalance_null() {
        // When
        Balance actual = mapper.mapToBalance((Xs2aBalance) null);

        //Then
        assertThat(actual).isNull();
    }

    @Test
    void mapToBalance_null_returnsNull() {
        // When
        ReadCardAccountBalanceResponse200 actual = mapper.mapToBalance((Xs2aBalancesReport) null);

        // Then
        assertThat(actual).isNull();
    }

    @Test
    void mapToCardAccountReport_null() {
        // When
        CardAccountReport actual = mapper.mapToCardAccountReport(null);

        //Then
        assertThat(actual).isNull();
    }

    @Test
    void mapToTransactionsResponse200Json_null() {
        // When
        CardAccountsTransactionsResponse200 actual = mapper.mapToTransactionsResponse200Json(null);

        // Then
        assertThat(actual).isNull();
    }

    @Test
    void mapToTransactions_null() {
        // When
        CardTransaction actual = mapper.mapToCardTransaction(null);

        // Then
        assertThat(actual).isNull();
    }

    @Test
    void mapToReportExchangeRate_null() {
        // When
        ReportExchangeRate actual = mapper.mapToReportExchangeRate(null);

        // Then
        assertThat(actual).isNull();
    }


    @ParameterizedTest
    @MethodSource("params")
    void accountStatusToAccountStatus_missingVariousParameters(String inputFilePath, String expectedFilePath) {
        // Given
        Xs2aCardAccountDetails accountDetails = jsonReader.getObjectFromFile(inputFilePath, Xs2aCardAccountDetails.class);

        // When
        CardAccountDetails actual = mapper.mapToCardAccountDetails(accountDetails);
        CardAccountDetails expectedAccountDetails = jsonReader.getObjectFromFile(expectedFilePath, CardAccountDetails.class);

        // Then
        assertLinks(expectedAccountDetails.getLinks(), actual.getLinks());
        expectedAccountDetails.setLinks(null);
        actual.setLinks(null);

        assertThat(actual).isEqualTo(expectedAccountDetails);
    }

    @Test
    void mapToCardTransaction_success() {
        // Given
        de.adorsys.psd2.xs2a.domain.CardTransaction transaction = jsonReader.getObjectFromFile("json/service/mapper/card-account-model-mapper/CardAccountModelMapper-card-transaction.json", de.adorsys.psd2.xs2a.domain.CardTransaction.class);

        // When
        CardTransaction actualCardTransaction = mapper.mapToCardTransaction(transaction);

        CardTransaction expectedReportTransactionDetails = jsonReader.getObjectFromFile("json/service/mapper/card-account-model-mapper/CardAccountModelMapper-card-transaction-expected.json",
                                                                                        CardTransaction.class);
        // Then
        assertThat(actualCardTransaction).isEqualTo(expectedReportTransactionDetails);
    }

    @Test
    void mapToCardTransactionsResponse200Json_success() {
        // Given
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

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void mapToAccountDetailsCurrency_currencyPresent() {
        // Given
        Currency currency = Currency.getInstance("EUR");
        // When
        String currencyRepresentation = mapper.mapToAccountDetailsCurrency(currency);
        // Then
        assertThat(currencyRepresentation).isEqualTo(currency.getCurrencyCode());
    }

    @Test
    void mapToAccountDetailsCurrency_currencyNull() {
        // When
        String currencyRepresentation = mapper.mapToAccountDetailsCurrency(null);
        // Then
        assertThat(currencyRepresentation).isNull();
    }

    @Test
    void mapToAccountDetailsCurrency_multicurrencySubaccount() {
        // Given
        when(aspspProfileService.getMulticurrencyAccountLevel()).thenReturn(MulticurrencyAccountLevel.SUBACCOUNT);
        // When
        String currencyRepresentation = mapper.mapToAccountDetailsCurrency(null);
        // Then
        assertThat(currencyRepresentation).isNull();
    }

    @Test
    void mapToAccountDetailsCurrency_multicurrencyAggregations() {
        Arrays.asList(MulticurrencyAccountLevel.AGGREGATION, MulticurrencyAccountLevel.AGGREGATION_AND_SUBACCOUNT).forEach(multicurrencyAccountLevel -> {
            // Given
            when(aspspProfileService.getMulticurrencyAccountLevel()).thenReturn(multicurrencyAccountLevel);
            // When
            String currencyRepresentation = mapper.mapToAccountDetailsCurrency(null);
            // Then
            assertThat(currencyRepresentation).isEqualTo("XXX");
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
        assertThat(cardAccountDetails.getCurrency()).isEqualTo(currency.getCurrencyCode());
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
        assertThat(accountDetails.getCurrency()).isNull();
    }

    private static Stream<Arguments> params() {
        String accountsDetailsNullFilePath = "json/service/mapper/account-model-mapper/xs2aCardAccountDetails-input-accountDetails-null.json";
        String accountDetailsNullExpectedFilePath = "json/service/mapper/account-model-mapper/CardAccountDetails-expected-accountDetails-null.json";
        String accountDetailsStatusDeletedFilePath = "json/service/mapper/account-model-mapper/xs2aCardAccountDetails-input-accountDetails-status-deleted.json";
        String accountDetailsStatusDeletedExpectedFilePath = "json/service/mapper/account-model-mapper/CardAccountDetails-expected-accountDetails-status-deleted.json";
        String accountDetailsStatusBlockedFilePath = "json/service/mapper/account-model-mapper/xs2aCardAccountDetails-input-accountDetails-status-blocked.json";
        String accountDetailsStatusBlockedExpectedFilePath = "json/service/mapper/account-model-mapper/CardAccountDetails-expected-accountDetails-status-blocked.json";
        String accountDetailsAccountUsageOrgaFilePath = "json/service/mapper/account-model-mapper/xs2aCardAccountDetails-input-accountDetails-accountUsage-orga.json";
        String accountDetailsAccountUsageOrgaExpectedFilePath = "json/service/mapper/account-model-mapper/CardAccountDetails-expected-accountDetails-usage-orga.json";

        return Stream.of(Arguments.arguments(accountsDetailsNullFilePath, accountDetailsNullExpectedFilePath),
                         Arguments.arguments(accountDetailsStatusDeletedFilePath, accountDetailsStatusDeletedExpectedFilePath),
                         Arguments.arguments(accountDetailsStatusBlockedFilePath, accountDetailsStatusBlockedExpectedFilePath),
                         Arguments.arguments(accountDetailsAccountUsageOrgaFilePath, accountDetailsAccountUsageOrgaExpectedFilePath)
        );
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
}
