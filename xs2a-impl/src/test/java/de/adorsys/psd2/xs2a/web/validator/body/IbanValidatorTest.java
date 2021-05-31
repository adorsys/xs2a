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

package de.adorsys.psd2.xs2a.web.validator.body;

import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.header.ErrorBuildingServiceMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IbanValidatorTest {

    private IbanValidator validator;
    private MessageError messageError;
    @Mock
    private AspspProfileServiceWrapper aspspProfileService;

    @BeforeEach
    void setUp() {
        messageError = new MessageError();
        ErrorBuildingService errorService = new ErrorBuildingServiceMock(ErrorType.PIS_400);
        validator = new IbanValidator(aspspProfileService, errorService);
    }

    @Test
    void validate_success() {
        // Given
        when(aspspProfileService.isIbanValidationDisabled()).thenReturn(false);

        // When
        validator.validate("DE15500105172295759744", messageError);

        // Then
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    void validate_invalidIban() {
        // Given
        when(aspspProfileService.isIbanValidationDisabled()).thenReturn(false);

        // When
        validator.validate("123", messageError);

        // Then
        assertFalse(messageError.getTppMessages().isEmpty());
        assertEquals(MessageErrorCode.FORMAT_ERROR_INVALID_FIELD, messageError.getTppMessage().getMessageErrorCode());
    }

    @Test
    void validate_validationDisabled() {
        // Given
        when(aspspProfileService.isIbanValidationDisabled()).thenReturn(true);

        // When
        validator.validate("123", messageError);

        // Then
        assertTrue(messageError.getTppMessages().isEmpty());
    }
}
