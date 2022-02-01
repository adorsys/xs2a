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
    private final LocalDate valueDate;
    private final SpiAmount transactionAmount;
    private final SpiAmount grandTotalAmount;
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

    public boolean isBookedTransaction() {
        return bookingDate != null;
    }

    public boolean isPendingTransaction() {
        return !isBookedTransaction();
    }
}
