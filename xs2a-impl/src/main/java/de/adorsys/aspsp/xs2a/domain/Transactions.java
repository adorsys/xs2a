/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.aspsp.xs2a.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.code.BankTransactionCode;
import de.adorsys.aspsp.xs2a.domain.code.Xs2aPurposeCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

@Data
@ApiModel(description = "TransactionsCreditorResponse information", value = "TransactionsCreditorResponse")
@JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
public class Transactions {

    @ApiModelProperty(value = "Can be used as access-ID in the API, where more details on an transaction is offered.", required = false, example = "1234567")
    @Size(max = 35)
    private String transactionId;

    @ApiModelProperty(value = "Identifier of the transaction as used e.g. for reference for deltafunction on application level. The same identification as for example used within camt.05x messages.", example = "12345")
    @Size(max = 35)
    private String entryReference;

    @ApiModelProperty(value = "End to end id", required = false, example = "123456789")
    @Size(max = 35)
    private String endToEndId;

    @ApiModelProperty(value = "Identifier of Mandates, e.g. a SEPA Mandate ID", required = false, example = "12345")
    @Size(max = 35)
    private String mandateId;

    @ApiModelProperty(value = "Identifier of a Cheque", example = "1234567")
    @Size(max = 35)
    private String checkId;

    @ApiModelProperty(value = "Identifier of Creditors, e.g. a SEPA Creditor ID", required = false, example = "12345")
    @Size(max = 35)
    private String creditorId;

    @ApiModelProperty(value = "Booking Date", example = "2017-01-01")
    private LocalDate bookingDate;

    @ApiModelProperty(value = "Value Date", example = "2017-01-01")
    private LocalDate valueDate;

    @ApiModelProperty(value = "Amount", required = true)
    private Xs2aAmount amount;

    @ApiModelProperty(value = "Array of Exchange Rate")
    private List<Xs2aExchangeRate> exchangeRate;

    @ApiModelProperty(value = "Name of the Creditor if a debited transaction", example = "John Miles")
    @Size(max = 70)
    private String creditorName;

    @ApiModelProperty(value = "Creditor account")
    private AccountReference creditorAccount;

    @ApiModelProperty(value = "Name of the last creditor", example = "Paul Simpson")
    @Size(max = 70)
    private String ultimateCreditor;

    @ApiModelProperty(value = "Name of the debtor if a “Credited” transaction", example = "Jan")
    private String debtorName;

    @ApiModelProperty(value = "Debtor account")
    private AccountReference debtorAccount;

    @ApiModelProperty(value = "Name of the last debtor", example = "Max")
    @Size(max = 70)
    private String ultimateDebtor;

    @ApiModelProperty(value = "Remittance information unstructured", example = "Ref Number Merchant")
    @Size(max = 140)
    private String remittanceInformationUnstructured;

    @ApiModelProperty(value = "Remittance information structured;", example = "Ref Number Merchant")
    @Size(max = 140)
    private String remittanceInformationStructured;

    @ApiModelProperty(value = "Purpose code")
    private Xs2aPurposeCode purposeCode;

    @ApiModelProperty(value = "Bank transaction code as used by the ASPSP in ISO20022 related formats.")
    private BankTransactionCode bankTransactionCodeCode;

    @ApiModelProperty(value = "Proprietary bank transaction code as used within a community or within an ASPSP e.g. for MT94x based transaction reports", example = "12345")
    @Size(max = 35)
    private String proprietaryBankTransactionCode;

    @ApiModelProperty(value = "The following links could be used for retrieving details of a transaction")
    @JsonProperty("_links")
    private Links links = new Links();
}
