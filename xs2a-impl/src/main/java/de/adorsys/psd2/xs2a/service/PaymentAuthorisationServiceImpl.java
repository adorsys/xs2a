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
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthorisationSubResources;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationServiceResolver;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisCommonPaymentService;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.pis.CommonPaymentObject;
import de.adorsys.psd2.xs2a.service.validator.pis.authorisation.initiation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.*;

@Service
@RequiredArgsConstructor
public class PaymentAuthorisationServiceImpl implements PaymentAuthorisationService {
    private static final String PAYMENT_NOT_FOUND_MESSAGE = "Payment not found";

    private final Xs2aEventService xs2aEventService;
    private final PisScaAuthorisationServiceResolver pisScaAuthorisationServiceResolver;
    private final Xs2aPisCommonPaymentService pisCommonPaymentService;
    private final CreatePisAuthorisationValidator createPisAuthorisationValidator;
    private final UpdatePisCommonPaymentPsuDataValidator updatePisCommonPaymentPsuDataValidator;
    private final GetPaymentInitiationAuthorisationsValidator getPaymentAuthorisationsValidator;
    private final GetPaymentInitiationAuthorisationScaStatusValidator getPaymentAuthorisationScaStatusValidator;

    /**
     * Creates authorisation for payment request if given psu data is valid
     *
     * @param paymentId      String representation of payment identification
     * @param psuData        Contains authorisation data about PSU
     * @param paymentType    Payment type supported by aspsp
     * @param paymentProduct payment product used for payment creation (e.g. sepa-credit-transfers, instant-sepa-credit-transfers...)
     * @return Xs2aCreatePisAuthorisationResponse that contains authorisationId, scaStatus, paymentType and related links
     */
    @Override
    public ResponseObject<Xs2aCreatePisAuthorisationResponse> createPisAuthorization(String paymentId, PaymentType paymentType, String paymentProduct, PsuIdData psuData) {
        xs2aEventService.recordPisTppRequest(paymentId, EventType.START_PAYMENT_AUTHORISATION_REQUEST_RECEIVED);

        Optional<PisCommonPaymentResponse> pisCommonPaymentResponse = pisCommonPaymentService.getPisCommonPaymentById(paymentId);
        if (!pisCommonPaymentResponse.isPresent()) {
            return ResponseObject.<Xs2aCreatePisAuthorisationResponse>builder()
                       .fail(PIS_404, of(RESOURCE_UNKNOWN_404, PAYMENT_NOT_FOUND_MESSAGE))
                       .build();
        }

        ValidationResult validationResult = createPisAuthorisationValidator.validate(new CommonPaymentObject(pisCommonPaymentResponse.get()));
        if (validationResult.isNotValid()) {
            return ResponseObject.<Xs2aCreatePisAuthorisationResponse>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        PisScaAuthorisationService pisScaAuthorisationService = pisScaAuthorisationServiceResolver.getService();
        return pisScaAuthorisationService.createCommonPaymentAuthorisation(paymentId, paymentType, psuData)
                   .map(resp -> ResponseObject.<Xs2aCreatePisAuthorisationResponse>builder()
                                    .body(resp)
                                    .build())
                   .orElseGet(ResponseObject.<Xs2aCreatePisAuthorisationResponse>builder()
                                  .fail(PIS_400, of(PAYMENT_FAILED))
                                  ::build);
    }

    /**
     * Update psu data for payment request if psu data and password are valid
     *
     * @param request update psu data request, which contains paymentId, authorisationId, psuData, password, authenticationMethodId, scaStatus, paymentService and scaAuthenticationData
     * @return Xs2aUpdatePisCommonPaymentPsuDataResponse that contains authorisationId, scaStatus, psuId and related links in case of success
     */
    @Override
    public ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> updatePisCommonPaymentPsuData(Xs2aUpdatePisCommonPaymentPsuDataRequest request) {
        xs2aEventService.recordPisTppRequest(request.getPaymentId(), EventType.UPDATE_PAYMENT_AUTHORISATION_PSU_DATA_REQUEST_RECEIVED, request);

        Optional<PisCommonPaymentResponse> pisCommonPaymentResponse = pisCommonPaymentService.getPisCommonPaymentById(request.getPaymentId());
        if (!pisCommonPaymentResponse.isPresent()) {
            return ResponseObject.<Xs2aUpdatePisCommonPaymentPsuDataResponse>builder()
                       .fail(PIS_404, of(RESOURCE_UNKNOWN_404, PAYMENT_NOT_FOUND_MESSAGE))
                       .build();
        }

        ValidationResult validationResult = updatePisCommonPaymentPsuDataValidator.validate(new UpdatePisCommonPaymentPsuDataPO(pisCommonPaymentResponse.get(), request.getAuthorisationId()));
        if (validationResult.isNotValid()) {
            return ResponseObject.<Xs2aUpdatePisCommonPaymentPsuDataResponse>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        PisScaAuthorisationService pisScaAuthorisationService = pisScaAuthorisationServiceResolver.getService();
        Xs2aUpdatePisCommonPaymentPsuDataResponse response = pisScaAuthorisationService.updateCommonPaymentPsuData(request);

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
     * Gets authorisations for current payment
     *
     * @param paymentId ASPSP identifier of the payment, associated with the authorisation
     * @return Response containing list of authorisations
     */
    @Override
    public ResponseObject<Xs2aAuthorisationSubResources> getPaymentInitiationAuthorisations(String paymentId) {
        xs2aEventService.recordPisTppRequest(paymentId, EventType.GET_PAYMENT_AUTHORISATION_REQUEST_RECEIVED);

        Optional<PisCommonPaymentResponse> pisCommonPaymentResponse = pisCommonPaymentService.getPisCommonPaymentById(paymentId);
        if (!pisCommonPaymentResponse.isPresent()) {
            return ResponseObject.<Xs2aAuthorisationSubResources>builder()
                       .fail(PIS_404, of(RESOURCE_UNKNOWN_404, PAYMENT_NOT_FOUND_MESSAGE))
                       .build();
        }

        ValidationResult validationResult = getPaymentAuthorisationsValidator.validate(new CommonPaymentObject(pisCommonPaymentResponse.get()));
        if (validationResult.isNotValid()) {
            return ResponseObject.<Xs2aAuthorisationSubResources>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        PisScaAuthorisationService pisScaAuthorisationService = pisScaAuthorisationServiceResolver.getService();
        return pisScaAuthorisationService.getAuthorisationSubResources(paymentId)
                   .map(resp -> ResponseObject.<Xs2aAuthorisationSubResources>builder().body(resp).build())
                   .orElseGet(ResponseObject.<Xs2aAuthorisationSubResources>builder()
                                  .fail(PIS_404, of(RESOURCE_UNKNOWN_404))
                                  ::build);
    }

    /**
     * Gets SCA status of payment initiation authorisation
     *
     * @param paymentId       ASPSP identifier of the payment, associated with the authorisation
     * @param authorisationId authorisation identifier
     * @return Response containing SCA status of authorisation or corresponding error
     */
    @Override
    public ResponseObject<ScaStatus> getPaymentInitiationAuthorisationScaStatus(String paymentId, String authorisationId) {
        xs2aEventService.recordPisTppRequest(paymentId, EventType.GET_PAYMENT_SCA_STATUS_REQUEST_RECEIVED);

        Optional<PisCommonPaymentResponse> pisCommonPaymentResponse = pisCommonPaymentService.getPisCommonPaymentById(paymentId);
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
        Optional<ScaStatus> scaStatus = pisScaAuthorisationService.getAuthorisationScaStatus(paymentId, authorisationId);

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
