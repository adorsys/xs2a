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
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountDetails;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountReference;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountReport;
import de.adorsys.aspsp.xs2a.domain.code.BankTransactionCode;
import de.adorsys.aspsp.xs2a.domain.code.Xs2aPurposeCode;
import de.adorsys.aspsp.xs2a.spi.domain.account.*;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
                           ad.getAccountType(),
                           mapToAccountType(ad.getCashSpiAccountType()),
                           ad.getBic(),
                           null,
                           null,
                           mapToXs2aBalanceList(ad.getBalances())
                       )
                   )
                   .orElse(null);
    }

    public Xs2aAmount mapToXs2aAmount(SpiAmount spiAmount) {
        return Optional.ofNullable(spiAmount)
                   .map(a -> {
                       Xs2aAmount amount = new Xs2aAmount();
                       amount.setAmount(a.getContent().toString());
                       amount.setCurrency(a.getCurrency());
                       return amount;
                   })
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
                   .map(spiReference -> {
                       Xs2aAccountReference reference = new Xs2aAccountReference();
                       reference.setIban(spiReference.getIban());
                       reference.setBban(spiReference.getBban());
                       reference.setPan(spiReference.getPan());
                       reference.setMaskedPan(spiReference.getMaskedPan());
                       reference.setMsisdn(spiReference.getMsisdn());
                       reference.setCurrency(spiReference.getCurrency());
                       return reference;
                   })
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
                       transactions.setAmount(mapToXs2aAmount(t.getSpiAmount()));
                       transactions.setBankTransactionCodeCode(new BankTransactionCode(t.getBankTransactionCodeCode()));
                       transactions.setBookingDate(t.getBookingDate());
                       transactions.setValueDate(t.getValueDate());
                       transactions.setCreditorAccount(mapToXs2aAccountReference(t.getCreditorAccount()));
                       transactions.setDebtorAccount(mapToXs2aAccountReference(t.getDebtorAccount()));
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
                   .map(d -> {
                       Xs2aAccountReference reference = new Xs2aAccountReference();
                       reference.setIban(d.getIban());
                       reference.setBban(d.getBban());
                       reference.setPan(d.getPan());
                       reference.setMaskedPan(d.getMaskedPan());
                       reference.setMsisdn(d.getMsisdn());
                       reference.setCurrency(d.getCurrency());
                       return reference;
                   })
                   .orElse(null);

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
            detail.getProduct(), detail.getCashAccountType(), detail.getBic(), null, null, null);
    }
}
