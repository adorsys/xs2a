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

import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_NO_PSU;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.PSU_CREDENTIALS_INVALID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AisPsuDataUpdateAuthorisationCheckerValidatorTest {
    @Mock
    private PsuDataUpdateAuthorisationChecker psuDataUpdateAuthorisationChecker;
    @InjectMocks
    private AisPsuDataUpdateAuthorisationCheckerValidator aisPsuDataUpdateAuthorisationCheckerValidator;

    private static final PsuIdData EMPTY_PSU = new PsuIdData(null, null, null, null, null);
    private static final PsuIdData PSU_ID_DATA_1 = new PsuIdData("psu-id", null, null, null, null);
    private static final PsuIdData PSU_ID_DATA_2 = new PsuIdData("psu-id-2", null, null, null, null);

    private static final MessageError FORMAT_BOTH_PSUS_ABSENT_ERROR = new MessageError(ErrorType.AIS_400, of(FORMAT_ERROR_NO_PSU));
    private static final MessageError CREDENTIALS_INVALID_ERROR = new MessageError(ErrorType.AIS_401, of(PSU_CREDENTIALS_INVALID));


    @Test
    void validate_withBothPsusAbsent_shouldReturnFormatError() {
        //Given
        when(psuDataUpdateAuthorisationChecker.areBothPsusAbsent(EMPTY_PSU, null))
            .thenReturn(true);

        //When
        ValidationResult validationResult = aisPsuDataUpdateAuthorisationCheckerValidator.validate(EMPTY_PSU, null);

        //Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(FORMAT_BOTH_PSUS_ABSENT_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_cantPsuUpdateAuthorisation_shouldReturnCredentialsInvalidError() {
        //When
        ValidationResult validationResult = aisPsuDataUpdateAuthorisationCheckerValidator.validate(PSU_ID_DATA_1, PSU_ID_DATA_2);

        //Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(CREDENTIALS_INVALID_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_successful() {
        when(psuDataUpdateAuthorisationChecker.canPsuUpdateAuthorisation(PSU_ID_DATA_1, PSU_ID_DATA_1))
            .thenReturn(true);

        ValidationResult validationResult = aisPsuDataUpdateAuthorisationCheckerValidator.validate(PSU_ID_DATA_1, PSU_ID_DATA_1);

        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }
}
