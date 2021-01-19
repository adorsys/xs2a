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

package de.adorsys.psd2.xs2a.service.validator.ais.consent;

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.SCA_INVALID;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.STATUS_INVALID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AisAuthorisationStatusValidatorTest {
    private static final MessageError STATUS_VALIDATION_ERROR =
        new MessageError(ErrorType.AIS_409, TppMessageInformation.of(STATUS_INVALID));
    private static final MessageError SCA_INVALID_ERROR =
        new MessageError(ErrorType.AIS_400, TppMessageInformation.of(SCA_INVALID));

    @InjectMocks
    private AisAuthorisationStatusValidator aisAuthorisationStatusValidator;

    @Mock
    private AspspProfileServiceWrapper aspspProfileService;

    @Test
    void validate_withValidStatus_shouldReturnValid() {
        // When
        ValidationResult validationResult = aisAuthorisationStatusValidator.validate(ScaStatus.RECEIVED, false);

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    void validate_withFailedStatus_shouldReturnError() {
        // When
        ValidationResult validationResult = aisAuthorisationStatusValidator.validate(ScaStatus.FAILED, false);

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(STATUS_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withFailedStatusAndAuthorisationConfirmationMandated_shouldReturnError() {
        // When
        when(aspspProfileService.isAuthorisationConfirmationRequestMandated()).thenReturn(true);
        ValidationResult validationResult = aisAuthorisationStatusValidator.validate(ScaStatus.FAILED, true);

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(SCA_INVALID_ERROR, validationResult.getMessageError());
    }
}
