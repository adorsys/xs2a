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

package de.adorsys.psd2.xs2a.domain;

import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.EXECUTION_DATE_INVALID;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.PAYMENT_FAILED;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ErrorHolderTest {
    private static final ErrorType ERROR_TYPE = ErrorType.PIS_400;

    private static final TppMessageInformation TPP_MESSAGE_INFORMATION = TppMessageInformation.of(PAYMENT_FAILED);

    @Test
    void build() {
        TppMessageInformation tppMessage = TppMessageInformation.of(PAYMENT_FAILED);
        TppMessageInformation anotherTppMessage = TppMessageInformation.of(EXECUTION_DATE_INVALID);

        ErrorHolder errorHolder = ErrorHolder.builder(ERROR_TYPE)
                                      .tppMessages(tppMessage, anotherTppMessage)
                                      .build();

        assertEquals(ERROR_TYPE, errorHolder.getErrorType());
        assertEquals(Arrays.asList(tppMessage, anotherTppMessage), errorHolder.getTppMessageInformationList());
        assertEquals(PAYMENT_FAILED.getCode(), errorHolder.getErrorType().getErrorCode());
    }

    @Test
    void build_withMultipleTppMessages() {
        TppMessageInformation tppMessageInformation = TppMessageInformation.of(PAYMENT_FAILED);

        ErrorHolder build = ErrorHolder
                                .builder(ERROR_TYPE)
                                .tppMessages(TPP_MESSAGE_INFORMATION)
                                .build();

        assertEquals(ERROR_TYPE, build.getErrorType());
        assertEquals(Collections.singletonList(tppMessageInformation), build.getTppMessageInformationList());
        assertEquals(PAYMENT_FAILED.getCode(), build.getErrorType().getErrorCode());
    }
}
