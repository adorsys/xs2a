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

package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.core.tpp.TppNotificationData;
import de.adorsys.psd2.xs2a.service.validator.tpp.TppDomainValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TppNotificationDataValidatorTest {
    private static final String TPP_NOTIFICATION_URI = "http://my.bank.example.com/notification";
    private final TppNotificationData tppNotificationData = new TppNotificationData(Arrays.asList(NotificationSupportedMode.SCA, NotificationSupportedMode.LAST), TPP_NOTIFICATION_URI);
    private final TppNotificationData emptyTppNotificationData = new TppNotificationData(Collections.emptyList(), TPP_NOTIFICATION_URI);

    @InjectMocks
    private TppNotificationDataValidator validator;

    @Mock
    private TppDomainValidator tppDomainValidator;

    @Test
    void validate() {
        // When
        ValidationResult actual = validator.validate(tppNotificationData);

        // Then
        assertTrue(actual.isValid());
    }

    @Test
    void buildWarningMessages_emptySet() {
        // Given
        Set<TppMessageInformation> emptySet = new HashSet<>();
        when(tppDomainValidator.buildWarningMessages(any()))
            .thenReturn(emptySet);

        // When
        Set<TppMessageInformation> actual = validator.buildWarningMessages(tppNotificationData);

        // Then
        assertEquals(actual, emptySet);
        verify(tppDomainValidator, times(1)).buildWarningMessages(any());
    }

    @Test
    void buildWarningMessages_noCheck() {
        // Given
        Set<TppMessageInformation> emptySet = new HashSet<>();

        // When
        Set<TppMessageInformation> actual = validator.buildWarningMessages(emptyTppNotificationData);

        // Then
        assertEquals(actual, emptySet);
        verify(tppDomainValidator, times(0)).buildWarningMessages(any());
    }
}
