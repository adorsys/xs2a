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

package de.adorsys.psd2.xs2a.web.interceptor;

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.web.validator.DefaultMethodValidatorImpl;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.MethodValidator;
import de.adorsys.psd2.xs2a.web.validator.MethodValidatorController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestValidationInterceptorTest {
    private static final String METHOD_NAME = "publicMethod";

    @InjectMocks
    private RequestValidationInterceptor interceptor;

    @Mock
    private DefaultMethodValidatorImpl defaultMethodValidator;
    @Mock
    private ErrorBuildingService errorBuildingService;
    @Spy
    private MethodValidatorController methodValidatorController = new MethodValidatorController(new ArrayList<>(), defaultMethodValidator);
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HandlerMethod handler;

    private MethodValidator methodValidator;

    @BeforeEach
    void setUp() {
        methodValidator = new MethodValidator() {
            @Override
            public String getMethodName() {
                return METHOD_NAME;
            }

            @Override
            public MessageError validate(HttpServletRequest request, MessageError messageError) {
                messageError.addTppMessage(TppMessageInformation.of(FORMAT_ERROR));
                return messageError;
            }
        };
    }

    public String publicMethod() {
        return "OK";
    }

    @Test
    void preHandle_instanceOf_incorrectFormat() throws IOException {
        assertTrue(interceptor.preHandle(request, response, null));

        verify(errorBuildingService, never()).buildErrorResponse(eq(response), any(MessageError.class));
    }

    @Test
    void preHandle_methodValidator_isNotPresent() throws IOException, NoSuchMethodException {
        Method method = getClass().getDeclaredMethod(METHOD_NAME);
        when(handler.getMethod()).thenReturn(method);
        when(methodValidatorController.getMethod(anyString())).thenReturn(defaultMethodValidator);
        when(defaultMethodValidator.validate(any(), any())).thenReturn(new MessageError());

        assertTrue(interceptor.preHandle(request, response, handler));

        verify(errorBuildingService, never()).buildErrorResponse(eq(response), any(MessageError.class));
    }

    @Test
    void preHandle_methodValidator_hasError() throws IOException, NoSuchMethodException {
        ArgumentCaptor<MessageError> messageErrorCaptor = ArgumentCaptor.forClass(MessageError.class);
        Method method = getClass().getDeclaredMethod(METHOD_NAME);
        when(handler.getMethod()).thenReturn(method);
        when(methodValidatorController.getMethod(METHOD_NAME)).thenReturn(methodValidator);


        assertFalse(interceptor.preHandle(request, response, handler));

        verify(errorBuildingService, times(1)).buildErrorResponse(eq(response), messageErrorCaptor.capture());
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageErrorCaptor.getValue().getTppMessage().getMessageErrorCode());
    }
}
