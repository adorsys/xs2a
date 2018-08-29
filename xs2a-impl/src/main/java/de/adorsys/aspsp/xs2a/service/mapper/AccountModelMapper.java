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
import de.adorsys.aspsp.xs2a.domain.Amount;
import de.adorsys.aspsp.xs2a.domain.Balance;
import de.adorsys.aspsp.xs2a.domain.Transactions;
import de.adorsys.aspsp.xs2a.domain.account.AccountDetails;
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.account.AccountReport;
import de.adorsys.psd2.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public final class AccountModelMapper {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static AccountList mapToAccountList(Map<String, List<AccountDetails>> accountDetailsList) {
        List<de.adorsys.psd2.model.AccountDetails> details = accountDetailsList.values().stream()
                                                                 .flatMap(accountDetails -> accountDetails.stream().map(AccountModelMapper::mapToAccountDetails))
                                                                 .collect(Collectors.toList());
        return new AccountList().accounts(details);
    }

    public static de.adorsys.psd2.model.AccountDetails mapToAccountDetails(AccountDetails accountDetails) {
        de.adorsys.psd2.model.AccountDetails target = new de.adorsys.psd2.model.AccountDetails();
        BeanUtils.copyProperties(accountDetails, target);

        // TODO fill missing values: product status usage details
        // https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/248
        target.resourceId(accountDetails.getId())
            .currency(accountDetails.getCurrency().getCurrencyCode())
            .cashAccountType(Optional.ofNullable(accountDetails.getCashAccountType()).map(Enum::name).orElse(null));

        BalanceList balances = new BalanceList();

        accountDetails.getBalances().forEach(balance -> balances.add(mapToBalance(balance)));

        return target
                   .balances(balances)
                   ._links(OBJECT_MAPPER.convertValue(accountDetails.getLinks(), Map.class));
    }

    public static ReadBalanceResponse200 mapToBalance(List<Balance> balances) {
        BalanceList balancesResponse = new BalanceList();
        balances.forEach(balance -> balancesResponse.add(mapToBalance(balance)));

        return new ReadBalanceResponse200()
                   .balances(balancesResponse);
    }

    public static de.adorsys.psd2.model.Balance mapToBalance(Balance balance) {
        de.adorsys.psd2.model.Balance target = new de.adorsys.psd2.model.Balance();
        BeanUtils.copyProperties(balance, target);

        target.setBalanceAmount(mapToAmount(balance.getBalanceAmount()));

        Optional.ofNullable(balance.getBalanceType())
            .ifPresent(balanceType -> target.setBalanceType(BalanceType.fromValue(balanceType.getValue())));

        Optional.ofNullable(balance.getLastChangeDateTime())
            .ifPresent(lastChangeDateTime -> {
                List<ZoneOffset> validOffsets = ZoneId.systemDefault().getRules().getValidOffsets(lastChangeDateTime);
                target.setLastChangeDateTime(lastChangeDateTime.atOffset(validOffsets.get(0)));
            });

        return target;
    }

    private static de.adorsys.psd2.model.Amount mapToAmount(Amount amount) {
        return new de.adorsys.psd2.model.Amount()
                   .amount(amount.getContent())
                   .currency(amount.getCurrency().getCurrencyCode());
    }

    public static de.adorsys.psd2.model.AccountReport mapToAccountReport(AccountReport accountReport) {
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

        return new de.adorsys.psd2.model.AccountReport()
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
            .ifPresent(amount -> target.setTransactionAmount(mapToAmount(amount)));

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
