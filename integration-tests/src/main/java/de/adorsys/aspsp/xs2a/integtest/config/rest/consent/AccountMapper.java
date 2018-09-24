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

package de.adorsys.aspsp.xs2a.integtest.config.rest.consent;

import de.adorsys.aspsp.xs2a.domain.*;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountDetails;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountReference;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountReport;
import de.adorsys.aspsp.xs2a.domain.code.BankTransactionCode;
import de.adorsys.aspsp.xs2a.domain.code.Xs2aPurposeCode;
import de.adorsys.aspsp.xs2a.spi.domain.account.*;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class AccountMapper {
    public Xs2aAccountDetails mapToAccountDetails(SpiAccountDetails accountDetails) {
        return Optional.ofNullable(accountDetails)
                   .map(ad -> new Xs2aAccountDetails(
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
                           null,
                           null,
                           mapToBalancesList(ad.getBalances())
                       )
                   )
                   .orElse(null);
    }

    public Xs2aAmount mapToAmount(SpiAmount spiAmount) {
        return Optional.ofNullable(spiAmount)
                   .map(a -> {
                       Xs2aAmount amount = new Xs2aAmount();
                       amount.setAmount(a.getContent().toString());
                       amount.setCurrency(a.getCurrency());
                       return amount;
                   })
                   .orElse(null);
    }

    public Optional<Xs2aAccountReport> mapToAccountReport(List<SpiTransaction> spiTransactions) {

        if (CollectionUtils.isEmpty(spiTransactions)) {
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

        return Optional.of(new Xs2aAccountReport(booked, pending));
    }

    public Xs2aAccountReference mapToAccountReference(SpiAccountReference spiAccountReference) {
        return Optional.ofNullable(spiAccountReference)
                   .map(ar -> getAccountReference(ar.getIban(), ar.getBban(), ar.getPan(), ar.getMaskedPan(), ar.getMsisdn(), ar.getCurrency()))
                   .orElse(null);

    }

    public List<SpiAccountReference> mapToSpiAccountReferences(List<Xs2aAccountReference> references) {
        return Optional.ofNullable(references)
                   .map(ref -> ref.stream()
                                   .map(this::mapToSpiAccountReference)
                                   .collect(Collectors.toList()))
                   .orElseGet(Collections::emptyList);
    }

    public SpiAccountReference mapToSpiAccountReference(Xs2aAccountReference account) {
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

    public List<Xs2aAccountReference> mapToAccountReferences(List<SpiAccountReference> references) {
        return Optional.ofNullable(references)
                   .map(ref -> ref.stream()
                                   .map(this::mapToAccountReference)
                                   .collect(Collectors.toList()))
                   .orElseGet(Collections::emptyList);
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
                       transactions.setPurposeCode(new Xs2aPurposeCode(t.getPurposeCode()));
                       transactions.setTransactionId(t.getTransactionId());
                       transactions.setRemittanceInformationStructured(t.getRemittanceInformationStructured());
                       transactions.setRemittanceInformationUnstructured(t.getRemittanceInformationUnstructured());
                       return transactions;
                   })
                   .orElse(null);
    }

    public List<Xs2aAccountReference> mapToAccountReferencesFromDetails(List<SpiAccountDetails> details) {
        return Optional.ofNullable(details)
                   .map(det -> det.stream()
                                   .map(this::mapToAccountDetails)
                                   .map(this::mapToAccountReference)
                                   .collect(Collectors.toList()))
                   .orElseGet(Collections::emptyList);
    }

    private List<Xs2aBalance> mapToBalancesList(List<SpiAccountBalance> spiBalances) {
        if (CollectionUtils.isEmpty(spiBalances)) {
            return new ArrayList<>();
        }

        return spiBalances.stream()
                   .map(this::mapToBalance)
                   .collect(Collectors.toList());
    }

    private Xs2aAccountReference mapToAccountReference(Xs2aAccountDetails details) {
        return Optional.ofNullable(details)
                   .map(det-> getAccountReference(det.getIban(), det.getBban(), det.getPan(), det.getMaskedPan(), det.getMsisdn(), det.getCurrency()))
                   .orElse(null);

    }

    private Xs2aAccountReference getAccountReference(String iban, String bban, String pan, String maskedPan, String msisdn, Currency currency) {
        Xs2aAccountReference reference = new Xs2aAccountReference();
        reference.setIban(iban);
        reference.setBban(bban);
        reference.setPan(pan);
        reference.setMaskedPan(maskedPan);
        reference.setMsisdn(msisdn);
        reference.setCurrency(currency);
        return reference;
    }

    private CashAccountType mapToAccountType(SpiAccountType spiAccountType) {
        return Optional.ofNullable(spiAccountType)
                   .map(type -> CashAccountType.valueOf(type.name()))
                   .orElse(null);
    }

    private Xs2aBalance mapToBalance(SpiAccountBalance spiAccountBalance) {
        return Optional.ofNullable(spiAccountBalance)
                   .map(b -> {
                       Xs2aBalance balance = new Xs2aBalance();
                       balance.setBalanceAmount(mapToAmount(spiAccountBalance.getSpiBalanceAmount()));
                       balance.setBalanceType(BalanceType.valueOf(spiAccountBalance.getSpiBalanceType().name()));
                       balance.setLastChangeDateTime(spiAccountBalance.getLastChangeDateTime());
                       balance.setReferenceDate(spiAccountBalance.getReferenceDate());
                       balance.setLastCommittedTransaction(spiAccountBalance.getLastCommittedTransaction());
                       return balance;
                   })
                   .orElse(null);
    }
}
