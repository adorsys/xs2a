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
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
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
public abstract class CardAccountModelMapper {
    private static final List<MulticurrencyAccountLevel> MULTICURRENCY_ACCOUNT_AGGREGATION_LEVELS = Arrays.asList(MulticurrencyAccountLevel.AGGREGATION, MulticurrencyAccountLevel.AGGREGATION_AND_SUBACCOUNT);

    @Autowired
    protected HrefLinkMapper hrefLinkMapper;
    @Autowired
    protected AspspProfileServiceWrapper aspspProfileServiceWrapper;

    public CardAccountList mapToCardAccountList(Xs2aCardAccountListHolder xs2aCardAccountListHolder) {
        List<Xs2aCardAccountDetails> accountDetailsList = xs2aCardAccountListHolder.getCardAccountDetails();

        List<CardAccountDetails> details = accountDetailsList.stream()
                                               .map(this::mapToCardAccountDetails)
                                               .collect(Collectors.toList());
        return new CardAccountList().cardAccounts(details);
    }

    public InlineResponse2002 mapToInlineResponse202(Xs2aCardAccountDetailsHolder xs2aAccountDetailsHolder) {
        InlineResponse2002 inlineResponse202 = new InlineResponse2002();
        inlineResponse202.setCardAccount(mapToCardAccountDetails(xs2aAccountDetailsHolder.getCardAccountDetails()));
        return inlineResponse202;
    }

    @Mapping(target = "_links", ignore = true)
    @Mapping(target = "links", expression = "java(hrefLinkMapper.mapToLinksMap(accountDetails.getLinks()))")
    @Mapping(target = "status", source = "accountStatus")
    @Mapping(target = "usage", source = "usageType")
    @Mapping(target = "currency", expression = "java(mapToAccountDetailsCurrency(accountDetails.getCurrency()))")
    public abstract CardAccountDetails mapToCardAccountDetails(Xs2aCardAccountDetails accountDetails);

    @Mapping(target = "balanceType", expression = "java(mapToBalanceType(balance.getBalanceType()))")
    @Mapping(target = "lastChangeDateTime", expression = "java(mapToOffsetDateTime(balance.getLastChangeDateTime()))")
    public abstract Balance mapToBalance(Xs2aBalance balance);

    @Mapping(target = "cardAccount", expression = "java(mapToCardAccount(balancesReport.getXs2aAccountReference()))")
    public abstract ReadCardAccountBalanceResponse200 mapToBalance(Xs2aBalancesReport balancesReport);

    @Mapping(target = "_links", ignore = true)
    @Mapping(target = "links", expression = "java(hrefLinkMapper.mapToLinksMap(accountReport.getLinks()))")
    public abstract CardAccountReport mapToCardAccountReport(Xs2aCardAccountReport accountReport);

    @Mapping(target = "_links", ignore = true)
    @Mapping(target = "links", expression = "java(hrefLinkMapper.mapToLinksMap(cardTransactionsReport.getLinks()))")
    @Mapping(target = "cardTransactions", source = "cardAccountReport")
    @Mapping(target = "cardAccount", source = "accountReference")
    public abstract CardAccountsTransactionsResponse200 mapToTransactionsResponse200Json(Xs2aCardTransactionsReport cardTransactionsReport);

    public byte[] mapToTransactionsResponseRaw(Xs2aCardTransactionsReport transactionsReport) {
        return transactionsReport.getCardAccountReport().getTransactionsRaw();
    }

    @Mapping(target = "currencyExchange", expression = "java(mapToReportExchanges(transactions.getCurrencyExchange()))")
    public abstract CardTransaction mapToCardTransaction(de.adorsys.psd2.xs2a.domain.CardTransaction transactions);

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

    protected @Nullable CardTransactionList mapToCardTransactionList(@Nullable List<de.adorsys.psd2.xs2a.domain.CardTransaction> transactions) {
        if (CollectionUtils.isEmpty(transactions)) {
            return null;
        }

        List<CardTransaction> transactionDetails = transactions.stream()
                                                       .map(this::mapToCardTransaction)
                                                       .collect(Collectors.toList());

        CardTransactionList transactionList = new CardTransactionList();
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

    protected de.adorsys.psd2.model.AccountReference mapToCardAccount(AccountReference xs2aAccountReference) {
        if (xs2aAccountReference == null) {
            return null;
        }
        de.adorsys.psd2.model.AccountReference accountReference = new de.adorsys.psd2.model.AccountReference();
        accountReference.setIban(xs2aAccountReference.getIban());
        accountReference.setBban(xs2aAccountReference.getBban());
        accountReference.setCurrency(xs2aAccountReference.getCurrency().getCurrencyCode());
        accountReference.setMaskedPan(xs2aAccountReference.getMaskedPan());
        accountReference.setPan(xs2aAccountReference.getPan());
        accountReference.setMsisdn(xs2aAccountReference.getMsisdn());
        accountReference.setOther(mapToOtherType(xs2aAccountReference.getOther()));
        accountReference.setCashAccountType(xs2aAccountReference.getCashAccountType());

        return accountReference;
    }

    private String getMulticurrencyRepresentationOrNull() {
        return MULTICURRENCY_ACCOUNT_AGGREGATION_LEVELS.contains(aspspProfileServiceWrapper.getMulticurrencyAccountLevel()) ? "XXX" : null;
    }

    protected OtherType mapToOtherType(String other){
        return other == null ? null : new OtherType().identification(other);
    }
}

