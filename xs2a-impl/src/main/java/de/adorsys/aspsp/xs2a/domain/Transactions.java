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
import de.adorsys.aspsp.xs2a.domain.code.BankTransactionCode;
import de.adorsys.aspsp.xs2a.domain.code.PurposeCode;
import de.adorsys.aspsp.xs2a.web.util.JsonFormatDateUTC;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;
import java.util.Date;

@Data
@ApiModel(description = "TransactionsCreditorResponse information", value = "TransactionsCreditorResponse")
@JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
public class Transactions {

    @ApiModelProperty(value = "Can be used as access-ID in the API, where more details on an transaction is offered.", required = false, example = "1234567")
    @Size(max = 35)
    private String transactionId;

    @ApiModelProperty(value = "End to end id", required = false, example = "123456789")
    @Size(max = 35)
    private String endToEndId;

    @ApiModelProperty(value = "Identification of Mandates, e.g. a SEPA Mandate ID", required = false, example = "12345")
    @Size(max = 35)
    private String mandateId;

    @ApiModelProperty(value = "Identification of Creditors, e.g. a SEPA Creditor ID", required = false, example = "12345")
    @Size(max = 35)
    private String creditorId;

    @ApiModelProperty(value = "Booking Date", example = "2017-01-01")
    @JsonFormatDateUTC
    private Date bookingDate;

    @ApiModelProperty(value = "Value Date", example = "2017-01-01")
    @JsonFormatDateUTC
    private Date valueDate;

    @ApiModelProperty(value = "Amount", required = true)
    private Amount amount;

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
    private PurposeCode purposeCode;

    @ApiModelProperty(value = "Bank transaction code as used by the ASPSP in ISO20022 related formats.")
    private BankTransactionCode bankTransactionCodeCode;
}
