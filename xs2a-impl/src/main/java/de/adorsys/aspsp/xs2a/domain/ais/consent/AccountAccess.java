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

package de.adorsys.aspsp.xs2a.domain.ais.consent;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.adorsys.aspsp.xs2a.domain.AccountReference;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "Account access", value = "AccountAccess")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountAccess {

    @ApiModelProperty(value = "detailed account information", required = false)
    private AccountReference[] accounts;

    @ApiModelProperty(value = "balances of the addressed accounts", required = false)
    private AccountReference[] balances;

    @ApiModelProperty(value = "transactions of the addressed accounts", required = false)
    private AccountReference[] transactions;

    @ApiModelProperty(value = "only the value 'all-accounts' is admitted", example = "all-accounts", required = false)
    private AccountAccessType availableAccounts;

    @ApiModelProperty(value = "only the value 'all-accounts' is admitted", example = "all-accounts", required = false)
    private AccountAccessType allPsd2;
}
