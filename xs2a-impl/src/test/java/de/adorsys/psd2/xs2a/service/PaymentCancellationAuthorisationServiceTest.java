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
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.pis.InternalPaymentStatus;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponseType;
import de.adorsys.psd2.xs2a.domain.authorisation.CancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.domain.consent.pis.PaymentAuthorisationParameters;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationChainResponsibilityService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationServiceResolver;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisCommonPaymentService;
import de.adorsys.psd2.xs2a.service.event.EventAuthorisationType;
import de.adorsys.psd2.xs2a.service.event.EventTypeService;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.validator.pis.CommonPaymentObject;
import de.adorsys.psd2.xs2a.service.validator.pis.authorisation.cancellation.*;
import de.adorsys.psd2.xs2a.service.validator.pis.authorisation.initiation.UpdatePaymentPsuDataPO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.core.profile.PaymentType.SINGLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentCancellationAuthorisationServiceTest {
    private static final String CORRECT_PSU_ID = "marion.mueller";
    private static final String PAYMENT_ID = "594ef79c-d785-41ec-9b14-2ea3a7ae2c7b";
    private static final String WRONG_PAYMENT_ID = "wrong payment id";
    private static final String NOT_EXISTING_PAYMENT_ID = "not existing payment id";
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String INVALID_AUTHORISATION_ID = "invalid authorisation id";
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(CORRECT_PSU_ID, null, null, null, null);
    private static final String WRONG_CANCELLATION_AUTHORISATION_ID = "wrong cancellation authorisation id";
    private static final String CANCELLATION_AUTHORISATION_ID = "dd5d766f-eeb7-4efe-b730-24d5ed53f537";
    private static final TransactionStatus TRANSACTION_STATUS = TransactionStatus.RCVD;

    private static final PisCommonPaymentResponse PIS_COMMON_PAYMENT_RESPONSE = buildPisCommonPaymentResponse();
    private static final PisCommonPaymentResponse INVALID_PIS_COMMON_PAYMENT_RESPONSE = buildInvalidPisCommonPaymentResponse();

    private static final MessageError VALIDATION_ERROR = new MessageError(ErrorType.PIS_401, TppMessageInformation.of(UNAUTHORIZED));
    private static final MessageError AUTHORISATION_SERVICE_ERROR = new MessageError(ErrorType.PIS_404, TppMessageInformation.of(RESOURCE_UNKNOWN_404));
    private static final MessageError UNKNOWN_PAYMENT_ERROR = new MessageError(ErrorType.PIS_404, TppMessageInformation.of(RESOURCE_UNKNOWN_404_NO_PAYMENT));
    private static final MessageError INVALID_FLOW_ERROR = new MessageError(ErrorType.PIS_403, TppMessageInformation.of(FORBIDDEN_INCORRECT_FLOW));
    private static final MessageError SCA_STATUS_ERROR = new MessageError(ErrorType.PIS_403, of(RESOURCE_UNKNOWN_403));
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    private static final ScaApproach SCA_APPROACH = ScaApproach.EMBEDDED;
    private static final Set<TppMessageInformation> TEST_TPP_MESSAGES = Collections.singleton(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR));
    private static final String TEST_PSU_MESSAGE = "This test message is created in ASPSP and directed to PSU";

    @InjectMocks
    private PaymentCancellationAuthorisationServiceImpl paymentCancellationAuthorisationService;

    @Mock
    private Xs2aEventService xs2aEventService;
    @Mock
    private PisScaAuthorisationService pisScaAuthorisationService;
    @Mock
    private PisScaAuthorisationServiceResolver pisScaAuthorisationServiceResolver;
    @Mock
    private Xs2aPisCommonPaymentService xs2aPisCommonPaymentService;
    @Mock
    private UpdatePisCancellationPsuDataValidator updatePisCancellationPsuDataValidator;
    @Mock
    private GetPaymentCancellationAuthorisationsValidator getPaymentCancellationAuthorisationsValidator;
    @Mock
    private GetPaymentCancellationAuthorisationScaStatusValidator getPaymentCancellationAuthorisationScaStatusValidator;
    @Mock
    private LoggingContextService loggingContextService;
    @Mock
    private CreatePisCancellationAuthorisationValidator createPisCancellationAuthorisationValidator;
    @Mock
    private PsuIdDataAuthorisationService psuIdDataAuthorisationService;
    @Mock
    private EventTypeService eventTypeService;
    @Mock
    private ScaApproachResolver scaApproachResolver;
    @Mock
    private AuthorisationChainResponsibilityService authorisationChainResponsibilityService;

    @Test
    void createPisCancellationAuthorisation_Success_ShouldRecordEvent() {
        // Given
        when(pisScaAuthorisationServiceResolver.getService())
            .thenReturn(pisScaAuthorisationService);

        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.of(PIS_COMMON_PAYMENT_RESPONSE));

        when(createPisCancellationAuthorisationValidator.validate(any(CreatePisCancellationAuthorisationObject.class))).thenReturn(ValidationResult.valid());

        when(pisScaAuthorisationService.createCommonPaymentCancellationAuthorisation(any(), any()))
            .thenReturn(Optional.of(new Xs2aCreatePisCancellationAuthorisationResponse(CANCELLATION_AUTHORISATION_ID, null, null, null)));
        CreatePaymentAuthorisationProcessorResponse response = new CreatePaymentAuthorisationProcessorResponse(SCA_STATUS, SCA_APPROACH, TEST_PSU_MESSAGE, TEST_TPP_MESSAGES, PAYMENT_ID, PSU_ID_DATA);
        when(authorisationChainResponsibilityService.apply(any())).thenReturn(response);

        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        paymentCancellationAuthorisationService.createPisCancellationAuthorisation(new Xs2aCreatePisAuthorisationRequest(PAYMENT_ID, PSU_ID_DATA, PAYMENT_PRODUCT, SINGLE, null));

        // Then
        verify(xs2aEventService, times(1)).recordPisTppRequest(eq(PAYMENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.START_PAYMENT_CANCELLATION_AUTHORISATION_REQUEST_RECEIVED);
    }

    @Test
    void createPisCancellationAuthorisation_success() {
        // Given
        ScaStatus scaStatus = ScaStatus.RECEIVED;
        PaymentType paymentType = PaymentType.SINGLE;

        when(pisScaAuthorisationServiceResolver.getService())
            .thenReturn(pisScaAuthorisationService);

        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.of(PIS_COMMON_PAYMENT_RESPONSE));

        when(createPisCancellationAuthorisationValidator.validate(any(CreatePisCancellationAuthorisationObject.class))).thenReturn(ValidationResult.valid());
        CreatePaymentAuthorisationProcessorResponse response = new CreatePaymentAuthorisationProcessorResponse(SCA_STATUS, SCA_APPROACH, TEST_PSU_MESSAGE, TEST_TPP_MESSAGES, PAYMENT_ID, PSU_ID_DATA);
        when(authorisationChainResponsibilityService.apply(any())).thenReturn(response);
        when(pisScaAuthorisationService.createCommonPaymentCancellationAuthorisation(any(), eq(paymentType)))
            .thenReturn(Optional.of(new Xs2aCreatePisCancellationAuthorisationResponse(CANCELLATION_AUTHORISATION_ID, scaStatus, paymentType, null)));

        // When
        ResponseObject<CancellationAuthorisationResponse> pisCancellationAuthorisation =
            paymentCancellationAuthorisationService.createPisCancellationAuthorisation(new Xs2aCreatePisAuthorisationRequest(PAYMENT_ID, PSU_ID_DATA, PAYMENT_PRODUCT, paymentType, null));

        // Then
        assertThat(pisCancellationAuthorisation.hasError()).isFalse();

        CancellationAuthorisationResponse responseBody = pisCancellationAuthorisation.getBody();
        assertThat(responseBody.getAuthorisationResponseType()).isEqualTo(AuthorisationResponseType.START);
        assertThat(responseBody.getAuthorisationId()).isEqualTo(CANCELLATION_AUTHORISATION_ID);
        assertThat(responseBody.getScaStatus()).isEqualTo(scaStatus);

        Xs2aCreatePisCancellationAuthorisationResponse concreteResponseBody = (Xs2aCreatePisCancellationAuthorisationResponse) responseBody;
        assertThat(concreteResponseBody.getPaymentType()).isEqualTo(paymentType);
    }

    @Test
    void createPisCancellationAuthorisation_shouldStoreStatusesInLoggingContext() {
        // Given:
        ScaStatus authorisationStatus = ScaStatus.PSUIDENTIFIED;

        when(pisScaAuthorisationServiceResolver.getService())
            .thenReturn(pisScaAuthorisationService);

        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.of(PIS_COMMON_PAYMENT_RESPONSE));

        when(createPisCancellationAuthorisationValidator.validate(any(CreatePisCancellationAuthorisationObject.class))).thenReturn(ValidationResult.valid());
        CreatePaymentAuthorisationProcessorResponse response = new CreatePaymentAuthorisationProcessorResponse(SCA_STATUS, SCA_APPROACH, TEST_PSU_MESSAGE, TEST_TPP_MESSAGES, PAYMENT_ID, PSU_ID_DATA);
        when(authorisationChainResponsibilityService.apply(any())).thenReturn(response);
        when(pisScaAuthorisationService.createCommonPaymentCancellationAuthorisation(any(), any()))
            .thenReturn(Optional.of(new Xs2aCreatePisCancellationAuthorisationResponse(CANCELLATION_AUTHORISATION_ID, authorisationStatus, null, null)));

        // When
        ResponseObject<CancellationAuthorisationResponse> actual = paymentCancellationAuthorisationService.createPisCancellationAuthorisation(new Xs2aCreatePisAuthorisationRequest(PAYMENT_ID, PSU_ID_DATA, PAYMENT_PRODUCT, SINGLE, null));

        // Then
        assertFalse(actual.hasError());
        verify(loggingContextService).storeTransactionAndScaStatus(TRANSACTION_STATUS, authorisationStatus);
    }

    @Test
    void createPisCancellationAuthorisation_withNotExistingPaymentId_shouldReturnError() {
        // Given

        // When
        ResponseObject<CancellationAuthorisationResponse> pisCancellationAuthorisation =
            paymentCancellationAuthorisationService.createPisCancellationAuthorisation(new Xs2aCreatePisAuthorisationRequest(NOT_EXISTING_PAYMENT_ID, PSU_ID_DATA, PAYMENT_PRODUCT, PaymentType.SINGLE, null));

        // Then
        assertThat(pisCancellationAuthorisation.hasError()).isTrue();
        assertThat(pisCancellationAuthorisation.getError()).isEqualTo(UNKNOWN_PAYMENT_ERROR);

        verify(pisScaAuthorisationServiceResolver, never()).getService(anyString());
        verify(pisScaAuthorisationService, never()).createCommonPaymentCancellationAuthorisation(any(Xs2aCreateAuthorisationRequest.class), any(PaymentType.class));
    }

    @Test
    void createPisCancellationAuthorisation_withNotCancelledPayment_shouldReturnError() {
        // Given
        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID)).thenReturn(Optional.of(buildPisCommonPaymentResponseNotCancelled()));

        // When
        ResponseObject<CancellationAuthorisationResponse> pisCancellationAuthorisation =
            paymentCancellationAuthorisationService.createPisCancellationAuthorisation(new Xs2aCreatePisAuthorisationRequest(PAYMENT_ID, PSU_ID_DATA, PAYMENT_PRODUCT, PaymentType.SINGLE, null));

        // Then
        assertThat(pisCancellationAuthorisation.hasError()).isTrue();
        assertThat(pisCancellationAuthorisation.getError()).isEqualTo(INVALID_FLOW_ERROR);

        verify(pisScaAuthorisationServiceResolver, never()).getService(anyString());
        verify(pisScaAuthorisationService, never()).createCommonPaymentCancellationAuthorisation(any(Xs2aCreateAuthorisationRequest.class), any(PaymentType.class));
    }

    @Test
    void createPisCancellationAuthorisation_withUpdatePsuData_success() {
        // Given
        when(pisScaAuthorisationServiceResolver.getService())
            .thenReturn(pisScaAuthorisationService);
        when(pisScaAuthorisationServiceResolver.getService(CANCELLATION_AUTHORISATION_ID))
            .thenReturn(pisScaAuthorisationService);

        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.of(PIS_COMMON_PAYMENT_RESPONSE));

        when(createPisCancellationAuthorisationValidator.validate(any(CreatePisCancellationAuthorisationObject.class))).thenReturn(ValidationResult.valid());

        when(pisScaAuthorisationService.updateCommonPaymentCancellationPsuData(any()))
            .thenReturn(new Xs2aUpdatePisCommonPaymentPsuDataResponse(ScaStatus.RECEIVED, PAYMENT_ID, CANCELLATION_AUTHORISATION_ID, PSU_ID_DATA, null));

        ScaStatus scaStatus = ScaStatus.RECEIVED;

        when(pisScaAuthorisationService.createCommonPaymentCancellationAuthorisation(any(), any()))
            .thenReturn(Optional.of(new Xs2aCreatePisCancellationAuthorisationResponse(CANCELLATION_AUTHORISATION_ID, scaStatus, PaymentType.SINGLE, null)));
        when(updatePisCancellationPsuDataValidator.validate(any()))
            .thenReturn(ValidationResult.valid());
        CreatePaymentAuthorisationProcessorResponse response = new CreatePaymentAuthorisationProcessorResponse(SCA_STATUS, SCA_APPROACH, TEST_PSU_MESSAGE, TEST_TPP_MESSAGES, PAYMENT_ID, PSU_ID_DATA);
        when(authorisationChainResponsibilityService.apply(any())).thenReturn(response);

        // When
        ResponseObject<CancellationAuthorisationResponse> pisCancellationAuthorisation =
            paymentCancellationAuthorisationService.createPisCancellationAuthorisation(new Xs2aCreatePisAuthorisationRequest(PAYMENT_ID, PSU_ID_DATA, PAYMENT_PRODUCT, PaymentType.SINGLE, "123"));

        // Then
        assertThat(pisCancellationAuthorisation.hasError()).isFalse();

        CancellationAuthorisationResponse responseBody = pisCancellationAuthorisation.getBody();
        assertThat(responseBody.getAuthorisationResponseType()).isEqualTo(AuthorisationResponseType.UPDATE);
        assertThat(responseBody.getAuthorisationId()).isEqualTo(CANCELLATION_AUTHORISATION_ID);
        assertThat(responseBody.getScaStatus()).isEqualTo(scaStatus);

        Xs2aUpdatePisCommonPaymentPsuDataResponse concreteResponseBody = (Xs2aUpdatePisCommonPaymentPsuDataResponse) responseBody;
        assertThat(concreteResponseBody.getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(concreteResponseBody.getAuthorisationId()).isEqualTo(CANCELLATION_AUTHORISATION_ID);
        assertThat(concreteResponseBody.getScaStatus()).isEqualTo(scaStatus);
        assertThat(concreteResponseBody.getPsuData()).isEqualTo(PSU_ID_DATA);
    }

    @Test
    void createPisCancellationAuthorisation_withUpdatePsuDataAndNotExistingPaymentId_shouldReturnError() {
        // When
        ResponseObject<CancellationAuthorisationResponse> pisCancellationAuthorisation =
            paymentCancellationAuthorisationService.createPisCancellationAuthorisation(new Xs2aCreatePisAuthorisationRequest(NOT_EXISTING_PAYMENT_ID, PSU_ID_DATA, PAYMENT_PRODUCT, SINGLE, "123"));

        // Then
        assertThat(pisCancellationAuthorisation.hasError()).isTrue();
        assertThat(pisCancellationAuthorisation.getError()).isEqualTo(UNKNOWN_PAYMENT_ERROR);

        verify(pisScaAuthorisationServiceResolver, never()).getService(anyString());
        verify(pisScaAuthorisationService, never()).updateCommonPaymentCancellationPsuData(any(PaymentAuthorisationParameters.class));
    }

    @Test
    void createPisCancellationAuthorisation_withUpdatePsuDataAndUpdateValidationError_shouldReturnError() {
        // Given
        when(pisScaAuthorisationServiceResolver.getService())
            .thenReturn(pisScaAuthorisationService);

        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.of(PIS_COMMON_PAYMENT_RESPONSE));

        when(createPisCancellationAuthorisationValidator.validate(any(CreatePisCancellationAuthorisationObject.class))).thenReturn(ValidationResult.valid());

        when(updatePisCancellationPsuDataValidator.validate(any(UpdatePaymentPsuDataPO.class)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        CreatePaymentAuthorisationProcessorResponse response = new CreatePaymentAuthorisationProcessorResponse(SCA_STATUS, SCA_APPROACH, TEST_PSU_MESSAGE, TEST_TPP_MESSAGES, PAYMENT_ID, PSU_ID_DATA);
        when(authorisationChainResponsibilityService.apply(any())).thenReturn(response);
        when(pisScaAuthorisationService.createCommonPaymentCancellationAuthorisation(any(), any()))
            .thenReturn(Optional.of(new Xs2aCreatePisCancellationAuthorisationResponse(CANCELLATION_AUTHORISATION_ID, SCA_STATUS, PaymentType.SINGLE, null)));

        // When
        ResponseObject<CancellationAuthorisationResponse> pisCancellationAuthorisation =
            paymentCancellationAuthorisationService.createPisCancellationAuthorisation(new Xs2aCreatePisAuthorisationRequest(PAYMENT_ID, PSU_ID_DATA, PAYMENT_PRODUCT, SINGLE, "123"));

        // Then
        assertThat(pisCancellationAuthorisation.hasError()).isTrue();
        assertThat(pisCancellationAuthorisation.getError()).isEqualTo(VALIDATION_ERROR);

        verify(pisScaAuthorisationServiceResolver, never()).getService(anyString());
        verify(pisScaAuthorisationService, never()).updateCommonPaymentCancellationPsuData(any(PaymentAuthorisationParameters.class));
    }

    @Test
    void createPisCancellationAuthorisation_withUpdatePsuDataAndAuthorisationServiceError_shouldReturnError() {
        // Given
        when(pisScaAuthorisationServiceResolver.getService())
            .thenReturn(pisScaAuthorisationService);
        when(pisScaAuthorisationServiceResolver.getService(CANCELLATION_AUTHORISATION_ID))
            .thenReturn(pisScaAuthorisationService);

        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.of(PIS_COMMON_PAYMENT_RESPONSE));

        when(createPisCancellationAuthorisationValidator.validate(any(CreatePisCancellationAuthorisationObject.class))).thenReturn(ValidationResult.valid());

        when(pisScaAuthorisationService.updateCommonPaymentCancellationPsuData(any()))
            .thenReturn(new Xs2aUpdatePisCommonPaymentPsuDataResponse(ScaStatus.RECEIVED, PAYMENT_ID, CANCELLATION_AUTHORISATION_ID, PSU_ID_DATA, null));
        ErrorHolder errorHolder = ErrorHolder.builder(AUTHORISATION_SERVICE_ERROR.getErrorType())
                                      .tppMessages(AUTHORISATION_SERVICE_ERROR.getTppMessage())
                                      .build();
        when(pisScaAuthorisationService.updateCommonPaymentCancellationPsuData(any(PaymentAuthorisationParameters.class)))
            .thenReturn(new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, PAYMENT_ID, CANCELLATION_AUTHORISATION_ID, PSU_ID_DATA));
        when(updatePisCancellationPsuDataValidator.validate(any()))
            .thenReturn(ValidationResult.valid());

        CreatePaymentAuthorisationProcessorResponse response = new CreatePaymentAuthorisationProcessorResponse(SCA_STATUS, SCA_APPROACH, TEST_PSU_MESSAGE, TEST_TPP_MESSAGES, PAYMENT_ID, PSU_ID_DATA);
        when(authorisationChainResponsibilityService.apply(any())).thenReturn(response);
        when(pisScaAuthorisationService.createCommonPaymentCancellationAuthorisation(any(), any()))
            .thenReturn(Optional.of(new Xs2aCreatePisCancellationAuthorisationResponse(CANCELLATION_AUTHORISATION_ID, SCA_STATUS, PaymentType.SINGLE, null)));

        // When
        ResponseObject<CancellationAuthorisationResponse> pisCancellationAuthorisation =
            paymentCancellationAuthorisationService.createPisCancellationAuthorisation(new Xs2aCreatePisAuthorisationRequest(PAYMENT_ID, PSU_ID_DATA, PAYMENT_PRODUCT, SINGLE, "123"));

        // Then
        assertThat(pisCancellationAuthorisation.hasError()).isTrue();
        assertThat(pisCancellationAuthorisation.getError()).isEqualTo(AUTHORISATION_SERVICE_ERROR);
    }

    @Test
    void updatePisCancellationPsuData_Success_ShouldRecordEvent() {
        when(pisScaAuthorisationServiceResolver.getService(CANCELLATION_AUTHORISATION_ID))
            .thenReturn(pisScaAuthorisationService);

        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.of(PIS_COMMON_PAYMENT_RESPONSE));

        when(updatePisCancellationPsuDataValidator.validate(new UpdatePaymentPsuDataPO(buildPisCommonPaymentResponse(), buildXs2aUpdatePisPsuDataRequest())))
            .thenReturn(ValidationResult.valid());

        when(pisScaAuthorisationService.updateCommonPaymentCancellationPsuData(any()))
            .thenReturn(new Xs2aUpdatePisCommonPaymentPsuDataResponse(ScaStatus.RECEIVED, PAYMENT_ID, CANCELLATION_AUTHORISATION_ID, PSU_ID_DATA, null));

        // Given:
        PaymentAuthorisationParameters request = buildXs2aUpdatePisPsuDataRequest();
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);
        when(eventTypeService.getEventType(request, EventAuthorisationType.PIS_CANCELLATION))
            .thenReturn(EventType.UPDATE_PAYMENT_CANCELLATION_PSU_DATA_IDENTIFICATION_REQUEST_RECEIVED);

        // When
        paymentCancellationAuthorisationService.updatePisCancellationPsuData(request);

        // Then
        verify(xs2aEventService, times(1)).recordPisTppRequest(eq(PAYMENT_ID), argumentCaptor.capture(), any());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.UPDATE_PAYMENT_CANCELLATION_PSU_DATA_IDENTIFICATION_REQUEST_RECEIVED);
    }

    @Test
    void updatePisCancellationPsuData_shouldStoreStatusesInLoggingContext() {
        when(pisScaAuthorisationServiceResolver.getService(CANCELLATION_AUTHORISATION_ID))
            .thenReturn(pisScaAuthorisationService);

        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.of(PIS_COMMON_PAYMENT_RESPONSE));

        when(updatePisCancellationPsuDataValidator.validate(new UpdatePaymentPsuDataPO(buildPisCommonPaymentResponse(), buildXs2aUpdatePisPsuDataRequest())))
            .thenReturn(ValidationResult.valid());

        when(pisScaAuthorisationService.updateCommonPaymentCancellationPsuData(any()))
            .thenReturn(new Xs2aUpdatePisCommonPaymentPsuDataResponse(SCA_STATUS, PAYMENT_ID, CANCELLATION_AUTHORISATION_ID, PSU_ID_DATA, null));

        // Given:
        PaymentAuthorisationParameters request = buildXs2aUpdatePisPsuDataRequest();

        // When
        ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> response = paymentCancellationAuthorisationService.updatePisCancellationPsuData(request);

        // Then
        assertFalse(response.hasError());

        InOrder inOrder = inOrder(loggingContextService, pisScaAuthorisationService);
        inOrder.verify(loggingContextService).storeTransactionStatus(TRANSACTION_STATUS);
        inOrder.verify(pisScaAuthorisationService).updateCommonPaymentCancellationPsuData(request);
        inOrder.verify(loggingContextService).storeScaStatus(SCA_STATUS);
    }

    @Test
    void updatePisCancellationPsuData_withInvalidPayment_shouldReturnValidationError() {
        // Given
        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(WRONG_PAYMENT_ID))
            .thenReturn(Optional.of(INVALID_PIS_COMMON_PAYMENT_RESPONSE));

        PaymentAuthorisationParameters invalidUpdatePisPsuDataRequest = buildInvalidXs2aUpdatePisPsuDataRequest();
        when(updatePisCancellationPsuDataValidator.validate(new UpdatePaymentPsuDataPO(INVALID_PIS_COMMON_PAYMENT_RESPONSE, buildInvalidXs2aUpdatePisPsuDataRequest())))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> actualResponse =
            paymentCancellationAuthorisationService.updatePisCancellationPsuData(invalidUpdatePisPsuDataRequest);

        // Then
        verify(updatePisCancellationPsuDataValidator).validate(new UpdatePaymentPsuDataPO(INVALID_PIS_COMMON_PAYMENT_RESPONSE, buildInvalidXs2aUpdatePisPsuDataRequest()));
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError()).isEqualTo(VALIDATION_ERROR);
    }

    @Test
    void updatePisCancellationPsuData_shouldStorePaymentTransactionStatusInLoggingContextWhenUpdatePaymentCancellationRequestInvalid() {
        // Given
        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.of(PIS_COMMON_PAYMENT_RESPONSE));

        when(updatePisCancellationPsuDataValidator.validate(new UpdatePaymentPsuDataPO(buildPisCommonPaymentResponse(), buildXs2aUpdatePisPsuDataRequest())))
            .thenReturn(ValidationResult.valid());

        PaymentAuthorisationParameters updatePisPsuDataRequest = buildXs2aUpdatePisPsuDataRequest();
        when(updatePisCancellationPsuDataValidator.validate(new UpdatePaymentPsuDataPO(PIS_COMMON_PAYMENT_RESPONSE, updatePisPsuDataRequest)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ArgumentCaptor<TransactionStatus> transactionStatusArgumentCaptor = ArgumentCaptor.forClass(TransactionStatus.class);
        ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> actualResponse =
            paymentCancellationAuthorisationService.updatePisCancellationPsuData(updatePisPsuDataRequest);

        // Then
        assertTrue(actualResponse.hasError());
        verify(loggingContextService).storeTransactionStatus(transactionStatusArgumentCaptor.capture());
        assertThat(transactionStatusArgumentCaptor.getValue()).isEqualTo(TransactionStatus.RCVD);
    }

    @Test
    void updatePisCancellationPsuData_shouldStoreAuthorisationScaStatusInLoggingContextWhenUpdatePaymentCancellationRequestInvalid() {
        // Given:
        when(pisScaAuthorisationServiceResolver.getService(CANCELLATION_AUTHORISATION_ID))
            .thenReturn(pisScaAuthorisationService);

        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.of(PIS_COMMON_PAYMENT_RESPONSE));

        when(updatePisCancellationPsuDataValidator.validate(new UpdatePaymentPsuDataPO(buildPisCommonPaymentResponse(), buildXs2aUpdatePisPsuDataRequest())))
            .thenReturn(ValidationResult.valid());

        PaymentAuthorisationParameters request = buildXs2aUpdatePisPsuDataRequest();
        ErrorHolder errorHolder = ErrorHolder.builder(AUTHORISATION_SERVICE_ERROR.getErrorType())
                                      .tppMessages(AUTHORISATION_SERVICE_ERROR.getTppMessage())
                                      .build();
        when(pisScaAuthorisationService.updateCommonPaymentCancellationPsuData(any(PaymentAuthorisationParameters.class)))
            .thenReturn(new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, PAYMENT_ID, CANCELLATION_AUTHORISATION_ID, PSU_ID_DATA));
        ArgumentCaptor<ScaStatus> scaStatusArgumentCaptor = ArgumentCaptor.forClass(ScaStatus.class);

        // When
        ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> response = paymentCancellationAuthorisationService.updatePisCancellationPsuData(request);

        // Then
        assertTrue(response.hasError());
        verify(loggingContextService).storeScaStatus(scaStatusArgumentCaptor.capture());
        assertThat(scaStatusArgumentCaptor.getValue()).isEqualTo(ScaStatus.FAILED);
    }

    @Test
    void getPaymentInitiationCancellationAuthorisationInformation_Success_ShouldRecordEvent() {
        // Given:
        when(pisScaAuthorisationServiceResolver.getService())
            .thenReturn(pisScaAuthorisationService);

        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.of(PIS_COMMON_PAYMENT_RESPONSE));

        when(getPaymentCancellationAuthorisationsValidator.validate(new CommonPaymentObject(buildPisCommonPaymentResponse(), SINGLE, PAYMENT_PRODUCT)))
            .thenReturn(ValidationResult.valid());

        when(pisScaAuthorisationService.getCancellationAuthorisationSubResources(anyString()))
            .thenReturn(Optional.of(new Xs2aPaymentCancellationAuthorisationSubResource(Collections.emptyList())));
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        paymentCancellationAuthorisationService.getPaymentInitiationCancellationAuthorisationInformation(PAYMENT_ID, SINGLE, PAYMENT_PRODUCT);

        // Then
        verify(xs2aEventService, times(1)).recordPisTppRequest(eq(PAYMENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.GET_PAYMENT_CANCELLATION_AUTHORISATION_REQUEST_RECEIVED);
    }

    @Test
    void getPaymentInitiationCancellationAuthorisationInformation_shouldStoreStatusesInLoggingContext() {
        // Given:
        when(pisScaAuthorisationServiceResolver.getService())
            .thenReturn(pisScaAuthorisationService);

        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.of(PIS_COMMON_PAYMENT_RESPONSE));

        when(getPaymentCancellationAuthorisationsValidator.validate(new CommonPaymentObject(buildPisCommonPaymentResponse(), SINGLE, PAYMENT_PRODUCT)))
            .thenReturn(ValidationResult.valid());

        when(pisScaAuthorisationService.getCancellationAuthorisationSubResources(anyString()))
            .thenReturn(Optional.of(new Xs2aPaymentCancellationAuthorisationSubResource(Collections.emptyList())));

        // When
        ResponseObject<Xs2aPaymentCancellationAuthorisationSubResource> response = paymentCancellationAuthorisationService.getPaymentInitiationCancellationAuthorisationInformation(PAYMENT_ID, SINGLE, PAYMENT_PRODUCT);

        // Then
        assertFalse(response.hasError());
        verify(loggingContextService).storeTransactionStatus(TRANSACTION_STATUS);
    }

    @Test
    void getPaymentInitiationCancellationAuthorisationInformation_withInvalidPayment_shouldReturnValidationError() {
        // Given:
        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(WRONG_PAYMENT_ID))
            .thenReturn(Optional.of(INVALID_PIS_COMMON_PAYMENT_RESPONSE));

        when(getPaymentCancellationAuthorisationsValidator.validate(new CommonPaymentObject(INVALID_PIS_COMMON_PAYMENT_RESPONSE, SINGLE, PAYMENT_PRODUCT)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<Xs2aPaymentCancellationAuthorisationSubResource> actualResponse =
            paymentCancellationAuthorisationService.getPaymentInitiationCancellationAuthorisationInformation(WRONG_PAYMENT_ID, SINGLE, PAYMENT_PRODUCT);

        // Then
        verify(getPaymentCancellationAuthorisationsValidator).validate(new CommonPaymentObject(INVALID_PIS_COMMON_PAYMENT_RESPONSE, SINGLE, PAYMENT_PRODUCT));
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError()).isEqualTo(VALIDATION_ERROR);
    }

    @Test
    void getPaymentCancellationAuthorisationScaStatus_success() {
        // Given
        PisCommonPaymentResponse pisCommonPaymentResponse = buildPisCommonPaymentResponse();
        PaymentScaStatus paymentScaStatus = new PaymentScaStatus(PSU_ID_DATA, pisCommonPaymentResponse, ScaStatus.RECEIVED);

        when(pisScaAuthorisationService.getCancellationAuthorisationScaStatus(PAYMENT_ID, CANCELLATION_AUTHORISATION_ID))
            .thenReturn(Optional.of(SCA_STATUS));
        when(pisScaAuthorisationServiceResolver.getService(CANCELLATION_AUTHORISATION_ID))
            .thenReturn(pisScaAuthorisationService);

        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.of(PIS_COMMON_PAYMENT_RESPONSE));

        when(getPaymentCancellationAuthorisationScaStatusValidator.validate(new GetPaymentCancellationAuthorisationScaStatusPO(pisCommonPaymentResponse, CANCELLATION_AUTHORISATION_ID, SINGLE, PAYMENT_PRODUCT)))
            .thenReturn(ValidationResult.valid());

        when(pisScaAuthorisationServiceResolver.getService(CANCELLATION_AUTHORISATION_ID))
            .thenReturn(pisScaAuthorisationService);

        when(psuIdDataAuthorisationService.getPsuIdData(CANCELLATION_AUTHORISATION_ID, Collections.singletonList(PSU_ID_DATA))).thenReturn(PSU_ID_DATA);

        // When
        ResponseObject<PaymentScaStatus> actual =
            paymentCancellationAuthorisationService.getPaymentCancellationAuthorisationScaStatus(PAYMENT_ID,
                                                                                                 CANCELLATION_AUTHORISATION_ID, SINGLE, PAYMENT_PRODUCT);

        // Then
        assertFalse(actual.hasError());
        assertEquals(paymentScaStatus, actual.getBody());
    }

    @Test
    void getPaymentCancellationAuthorisationScaStatus_success_shouldRecordEvent() {
        // Given:
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);
        when(pisScaAuthorisationService.getCancellationAuthorisationScaStatus(PAYMENT_ID, CANCELLATION_AUTHORISATION_ID))
            .thenReturn(Optional.of(SCA_STATUS));
        when(pisScaAuthorisationServiceResolver.getService(CANCELLATION_AUTHORISATION_ID))
            .thenReturn(pisScaAuthorisationService);

        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.of(PIS_COMMON_PAYMENT_RESPONSE));

        when(getPaymentCancellationAuthorisationScaStatusValidator.validate(new GetPaymentCancellationAuthorisationScaStatusPO(buildPisCommonPaymentResponse(), CANCELLATION_AUTHORISATION_ID, SINGLE, PAYMENT_PRODUCT)))
            .thenReturn(ValidationResult.valid());

        when(pisScaAuthorisationServiceResolver.getService(CANCELLATION_AUTHORISATION_ID))
            .thenReturn(pisScaAuthorisationService);

        // When
        paymentCancellationAuthorisationService.getPaymentCancellationAuthorisationScaStatus(PAYMENT_ID,
                                                                                             CANCELLATION_AUTHORISATION_ID, SINGLE, PAYMENT_PRODUCT);

        // Then
        verify(xs2aEventService, times(1))
            .recordPisTppRequest(eq(PAYMENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.GET_PAYMENT_CANCELLATION_SCA_STATUS_REQUEST_RECEIVED);
    }

    @Test
    void getPaymentCancellationAuthorisationScaStatus_shouldStoreStatusesInLoggingContext() {
        // Given:
        when(pisScaAuthorisationService.getCancellationAuthorisationScaStatus(PAYMENT_ID, CANCELLATION_AUTHORISATION_ID))
            .thenReturn(Optional.of(SCA_STATUS));
        when(pisScaAuthorisationServiceResolver.getService(CANCELLATION_AUTHORISATION_ID))
            .thenReturn(pisScaAuthorisationService);

        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.of(PIS_COMMON_PAYMENT_RESPONSE));

        when(getPaymentCancellationAuthorisationScaStatusValidator.validate(new GetPaymentCancellationAuthorisationScaStatusPO(buildPisCommonPaymentResponse(), CANCELLATION_AUTHORISATION_ID, SINGLE, PAYMENT_PRODUCT)))
            .thenReturn(ValidationResult.valid());

        when(pisScaAuthorisationServiceResolver.getService(CANCELLATION_AUTHORISATION_ID))
            .thenReturn(pisScaAuthorisationService);

        // When
        ResponseObject<PaymentScaStatus> response = paymentCancellationAuthorisationService.getPaymentCancellationAuthorisationScaStatus(PAYMENT_ID, CANCELLATION_AUTHORISATION_ID, SINGLE, PAYMENT_PRODUCT);

        // Then
        assertFalse(response.hasError());
        verify(loggingContextService).storeTransactionAndScaStatus(TRANSACTION_STATUS, SCA_STATUS);
    }

    @Test
    void getPaymentCancellationAuthorisationScaStatus_failure_status() {
        when(pisScaAuthorisationService.getCancellationAuthorisationScaStatus(PAYMENT_ID, CANCELLATION_AUTHORISATION_ID))
            .thenReturn(Optional.of(SCA_STATUS));
        when(pisScaAuthorisationServiceResolver.getService(CANCELLATION_AUTHORISATION_ID))
            .thenReturn(pisScaAuthorisationService);

        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.of(PIS_COMMON_PAYMENT_RESPONSE));

        when(getPaymentCancellationAuthorisationScaStatusValidator.validate(new GetPaymentCancellationAuthorisationScaStatusPO(buildPisCommonPaymentResponse(), CANCELLATION_AUTHORISATION_ID, SINGLE, PAYMENT_PRODUCT)))
            .thenReturn(ValidationResult.valid());

        when(getPaymentCancellationAuthorisationScaStatusValidator.validate(new GetPaymentCancellationAuthorisationScaStatusPO(buildPisCommonPaymentResponse(), CANCELLATION_AUTHORISATION_ID, SINGLE, PAYMENT_PRODUCT)))
            .thenReturn(ValidationResult.valid());
        when(pisScaAuthorisationServiceResolver.getService(CANCELLATION_AUTHORISATION_ID).getCancellationAuthorisationScaStatus(PAYMENT_ID, CANCELLATION_AUTHORISATION_ID))
            .thenReturn(Optional.empty());

        ResponseObject<PaymentScaStatus> actual = paymentCancellationAuthorisationService.getPaymentCancellationAuthorisationScaStatus(PAYMENT_ID, CANCELLATION_AUTHORISATION_ID, SINGLE, PAYMENT_PRODUCT);

        assertTrue(actual.hasError());
        assertEquals(SCA_STATUS_ERROR, actual.getError());
        assertNull(actual.getBody());
    }

    @Test
    void getPaymentCancellationAuthorisationScaStatus_withInvalidPayment_shouldReturnValidationError() {
        // Given
        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(WRONG_PAYMENT_ID))
            .thenReturn(Optional.of(INVALID_PIS_COMMON_PAYMENT_RESPONSE));

        when(getPaymentCancellationAuthorisationScaStatusValidator.validate(new GetPaymentCancellationAuthorisationScaStatusPO(INVALID_PIS_COMMON_PAYMENT_RESPONSE, WRONG_CANCELLATION_AUTHORISATION_ID, SINGLE, PAYMENT_PRODUCT)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<PaymentScaStatus> actualResponse =
            paymentCancellationAuthorisationService.getPaymentCancellationAuthorisationScaStatus(WRONG_PAYMENT_ID,
                                                                                                 WRONG_CANCELLATION_AUTHORISATION_ID, SINGLE, PAYMENT_PRODUCT);

        // Then
        verify(getPaymentCancellationAuthorisationScaStatusValidator).validate(new GetPaymentCancellationAuthorisationScaStatusPO(INVALID_PIS_COMMON_PAYMENT_RESPONSE, WRONG_CANCELLATION_AUTHORISATION_ID, SINGLE, PAYMENT_PRODUCT));
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError()).isEqualTo(VALIDATION_ERROR);
    }

    private static PisCommonPaymentResponse buildPisCommonPaymentResponse() {
        PisCommonPaymentResponse response = new PisCommonPaymentResponse();
        response.setPsuData(Collections.singletonList(PSU_ID_DATA));
        response.setTransactionStatus(TRANSACTION_STATUS);
        response.setPaymentProduct(PAYMENT_PRODUCT);
        response.setPaymentType(SINGLE);
        response.setInternalPaymentStatus(InternalPaymentStatus.CANCELLED_INITIATED);
        return response;
    }

    private static PisCommonPaymentResponse buildPisCommonPaymentResponseNotCancelled() {
        PisCommonPaymentResponse response = new PisCommonPaymentResponse();
        response.setPsuData(Collections.singletonList(PSU_ID_DATA));
        response.setTransactionStatus(TRANSACTION_STATUS);
        response.setPaymentProduct(PAYMENT_PRODUCT);
        response.setPaymentType(SINGLE);
        response.setInternalPaymentStatus(InternalPaymentStatus.INITIATED);
        return response;
    }

    private static PisCommonPaymentResponse buildInvalidPisCommonPaymentResponse() {
        PisCommonPaymentResponse response = new PisCommonPaymentResponse();
        response.setTppInfo(new TppInfo());
        response.setPaymentProduct(PAYMENT_PRODUCT);
        response.setPaymentType(SINGLE);
        return response;
    }

    private PaymentAuthorisationParameters buildXs2aUpdatePisPsuDataRequest() {
        PaymentAuthorisationParameters request = new PaymentAuthorisationParameters();
        request.setAuthorisationId(CANCELLATION_AUTHORISATION_ID);
        request.setPaymentId(PAYMENT_ID);
        request.setPsuData(PSU_ID_DATA);
        return request;
    }

    private PaymentAuthorisationParameters buildInvalidXs2aUpdatePisPsuDataRequest() {
        PaymentAuthorisationParameters request = new PaymentAuthorisationParameters();
        request.setAuthorisationId(INVALID_AUTHORISATION_ID);
        request.setPaymentId(WRONG_PAYMENT_ID);
        request.setPsuData(PSU_ID_DATA);
        return request;
    }
}
