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
