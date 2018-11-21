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

package de.adorsys.aspsp.aspspmockserver.domain.pis.proto;

import de.adorsys.aspsp.aspspmockserver.domain.ConsentStatus;
import de.adorsys.aspsp.aspspmockserver.domain.TppInfo;
import de.adorsys.aspsp.aspspmockserver.domain.pis.PisPayment;
import de.adorsys.aspsp.aspspmockserver.domain.pis.PisPaymentProduct;
import de.adorsys.aspsp.aspspmockserver.domain.pis.PisPaymentType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "Pis payment initialisation consent response", value = "PisConsentResponse")
public class PisConsentResponse {
    @ApiModelProperty(value = "Payment data", required = true)
    private List<PisPayment> payments;

    @ApiModelProperty(value = "Payment product", required = true, example = "sepa-credit-transfers")
    private PisPaymentProduct paymentProduct;

    @ApiModelProperty(value = "Payment type: BULK, SINGLE or PERIODIC.", required = true, example = "SINGLE")
    private PisPaymentType paymentType;

    @ApiModelProperty(value = "Tpp information", required = true)
    private TppInfo tppInfo;

    @ApiModelProperty(value = "An external exposed identification of the created payment consent", required = true, example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
    private String externalId;

    @ApiModelProperty(value = "The following code values are permitted 'received', 'valid', 'rejected', 'expired', 'revoked by psu', 'terminated by tpp'. These values might be extended by ASPSP.", required = true, example = "VALID")
    private ConsentStatus consentStatus;

    @ApiModelProperty(value = "ASPSP consent data", example = "zzzzzzzz")
    private byte[] aspspConsentData;
}
