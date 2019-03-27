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

import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.event.EventType;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisCancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aPaymentCancellationAuthorisationSubResource;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationServiceResolver;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisCommonPaymentService;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.pis.CommonPaymentObject;
import de.adorsys.psd2.xs2a.service.validator.pis.authorisation.cancellation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.*;

@Service
@RequiredArgsConstructor
public class PaymentCancellationAuthorisationServiceImpl implements PaymentCancellationAuthorisationService {
    private static final String PAYMENT_NOT_FOUND_MESSAGE = "Payment not found";

    private final PisScaAuthorisationServiceResolver pisScaAuthorisationServiceResolver;
    private final Xs2aEventService xs2aEventService;
    private final Xs2aPisCommonPaymentService xs2aPisCommonPaymentService;
    private final CreatePisCancellationAuthorisationValidator createPisCancellationAuthorisationValidator;
    private final UpdatePisCancellationPsuDataValidator updatePisCancellationPsuDataValidator;
    private final GetPaymentCancellationAuthorisationsValidator getPaymentAuthorisationsValidator;
    private final GetPaymentCancellationAuthorisationScaStatusValidator getPaymentAuthorisationScaStatusValidator;

    /**
     * Creates authorisation for payment cancellation request if given psu data is valid
     *
     * @param paymentId      String representation of payment identification
     * @param psuData        Contains authorisation data about PSU
     * @param paymentType    Payment type supported by aspsp
     * @param paymentProduct payment product used for payment creation (e.g. sepa-credit-transfers, instant-sepa-credit-transfers...)
     * @return Xs2aCreatePisCancellationAuthorisationResponse that contains authorisationId, scaStatus, paymentType and related links
     */
    @Override
    public ResponseObject<Xs2aCreatePisCancellationAuthorisationResponse> createPisCancellationAuthorization(String paymentId, PsuIdData psuData, PaymentType paymentType, String paymentProduct) {
        xs2aEventService.recordPisTppRequest(paymentId, EventType.START_PAYMENT_CANCELLATION_AUTHORISATION_REQUEST_RECEIVED);

        Optional<PisCommonPaymentResponse> pisCommonPaymentResponse = xs2aPisCommonPaymentService.getPisCommonPaymentById(paymentId);
        if (!pisCommonPaymentResponse.isPresent()) {
            return ResponseObject.<Xs2aCreatePisCancellationAuthorisationResponse>builder()
                       .fail(PIS_404, of(RESOURCE_UNKNOWN_404, PAYMENT_NOT_FOUND_MESSAGE))
                       .build();
        }

        ValidationResult validationResult = createPisCancellationAuthorisationValidator.validate(new CreatePisCancellationAuthorisationPO(pisCommonPaymentResponse.get(), psuData));
        if (validationResult.isNotValid()) {
            return ResponseObject.<Xs2aCreatePisCancellationAuthorisationResponse>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        PisScaAuthorisationService pisScaAuthorisationService = pisScaAuthorisationServiceResolver.getService();
        return pisScaAuthorisationService.createCommonPaymentCancellationAuthorisation(paymentId, paymentType, psuData)
                   .map(resp -> ResponseObject.<Xs2aCreatePisCancellationAuthorisationResponse>builder()
                                    .body(resp)
                                    .build())
                   .orElseGet(ResponseObject.<Xs2aCreatePisCancellationAuthorisationResponse>builder()
                                  .fail(PIS_400, of(FORMAT_ERROR))
                                  ::build);
    }

    /**
     * Update psu data for payment cancellation request if psu data and password are valid
     *
     * @param request update psu data request, which contains paymentId, authorisationId, psuData, password, authenticationMethodId, scaStatus, paymentService and scaAuthenticationData
     * @return Xs2aUpdatePisCommonPaymentPsuDataResponse that contains authorisationId, scaStatus, psuId and related links in case of success, otherwise contains error
     */
    @Override
    public ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> updatePisCancellationPsuData(Xs2aUpdatePisCommonPaymentPsuDataRequest request) {
        String paymentId = request.getPaymentId();
        xs2aEventService.recordPisTppRequest(paymentId, EventType.UPDATE_PAYMENT_CANCELLATION_PSU_DATA_REQUEST_RECEIVED, request);

        Optional<PisCommonPaymentResponse> pisCommonPaymentResponse = xs2aPisCommonPaymentService.getPisCommonPaymentById(paymentId);
        if (!pisCommonPaymentResponse.isPresent()) {
            return ResponseObject.<Xs2aUpdatePisCommonPaymentPsuDataResponse>builder()
                       .fail(PIS_404, of(RESOURCE_UNKNOWN_404, PAYMENT_NOT_FOUND_MESSAGE))
                       .build();
        }

        ValidationResult validationResult = updatePisCancellationPsuDataValidator.validate(new UpdatePisCancellationPsuDataPO(pisCommonPaymentResponse.get(), request.getAuthorisationId()));
        if (validationResult.isNotValid()) {
            return ResponseObject.<Xs2aUpdatePisCommonPaymentPsuDataResponse>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        PisScaAuthorisationService pisScaAuthorisationService = pisScaAuthorisationServiceResolver.getService();
        Xs2aUpdatePisCommonPaymentPsuDataResponse response = pisScaAuthorisationService.updateCommonPaymentCancellationPsuData(request);

        if (response.hasError()) {
            return ResponseObject.<Xs2aUpdatePisCommonPaymentPsuDataResponse>builder()
                       .fail(new MessageError(response.getErrorHolder()))
                       .build();
        }
        return ResponseObject.<Xs2aUpdatePisCommonPaymentPsuDataResponse>builder()
                   .body(response)
                   .build();
    }

    /**
     * Gets list of cancellation identifiers
     *
     * @param paymentId ASPSP identifier of the payment, associated with the authorisation
     * @return Response containing list of cancellation identifiers in case of success or empty list in case of failure
     */
    @Override
    public ResponseObject<Xs2aPaymentCancellationAuthorisationSubResource> getPaymentInitiationCancellationAuthorisationInformation(String paymentId) {
        xs2aEventService.recordPisTppRequest(paymentId, EventType.GET_PAYMENT_CANCELLATION_AUTHORISATION_REQUEST_RECEIVED);

        Optional<PisCommonPaymentResponse> pisCommonPaymentResponse = xs2aPisCommonPaymentService.getPisCommonPaymentById(paymentId);
        if (!pisCommonPaymentResponse.isPresent()) {
            return ResponseObject.<Xs2aPaymentCancellationAuthorisationSubResource>builder()
                       .fail(PIS_404, of(RESOURCE_UNKNOWN_404, PAYMENT_NOT_FOUND_MESSAGE))
                       .build();
        }

        ValidationResult validationResult = getPaymentAuthorisationsValidator.validate(new CommonPaymentObject(pisCommonPaymentResponse.get()));
        if (validationResult.isNotValid()) {
            return ResponseObject.<Xs2aPaymentCancellationAuthorisationSubResource>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        PisScaAuthorisationService pisScaAuthorisationService = pisScaAuthorisationServiceResolver.getService();
        return pisScaAuthorisationService.getCancellationAuthorisationSubResources(paymentId)
                   .map(resp -> ResponseObject.<Xs2aPaymentCancellationAuthorisationSubResource>builder().body(resp).build())
                   .orElseGet(ResponseObject.<Xs2aPaymentCancellationAuthorisationSubResource>builder()
                                  .fail(PIS_404, of(RESOURCE_UNKNOWN_404))
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

        Optional<PisCommonPaymentResponse> pisCommonPaymentResponse = xs2aPisCommonPaymentService.getPisCommonPaymentById(paymentId);
        if (!pisCommonPaymentResponse.isPresent()) {
            return ResponseObject.<ScaStatus>builder()
                       .fail(PIS_404, of(RESOURCE_UNKNOWN_404, PAYMENT_NOT_FOUND_MESSAGE))
                       .build();
        }

        ValidationResult validationResult = getPaymentAuthorisationScaStatusValidator.validate(new CommonPaymentObject(pisCommonPaymentResponse.get()));
        if (validationResult.isNotValid()) {
            return ResponseObject.<ScaStatus>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        PisScaAuthorisationService pisScaAuthorisationService = pisScaAuthorisationServiceResolver.getService();
        Optional<ScaStatus> scaStatus = pisScaAuthorisationService.getCancellationAuthorisationScaStatus(paymentId, cancellationId);

        if (!scaStatus.isPresent()) {
            return ResponseObject.<ScaStatus>builder()
                       .fail(PIS_403, of(RESOURCE_UNKNOWN_403))
                       .build();
        }

        return ResponseObject.<ScaStatus>builder()
                   .body(scaStatus.get())
                   .build();
    }
}
