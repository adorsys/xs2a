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

package de.adorsys.psd2.consent.api.pis;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "Pis consent aspsp data response", value = "PisConsentAspspDataResponse")
public class PisConsentAspspDataResponse {

    @ApiModelProperty(value = "ASPSP consent data", required = true, example = "zdxcvvzzzxcvzzzz")
    private byte[] aspspConsentData;

    @ApiModelProperty(value = "Consent ID", required = true, example = "d2796b05-418e-49bc-84ce-c6728a1b2018")
    private String consentId;
}
