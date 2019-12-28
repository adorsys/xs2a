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

package de.adorsys.psd2.xs2a.service.mapper;

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ValidationResultMapperTest {
    private ValidationResultMapper validationResultMapper = new ValidationResultMapper();

    @Test
    public void mapToValidationResult_valid() {
        // Given
        ValidationResult xs2aValidationResult = ValidationResult.valid();

        // When
        de.adorsys.psd2.xs2a.core.service.validator.ValidationResult actual = validationResultMapper.mapToValidationResult(xs2aValidationResult);

        // Then
        assertEquals(de.adorsys.psd2.xs2a.core.service.validator.ValidationResult.valid(), actual);
    }

    @Test
    public void mapToValidationResult_invalid_messageErrors() {
        // Given
        MessageError messageError = new MessageError(ErrorType.AIS_400, TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR));
        ValidationResult xs2aValidationResult = ValidationResult.invalid(messageError);

        // When
        de.adorsys.psd2.xs2a.core.service.validator.ValidationResult actual = validationResultMapper.mapToValidationResult(xs2aValidationResult);

        // Then
        assertEquals(de.adorsys.psd2.xs2a.core.service.validator.ValidationResult.invalid(messageError), actual);
    }

    @Test
    public void mapToValidationResult_invalid_errorTypeAndCode() {
        // Given
        ErrorType errorType = ErrorType.AIS_400;
        MessageErrorCode errorCode = MessageErrorCode.FORMAT_ERROR;
        ValidationResult xs2aValidationResult = ValidationResult.invalid(errorType, errorCode);

        // When
        de.adorsys.psd2.xs2a.core.service.validator.ValidationResult actual = validationResultMapper.mapToValidationResult(xs2aValidationResult);

        // Then
        assertEquals(de.adorsys.psd2.xs2a.core.service.validator.ValidationResult.invalid(errorType, errorCode), actual);
    }

    @Test
    public void mapToValidationResult_invalid_errorTypeAndTppMessageInformation() {
        // Given
        ErrorType errorType = ErrorType.AIS_400;
        TppMessageInformation tppMessageInformation = TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR);
        ValidationResult xs2aValidationResult = ValidationResult.invalid(errorType, tppMessageInformation);

        // When
        de.adorsys.psd2.xs2a.core.service.validator.ValidationResult actual = validationResultMapper.mapToValidationResult(xs2aValidationResult);

        // Then
        assertEquals(de.adorsys.psd2.xs2a.core.service.validator.ValidationResult.invalid(errorType, tppMessageInformation), actual);
    }
}
