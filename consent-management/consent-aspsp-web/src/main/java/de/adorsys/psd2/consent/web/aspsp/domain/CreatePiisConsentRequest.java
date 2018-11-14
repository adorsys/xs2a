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

package de.adorsys.psd2.consent.web.aspsp.domain;

import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@ApiModel(description = "Piis consent request", value = "PiisConsentRequest")
public class CreatePiisConsentRequest {
    @ApiModelProperty(value = "Tpp for which the consent will be created. If the property is omitted, the consent will be created for all TPPs")
    private TppInfo tppInfo;

    @ApiModelProperty(value = "Accounts for which the consent is created")
    private List<AccountReference> accounts;

    @ApiModelProperty(value = "Consent`s expiration date. The content is the local ASPSP date in ISODate Format", example = "2020-10-10")
    private LocalDate validUntil;

    @ApiModelProperty(value = "Maximum frequency for an access per day. For a once-off access, this attribute is set to 1", required = true, example = "4")
    private int allowedFrequencyPerDay;
}

