/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.spi.domain.account;

import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiAddress;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@RequiredArgsConstructor
public class SpiCardTransaction {
    private final String cardTransactionId;
    private final String terminalId;
    private final LocalDate transactionDate;
    private final OffsetDateTime acceptorTransactionDateTime;
    private final LocalDate bookingDate;
    private final SpiAmount transactionAmount;
    private final List<SpiExchangeRate> currencyExchange;
    private final SpiAmount originalAmount;
    private final SpiAmount markupFee;
    private final String markupFeePercentage;
    private final String cardAcceptorId;
    private final SpiAddress cardAcceptorAddress;
    private final String cardAcceptorPhone;
    private final String merchantCategoryCode;
    private final String maskedPAN;
    private final String transactionDetails;
    private final Boolean invoiced;
    private final String proprietaryBankTransactionCode;


    /**
     * @param cardTransactionId ID of card transaction
     * @param terminalId Identification of the Terminal, where the card has been used
     * @param transactionDate date of the actual card transaction
     * @param bookingDate booking date of the related booking on the card account
     * @param transactionAmount The amount of the transaction as billed to the card account
     * @param currencyExchange currency exchange, often is restricted by the ASPSP to use only one exchange rate
     * @param originalAmount Original amount of the transaction at the Point of Interaction in orginal currency
     * @param markupFee Any fee related to the transaction in billing currency
     * @param markupFeePercentage Percentage of the involved transaction fee in relation to the billing amount
     * @param cardAcceptorId Identification of the Card Acceptor as given in the related card transaction
     * @param cardAcceptorAddress Address of the Card Acceptor as given in the related card transaction
     * @param merchantCategoryCode Merchant Category Code of the Card Acceptor as given in the related card transaction
     * @param maskedPAN The masked PAN of the card used in the transaction
     * @param transactionDetails Additional details given for the related card transactions
     * @param invoiced Flag indicating whether the underlying card transaction is already invoiced
     * @param proprietaryBankTransactionCode proprietary bank transaction code as used within a community or within an ASPSP
     *
     * @deprecated since 6.0/7.0, use all args constructor instead
     */
    @Deprecated // ToDo remove deprecated constructor https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/1240
    public SpiCardTransaction(String cardTransactionId, String terminalId, LocalDate transactionDate, LocalDate bookingDate,
                              SpiAmount transactionAmount, List<SpiExchangeRate> currencyExchange, SpiAmount originalAmount,
                              SpiAmount markupFee, String markupFeePercentage, String cardAcceptorId, SpiAddress cardAcceptorAddress,
                              String merchantCategoryCode, String maskedPAN, String transactionDetails,
                              Boolean invoiced, String proprietaryBankTransactionCode) {
        this(cardTransactionId, terminalId, transactionDate, null, bookingDate, transactionAmount, currencyExchange,
             originalAmount, markupFee, markupFeePercentage, cardAcceptorId, cardAcceptorAddress, null,
             merchantCategoryCode, maskedPAN, transactionDetails, invoiced, proprietaryBankTransactionCode);
    }

    public boolean isBookedTransaction() {
        return bookingDate != null;
    }

    public boolean isPendingTransaction() {
        return !isBookedTransaction();
    }
}
