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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.pis.InternalPaymentStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.authorisation.CancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.domain.consent.pis.PaymentAuthorisationParameters;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationChainResponsibilityService;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationServiceResolver;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.PisCancellationAuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisCommonPaymentService;
import de.adorsys.psd2.xs2a.service.event.EventAuthorisationType;
import de.adorsys.psd2.xs2a.service.event.EventTypeService;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.validator.pis.CommonPaymentObject;
import de.adorsys.psd2.xs2a.service.validator.pis.authorisation.cancellation.*;
import de.adorsys.psd2.xs2a.service.validator.pis.authorisation.initiation.UpdatePaymentPsuDataPO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.core.error.ErrorType.*;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCancellationAuthorisationServiceImpl implements PaymentCancellationAuthorisationService {

    private final PisScaAuthorisationServiceResolver pisScaAuthorisationServiceResolver;
    private final Xs2aEventService xs2aEventService;
    private final Xs2aAuthorisationService xs2aAuthorisationService;
    private final Xs2aPisCommonPaymentService xs2aPisCommonPaymentService;
    private final CreatePisCancellationAuthorisationValidator createPisCancellationAuthorisationValidator;
    private final UpdatePisCancellationPsuDataValidator updatePisCancellationPsuDataValidator;
    private final GetPaymentCancellationAuthorisationsValidator getPaymentAuthorisationsValidator;
    private final GetPaymentCancellationAuthorisationScaStatusValidator getPaymentAuthorisationScaStatusValidator;
    private final LoggingContextService loggingContextService;
    private final PsuIdDataAuthorisationService psuIdDataAuthorisationService;
    private final EventTypeService eventTypeService;
    private final ScaApproachResolver scaApproachResolver;
    private final AuthorisationChainResponsibilityService authorisationChainResponsibilityService;

    /**
     * Creates authorisation for payment cancellation request if given psu data is valid
     *
     * @param request parameters for creating new authorisation
     * @return Xs2aCreatePisCancellationAuthorisationResponse that contains authorisationId, scaStatus, paymentType and related links
     */
    @Override
    public ResponseObject<CancellationAuthorisationResponse> createPisCancellationAuthorisation(Xs2aCreatePisAuthorisationRequest request) {
        ResponseObject<Xs2aCreatePisCancellationAuthorisationResponse> cancellationAuthorisation =
            createCancellationAuthorisation(request.getPaymentId(), request.getPsuData(), request.getPaymentService(), request.getPaymentProduct());

        if (cancellationAuthorisation.hasError()) {
            return ResponseObject.<CancellationAuthorisationResponse>builder().fail(cancellationAuthorisation.getError()).build();
        }

        if (request.hasNoUpdateData()) {
            return ResponseObject.<CancellationAuthorisationResponse>builder().body(cancellationAuthorisation.getBody()).build();
        }

        String authorisationId = cancellationAuthorisation.getBody().getAuthorisationId();
        PaymentAuthorisationParameters updateRequest = new PaymentAuthorisationParameters(request, authorisationId);
        ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> updatePsuDataResponse = updatePisCancellationPsuData(updateRequest);

        if (updatePsuDataResponse.hasError()) {
            return ResponseObject.<CancellationAuthorisationResponse>builder()
                       .fail(updatePsuDataResponse.getError())
                       .build();
        }

        return ResponseObject.<CancellationAuthorisationResponse>builder()
                   .body(updatePsuDataResponse.getBody())
                   .build();
    }

    /**
     * Update psu data for payment cancellation request if psu data and password are valid
     *
     * @param request update psu data request, which contains paymentId, authorisationId, psuData, password, authenticationMethodId, scaStatus, paymentService and scaAuthenticationData
     * @return Xs2aUpdatePisCommonPaymentPsuDataResponse that contains authorisationId, scaStatus, psuId and related links in case of success, otherwise contains error
     */
    @Override
    public ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> updatePisCancellationPsuData(PaymentAuthorisationParameters request) {
        String paymentId = request.getPaymentId();
        xs2aEventService.recordPisTppRequest(paymentId, eventTypeService.getEventType(request, EventAuthorisationType.PIS_CANCELLATION), request);

        Optional<PisCommonPaymentResponse> pisCommonPaymentResponseOptional = xs2aPisCommonPaymentService.getPisCommonPaymentById(paymentId);
        if (pisCommonPaymentResponseOptional.isEmpty()) {
            log.info("Payment-ID [{}]. Update PIS Cancellation PSU Data has failed. Payment not found by id.", paymentId);
            return ResponseObject.<Xs2aUpdatePisCommonPaymentPsuDataResponse>builder()
                       .fail(PIS_404, of(RESOURCE_UNKNOWN_404_NO_PAYMENT))
                       .build();
        }

        PisCommonPaymentResponse pisCommonPaymentResponse = pisCommonPaymentResponseOptional.get();
        loggingContextService.storeTransactionStatus(pisCommonPaymentResponse.getTransactionStatus());
        ValidationResult validationResult = updatePisCancellationPsuDataValidator.validate(new UpdatePaymentPsuDataPO(pisCommonPaymentResponse, request));

        if (validationResult.isNotValid()) {
            MessageErrorCode messageErrorCode = validationResult.getMessageError().getTppMessage().getMessageErrorCode();

            if (EnumSet.of(PSU_CREDENTIALS_INVALID, FORMAT_ERROR_NO_PSU).contains(messageErrorCode)) {
                xs2aAuthorisationService.updateAuthorisationStatus(request.getAuthorisationId(), ScaStatus.FAILED);
            }

            log.info("Payment-ID [{}], Authorisation-ID [{}]. Update PIS cancellation authorisation - validation failed: {}",
                     paymentId, request.getAuthorisationId(), validationResult.getMessageError());
            return ResponseObject.<Xs2aUpdatePisCommonPaymentPsuDataResponse>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        PisScaAuthorisationService pisScaAuthorisationService = pisScaAuthorisationServiceResolver.getService(request.getAuthorisationId());
        Xs2aUpdatePisCommonPaymentPsuDataResponse response = pisScaAuthorisationService.updateCommonPaymentCancellationPsuData(request);
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
     * Gets list of cancellation identifiers
     *
     * @param paymentId ASPSP identifier of the payment, associated with the authorisation
     * @return Response containing list of cancellation identifiers in case of success or empty list in case of failure
     */
    @Override
    public ResponseObject<Xs2aPaymentCancellationAuthorisationSubResource> getPaymentInitiationCancellationAuthorisationInformation(String paymentId,
                                                                                                                                    PaymentType paymentType,
                                                                                                                                    String paymentProduct) {
        xs2aEventService.recordPisTppRequest(paymentId, EventType.GET_PAYMENT_CANCELLATION_AUTHORISATION_REQUEST_RECEIVED);

        Optional<PisCommonPaymentResponse> pisCommonPaymentResponse = xs2aPisCommonPaymentService.getPisCommonPaymentById(paymentId);
        if (pisCommonPaymentResponse.isEmpty()) {
            log.info("Payment-ID [{}]. Get information PIS Cancellation Authorisation has failed. Payment not found by id.",
                     paymentId);
            return ResponseObject.<Xs2aPaymentCancellationAuthorisationSubResource>builder()
                       .fail(PIS_404, of(RESOURCE_UNKNOWN_404_NO_PAYMENT))
                       .build();
        }

        loggingContextService.storeTransactionStatus(pisCommonPaymentResponse.get().getTransactionStatus());

        ValidationResult validationResult = getPaymentAuthorisationsValidator.validate(new CommonPaymentObject(pisCommonPaymentResponse.get(), paymentType, paymentProduct));
        if (validationResult.isNotValid()) {
            log.info("Payment-ID [{}]. Get information PIS cancellation authorisation - validation failed: {}",
                     paymentId, validationResult.getMessageError());
            return ResponseObject.<Xs2aPaymentCancellationAuthorisationSubResource>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        PisScaAuthorisationService pisScaAuthorisationService = pisScaAuthorisationServiceResolver.getService();
        return pisScaAuthorisationService.getCancellationAuthorisationSubResources(paymentId)
                   .map(resp -> ResponseObject.<Xs2aPaymentCancellationAuthorisationSubResource>builder().body(resp).build())
                   .orElseGet(() -> {
                       log.info("Payment-ID [{}]. Get information PIS Cancellation Authorisation has failed. Authorisation not found by payment id.",
                                paymentId);
                       return ResponseObject.<Xs2aPaymentCancellationAuthorisationSubResource>builder()
                                  .fail(PIS_404, of(RESOURCE_UNKNOWN_404))
                                  .build();
                   });
    }

    /**
     * Gets SCA status response of payment cancellation authorisation
     *
     * @param paymentId       ASPSP identifier of the payment, associated with the authorisation
     * @param authorisationId cancellation authorisation identifier
     * @return Response containing SCA status of authorisation and optionally trusted beneficiary flag or corresponding error
     */
    @Override
    public ResponseObject<PaymentScaStatus> getPaymentCancellationAuthorisationScaStatus(String paymentId,
                                                                                         String authorisationId,
                                                                                         PaymentType paymentType,
                                                                                         String paymentProduct) {
        xs2aEventService.recordPisTppRequest(paymentId, EventType.GET_PAYMENT_CANCELLATION_SCA_STATUS_REQUEST_RECEIVED);

        Optional<PisCommonPaymentResponse> pisCommonPaymentResponseOptional = xs2aPisCommonPaymentService.getPisCommonPaymentById(paymentId);
        if (pisCommonPaymentResponseOptional.isEmpty()) {
            log.info("Payment-ID [{}]. Get SCA status PIS Cancellation Authorisation has failed. Payment not found by id.",
                     paymentId);
            return ResponseObject.<PaymentScaStatus>builder()
                       .fail(PIS_404, of(RESOURCE_UNKNOWN_404_NO_PAYMENT))
                       .build();
        }

        PisCommonPaymentResponse pisCommonPaymentResponse = pisCommonPaymentResponseOptional.get();
        ValidationResult validationResult =
            getPaymentAuthorisationScaStatusValidator.validate(new GetPaymentCancellationAuthorisationScaStatusPO(pisCommonPaymentResponse,
                                                                                                                  authorisationId,
                                                                                                                  paymentType,
                                                                                                                  paymentProduct));
        if (validationResult.isNotValid()) {
            log.info("Payment-ID [{}]. Get SCA status PIS cancellation authorisation - validation failed: {}",
                     paymentId, validationResult.getMessageError());
            return ResponseObject.<PaymentScaStatus>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        PisScaAuthorisationService pisScaAuthorisationService = pisScaAuthorisationServiceResolver.getService(authorisationId);
        Optional<ScaStatus> scaStatusOptional = pisScaAuthorisationService.getCancellationAuthorisationScaStatus(paymentId, authorisationId);

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

    private ResponseObject<Xs2aCreatePisCancellationAuthorisationResponse> createCancellationAuthorisation(String paymentId, PsuIdData psuIdData, PaymentType paymentType, String paymentProduct) {
        xs2aEventService.recordPisTppRequest(paymentId, EventType.START_PAYMENT_CANCELLATION_AUTHORISATION_REQUEST_RECEIVED);

        Optional<PisCommonPaymentResponse> pisCommonPaymentResponseOptional = xs2aPisCommonPaymentService.getPisCommonPaymentById(paymentId);
        if (pisCommonPaymentResponseOptional.isEmpty()) {
            log.info("Payment-ID [{}]. Create PIS Cancellation Authorization has failed. Payment not found by id.",
                     paymentId);
            return ResponseObject.<Xs2aCreatePisCancellationAuthorisationResponse>builder()
                       .fail(PIS_404, of(RESOURCE_UNKNOWN_404_NO_PAYMENT))
                       .build();
        }

        PisCommonPaymentResponse pisCommonPaymentResponse = pisCommonPaymentResponseOptional.get();
        if (pisCommonPaymentResponse.getInternalPaymentStatus() != InternalPaymentStatus.CANCELLED_INITIATED) {
            log.info("Payment-ID [{}]. Create PIS Cancellation Authorization has failed. Invalid flow: payment wasn't cancelled.",
                     paymentId);
            return ResponseObject.<Xs2aCreatePisCancellationAuthorisationResponse>builder()
                       .fail(PIS_403, of(FORBIDDEN_INCORRECT_FLOW))
                       .build();
        }

        ValidationResult validationResult =
            createPisCancellationAuthorisationValidator.validate(new CreatePisCancellationAuthorisationObject(pisCommonPaymentResponseOptional.get(),
                                                                                                              psuIdData,
                                                                                                              paymentType,
                                                                                                              paymentProduct));
        if (validationResult.isNotValid()) {
            log.info("Payment-ID [{}]. Create PIS Cancellation Authorisation - validation failed: {}",
                     paymentId, validationResult.getMessageError());
            return ResponseObject.<Xs2aCreatePisCancellationAuthorisationResponse>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }
        // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/-/issues/1629
        ScaStatus scaStatus = ScaStatus.STARTED;
        String authorisationId = UUID.randomUUID().toString();
        ScaApproach scaApproach = scaApproachResolver.resolveScaApproach();
        StartAuthorisationsParameters startAuthorisationsParameters = StartAuthorisationsParameters.builder()
                                                                          .psuData(psuIdData)
                                                                          .businessObjectId(paymentId)
                                                                          .scaStatus(scaStatus)
                                                                          .authorisationId(authorisationId)
                                                                          .build();
        Authorisation authorisation = new Authorisation(authorisationId, psuIdData, paymentId, AuthorisationType.PIS_CANCELLATION, scaStatus);
        PisCancellationAuthorisationProcessorRequest processorRequest = new PisCancellationAuthorisationProcessorRequest(scaApproach, scaStatus, startAuthorisationsParameters, authorisation);
        CreatePaymentAuthorisationProcessorResponse processorResponse =
            (CreatePaymentAuthorisationProcessorResponse) authorisationChainResponsibilityService.apply(processorRequest);

        loggingContextService.storeScaStatus(processorResponse.getScaStatus());

        Xs2aCreateAuthorisationRequest createAuthorisationRequest = Xs2aCreateAuthorisationRequest.builder()
                                                                        .psuData(psuIdData)
                                                                        .paymentId(paymentId)
                                                                        .authorisationId(authorisationId)
                                                                        .scaStatus(processorResponse.getScaStatus())
                                                                        .scaApproach(processorResponse.getScaApproach())
                                                                        .build();

        PisScaAuthorisationService pisScaAuthorisationService = pisScaAuthorisationServiceResolver.getService();
        Optional<Xs2aCreatePisCancellationAuthorisationResponse> createAuthorisationResponseOptional =
            pisScaAuthorisationService.createCommonPaymentCancellationAuthorisation(createAuthorisationRequest, paymentType);

        if (createAuthorisationResponseOptional.isEmpty()) {
            return ResponseObject.<Xs2aCreatePisCancellationAuthorisationResponse>builder()
                       .fail(PIS_400, of(FORMAT_ERROR))
                       .build();
        }

        Xs2aCreatePisCancellationAuthorisationResponse createAuthorisationResponse = createAuthorisationResponseOptional.get();
        setPsuMessageAndTppMessages(createAuthorisationResponse, processorResponse.getPsuMessage(), processorResponse.getTppMessages());
        loggingContextService.storeTransactionAndScaStatus(pisCommonPaymentResponseOptional.get().getTransactionStatus(), createAuthorisationResponse.getScaStatus());

        return ResponseObject.<Xs2aCreatePisCancellationAuthorisationResponse>builder()
                   .body(createAuthorisationResponse)
                   .build();
    }

    private void setPsuMessageAndTppMessages(AuthorisationResponse response,
                                             String psuMessage, Set<TppMessageInformation> tppMessageInformationSet) {
        if (psuMessage != null) {
            response.setPsuMessage(psuMessage);
        }
        if (tppMessageInformationSet != null) {
            response.getTppMessageInformation().addAll(tppMessageInformationSet);
        }
    }
}
