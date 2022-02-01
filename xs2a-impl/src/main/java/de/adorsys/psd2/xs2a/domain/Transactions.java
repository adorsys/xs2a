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

package de.adorsys.psd2.xs2a.domain;

import de.adorsys.psd2.xs2a.core.pis.Xs2aAmount;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAdditionalInformationStructured;
import de.adorsys.psd2.xs2a.domain.code.BankTransactionCode;
import lombok.Data;

import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

@Data
public class Transactions {
    @Size(max = 35)
    private String transactionId;
    @Size(max = 35)
    private String entryReference;
    @Size(max = 35)
    private String endToEndId;
    @Size(max = 35)
    private String mandateId;
    @Size(max = 35)
    private String checkId;
    @Size(max = 35)
    private String creditorId;
    private LocalDate bookingDate;
    private LocalDate valueDate;
    private Xs2aAmount amount;
    private List<Xs2aExchangeRate> exchangeRate;
    private TransactionInfo transactionInfo;
    private BankTransactionCode bankTransactionCodeCode;
    @Size(max = 35)
    private String proprietaryBankTransactionCode;
    private String additionalInformation;
    private Xs2aAdditionalInformationStructured additionalInformationStructured;
    private Xs2aBalance balanceAfterTransaction;
    private Boolean batchIndicator;
    private Integer batchNumberOfTransactions;
    private List<EntryDetails> entryDetails;
    private Links links = new Links();
}
