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

import de.adorsys.psd2.aspsp.profile.domain.MulticurrencyAccountLevel;
import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.pis.PisDayOfExecution;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.domain.Transactions;
import de.adorsys.psd2.xs2a.domain.Xs2aBalance;
import de.adorsys.psd2.xs2a.domain.Xs2aExchangeRate;
import de.adorsys.psd2.xs2a.domain.account.*;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.mapper.HrefLinkMapper;
import de.adorsys.psd2.xs2a.web.mapper.PurposeCodeMapper;
import de.adorsys.psd2.xs2a.web.mapper.Xs2aAddressMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {AmountModelMapper.class, PurposeCodeMapper.class, Xs2aAddressMapper.class, AspspProfileServiceWrapper.class})
public abstract class AccountModelMapper {
    private static final List<MulticurrencyAccountLevel> MULTICURRENCY_ACCOUNT_AGGREGATION_LEVELS = Arrays.asList(MulticurrencyAccountLevel.AGGREGATION, MulticurrencyAccountLevel.AGGREGATION_AND_SUBACCOUNT);

    @Autowired
    protected HrefLinkMapper hrefLinkMapper;
    @Autowired
    protected AspspProfileServiceWrapper aspspProfileServiceWrapper;

    @Mapping(target = "currency", source = "currency.currencyCode")
    public abstract de.adorsys.psd2.model.AccountReference mapToAccountReference(AccountReference accountReference);

    public abstract List<de.adorsys.psd2.model.AccountReference> mapToAccountReferences(List<AccountReference> accountReferences);

    public AccountList mapToAccountList(Xs2aAccountListHolder xs2aAccountListHolder) {
        List<Xs2aAccountDetails> accountDetailsList = xs2aAccountListHolder.getAccountDetails();

        List<AccountDetails> details = accountDetailsList.stream()
                                           .map(this::mapToAccountDetails)
                                           .collect(Collectors.toList());
        return new AccountList().accounts(details);
    }

    public InlineResponse200 mapToInlineResponse200(Xs2aAccountDetailsHolder xs2aAccountDetailsHolder) {
        InlineResponse200 inlineResponse200 = new InlineResponse200();
        inlineResponse200.setAccount(mapToAccountDetails(xs2aAccountDetailsHolder.getAccountDetails()));
        return inlineResponse200;
    }

    @Mapping(target = "_links", ignore = true)
    @Mapping(target = "links", expression = "java(hrefLinkMapper.mapToLinksMap(accountDetails.getLinks()))")
    @Mapping(target = "status", source = "accountStatus")
    @Mapping(target = "usage", source = "usageType")
    @Mapping(target = "currency", expression = "java(mapToAccountDetailsCurrency(accountDetails.getCurrency()))")
    public abstract AccountDetails mapToAccountDetails(Xs2aAccountDetails accountDetails);

    @Mapping(target = "balanceType", expression = "java(mapToBalanceType(balance.getBalanceType()))")
    @Mapping(target = "lastChangeDateTime", expression = "java(mapToOffsetDateTime(balance.getLastChangeDateTime()))")
    public abstract Balance mapToBalance(Xs2aBalance balance);

    @Mapping(target = "account", source = "xs2aAccountReference")
    public abstract ReadAccountBalanceResponse200 mapToBalance(Xs2aBalancesReport balancesReport);

    @Mapping(target = "_links", ignore = true)
    @Mapping(target = "links", expression = "java(hrefLinkMapper.mapToLinksMap(accountReport.getLinks()))")
    public abstract AccountReport mapToAccountReport(Xs2aAccountReport accountReport);

    @Mapping(target = "_links", ignore = true)
    @Mapping(target = "links", expression = "java(hrefLinkMapper.mapToLinksMap(transactionsReport.getLinks()))")
    @Mapping(target = "transactions", source = "accountReport")
    @Mapping(target = "account", source = "accountReference")
    public abstract TransactionsResponse200Json mapToTransactionsResponse200Json(Xs2aTransactionsReport transactionsReport);

