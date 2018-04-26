package de.adorsys.aspsp.xs2a.spi.rest.exception;

import lombok.Value;
import org.springframework.http.HttpStatus;

@Value
public class RestException extends RuntimeException {
    private HttpStatus httpStatus;
    private String message;
}
