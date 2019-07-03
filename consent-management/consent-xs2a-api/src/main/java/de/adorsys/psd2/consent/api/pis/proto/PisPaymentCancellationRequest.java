/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.api.pis.proto;

import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@ApiModel(description = "Pis payment cancellation request", value = "PisPaymentCancellationRequest")
public class PisPaymentCancellationRequest {
    @ApiModelProperty(value = "Payment type: BULK, SINGLE or PERIODIC.", required = true, example = "SINGLE")
    private PaymentType paymentType;
    @ApiModelProperty(value = "Payment product", required = true, example = "sepa-credit-transfers")
    private String paymentProduct;
    @ApiModelProperty(value = "ASPSP identifier of the payment", required = true, example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
    private String encryptedPaymentId;
    @ApiModelProperty(value = "TPP's choice of authorisation method", required = true, example = "TRUE | FALSE")
    private Boolean tppExplicitAuthorisationPreferred;
    @ApiModelProperty(value = "Tpp redirect URIs", required = true)
    private TppRedirectUri tppRedirectUri;
}
