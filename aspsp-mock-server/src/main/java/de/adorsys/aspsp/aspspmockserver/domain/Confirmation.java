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

package de.adorsys.aspsp.aspspmockserver.domain;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class Confirmation {
    @ApiModelProperty(value = "IBAN: This data element can be used in the body of the CreateConsentReq Request Message for retrieving account access consent from this payment account", example = "DE89370400440532013000")
    private String iban;

    @ApiModelProperty(value = "Identification resource of the given consent", example = "6d4b403b-f5f5-41c0-847f-b6abf1edb102")
    private String consentId;

    @ApiModelProperty(value = "Identification resource of the created payment", example = "6d4b403b-f5f5-41c0-847f-b6abf1edb102")
    private String paymentId;

    @ApiModelProperty(value = "A transaction authentication number (TAN) is used by online banking services as a form of single use one-time passwords", example = "sR111a")
    private String tanNumber;
}
