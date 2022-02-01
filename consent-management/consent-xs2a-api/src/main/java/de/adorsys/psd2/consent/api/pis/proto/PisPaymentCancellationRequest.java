/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
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
