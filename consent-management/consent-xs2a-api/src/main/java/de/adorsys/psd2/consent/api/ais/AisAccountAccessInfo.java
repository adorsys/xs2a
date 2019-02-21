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

package de.adorsys.psd2.consent.api.ais;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.adorsys.psd2.consent.api.AccountInfo;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "Ais account access information", value = "AisAccountAccessInfo")
public class AisAccountAccessInfo {

    @ApiModelProperty(value = "Access to accounts")
    private List<AccountInfo> accounts;

    @ApiModelProperty(value = "Access to balances")
    private List<AccountInfo> balances;

    @ApiModelProperty(value = "Access to transactions")
    private List<AccountInfo> transactions;

    @ApiModelProperty(value = "Consent on all available accounts of psu", example = "ALL_ACCOUNTS")
    private AccountAccessType availableAccounts;

    @ApiModelProperty(value = "Consent on all accounts, balances and transactions of psu", example = "ALL_ACCOUNTS")
    private AccountAccessType allPsd2;
}
