/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers;

import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationResponse;
import de.adorsys.psd2.consent.api.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisCancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aPisCommonPayment;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class Xs2aPisCommonPaymentMapper {

    public Optional<Xs2aCreatePisAuthorisationResponse> mapToXsa2CreatePisAuthorisationResponse(CreateAuthorisationResponse response, PaymentType paymentType) {
        if (response != null) {
            return Optional.of(new Xs2aCreatePisAuthorisationResponse(response.getAuthorizationId(), response.getScaStatus(), paymentType, response.getInternalRequestId(), response.getPsuIdData()));
        }

        return Optional.empty();
    }

    public Optional<Xs2aCreatePisCancellationAuthorisationResponse> mapToXs2aCreatePisCancellationAuthorisationResponse(CreateAuthorisationResponse response, PaymentType paymentType) {
        if (response != null) {
            return Optional.of(new Xs2aCreatePisCancellationAuthorisationResponse(response.getAuthorizationId(), response.getScaStatus(), paymentType, response.getInternalRequestId()));
        }

        return Optional.empty();
    }

    public Xs2aPisCommonPayment mapToXs2aPisCommonPayment(CreatePisCommonPaymentResponse response, PsuIdData psuData) {
        return new Xs2aPisCommonPayment(response.getPaymentId(), psuData);
    }

    public UpdateAuthorisationRequest mapToUpdateAuthorisationRequest(AuthorisationProcessorResponse response, AuthorisationType authorisationType) {
        return Optional.ofNullable(response)
                   .map(data -> {
                       UpdateAuthorisationRequest req = new UpdateAuthorisationRequest();
                       req.setPsuData(response.getPsuData());
                       req.setAuthenticationMethodId(Optional.ofNullable(data.getChosenScaMethod())
                                                         .map(AuthenticationObject::getAuthenticationMethodId)
                                                         .orElse(null));
                       req.setScaStatus(data.getScaStatus());
                       req.setAuthorisationType(authorisationType);
                       return req;
                   })
                   .orElse(null);
    }

    public SpiScaConfirmation buildSpiScaConfirmation(Xs2aUpdatePisCommonPaymentPsuDataRequest request, String consentId, String paymentId, PsuIdData psuData) {
        SpiScaConfirmation paymentConfirmation = new SpiScaConfirmation();
        paymentConfirmation.setPaymentId(paymentId);
        paymentConfirmation.setTanNumber(request.getScaAuthenticationData());
        paymentConfirmation.setConsentId(consentId);
        paymentConfirmation.setPsuId(Optional.ofNullable(psuData).map(PsuIdData::getPsuId).orElse(null));
        return paymentConfirmation;
    }

}
