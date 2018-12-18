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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.service.EventService;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.event.Event;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EventServiceInternalEncryptedTest {
    private static final String ENCRYPTED_CONSENT_ID = "encrypted consent id";
    private static final String UNDECRYPTABLE_CONSENT_ID = "undecryptable consent id";
    private static final String DECRYPTED_CONSENT_ID = "255574b2-f115-4f3c-8d77-c1897749c060";

    private static final String ENCRYPTED_PAYMENT_ID = "encrypted payment id";
    private static final String UNDECRYPTABLE_PAYMENT_ID = "undecryptable payment id";
    private static final String DECRYPTED_PAYMENT_ID = "91cd2158-4344-44f4-bdbb-c736ededa436";

    @InjectMocks
    private EventServiceInternalEncrypted eventServiceInternalEncrypted;
    @Mock
    private EventService eventService;
    @Mock
    private SecurityDataService securityDataService;

    @Before
    public void setUp() {
        when(eventService.recordEvent(buildEvent(DECRYPTED_CONSENT_ID, DECRYPTED_PAYMENT_ID))).thenReturn(true);
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.of(DECRYPTED_PAYMENT_ID));
        when(securityDataService.decryptId(UNDECRYPTABLE_PAYMENT_ID)).thenReturn(Optional.empty());
        when(securityDataService.decryptId(ENCRYPTED_CONSENT_ID)).thenReturn(Optional.of(DECRYPTED_CONSENT_ID));
        when(securityDataService.decryptId(UNDECRYPTABLE_CONSENT_ID)).thenReturn(Optional.empty());
    }

    @Test
    public void recordEvent_success() {
        // Given
        Event event = buildEvent(ENCRYPTED_CONSENT_ID, ENCRYPTED_PAYMENT_ID);

        // When
        boolean actual = eventServiceInternalEncrypted.recordEvent(event);

        // Then
        assertTrue(actual);
        verify(eventService, times(1))
            .recordEvent(buildEvent(DECRYPTED_CONSENT_ID, DECRYPTED_PAYMENT_ID));
    }

    @Test
    public void recordEvent_fail_recordingFailed() {
        when(eventService.recordEvent(any())).thenReturn(false);

        // Given
        Event event = buildEvent(ENCRYPTED_CONSENT_ID, ENCRYPTED_PAYMENT_ID);

        // When
        boolean actual = eventServiceInternalEncrypted.recordEvent(event);

        // Then
        assertFalse(actual);
        verify(eventService, times(1))
            .recordEvent(buildEvent(DECRYPTED_CONSENT_ID, DECRYPTED_PAYMENT_ID));
    }

    @Test
    public void recordEvent_fail_decryptionFailed() {
        // Given
        Event event = buildEvent(UNDECRYPTABLE_CONSENT_ID, UNDECRYPTABLE_PAYMENT_ID);

        // When
        boolean actual = eventServiceInternalEncrypted.recordEvent(event);

        // Then
        assertFalse(actual);
        verify(eventService, times(1)).recordEvent(buildEvent());
    }

    private Event buildEvent() {
        return buildEvent(null, null);
    }

    private Event buildEvent(String consentId, String paymentId) {
        return Event.builder()
            .consentId(consentId)
            .paymentId(paymentId)
            .build();
    }
}
