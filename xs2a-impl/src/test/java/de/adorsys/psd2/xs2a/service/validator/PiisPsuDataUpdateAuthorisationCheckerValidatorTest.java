/*
 * Copyright 2018-2021 adorsys GmbH & Co KG
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.adorsys.psd2.xs2a.core.domain.MessageCategory.ERROR;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_NO_PSU;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.PSU_CREDENTIALS_INVALID;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PiisPsuDataUpdateAuthorisationCheckerValidatorTest {

    @InjectMocks
    private PiisPsuDataUpdateAuthorisationCheckerValidator validator;

    @Test
    void getMessageErrorAreBothPsusAbsent() {
        //When
        MessageError actual = validator.getMessageErrorAreBothPsusAbsent();

        //Then
        assertThat(actual.getErrorType()).isEqualTo(ErrorType.PIIS_400);
        assertThat(actual.getTppMessage().getMessageErrorCode()).isEqualTo(FORMAT_ERROR_NO_PSU);
        assertThat(actual.getTppMessage().getCategory()).isEqualTo(ERROR);
    }

    @Test
    void getMessageErrorCanPsuUpdateAuthorisation() {
        //When
        MessageError actual = validator.getMessageErrorCanPsuUpdateAuthorisation();

        //Then
        assertThat(actual.getErrorType()).isEqualTo(ErrorType.PIIS_401);
        assertThat(actual.getTppMessage().getMessageErrorCode()).isEqualTo(PSU_CREDENTIALS_INVALID);
        assertThat(actual.getTppMessage().getCategory()).isEqualTo(ERROR);
    }
}
