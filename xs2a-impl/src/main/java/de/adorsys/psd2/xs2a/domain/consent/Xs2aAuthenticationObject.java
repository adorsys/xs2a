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

package de.adorsys.psd2.xs2a.domain.consent;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;

@Data
@ApiModel(description = "Authentication object", value = "AuthenticationObject")
public class Xs2aAuthenticationObject {

    @ApiModelProperty(value = "Type of the authentication method", required = true)
    private String authenticationType;

    @ApiModelProperty(value = "Version can be used by differentiating authentication tools used within performing OTP generation in the same authentication type")
    private String authenticationVersion;

    @ApiModelProperty(value = "Provided by the ASPSP for the later identification of the authentication method selection.", required = true)
    @Size(max = 35)
    private String authenticationMethodId;

    @ApiModelProperty(value = "Name of the authentication method", required = false, example = "redirect")
    private String name;

    @ApiModelProperty(value = "Detailed information about the sca method for the PSU", required = false)
    private String explanation;

}

