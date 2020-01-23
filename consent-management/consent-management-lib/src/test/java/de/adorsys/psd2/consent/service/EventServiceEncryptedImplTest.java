/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.event.service.Xs2aEventService;
import de.adorsys.psd2.event.service.model.EventBO;
import de.adorsys.psd2.event.service.model.PsuIdDataBO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceEncryptedImplTest {
    private static final String ENCRYPTED_CONSENT_ID = "encrypted consent id";
    private static final String UNDECRYPTABLE_CONSENT_ID = "undecryptable consent id";
    private static final String DECRYPTED_CONSENT_ID = "255574b2-f115-4f3c-8d77-c1897749c060";

    private static final String ENCRYPTED_PAYMENT_ID = "encrypted payment id";
    private static final String UNDECRYPTABLE_PAYMENT_ID = "undecryptable payment id";
    private static final String DECRYPTED_PAYMENT_ID = "91cd2158-4344-44f4-bdbb-c736ededa436";

    private static final String PSU_ID = "ID";
    private static final String PSU_ID_TYPE = "TYPE";
    private static final String PSU_CORPORATE_ID = "CORPORATE_ID";
    private static final String PSU_CORPORATE_ID_TYPE = "CORPORATE_ID_TYPE";
    private static final String AUTHORISATION_NUMBER = "999";
    private static final UUID X_REQUEST_ID = UUID.fromString("0d7f200e-09b4-46f5-85bd-f4ea89fccace");
    private static final UUID INTERNAL_REQUEST_ID = UUID.fromString("b571c834-4eb1-468f-91b0-f5e83589bc22");

    @InjectMocks
    private EventServiceEncryptedImpl eventServiceEncryptedImpl;
    @Mock
    private Xs2aEventService eventService;
    @Mock
    private SecurityDataService securityDataService;
    private EventBO decryptedEvent;
    private EventBO event;

    @BeforeEach
    void setUp() {
        decryptedEvent = buildEvent(DECRYPTED_CONSENT_ID, DECRYPTED_PAYMENT_ID);
        event = buildEvent(ENCRYPTED_CONSENT_ID, ENCRYPTED_PAYMENT_ID);
    }

    @Test
    void recordEvent_success() {
        // Given
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.of(DECRYPTED_PAYMENT_ID));
        when(securityDataService.decryptId(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(DECRYPTED_CONSENT_ID));
        when(eventService.recordEvent(decryptedEvent)).thenReturn(true);

        // When
        boolean actual = eventServiceEncryptedImpl.recordEvent(event);

        // Then
        assertTrue(actual);
        verify(eventService, times(1)).recordEvent(decryptedEvent);
    }

    @Test
    void recordEvent_fail_recordingFailed() {
        // Given
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.of(DECRYPTED_PAYMENT_ID));
        when(securityDataService.decryptId(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(DECRYPTED_CONSENT_ID));
        when(eventService.recordEvent(decryptedEvent)).thenReturn(false);

        // When
        boolean actual = eventServiceEncryptedImpl.recordEvent(event);

        // Then
        assertFalse(actual);
        verify(eventService, times(1)).recordEvent(decryptedEvent);
    }

    @Test
    void recordEvent_fail_decryptionFailed() {
        // Given
        EventBO event = buildEvent(UNDECRYPTABLE_CONSENT_ID, UNDECRYPTABLE_PAYMENT_ID);
        when(securityDataService.decryptId(UNDECRYPTABLE_PAYMENT_ID)).thenReturn(Optional.empty());
        when(securityDataService.decryptId(UNDECRYPTABLE_CONSENT_ID)).thenReturn(Optional.empty());
        when(eventService.recordEvent(buildEvent())).thenReturn(false);

        // When
        boolean actual = eventServiceEncryptedImpl.recordEvent(event);

        // Then
        assertFalse(actual);
        verify(eventService, times(1)).recordEvent(buildEvent());
    }

    @Test
    void recordEvent_CheckEventBuilder() {
        // Given
        ArgumentCaptor<EventBO> argumentCaptor = ArgumentCaptor.forClass(EventBO.class);
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.of(DECRYPTED_PAYMENT_ID));
        when(securityDataService.decryptId(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(DECRYPTED_CONSENT_ID));
        when(eventService.recordEvent(argumentCaptor.capture())).thenReturn(true);

        // When
        boolean actual = eventServiceEncryptedImpl.recordEvent(event);

        // Then
        assertTrue(actual);
        verify(eventService).recordEvent(argumentCaptor.capture());
        verify(eventService, times(1)).recordEvent(decryptedEvent);
        assertEquals(decryptedEvent, argumentCaptor.getValue());
    }

    private EventBO buildEvent() {
        return buildEvent(null, null);
    }

    private EventBO buildEvent(String consentId, String paymentId) {
        return EventBO.builder()
                   .consentId(consentId)
                   .paymentId(paymentId)
                   .psuIdData(buildPsuIdData())
                   .tppAuthorisationNumber(AUTHORISATION_NUMBER)
                   .xRequestId(X_REQUEST_ID)
                   .internalRequestId(INTERNAL_REQUEST_ID)
                   .build();
    }

    private PsuIdDataBO buildPsuIdData() {
        return new PsuIdDataBO(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE);
    }
}
