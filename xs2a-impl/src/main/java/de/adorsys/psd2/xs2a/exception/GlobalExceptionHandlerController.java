/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

import de.adorsys.psd2.aspsp.profile.exception.AspspProfileRestException;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.service.discovery.ServiceTypeDiscoveryService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ResponseErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceTypeToErrorTypeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;

import javax.validation.ValidationException;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;

@Slf4j
@RestControllerAdvice(basePackages = "de.adorsys.psd2.xs2a.web.controller")
@RequiredArgsConstructor
public class GlobalExceptionHandlerController {
    private final ResponseErrorMapper responseErrorMapper;
    private final ServiceTypeDiscoveryService serviceTypeDiscoveryService;
    private final ServiceTypeToErrorTypeMapper errorTypeMapper;

    @ExceptionHandler(value = ValidationException.class)
    public ResponseEntity validationException(ValidationException ex, HandlerMethod handlerMethod) {
        log.warn("Validation exception handled in service: {}, message: {}", handlerMethod.getMethod().getDeclaringClass().getSimpleName(), ex.getMessage());
        return responseErrorMapper.generateErrorResponse(createMessageError(FORMAT_ERROR));
    }

    @ExceptionHandler(value = ServletRequestBindingException.class)
    public ResponseEntity servletRequestBindingException(ServletRequestBindingException ex, HandlerMethod handlerMethod) {
        log.warn("Validation exception handled in service: {}, message: {}", handlerMethod.getMethod().getDeclaringClass().getSimpleName(), ex.getMessage());
        return responseErrorMapper.generateErrorResponse(createMessageError(FORMAT_ERROR));
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity illegalArgumentException(IllegalArgumentException ex, HandlerMethod handlerMethod) {
        log.warn("Illegal argument exception handled in: {}, message: {}", handlerMethod.getMethod().getDeclaringClass().getSimpleName(), ex.getMessage());
        log.debug("Stacktrace: {}", ex);
        return responseErrorMapper.generateErrorResponse(createMessageError(FORMAT_ERROR));
    }

    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public ResponseEntity httpMessageException(HttpMessageNotReadableException ex, HandlerMethod handlerMethod) {
        log.warn("Uncatched exception of HttpMessageNotReadableException class handled in Controller: {}, message: {}", handlerMethod.getMethod().getDeclaringClass().getSimpleName(), ex.getMessage());
        return responseErrorMapper.generateErrorResponse(createMessageError(FORMAT_ERROR));
    }

    @ExceptionHandler(value = HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity mediaTypeNotSupportedException(HttpMediaTypeNotAcceptableException ex, HandlerMethod handlerMethod) {
        log.warn("Media type unsupported exception: {}, message: {}", handlerMethod.getMethod().getDeclaringClass().getSimpleName(), ex.getMessage());
        return responseErrorMapper.generateErrorResponse(createMessageError(UNSUPPORTED_MEDIA_TYPE, HttpStatus.UNSUPPORTED_MEDIA_TYPE.getReasonPhrase()));
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity exception(Exception ex, HandlerMethod handlerMethod) {
        log.warn("Uncatched exception handled in Controller: {}, message: {}, stackTrace: {}", handlerMethod.getMethod().getDeclaringClass().getSimpleName(), ex.getMessage(), ex);
        return responseErrorMapper.generateErrorResponse(createMessageError(INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()));
    }

    @ExceptionHandler(value = RestException.class)
    public ResponseEntity restException(RestException ex, HandlerMethod handlerMethod) {
        log.warn("RestException handled in service: {}, message: {}", handlerMethod.getMethod().getDeclaringClass().getSimpleName(), ex.getMessage());
        log.debug("Stacktrace: {}", ex);
        return responseErrorMapper.generateErrorResponse(createMessageError(ex.getMessageErrorCode()));
    }

    @ExceptionHandler(value = AspspProfileRestException.class)
    public ResponseEntity aspspProfileRestException(AspspProfileRestException ex, HandlerMethod handlerMethod) {
        log.warn("RestException handled in service: {}, message: {}", handlerMethod.getMethod().getDeclaringClass().getSimpleName(), ex.getMessage());
        log.debug("Stacktrace: {}", ex);
        return responseErrorMapper.generateErrorResponse(createMessageError(INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity requestBodyValidationException(MethodArgumentNotValidException ex, HandlerMethod handlerMethod) {
        log.warn("RequestBodyValidationException handled in controller: {}, message: {} ", handlerMethod.getMethod().getDeclaringClass().getSimpleName(), ex.getMessage());
        log.debug("Stacktrace: {}", ex);
        return responseErrorMapper.generateErrorResponse(createMessageError(FORMAT_ERROR));
    }

    @ExceptionHandler(value = CertificateException.class)
    public ResponseEntity getTppIdException(CertificateException ex, HandlerMethod handlerMethod) {
        log.warn("Can't find tpp id in SecurityContextHolder in: {}, message: {}", handlerMethod.getMethod().getDeclaringClass().getSimpleName(), ex.getMessage());
        log.debug("Stacktrace: {}", ex);
        return responseErrorMapper.generateErrorResponse(createMessageError(CERTIFICATE_INVALID));
    }

    private MessageError createMessageError(MessageErrorCode messageErrorCode) {
        return new MessageError(
            errorTypeMapper.mapToErrorType(serviceTypeDiscoveryService.getServiceType(), messageErrorCode.getCode()), of(messageErrorCode)
        );
    }

    private MessageError createMessageError(MessageErrorCode messageErrorCode, String message) {
        return new MessageError(
            errorTypeMapper.mapToErrorType(serviceTypeDiscoveryService.getServiceType(), messageErrorCode.getCode()), of(messageErrorCode, message)
        );
    }
}
