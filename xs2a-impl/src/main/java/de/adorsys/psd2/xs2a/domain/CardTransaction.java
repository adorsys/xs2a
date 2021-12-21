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

import de.adorsys.psd2.xs2a.core.domain.address.Xs2aAddress;
import de.adorsys.psd2.xs2a.core.pis.Xs2aAmount;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class CardTransaction {
    private String cardTransactionId;
    private String terminalId;
    private LocalDate transactionDate;
    private OffsetDateTime acceptorTransactionDateTime;
    private LocalDate bookingDate;
    private LocalDate valueDate;
    private Xs2aAmount transactionAmount;
    private Xs2aAmount grandTotalAmount;
    private List<Xs2aExchangeRate> currencyExchange;
    private Xs2aAmount originalAmount;
    private Xs2aAmount markupFee;
    private String markupFeePercentage;
    private String cardAcceptorId;
    private Xs2aAddress cardAcceptorAddress;
    private String cardAcceptorPhone;
    private String merchantCategoryCode;
    private String maskedPAN;
    private String transactionDetails;
    private Boolean invoiced;
    private String proprietaryBankTransactionCode;
}
