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

package de.adorsys.psd2.xs2a.service.mapper.consent;

import de.adorsys.psd2.consent.api.pis.authorisation.CreatePisConsentAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisConsentPsuDataRequest;
import de.adorsys.psd2.consent.api.pis.proto.CreatePisConsentResponse;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisConsentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;

@Component
public class Xs2aPisConsentMapper {

    public Optional<Xsa2CreatePisConsentAuthorisationResponse> mapToXsa2CreatePisConsentAuthorizationResponse(CreatePisConsentAuthorisationResponse response, PaymentType paymentType) {
        return Optional.of(new Xsa2CreatePisConsentAuthorisationResponse(response.getAuthorizationId(), ScaStatus.RECEIVED, paymentType));
    }

    public Optional<Xs2aCreatePisConsentCancellationAuthorisationResponse> mapToXs2aCreatePisConsentCancellationAuthorisationResponse(CreatePisConsentAuthorisationResponse response, PaymentType paymentType) {
        return Optional.of(new Xs2aCreatePisConsentCancellationAuthorisationResponse(response.getAuthorizationId(), ScaStatus.RECEIVED, paymentType));
    }

    public Optional<Xs2aPaymentCancellationAuthorisationSubResource> mapToXs2aPaymentCancellationAuthorisationSubResource(String authorisationId) {
        return Optional.of(new Xs2aPaymentCancellationAuthorisationSubResource(Collections.singletonList(authorisationId)));
    }

    public Xs2aPisConsent mapToXs2aPisConsent(CreatePisConsentResponse response, PsuIdData psuData) {
        return new Xs2aPisConsent(response.getConsentId(), psuData);
    }

    public UpdatePisConsentPsuDataRequest mapToCmsUpdateConsentPsuDataReq(Xs2aUpdatePisConsentPsuDataRequest updatePsuDataRequest,
                                                                          Xs2aUpdatePisConsentPsuDataResponse updatePsuDataResponse) {
        return Optional.ofNullable(updatePsuDataResponse)
                   .map(data -> {
                       UpdatePisConsentPsuDataRequest request = new UpdatePisConsentPsuDataRequest();
                       request.setPsuData(request.getPsuData());
                       request.setPaymentId(updatePsuDataRequest.getPaymentId());
                       request.setAuthorizationId(updatePsuDataRequest.getAuthorizationId());
                       request.setAuthenticationMethodId(getAuthenticationMethodId(data));
                       request.setScaStatus(data.getScaStatus());
                       return request;
                   })
                   .orElse(null);
    }

    private String getAuthenticationMethodId(Xs2aUpdatePisConsentPsuDataResponse data) {
        return Optional.ofNullable(data.getChosenScaMethod())
                   .map(Xs2aAuthenticationObject::getAuthenticationMethodId)
                   .orElse(null);
    }

    public SpiScaConfirmation buildSpiScaConfirmation(Xs2aUpdatePisConsentPsuDataRequest request, String consentId, String paymentId) {
        SpiScaConfirmation paymentConfirmation = new SpiScaConfirmation();
        paymentConfirmation.setPaymentId(paymentId);
        paymentConfirmation.setTanNumber(request.getScaAuthenticationData());
        paymentConfirmation.setConsentId(consentId);
        paymentConfirmation.setPsuId(Optional.ofNullable(request.getPsuData()).map(PsuIdData::getPsuId).orElse(null));
        return paymentConfirmation;
    }

}
