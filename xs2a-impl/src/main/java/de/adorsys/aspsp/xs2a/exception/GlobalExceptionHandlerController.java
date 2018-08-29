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

package de.adorsys.aspsp.xs2a.exception;

import de.adorsys.aspsp.xs2a.domain.MessageErrorCode;
import de.adorsys.aspsp.xs2a.domain.TppMessageInformation;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.service.message.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;

import javax.validation.ValidationException;

import static de.adorsys.aspsp.xs2a.domain.MessageErrorCode.FORMAT_ERROR;
import static de.adorsys.aspsp.xs2a.exception.MessageCategory.ERROR;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandlerController {
    private MessageService messageService;

    @Autowired
    public GlobalExceptionHandlerController(MessageService messageService) {
        this.messageService = messageService;
    }

    @ExceptionHandler(value = {ValidationException.class})
    public ResponseEntity validationException(ValidationException ex, HandlerMethod handlerMethod) {
        log.warn("ValidationException handled in service: {}, message: {}", handlerMethod.getMethod().getDeclaringClass().getSimpleName(), ex.getMessage());
        return new ResponseEntity<>(getMessageError(FORMAT_ERROR), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {HttpMessageNotReadableException.class})
    public ResponseEntity httpMessageException(HttpMessageNotReadableException ex, HandlerMethod handlerMethod) {
        log.warn("Uncatched exception handled in Controller: {}, message: {}", handlerMethod.getMethod().getDeclaringClass().getSimpleName(), ex.getMessage());
        return new ResponseEntity<>(getMessageError(FORMAT_ERROR), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity mediaTypeNotSupportedException(HttpMediaTypeNotAcceptableException ex, HandlerMethod handlerMethod) {
        log.warn("Media type unsupported exception: {}, message: {}", handlerMethod.getMethod().getDeclaringClass().getSimpleName(), ex.getMessage());
        return new ResponseEntity<>(UNSUPPORTED_MEDIA_TYPE.getReasonPhrase(), UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity exception(Exception ex, HandlerMethod handlerMethod) {
        log.warn("Uncatched exception handled in Controller: {}, message: {}, stackTrace: {}", handlerMethod.getMethod().getDeclaringClass().getSimpleName(), ex.getMessage(), ex);
        return new ResponseEntity<>(INTERNAL_SERVER_ERROR.getReasonPhrase(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = RestException.class)
    public ResponseEntity restException(RestException ex, HandlerMethod handlerMethod) {
        log.warn("RestException handled in service: {}, message: {}", handlerMethod.getMethod().getDeclaringClass().getSimpleName(), ex.getMessage());

        return new ResponseEntity<>(new MessageError(new TppMessageInformation(ERROR, ex.getMessageErrorCode())
                                                         .text(ex.getMessage())), ex.getHttpStatus());
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity requestBodyValidationException(MethodArgumentNotValidException ex, HandlerMethod handlerMethod) {
        log.warn("RequestBodyValidationException handled in controller: {}, message: {} ", handlerMethod.getMethod().getDeclaringClass().getSimpleName(), ex.getMessage());

        return new ResponseEntity<>(getMessageError(FORMAT_ERROR), HttpStatus.BAD_REQUEST);
    }

    private MessageError getMessageError(MessageErrorCode errorCode) {
        TppMessageInformation messageInformation = new TppMessageInformation(ERROR, errorCode);
        messageInformation.setText(messageService.getMessage(errorCode.name()));
        return new MessageError(TransactionStatus.RJCT, messageInformation);
    }
}
