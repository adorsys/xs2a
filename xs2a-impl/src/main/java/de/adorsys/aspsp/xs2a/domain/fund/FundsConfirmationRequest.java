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

package de.adorsys.aspsp.xs2a.domain.fund;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.Amount;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(description = "Request for the Confirmation Funds")
public class FundsConfirmationRequest {

    @ApiModelProperty(value = "Card Number of the card issued by the PIISP. Must be delivered if available.", example = "12345")
    private  String cardNumber;

    @NotNull
    @ApiModelProperty(value = "PSU’s account number.", required = true)
    private  AccountReference psuAccount;

    @ApiModelProperty(value = "The merchant where the card is accepted as an information to the PSU.", example = "Check24")
    private  String payee;

    @NotNull
    @ApiModelProperty(value = "Transaction amount to be checked within the funds check mechanism.", required = true)
    private  Amount instructedAmount;
}

