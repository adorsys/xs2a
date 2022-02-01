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
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.validator.tpp.TppDomainValidator;
import de.adorsys.psd2.xs2a.web.validator.constants.Xs2aHeaderConstant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_INVALID_DOMAIN;
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
    private  ScaApproachResolver scaApproachResolver;

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

    @Test
    void validate_valid() {
        // Given
        Map<String, String> headers = new HashMap<>();
        headers.put(Xs2aHeaderConstant.TPP_REDIRECT_URI, TPP_REDIRECT_URI);
        headers.put(Xs2aHeaderConstant.TPP_NOK_REDIRECT_URI, TPP_NOK_REDIRECT_URI);

        MessageError messageError = new MessageError();

        when(tppDomainValidator.validate(TPP_REDIRECT_URI)).thenReturn(ValidationResult.valid());
        when(tppDomainValidator.validate(TPP_NOK_REDIRECT_URI)).thenReturn(ValidationResult.valid());

        // When
        MessageError actual = validator.validate(headers, messageError);

        // Then
        assertEquals(messageError, actual);
    }

    @Test
    void validate_invalidRedirectUri() {
        // Given
        Map<String, String> headers = new HashMap<>();
        headers.put(Xs2aHeaderConstant.TPP_REDIRECT_URI, TPP_REDIRECT_URI);
        headers.put(Xs2aHeaderConstant.TPP_NOK_REDIRECT_URI, TPP_NOK_REDIRECT_URI);

        MessageError messageError = new MessageError();

        ValidationResult validationResult = buildInvalidResult();

        when(tppDomainValidator.validate(TPP_REDIRECT_URI)).thenReturn(ValidationResult.valid());
        when(tppDomainValidator.validate(TPP_NOK_REDIRECT_URI)).thenReturn(validationResult);

        // When
        MessageError actual = validator.validate(headers, messageError);

        // Then
        assertEquals(validationResult.getMessageError(), actual);
    }

    @Test
    void validate_invalidNokRedirectUri() {
        // Given
        Map<String, String> headers = new HashMap<>();
        headers.put(Xs2aHeaderConstant.TPP_REDIRECT_URI, TPP_REDIRECT_URI);
        headers.put(Xs2aHeaderConstant.TPP_NOK_REDIRECT_URI, TPP_NOK_REDIRECT_URI);

        MessageError messageError = new MessageError();

        ValidationResult validationResult = buildInvalidResult();

        when(tppDomainValidator.validate(TPP_REDIRECT_URI)).thenReturn(validationResult);

        // When
        MessageError actual = validator.validate(headers, messageError);

        // Then
        assertEquals(validationResult.getMessageError(), actual);
    }

    private ValidationResult buildInvalidResult() {
        return ValidationResult.invalid(
            ErrorType.PIS_400, TppMessageInformation.of(FORMAT_ERROR_INVALID_DOMAIN));
    }
}