    public byte[] mapToTransactionsResponseRaw(Xs2aTransactionsReport transactionsReport) {
        return transactionsReport.getAccountReport().getTransactionsRaw();
    }

    @Mapping(target = "currencyExchange", expression = "java(mapToReportExchanges(transactions.getExchangeRate()))")
    @Mapping(target = "bankTransactionCode", source = "bankTransactionCodeCode.code")
    @Mapping(target = "transactionAmount", source = "amount")
    @Mapping(target = "additionalInformationStructured.standingOrderDetails.dayOfExecution", expression = "java(mapDayOfExecution(xs2aStandingOrderDetails.getDayOfExecution()))")
    @Mapping(target = "links", ignore = true)
    @Mapping(target = "_links", ignore = true)
    public abstract de.adorsys.psd2.model.Transactions mapToTransactions(Transactions transactions);

    public InlineResponse2001 mapToTransactionDetails(Transactions transactions) {
        InlineResponse2001 inlineResponse2001 = new InlineResponse2001();
        TransactionDetailsBody transactionDetailsBody = new TransactionDetailsBody();
        transactionDetailsBody.setTransactionDetails(mapToTransactions(transactions));
        inlineResponse2001.setTransactionsDetails(transactionDetailsBody);
        return inlineResponse2001;
    }

    protected OffsetDateTime mapToOffsetDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        List<ZoneOffset> validOffsets = ZoneId.systemDefault().getRules().getValidOffsets(localDateTime);
        return localDateTime.atOffset(validOffsets.get(0));
    }

    protected BalanceType mapToBalanceType(de.adorsys.psd2.xs2a.domain.BalanceType balanceType) {
        if (balanceType == null) {
            return null;
        }
        return BalanceType.fromValue(balanceType.getValue());
    }

    protected BalanceList mapToBalanceList(List<Xs2aBalance> balances) {
        BalanceList balanceList = null;

        if (CollectionUtils.isNotEmpty(balances)) {
            balanceList = new BalanceList();

            balanceList.addAll(balances.stream()
                                   .map(this::mapToBalance)
                                   .collect(Collectors.toList()));
        }

        return balanceList;
    }

    protected @Nullable TransactionList mapToTransactionList(@Nullable List<Transactions> transactions) {
        if (CollectionUtils.isEmpty(transactions)) {
            return null;
        }

        List<de.adorsys.psd2.model.Transactions> transactionDetails = transactions.stream()
                                                                          .map(this::mapToTransactions)
                                                                          .collect(Collectors.toList());

        TransactionList transactionList = new TransactionList();
        transactionList.addAll(transactionDetails);
        return transactionList;
    }

    protected ReportExchangeRateList mapToReportExchanges(List<Xs2aExchangeRate> xs2aExchangeRates) {
        if (CollectionUtils.isEmpty(xs2aExchangeRates)) {
            return null;
        }

        return xs2aExchangeRates.stream()
                   .map(this::mapToReportExchangeRate)
                   .collect(Collectors.toCollection(ReportExchangeRateList::new));
    }

    protected abstract ReportExchangeRate mapToReportExchangeRate(Xs2aExchangeRate xs2aExchangeRate);

    protected String mapToAccountDetailsCurrency(Currency currency) {
        return Optional.ofNullable(currency)
                   .map(Currency::getCurrencyCode)
                   .orElseGet(this::getMulticurrencyRepresentationOrNull);
    }

    private String getMulticurrencyRepresentationOrNull() {
        return MULTICURRENCY_ACCOUNT_AGGREGATION_LEVELS.contains(aspspProfileServiceWrapper.getMulticurrencyAccountLevel()) ? "XXX" : null;
    }

    protected DayOfExecution mapDayOfExecution(PisDayOfExecution dayOfExecution) {
        return dayOfExecution != null
                   ? DayOfExecution.fromValue(dayOfExecution.toString())
                   : null;
    }
}

