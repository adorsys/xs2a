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

import de.adorsys.aspsp.xs2a.domain.SingleAccountAccess;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel(description = "Response for created by some methods in the consent Service")
public class AisAccountsList {
	private SingleAccountAccess[] accounts;
	private String valid_until;
	private Integer frequency_per_day;
	private boolean recurring_indicator;
	private TransactionStatus transaction_status;
	private String consent_status;
}


