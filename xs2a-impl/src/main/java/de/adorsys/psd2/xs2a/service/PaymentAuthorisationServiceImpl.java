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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.xs2a.core.event.EventType;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.Xsa2CreatePisConsentAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisConsentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentAuthorisationServiceImpl implements PaymentAuthorisationService {
    private final Xs2aEventService xs2aEventService;
    private final PisScaAuthorisationService pisAuthorizationService;

    public ResponseObject<Xsa2CreatePisConsentAuthorisationResponse> createPisConsentAuthorization(String paymentId, PaymentType paymentType, PsuIdData psuData) {
        xs2aEventService.recordPisTppRequest(paymentId, EventType.START_PAYMENT_AUTHORISATION_REQUEST_RECEIVED);

        return pisAuthorizationService.createConsentAuthorisation(paymentId, paymentType, psuData)
                   .map(resp -> ResponseObject.<Xsa2CreatePisConsentAuthorisationResponse>builder()
                                    .body(resp)
                                    .build())
                   .orElseGet(ResponseObject.<Xsa2CreatePisConsentAuthorisationResponse>builder()
                                  .fail(new MessageError(MessageErrorCode.PAYMENT_FAILED))
                                  ::build);
    }

    public ResponseObject<Xs2aUpdatePisConsentPsuDataResponse> updatePisConsentPsuData(Xs2aUpdatePisConsentPsuDataRequest request) {
        xs2aEventService.recordPisTppRequest(request.getPaymentId(), EventType.UPDATE_PAYMENT_AUTHORISATION_PSU_DATA_REQUEST_RECEIVED, request);
        Xs2aUpdatePisConsentPsuDataResponse response = pisAuthorizationService.updateConsentPsuData(request);

        if (response.hasError()) {
            return ResponseObject.<Xs2aUpdatePisConsentPsuDataResponse>builder()
                       .fail(new MessageError(response.getErrorHolder().getErrorCode(), response.getErrorHolder().getMessage()))
                       .build();
        }
        return ResponseObject.<Xs2aUpdatePisConsentPsuDataResponse>builder()
                   .body(response)
                   .build();
    }
}
