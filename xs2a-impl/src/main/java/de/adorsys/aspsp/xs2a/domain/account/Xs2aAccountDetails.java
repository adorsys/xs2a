
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

package de.adorsys.aspsp.xs2a.domain.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.adorsys.aspsp.xs2a.domain.CashAccountType;
import de.adorsys.aspsp.xs2a.domain.Links;
import de.adorsys.aspsp.xs2a.domain.Xs2aBalance;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Currency;
import java.util.List;

@Data
@ApiModel(description = "SpiAccountDetails information", value = "SpiAccountDetails")
public class Xs2aAccountDetails {

    @ApiModelProperty(value = "ID: This is the data element to be used in the path when retrieving data from a dedicated account", required = true, example = "3dc3d5b3-7023-4848-9853-f5400a64e80f")
    @Size(max = 35)
    @NotNull
    private final String id;

    @ApiModelProperty(value = "IBAN: This data element can be used in the body of the CreateConsentReq Request Message for retrieving account access consent from this payment account", example = "DE89370400440532013000")
    private final String iban;

    @ApiModelProperty(value = "BBAN: This data element can be used in the body of the CreateConsentReq Request Message for retrieving account access consent from this account, for payment accounts which have no IBAN. ", example = "89370400440532013000")
    private final String bban;

    @ApiModelProperty(value = "PAN: Primary Account Number (PAN) of a card, can be tokenized by the ASPSP due to PCI DSS requirements", example = "2356 5746 3217 1234")
    @Size(max = 35)
    private final String pan;

    @ApiModelProperty(value = "MASKEDPAN: Primary Account Number (PAN) of a card in a masked form.", example = "2356xxxxxxxx1234")
    @Size(max = 35)
    private final String maskedPan;

    @ApiModelProperty(value = "MSISDN: An alias to access a payment account via a registered mobile phone number.", example = "49057543010")
    @Size(max = 35)
    private final String msisdn;

    @ApiModelProperty(value = "Currency Type", required = true, example = "EUR")
    @NotNull
    private final Currency currency;

    @ApiModelProperty(value = "Name: Name given by the bank or the Psu in Online- Banking", example = "Main Account")
    private final String name;

    @ApiModelProperty(value = "Product Name of the Bank for this account, proprietary definition", example = "Girokonto")
    @Size(max = 35)
    private final String product;

    @ApiModelProperty(value = "Cash Account Type: PExternalCashAccountType1Code from ISO20022")
    private final CashAccountType cashAccountType;

    @ApiModelProperty(value = "Account status")
    private final AccountStatus accountStatus;

    @ApiModelProperty(value = "BIC: The BIC associated to the account.", example = "EDEKDEHHXXX")
    private final String bic;

    @ApiModelProperty(value = "Case of a set of pending card transactions, the APSP will provide the relevant cash account the card is set up on.")
    @Size(max = 70)
    private final String linkedAccounts;

    @ApiModelProperty(value = "Specifies the usage of the account")
    private final Xs2aUsageType usageType;

    @ApiModelProperty(value = "Specifications that might be provided by the ASPSP", example = "Details")
    private final String details;

    @ApiModelProperty(value = "Balances")
    private final List<Xs2aBalance> balances;

    @ApiModelProperty(value = "links: inks to the account, which can be directly used for retrieving account information from the dedicated account")
    @JsonProperty("_links")
    private Links links = new Links();
}
