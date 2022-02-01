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
import de.adorsys.psd2.xs2a.domain.consent.pis.PaymentAuthorisationParameters;
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

    public SpiScaConfirmation buildSpiScaConfirmation(PaymentAuthorisationParameters request, String consentId, String paymentId, PsuIdData psuData) {
        SpiScaConfirmation paymentConfirmation = new SpiScaConfirmation();
        paymentConfirmation.setPaymentId(paymentId);
        paymentConfirmation.setTanNumber(request.getScaAuthenticationData());
        paymentConfirmation.setConsentId(consentId);
        paymentConfirmation.setPsuId(Optional.ofNullable(psuData).map(PsuIdData::getPsuId).orElse(null));
        return paymentConfirmation;
    }

}
