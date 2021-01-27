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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.PaymentScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthorisationSubResources;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationRequest;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationServiceResolver;
import de.adorsys.psd2.xs2a.service.consent.PisPsuDataService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisCommonPaymentService;
import de.adorsys.psd2.xs2a.service.event.EventAuthorisationType;
import de.adorsys.psd2.xs2a.service.event.EventTypeService;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.pis.CommonPaymentObject;
import de.adorsys.psd2.xs2a.service.validator.pis.authorisation.initiation.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.core.error.ErrorType.*;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentAuthorisationServiceImpl implements PaymentAuthorisationService {

    private final Xs2aEventService xs2aEventService;
    private final PisScaAuthorisationServiceResolver pisScaAuthorisationServiceResolver;
    private final Xs2aAuthorisationService authorisationService;
    private final Xs2aPisCommonPaymentService pisCommonPaymentService;
    private final CreatePisAuthorisationValidator createPisAuthorisationValidator;
    private final UpdatePisCommonPaymentPsuDataValidator updatePisCommonPaymentPsuDataValidator;
    private final GetPaymentInitiationAuthorisationsValidator getPaymentAuthorisationsValidator;
    private final GetPaymentInitiationAuthorisationScaStatusValidator getPaymentAuthorisationScaStatusValidator;
    private final PisPsuDataService pisPsuDataService;
    private final LoggingContextService loggingContextService;
    private final PsuIdDataAuthorisationService psuIdDataAuthorisationService;
    private final EventTypeService eventTypeService;

    /**
     * Creates pis authorisation for payment. In case when psu data and password came then second step will be update psu data in created authorisation
     *
     * @param createRequest data container to create pis authorisation
     * @return ResponseObject it could contains data after create or update pis authorisation
     */
    @Override
    public ResponseObject<AuthorisationResponse> createPisAuthorisation(Xs2aCreatePisAuthorisationRequest createRequest) {
        ResponseObject<Xs2aCreatePisAuthorisationResponse> createPisAuthorisationResponse = createPisAuthorisation(createRequest.getPaymentId(),
                                                                                                                   createRequest.getPaymentService(),
                                                                                                                   createRequest.getPaymentProduct(),
                                                                                                                   createRequest.getPsuData());

        if (createPisAuthorisationResponse.hasError()) {
            return ResponseObject.<AuthorisationResponse>builder()
                       .fail(createPisAuthorisationResponse.getError())
                       .build();
        }

        PsuIdData psuIdDataFromResponse = createPisAuthorisationResponse.getBody().getPsuIdData();
        if (psuIdDataFromResponse == null || psuIdDataFromResponse.isEmpty()
                || StringUtils.isBlank(createRequest.getPassword())) {
            return ResponseObject.<AuthorisationResponse>builder()
                       .body(createPisAuthorisationResponse.getBody())
                       .build();
        }

        String authorisationId = createPisAuthorisationResponse.getBody().getAuthorisationId();
        Xs2aUpdatePisCommonPaymentPsuDataRequest updateRequest = new Xs2aUpdatePisCommonPaymentPsuDataRequest(createRequest, authorisationId);
        ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> updatePsuDataResponse = updatePisCommonPaymentPsuData(updateRequest);

        if (updatePsuDataResponse.hasError()) {
            return ResponseObject.<AuthorisationResponse>builder()
                       .fail(updatePsuDataResponse.getError())
                       .build();
        }

        return ResponseObject.<AuthorisationResponse>builder()
                   .body(updatePsuDataResponse.getBody())
                   .build();
    }

    /**
     * Update psu data for payment request if psu data and password are valid
     *
     * @param request update psu data request, which contains paymentId, authorisationId, psuData, password, authenticationMethodId, scaStatus, paymentService and scaAuthenticationData
     * @return Xs2aUpdatePisCommonPaymentPsuDataResponse that contains authorisationId, scaStatus, psuId and related links in case of success
     */
    @Override
    public ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> updatePisCommonPaymentPsuData(Xs2aUpdatePisCommonPaymentPsuDataRequest request) {
        xs2aEventService.recordPisTppRequest(request.getPaymentId(), eventTypeService.getEventType(request, EventAuthorisationType.PIS), request);

        Optional<PisCommonPaymentResponse> pisCommonPaymentResponse = pisCommonPaymentService.getPisCommonPaymentById(request.getPaymentId());
        if (pisCommonPaymentResponse.isEmpty()) {
            log.info("Payment-ID [{}]. Update PIS CommonPayment PSU data failed. PIS CommonPayment not found by id",
                     request.getPaymentId());
            return ResponseObject.<Xs2aUpdatePisCommonPaymentPsuDataResponse>builder()
                       .fail(PIS_404, of(RESOURCE_UNKNOWN_404_NO_PAYMENT))
                       .build();
        }

        PisCommonPaymentResponse pisCommonPayment = pisCommonPaymentResponse.get();
        loggingContextService.storeTransactionStatus(pisCommonPayment.getTransactionStatus());
        ValidationResult validationResult = updatePisCommonPaymentPsuDataValidator.validate(new UpdatePisCommonPaymentPsuDataPO(pisCommonPayment, request));

        if (validationResult.isNotValid()) {
            MessageErrorCode messageErrorCode = validationResult.getMessageError().getTppMessage().getMessageErrorCode();

            if (EnumSet.of(PSU_CREDENTIALS_INVALID, FORMAT_ERROR_NO_PSU).contains(messageErrorCode)) {
                authorisationService.updateAuthorisationStatus(request.getAuthorisationId(), ScaStatus.FAILED);
            }

            log.info("Payment-ID [{}]. Update PIS CommonPayment PSU data - validation failed: {}",
                     request.getPaymentId(), validationResult.getMessageError());
            return ResponseObject.<Xs2aUpdatePisCommonPaymentPsuDataResponse>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        PisScaAuthorisationService pisScaAuthorisationService = pisScaAuthorisationServiceResolver.getService(request.getAuthorisationId());
        Xs2aUpdatePisCommonPaymentPsuDataResponse response = pisScaAuthorisationService.updateCommonPaymentPsuData(request);
        loggingContextService.storeScaStatus(response.getScaStatus());

        return response.hasError()
                   ? ResponseObject.<Xs2aUpdatePisCommonPaymentPsuDataResponse>builder()
                         .fail(response.getErrorHolder())
                         .build()
                   : ResponseObject.<Xs2aUpdatePisCommonPaymentPsuDataResponse>builder()
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
    public ResponseObject<Xs2aAuthorisationSubResources> getPaymentInitiationAuthorisations(String paymentId, String paymentProduct, PaymentType paymentType) {
        xs2aEventService.recordPisTppRequest(paymentId, EventType.GET_PAYMENT_AUTHORISATION_REQUEST_RECEIVED);

        Optional<PisCommonPaymentResponse> pisCommonPaymentResponseOptional = pisCommonPaymentService.getPisCommonPaymentById(paymentId);
        if (pisCommonPaymentResponseOptional.isEmpty()) {
            log.info("Payment-ID [{}]. Get Payment authorisation failed. PIS CommonPayment not found by id", paymentId);
            return ResponseObject.<Xs2aAuthorisationSubResources>builder()
                       .fail(PIS_404, of(RESOURCE_UNKNOWN_404_NO_PAYMENT))
                       .build();
        }
        PisCommonPaymentResponse pisCommonPaymentResponse = pisCommonPaymentResponseOptional.get();

        ValidationResult validationResult = getPaymentAuthorisationsValidator.validate(new CommonPaymentObject(pisCommonPaymentResponse, paymentType, paymentProduct));
        if (validationResult.isNotValid()) {
            log.info("Payment-ID [{}]. Get payment initiation authorisation - validation failed: {}",
                     paymentId, validationResult.getMessageError());
            return ResponseObject.<Xs2aAuthorisationSubResources>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        loggingContextService.storeTransactionStatus(pisCommonPaymentResponse.getTransactionStatus());

        PisScaAuthorisationService pisScaAuthorisationService = pisScaAuthorisationServiceResolver.getService();
        return pisScaAuthorisationService.getAuthorisationSubResources(paymentId)
                   .map(resp -> ResponseObject.<Xs2aAuthorisationSubResources>builder().body(resp).build())
                   .orElseGet(() -> {
                       log.info("Payment-ID [{}]. Get payment initiation authorisation has failed. Authorisation not found by payment id.",
                                paymentId);
                       return ResponseObject.<Xs2aAuthorisationSubResources>builder()
                                  .fail(PIS_404, of(RESOURCE_UNKNOWN_404))
                                  .build();
                   });
    }

    /**
     * Gets SCA status response of payment initiation authorisation
     *
     * @param paymentId       ASPSP identifier of the payment, associated with the authorisation
     * @param authorisationId authorisation identifier
     * @return Response containing SCA status of authorisation and optionally trusted beneficiary flag or corresponding error
     */
    @Override
    public ResponseObject<PaymentScaStatus> getPaymentInitiationAuthorisationScaStatus(String paymentId, String authorisationId,
                                                                                       PaymentType paymentType, String paymentProduct) {
        xs2aEventService.recordPisTppRequest(paymentId, EventType.GET_PAYMENT_SCA_STATUS_REQUEST_RECEIVED);

        Optional<PisCommonPaymentResponse> pisCommonPaymentResponseOptional = pisCommonPaymentService.getPisCommonPaymentById(paymentId);
        if (pisCommonPaymentResponseOptional.isEmpty()) {
            log.info("Payment-ID [{}]. Get SCA status payment initiation authorisation failed. PIS CommonPayment not found by id",
                     paymentId);
            return ResponseObject.<PaymentScaStatus>builder()
                       .fail(PIS_404, of(RESOURCE_UNKNOWN_404_NO_PAYMENT))
                       .build();
        }

        PisCommonPaymentResponse pisCommonPaymentResponse = pisCommonPaymentResponseOptional.get();
        ValidationResult validationResult = getPaymentAuthorisationScaStatusValidator.validate(new GetPaymentInitiationAuthorisationScaStatusPO(pisCommonPaymentResponse,
                                                                                                                                                authorisationId,
                                                                                                                                                paymentType,
                                                                                                                                                paymentProduct));
        if (validationResult.isNotValid()) {
            log.info("Payment-ID [{}]. Get SCA status payment initiation authorisation - validation failed: {}",
                     paymentId, validationResult.getMessageError());
            return ResponseObject.<PaymentScaStatus>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        PisScaAuthorisationService pisScaAuthorisationService = pisScaAuthorisationServiceResolver.getService(authorisationId);
        Optional<ScaStatus> scaStatusOptional = pisScaAuthorisationService.getAuthorisationScaStatus(paymentId, authorisationId);

        if (scaStatusOptional.isEmpty()) {
            return ResponseObject.<PaymentScaStatus>builder()
                       .fail(PIS_403, of(RESOURCE_UNKNOWN_403))
                       .build();
        }

        ScaStatus scaStatus = scaStatusOptional.get();

        PsuIdData psuIdData = psuIdDataAuthorisationService.getPsuIdData(authorisationId, pisCommonPaymentResponse.getPsuData());

        PaymentScaStatus paymentScaStatus = new PaymentScaStatus(psuIdData, pisCommonPaymentResponse, scaStatus);

        loggingContextService.storeTransactionAndScaStatus(pisCommonPaymentResponse.getTransactionStatus(), scaStatus);

        return ResponseObject.<PaymentScaStatus>builder()
                   .body(paymentScaStatus)
                   .build();
    }

    private ResponseObject<Xs2aCreatePisAuthorisationResponse> createPisAuthorisation(String paymentId, PaymentType paymentService,
                                                                                      String paymentProduct, PsuIdData psuDataFromRequest) {
        xs2aEventService.recordPisTppRequest(paymentId, EventType.START_PAYMENT_AUTHORISATION_REQUEST_RECEIVED);

        Optional<PisCommonPaymentResponse> pisCommonPaymentResponseOptional = pisCommonPaymentService.getPisCommonPaymentById(paymentId);
        if (pisCommonPaymentResponseOptional.isEmpty()) {
            log.info("Payment-ID [{}]. Create PIS Authorisation failed. PIS CommonPayment not found by id", paymentId);
            return ResponseObject.<Xs2aCreatePisAuthorisationResponse>builder()
                       .fail(PIS_404, of(RESOURCE_UNKNOWN_404_NO_PAYMENT))
                       .build();
        }

        PisCommonPaymentResponse pisCommonPayment = pisCommonPaymentResponseOptional.get();

        ValidationResult validationResult = createPisAuthorisationValidator.validate(new CreatePisAuthorisationObject(pisCommonPayment, paymentService, paymentProduct, psuDataFromRequest));

        if (validationResult.isNotValid()) {
            log.info("Payment-ID [{}]. Create PIS Authorisation - validation failed: {}",
                     paymentId, validationResult.getMessageError());
            return ResponseObject.<Xs2aCreatePisAuthorisationResponse>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        PisScaAuthorisationService pisScaAuthorisationService = pisScaAuthorisationServiceResolver.getService();

        PsuIdData psuIdData = getActualPsuData(psuDataFromRequest, paymentId, pisCommonPayment.isMultilevelScaRequired());
        Optional<Xs2aCreatePisAuthorisationResponse> commonPaymentAuthorisation = pisScaAuthorisationService.createCommonPaymentAuthorisation(paymentId, paymentService, psuIdData);

        if (commonPaymentAuthorisation.isEmpty()) {
            return ResponseObject.<Xs2aCreatePisAuthorisationResponse>builder()
                       .fail(PIS_400, of(PAYMENT_FAILED))
                       .build();
        }

        Xs2aCreatePisAuthorisationResponse createAuthorisationResponse = commonPaymentAuthorisation.get();
        loggingContextService.storeTransactionAndScaStatus(pisCommonPayment.getTransactionStatus(), createAuthorisationResponse.getScaStatus());

        return ResponseObject.<Xs2aCreatePisAuthorisationResponse>builder()
                   .body(createAuthorisationResponse)
                   .build();
    }

    private PsuIdData getActualPsuData(PsuIdData psuDataFromRequest, String paymentId, boolean isMultilevel) {
        if (psuDataFromRequest.isNotEmpty() || isMultilevel) {
            return psuDataFromRequest;
        }

        return pisPsuDataService.getPsuDataByPaymentId(paymentId).stream()
                   .findFirst()
                   .orElse(psuDataFromRequest);
    }
}
