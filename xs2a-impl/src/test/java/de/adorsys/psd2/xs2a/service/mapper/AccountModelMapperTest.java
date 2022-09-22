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
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountDetails;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountDetailsHolder;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountListHolder;
import de.adorsys.psd2.xs2a.domain.account.Xs2aBalancesReport;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.mapper.*;
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
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AccountModelMapperImpl.class, TestMapperConfiguration.class, BalanceMapperImpl.class,
    ReportExchangeMapperImpl.class, DayOfExecutionMapper.class, OffsetDateTimeMapper.class, HrefLinkMapper.class,
    Xs2aObjectMapper.class, PurposeCodeMapperImpl.class, AmountModelMapper.class
})
class AccountModelMapperTest {
    private static final OffsetDateTime OFFSET_DATE_TIME = OffsetDateTime.now();

    @Autowired
    private AccountModelMapper mapper;

    @MockBean
    private AspspProfileServiceWrapper aspspProfileService;

    private final JsonReader jsonReader = new JsonReader();

    @Test
    void mapToAccountList() {
        // Given
        Xs2aAccountListHolder xs2aAccountListHolder = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-xs2a-account-list-holder.json", Xs2aAccountListHolder.class);

        // When
        AccountList actualAccountList = mapper.mapToAccountList(xs2aAccountListHolder);


        actualAccountList.getAccounts().get(0).getBalances().get(0).setLastChangeDateTime(OFFSET_DATE_TIME);

        AccountList expectedAccountList = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-account-list-expected.json", AccountList.class);
        expectedAccountList.getAccounts().get(0).getBalances().get(0).setLastChangeDateTime(OFFSET_DATE_TIME);

        // Then
        assertLinks(expectedAccountList.getAccounts().get(0).getLinks(), actualAccountList.getAccounts().get(0).getLinks());

        expectedAccountList.getAccounts().get(0).setLinks(actualAccountList.getAccounts().get(0).getLinks());
        assertThat(actualAccountList).isEqualTo(expectedAccountList);
    }

    @Test
    void mapToAccountDetails() {
        // Given
        Xs2aAccountDetailsHolder xs2aAccountDetailsHolder = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-xs2a-account-details-holder.json", Xs2aAccountDetailsHolder.class);

        // When
        InlineResponse200 actualInlineResponse200 = mapper.mapToInlineResponse200(xs2aAccountDetailsHolder);

        AccountDetails expectedAccountDetails = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-account-details-expected.json", AccountDetails.class);

        //Then
        assertLinks(expectedAccountDetails.getLinks(), actualInlineResponse200.getAccount().getLinks());
        expectedAccountDetails.setLinks(actualInlineResponse200.getAccount().getLinks());

        assertThat(expectedAccountDetails).isEqualTo(actualInlineResponse200.getAccount());
    }

    @Test
    void mapToAccountDetails_null() {
        // When
        AccountDetails actual = mapper.mapToAccountDetails(null);

        // Then
        assertThat(actual).isNull();
    }

    @Test
    void mapToAccountReference_success() {
        // Given
        AccountReference accountReference = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-account-reference.json", AccountReference.class);

        // When
        de.adorsys.psd2.model.AccountReference actualAccountReference = mapper.mapToAccountReference(accountReference);

        de.adorsys.psd2.model.AccountReference expectedAccountReference = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-account-reference-expected.json",
                                                                                                       de.adorsys.psd2.model.AccountReference.class);

