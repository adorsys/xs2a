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
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.domain.consent.pis.PaymentAuthorisationParameters;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationChainResponsibilityService;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationServiceResolver;
import de.adorsys.psd2.xs2a.service.consent.PisPsuDataService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisCommonPaymentService;
import de.adorsys.psd2.xs2a.service.event.EventAuthorisationType;
import de.adorsys.psd2.xs2a.service.event.EventTypeService;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.validator.pis.CommonPaymentObject;
import de.adorsys.psd2.xs2a.service.validator.pis.authorisation.initiation.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.core.error.ErrorType.*;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.core.profile.PaymentType.SINGLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentAuthorisationServiceTest {
    private static final String CORRECT_PSU_ID = "marion.mueller";
    private static final String PAYMENT_ID = "594ef79c-d785-41ec-9b14-2ea3a7ae2c7b";
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String AUTHORISATION_ID = "a8fc1f02-3639-4528-bd19-3eacf1c67038";
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(CORRECT_PSU_ID, null, null, null, null);
    private static final PsuIdData PSU_ID_DATA_EMPTY = new PsuIdData(null, null, null, null, null);
    private static final String WRONG_AUTHORISATION_ID = "wrong authorisation id";
    private static final String WRONG_PAYMENT_ID = "wrong payment id";
    private static final TransactionStatus TRANSACTION_STATUS = TransactionStatus.ACCP;
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    private final Xs2aCreatePisAuthorisationRequest CREATE_AUTHORISATION_REQUEST = new Xs2aCreatePisAuthorisationRequest(PAYMENT_ID, PSU_ID_DATA, PAYMENT_PRODUCT, SINGLE, "");
    private final Xs2aCreatePisAuthorisationRequest CREATE_AUTHORISATION_REQUEST_WITH_PASSWORD = new Xs2aCreatePisAuthorisationRequest(PAYMENT_ID, PSU_ID_DATA, PAYMENT_PRODUCT, SINGLE, "123");
    private final Xs2aCreatePisAuthorisationRequest CREATE_AUTHORISATION_REQUEST_NO_PSU_ID = new Xs2aCreatePisAuthorisationRequest(PAYMENT_ID, PSU_ID_DATA_EMPTY, PAYMENT_PRODUCT, SINGLE, "");

    private static final MessageError VALIDATION_ERROR = new MessageError(PIS_401, TppMessageInformation.of(UNAUTHORIZED));
    private static final MessageError RESOURCE_UNKNOWN_ERROR = new MessageError(PIS_404, of(RESOURCE_UNKNOWN_404_NO_PAYMENT));
    private static final MessageError PAYMENT_FAILED_ERROR = new MessageError(PIS_400, of(PAYMENT_FAILED));
    private static final MessageError PSU_CREDENTIALS_INVALID_ERROR = new MessageError(PIS_401, of(PSU_CREDENTIALS_INVALID));
    private static final ScaApproach SCA_APPROACH = ScaApproach.EMBEDDED;
    private static final Set<TppMessageInformation> TEST_TPP_MESSAGES = Collections.singleton(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR));
    private static final String TEST_PSU_MESSAGE = "This test message is created in ASPSP and directed to PSU";

    @InjectMocks
    private PaymentAuthorisationServiceImpl paymentAuthorisationService;

    @Mock
    private Xs2aEventService xs2aEventService;
    @Mock
    private PisScaAuthorisationServiceResolver pisScaAuthorisationServiceResolver;
    @Mock
    private PisScaAuthorisationService pisScaAuthorisationService;
    @Mock
    private Xs2aAuthorisationService authorisationService;
    @Mock
    private Xs2aPisCommonPaymentService pisCommonPaymentService;
    @Mock
    private CreatePisAuthorisationValidator createPisAuthorisationValidator;
    @Mock
    private UpdatePisCommonPaymentPsuDataValidator updatePisCommonPaymentPsuDataValidator;
    @Mock
    private GetPaymentInitiationAuthorisationsValidator getPaymentInitiationAuthorisationsValidator;
    @Mock
    private GetPaymentInitiationAuthorisationScaStatusValidator getPaymentInitiationAuthorisationScaStatusValidator;
    @Mock
    private PisPsuDataService pisPsuDataService;
    @Mock
    private LoggingContextService loggingContextService;
    @Mock
    private PsuIdDataAuthorisationService psuIdDataAuthorisationService;
    @Mock
    private EventTypeService eventTypeService;
    @Mock
    private ScaApproachResolver scaApproachResolver;
    @Mock
    private AuthorisationChainResponsibilityService authorisationChainResponsibilityService;


    @Test
    void createPisAuthorization_Success_ShouldRecordEvent() {
        // Given
        when(pisScaAuthorisationServiceResolver.getService())
            .thenReturn(pisScaAuthorisationService);
        when(createPisAuthorisationValidator.validate(new CreatePisAuthorisationObject(buildPisCommonPaymentResponse(), SINGLE, PAYMENT_PRODUCT, PSU_ID_DATA)))
            .thenReturn(ValidationResult.valid());
        when(pisScaAuthorisationService.createCommonPaymentAuthorisation(any(), eq(SINGLE)))
            .thenReturn(Optional.of(new Xs2aCreatePisAuthorisationResponse(null, null, null, null, null, PSU_ID_DATA, null, SCA_APPROACH)));

        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse();

        when(pisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.of(commonPaymentResponse));
        CreatePaymentAuthorisationProcessorResponse response = new CreatePaymentAuthorisationProcessorResponse(SCA_STATUS, SCA_APPROACH, TEST_PSU_MESSAGE, TEST_TPP_MESSAGES, PAYMENT_ID, PSU_ID_DATA);
        when(authorisationChainResponsibilityService.apply(any())).thenReturn(response);
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        paymentAuthorisationService.createPisAuthorisation(CREATE_AUTHORISATION_REQUEST);

        // Then
        verify(xs2aEventService, times(1)).recordPisTppRequest(eq(PAYMENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.START_PAYMENT_AUTHORISATION_REQUEST_RECEIVED);
    }

    @Test
    void createPisAuthorization_Success_WithNoPsuId() {
        // Given
        when(pisScaAuthorisationServiceResolver.getService())
            .thenReturn(pisScaAuthorisationService);

        when(pisScaAuthorisationService.createCommonPaymentAuthorisation(any(), eq(SINGLE)))
            .thenReturn(Optional.of(new Xs2aCreatePisAuthorisationResponse(null, null, null, null, null, PSU_ID_DATA, null, SCA_APPROACH)));

        when(createPisAuthorisationValidator.validate(new CreatePisAuthorisationObject(buildPisCommonPaymentResponse(), SINGLE, PAYMENT_PRODUCT, PSU_ID_DATA_EMPTY)))
            .thenReturn(ValidationResult.valid());

        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse();

        when(pisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.of(commonPaymentResponse));

        when(pisPsuDataService.getPsuDataByPaymentId(PAYMENT_ID)).thenReturn(Collections.singletonList(PSU_ID_DATA));
        CreatePaymentAuthorisationProcessorResponse response = new CreatePaymentAuthorisationProcessorResponse(SCA_STATUS, SCA_APPROACH, TEST_PSU_MESSAGE, TEST_TPP_MESSAGES, PAYMENT_ID, PSU_ID_DATA);
        when(authorisationChainResponsibilityService.apply(any())).thenReturn(response);
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        paymentAuthorisationService.createPisAuthorisation(CREATE_AUTHORISATION_REQUEST_NO_PSU_ID);

        // Then
        verify(xs2aEventService, times(1)).recordPisTppRequest(eq(PAYMENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.START_PAYMENT_AUTHORISATION_REQUEST_RECEIVED);
    }

    @Test
    void createPisAuthorization_ResourceUnknownError() {
        // Given
        when(pisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.empty());

        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        ResponseObject actual = paymentAuthorisationService.createPisAuthorisation(CREATE_AUTHORISATION_REQUEST_NO_PSU_ID);

        // Then
        verify(xs2aEventService, times(1)).recordPisTppRequest(eq(PAYMENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.START_PAYMENT_AUTHORISATION_REQUEST_RECEIVED);
        assertThat(actual.getError()).isEqualTo(RESOURCE_UNKNOWN_ERROR);
    }

    @Test
    void createPisAuthorization_PaymentFailedError() {
        // Given
        when(pisScaAuthorisationServiceResolver.getService())
            .thenReturn(pisScaAuthorisationService);

        when(pisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.of(buildPisCommonPaymentResponse()));

        when(pisPsuDataService.getPsuDataByPaymentId(PAYMENT_ID)).thenReturn(Collections.emptyList());
        when(createPisAuthorisationValidator.validate(new CreatePisAuthorisationObject(buildPisCommonPaymentResponse(), SINGLE, PAYMENT_PRODUCT, PSU_ID_DATA_EMPTY)))
            .thenReturn(ValidationResult.valid());
        CreatePaymentAuthorisationProcessorResponse response = new CreatePaymentAuthorisationProcessorResponse(SCA_STATUS, SCA_APPROACH, TEST_PSU_MESSAGE, TEST_TPP_MESSAGES, PAYMENT_ID, PSU_ID_DATA);
        when(authorisationChainResponsibilityService.apply(any())).thenReturn(response);
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        ResponseObject actual = paymentAuthorisationService.createPisAuthorisation(CREATE_AUTHORISATION_REQUEST_NO_PSU_ID);

        // Then
        verify(xs2aEventService, times(1)).recordPisTppRequest(eq(PAYMENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.START_PAYMENT_AUTHORISATION_REQUEST_RECEIVED);
        assertThat(actual.getError()).isEqualTo(PAYMENT_FAILED_ERROR);
    }


    @Test
    void createPisAuthorization_UpdatePsuDataError() {
        // Given
        when(pisScaAuthorisationServiceResolver.getService())
            .thenReturn(pisScaAuthorisationService);
        when(createPisAuthorisationValidator.validate(new CreatePisAuthorisationObject(buildPisCommonPaymentResponse(), SINGLE, PAYMENT_PRODUCT, PSU_ID_DATA)))
            .thenReturn(ValidationResult.valid());

        when(pisScaAuthorisationService.createCommonPaymentAuthorisation(any(), eq(SINGLE)))
            .thenReturn(Optional.of(new Xs2aCreatePisAuthorisationResponse(null, null, null, null, null, PSU_ID_DATA, null, SCA_APPROACH)));

        when(pisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.of(buildPisCommonPaymentResponse()));
        CreatePaymentAuthorisationProcessorResponse response = new CreatePaymentAuthorisationProcessorResponse(SCA_STATUS, SCA_APPROACH, TEST_PSU_MESSAGE, TEST_TPP_MESSAGES, PAYMENT_ID, PSU_ID_DATA);
        when(authorisationChainResponsibilityService.apply(any())).thenReturn(response);
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        when(updatePisCommonPaymentPsuDataValidator.validate(any(UpdatePaymentPsuDataPO.class)))
            .thenReturn(ValidationResult.invalid(PSU_CREDENTIALS_INVALID_ERROR));

        // When
        ResponseObject actual = paymentAuthorisationService.createPisAuthorisation(CREATE_AUTHORISATION_REQUEST_WITH_PASSWORD);

        // Then
        verify(xs2aEventService, times(1)).recordPisTppRequest(eq(PAYMENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.START_PAYMENT_AUTHORISATION_REQUEST_RECEIVED);
        assertThat(actual.getError()).isEqualTo(PSU_CREDENTIALS_INVALID_ERROR);
    }

    @Test
    void createPisAuthorization_Success() {
        // Given
        when(pisScaAuthorisationServiceResolver.getService())
            .thenReturn(pisScaAuthorisationService);
        when(pisScaAuthorisationServiceResolver.getService(AUTHORISATION_ID))
            .thenReturn(pisScaAuthorisationService);
        when(createPisAuthorisationValidator.validate(new CreatePisAuthorisationObject(buildPisCommonPaymentResponse(), SINGLE, PAYMENT_PRODUCT, PSU_ID_DATA)))
            .thenReturn(ValidationResult.valid());

        when(pisScaAuthorisationServiceResolver.getService(AUTHORISATION_ID)).thenReturn(pisScaAuthorisationService);

        Xs2aUpdatePisCommonPaymentPsuDataResponse expected = new Xs2aUpdatePisCommonPaymentPsuDataResponse();

        when(pisScaAuthorisationService.createCommonPaymentAuthorisation(any(), eq(SINGLE)))
            .thenReturn(Optional.of(new Xs2aCreatePisAuthorisationResponse(AUTHORISATION_ID, null, null, null, null, PSU_ID_DATA, null, SCA_APPROACH)));

        when(pisScaAuthorisationService.updateCommonPaymentPsuData(buildXs2aUpdatePisPsuDataRequestWithFullData()))
            .thenReturn(expected);

        when(pisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.of(buildPisCommonPaymentResponse()));
        CreatePaymentAuthorisationProcessorResponse response = new CreatePaymentAuthorisationProcessorResponse(SCA_STATUS, SCA_APPROACH, TEST_PSU_MESSAGE, TEST_TPP_MESSAGES, PAYMENT_ID, PSU_ID_DATA);
        when(authorisationChainResponsibilityService.apply(any())).thenReturn(response);
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        when(updatePisCommonPaymentPsuDataValidator.validate(any(UpdatePaymentPsuDataPO.class)))
            .thenReturn(ValidationResult.valid());

        // When
        ResponseObject actual = paymentAuthorisationService.createPisAuthorisation(CREATE_AUTHORISATION_REQUEST_WITH_PASSWORD);

        // Then
        verify(xs2aEventService, times(1)).recordPisTppRequest(eq(PAYMENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.START_PAYMENT_AUTHORISATION_REQUEST_RECEIVED);
        assertThat(expected).isEqualTo(actual.getBody());
    }

    @Test
    void createPisAuthorisation_shouldStoreStatusesInLoggingContext() {
        // Given
        ScaStatus authorisationStatus = ScaStatus.PSUIDENTIFIED;

        when(pisScaAuthorisationServiceResolver.getService())
            .thenReturn(pisScaAuthorisationService);
        when(createPisAuthorisationValidator.validate(new CreatePisAuthorisationObject(buildPisCommonPaymentResponse(), SINGLE, PAYMENT_PRODUCT, PSU_ID_DATA)))
            .thenReturn(ValidationResult.valid());
        CreatePaymentAuthorisationProcessorResponse response = new CreatePaymentAuthorisationProcessorResponse(SCA_STATUS, SCA_APPROACH, TEST_PSU_MESSAGE, TEST_TPP_MESSAGES, PAYMENT_ID, PSU_ID_DATA);
        when(authorisationChainResponsibilityService.apply(any())).thenReturn(response);
        when(pisScaAuthorisationService.createCommonPaymentAuthorisation(any(), eq(SINGLE)))
            .thenReturn(Optional.of(new Xs2aCreatePisAuthorisationResponse(null, authorisationStatus, null, null, null, PSU_ID_DATA, null, SCA_APPROACH)));

        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse();

        when(pisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.of(commonPaymentResponse));

        // When
        ResponseObject<AuthorisationResponse> actual = paymentAuthorisationService.createPisAuthorisation(CREATE_AUTHORISATION_REQUEST);

        // Then
        assertFalse(actual.hasError());
        verify(loggingContextService).storeTransactionAndScaStatus(TRANSACTION_STATUS, authorisationStatus);
    }

    @Test
    void createPisAuthorization_withInvalidPayment_shouldReturnValidationError() {
        // Given
        PisCommonPaymentResponse invalidPisCommonPaymentResponse = buildInvalidPisCommonPaymentResponse();

        when(pisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.of(invalidPisCommonPaymentResponse));

        when(createPisAuthorisationValidator.validate(any(CreatePisAuthorisationObject.class)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<AuthorisationResponse> actualResponse = paymentAuthorisationService.createPisAuthorisation(CREATE_AUTHORISATION_REQUEST);

        // Then
        verify(createPisAuthorisationValidator).validate(new CreatePisAuthorisationObject(invalidPisCommonPaymentResponse, SINGLE, PAYMENT_PRODUCT, PSU_ID_DATA));
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError()).isEqualTo(VALIDATION_ERROR);
    }

    @Test
    void updatePisPsuData_Success_ShouldRecordEvent() {
        // Given:
        when(pisScaAuthorisationServiceResolver.getService(AUTHORISATION_ID))
            .thenReturn(pisScaAuthorisationService);
        when(updatePisCommonPaymentPsuDataValidator.validate(buildUpdatePisCommonPaymentPsuDataPO(buildPisCommonPaymentResponse())))
            .thenReturn(ValidationResult.valid());

        when(pisScaAuthorisationService.updateCommonPaymentPsuData(any()))
            .thenReturn(new Xs2aUpdatePisCommonPaymentPsuDataResponse(ScaStatus.RECEIVED, PAYMENT_ID, AUTHORISATION_ID, PSU_ID_DATA, null));

        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse();

        when(pisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.of(commonPaymentResponse));

        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);
        PaymentAuthorisationParameters request = buildXs2aUpdatePisPsuDataRequest();
        when(eventTypeService.getEventType(request, EventAuthorisationType.PIS))
            .thenReturn(EventType.UPDATE_PAYMENT_AUTHORISATION_PSU_DATA_IDENTIFICATION_REQUEST_RECEIVED);

        // When
        paymentAuthorisationService.updatePisCommonPaymentPsuData(request);

        // Then
        verify(xs2aEventService, times(1)).recordPisTppRequest(eq(PAYMENT_ID), argumentCaptor.capture(), any());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.UPDATE_PAYMENT_AUTHORISATION_PSU_DATA_IDENTIFICATION_REQUEST_RECEIVED);
    }

    @Test
    void updatePisPsuData_shouldStoreStatusesInLoggingContext() {
        // Given:
        when(pisScaAuthorisationServiceResolver.getService(AUTHORISATION_ID))
            .thenReturn(pisScaAuthorisationService);
        when(updatePisCommonPaymentPsuDataValidator.validate(buildUpdatePisCommonPaymentPsuDataPO(buildPisCommonPaymentResponse())))
            .thenReturn(ValidationResult.valid());

        when(pisScaAuthorisationService.updateCommonPaymentPsuData(any()))
            .thenReturn(new Xs2aUpdatePisCommonPaymentPsuDataResponse(SCA_STATUS, PAYMENT_ID, AUTHORISATION_ID, PSU_ID_DATA, null));

        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse();

        when(pisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.of(commonPaymentResponse));

        PaymentAuthorisationParameters request = buildXs2aUpdatePisPsuDataRequest();

        // When
        ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> response = paymentAuthorisationService.updatePisCommonPaymentPsuData(request);

        // Then
        assertFalse(response.hasError());

        InOrder inOrder = inOrder(loggingContextService, pisScaAuthorisationService);
        inOrder.verify(loggingContextService).storeTransactionStatus(TRANSACTION_STATUS);
        inOrder.verify(pisScaAuthorisationService).updateCommonPaymentPsuData(request);
        inOrder.verify(loggingContextService).storeScaStatus(SCA_STATUS);
    }

    @Test
    void updatePisPsuData_shouldStorePaymentTransactionStatusInLoggingContextWhenUpdatePaymentRequestInvalid() {
        // Given:
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse();

        when(pisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.of(commonPaymentResponse));

        when(updatePisCommonPaymentPsuDataValidator.validate(any(UpdatePaymentPsuDataPO.class)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        PaymentAuthorisationParameters request = buildXs2aUpdatePisPsuDataRequest();

        ArgumentCaptor<TransactionStatus> transactionStatusArgumentCaptor = ArgumentCaptor.forClass(TransactionStatus.class);
        // When
        ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> response = paymentAuthorisationService.updatePisCommonPaymentPsuData(request);

        // Then
        assertTrue(response.hasError());
        verify(loggingContextService).storeTransactionStatus(transactionStatusArgumentCaptor.capture());
        assertThat(transactionStatusArgumentCaptor.getValue()).isEqualTo(TransactionStatus.ACCP);
    }

    @Test
    void updatePisPsuData_shouldStoreAuthorisationScaStatusInLoggingContextWhenUpdatePaymentRequestInvalid() {
        // Given:
        ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_400).tppMessages(VALIDATION_ERROR.getTppMessage()).build();

        when(pisScaAuthorisationServiceResolver.getService(AUTHORISATION_ID))
            .thenReturn(pisScaAuthorisationService);
        when(updatePisCommonPaymentPsuDataValidator.validate(buildUpdatePisCommonPaymentPsuDataPO(buildPisCommonPaymentResponse())))
            .thenReturn(ValidationResult.valid());

        when(pisScaAuthorisationService.updateCommonPaymentPsuData(any()))
            .thenReturn(new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, PAYMENT_ID, AUTHORISATION_ID, PSU_ID_DATA));

        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse();
        when(pisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.of(commonPaymentResponse));

        PaymentAuthorisationParameters request = buildXs2aUpdatePisPsuDataRequest();

        ArgumentCaptor<ScaStatus> scaStatusArgumentCaptor = ArgumentCaptor.forClass(ScaStatus.class);
        // When
        ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> response = paymentAuthorisationService.updatePisCommonPaymentPsuData(request);

        // Then
        assertTrue(response.hasError());
        verify(loggingContextService).storeScaStatus(scaStatusArgumentCaptor.capture());
        assertThat(scaStatusArgumentCaptor.getValue()).isEqualTo(ScaStatus.FAILED);
    }

    @Test
    void updatePisPsuData_withInvalidPayment_shouldReturnValidationError() {
        // Given
        PisCommonPaymentResponse invalidPisCommonPaymentResponse = buildInvalidPisCommonPaymentResponse();

        when(pisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.of(invalidPisCommonPaymentResponse));

        when(updatePisCommonPaymentPsuDataValidator.validate(any(UpdatePaymentPsuDataPO.class)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        PaymentAuthorisationParameters request = buildXs2aUpdatePisPsuDataRequest();

        // When:
        ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> actualResponse = paymentAuthorisationService.updatePisCommonPaymentPsuData(request);

        // Then
        verify(updatePisCommonPaymentPsuDataValidator).validate(buildUpdatePisCommonPaymentPsuDataPO(invalidPisCommonPaymentResponse));
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError()).isEqualTo(VALIDATION_ERROR);
    }

    @Test
    void getPaymentInitiationAuthorisation() {
        // Given
        when(pisScaAuthorisationServiceResolver.getService())
            .thenReturn(pisScaAuthorisationService);
        when(getPaymentInitiationAuthorisationsValidator.validate(new CommonPaymentObject(buildPisCommonPaymentResponse(), SINGLE, PAYMENT_PRODUCT)))
            .thenReturn(ValidationResult.valid());

        when(pisScaAuthorisationService.getAuthorisationSubResources(anyString()))
            .thenReturn(Optional.of(new Xs2aAuthorisationSubResources(Collections.singletonList(PAYMENT_ID))));

        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse();

        when(pisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.of(commonPaymentResponse));

        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        ResponseObject<Xs2aAuthorisationSubResources> paymentInitiationAuthorisation = paymentAuthorisationService.getPaymentInitiationAuthorisations(PAYMENT_ID, PAYMENT_PRODUCT, SINGLE);

        // Then
        verify(xs2aEventService, times(1)).recordPisTppRequest(eq(PAYMENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.GET_PAYMENT_AUTHORISATION_REQUEST_RECEIVED);

        assertThat(paymentInitiationAuthorisation.getBody()).isNotNull();
        List<String> authorisationIds = paymentInitiationAuthorisation.getBody().getAuthorisationIds();
        assertFalse(authorisationIds.isEmpty());
        assertThat(authorisationIds.get(0)).isEqualTo(PAYMENT_ID);
    }

    @Test
    void getPaymentInitiationAuthorisation_shouldStoreTransactionStatusInLoggingContext() {
        // Given
        when(pisScaAuthorisationServiceResolver.getService())
            .thenReturn(pisScaAuthorisationService);
        when(getPaymentInitiationAuthorisationsValidator.validate(new CommonPaymentObject(buildPisCommonPaymentResponse(), SINGLE, PAYMENT_PRODUCT)))
            .thenReturn(ValidationResult.valid());

        when(pisScaAuthorisationService.getAuthorisationSubResources(anyString()))
            .thenReturn(Optional.of(new Xs2aAuthorisationSubResources(Collections.singletonList(PAYMENT_ID))));

        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse();

        when(pisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.of(commonPaymentResponse));

        // When
        ResponseObject<Xs2aAuthorisationSubResources> response = paymentAuthorisationService.getPaymentInitiationAuthorisations(PAYMENT_ID, PAYMENT_PRODUCT, SINGLE);

        // Then
        assertFalse(response.hasError());
        verify(loggingContextService).storeTransactionStatus(TRANSACTION_STATUS);
    }

    @Test
    void getPaymentInitiationAuthorisation_withInvalidPayment_shouldReturnValidationError() {
        // Given
        PisCommonPaymentResponse invalidPisCommonPaymentResponse = buildInvalidPisCommonPaymentResponse();

        when(pisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.of(invalidPisCommonPaymentResponse));

        when(getPaymentInitiationAuthorisationsValidator.validate(any(CommonPaymentObject.class)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<Xs2aAuthorisationSubResources> actualResponse = paymentAuthorisationService.getPaymentInitiationAuthorisations(PAYMENT_ID, PAYMENT_PRODUCT, SINGLE);

        // Then
        verify(getPaymentInitiationAuthorisationsValidator).validate(new CommonPaymentObject(invalidPisCommonPaymentResponse, SINGLE, PAYMENT_PRODUCT));
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError()).isEqualTo(VALIDATION_ERROR);
    }

    @Test
    void getPaymentInitiationAuthorisationScaStatus_success() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse();
        PaymentScaStatus paymentScaStatus = new PaymentScaStatus(PSU_ID_DATA, commonPaymentResponse, ScaStatus.RECEIVED);

        when(pisScaAuthorisationService.getAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID))
            .thenReturn(Optional.of(SCA_STATUS));
        when(pisScaAuthorisationServiceResolver.getService(AUTHORISATION_ID))
            .thenReturn(pisScaAuthorisationService);
        when(getPaymentInitiationAuthorisationScaStatusValidator.validate(new GetPaymentInitiationAuthorisationScaStatusPO(buildPisCommonPaymentResponse(), AUTHORISATION_ID, SINGLE, PAYMENT_PRODUCT)))
            .thenReturn(ValidationResult.valid());

        when(pisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.of(commonPaymentResponse));

        when(psuIdDataAuthorisationService.getPsuIdData(AUTHORISATION_ID, null)).thenReturn(PSU_ID_DATA);

        // When
        ResponseObject<PaymentScaStatus> actual = paymentAuthorisationService.getPaymentInitiationAuthorisationScaStatus(PAYMENT_ID,
                                                                                                                         AUTHORISATION_ID, SINGLE, PAYMENT_PRODUCT);
        // Then
        assertFalse(actual.hasError());
        assertEquals(paymentScaStatus, actual.getBody());
    }

    @Test
    void getPaymentInitiationAuthorisationScaStatus_success_shouldRecordEvent() {
        // Given:
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse();
        when(pisScaAuthorisationService.getAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID))
            .thenReturn(Optional.of(SCA_STATUS));
        when(pisScaAuthorisationServiceResolver.getService(AUTHORISATION_ID))
            .thenReturn(pisScaAuthorisationService);
        when(getPaymentInitiationAuthorisationScaStatusValidator.validate(new GetPaymentInitiationAuthorisationScaStatusPO(buildPisCommonPaymentResponse(), AUTHORISATION_ID, SINGLE, PAYMENT_PRODUCT)))
            .thenReturn(ValidationResult.valid());

        when(pisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.of(commonPaymentResponse));
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        paymentAuthorisationService.getPaymentInitiationAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID, SINGLE, PAYMENT_PRODUCT);


        // Then
        verify(xs2aEventService, times(1)).recordPisTppRequest(eq(PAYMENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.GET_PAYMENT_SCA_STATUS_REQUEST_RECEIVED);
    }

    @Test
    void getPaymentInitiationAuthorisationScaStatus_shouldStoreStatusesInLoggingContext() {
        // Given:
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse();
        PaymentScaStatus paymentScaStatus = new PaymentScaStatus(PSU_ID_DATA, commonPaymentResponse, ScaStatus.RECEIVED);

        when(pisScaAuthorisationService.getAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID))
            .thenReturn(Optional.of(SCA_STATUS));
        when(pisScaAuthorisationServiceResolver.getService(AUTHORISATION_ID))
            .thenReturn(pisScaAuthorisationService);
        when(getPaymentInitiationAuthorisationScaStatusValidator.validate(new GetPaymentInitiationAuthorisationScaStatusPO(buildPisCommonPaymentResponse(), AUTHORISATION_ID, SINGLE, PAYMENT_PRODUCT)))
            .thenReturn(ValidationResult.valid());

        when(pisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.of(commonPaymentResponse));

        when(psuIdDataAuthorisationService.getPsuIdData(AUTHORISATION_ID, null)).thenReturn(PSU_ID_DATA);

        // When
        ResponseObject<PaymentScaStatus> response = paymentAuthorisationService.getPaymentInitiationAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID, SINGLE, PAYMENT_PRODUCT);

        // Then
        assertFalse(response.hasError());
        assertEquals(paymentScaStatus, response.getBody());
        verify(loggingContextService).storeTransactionAndScaStatus(TRANSACTION_STATUS, SCA_STATUS);
    }

    @Test
    void getPaymentInitiationAuthorisationScaStatus_failure_wrongIds() {
        // Given
        when(pisCommonPaymentService.getPisCommonPaymentById(WRONG_PAYMENT_ID))
            .thenReturn(Optional.empty());

        // When
        ResponseObject<PaymentScaStatus> actual = paymentAuthorisationService.getPaymentInitiationAuthorisationScaStatus(WRONG_PAYMENT_ID,
                                                                                                                         WRONG_AUTHORISATION_ID, SINGLE, PAYMENT_PRODUCT);

        // Then
        assertTrue(actual.hasError());
        assertNull(actual.getBody());
    }

    @Test
    void getPaymentInitiationAuthorisationScaStatus_withInvalidPayment_shouldReturnValidationError() {
        // Given
        PisCommonPaymentResponse invalidPisCommonPaymentResponse = buildInvalidPisCommonPaymentResponse();
        when(pisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.of(invalidPisCommonPaymentResponse));
        when(getPaymentInitiationAuthorisationScaStatusValidator.validate(any(GetPaymentInitiationAuthorisationScaStatusPO.class)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<PaymentScaStatus> actualResponse = paymentAuthorisationService.getPaymentInitiationAuthorisationScaStatus(PAYMENT_ID,
                                                                                                                                 AUTHORISATION_ID, SINGLE, PAYMENT_PRODUCT);

        // Then
        verify(getPaymentInitiationAuthorisationScaStatusValidator).validate(new GetPaymentInitiationAuthorisationScaStatusPO(invalidPisCommonPaymentResponse, AUTHORISATION_ID, SINGLE, PAYMENT_PRODUCT));
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError()).isEqualTo(VALIDATION_ERROR);
    }

    private PaymentAuthorisationParameters buildXs2aUpdatePisPsuDataRequest() {
        PaymentAuthorisationParameters request = new PaymentAuthorisationParameters();
        request.setAuthorisationId(AUTHORISATION_ID);
        request.setPaymentId(PAYMENT_ID);
        request.setPsuData(PSU_ID_DATA);
        return request;
    }

    private PaymentAuthorisationParameters buildXs2aUpdatePisPsuDataRequestWithFullData() {
        PaymentAuthorisationParameters request = new PaymentAuthorisationParameters();
        request.setAuthorisationId(AUTHORISATION_ID);
        request.setPaymentId(PAYMENT_ID);
        request.setPsuData(PSU_ID_DATA);
        request.setPassword("123");
        request.setPaymentService(SINGLE);
        request.setPaymentProduct(PAYMENT_PRODUCT);
        return request;
    }

    private PisCommonPaymentResponse buildPisCommonPaymentResponse() {
        PisCommonPaymentResponse response = new PisCommonPaymentResponse();
        response.setTransactionStatus(TRANSACTION_STATUS);
        return response;
    }

    private PisCommonPaymentResponse buildInvalidPisCommonPaymentResponse() {
        PisCommonPaymentResponse response = new PisCommonPaymentResponse();
        response.setTppInfo(new TppInfo());
        response.setPaymentType(SINGLE);
        response.setPaymentProduct(PAYMENT_PRODUCT);
        return response;
    }

    private UpdatePaymentPsuDataPO buildUpdatePisCommonPaymentPsuDataPO(PisCommonPaymentResponse invalidPisCommonPaymentResponse) {
        return new UpdatePaymentPsuDataPO(invalidPisCommonPaymentResponse, buildXs2aUpdatePisPsuDataRequest());
    }
}
