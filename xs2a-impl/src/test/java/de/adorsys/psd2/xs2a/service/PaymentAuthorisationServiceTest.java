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
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PaymentAuthorisationServiceTest {
    private static final String PAYMENT_ID = "c713a32c-15ff-4f90-afa0-34a500359844";
    private static final String WRONG_PAYMENT_ID = "wrong payment id";
    private static final String AUTHORISATION_ID = "ad746cb3-a01b-4196-a6b9-40b0e4cd2350";
    private static final String WRONG_AUTHORISATION_ID = "wrong authorisation id";

    @InjectMocks
    private PaymentAuthorisationService paymentAuthorisationService;
    @Mock
    private PisScaAuthorisationService pisScaAuthorisationService;
    @Mock
    private Xs2aEventService xs2aEventService;

    @Before
    public void setUp() {
        when(pisScaAuthorisationService.getAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID))
            .thenReturn(Optional.of(ScaStatus.RECEIVED));
        when(pisScaAuthorisationService.getAuthorisationScaStatus(WRONG_PAYMENT_ID, WRONG_AUTHORISATION_ID))
            .thenReturn(Optional.empty());
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
}
