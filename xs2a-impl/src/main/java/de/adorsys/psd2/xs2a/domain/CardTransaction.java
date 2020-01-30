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

import com.fasterxml.jackson.annotation.JsonInclude;
import de.adorsys.psd2.xs2a.core.domain.address.Xs2aAddress;
import de.adorsys.psd2.xs2a.core.pis.Xs2aAmount;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@ApiModel(description = "CardTransactions information", value = "TransactionsCreditorResponse")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CardTransaction {

    @ApiModelProperty(value = "Can be used as access-ID in the API, where more details on an transaction is offered.", required = false, example = "1234567")
    private String cardTransactionId;

    @ApiModelProperty(value = "Identification of the Terminal, where the card has been used.", example = "1234567")
    private String terminalId;

    @ApiModelProperty(value = "Date of the actual card transaction", example = "1234567")
    private LocalDate transactionDate;

    @ApiModelProperty(value = "Booking date of the related booking on the card account", example = "1234567")
    private LocalDate bookingDate;

    @ApiModelProperty(value = "The amount of the transaction as billed to the card account.", example = "1234567")
    private Xs2aAmount transactionAmount;

    @ApiModelProperty(value = "For card accounts, only one exchange rate is used.")
    private List<Xs2aExchangeRate> currencyExchange;

    @ApiModelProperty(value = "Original amount of the transaction at the Point of Interaction in orginal currency", example = "1234567")
    private Xs2aAmount originalAmount;

    @ApiModelProperty(value = "Any fee related to the transaction in billing currency.", example = "1234567")
    private Xs2aAmount markupFee;

    @ApiModelProperty(value = "Percentage of the involved transaction fee in relation to the billing amount.", example = "1234567")
    private String markupFeePercentage;

    @ApiModelProperty(value = "Identification of the Card Acceptor (e.g. merchant) as given in the related card transaction.", example = "1234567")
    private String cardAcceptorId;

    @ApiModelProperty(value = "Address of the Card Acceptor as given in the related card transaction.", example = "1234567")
    private Xs2aAddress cardAcceptorAddress;

    @ApiModelProperty(value = "Card Acceptor Category Code of the Card Acceptor as given in the related card transaction.", example = "1234567")
    private String merchantCategoryCode;

    @ApiModelProperty(value = "The masked PAN of the card used in the transaction.", example = "1234567")
    private String maskedPAN;

    @ApiModelProperty(value = "Additional details given for the related card transactions.", example = "1234567")
    private String transactionDetails;

    @ApiModelProperty(value = "Flag indicating whether the underlying card transaction is already invoiced.", example = "1234567")
    private Boolean invoiced;

    @ApiModelProperty(value = "Proprietary bank transaction code as used within a community or within an ASPSP e.g. for MT94x based transaction reports", example = "1234567")
    private String proprietaryBankTransactionCode;
}
