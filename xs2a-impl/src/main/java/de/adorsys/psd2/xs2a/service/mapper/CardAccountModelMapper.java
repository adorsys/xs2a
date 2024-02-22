/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
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

    public InlineResponse2002 mapToInlineResponse2002(Xs2aCardAccountDetailsHolder xs2aAccountDetailsHolder) {
        InlineResponse2002 inlineResponse2002 = new InlineResponse2002();
        inlineResponse2002.cardAccount(mapToCardAccountDetails(xs2aAccountDetailsHolder.getCardAccountDetails()));
        return inlineResponse2002;
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
        if (transactions == null) {
            return null;
        }
        CardTransactionList transactionList = new CardTransactionList();
        if (transactions.isEmpty()) {
            return transactionList;
        }

        List<CardTransaction> transactionDetails = transactions.stream()
                                                       .map(this::mapToCardTransaction)
                                                       .collect(Collectors.toList());

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

    @Mapping(target = "currency", expression = "java(mapToAccountDetailsCurrency(xs2aAccountReference.getCurrency()))")
    @Mapping(target = "other", expression = "java(mapToOtherType(xs2aAccountReference.getOther()))")
    public abstract de.adorsys.psd2.model.AccountReference mapToCardAccount(AccountReference xs2aAccountReference);

    private String getMulticurrencyRepresentationOrNull() {
        return MULTICURRENCY_ACCOUNT_AGGREGATION_LEVELS.contains(aspspProfileServiceWrapper.getMulticurrencyAccountLevel()) ? "XXX" : null;
    }

    protected OtherType mapToOtherType(String other){
        return other == null
                   ? null
                   : new OtherType().identification(other);
    }
}

