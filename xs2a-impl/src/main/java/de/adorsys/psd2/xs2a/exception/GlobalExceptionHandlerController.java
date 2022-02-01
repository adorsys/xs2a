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

package de.adorsys.psd2.xs2a.exception;

import de.adorsys.psd2.aspsp.profile.exception.AspspProfileRestException;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.service.discovery.ServiceTypeDiscoveryService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ResponseErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceTypeToErrorTypeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ValidationException;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.buildWithCustomError;
import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;

@Slf4j
@SuppressWarnings("rawtypes")
@RestControllerAdvice(basePackages = "de.adorsys.psd2.xs2a.web.controller")
@RequiredArgsConstructor
public class GlobalExceptionHandlerController {
    public static final String STACKTRACE_LOG = "Stacktrace: {}";
    private final ResponseErrorMapper responseErrorMapper;
    private final ServiceTypeDiscoveryService serviceTypeDiscoveryService;
    private final ServiceTypeToErrorTypeMapper errorTypeMapper;

    @ExceptionHandler(value = ValidationException.class)
    public ResponseEntity validationException(ValidationException ex, HandlerMethod handlerMethod) {
        log.warn("Validation exception handled in service: {}, message: {}",
            handlerMethod.getMethod().getDeclaringClass().getSimpleName(), ex.getMessage());
        return responseErrorMapper.generateErrorResponse(createMessageError(FORMAT_ERROR));
    }

    @ExceptionHandler(value = ServletRequestBindingException.class)
    public ResponseEntity servletRequestBindingException(ServletRequestBindingException ex,
                                                         HandlerMethod handlerMethod) {
        log.warn("Validation exception handled in service: {}, message: {}",
            handlerMethod.getMethod().getDeclaringClass().getSimpleName(), ex.getMessage());
        return responseErrorMapper.generateErrorResponse(createMessageError(FORMAT_ERROR));
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity illegalArgumentException(IllegalArgumentException ex, HandlerMethod handlerMethod) {
        log.warn("Illegal argument exception handled in: {}, message: {}",
            handlerMethod.getMethod().getDeclaringClass().getSimpleName(), ex.getMessage());
        log.debug(STACKTRACE_LOG, ex.toString());
        return responseErrorMapper.generateErrorResponse(createMessageError(FORMAT_ERROR));
    }

    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public ResponseEntity httpMessageException(HttpMessageNotReadableException ex, HandlerMethod handlerMethod) {
        log.warn("Uncatched exception of HttpMessageNotReadableException class handled in Controller: {}, message: " +
            "{}", handlerMethod.getMethod().getDeclaringClass().getSimpleName(), ex.getMessage());
        return responseErrorMapper.generateErrorResponse(createMessageError(FORMAT_ERROR));
    }

    @ExceptionHandler(value = HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity mediaTypeNotSupportedException(HttpMediaTypeNotAcceptableException ex,
                                                         HandlerMethod handlerMethod) {
        log.warn("Media type unsupported exception: {}, message: {}",
            handlerMethod.getMethod().getDeclaringClass().getSimpleName(), ex.getMessage());
        return responseErrorMapper.generateErrorResponse(createMessageError(UNSUPPORTED_MEDIA_TYPE));
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity exception(Exception ex, HandlerMethod handlerMethod) {
        log.warn("Uncatched exception handled in Controller: {}, message: {}, stackTrace: {}",
            handlerMethod.getMethod().getDeclaringClass().getSimpleName(), ex.getMessage(), ex);
        return responseErrorMapper.generateErrorResponse(createMessageError(INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(value = RestException.class)
    public ResponseEntity restException(RestException ex, HandlerMethod handlerMethod) {
        log.warn("RestException handled in service: {}, message: {}",
            handlerMethod.getMethod().getDeclaringClass().getSimpleName(), ex.getMessage());
        log.debug(STACKTRACE_LOG, ex.toString());
        return responseErrorMapper.generateErrorResponse(createMessageError(ex.getMessageErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(value = AspspProfileRestException.class)
    public ResponseEntity aspspProfileRestException(AspspProfileRestException ex, HandlerMethod handlerMethod) {
        log.warn("RestException handled in service: {}, message: {}",
            handlerMethod.getMethod().getDeclaringClass().getSimpleName(), ex.getMessage());
        log.debug(STACKTRACE_LOG, ex.toString());
        return responseErrorMapper.generateErrorResponse(createMessageError(INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(value = ResourceAccessException.class)
    public ResponseEntity resourceAccessException(ResourceAccessException ex, HandlerMethod handlerMethod) {
        log.warn("ResourceAccessException handled in service: {}, message: {}",
                 handlerMethod.getMethod().getDeclaringClass().getSimpleName(), ex.getMessage());
        log.debug(STACKTRACE_LOG, ex.toString());
        return responseErrorMapper.generateServiceUnavailableErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity requestBodyValidationException(MethodArgumentNotValidException ex,
                                                         HandlerMethod handlerMethod) {
        log.warn("RequestBodyValidationException handled in controller: {}, message: {} ",
            handlerMethod.getMethod().getDeclaringClass().getSimpleName(), ex.getMessage());
        log.debug(STACKTRACE_LOG, ex.toString());
        return responseErrorMapper.generateErrorResponse(createMessageError(FORMAT_ERROR));
    }

    @ExceptionHandler(value = CertificateException.class)
    public ResponseEntity getTppIdException(CertificateException ex, HandlerMethod handlerMethod) {
        log.warn("Can't find tpp id in SecurityContextHolder in: {}, message: {}",
            handlerMethod.getMethod().getDeclaringClass().getSimpleName(), ex.getMessage());
        log.debug(STACKTRACE_LOG, ex.toString());
        return responseErrorMapper.generateErrorResponse(createMessageError(CERTIFICATE_INVALID));
    }

    @ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
    public ResponseEntity methodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex,
                                                              HandlerMethod handlerMethod) {
        log.warn("MethodArgumentTypeMismatchException handled in service: {}, message: {}",
            handlerMethod.getMethod().getDeclaringClass().getSimpleName(), ex.getMessage());
        return responseErrorMapper.generateErrorResponse(createMessageError(FORMAT_ERROR));
    }

    @ExceptionHandler(value = WrongPaymentTypeException.class)
    public ResponseEntity wrongPaymentTypeException(WrongPaymentTypeException ex,
                                                              HandlerMethod handlerMethod) {
        log.warn("WrongPaymentTypeException handled in service: {}, message: {}",
                 handlerMethod.getMethod().getDeclaringClass().getSimpleName(), ex.getMessage());
        MessageError messageError = new MessageError(ErrorType.PIS_400, TppMessageInformation.of(PARAMETER_NOT_SUPPORTED_WRONG_PAYMENT_TYPE, ex.getPaymentType()));
        return responseErrorMapper.generateErrorResponse(messageError);
    }

    private MessageError createMessageError(MessageErrorCode messageErrorCode) {
        return new MessageError(getErrorType(messageErrorCode.getCode()), of(messageErrorCode));
    }

    private MessageError createMessageError(MessageErrorCode messageErrorCode, String message) {
        return new MessageError(getErrorType(messageErrorCode.getCode()), buildWithCustomError(messageErrorCode, message));
    }

    private ErrorType getErrorType(int code) {
        return errorTypeMapper.mapToErrorType(serviceTypeDiscoveryService.getServiceType(), code);
    }
}
