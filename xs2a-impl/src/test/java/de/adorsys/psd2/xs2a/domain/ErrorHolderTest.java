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
