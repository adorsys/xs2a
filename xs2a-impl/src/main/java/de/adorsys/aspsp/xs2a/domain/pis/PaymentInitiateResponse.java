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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import de.adorsys.aspsp.xs2a.domain.*;
import de.adorsys.aspsp.xs2a.domain.consent.Xs2aAuthenticationObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public abstract class PaymentInitiateResponse {

    @JsonUnwrapped
    @ApiModelProperty(value = "The transaction status is filled with value of the ISO20022 data table", required = true, example = "ACCP")
    private Xs2aTransactionStatus transactionStatus;

    @ApiModelProperty(value = "Can be used by the ASPSP to transport transaction fees relevant for the underlying payments.")
    private Xs2aAmount transactionFees;

    @ApiModelProperty(value = "If equals true, the transaction will involve specific transaction cost as shown by the ASPSP in their public price list or as agreed between ASPSP and PSU.", example = "false")
    private boolean transactionFeeIndicator;

    @ApiModelProperty(value = "Resource identification of the generated payment initiation resource.", required = true, example = "qwer3456tzui7890")
    private String paymentId;

    @ApiModelProperty(value = "This data element might be contained, if SCA is required and if the PSU has a choice between different authentication methods")
    private Xs2aAuthenticationObject[] scaMethods;

    @ApiModelProperty(value = "It is contained in addition to the data element 'chosenScaMethod' if challenge data is needed for SCA.")
    private Xs2aChallengeData challengeData;

    @ApiModelProperty(value = "Text to be displayed to the PSU")
    private String psuMessage;

    @ApiModelProperty(value = "Messages to the TPP on operational issues.")
    private MessageErrorCode[] tppMessages;

    @ApiModelProperty(value = "Links: a list of hyperlinks to be recognised by the TPP.")
    @JsonProperty("_links")
    private Links links;

    @JsonIgnore
    private String pisConsentId;

    //For Embedded approach Implicit case
    @JsonIgnore
    private String authorizationId;
}
