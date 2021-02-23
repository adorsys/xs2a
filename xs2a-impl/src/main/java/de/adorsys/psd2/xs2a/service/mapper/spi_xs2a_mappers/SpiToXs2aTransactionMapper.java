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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.core.payment.model.PurposeCode;
import de.adorsys.psd2.xs2a.domain.EntryDetails;
import de.adorsys.psd2.xs2a.domain.TransactionInfo;
import de.adorsys.psd2.xs2a.domain.Transactions;
import de.adorsys.psd2.xs2a.domain.code.BankTransactionCode;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiEntryDetails;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiTransaction;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiTransactionInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring",
    uses = {SpiToXs2aAmountMapper.class, SpiToXs2aBalanceMapper.class, SpiToXs2aExchangeRateMapper.class, SpiToXs2aAccountReferenceMapper.class},
    imports = {PurposeCode.class, BankTransactionCode.class})
public interface SpiToXs2aTransactionMapper {

    @Mapping(target = "amount", source = "spiAmount")
    @Mapping(target = "bankTransactionCodeCode", expression = "java(new BankTransactionCode(spiTransaction.getBankTransactionCodeCode()))")
    @Mapping(target = "transactionInfo", source = "spiTransactionInfo")
    Transactions mapToXs2aTransaction(SpiTransaction spiTransaction);

    List<Transactions> mapToXs2aTransactionList(List<SpiTransaction> spiTransactions);

    @Mapping(target = "transactionInfo", source = "spiTransactionInfo")
    EntryDetails mapToEntryDetails(SpiEntryDetails spiEntryDetails);

    List<EntryDetails> mapToEntryDetailsList(List<SpiEntryDetails> spiEntryDetails);

    @Mapping(target = "purposeCode", expression = "java(PurposeCode.fromValue(spiTransactionInfo.getPurposeCode()))")
    TransactionInfo mapToTransactionInfo(SpiTransactionInfo spiTransactionInfo);
}
