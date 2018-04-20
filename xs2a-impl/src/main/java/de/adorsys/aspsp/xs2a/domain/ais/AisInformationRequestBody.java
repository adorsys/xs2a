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
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "Request Body for created by some methods in the Ais Service")
public class AisInformationRequestBody {

	@ApiModelProperty(value = "Requested access service per account.", required = true)
	private SingleAccountAccess[] accounts;
	@ApiModelProperty(value = "This parameter is requesting a valid until date for the requested consent. The content is the local ASPSP date in ISODate Format", required = true, example = "2017-10-30")
	private String valid_until;
	@ApiModelProperty(value = "This field indicates the requested maximum frequency for an access per day. For a once-off access, this attribute is set to 1", required = true, example = "4")
	private Integer frequency_per_day;
	@ApiModelProperty(value = "\"true\", if the consent is for recurring access to the account data \"false\", if the consent is for one access to the account data", required = true)
	private boolean recurring_indicator;
	@ApiModelProperty(value = "If \"true\" indicates that a payment initiation service will be addressed in the same \"session\"", required = true)
	private boolean combined_service_indicator;
}


