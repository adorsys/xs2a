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
