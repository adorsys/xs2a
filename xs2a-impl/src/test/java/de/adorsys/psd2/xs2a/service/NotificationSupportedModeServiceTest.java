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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode;
import de.adorsys.psd2.xs2a.domain.NotificationModeResponseHeaders;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.web.validator.header.TppDomainValidator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NotificationSupportedModeServiceTest {
    private static final String TPP_NOTIFICATION_URI = "http://my.bank.example.com/notification";

    @Mock
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;

    @Mock
    private TppDomainValidator tppDomainValidator;

    @Mock
    private RequestProviderService requestProviderService;

    @InjectMocks
    private NotificationSupportedModeService notificationSupportedModeService;

    @Test
    public void resolveNotificationHeaders_no_filtering() {
        // Given
        List<NotificationSupportedMode> usedModes = Arrays.asList(NotificationSupportedMode.SCA, NotificationSupportedMode.LAST);
        when(aspspProfileServiceWrapper.getNotificationSupportedModes()).thenReturn(Arrays.asList(NotificationSupportedMode.SCA,
                                                                                                  NotificationSupportedMode.LAST,
                                                                                                  NotificationSupportedMode.PROCESS));
        when(tppDomainValidator.validate(TPP_NOTIFICATION_URI))
            .thenReturn(ValidationResult.valid());

        // When
        NotificationModeResponseHeaders actual = notificationSupportedModeService.resolveNotificationHeaders(usedModes, TPP_NOTIFICATION_URI);

        // Then
        assertTrue(actual.getAspspNotificationSupport());
        assertNotNull(actual.getAspspNotificationContent());
        assertEquals("status=SCA,LAST", actual.getAspspNotificationContent());
    }

    @Test
    public void resolveNotificationHeaders_with_filtering() {
        // Given
        List<NotificationSupportedMode> usedModes = Arrays.asList(NotificationSupportedMode.SCA,
                                                                  NotificationSupportedMode.LAST);
        when(aspspProfileServiceWrapper.getNotificationSupportedModes())
            .thenReturn(Collections.singletonList(NotificationSupportedMode.NONE));

        // When
        NotificationModeResponseHeaders actual = notificationSupportedModeService.resolveNotificationHeaders(usedModes, TPP_NOTIFICATION_URI);

        // Then
        assertNull(actual.getAspspNotificationSupport());
        assertNull(actual.getAspspNotificationContent());
    }

    @Test
    public void resolveNotificationHeaders_empty_modes() {
        // Given
        List<NotificationSupportedMode> usedModes = Collections.emptyList();
        when(aspspProfileServiceWrapper.getNotificationSupportedModes())
            .thenReturn(Collections.singletonList(NotificationSupportedMode.SCA));

        when(tppDomainValidator.validate(TPP_NOTIFICATION_URI))
            .thenReturn(ValidationResult.valid());

        // When
        NotificationModeResponseHeaders actual = notificationSupportedModeService.resolveNotificationHeaders(usedModes, TPP_NOTIFICATION_URI);

        // Then
        assertFalse(actual.getAspspNotificationSupport());
        assertNull(actual.getAspspNotificationContent());
    }

    @Test
    public void getProcessedNotificationModes_with_filtering() {
        // Given
        when(aspspProfileServiceWrapper.getNotificationSupportedModes())
            .thenReturn(Arrays.asList(NotificationSupportedMode.SCA, NotificationSupportedMode.LAST));
        String tppNotificationContentPreferred = "status=SCA,LAST";

        // When
        Set<NotificationSupportedMode> actual = new HashSet<>(notificationSupportedModeService.getProcessedNotificationModes(tppNotificationContentPreferred));

        // Then
        assertEquals(EnumSet.of(NotificationSupportedMode.LAST, NotificationSupportedMode.SCA), actual);
    }

    @Test
    public void getProcessedNotificationModes_empty() {
        // Given
        when(aspspProfileServiceWrapper.getNotificationSupportedModes())
            .thenReturn(Arrays.asList(NotificationSupportedMode.SCA, NotificationSupportedMode.LAST));
        String tppNotificationContentPreferred = "";

        // When
        Set<NotificationSupportedMode> actual = new HashSet<>(notificationSupportedModeService.getProcessedNotificationModes(tppNotificationContentPreferred));

        // Then
        assertEquals(Collections.emptySet(), actual);
    }

    @Test
    public void getProcessedNotificationModes_supported_none() {
        // Given
        when(aspspProfileServiceWrapper.getNotificationSupportedModes())
            .thenReturn(Arrays.asList(NotificationSupportedMode.NONE, NotificationSupportedMode.LAST));
        String tppNotificationContentPreferred = "";

        // When
        Set<NotificationSupportedMode> actual = new HashSet<>(notificationSupportedModeService.getProcessedNotificationModes(tppNotificationContentPreferred));

        // Then
        assertEquals(Collections.emptySet(), actual);
    }

}
