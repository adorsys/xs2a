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
import de.adorsys.aspsp.xs2a.domain.account.AccountDetails;
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.account.AccountReport;
import de.adorsys.aspsp.xs2a.domain.code.BankTransactionCode;
import de.adorsys.aspsp.xs2a.domain.code.PurposeCode;
import de.adorsys.aspsp.xs2a.spi.domain.account.*;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class AccountMapper {
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

    public AccountReference mapToAccountReference(SpiAccountReference spiAccountReference) {
        return Optional.ofNullable(spiAccountReference)
                   .map(ar -> getAccountReference(ar.getIban(), ar.getBban(), ar.getPan(), ar.getMaskedPan(), ar.getMsisdn(), ar.getCurrency()))
                   .orElse(null);

    }

    public List<SpiAccountReference> mapToSpiAccountReferences(List<AccountReference> references) {
        return Optional.ofNullable(references)
                   .map(ref -> ref.stream()
                                   .map(this::mapToSpiAccountReference)
                                   .collect(Collectors.toList()))
                   .orElseGet(Collections::emptyList);
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

    public List<AccountReference> mapToAccountReferences(List<SpiAccountReference> references) {
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
                       transactions.setPurposeCode(new PurposeCode(t.getPurposeCode()));
                       transactions.setTransactionId(t.getTransactionId());
                       transactions.setRemittanceInformationStructured(t.getRemittanceInformationStructured());
                       transactions.setRemittanceInformationUnstructured(t.getRemittanceInformationUnstructured());
                       return transactions;
                   })
                   .orElse(null);
    }

    public List<AccountReference> mapToAccountReferencesFromDetails(List<SpiAccountDetails> details) {
        return Optional.ofNullable(details)
                   .map(det -> det.stream()
                                   .map(this::mapToAccountDetails)
                                   .map(this::mapToAccountReference)
                                   .collect(Collectors.toList()))
                   .orElseGet(Collections::emptyList);
    }

    private List<Balance> mapToBalancesList(List<SpiAccountBalance> spiBalances) {
        if (CollectionUtils.isEmpty(spiBalances)) {
            return new ArrayList<>();
        }

        return spiBalances.stream()
                   .map(this::mapToBalance)
                   .collect(Collectors.toList());
    }

    private AccountReference mapToAccountReference(AccountDetails details) {
        return Optional.ofNullable(details)
                   .map(det -> getAccountReference(det.getIban(), det.getBban(), det.getPan(), det.getMaskedPan(), det.getMsisdn(), det.getCurrency()))
                   .orElse(null);

    }

    private AccountReference getAccountReference(String iban, String bban, String pan, String maskedPan, String msisdn, Currency currency) {
        AccountReference reference = new AccountReference();
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

    private Balance mapToBalance(SpiAccountBalance spiAccountBalance) {
        return Optional.ofNullable(spiAccountBalance)
                   .map(b -> {
                       Balance balance = new Balance();
                       balance.setBalanceAmount(mapToAmount(spiAccountBalance.getSpiBalanceAmount()));
                       balance.setBalanceType(BalanceType.valueOf(spiAccountBalance.getSpiBalanceType().name()));
                       balance.setLastChangeDateTime(spiAccountBalance.getLastChangeDateTime());
                       balance.setReferenceDate(spiAccountBalance.getReferenceDate());
                       balance.setLastCommittedTransaction(spiAccountBalance.getLastCommittedTransaction());
                       return balance;
                   })
                   .orElse(null);
    }

    public List<AccountDetails> mapToAccountDetailsListNoBalances(List<AccountDetails> details) {
        return details.stream()
                   .map(this::mapToAccountDetailNoBalances)
                   .collect(Collectors.toList());
    }

    public AccountDetails mapToAccountDetailNoBalances(AccountDetails detail) {
        return new AccountDetails(detail.getId(), detail.getIban(), detail.getBban(), detail.getPan(),
            detail.getMaskedPan(), detail.getMsisdn(), detail.getCurrency(), detail.getName(),
            detail.getAccountType(), detail.getCashAccountType(), detail.getBic(), null);
    }



    public List<AccountDetails> mapToAccountDetailsNoBalances(List<AccountDetails> details) {
        return details.stream()
                   .map(this::mapToAccountDetailNoBalances)
                   .collect(Collectors.toList());
    }

    public AccountDetails mapToAccountDetailNoBalances(AccountDetails detail) {
        return new AccountDetails(detail.getId(), detail.getIban(), detail.getBban(), detail.getPan(),
            detail.getMaskedPan(), detail.getMsisdn(), detail.getCurrency(), detail.getName(),
            detail.getAccountType(), detail.getCashAccountType(), detail.getBic(), null);
    }
}
