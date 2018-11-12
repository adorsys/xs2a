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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.xs2a.domain.Transactions;
import de.adorsys.psd2.xs2a.domain.code.BankTransactionCode;
import de.adorsys.psd2.xs2a.domain.code.Xs2aPurposeCode;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiTransaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SpiToXs2aTransactionMapper {
    private final SpiToXs2aAmountMapper amountMapper;
    private final SpiToXs2aExchangeRateMapper exchangeRateMapper;
    private final SpiToXs2aAccountReferenceMapper accountReferenceMapper;

    public Transactions mapToXs2aTransaction(SpiTransaction spiTransaction) {
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
                       transactions.setAmount(amountMapper.mapToXs2aAmount(t.getSpiAmount()));
                       transactions.setExchangeRate(exchangeRateMapper.mapToExchangeRateList(t.getExchangeRate()));
                       transactions.setCreditorName(t.getCreditorName());
                       transactions.setCreditorAccount(accountReferenceMapper.mapToXs2aAccountReference(t.getCreditorAccount()).orElse(null));
                       transactions.setUltimateCreditor(t.getUltimateCreditor());
                       transactions.setDebtorName(t.getDebtorName());
                       transactions.setDebtorAccount(accountReferenceMapper.mapToXs2aAccountReference(t.getDebtorAccount()).orElse(null));
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
}
