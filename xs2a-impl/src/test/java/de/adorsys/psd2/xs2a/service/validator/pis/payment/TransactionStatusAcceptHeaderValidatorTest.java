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

package de.adorsys.psd2.xs2a.service.validator.pis.payment;

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionStatusAcceptHeaderValidatorTest {
    private static final String JSON_MEDIA_TYPE = "application/json";
    private static final String ACCEPT_HEADER_JSON_XML = "application/json,application/xml";
    private static final String ACCEPT_HEADER_JSON = "application/json";
    private static final String ACCEPT_HEADER_XML = "application/xml";
    private static final String ACCEPT_HEADER_WILDCARD = "*/*";

    private static final MessageError VALIDATION_ERROR = new MessageError(ErrorType.PIS_406, TppMessageInformation.of(MessageErrorCode.REQUESTED_FORMATS_INVALID));

    @Mock
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;

    @InjectMocks
    private TransactionStatusAcceptHeaderValidator transactionStatusAcceptHeaderValidator;

    @Test
    void validate_withSupportedHeader_shouldReturnValid() {
        // Given
        when(aspspProfileServiceWrapper.getSupportedTransactionStatusFormats())
            .thenReturn(Collections.singletonList(JSON_MEDIA_TYPE));

        // When
        ValidationResult validationResult = transactionStatusAcceptHeaderValidator.validate(ACCEPT_HEADER_JSON);

        // Then
        verify(aspspProfileServiceWrapper).getSupportedTransactionStatusFormats();

        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    void validate_withMixedSupportedHeader_shouldReturnValid() {
        // Given
        when(aspspProfileServiceWrapper.getSupportedTransactionStatusFormats())
            .thenReturn(Collections.singletonList(JSON_MEDIA_TYPE));

        // When
        ValidationResult validationResult = transactionStatusAcceptHeaderValidator.validate(ACCEPT_HEADER_JSON_XML);

        // Then
        verify(aspspProfileServiceWrapper).getSupportedTransactionStatusFormats();

        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    void validate_withWildcardHeader_shouldReturnValid() {
        // When
        ValidationResult validationResult = transactionStatusAcceptHeaderValidator.validate(ACCEPT_HEADER_WILDCARD);

        // Then
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    void validate_withEmptyListInProfile_shouldReturnValid() {
        // Given
        when(aspspProfileServiceWrapper.getSupportedTransactionStatusFormats())
            .thenReturn(Collections.emptyList());

        // When
        ValidationResult validationResult = transactionStatusAcceptHeaderValidator.validate(ACCEPT_HEADER_JSON_XML);

        // Then
        verify(aspspProfileServiceWrapper).getSupportedTransactionStatusFormats();

        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    void validate_withNotSupportedHeader_shouldReturnValidationError() {
        // Given
        when(aspspProfileServiceWrapper.getSupportedTransactionStatusFormats())
            .thenReturn(Collections.singletonList(JSON_MEDIA_TYPE));

        // When
        ValidationResult validationResult = transactionStatusAcceptHeaderValidator.validate(ACCEPT_HEADER_XML);

        // Then
        verify(aspspProfileServiceWrapper).getSupportedTransactionStatusFormats();

        assertTrue(validationResult.isNotValid());
        assertEquals(VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_multipleAcceptHeaders() {
        // Given
        when(aspspProfileServiceWrapper.getSupportedTransactionStatusFormats())
            .thenReturn(Collections.singletonList(JSON_MEDIA_TYPE));

        // When
        ValidationResult validationResult = transactionStatusAcceptHeaderValidator.validate("application/xml, application/json");

        // Then
        verify(aspspProfileServiceWrapper).getSupportedTransactionStatusFormats();

        assertTrue(validationResult.isValid());
    }

    @Test
    void validate_multipleAcceptHeaders_caseSensitive() {
        // Given
        when(aspspProfileServiceWrapper.getSupportedTransactionStatusFormats())
            .thenReturn(Collections.singletonList(JSON_MEDIA_TYPE));

        // When
        ValidationResult validationResult = transactionStatusAcceptHeaderValidator.validate("   application/xml, application/JSON");

        // Then
        verify(aspspProfileServiceWrapper).getSupportedTransactionStatusFormats();

        assertTrue(validationResult.isValid());
    }
}
