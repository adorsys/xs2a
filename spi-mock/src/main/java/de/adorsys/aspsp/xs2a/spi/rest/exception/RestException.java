package de.adorsys.aspsp.xs2a.spi.rest.exception;

import lombok.Getter;
import lombok.Value;
import org.springframework.http.HttpStatus;

@Value
@Getter
public class RestException extends RuntimeException {
    private HttpStatus httpStatus;
    private String message;

    public RestException(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
