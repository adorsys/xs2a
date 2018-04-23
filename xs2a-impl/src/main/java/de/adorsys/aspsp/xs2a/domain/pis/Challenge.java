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

package de.adorsys.aspsp.xs2a.domain.pis;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "Challenge authentication", value = "Challenge")
public class Challenge {

    @ApiModelProperty(value = "OTP maximal length", required = false, example = "me.png")
    private String image;

    @ApiModelProperty(value = "String challenge data", required = false, example = "me.png")
    private String data;

    @ApiModelProperty(value = "A link where the ASPSP will provides the challenge image for the TPP.", required = false,  example = "me.png")
    private String imageLink;

    @ApiModelProperty(value = "The maximal length for the OTP to be typed in by the PSU.", required = false, example = "1234")
	private int otpMaxLength;

	@ApiModelProperty(value = "The format type of the OTP to be typed in. The admitted values are “characters” or “integer”.", required = false, example = "Push")
	private String otpFormat;

	@ApiModelProperty(value = "Additional explanation for the PSU to explain e.g. fallback mechanism for the chosen SCA method. The TPP is obliged to show this to the PSU.", required = false, example = "SCA Redirect")
	private String additionalInformation;
}
