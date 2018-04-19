package de.adorsys.aspsp.xs2a.exception;

import de.adorsys.aspsp.xs2a.domain.MessageCode;
import de.adorsys.aspsp.xs2a.domain.TppMessageInformation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;

import javax.validation.ValidationException;

import static de.adorsys.aspsp.xs2a.exception.MessageCategory.ERROR;

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
