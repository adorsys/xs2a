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
