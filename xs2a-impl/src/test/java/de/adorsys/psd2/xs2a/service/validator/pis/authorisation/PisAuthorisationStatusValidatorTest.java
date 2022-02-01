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

package de.adorsys.psd2.xs2a.service.validator.pis.authorisation;

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
class PisAuthorisationStatusValidatorTest {
    private static final MessageError STATUS_VALIDATION_ERROR =
        new MessageError(ErrorType.PIS_409, TppMessageInformation.of(STATUS_INVALID));
    private static final MessageError SCA_INVALID_ERROR =
        new MessageError(ErrorType.PIS_400, TppMessageInformation.of(SCA_INVALID));

    @InjectMocks
    private PisAuthorisationStatusValidator pisAuthorisationValidator;

    @Mock
    private AspspProfileServiceWrapper aspspProfileService;

    @Test
    void validate_withValidStatus_shouldReturnValid() {
        // When
        ValidationResult validationResult = pisAuthorisationValidator.validate(ScaStatus.RECEIVED, false);

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    void validate_withFailedStatus_shouldReturnError() {
        // When
        ValidationResult validationResult = pisAuthorisationValidator.validate(ScaStatus.FAILED, false);

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(STATUS_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withFailedStatusAndAuthorisationConfirmationMandated_shouldReturnError() {
        // When
        when(aspspProfileService.isAuthorisationConfirmationRequestMandated()).thenReturn(true);
        ValidationResult validationResult = pisAuthorisationValidator.validate(ScaStatus.FAILED, true);

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(SCA_INVALID_ERROR, validationResult.getMessageError());
    }
}
