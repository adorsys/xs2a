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
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisCancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aPaymentCancellationAuthorisationSubResource;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationServiceResolver;
import de.adorsys.psd2.xs2a.service.consent.PisPsuDataService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisCommonPaymentService;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.pis.CommonPaymentObject;
import de.adorsys.psd2.xs2a.service.validator.pis.authorisation.cancellation.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PaymentCancellationAuthorisationServiceTest {
    private static final String CORRECT_PSU_ID = "123456789";
    private static final String INVALID_PSU_ID = "invalid id";
    private static final String PAYMENT_ID = "594ef79c-d785-41ec-9b14-2ea3a7ae2c7b";
    private static final String WRONG_PAYMENT_ID = "wrong payment id";
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String AUTHORISATION_ID = "a8fc1f02-3639-4528-bd19-3eacf1c67038";
    private static final String INVALID_AUTHORISATION_ID = "invalid authorisation id";
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(CORRECT_PSU_ID, null, null, null);
    private static final PsuIdData INVALID_PSU_ID_DATA = new PsuIdData(INVALID_PSU_ID, null, null, null);
    private static final String WRONG_CANCELLATION_AUTHORISATION_ID = "wrong cancellation authorisation id";
    private static final String CANCELLATION_AUTHORISATION_ID = "dd5d766f-eeb7-4efe-b730-24d5ed53f537";

    private static final PisCommonPaymentResponse PIS_COMMON_PAYMENT_RESPONSE = buildPisCommonPaymentResponse();
    private static final PisCommonPaymentResponse INVALID_PIS_COMMON_PAYMENT_RESPONSE = buildInvalidPisCommonPaymentResponse();

    private static final MessageError VALIDATION_ERROR = new MessageError(ErrorType.PIS_401, TppMessageInformation.of(UNAUTHORIZED));

    @InjectMocks
    private PaymentCancellationAuthorisationServiceImpl paymentCancellationAuthorisationService;

    @Mock
    private Xs2aEventService xs2aEventService;
    @Mock
    private PisPsuDataService pisPsuDataService;
    @Mock
    private PisScaAuthorisationService pisScaAuthorisationService;
    @Mock
    private PisScaAuthorisationServiceResolver pisScaAuthorisationServiceResolver;
    @Mock
    private Xs2aPisCommonPaymentService xs2aPisCommonPaymentService;
    @Mock
    private CreatePisCancellationAuthorisationValidator createPisCancellationAuthorisationValidator;
    @Mock
    private UpdatePisCancellationPsuDataValidator updatePisCancellationPsuDataValidator;
    @Mock
    private GetPaymentCancellationAuthorisationsValidator getPaymentCancellationAuthorisationsValidator;
    @Mock
    private GetPaymentCancellationAuthorisationScaStatusValidator getPaymentCancellationAuthorisationScaStatusValidator;

    @Before
    public void setUp() {
        when(pisScaAuthorisationService.getCancellationAuthorisationScaStatus(PAYMENT_ID, CANCELLATION_AUTHORISATION_ID))
            .thenReturn(Optional.of(ScaStatus.RECEIVED));
        when(pisScaAuthorisationService.getCancellationAuthorisationScaStatus(PAYMENT_ID, WRONG_CANCELLATION_AUTHORISATION_ID))
            .thenReturn(Optional.empty());
        when(pisScaAuthorisationServiceResolver.getService())
            .thenReturn(pisScaAuthorisationService);

        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.of(PIS_COMMON_PAYMENT_RESPONSE));
        when(xs2aPisCommonPaymentService.getPisCommonPaymentById(WRONG_PAYMENT_ID))
            .thenReturn(Optional.of(INVALID_PIS_COMMON_PAYMENT_RESPONSE));

        when(createPisCancellationAuthorisationValidator.validate(new CreatePisCancellationAuthorisationPO(buildPisCommonPaymentResponse(), PSU_ID_DATA)))
            .thenReturn(ValidationResult.valid());
        when(updatePisCancellationPsuDataValidator.validate(new UpdatePisCancellationPsuDataPO(buildPisCommonPaymentResponse(), AUTHORISATION_ID)))
            .thenReturn(ValidationResult.valid());
        when(getPaymentCancellationAuthorisationsValidator.validate(new CommonPaymentObject(buildPisCommonPaymentResponse())))
            .thenReturn(ValidationResult.valid());
        when(getPaymentCancellationAuthorisationScaStatusValidator.validate(new CommonPaymentObject(buildPisCommonPaymentResponse())))
            .thenReturn(ValidationResult.valid());
    }

    @Test
    public void createPisCancellationAuthorization_Success_ShouldRecordEvent() {
        when(pisScaAuthorisationService.createCommonPaymentCancellationAuthorisation(anyString(), any(), any()))
            .thenReturn(Optional.of(new Xs2aCreatePisCancellationAuthorisationResponse(null, null, null)));
        when(pisPsuDataService.getPsuDataByPaymentId(anyString())).thenReturn(Collections.singletonList(PSU_ID_DATA));

        // Given:
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        paymentCancellationAuthorisationService.createPisCancellationAuthorization(PAYMENT_ID, PSU_ID_DATA, PaymentType.SINGLE, PAYMENT_PRODUCT);

        // Then
        verify(xs2aEventService, times(1)).recordPisTppRequest(eq(PAYMENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.START_PAYMENT_CANCELLATION_AUTHORISATION_REQUEST_RECEIVED);
    }

    @Test
    public void createPisCancellationAuthorization_withInvalidPayment_shouldReturnValidationError() {
        // Given:
        when(pisScaAuthorisationService.createCommonPaymentCancellationAuthorisation(anyString(), any(), any()))
            .thenReturn(Optional.of(new Xs2aCreatePisCancellationAuthorisationResponse(null, null, null)));

        when(createPisCancellationAuthorisationValidator.validate(new CreatePisCancellationAuthorisationPO(INVALID_PIS_COMMON_PAYMENT_RESPONSE, INVALID_PSU_ID_DATA)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<Xs2aCreatePisCancellationAuthorisationResponse> actualResponse =
            paymentCancellationAuthorisationService.createPisCancellationAuthorization(WRONG_PAYMENT_ID, INVALID_PSU_ID_DATA, PaymentType.SINGLE, PAYMENT_PRODUCT);

        // Then
        verify(createPisCancellationAuthorisationValidator).validate(new CreatePisCancellationAuthorisationPO(INVALID_PIS_COMMON_PAYMENT_RESPONSE, INVALID_PSU_ID_DATA));
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError()).isEqualTo(VALIDATION_ERROR);
    }

    @Test
    public void updatePisCancellationPsuData_Success_ShouldRecordEvent() {
        when(pisScaAuthorisationService.updateCommonPaymentCancellationPsuData(any()))
            .thenReturn(new Xs2aUpdatePisCommonPaymentPsuDataResponse(ScaStatus.STARTED, PAYMENT_ID, AUTHORISATION_ID, PSU_ID_DATA));

        // Given:
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = buildXs2aUpdatePisPsuDataRequest();
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        paymentCancellationAuthorisationService.updatePisCancellationPsuData(request);

        // Then
        verify(xs2aEventService, times(1)).recordPisTppRequest(eq(PAYMENT_ID), argumentCaptor.capture(), any());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.UPDATE_PAYMENT_CANCELLATION_PSU_DATA_REQUEST_RECEIVED);
    }

    @Test
    public void updatePisCancellationPsuData_withInvalidPayment_shouldReturnValidationError() {
        // Given
        Xs2aUpdatePisCommonPaymentPsuDataRequest invalidUpdatePisPsuDataRequest = buildInvalidXs2aUpdatePisPsuDataRequest();
        when(updatePisCancellationPsuDataValidator.validate(new UpdatePisCancellationPsuDataPO(INVALID_PIS_COMMON_PAYMENT_RESPONSE, INVALID_AUTHORISATION_ID)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> actualResponse =
            paymentCancellationAuthorisationService.updatePisCancellationPsuData(invalidUpdatePisPsuDataRequest);

        // Then
        verify(updatePisCancellationPsuDataValidator).validate(new UpdatePisCancellationPsuDataPO(INVALID_PIS_COMMON_PAYMENT_RESPONSE, INVALID_AUTHORISATION_ID));
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError()).isEqualTo(VALIDATION_ERROR);
    }

    @Test
    public void getPaymentInitiationCancellationAuthorisationInformation_Success_ShouldRecordEvent() {
        when(pisScaAuthorisationService.getCancellationAuthorisationSubResources(anyString()))
            .thenReturn(Optional.of(new Xs2aPaymentCancellationAuthorisationSubResource(Collections.emptyList())));

        // Given:
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = buildXs2aUpdatePisPsuDataRequest();
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        paymentCancellationAuthorisationService.getPaymentInitiationCancellationAuthorisationInformation(PAYMENT_ID);

        // Then
        verify(xs2aEventService, times(1)).recordPisTppRequest(eq(PAYMENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.GET_PAYMENT_CANCELLATION_AUTHORISATION_REQUEST_RECEIVED);
    }

    @Test
    public void getPaymentInitiationCancellationAuthorisationInformation_withInvalidPayment_shouldReturnValidationError() {
        // Given:
        when(pisScaAuthorisationService.getCancellationAuthorisationSubResources(anyString()))
            .thenReturn(Optional.of(new Xs2aPaymentCancellationAuthorisationSubResource(Collections.emptyList())));
        when(getPaymentCancellationAuthorisationsValidator.validate(new CommonPaymentObject(INVALID_PIS_COMMON_PAYMENT_RESPONSE)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<Xs2aPaymentCancellationAuthorisationSubResource> actualResponse =
            paymentCancellationAuthorisationService.getPaymentInitiationCancellationAuthorisationInformation(WRONG_PAYMENT_ID);

        // Then
        verify(getPaymentCancellationAuthorisationsValidator).validate(new CommonPaymentObject(INVALID_PIS_COMMON_PAYMENT_RESPONSE));
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError()).isEqualTo(VALIDATION_ERROR);
    }

    @Test
    public void getPaymentCancellationAuthorisationScaStatus_success() {
        // When
        ResponseObject<ScaStatus> actual =
            paymentCancellationAuthorisationService.getPaymentCancellationAuthorisationScaStatus(PAYMENT_ID,
                                                                                                 CANCELLATION_AUTHORISATION_ID);

        // Then
        assertFalse(actual.hasError());
        assertEquals(ScaStatus.RECEIVED, actual.getBody());
    }

    @Test
    public void getPaymentCancellationAuthorisationScaStatus_success_shouldRecordEvent() {
        // Given:
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        paymentCancellationAuthorisationService.getPaymentCancellationAuthorisationScaStatus(PAYMENT_ID,
                                                                                             CANCELLATION_AUTHORISATION_ID);

        // Then
        verify(xs2aEventService, times(1))
            .recordPisTppRequest(eq(PAYMENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.GET_PAYMENT_CANCELLATION_SCA_STATUS_REQUEST_RECEIVED);
    }

    @Test
    public void getPaymentCancellationAuthorisationScaStatus_failure_wrongIds() {
        // When
        ResponseObject<ScaStatus> actual =
            paymentCancellationAuthorisationService.getPaymentCancellationAuthorisationScaStatus(PAYMENT_ID,
                                                                                                 WRONG_CANCELLATION_AUTHORISATION_ID);

        // Then
        verify(pisScaAuthorisationService).getCancellationAuthorisationScaStatus(PAYMENT_ID, WRONG_CANCELLATION_AUTHORISATION_ID);
        assertTrue(actual.hasError());
        assertNull(actual.getBody());
    }

    @Test
    public void getPaymentCancellationAuthorisationScaStatus_withInvalidPayment_shouldReturnValidationError() {
        // Given
        when(getPaymentCancellationAuthorisationScaStatusValidator.validate(new CommonPaymentObject(INVALID_PIS_COMMON_PAYMENT_RESPONSE)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<ScaStatus> actualResponse =
            paymentCancellationAuthorisationService.getPaymentCancellationAuthorisationScaStatus(WRONG_PAYMENT_ID,
                                                                                                 WRONG_CANCELLATION_AUTHORISATION_ID);

        // Then
        verify(getPaymentCancellationAuthorisationScaStatusValidator).validate(new CommonPaymentObject(INVALID_PIS_COMMON_PAYMENT_RESPONSE));
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError()).isEqualTo(VALIDATION_ERROR);
    }

    private static PisCommonPaymentResponse buildPisCommonPaymentResponse() {
        PisCommonPaymentResponse response = new PisCommonPaymentResponse();
        response.setTransactionStatus(TransactionStatus.RCVD);
        return response;
    }

    private static PisCommonPaymentResponse buildInvalidPisCommonPaymentResponse() {
        PisCommonPaymentResponse response = new PisCommonPaymentResponse();
        response.setTppInfo(new TppInfo());
        return response;
    }

    private Xs2aUpdatePisCommonPaymentPsuDataRequest buildXs2aUpdatePisPsuDataRequest() {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = new Xs2aUpdatePisCommonPaymentPsuDataRequest();
        request.setAuthorisationId(AUTHORISATION_ID);
        request.setPaymentId(PAYMENT_ID);
        return request;
    }

    private Xs2aUpdatePisCommonPaymentPsuDataRequest buildInvalidXs2aUpdatePisPsuDataRequest() {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = new Xs2aUpdatePisCommonPaymentPsuDataRequest();
        request.setAuthorisationId(INVALID_AUTHORISATION_ID);
        request.setPaymentId(WRONG_PAYMENT_ID);
        return request;
    }
}
