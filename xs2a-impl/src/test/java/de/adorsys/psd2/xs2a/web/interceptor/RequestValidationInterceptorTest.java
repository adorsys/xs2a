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

package de.adorsys.psd2.xs2a.web.interceptor;

import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.MethodValidator;
import de.adorsys.psd2.xs2a.web.validator.MethodValidatorController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.FORMAT_ERROR;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RequestValidationInterceptorTest {

    private static final String METHOD_NAME = "publicMethod";
    private static final String ERROR_MESSAGE = "error_message";

    @InjectMocks
    private RequestValidationInterceptor interceptor;

    @Mock
    private ErrorBuildingService errorBuildingService;
    @Spy
    private MethodValidatorController methodValidatorController = new MethodValidatorController(new ArrayList<>());
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HandlerMethod handler;

    private MethodValidator methodValidator;

    @Before
    public void setUp() {
        methodValidator = new MethodValidator() {
            @Override
            public String getMethodName() {
                return METHOD_NAME;
            }

            @Override
            public void validate(HttpServletRequest request, MessageError messageError) {
                messageError.addTppMessage(TppMessageInformation.of(FORMAT_ERROR, ERROR_MESSAGE));
            }
        };
    }

    public String publicMethod() {
        return "OK";
    }

    @Test
    public void preHandle_instanceOf_incorrectFormat() throws IOException {
        assertTrue(interceptor.preHandle(request, response, null));

        verify(errorBuildingService, never()).buildErrorResponse(eq(response), any(MessageError.class));
    }

    @Test
    public void preHandle_methodValidator_isNotPresent() throws IOException, NoSuchMethodException {
        Method method = getClass().getDeclaredMethod(METHOD_NAME);
        when(handler.getMethod()).thenReturn(method);

        assertTrue(interceptor.preHandle(request, response, handler));

        verify(errorBuildingService, never()).buildErrorResponse(eq(response), any(MessageError.class));
    }

    @Test
    public void preHandle_methodValidator_hasError() throws IOException, NoSuchMethodException {
        ArgumentCaptor<MessageError> messageErrorCaptor = ArgumentCaptor.forClass(MessageError.class);
        Method method = getClass().getDeclaredMethod(METHOD_NAME);
        when(handler.getMethod()).thenReturn(method);
        when(methodValidatorController.getMethod(METHOD_NAME)).thenReturn(Optional.of(methodValidator));


        assertFalse(interceptor.preHandle(request, response, handler));

        verify(errorBuildingService, times(1)).buildErrorResponse(eq(response), messageErrorCaptor.capture());
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageErrorCaptor.getValue().getTppMessage().getMessageErrorCode());
        assertEquals(ERROR_MESSAGE, messageErrorCaptor.getValue().getTppMessage().getText());
    }
}
