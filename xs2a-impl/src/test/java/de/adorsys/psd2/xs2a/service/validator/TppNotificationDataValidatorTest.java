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

package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode;
import de.adorsys.psd2.xs2a.core.tpp.TppNotificationData;
import de.adorsys.psd2.xs2a.service.validator.tpp.TppDomainValidator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TppNotificationDataValidatorTest {
    private static final String TPP_NOTIFICATION_URI = "http://my.bank.example.com/notification";
    private final TppNotificationData tppNotificationData = new TppNotificationData(Arrays.asList(NotificationSupportedMode.SCA, NotificationSupportedMode.LAST), TPP_NOTIFICATION_URI);
    private final TppNotificationData emptyTppNotificationData = new TppNotificationData(Collections.EMPTY_LIST, TPP_NOTIFICATION_URI);

    @InjectMocks
    private TppNotificationDataValidator validator;

    @Mock
    private TppDomainValidator tppDomainValidator;

    @Test
    public void validate() {
        // When
        ValidationResult actual = validator.validate(tppNotificationData);

        // Then
        assertTrue(actual.isValid());
    }

    @Test
    public void buildWarningMessages_emptySet() {
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
    public void buildWarningMessages_noCheck() {
        // Given
        Set<TppMessageInformation> emptySet = new HashSet<>();

        // When
        Set<TppMessageInformation> actual = validator.buildWarningMessages(emptyTppNotificationData);

        // Then
        assertEquals(actual, emptySet);
        verify(tppDomainValidator, times(0)).buildWarningMessages(any());
    }
}
