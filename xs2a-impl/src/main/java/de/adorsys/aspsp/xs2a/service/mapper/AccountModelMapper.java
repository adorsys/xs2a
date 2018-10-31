/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.aspsp.xs2a.service.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.aspsp.xs2a.domain.Transactions;
import de.adorsys.aspsp.xs2a.domain.Xs2aAmount;
import de.adorsys.aspsp.xs2a.domain.Xs2aBalance;
import de.adorsys.aspsp.xs2a.domain.account.*;
import de.adorsys.aspsp.xs2a.domain.address.Xs2aAddress;
import de.adorsys.aspsp.xs2a.domain.address.Xs2aCountryCode;
import de.adorsys.aspsp.xs2a.domain.code.Xs2aPurposeCode;
import de.adorsys.psd2.model.*;
import de.adorsys.psd2.model.AccountStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
    private final ObjectMapper objectMapper;

    public AccountList mapToAccountList(Map<String, List<Xs2aAccountDetails>> accountDetailsList) {
        List<AccountDetails> details = accountDetailsList.values().stream()
                                           .flatMap(ad -> ad.stream().map(this::mapToAccountDetails))
                                           .collect(Collectors.toList());
        return new AccountList().accounts(details);
    }

    public AccountDetails mapToAccountDetails(Xs2aAccountDetails accountDetails) {
        AccountDetails target = new AccountDetails();
        BeanUtils.copyProperties(accountDetails, target);

        // TODO fill missing values: product status usage details
        // https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/248
        target.resourceId(accountDetails.getResourceId())
            .currency(accountDetails.getCurrency().getCurrencyCode())
            .cashAccountType(Optional.ofNullable(accountDetails.getCashAccountType())
                                 .map(Enum::name)
                                 .orElse(null))
            .usage(mapToAccountDetailsUsageEnum(accountDetails.getUsageType()))
            .status(mapToAccountStatus(accountDetails.getAccountStatus()));
        return target
                   .balances(mapToBalanceList(accountDetails.getBalances()))
                   ._links(objectMapper.convertValue(accountDetails.getLinks(), Map.class));
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

    private AccountStatus mapToAccountStatus(de.adorsys.aspsp.xs2a.domain.account.AccountStatus accountStatus) {
        return Optional.ofNullable(accountStatus)
                   .map(de.adorsys.aspsp.xs2a.domain.account.AccountStatus::getValue)
                   .map(AccountStatus::fromValue)
                   .orElse(null);
    }

    public ReadBalanceResponse200 mapToBalance(Xs2aBalancesReport balancesReport) {
        BalanceList balanceList = new BalanceList();
        balancesReport.getBalances().forEach(balance -> balanceList.add(mapToBalance(balance)));

        return new ReadBalanceResponse200()
                   .balances(balanceList)
                   .account(balancesReport.getXs2aAccountReference());
    }

    public Balance mapToBalance(Xs2aBalance balance) {
        Balance target = new Balance();
        BeanUtils.copyProperties(balance, target);

        target.setBalanceAmount(AmountModelMapper.mapToAmount(balance.getBalanceAmount()));

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
                   ._links(objectMapper.convertValue(accountReport.getLinks(), Map.class));
    }

    public TransactionDetails mapToTransaction(Transactions transactions) {
        TransactionDetails target = new TransactionDetails();
        BeanUtils.copyProperties(transactions, target);

        target.setCreditorAccount(createAccountObject(transactions.getCreditorAccount()));
        target.setDebtorAccount(createAccountObject(transactions.getDebtorAccount()));

        // TODO fill missing values: entryReference checkId exchangeRate proprietaryBankTransactionCode links
        // https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/248
        Optional.ofNullable(transactions.getAmount())
            .ifPresent(amount -> target.setTransactionAmount(AmountModelMapper.mapToAmount(amount)));

        target.setPurposeCode(PurposeCode.fromValue(Optional.ofNullable(transactions.getPurposeCode())
                                                        .map(Xs2aPurposeCode::getCode)
                                                        .orElse(null)));

        Optional.ofNullable(transactions.getBankTransactionCodeCode())
            .ifPresent(transactionCode -> target.setBankTransactionCode(transactionCode.getCode()));

        return target;
    }

    public <T> T mapToAccountReference12(Xs2aAccountReference reference) {
        if (reference == null) {
            return null;
        }

        T accountReference = null;

        if (StringUtils.isNotBlank(reference.getIban())) {
            accountReference = (T) new AccountReferenceIban().iban(reference.getIban());
            ((AccountReferenceIban) accountReference).setCurrency(reference.getCurrency().getCurrencyCode());
        } else if (StringUtils.isNotBlank(reference.getBban())) {
            accountReference = (T) new AccountReferenceBban().bban(reference.getBban());
            ((AccountReferenceBban) accountReference).setCurrency(reference.getCurrency().getCurrencyCode());
        } else if (StringUtils.isNotBlank(reference.getPan())) {
            accountReference = (T) new AccountReferencePan().pan(reference.getPan());
            ((AccountReferencePan) accountReference).setCurrency(reference.getCurrency().getCurrencyCode());
        } else if (StringUtils.isNotBlank(reference.getMaskedPan())) {
            accountReference = (T) new AccountReferenceMaskedPan().maskedPan(reference.getMaskedPan());
            ((AccountReferenceMaskedPan) accountReference).setCurrency(reference.getCurrency().getCurrencyCode());
        } else if (StringUtils.isNotBlank(reference.getMsisdn())) {
            accountReference = (T) new AccountReferenceMsisdn().msisdn(reference.getMsisdn());
            ((AccountReferenceMsisdn) accountReference).setCurrency(reference.getCurrency().getCurrencyCode());
        }
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

    public Xs2aAmount mapToXs2aAmount(Amount amount) {
        return Optional.ofNullable(amount)
                   .map(a -> {
                       Xs2aAmount targetAmount = new Xs2aAmount();
                       targetAmount.setAmount(a.getAmount());
                       targetAmount.setCurrency(Currency.getInstance(a.getCurrency()));
                       return targetAmount;
                   })
                   .orElseGet(Xs2aAmount::new);

    }

    public TransactionsResponse200Json mapToTransactionsResponse200Json(Xs2aTransactionsReport transactionsReport) {
        TransactionsResponse200Json transactionsResponse200Json = new TransactionsResponse200Json();
        transactionsResponse200Json.setTransactions(mapToAccountReport(transactionsReport.getAccountReport()));
        transactionsResponse200Json.setBalances(mapToBalanceList(transactionsReport.getBalances()));
        transactionsResponse200Json.setAccount(mapToAccountReference12(transactionsReport.getXs2aAccountReference()));
        transactionsResponse200Json.setLinks(objectMapper.convertValue(transactionsReport.getLinks(), Map.class));
        return transactionsResponse200Json;

    }

    private Object createAccountObject(Xs2aAccountReference xs2aAccountReference) {
        return Optional.ofNullable(xs2aAccountReference)
                   .map(account -> {
                       if (account.getIban() != null) {
                           return new AccountReferenceIban()
                                      .iban(account.getIban())
                                      .currency(getCurrencyFromAccountReference(account));
                       } else if (account.getBban() != null) {
                           return new AccountReferenceBban()
                                      .bban(account.getBban())
                                      .currency(getCurrencyFromAccountReference(account));
                       } else if (account.getPan() != null) {
                           return new AccountReferencePan()
                                      .pan(account.getPan())
                                      .currency(getCurrencyFromAccountReference(account));
                       } else if (account.getMsisdn() != null) {
                           return new AccountReferenceMsisdn()
                                      .msisdn(account.getMsisdn())
                                      .currency(getCurrencyFromAccountReference(account));
                       } else if (account.getMaskedPan() != null) {
                           return new AccountReferenceMaskedPan()
                                      .maskedPan(account.getMaskedPan())
                                      .currency(getCurrencyFromAccountReference(account));
                       }

                       return null;
                   })
                   .orElse(null);
    }

    private String getCurrencyFromAccountReference(Xs2aAccountReference xs2aAccountReference) {
        return Optional.ofNullable(xs2aAccountReference.getCurrency())
                   .map(Currency::getCurrencyCode)
                   .orElse(null);
    }
}
