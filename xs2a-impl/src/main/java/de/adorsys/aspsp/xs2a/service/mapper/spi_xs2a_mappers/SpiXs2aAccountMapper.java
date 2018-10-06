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

package de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.aspsp.xs2a.domain.*;
import de.adorsys.aspsp.xs2a.domain.account.*;
import de.adorsys.aspsp.xs2a.domain.code.BankTransactionCode;
import de.adorsys.aspsp.xs2a.domain.code.Xs2aPurposeCode;
import de.adorsys.aspsp.xs2a.spi.domain.account.*;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class SpiXs2aAccountMapper {
    public Xs2aAccountDetails mapToXs2aAccountDetails(SpiAccountDetails accountDetails) {
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
                           ad.getProduct(),
                           mapToAccountType(ad.getCashSpiAccountType()),
                           mapToAccountStatus(ad.getSpiAccountStatus()),
                           ad.getBic(),
                           ad.getLinkedAccounts(),
                           mapToXs2aUsageType(ad.getUsageType()),
                           ad.getDetails(),
                           mapToXs2aBalanceList(ad.getBalances())
                       )
                   )
                   .orElse(null);
    }

    public Xs2aAmount mapToXs2aAmount(SpiAmount spiAmount) {
        return Optional.ofNullable(spiAmount)
                   .map(a -> {
                       Xs2aAmount amount = new Xs2aAmount();
                       amount.setAmount(a.getAmount().toString());
                       amount.setCurrency(a.getCurrency());
                       return amount;
                   })
                   .orElse(null);
    }

    public SpiAmount mapToSpiAmount(Xs2aAmount amount) {
        return Optional.ofNullable(amount)
                   .map(am -> new SpiAmount(am.getCurrency(), new BigDecimal(am.getAmount())))
                   .orElse(null);
    }

    public Optional<Xs2aAccountReport> mapToXs2aAccountReport(List<SpiTransaction> spiTransactions) {

        if (spiTransactions.isEmpty()) {
            return Optional.empty();
        }

        Transactions[] booked = spiTransactions
                                    .stream()
                                    .filter(transaction -> transaction.getBookingDate() != null)
                                    .map(this::mapToXs2aTransaction)
                                    .toArray(Transactions[]::new);

        Transactions[] pending = spiTransactions
                                     .stream()
                                     .filter(transaction -> transaction.getBookingDate() == null)
                                     .map(this::mapToXs2aTransaction)
                                     .toArray(Transactions[]::new);

        return Optional.of(new Xs2aAccountReport(booked, pending));
    }

    public Xs2aAccountReference mapToXs2aAccountReference(SpiAccountReference spiAccountReference) {
        return Optional.ofNullable(spiAccountReference)
                   .map(spiReference -> getXs2aAccountReference(spiReference.getIban(), spiReference.getBban(),
                       spiReference.getPan(), spiReference.getMaskedPan(), spiReference.getMsisdn(),
                       spiReference.getCurrency()))
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

    public List<Xs2aAccountReference> mapToXs2aAccountReferences(List<SpiAccountReference> references) {
        return Optional.ofNullable(references)
                   .map(ref -> ref.stream()
                                   .map(this::mapToXs2aAccountReference)
                                   .collect(Collectors.toList()))
                   .orElseGet(Collections::emptyList);
    }

    private Transactions mapToXs2aTransaction(SpiTransaction spiTransaction) {
        return Optional.ofNullable(spiTransaction)
                   .map(t -> {
                       Transactions transactions = new Transactions();
                       transactions.setTransactionId(t.getTransactionId());
                       transactions.setEntryReference(t.getEntryReference());
                       transactions.setEndToEndId(t.getEndToEndId());
                       transactions.setMandateId(t.getMandateId());
                       transactions.setCheckId(t.getCheckId());
                       transactions.setCreditorId(t.getCreditorId());
                       transactions.setBookingDate(t.getBookingDate());
                       transactions.setValueDate(t.getValueDate());
                       transactions.setAmount(mapToXs2aAmount(t.getSpiAmount()));
                       transactions.setExchangeRate(mapToExchangeRateList(t.getExchangeRate()));
                       transactions.setCreditorName(t.getCreditorName());
                       transactions.setCreditorAccount(mapToXs2aAccountReference(t.getCreditorAccount()));
                       transactions.setUltimateCreditor(t.getUltimateCreditor());
                       transactions.setDebtorName(t.getDebtorName());
                       transactions.setDebtorAccount(mapToXs2aAccountReference(t.getDebtorAccount()));
                       transactions.setUltimateDebtor(t.getUltimateDebtor());
                       transactions.setRemittanceInformationUnstructured(t.getRemittanceInformationUnstructured());
                       transactions.setRemittanceInformationStructured(t.getRemittanceInformationStructured());
                       transactions.setPurposeCode(new Xs2aPurposeCode(t.getPurposeCode()));
                       transactions.setBankTransactionCodeCode(new BankTransactionCode(t.getBankTransactionCodeCode()));
                       transactions.setProprietaryBankTransactionCode(t.getProprietaryBankTransactionCode());
                       return transactions;
                   })
                   .orElse(null);
    }

    public List<Xs2aAccountReference> mapToXs2aAccountReferencesFromDetails(List<SpiAccountDetails> details) {
        return Optional.ofNullable(details)
                   .map(det -> det.stream()
                                   .map(this::mapToXs2aAccountDetails)
                                   .map(this::mapToXs2aAccountReference)
                                   .collect(Collectors.toList()))
                   .orElseGet(Collections::emptyList);
    }

    private List<Xs2aBalance> mapToXs2aBalanceList(List<SpiAccountBalance> spiBalances) {
        if (CollectionUtils.isEmpty(spiBalances)) {
            return new ArrayList<>();
        }
        return spiBalances.stream()
                   .map(this::mapToBalance)
                   .collect(Collectors.toList());
    }

    public Xs2aAccountReference mapToXs2aAccountReference(Xs2aAccountDetails details) {
        return Optional.ofNullable(details)
                   .map(d -> getXs2aAccountReference(d.getIban(), d.getBban(), d.getPan(), d.getMaskedPan(), d.getMsisdn(), d.getCurrency()))
                   .orElse(null);
    }

    private List<Xs2aExchangeRate> mapToExchangeRateList(List<SpiExchangeRate> spiExchangeRates) {
        if (CollectionUtils.isEmpty(spiExchangeRates)) {
            return new ArrayList<>();
        }

        return spiExchangeRates.stream()
                   .map(this::mapToExchangeRate)
                   .collect(Collectors.toList());
    }

    private Xs2aExchangeRate mapToExchangeRate(SpiExchangeRate spiExchangeRate) {
        return Optional.ofNullable(spiExchangeRate)
                   .map(e -> {
                       Xs2aExchangeRate exchangeRate = new Xs2aExchangeRate();
                       exchangeRate.setCurrencyFrom(e.getCurrencyFrom());
                       exchangeRate.setRateFrom(e.getRateFrom());
                       exchangeRate.setCurrencyTo(e.getCurrencyTo());
                       exchangeRate.setRateTo(e.getRateTo());
                       exchangeRate.setRateDate(e.getRateDate());
                       exchangeRate.setRateContract(e.getRateContract());
                       return exchangeRate;
                   })
                   .orElse(null);
    }

    private Xs2aAccountReference getXs2aAccountReference(String iban, String bban, String pan, String maskedPan, String msisdn, Currency currency) {
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

    private AccountStatus mapToAccountStatus(SpiAccountStatus spiAccountStatus) {
        return Optional.ofNullable(spiAccountStatus)
                   .map(status -> AccountStatus.valueOf(status.name()))
                   .orElse(null);
    }

    private Xs2aUsageType mapToXs2aUsageType(SpiUsageType spiUsageType) {
        return Optional.ofNullable(spiUsageType)
                   .map(usage -> Xs2aUsageType.valueOf(usage.name()))
                   .orElse(null);
    }

    private Xs2aBalance mapToBalance(SpiAccountBalance spiAccountBalance) {
        return Optional.ofNullable(spiAccountBalance)
                   .map(b -> {
                       Xs2aBalance balance = new Xs2aBalance();
                       balance.setBalanceAmount(mapToXs2aAmount(spiAccountBalance.getSpiBalanceAmount()));
                       balance.setBalanceType(BalanceType.valueOf(spiAccountBalance.getSpiBalanceType().name()));
                       balance.setLastChangeDateTime(spiAccountBalance.getLastChangeDateTime());
                       balance.setReferenceDate(spiAccountBalance.getReferenceDate());
                       balance.setLastCommittedTransaction(spiAccountBalance.getLastCommittedTransaction());
                       return balance;
                   })
                   .orElse(null);
    }

    public List<Xs2aAccountDetails> mapToAccountDetailsListNoBalances(List<Xs2aAccountDetails> details) {
        return details.stream()
                   .map(this::mapToAccountDetailNoBalances)
                   .collect(Collectors.toList());
    }

    public Xs2aAccountDetails mapToAccountDetailNoBalances(Xs2aAccountDetails detail) {
        return new Xs2aAccountDetails(detail.getId(), detail.getIban(), detail.getBban(), detail.getPan(),
            detail.getMaskedPan(), detail.getMsisdn(), detail.getCurrency(), detail.getName(),
            detail.getProduct(), detail.getCashAccountType(), detail.getAccountStatus(), detail.getBic(), detail.getLinkedAccounts(), detail.getUsageType(), detail.getDetails(), null);
    }
}
