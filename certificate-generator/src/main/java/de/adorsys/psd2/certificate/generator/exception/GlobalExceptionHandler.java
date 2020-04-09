package de.adorsys.psd2.certificate.generator.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice(basePackages = "de.adorsys.psd2.certificate.generator.controller")
public class GlobalExceptionHandler {
    private static final String MESSAGE = "message";
    private static final String CODE = "code";
    private static final String DATE_TIME = "dateTime";

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<Map<String, String>> handleInvalidFormatException(InvalidFormatException e, HandlerMethod handlerMethod) {
        log.warn("Invalid format exception handled in service: {}, message: {}",
                 handlerMethod.getMethod().getDeclaringClass().getSimpleName(), e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                   .body(getHandlerContent("Invalid initial data"));
    }

    @ExceptionHandler(CertificateGeneratorException.class)
    public ResponseEntity<Map<String, String>> handleCertificateException(CertificateGeneratorException e, HandlerMethod handlerMethod) {
        log.warn("Invalid format exception handled in service: {}, message: {}",
                 handlerMethod.getMethod().getDeclaringClass().getSimpleName(), e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                   .body(getHandlerContent(e.getMessage()));
    }

    private Map<String, String> getHandlerContent(String message) {
        Map<String, String> error = new HashMap<>();
        error.put(CODE, String.valueOf(HttpStatus.BAD_REQUEST.value()));
        error.put(MESSAGE, message);
        error.put(DATE_TIME, LocalDateTime.now().toString());
        return error;
    }
}
