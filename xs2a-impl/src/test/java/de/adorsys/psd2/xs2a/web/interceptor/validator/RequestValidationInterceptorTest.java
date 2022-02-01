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

package de.adorsys.psd2.xs2a.web.interceptor.validator;

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

        verify(errorBuildingService, never()).buildFormatErrorResponse(eq(response), any(MessageError.class));
    }

    @Test
    void preHandle_methodValidator_isNotPresent() throws IOException, NoSuchMethodException {
        Method method = getClass().getDeclaredMethod(METHOD_NAME);
        when(handler.getMethod()).thenReturn(method);
        when(methodValidatorController.getMethod(anyString())).thenReturn(defaultMethodValidator);
        when(defaultMethodValidator.validate(any(), any())).thenReturn(new MessageError());

        assertTrue(interceptor.preHandle(request, response, handler));

        verify(errorBuildingService, never()).buildFormatErrorResponse(eq(response), any(MessageError.class));
    }

    @Test
    void preHandle_methodValidator_hasError() throws IOException, NoSuchMethodException {
        ArgumentCaptor<MessageError> messageErrorCaptor = ArgumentCaptor.forClass(MessageError.class);
        Method method = getClass().getDeclaredMethod(METHOD_NAME);
        when(handler.getMethod()).thenReturn(method);
        when(methodValidatorController.getMethod(METHOD_NAME)).thenReturn(methodValidator);


        assertFalse(interceptor.preHandle(request, response, handler));

        verify(errorBuildingService, times(1)).buildFormatErrorResponse(eq(response), messageErrorCaptor.capture());
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageErrorCaptor.getValue().getTppMessage().getMessageErrorCode());
    }
}
