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

package de.adorsys.psd2.xs2a.exception;

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.service.discovery.ServiceTypeDiscoveryService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ResponseErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceTypeToErrorTypeMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDate;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.PARAMETER_NOT_SUPPORTED_WRONG_PAYMENT_TYPE;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerControllerTest {
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
    void methodArgumentTypeMismatchException_shouldReturnFormatError() throws NoSuchMethodException {
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

    @Test
    void wrongPaymentTypeException_shouldReturnParameterNotSupported() throws NoSuchMethodException {
        // Given
        when(handlerMethod.getMethod()).thenReturn(Object.class.getMethod("toString"));
        String paymentType = "wrong payment type";
        WrongPaymentTypeException exception = new WrongPaymentTypeException(paymentType);
        // When
        globalExceptionHandlerController.wrongPaymentTypeException(exception, handlerMethod);
        // Then
        MessageError messageError = new MessageError(ErrorType.PIS_400, TppMessageInformation.of(PARAMETER_NOT_SUPPORTED_WRONG_PAYMENT_TYPE, paymentType));
        verify(responseErrorMapper).generateErrorResponse(messageError);
    }

    @Test
    void methodRestException_shouldReturnErrorWithMessage() throws NoSuchMethodException {
        //Given
        ErrorType errorType = ErrorType.AIS_400;
        MessageErrorCode messageErrorCode = MessageErrorCode.FORMAT_ERROR;
        String restExceptionMessage = "restExceptionMessage";
        when(serviceTypeDiscoveryService.getServiceType()).thenReturn(ServiceType.AIS);
        when(errorTypeMapper.mapToErrorType(ServiceType.AIS, 400)).thenReturn(errorType);
        when(handlerMethod.getMethod()).thenReturn(Object.class.getMethod("toString"));
        RestException restException = new RestException(messageErrorCode, restExceptionMessage);
        //When
        globalExceptionHandlerController.restException(restException, handlerMethod);
        //Then
        verify(errorTypeMapper).mapToErrorType(ServiceType.AIS, 400);
        verify(responseErrorMapper).generateErrorResponse(new MessageError(errorType, TppMessageInformation.buildWithCustomError(messageErrorCode, restExceptionMessage)));
    }
}
