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
import de.adorsys.aspsp.xs2a.domain.Links;
import de.adorsys.aspsp.xs2a.domain.Xs2aBalance;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "Transactions Report", value = "TransactionsReport")
public class TransactionsReport {

    @ApiModelProperty(value = "AccountReference")
    private AccountReference accountReference;

    @ApiModelProperty(value = "AccountReport")
    private Xs2aAccountReport accountReport;

    @ApiModelProperty(value = "BalanceList")
    private List<Xs2aBalance> balances;

    @ApiModelProperty(value = "Links")
    @JsonProperty("_links")
    private Links links;
}
