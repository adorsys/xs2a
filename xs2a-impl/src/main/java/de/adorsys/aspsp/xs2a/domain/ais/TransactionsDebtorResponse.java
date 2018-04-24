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

package de.adorsys.aspsp.xs2a.domain.ais;

import de.adorsys.aspsp.xs2a.domain.AccountDetails;
import de.adorsys.aspsp.xs2a.domain.Amount;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel(description = "Transaction Response information", value = "TransactionsCreditorResponse")
public class TransactionsDebtorResponse {

	@ApiModelProperty(value = "Transaction ID: Can be used as access-id in the API, where more details on an transaction is offered", example = "12345")
	// we get it in the Header in the Prozess-ID
	private String transaction_id;

	@ApiModelProperty(value = "Name of the Debtor if a credited transaction", example = "Jan")
	private String debtor;

	@ApiModelProperty(value = "Debtor account", example = "56666")
	private AccountDetails debtor_accountDetails;

	@ApiModelProperty(value = "Amount", required = true)
	private Amount amount;

	@ApiModelProperty(value = "Booking Date", example = "2017-01-01")
	private Date booking_date;

	@ApiModelProperty(value = "Value Date", example = "2017-01-01")
	private Date value_date;

	@ApiModelProperty(value = "Remittance information", example = "Otto")
	private String remittance_information;
}
