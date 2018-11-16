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
import com.fasterxml.jackson.annotation.JsonProperty;
import de.adorsys.psd2.xs2a.core.sca.ChallengeData;
import de.adorsys.psd2.xs2a.domain.Links;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "Response for the create account information consent request in the Account service")
public class CreateConsentResponse {

    @ApiModelProperty(value = "Authentication status of the consent", required = true)
    private final String consentStatus;

    @ApiModelProperty(value = "Identification of the consent resource as it is used in the API structure", required = false)
    private final String consentId;

    @ApiModelProperty(value = "This data element might be contained, if SCA is required and if the PSU has a choice between different authentication methods. Depending on the risk management of the ASPSP this choice might be offered before or after the PSU has been identified with the first relevant factor, or if an access token is transported. If this data element is contained, then there is also an hyperlink of type 'selectAuthenticationMethods' contained in the response body.", required = false)
    private final Xs2aAuthenticationObject[] scaMethods;

    @ApiModelProperty(value = "This data element is only contained in the response if the APSPS has chosen the Embedded SCA Approach, if the PSU is already identified with the first relevant factor or alternatively an access token, if SCA is required and if the authentication method is implicitly selected")
    private final Xs2aAuthenticationObject chosenScaMethod;

    @ApiModelProperty(value = "It is contained in addition to the data element chosenScaMethod if challenge data is needed for SCA")
    private final ChallengeData challengeData;

    @ApiModelProperty(value = "A list of hyperlinks to be recognized by Tpp", required = true)
    @JsonProperty("_links")
    private Links links = new Links();

    @ApiModelProperty(value = "Text to be displayed to the PSU, e.g. in a Decoupled SCA Approach", required = false)
    private final String psuMessage;

    //For Embedded approach Implicit case
    @JsonIgnore
    private String authorizationId;
}
