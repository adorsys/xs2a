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
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisConsentCancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aPaymentCancellationAuthorisationSubResource;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisConsentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.consent.PisPsuDataService;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentCancellationAuthorisationServiceTest {
    private static final String CORRECT_PSU_ID = "123456789";
    private static final String PAYMENT_ID = "594ef79c-d785-41ec-9b14-2ea3a7ae2c7b";
    private static final String AUTHORISATION_ID = "a8fc1f02-3639-4528-bd19-3eacf1c67038";
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(CORRECT_PSU_ID, null, null, null);
    private static final String WRONG_CANCELLATION_AUTHORISATION_ID = "wrong cancellation authorisation id";
    private static final String WRONG_PAYMENT_ID = "wrong payment id";
    private static final String CANCELLATION_AUTHORISATION_ID = "dd5d766f-eeb7-4efe-b730-24d5ed53f537";

    @InjectMocks
    private PaymentCancellationAuthorisationServiceImpl paymentCancellationAuthorisationService;

    @Mock
    private Xs2aEventService xs2aEventService;
    @Mock
    private PisPsuDataService pisPsuDataService;
    @Mock
    private PisScaAuthorisationService pisScaAuthorisationService;

    @Before
    public void setUp() {
        when(pisScaAuthorisationService.getCancellationAuthorisationScaStatus(PAYMENT_ID, CANCELLATION_AUTHORISATION_ID))
            .thenReturn(Optional.of(ScaStatus.RECEIVED));
        when(pisScaAuthorisationService.getCancellationAuthorisationScaStatus(WRONG_PAYMENT_ID, WRONG_CANCELLATION_AUTHORISATION_ID))
            .thenReturn(Optional.empty());
    }

    @Test
    public void createPisConsentCancellationAuthorization_Success_ShouldRecordEvent() {
        when(pisScaAuthorisationService.createConsentCancellationAuthorisation(anyString(), any(), any()))
            .thenReturn(Optional.of(new Xs2aCreatePisConsentCancellationAuthorisationResponse(null, null, null)));
        when(pisPsuDataService.getPsuDataByPaymentId(anyString())).thenReturn(PSU_ID_DATA);

        // Given:
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        paymentCancellationAuthorisationService.createPisConsentCancellationAuthorization(PAYMENT_ID, PaymentType.SINGLE);

        // Then
        verify(xs2aEventService, times(1)).recordPisTppRequest(eq(PAYMENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.START_PAYMENT_CANCELLATION_AUTHORISATION_REQUEST_RECEIVED);
    }

    @Test
    public void getPaymentInitiationCancellationAuthorisationInformation_Success_ShouldRecordEvent() {
        when(pisScaAuthorisationService.getCancellationAuthorisationSubResources(anyString()))
            .thenReturn(Optional.of(new Xs2aPaymentCancellationAuthorisationSubResource(Collections.emptyList())));

        // Given:
        Xs2aUpdatePisConsentPsuDataRequest request = buildXs2aUpdatePisConsentPsuDataRequest();
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        paymentCancellationAuthorisationService.getPaymentInitiationCancellationAuthorisationInformation(PAYMENT_ID);

        // Then
        verify(xs2aEventService, times(1)).recordPisTppRequest(eq(PAYMENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.GET_PAYMENT_CANCELLATION_AUTHORISATION_REQUEST_RECEIVED);
    }

    @Test
    public void updatePisConsentCancellationPsuData_Success_ShouldRecordEvent() {
        when(pisScaAuthorisationService.updateConsentCancellationPsuData(any()))
            .thenReturn(new Xs2aUpdatePisConsentPsuDataResponse(ScaStatus.STARTED));

        // Given:
        Xs2aUpdatePisConsentPsuDataRequest request = buildXs2aUpdatePisConsentPsuDataRequest();
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        paymentCancellationAuthorisationService.updatePisConsentCancellationPsuData(request);

        // Then
        verify(xs2aEventService, times(1)).recordPisTppRequest(eq(PAYMENT_ID), argumentCaptor.capture(), any());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.UPDATE_PAYMENT_CANCELLATION_PSU_DATA_REQUEST_RECEIVED);
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
            paymentCancellationAuthorisationService.getPaymentCancellationAuthorisationScaStatus(WRONG_PAYMENT_ID,
                WRONG_CANCELLATION_AUTHORISATION_ID);

        // Then
        assertTrue(actual.hasError());
        assertNull(actual.getBody());
    }

    private Xs2aUpdatePisConsentPsuDataRequest buildXs2aUpdatePisConsentPsuDataRequest() {
        Xs2aUpdatePisConsentPsuDataRequest request = new Xs2aUpdatePisConsentPsuDataRequest();
        request.setAuthorizationId(AUTHORISATION_ID);
        request.setPaymentId(PAYMENT_ID);
        return request;
    }
}
