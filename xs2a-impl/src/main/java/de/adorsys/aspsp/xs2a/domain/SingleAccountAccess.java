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
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Single account access", value = "SingleAccountAccess")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SingleAccountAccess {

	@ApiModelProperty(value = "account", required = true)
	private AccountReference account; //NOPMD TODO review and check PMD assertion
	@ApiModelProperty(value = "access type: The values balance and transactions are permitted. ", required = true, example = "balance, transactions")
	private String[] access_type; //NOPMD TODO review and check PMD assertion
}
