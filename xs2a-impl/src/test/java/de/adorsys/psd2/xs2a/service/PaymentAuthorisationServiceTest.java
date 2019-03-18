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
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthorisationSubResources;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.domain.pis.PaymentAuthorisationType;
import de.adorsys.psd2.xs2a.exception.MessageCategory;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationServiceResolver;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisCommonPaymentService;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.validator.PisEndpointAccessCheckerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PaymentAuthorisationServiceTest {
    private static final String CORRECT_PSU_ID = "123456789";
    private static final String PAYMENT_ID = "594ef79c-d785-41ec-9b14-2ea3a7ae2c7b";
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String AUTHORISATION_ID = "a8fc1f02-3639-4528-bd19-3eacf1c67038";
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(CORRECT_PSU_ID, null, null, null);
    private static final String WRONG_AUTHORISATION_ID = "wrong authorisation id";
    private static final String WRONG_PAYMENT_ID = "wrong payment id";

    @InjectMocks
    private PaymentAuthorisationServiceImpl paymentAuthorisationService;

    @Mock
    private Xs2aEventService xs2aEventService;
    @Mock
    private PisScaAuthorisationServiceResolver pisScaAuthorisationServiceResolver;
    @Mock
    private PisScaAuthorisationService pisScaAuthorisationService;
    @Mock
    private Xs2aPisCommonPaymentService pisCommonPaymentService;
    @Mock
    private PisEndpointAccessCheckerService endpointAccessCheckerService;

    @Before
    public void setUp() {
        when(pisScaAuthorisationService.getAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID))
            .thenReturn(Optional.of(ScaStatus.RECEIVED));
        when(pisScaAuthorisationService.getAuthorisationScaStatus(WRONG_PAYMENT_ID, WRONG_AUTHORISATION_ID))
            .thenReturn(Optional.empty());
        when(pisScaAuthorisationServiceResolver.getService())
            .thenReturn(pisScaAuthorisationService);
    }

    @Test
    public void createPisAuthorization_Success_ShouldRecordEvent() {
        when(pisScaAuthorisationService.createCommonPaymentAuthorisation(PAYMENT_ID, PaymentType.SINGLE, PSU_ID_DATA))
            .thenReturn(Optional.of(new Xs2aCreatePisAuthorisationResponse(null, null, null)));
        when(pisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.of(buildPisCommonPaymentResponse()));

        // Given:
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        paymentAuthorisationService.createPisAuthorization(PAYMENT_ID, PaymentType.SINGLE, PAYMENT_PRODUCT, PSU_ID_DATA);

        // Then
        verify(xs2aEventService, times(1)).recordPisTppRequest(eq(PAYMENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.START_PAYMENT_AUTHORISATION_REQUEST_RECEIVED);
    }

    @Test
    public void updatePisPsuData_Success_ShouldRecordEvent() {
        when(pisScaAuthorisationService.updateCommonPaymentPsuData(any()))
            .thenReturn(new Xs2aUpdatePisCommonPaymentPsuDataResponse(ScaStatus.STARTED));
        when(pisCommonPaymentService.getPisCommonPaymentById(PAYMENT_ID))
            .thenReturn(Optional.of(buildPisCommonPaymentResponse()));
        when(endpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, PaymentAuthorisationType.INITIATION))
            .thenReturn(true);

        // Given:
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = buildXs2aUpdatePisPsuDataRequest();

        // When
        paymentAuthorisationService.updatePisCommonPaymentPsuData(request);

        // Then
        verify(xs2aEventService, times(1)).recordPisTppRequest(eq(PAYMENT_ID), argumentCaptor.capture(), any());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.UPDATE_PAYMENT_AUTHORISATION_PSU_DATA_REQUEST_RECEIVED);
    }

    @Test
    public void updatePisPsuData_Failure_EndpointIsNotAccessible() {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = buildXs2aUpdatePisPsuDataRequest();

        doNothing()
            .when(xs2aEventService).recordPisTppRequest(PAYMENT_ID, EventType.UPDATE_PAYMENT_AUTHORISATION_PSU_DATA_REQUEST_RECEIVED, request);

        when(endpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, PaymentAuthorisationType.INITIATION))
            .thenReturn(false);

        ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> actualResponse = paymentAuthorisationService.updatePisCommonPaymentPsuData(request);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError().getErrorType()).isEqualTo(ErrorType.PIS_403);
        assertThat(actualResponse.getError().getTppMessage().getCategory()).isEqualTo(MessageCategory.ERROR);
        assertThat(actualResponse.getError().getTppMessage().getMessageErrorCode()).isEqualTo(MessageErrorCode.SERVICE_BLOCKED);
    }

    @Test
    public void getPaymentInitiationAuthorisation() {
        when(pisScaAuthorisationService.getAuthorisationSubResources(anyString()))
            .thenReturn(Optional.of(new Xs2aAuthorisationSubResources(Collections.singletonList(PAYMENT_ID))));

        // Given:
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        ResponseObject<Xs2aAuthorisationSubResources> paymentInitiationAuthorisation = paymentAuthorisationService.getPaymentInitiationAuthorisations(PAYMENT_ID);

        // Then
        verify(xs2aEventService, times(1)).recordPisTppRequest(eq(PAYMENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.GET_PAYMENT_AUTHORISATION_REQUEST_RECEIVED);

        assertThat(paymentInitiationAuthorisation.getBody()).isNotNull();
        List<String> authorisationIds = paymentInitiationAuthorisation.getBody().getAuthorisationIds();
        assertFalse(authorisationIds.isEmpty());
        assertThat(authorisationIds.get(0)).isEqualTo(PAYMENT_ID);
    }

    @Test
    public void getPaymentInitiationAuthorisationScaStatus_success() {
        // When
        ResponseObject<ScaStatus> actual = paymentAuthorisationService.getPaymentInitiationAuthorisationScaStatus(PAYMENT_ID,
                                                                                                                  AUTHORISATION_ID);

        // Then
        assertFalse(actual.hasError());
        assertEquals(ScaStatus.RECEIVED, actual.getBody());
    }

    @Test
    public void getPaymentInitiationAuthorisationScaStatus_success_shouldRecordEvent() {
        // Given:
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        paymentAuthorisationService.getPaymentInitiationAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID);


        // Then
        verify(xs2aEventService, times(1)).recordPisTppRequest(eq(PAYMENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.GET_PAYMENT_SCA_STATUS_REQUEST_RECEIVED);
    }

    @Test
    public void getPaymentInitiationAuthorisationScaStatus_failure_wrongIds() {
        // When
        ResponseObject<ScaStatus> actual = paymentAuthorisationService.getPaymentInitiationAuthorisationScaStatus(WRONG_PAYMENT_ID,
                                                                                                                  WRONG_AUTHORISATION_ID);

        // Then
        assertTrue(actual.hasError());
        assertNull(actual.getBody());
    }

    private Xs2aUpdatePisCommonPaymentPsuDataRequest buildXs2aUpdatePisPsuDataRequest() {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = new Xs2aUpdatePisCommonPaymentPsuDataRequest();
        request.setAuthorisationId(AUTHORISATION_ID);
        request.setPaymentId(PAYMENT_ID);
        return request;
    }

    private PisCommonPaymentResponse buildPisCommonPaymentResponse() {
        PisCommonPaymentResponse response = new PisCommonPaymentResponse();
        response.setTransactionStatus(TransactionStatus.ACCP);
        return response;
    }
}
