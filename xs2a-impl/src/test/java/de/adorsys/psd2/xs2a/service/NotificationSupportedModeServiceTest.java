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
