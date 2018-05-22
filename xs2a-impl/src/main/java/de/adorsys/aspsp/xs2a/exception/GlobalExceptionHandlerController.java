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

import de.adorsys.aspsp.xs2a.domain.MessageCode;
import de.adorsys.aspsp.xs2a.domain.TppMessageInformation;
import de.adorsys.aspsp.xs2a.spi.rest.exception.RestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;

import javax.validation.ValidationException;

import static de.adorsys.aspsp.xs2a.exception.MessageCategory.ERROR;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandlerController {

    @ExceptionHandler(value = {ValidationException.class})
    public ResponseEntity validationException(ValidationException ex, HandlerMethod handlerMethod) {
        log.warn("ValidationException handled in service: {}, message: {} ", handlerMethod.getMethod().getDeclaringClass().getSimpleName(), ex.getMessage());

        return new ResponseEntity(new MessageError(new TppMessageInformation(ERROR, MessageCode.FORMAT_ERROR)
                                                                      .text(ex.getMessage())), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {HttpMessageNotReadableException.class})
    public ResponseEntity httpMessageException(HttpMessageNotReadableException ex, HandlerMethod handlerMethod) {
        log.warn("HttpMessageNotReadableException handled in Controller: {}, message: {}, stackTrace: {}", handlerMethod.getMethod().getDeclaringClass().getSimpleName(), ex.getMessage(), ex);

        HttpStatus status = HttpStatus.BAD_REQUEST;
        return new ResponseEntity(status.getReasonPhrase(), status);
    }

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity exception(Exception ex, HandlerMethod handlerMethod) {
        log.warn("Uncatched exception handled in Controller: {}, message: {}, stackTrace: {}", handlerMethod.getMethod().getDeclaringClass().getSimpleName(), ex.getMessage(), ex);

        HttpStatus status = INTERNAL_SERVER_ERROR;
        return new ResponseEntity(status.getReasonPhrase(), status);
    }

    @ExceptionHandler(value = {RestException.class})
    public ResponseEntity restException(RestException ex, HandlerMethod handlerMethod) {
        log.warn("RestException handled in service: {}, message: {} ", handlerMethod.getMethod().getDeclaringClass().getSimpleName(), ex.getMessage());

        return new ResponseEntity(new MessageError(new TppMessageInformation(ERROR, MessageCode.INTERNAL_SERVER_ERROR)
                                                       .text(ex.getMessage())), ex.getHttpStatus());
    }
}
