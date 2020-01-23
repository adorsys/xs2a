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

package de.adorsys.psd2.xs2a.web.validator.header;

import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_BOOLEAN_VALUE;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbstractHeaderValidatorImplTest {

    @Mock
    private ErrorBuildingService errorBuildingService;
    private TppRedirectPreferredHeaderValidatorImpl validator;
    private MessageError messageError;
    private Map<String, String> headers;

    @BeforeEach
    void setUp() {
        validator = new TppRedirectPreferredHeaderValidatorImpl(errorBuildingService);
        messageError = new MessageError();
        headers = new HashMap<>();
    }

    @Test
    void checkBooleanFormat_headerIsNotPresented() {
        validator.checkBooleanFormat(headers, messageError);

        verify(errorBuildingService, never()).enrichMessageError(any(MessageError.class), any(TppMessageInformation.class));
    }

    @Test
    void checkBooleanFormat_success() {
        headers.put(validator.getHeaderName(), "true");
        validator.checkBooleanFormat(headers, messageError);

        verify(errorBuildingService, never()).enrichMessageError(any(MessageError.class), any(TppMessageInformation.class));
        reset(errorBuildingService);

        validator = new TppRedirectPreferredHeaderValidatorImpl(errorBuildingService);
        headers.put(validator.getHeaderName(), "FaLsE");
        validator.checkBooleanFormat(headers, messageError);

        verify(errorBuildingService, never()).enrichMessageError(any(MessageError.class), any(TppMessageInformation.class));
        reset(errorBuildingService);
    }

    @Test
    void checkBooleanFormat_error() {
        headers.put(validator.getHeaderName(), "wrong_format");
        validator.checkBooleanFormat(headers, messageError);

        verify(errorBuildingService, times(1)).enrichMessageError(eq(messageError),
                                                                  eq(TppMessageInformation.of(FORMAT_ERROR_BOOLEAN_VALUE, validator.getHeaderName())));
    }
}
