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
