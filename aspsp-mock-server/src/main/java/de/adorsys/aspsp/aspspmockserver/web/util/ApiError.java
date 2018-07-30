package de.adorsys.aspsp.aspspmockserver.web.util;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiError {

    private HttpStatus status;
    private String message;
    private String error;

    public ApiError(HttpStatus status, String message, String error) {
        this.status = status;
        this.message = message;
        this.error = error;
    }
}
