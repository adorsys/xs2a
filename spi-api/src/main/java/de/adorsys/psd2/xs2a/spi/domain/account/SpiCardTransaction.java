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
