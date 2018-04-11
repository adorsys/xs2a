package de.adorsys.aspsp.xs2a.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandlerController {

    @ExceptionHandler(value = {HttpMessageNotReadableException.class})
    public ResponseEntity handleTppException(HttpMessageNotReadableException ex, HandlerMethod handlerMethod) {
        log.warn("HttpMessageNotReadableException handled in Controller: {}, message: {}, stackTrace: {}", handlerMethod.getMethod().getDeclaringClass().getSimpleName(), ex.getMessage(), ex);

        HttpStatus status = HttpStatus.BAD_REQUEST;
        return new ResponseEntity(status.getReasonPhrase(), status);
    }

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity handleTppException(Exception ex, HandlerMethod handlerMethod) {
        log.warn("Uncatched exception handled in Controller: {}, message: {}, stackTrace: {}", handlerMethod.getMethod().getDeclaringClass().getSimpleName(), ex.getMessage(), ex);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return new ResponseEntity(status.getReasonPhrase(), status);
    }
}
