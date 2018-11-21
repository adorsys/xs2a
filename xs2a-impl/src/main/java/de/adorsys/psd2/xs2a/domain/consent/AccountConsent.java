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

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.EnumSet;

import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.RECEIVED;
import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.VALID;

@Data
@ApiModel(description = "Response for the get account information consent request by consent Id")
public class AccountConsent {
    @ApiModelProperty(value = "ID of the corresponding consent object as returned by an Account Information Consent Request", required = true)
    @JsonIgnore
    private final String id;

    @ApiModelProperty(value = "Access", required = true)
    private final Xs2aAccountAccess access;

    @ApiModelProperty(value = "'true', if the consent is for recurring access to the account data , 'false', if the consent is for one access to the account data", required = true)
    private final boolean recurringIndicator;

    @ApiModelProperty(value = "valid until date for the requested consent. The content is the local ASPSP date in ISODate Format", required = true, example = "2017-10-30")
    private final LocalDate validUntil;

    @ApiModelProperty(value = "requested maximum frequency for an access per day. For a once-off access, this attribute is set to 1", required = true, example = "4")
    private final int frequencyPerDay;

    @ApiModelProperty(value = "This date is containing the date of the last action on the consent object either through the XS2A interface or the PSU/ASPSP interface having an impact on the status.", required = true, example = "2017-10-30")
    private final LocalDate lastActionDate;

    @ApiModelProperty(value = "The following code values are permitted 'received', 'valid', 'rejected', 'expired', 'revoked by psu', 'terminated by tpp'. These values might be extended by ASPSP by more values.", required = true, example = "VALID")
    private final ConsentStatus consentStatus;

    @ApiModelProperty(name = "withBalance", value = "If contained, this function reads the list of accessible payment accounts including the balance.")
    @JsonIgnore
    private final boolean withBalance;

    @ApiModelProperty(name = "tppRedirectPreferred", value = "If it equals “true”, the TPP prefers a redirect over an embedded SCA approach.")
    @JsonIgnore
    private final boolean tppRedirectPreferred;

    @ApiModelProperty(value = "Corresponding PSU", name = "psuData")
    @JsonIgnore
    private final PsuIdData psuData;

    @ApiModelProperty(value = "TPP id", required = true, example = "af006545-d713-46d7-b6cf-09c9628f9a5d")
    @JsonIgnore
    private final String tppId;

    @JsonIgnore
    public boolean isValidStatus() {
        return EnumSet.of(VALID, RECEIVED).contains(consentStatus);
    }

    @JsonIgnore
    public boolean isValidFrequency() {
        return frequencyPerDay > 0;
    }
}