        // Then
        assertThat(actualAccountReference).isEqualTo(expectedAccountReference);
    }

    @Test
    void mapToAccountReference_nullValue() {
        // When
        de.adorsys.psd2.model.AccountReference accountReference = mapper.mapToAccountReference(null);

        // Then
        assertThat(accountReference).isNull();
    }

    @Test
    void mapToAccountReferences() {
        // Given
        AccountReference accountReference = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-account-reference.json", AccountReference.class);

        // When
        List<de.adorsys.psd2.model.AccountReference> actualAccountReferences = mapper.mapToAccountReferences(Collections.singletonList(accountReference));

        de.adorsys.psd2.model.AccountReference expectedAccountReference = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-account-reference-expected.json",
                                                                                                       de.adorsys.psd2.model.AccountReference.class);

        // Then
        assertThat(actualAccountReferences).asList().hasSize(1).contains(expectedAccountReference);
    }

    @Test
    void mapToBalance_null() {
        // When
        ReadAccountBalanceResponse200 actual = mapper.mapToBalance(null);

        // Then
        assertThat(actual).isNull();
    }

    @Test
    void mapToAccountReferences_null() {
        // When
        List<de.adorsys.psd2.model.AccountReference> actual = mapper.mapToAccountReferences(null);

        // Then
        assertThat(actual).isNull();
    }

    @ParameterizedTest
    @MethodSource("params")
    void accountStatusToAccountStatus_status_deleted(String inputFilePath, String expectedFilePath) {
        // Given
        Xs2aAccountDetailsHolder accountDetails = jsonReader.getObjectFromFile(inputFilePath, Xs2aAccountDetailsHolder.class);

        // When
        AccountDetails actual = mapper.mapToAccountDetails(accountDetails.getAccountDetails());

        AccountDetails accountDetailsExpected = jsonReader.getObjectFromFile(expectedFilePath, AccountDetails.class);

        // Then
        assertLinks(accountDetailsExpected.getLinks(), actual.getLinks());
        accountDetailsExpected.setLinks(null);
        actual.setLinks(null);

        accountDetailsExpected.setLinks(actual.getLinks());
        assertThat(actual).isEqualTo(accountDetailsExpected);
    }

    @Test
    void mapToBalance_ReadAccountBalanceResponse200() {
        // Given
        Xs2aBalancesReport xs2aBalancesReport = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-xs2a-balances-report.json", Xs2aBalancesReport.class);

        LocalDateTime lastChangeDateTime = LocalDateTime.parse("2018-03-31T15:16:16.374");
        ZoneOffset zoneOffset = ZoneId.systemDefault().getRules().getOffset(lastChangeDateTime);
        OffsetDateTime expectedLastChangeDateTime = lastChangeDateTime.atOffset(zoneOffset);

        // When
        ReadAccountBalanceResponse200 actualReadAccountBalanceResponse200 = mapper.mapToBalance(xs2aBalancesReport);

        ReadAccountBalanceResponse200 expectedReadAccountBalanceResponse200 = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-read-account-balance-expected.json", ReadAccountBalanceResponse200.class);

        // Then
        Balance actualBalance = actualReadAccountBalanceResponse200.getBalances().get(0);
        assertThat(actualBalance.getLastChangeDateTime()).isEqualTo(expectedLastChangeDateTime);

        actualBalance.setLastChangeDateTime(OFFSET_DATE_TIME);
        expectedReadAccountBalanceResponse200.getBalances().get(0).setLastChangeDateTime(OFFSET_DATE_TIME);

        assertThat(actualReadAccountBalanceResponse200).isEqualTo(expectedReadAccountBalanceResponse200);
    }

    @Test
    void mapToAccountDetailsCurrency_currencyPresent() {
        //Given
        Currency currency = Currency.getInstance("EUR");
        //When
        String currencyRepresentation = mapper.mapToAccountDetailsCurrency(currency);
        //Then
        assertThat(currency.getCurrencyCode()).isEqualTo(currencyRepresentation);
    }

    @Test
    void mapToAccountDetailsCurrency_currencyNull() {
        //When
        String currencyRepresentation = mapper.mapToAccountDetailsCurrency(null);
        //Then
        assertThat(currencyRepresentation).isNull();
    }

    @Test
    void mapToAccountDetailsCurrency_multicurrencySubaccount() {
        //Given
        when(aspspProfileService.getMulticurrencyAccountLevel()).thenReturn(MulticurrencyAccountLevel.SUBACCOUNT);
        //When
        String currencyRepresentation = mapper.mapToAccountDetailsCurrency(null);
        //Then
        assertThat(currencyRepresentation).isNull();
    }

    @Test
    void mapToAccountDetailsCurrency_multicurrencyAggregations() {
        Arrays.asList(MulticurrencyAccountLevel.AGGREGATION, MulticurrencyAccountLevel.AGGREGATION_AND_SUBACCOUNT).forEach(multicurrencyAccountLevel -> {
            //Given
            when(aspspProfileService.getMulticurrencyAccountLevel()).thenReturn(multicurrencyAccountLevel);
            //When
            String currencyRepresentation = mapper.mapToAccountDetailsCurrency(null);
            //Then
            assertThat(currencyRepresentation).isEqualTo("XXX");
        });
    }

    @Test
    void mapToAccountList_currencyPresent_multicurrencyLevelSubaccount() {
        //Given
        when(aspspProfileService.getMulticurrencyAccountLevel()).thenReturn(MulticurrencyAccountLevel.SUBACCOUNT);
        Currency currency = Currency.getInstance("EUR");
        Xs2aAccountDetails xs2aAccountDetails = buildXs2aAccountDetails(currency);
        Xs2aAccountListHolder xs2aAccountListHolder = new Xs2aAccountListHolder(Collections.singletonList(xs2aAccountDetails), null);
        //When
        AccountList actualAccountList = mapper.mapToAccountList(xs2aAccountListHolder);
        //Then
        assertThat(actualAccountList.getAccounts()).asList().hasSize(1);
        AccountDetails accountDetails = actualAccountList.getAccounts().get(0);
        assertThat(accountDetails.getCurrency()).isEqualTo(currency.getCurrencyCode());
    }

    @Test
    void mapToAccountList_currencyNull_multicurrencyLevelSubaccount() {
        //Given
        when(aspspProfileService.getMulticurrencyAccountLevel()).thenReturn(MulticurrencyAccountLevel.SUBACCOUNT);
        Currency currency = null;
        Xs2aAccountDetails xs2aAccountDetails = buildXs2aAccountDetails(currency);
        Xs2aAccountListHolder xs2aAccountListHolder = new Xs2aAccountListHolder(Collections.singletonList(xs2aAccountDetails), null);
        //When
        AccountList actualAccountList = mapper.mapToAccountList(xs2aAccountListHolder);
        //Then
        AccountDetails accountDetails = actualAccountList.getAccounts().get(0);
        assertThat(accountDetails.getCurrency()).isNull();
    }

    @Test
    void mapToAccountList_currencyPresent_multicurrencyLevelAggregation() {
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
            assertThat(accountDetails.getCurrency()).isEqualTo(currency.getCurrencyCode());
        });
    }

    @Test
    void mapToAccountList_currencyNull_multicurrencyLevelAggregation() {
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
            assertThat(actualAccountList.getAccounts()).asList().hasSize(1);
            AccountDetails accountDetails = actualAccountList.getAccounts().get(0);
            assertThat(accountDetails.getCurrency()).isEqualTo("XXX");
        });
    }

    private static Stream<Arguments> params() {
        String accountsDetailsStatusDeletedFilePath = "json/service/mapper/account-model-mapper/AccountModelMapper-xs2a-account-details-holder-accountStatusDeleted.json";
        String accountDetailsStatusDeletedExpectedFilePath = "json/service/mapper/account-model-mapper/AccountModelMapper-account-details-expected-statusDeleted.json";
        String accountDetailsStatusBlockedFilePath = "json/service/mapper/account-model-mapper/AccountModelMapper-xs2a-account-details-holder-accountStatusBlocked.json";
        String accountDetailsStatusBlockedExpectedFilePath = "json/service/mapper/account-model-mapper/AccountModelMapper-account-details-expected-statusBlocked.json";
        String accountDetailsAccountUsageOrgaFilePath = "json/service/mapper/account-model-mapper/AccountModelMapper-xs2a-account-details-holder-accountUsage-orga.json";
        String accountDetailsAccountUsageOrgaExpectedFilePath = "json/service/mapper/account-model-mapper/AccountModelMapper-account-details-expected-accountUsage-orga.json";

        return Stream.of(Arguments.arguments(accountsDetailsStatusDeletedFilePath, accountDetailsStatusDeletedExpectedFilePath),
                         Arguments.arguments(accountDetailsStatusBlockedFilePath, accountDetailsStatusBlockedExpectedFilePath),
                         Arguments.arguments(accountDetailsAccountUsageOrgaFilePath, accountDetailsAccountUsageOrgaExpectedFilePath)
        );
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
}
