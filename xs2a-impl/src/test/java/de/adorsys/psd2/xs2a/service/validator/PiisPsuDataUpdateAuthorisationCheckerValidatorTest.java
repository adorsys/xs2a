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
