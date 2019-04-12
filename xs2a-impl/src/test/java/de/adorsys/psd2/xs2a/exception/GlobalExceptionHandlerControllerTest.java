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

package de.adorsys.psd2.xs2a.exception;

import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.service.discovery.ServiceTypeDiscoveryService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ResponseErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceTypeToErrorTypeMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GlobalExceptionHandlerControllerTest {
    @Mock
    private ResponseErrorMapper responseErrorMapper;
    @Mock
    private ServiceTypeDiscoveryService serviceTypeDiscoveryService;
    @Mock
    private ServiceTypeToErrorTypeMapper errorTypeMapper;
    @Mock
    private HandlerMethod handlerMethod;

    @InjectMocks
    private GlobalExceptionHandlerController globalExceptionHandlerController;

    @Test
    public void methodArgumentTypeMismatchException_shouldReturnFormatError() throws NoSuchMethodException {
        // Given
        when(serviceTypeDiscoveryService.getServiceType())
            .thenReturn(ServiceType.AIS);
        when(errorTypeMapper.mapToErrorType(ServiceType.AIS, 400))
            .thenReturn(ErrorType.AIS_400);
        when(handlerMethod.getMethod()).thenReturn(Object.class.getMethod("toString"));

        MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(new Object(), LocalDate.class, null, null, null);

        // When
        globalExceptionHandlerController.methodArgumentTypeMismatchException(exception, handlerMethod);

        // Then
        verify(errorTypeMapper).mapToErrorType(ServiceType.AIS, 400);
        verify(responseErrorMapper).generateErrorResponse(new MessageError(ErrorType.AIS_400, TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR)));
    }
}
