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

package de.adorsys.psd2.xs2a.service.mapper.consent;

import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.CreatePisAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthenticationObject;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisCancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aPisCommonPayment;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class Xs2aPisCommonPaymentMapper {

    public Optional<Xs2aCreatePisAuthorisationResponse> mapToXsa2CreatePisAuthorisationResponse(CreatePisAuthorisationResponse response, PaymentType paymentType) {
        return Optional.of(new Xs2aCreatePisAuthorisationResponse(response.getAuthorizationId(), ScaStatus.STARTED, paymentType));
    }

    public Optional<Xs2aCreatePisCancellationAuthorisationResponse> mapToXs2aCreatePisCancellationAuthorisationResponse(CreatePisAuthorisationResponse response, PaymentType paymentType) {
        if (response != null) {
            return Optional.of(new Xs2aCreatePisCancellationAuthorisationResponse(response.getAuthorizationId(), ScaStatus.STARTED, paymentType));
        }

        return Optional.empty();
    }

    public Xs2aPisCommonPayment mapToXs2aPisCommonPayment(CreatePisCommonPaymentResponse response, PsuIdData psuData) {
        return new Xs2aPisCommonPayment(response.getPaymentId(), psuData);
    }

    public UpdatePisCommonPaymentPsuDataRequest mapToCmsUpdateCommonPaymentPsuDataReq(Xs2aUpdatePisCommonPaymentPsuDataResponse updatePsuDataResponse) {
        return Optional.ofNullable(updatePsuDataResponse)
                   .map(data -> {
                       UpdatePisCommonPaymentPsuDataRequest request = new UpdatePisCommonPaymentPsuDataRequest();
                       request.setPsuData(data.getPsuData());
                       request.setPaymentId(data.getPaymentId());
                       request.setAuthorizationId(data.getAuthorisationId());
                       request.setAuthenticationMethodId(getAuthenticationMethodId(data));
                       request.setScaStatus(data.getScaStatus());
                       return request;
                   })
                   .orElse(null);
    }

    private String getAuthenticationMethodId(Xs2aUpdatePisCommonPaymentPsuDataResponse data) {
        return Optional.ofNullable(data.getChosenScaMethod())
                   .map(Xs2aAuthenticationObject::getAuthenticationMethodId)
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
