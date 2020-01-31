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

import de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode;
import de.adorsys.psd2.xs2a.core.tpp.TppNotificationData;
import de.adorsys.psd2.xs2a.domain.NotificationModeResponseHeaders;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationSupportedModeServiceTest {
    private static final String TPP_NOTIFICATION_URI = "http://my.bank.example.com/notification";

    @Mock
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;

    @InjectMocks
    private NotificationSupportedModeService notificationSupportedModeService;

    @Test
    void resolveNotificationHeaders_no_filtering() {
        // Given
        List<NotificationSupportedMode> usedModes = Arrays.asList(NotificationSupportedMode.SCA, NotificationSupportedMode.LAST);
        when(aspspProfileServiceWrapper.getNotificationSupportedModes()).thenReturn(Arrays.asList(NotificationSupportedMode.SCA,
                                                                                                  NotificationSupportedMode.LAST,
                                                                                                  NotificationSupportedMode.PROCESS));
        // When
        NotificationModeResponseHeaders actual = notificationSupportedModeService.resolveNotificationHeaders(usedModes);

        // Then
        assertTrue(actual.getAspspNotificationSupport());
        assertNotNull(actual.getAspspNotificationContent());
        assertEquals("status=SCA,LAST", actual.getAspspNotificationContent());
    }

    @Test
    void resolveNotificationHeaders_with_filtering() {
        // Given
        List<NotificationSupportedMode> usedModes = Arrays.asList(NotificationSupportedMode.SCA,
                                                                  NotificationSupportedMode.LAST);
        when(aspspProfileServiceWrapper.getNotificationSupportedModes())
            .thenReturn(Collections.singletonList(NotificationSupportedMode.NONE));

        // When
        NotificationModeResponseHeaders actual = notificationSupportedModeService.resolveNotificationHeaders(usedModes);

        // Then
        assertNull(actual.getAspspNotificationSupport());
        assertNull(actual.getAspspNotificationContent());
    }

    @Test
    void resolveNotificationHeaders_empty_modes() {
        // Given
        List<NotificationSupportedMode> usedModes = Collections.emptyList();
        when(aspspProfileServiceWrapper.getNotificationSupportedModes())
            .thenReturn(Collections.singletonList(NotificationSupportedMode.SCA));

        // When
        NotificationModeResponseHeaders actual = notificationSupportedModeService.resolveNotificationHeaders(usedModes);

        // Then
        assertFalse(actual.getAspspNotificationSupport());
        assertNull(actual.getAspspNotificationContent());
    }

    @Test
    void getProcessedNotificationModes_with_filtering() {
        // Given
        when(aspspProfileServiceWrapper.getNotificationSupportedModes())
            .thenReturn(Arrays.asList(NotificationSupportedMode.SCA, NotificationSupportedMode.LAST));
        String tppNotificationContentPreferred = "status=SCA,LAST";
        TppNotificationData expected = new TppNotificationData(Arrays.asList(NotificationSupportedMode.SCA, NotificationSupportedMode.LAST), TPP_NOTIFICATION_URI);
        // When
        TppNotificationData actual = notificationSupportedModeService.getTppNotificationData(tppNotificationContentPreferred, TPP_NOTIFICATION_URI);

        // Then
        assertEquals(expected, actual);
    }

    @Test
    void getProcessedNotificationModes_empty() {
        // Given
        when(aspspProfileServiceWrapper.getNotificationSupportedModes())
            .thenReturn(Arrays.asList(NotificationSupportedMode.SCA, NotificationSupportedMode.LAST));
        String tppNotificationContentPreferred = "";
        TppNotificationData expected = new TppNotificationData(Collections.emptyList(), TPP_NOTIFICATION_URI);

        // When
        TppNotificationData actual = notificationSupportedModeService.getTppNotificationData(tppNotificationContentPreferred, TPP_NOTIFICATION_URI);

        // Then
        assertEquals(expected, actual);
    }

    @Test
    void getProcessedNotificationModes_supported_none() {
        // Given
        when(aspspProfileServiceWrapper.getNotificationSupportedModes())
            .thenReturn(Arrays.asList(NotificationSupportedMode.NONE, NotificationSupportedMode.LAST));
        String tppNotificationContentPreferred = "";
        TppNotificationData expected = new TppNotificationData(Collections.emptyList(), TPP_NOTIFICATION_URI);
        // When
        TppNotificationData actual = notificationSupportedModeService.getTppNotificationData(tppNotificationContentPreferred, TPP_NOTIFICATION_URI);

        // Then
        assertEquals(expected, actual);
    }

}
