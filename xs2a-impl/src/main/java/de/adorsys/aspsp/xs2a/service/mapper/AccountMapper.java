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

import de.adorsys.aspsp.xs2a.domain.*;
import de.adorsys.aspsp.xs2a.domain.code.BankTransactionCode;
import de.adorsys.aspsp.xs2a.domain.code.PurposeCode;
import de.adorsys.aspsp.xs2a.spi.domain.account.*;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class AccountMapper {
    public List<AccountDetails> mapToAccountDetailsList(List<SpiAccountDetails> spiAccountDetailsList) {
        return Optional.ofNullable(spiAccountDetailsList)
               .map(acl -> acl.stream()
                           .map(this::mapToAccountDetails)
                           .collect(Collectors.toList()))
               .orElse(Collections.emptyList());
    }

    public AccountDetails mapToAccountDetails(SpiAccountDetails accountDetails) {
        return Optional.ofNullable(accountDetails)
               .map(ad -> new AccountDetails(
               ad.getId(),
               ad.getIban(),
               ad.getBban(),
               ad.getPan(),
               ad.getMaskedPan(),
               ad.getMsisdn(),
               ad.getCurrency(),
               ad.getName(),
               ad.getAccountType(),
               mapToAccountType(ad.getCashSpiAccountType()),
               ad.getBic(),
               mapToBalancesList(ad.getBalances())
               )
               )
               .orElse(null);
    }

    private CashAccountType mapToAccountType(SpiAccountType spiAccountType) {
        return Optional.ofNullable(spiAccountType)
               .map(type -> CashAccountType.valueOf(type.name()))
               .orElse(null);
    }

    public List<Balances> mapToBalancesList(List<SpiBalances> spiBalances) {
        if (CollectionUtils.isEmpty(spiBalances)) {
            return new ArrayList<>();
        }

        return spiBalances
               .stream()
               .map(this::mapToBalances)
               .collect(Collectors.toList());
    }

    private Balances mapToBalances(SpiBalances spiBalances) {
        return Optional.ofNullable(spiBalances)
               .map(b -> {
                   Balances balances = new Balances();
                   balances.setAuthorised(mapToSingleBalance(b.getAuthorised()));
                   balances.setClosingBooked(mapToSingleBalance(b.getClosingBooked()));
                   balances.setExpected(mapToSingleBalance(b.getExpected()));
                   balances.setInterimAvailable(mapToSingleBalance(b.getInterimAvailable()));
                   balances.setOpeningBooked(mapToSingleBalance(b.getOpeningBooked()));
                   return balances;
               })
               .orElse(null);
    }

    private SingleBalance mapToSingleBalance(SpiAccountBalance spiAccountBalance) {
        return Optional.ofNullable(spiAccountBalance)
               .map(b -> {
                   SingleBalance singleBalance = new SingleBalance();
                   singleBalance.setAmount(mapToAmount(b.getSpiAmount()));
                   singleBalance.setDate(b.getDate().toInstant());
                   singleBalance.setLastActionDateTime(b.getLastActionDateTime().toInstant());
                   return singleBalance;
               })
               .orElse(null);
    }

    public Amount mapToAmount(SpiAmount spiAmount) {
        return Optional.ofNullable(spiAmount)
               .map(a -> {
                   Amount amount = new Amount();
                   amount.setContent(a.getContent().toString());
                   amount.setCurrency(a.getCurrency());
                   return amount;
               })
               .orElse(null);
    }

    public SpiAmount mapToSpiAmount(Amount amount) {
        return Optional.ofNullable(amount)
                   .map(am -> new SpiAmount(am.getCurrency(), new BigDecimal(am.getContent())))
                   .orElse(null);
    }

    public Optional<AccountReport> mapToAccountReport(List<SpiTransaction> spiTransactions) {

        if (spiTransactions.isEmpty()) {
            return Optional.empty();
        }

        Transactions[] booked = spiTransactions
                                .stream()
                                .filter(transaction -> transaction.getBookingDate() != null)
                                .map(this::mapToTransaction)
                                .toArray(Transactions[]::new);

        Transactions[] pending = spiTransactions
                                 .stream()
                                 .filter(transaction -> transaction.getBookingDate() == null)
                                 .map(this::mapToTransaction)
                                 .toArray(Transactions[]::new);

        return Optional.of(new AccountReport(booked, pending));
    }

    private Transactions mapToTransaction(SpiTransaction spiTransaction) {
        return Optional.ofNullable(spiTransaction)
               .map(t -> {
                   Transactions transactions = new Transactions();
                   transactions.setAmount(mapToAmount(t.getSpiAmount()));
                   transactions.setBankTransactionCodeCode(new BankTransactionCode(t.getBankTransactionCodeCode()));
                   transactions.setBookingDate(t.getBookingDate());
                   transactions.setValueDate(t.getValueDate());
                   transactions.setCreditorAccount(mapToAccountReference(t.getCreditorAccount()));
                   transactions.setDebtorAccount(mapToAccountReference(t.getDebtorAccount()));
                   transactions.setCreditorId(t.getCreditorId());
                   transactions.setCreditorName(t.getCreditorName());
                   transactions.setUltimateCreditor(t.getUltimateCreditor());
                   transactions.setDebtorName(t.getDebtorName());
                   transactions.setUltimateDebtor(t.getUltimateDebtor());
                   transactions.setEndToEndId(t.getEndToEndId());
                   transactions.setMandateId(t.getMandateId());
                   transactions.setPurposeCode(new PurposeCode(t.getPurposeCode()));
                   transactions.setTransactionId(t.getTransactionId());
                   transactions.setRemittanceInformationStructured(t.getRemittanceInformationStructured());
                   transactions.setRemittanceInformationUnstructured(t.getRemittanceInformationUnstructured());
                   return transactions;
               })
               .orElse(null);
    }

    private AccountReference mapToAccountReference(SpiAccountReference spiAccountReference) {
        return Optional.ofNullable(spiAccountReference)
               .map(ar -> {
                   AccountReference accountReference = new AccountReference();
                   accountReference.setIban(ar.getIban());
                   accountReference.setBban(ar.getBban());
                   accountReference.setPan(ar.getPan());
                   accountReference.setMaskedPan(ar.getMaskedPan());
                   accountReference.setMsisdn(ar.getMsisdn());
                   accountReference.setCurrency(ar.getCurrency());
                   return accountReference;
               })
               .orElse(null);

    }

    public SpiAccountReference mapToSpiAccountReference(AccountReference account) {
        return Optional.ofNullable(account)
               .map(ac -> new SpiAccountReference(
               ac.getIban(),
               ac.getBban(),
               ac.getPan(),
               ac.getMaskedPan(),
               ac.getMsisdn(),
               ac.getCurrency()))
               .orElse(null);
    }

}
