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
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisCancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aPaymentCancellationAuthorisationSubResource;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.consent.PisPsuDataService;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentCancellationAuthorisationServiceImpl implements PaymentCancellationAuthorisationService {
    private final PisScaAuthorisationService pisScaAuthorisationService;
    private final PisPsuDataService pisPsuDataService;
    private final Xs2aEventService xs2aEventService;

    @Override
    public ResponseObject<Xs2aCreatePisCancellationAuthorisationResponse> createPisCancellationAuthorization(String paymentId, PsuIdData psuData, PaymentType paymentType) {
        xs2aEventService.recordPisTppRequest(paymentId, EventType.START_PAYMENT_CANCELLATION_AUTHORISATION_REQUEST_RECEIVED);

        if (!isPsuDataCorrect(paymentId, psuData)) {
            return ResponseObject.<Xs2aCreatePisCancellationAuthorisationResponse>builder()
                       .fail(new MessageError(MessageErrorCode.PSU_CREDENTIALS_INVALID))
                       .build();
        }

        return pisScaAuthorisationService.createCommonPaymentCancellationAuthorisation(paymentId, paymentType, psuData)
                   .map(resp -> ResponseObject.<Xs2aCreatePisCancellationAuthorisationResponse>builder()
                                    .body(resp)
                                    .build())
                   .orElseGet(ResponseObject.<Xs2aCreatePisCancellationAuthorisationResponse>builder()
                                  .fail(new MessageError(MessageErrorCode.FORMAT_ERROR))
                                  ::build);
    }

    @Override
    public ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> updatePisCancellationPsuData(Xs2aUpdatePisCommonPaymentPsuDataRequest request) {
        xs2aEventService.recordPisTppRequest(request.getPaymentId(), EventType.UPDATE_PAYMENT_CANCELLATION_PSU_DATA_REQUEST_RECEIVED, request);

        Xs2aUpdatePisCommonPaymentPsuDataResponse response = pisScaAuthorisationService.updateCommonPaymentCancellationPsuData(request);

        if (response.hasError()) {
            return ResponseObject.<Xs2aUpdatePisCommonPaymentPsuDataResponse>builder()
                       .fail(new MessageError(response.getErrorHolder().getErrorCode(), response.getErrorHolder().getMessage()))
                       .build();
        }
        return ResponseObject.<Xs2aUpdatePisCommonPaymentPsuDataResponse>builder()
                   .body(response)
                   .build();
    }

    private boolean isPsuDataCorrect(String paymentId, PsuIdData psuData) {
        List<PsuIdData> psuIdDataList = pisPsuDataService.getPsuDataByPaymentId(paymentId);

        return psuIdDataList.stream()
                   .anyMatch(psu -> psu.contentEquals(psuData));
    }

    @Override
    public ResponseObject<Xs2aPaymentCancellationAuthorisationSubResource> getPaymentInitiationCancellationAuthorisationInformation(String paymentId) {
        xs2aEventService.recordPisTppRequest(paymentId, EventType.GET_PAYMENT_CANCELLATION_AUTHORISATION_REQUEST_RECEIVED);

        return pisScaAuthorisationService.getCancellationAuthorisationSubResources(paymentId)
                   .map(resp -> ResponseObject.<Xs2aPaymentCancellationAuthorisationSubResource>builder().body(resp).build())
                   .orElseGet(ResponseObject.<Xs2aPaymentCancellationAuthorisationSubResource>builder()
                                  .fail(new MessageError(MessageErrorCode.RESOURCE_UNKNOWN_404))
                                  ::build);
    }

    /**
     * Gets SCA status of payment cancellation authorisation
     *
     * @param paymentId      ASPSP identifier of the payment, associated with the authorisation
     * @param cancellationId cancellation authorisation identifier
     * @return Response containing SCA status of authorisation or corresponding error
     */
    @Override
    public ResponseObject<ScaStatus> getPaymentCancellationAuthorisationScaStatus(String paymentId, String cancellationId) {
        xs2aEventService.recordPisTppRequest(paymentId, EventType.GET_PAYMENT_CANCELLATION_SCA_STATUS_REQUEST_RECEIVED);

        Optional<ScaStatus> scaStatus = pisScaAuthorisationService.getCancellationAuthorisationScaStatus(paymentId, cancellationId);

        if (!scaStatus.isPresent()) {
            return ResponseObject.<ScaStatus>builder()
                       .fail(new MessageError(MessageErrorCode.RESOURCE_UNKNOWN_403))
                       .build();
        }

        return ResponseObject.<ScaStatus>builder()
                   .body(scaStatus.get())
                   .build();
    }
}
