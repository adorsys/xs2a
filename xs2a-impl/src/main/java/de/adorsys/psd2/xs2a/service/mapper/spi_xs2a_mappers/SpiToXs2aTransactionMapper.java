/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
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
