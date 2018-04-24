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
import com.fasterxml.jackson.annotation.JsonRootName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "Balances", value = "Balances")
@JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@JsonRootName(value = "balances")
public class Balances {

	@ApiModelProperty(value = "booked: Balance of the account at the end of the pre-agreed account reporting period.")
	private SingleBalance closingBooked;

	@ApiModelProperty(value = "expected: Balance composed of booked entries and pending items known at the time of calculation, which projects the end of day balance if everything is booked on the account and no other entry is posted.")
	private SingleBalance expected;

	@ApiModelProperty(value = "authorised: The expected balance together with the value of a pre-approved credit line the ASPSP makes permanently available to the user.")
	private SingleBalance authorised;

	@ApiModelProperty(value = "opening booked: Book balance of the account at the beginning of the account reporting period. It always equals the closing book balance from the previous report.")
	private SingleBalance openingBooked;

	@ApiModelProperty(value = "interim available: Available balance calculated in the course of the account ’servicer's business day, at the time specified, and subject to further changes during the business day. The interim balance is calculated on the basis of booked credit and debit items during the calculation time/period specified.")
	private SingleBalance interimAvailable;
}
