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

package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.validator.tpp.TppDomainValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TppUriHeaderValidatorTest {
    private static final String TPP_REDIRECT_URI = "request/redirect_uri";
    private static final String TPP_NOK_REDIRECT_URI = "request/nok_redirect_uri";
    private static final TppRedirectUri TPP_REDIRECT_URIs = new TppRedirectUri(TPP_REDIRECT_URI, TPP_NOK_REDIRECT_URI);

    @InjectMocks
    private TppUriHeaderValidator validator;

    @Mock
    private TppDomainValidator tppDomainValidator;
    @Mock
    private ScaApproachResolver scaApproachResolver;

    @Test
    void validate() {
        // When
        ValidationResult actual = validator.validate(TPP_REDIRECT_URIs);

        // Then
        assertTrue(actual.isValid());
    }

    @Test
    void buildWarningMessages_emptySet() {
        // Given
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.REDIRECT);
        Set<TppMessageInformation> emptySet = new HashSet<>();
        when(tppDomainValidator.buildWarningMessages(any())).thenReturn(emptySet);

        // When
        Set<TppMessageInformation> actual = validator.buildWarningMessages(TPP_REDIRECT_URIs);

        // Then
        assertEquals(actual, emptySet);
        verify(tppDomainValidator, times(2)).buildWarningMessages(any());
    }

    @Test
    void buildWarningMessages_noCheck() {
        // Given
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.EMBEDDED);
        Set<TppMessageInformation> emptySet = new HashSet<>();

        // When
        Set<TppMessageInformation> actual = validator.buildWarningMessages(TPP_REDIRECT_URIs);

        // Then
        assertEquals(actual, emptySet);
        verify(tppDomainValidator, times(0)).buildWarningMessages(any());
    }
}
