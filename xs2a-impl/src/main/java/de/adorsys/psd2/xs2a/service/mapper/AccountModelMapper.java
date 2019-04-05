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

import de.adorsys.psd2.model.AccountStatus;
import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.domain.Transactions;
import de.adorsys.psd2.xs2a.domain.Xs2aBalance;
import de.adorsys.psd2.xs2a.domain.Xs2aExchangeRate;
import de.adorsys.psd2.xs2a.domain.account.*;
import de.adorsys.psd2.xs2a.domain.address.Xs2aAddress;
import de.adorsys.psd2.xs2a.domain.address.Xs2aCountryCode;
import de.adorsys.psd2.xs2a.domain.code.Xs2aPurposeCode;
import de.adorsys.psd2.xs2a.web.mapper.HrefLinkMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountModelMapper {
    private final AmountModelMapper amountModelMapper;
    private final HrefLinkMapper hrefLinkMapper;

    public AccountList mapToAccountList(Xs2aAccountListHolder xs2aAccountListHolder) {
        List<Xs2aAccountDetails> accountDetailsList = xs2aAccountListHolder.getAccountDetails();

        List<AccountDetails> details = accountDetailsList.stream()
                                           .map(this::mapToAccountDetails)
                                           .collect(Collectors.toList());
        return new AccountList().accounts(details);
    }

    public AccountDetails mapToAccountDetails(Xs2aAccountDetailsHolder xs2aAccountDetailsHolder) {
        return mapToAccountDetails(xs2aAccountDetailsHolder.getAccountDetails());
    }

    private AccountDetails mapToAccountDetails(Xs2aAccountDetails accountDetails) {
        AccountDetails target = new AccountDetails();

        BeanUtils.copyProperties(accountDetails, target);

        target.resourceId(accountDetails.getResourceId())
            .currency(accountDetails.getCurrency() != null ? accountDetails.getCurrency().getCurrencyCode() : null)
            .cashAccountType(Optional.ofNullable(accountDetails.getCashAccountType())
                                 .map(Enum::name)
                                 .orElse(null))
            .usage(mapToAccountDetailsUsageEnum(accountDetails.getUsageType()))
            .status(mapToAccountStatus(accountDetails.getAccountStatus()));
        return target
                   .balances(mapToBalanceList(accountDetails.getBalances()))
                   ._links(hrefLinkMapper.mapToLinksMap(accountDetails.getLinks()));
    }

    private BalanceList mapToBalanceList(List<Xs2aBalance> balances) {
        BalanceList balanceList = null;

        if (CollectionUtils.isNotEmpty(balances)) {
            balanceList = new BalanceList();

            balanceList.addAll(balances.stream()
                                   .map(this::mapToBalance)
                                   .collect(Collectors.toList()));
        }

        return balanceList;
    }

    private AccountDetails.UsageEnum mapToAccountDetailsUsageEnum(Xs2aUsageType usageType) {
        return Optional.ofNullable(usageType)
                   .map(Xs2aUsageType::getValue)
                   .map(AccountDetails.UsageEnum::fromValue)
                   .orElse(null);
    }

    private AccountStatus mapToAccountStatus(de.adorsys.psd2.xs2a.domain.account.AccountStatus accountStatus) {
        return Optional.ofNullable(accountStatus)
                   .map(de.adorsys.psd2.xs2a.domain.account.AccountStatus::getValue)
                   .map(AccountStatus::fromValue)
                   .orElse(null);
    }

    public ReadAccountBalanceResponse200 mapToBalance(Xs2aBalancesReport balancesReport) {
        BalanceList balanceList = new BalanceList();
        balancesReport.getBalances().forEach(balance -> balanceList.add(mapToBalance(balance)));

        return new ReadAccountBalanceResponse200()
                   .balances(balanceList)
                   .account(mapToAccountReference(balancesReport.getXs2aAccountReference()));
    }

    public Balance mapToBalance(Xs2aBalance balance) {
        Balance target = new Balance();
        BeanUtils.copyProperties(balance, target);

        target.setBalanceAmount(amountModelMapper.mapToAmount(balance.getBalanceAmount()));

        Optional.ofNullable(balance.getBalanceType())
            .ifPresent(balanceType -> target.setBalanceType(BalanceType.fromValue(balanceType.getValue())));

        Optional.ofNullable(balance.getLastChangeDateTime())
            .ifPresent(lastChangeDateTime -> {
                List<ZoneOffset> validOffsets = ZoneId.systemDefault().getRules().getValidOffsets(lastChangeDateTime);
                target.setLastChangeDateTime(lastChangeDateTime.atOffset(validOffsets.get(0)));
            });

        return target;
    }

    public AccountReport mapToAccountReport(Xs2aAccountReport accountReport) {
        TransactionList booked = new TransactionList();
        List<TransactionDetails> bookedTransactions = Optional.ofNullable(accountReport.getBooked())
                                                          .map(ts -> ts.stream().map(this::mapToTransaction).collect(Collectors.toList()))
                                                          .orElseGet(ArrayList::new);
        booked.addAll(bookedTransactions);

        TransactionList pending = new TransactionList();
        List<TransactionDetails> pendingTransactions = Optional.ofNullable(accountReport.getPending())
                                                           .map(ts -> ts.stream().map(this::mapToTransaction).collect(Collectors.toList()))
                                                           .orElseGet(ArrayList::new);
        pending.addAll(pendingTransactions);

        return new AccountReport()
                   .booked(booked)
                   .pending(pending)
                   ._links(hrefLinkMapper.mapToLinksMap(accountReport.getLinks()));
    }

    public TransactionDetails mapToTransaction(Transactions transactions) {
        TransactionDetails target = new TransactionDetails();
        BeanUtils.copyProperties(transactions, target);

        target.setCreditorAccount(mapToAccountReference(transactions.getCreditorAccount()));
        target.setDebtorAccount(mapToAccountReference(transactions.getDebtorAccount()));

        Optional.ofNullable(transactions.getExchangeRate())
            .ifPresent(xs2aExchangeRates -> {
                ExchangeRateList exchangeRates = xs2aExchangeRates.stream()
                                                     .map(this::mapToExchangeRate)
                                                     .collect(Collectors.toCollection(ExchangeRateList::new));
                target.setExchangeRate(exchangeRates);
            });

        Optional.ofNullable(transactions.getAmount())
            .ifPresent(amount -> target.setTransactionAmount(amountModelMapper.mapToAmount(amount)));

        target.setPurposeCode(PurposeCode.fromValue(Optional.ofNullable(transactions.getPurposeCode())
                                                        .map(Xs2aPurposeCode::getCode)
                                                        .orElse(null)));

        Optional.ofNullable(transactions.getBankTransactionCodeCode())
            .ifPresent(transactionCode -> target.setBankTransactionCode(transactionCode.getCode()));

        return target;
    }

    // TODO rename class to avoid putting full path https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/603
    public de.adorsys.psd2.model.AccountReference mapToAccountReference12(AccountReference reference) {
        if (reference == null) {
            return null;
        }
        de.adorsys.psd2.model.AccountReference accountReference = new de.adorsys.psd2.model.AccountReference();
        accountReference.setIban(reference.getIban());
        accountReference.setBban(reference.getBban());
        if (reference.getCurrency() != null) {
            accountReference.setCurrency(reference.getCurrency().getCurrencyCode());
        }
        accountReference.setMaskedPan(reference.getMaskedPan());
        accountReference.setMsisdn(reference.getMsisdn());
        accountReference.setPan(reference.getPan());

        return accountReference;
    }

    public Address mapToAddress12(Xs2aAddress address) {
        Address targetAddress = new Address().street(address.getStreet());
        targetAddress.setStreet(address.getStreet());
        targetAddress.setBuildingNumber(address.getBuildingNumber());
        targetAddress.setCity(address.getCity());
        targetAddress.setPostalCode(address.getPostalCode());
        targetAddress.setCountry(
            Optional.ofNullable(address.getCountry())
                .map(Xs2aCountryCode::getCode)
                .orElse(null));
        return targetAddress;
    }

    public Xs2aAddress mapToXs2aAddress(Address address) {
        return Optional.ofNullable(address)
                   .map(a -> {
                       Xs2aAddress targetAddress = new Xs2aAddress();
                       targetAddress.setStreet(a.getStreet());
                       targetAddress.setBuildingNumber(a.getBuildingNumber());
                       targetAddress.setCity(a.getCity());
                       targetAddress.setPostalCode(a.getPostalCode());
                       targetAddress.setCountry(new Xs2aCountryCode(a.getCountry()));
                       return targetAddress;
                   })
                   .orElseGet(Xs2aAddress::new);
    }

    public TransactionsResponse200Json mapToTransactionsResponse200Json(Xs2aTransactionsReport transactionsReport) {
        TransactionsResponse200Json transactionsResponse200Json = new TransactionsResponse200Json();
        transactionsResponse200Json.setTransactions(mapToAccountReport(transactionsReport.getAccountReport()));
        transactionsResponse200Json.setBalances(mapToBalanceList(transactionsReport.getBalances()));
        transactionsResponse200Json.setAccount(mapToAccountReference12(transactionsReport.getAccountReference()));
        transactionsResponse200Json.setLinks(hrefLinkMapper.mapToLinksMap(transactionsReport.getLinks()));
        return transactionsResponse200Json;

    }

    public byte[] mapToTransactionsResponseRaw(Xs2aTransactionsReport transactionsReport) {
        return transactionsReport.getAccountReport().getTransactionsRaw();
    }


    public Map<String, TransactionDetails> mapToTransactionDetails(Transactions transactions) {
        //TODO Change to an appropriate object when it will be possible https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/574
        Map<String, TransactionDetails> transactionDetails = new HashMap<>();
        transactionDetails.put("transactionsDetails", mapToTransaction(transactions));
        return transactionDetails;
    }

    public List<de.adorsys.psd2.model.AccountReference> mapToAccountReferences(List<AccountReference> accountReferences) {
        if (CollectionUtils.isEmpty(accountReferences)) {
            return Collections.emptyList();
        }
        return accountReferences.stream()
                   .map(this::mapToAccountReference)
                   .collect(Collectors.toList());
    }

    public de.adorsys.psd2.model.AccountReference mapToAccountReference(AccountReference accountReference) {
        return Optional.ofNullable(accountReference)
                   .map(account -> {
                       de.adorsys.psd2.model.AccountReference reference = new de.adorsys.psd2.model.AccountReference();
                       reference.setIban(account.getIban());
                       reference.setBban(account.getBban());
                       if (account.getCurrency() != null) {
                           reference.setCurrency(account.getCurrency().getCurrencyCode());
                       }
                       reference.setMaskedPan(account.getMaskedPan());
                       reference.setMsisdn(account.getMsisdn());
                       reference.setPan(account.getPan());

                       return reference;
                   })
                   .orElse(null);
    }

    private ExchangeRate mapToExchangeRate(Xs2aExchangeRate xs2aExchangeRate) {
        ExchangeRate exchangeRate = new ExchangeRate();

        exchangeRate.setRateContract(xs2aExchangeRate.getRateContract());
        exchangeRate.setUnitCurrency(xs2aExchangeRate.getUnitCurrency());
        exchangeRate.setRate(xs2aExchangeRate.getRate());
        exchangeRate.setSourceCurrency(xs2aExchangeRate.getSourceCurrency());
        exchangeRate.setTargetCurrency(xs2aExchangeRate.getTargetCurrency());
        exchangeRate.setRateDate(xs2aExchangeRate.getRateDate());

        return exchangeRate;
    }
}
