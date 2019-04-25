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

package de.adorsys.psd2.xs2a.web.validator.header;

import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static de.adorsys.psd2.xs2a.web.validator.header.AbstractHeaderValidatorImpl.ERROR_TEXT_BOOLEAN_FORMAT;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AbstractHeaderValidatorImplTest {

    @Mock
    private ErrorBuildingService errorBuildingService;
    private TppRedirectPreferredHeaderValidatorImpl validator;
    private MessageError messageError;
    private Map<String, String> headers;

    @Before
    public void setUp() {
        validator = new TppRedirectPreferredHeaderValidatorImpl(errorBuildingService);
        messageError = new MessageError();
        headers = new HashMap<>();
    }

    @Test
    public void checkBooleanFormat_headerIsNotPresented() {
        validator.checkBooleanFormat(headers, messageError);

        verify(errorBuildingService, never()).enrichMessageError(any(MessageError.class), any(String.class));
    }

    @Test
    public void checkBooleanFormat_success() {
        headers.put(validator.getHeaderName(), "true");
        validator.checkBooleanFormat(headers, messageError);

        verify(errorBuildingService, never()).enrichMessageError(any(MessageError.class), any(String.class));
        reset(errorBuildingService);

        validator = new TppRedirectPreferredHeaderValidatorImpl(errorBuildingService);
        headers.put(validator.getHeaderName(), "FaLsE");
        validator.checkBooleanFormat(headers, messageError);

        verify(errorBuildingService, never()).enrichMessageError(any(MessageError.class), any(String.class));
        reset(errorBuildingService);
    }

    @Test
    public void checkBooleanFormat_error() {
        headers.put(validator.getHeaderName(), "wrong_format");
        validator.checkBooleanFormat(headers, messageError);

        verify(errorBuildingService, times(1)).enrichMessageError(eq(messageError),
                                                                  eq(String.format(ERROR_TEXT_BOOLEAN_FORMAT, validator.getHeaderName())));
    }
}
