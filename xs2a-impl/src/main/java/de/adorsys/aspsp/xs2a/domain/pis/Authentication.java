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

import de.adorsys.aspsp.xs2a.domain.consent.AuthenticationType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;

@Data
@ApiModel(description = "Authentication", value = "Authentication")
public class Authentication {

	@ApiModelProperty(value = "Authentication Type", required = true)
	private AuthenticationType authenticationType;

    @ApiModelProperty(value = "Depending on the authenticationType. This version can be used by differentiating authentication tools used within performing OTP generation in the same authentication type. This version can be referred to in the ASPSP’s documentation", required = true)
    private String authenticationVersion;

    @ApiModelProperty(value = "An identification provided by the ASPSP for the later identification of the authentication method selection.", required = true)
	@Size(max = 35)
    private String authenticationMethodId;

	@ApiModelProperty(value = "Name of the authentication method", example = "redirect", required = false)
	private String name;

	@ApiModelProperty(value = "Details information about the sca method", required = false)
	private String explanation;
}

