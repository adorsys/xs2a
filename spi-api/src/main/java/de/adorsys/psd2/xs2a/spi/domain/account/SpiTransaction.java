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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@RequiredArgsConstructor
@JsonIgnoreProperties({"bookedTransaction", "pendingTransaction", "informationTransaction"})
public class SpiTransaction {
    private final String transactionId;
    private final String entryReference;
    private final String endToEndId;
    private final String mandateId;
    private final String checkId;
    private final String creditorId;
    private final LocalDate bookingDate;
    private final LocalDate valueDate;
    private final SpiAmount spiAmount;
    private final List<SpiExchangeRate> exchangeRate;
    private final SpiTransactionInfo spiTransactionInfo;
    private final String bankTransactionCodeCode;
    private final String proprietaryBankTransactionCode;
    private final String additionalInformation;
    private final SpiAdditionalInformationStructured additionalInformationStructured;
    private final SpiAccountBalance balanceAfterTransaction;
    private final Boolean batchIndicator;
    private final Integer batchNumberOfTransactions;
    private final List<SpiEntryDetails> entryDetails;

    public boolean isBookedTransaction() {
        return bookingDate != null;
    }

    public boolean isPendingTransaction() {
        return bookingDate == null && additionalInformationStructured == null;
    }

    public boolean isInformationTransaction() {
        return additionalInformationStructured != null;
    }
}
