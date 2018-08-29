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

package de.adorsys.aspsp.xs2a.domain.consent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Value;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

@Value
@ApiModel(description = "Account access", value = "AccountAccess")
public class AccountAccess {

    @ApiModelProperty(value = "detailed account information", required = false)
    private List<AccountReference> accounts;

    @ApiModelProperty(value = "balances of the addressed accounts", required = false)
    private List<AccountReference> balances;

    @ApiModelProperty(value = "transactions of the addressed accounts", required = false)
    private List<AccountReference> transactions;

    @ApiModelProperty(value = "only the value 'allAccounts' or 'allAccountsWithBalances' is admitted", example = "allAccounts", required = false)
    private AccountAccessType availableAccounts;

    @ApiModelProperty(value = "only the value 'allAccounts' is admitted", example = "allAccounts", required = false)
    private AccountAccessType allPsd2;

    @JsonIgnore
    public boolean isNotEmpty(){
       return  !(CollectionUtils.isEmpty(accounts)
              && CollectionUtils.isEmpty(balances)
              && CollectionUtils.isEmpty(transactions)
              && allPsd2 == null
              && availableAccounts == null);
    }
}
