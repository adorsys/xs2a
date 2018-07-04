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

package de.adorsys.aspsp.xs2a.consent.api.ais;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;

@Data
@ApiModel(description = "Ais consent request", value = "AisConsentRequest")
public class AisConsentRequest {

    @ApiModelProperty(value = "ID of the corresponding PSU", required = true, example = "32aad578-58a6-4d5d-8b0c-45546dd88f07")
    private String psuId;

    @ApiModelProperty(value = "ID of the corresponding TPP.", required = true, example = "testTPP")
    private String tppId;

    @ApiModelProperty(value = "Requested maximum frequency for an access per day. For a once-off access, this attribute is set to 1", required = true, example = "4")
    private int frequencyPerDay;

    @ApiModelProperty(value = "Set of accesses given by psu for this account", required = true)
    private AisAccountAccessInfo access;

    @ApiModelProperty(value = "Consent`s expiration date. The content is the local ASPSP date in ISODate Format", required = true, example = "2020-10-10")
    private LocalDate validUntil;

    @ApiModelProperty(value = "'true', if the consent is for recurring access to the account data , 'false', if the consent is for one access to the account data", required = true, example = "false")
    private boolean recurringIndicator;

    @ApiModelProperty(name = "tppRedirectPreferred", value = "If it equals “true”, the TPP prefers a redirect over an embedded SCA approach.", example = "true")
    private boolean tppRedirectPreferred;

    @ApiModelProperty(value = "If 'true' indicates that a payment initiation service will be addressed in the same 'session'", required = true, example = "false")
    private boolean combinedServiceIndicator;
}
