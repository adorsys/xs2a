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

package de.adorsys.psd2.xs2a.domain.consent.pis;

import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.authorisation.CommonAuthorisationParameters;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationRequest;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PaymentAuthorisationParameters implements CommonAuthorisationParameters {
    private PsuIdData psuData;
    private String paymentId;
    private String authorisationId;

    private ScaStatus scaStatus;
    private boolean updatePsuIdentification;
    private String authenticationMethodId;
    private String scaAuthenticationData;
    private String confirmationCode;
    private String password;
    private String paymentProduct;
    private PaymentType paymentService;

    public PaymentAuthorisationParameters(Xs2aCreatePisAuthorisationRequest createRequest, String authorisationId) {
        this.psuData = createRequest.getPsuData();
        this.paymentId = createRequest.getPaymentId();
        this.authorisationId = authorisationId;
        this.paymentService = createRequest.getPaymentService();
        this.paymentProduct = createRequest.getPaymentProduct();
        this.password = createRequest.getPassword();
    }

    @Override
    public String getBusinessObjectId() {
        return paymentId;
    }
}
