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

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.adorsys.aspsp.xs2a.domain.Transactions;
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.psd2.model.AccountDetails;
import de.adorsys.psd2.model.AccountList;
import de.adorsys.psd2.model.AccountReferenceBban;
import de.adorsys.psd2.model.AccountReferenceIban;
import de.adorsys.psd2.model.AccountReferenceMaskedPan;
import de.adorsys.psd2.model.AccountReferenceMsisdn;
import de.adorsys.psd2.model.AccountReferencePan;
import de.adorsys.psd2.model.AccountReport;
import de.adorsys.psd2.model.Balance;
import de.adorsys.psd2.model.BalanceList;
import de.adorsys.psd2.model.BalanceType;
import de.adorsys.psd2.model.PurposeCode;
import de.adorsys.psd2.model.ReadBalanceResponse200;
import de.adorsys.psd2.model.TransactionDetails;
import de.adorsys.psd2.model.TransactionList;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class AccountModelMapper {
    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.instance();

    public static AccountList mapToAccountList(Map<String, List<de.adorsys.aspsp.xs2a.domain.account.AccountDetails>> accountDetailsList) {
        List<AccountDetails> details = accountDetailsList.values().stream()
                                           .flatMap(ad -> ad.stream().map(AccountModelMapper::mapToAccountDetails))
                                           .collect(Collectors.toList());
        return new AccountList().accounts(details);
    }

	public static AccountDetails mapToAccountDetails(de.adorsys.aspsp.xs2a.domain.account.AccountDetails accountDetails) {
        AccountDetails target = new AccountDetails();
        BeanUtils.copyProperties(accountDetails, target);

        // TODO fill missing values: product status usage details
        // https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/248
        target.resourceId(accountDetails.getId())
            .currency(accountDetails.getCurrency().getCurrencyCode())
            .cashAccountType(Optional.ofNullable(accountDetails.getCashAccountType())
                                 .map(Enum::name)
                                 .orElse(null));
        return target
                   .balances(mapToBalanceList(accountDetails.getBalances()))
                   ._links(OBJECT_MAPPER.convertValue(accountDetails.getLinks(), Map.class));
    }

    private static BalanceList mapToBalanceList(List<de.adorsys.aspsp.xs2a.domain.Balance> balances) {
        BalanceList balanceList = null;

        if (CollectionUtils.isNotEmpty(balances)) {
            balanceList = new BalanceList();

            balanceList.addAll(balances.stream()
                                   .map(AccountModelMapper::mapToBalance)
                                   .collect(Collectors.toList()));
        }

        return balanceList;
    }

    public static ReadBalanceResponse200 mapToBalance(List<de.adorsys.aspsp.xs2a.domain.Balance> balances) {
        BalanceList balancesResponse = new BalanceList();
        balances.forEach(balance -> balancesResponse.add(mapToBalance(balance)));

        return new ReadBalanceResponse200()
                   .balances(balancesResponse);
    }

    public static Balance mapToBalance(de.adorsys.aspsp.xs2a.domain.Balance balance) {
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

    public static AccountReport mapToAccountReport(de.adorsys.aspsp.xs2a.domain.account.AccountReport accountReport) {
        TransactionList booked = new TransactionList();
        List<TransactionDetails> bookedTransactions = Optional.ofNullable(accountReport.getBooked())
                                                          .map(ts -> Arrays.stream(ts).map(AccountModelMapper::mapToTransaction).collect(Collectors.toList()))
                                                          .orElse(new ArrayList<>());
        booked.addAll(bookedTransactions);

        TransactionList pending = new TransactionList();
        List<TransactionDetails> pendingTransactions = Optional.ofNullable(accountReport.getPending())
                                                           .map(ts -> Arrays.stream(ts).map(AccountModelMapper::mapToTransaction).collect(Collectors.toList()))
                                                           .orElse(new ArrayList<>());
        pending.addAll(pendingTransactions);

        return new AccountReport()
                   .booked(booked)
                   .pending(pending)
                   ._links(OBJECT_MAPPER.convertValue(accountReport.getLinks(), Map.class));
    }

    public static TransactionDetails mapToTransaction(Transactions transactions) {
        TransactionDetails target = new TransactionDetails();
        BeanUtils.copyProperties(transactions, target);

        target.setCreditorAccount(createAccountObject(transactions.getCreditorAccount()));
        target.setDebtorAccount(createAccountObject(transactions.getDebtorAccount()));

        // TODO fill missing values: entryReference checkId exchangeRate proprietaryBankTransactionCode links
        // https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/248
        Optional.ofNullable(transactions.getAmount())
            .ifPresent(amount -> target.setTransactionAmount(AmountModelMapper.mapToAmount(amount)));

        target.setPurposeCode(PurposeCode.fromValue(Optional.ofNullable(transactions.getPurposeCode())
                                                        .map(de.adorsys.aspsp.xs2a.domain.code.PurposeCode::getCode)
                                                        .orElse(null)));

        Optional.ofNullable(transactions.getBankTransactionCodeCode())
            .ifPresent(transactionCode -> target.setBankTransactionCode(transactionCode.getCode()));

        return target;
    }

    private static Object createAccountObject(AccountReference accountReference) {
        return Optional.ofNullable(accountReference)
                   .map(account -> {
                       if (account.getIban() != null) {
                           return new AccountReferenceIban()
                                      .iban(accountReference.getIban())
                                      .currency(getCurrencyFromAccountReference(accountReference));
                       } else if (account.getBban() != null) {
                           return new AccountReferenceBban()
                                      .bban(accountReference.getBban())
                                      .currency(getCurrencyFromAccountReference(accountReference));
                       } else if (account.getPan() != null) {
                           return new AccountReferencePan()
                                      .pan(accountReference.getPan())
                                      .currency(getCurrencyFromAccountReference(accountReference));
                       } else if (account.getMsisdn() != null) {
                           return new AccountReferenceMsisdn()
                                      .msisdn(accountReference.getMsisdn())
                                      .currency(getCurrencyFromAccountReference(accountReference));
                       } else if (account.getMaskedPan() != null) {
                           return new AccountReferenceMaskedPan()
                                      .maskedPan(accountReference.getMaskedPan())
                                      .currency(getCurrencyFromAccountReference(accountReference));
                       }

                       return null;
                   })
                   .orElse(null);
    }

    private static String getCurrencyFromAccountReference(AccountReference accountReference) {
        return Optional.ofNullable(accountReference.getCurrency())
                   .map(Currency::getCurrencyCode)
                   .orElse(null);
    }
}
