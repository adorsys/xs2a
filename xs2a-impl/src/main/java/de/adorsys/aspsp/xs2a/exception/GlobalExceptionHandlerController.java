package de.adorsys.aspsp.xs2a.exception;

import de.adorsys.aspsp.xs2a.service.MessageService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@AllArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandlerController {
    private MessageService messageService;

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity handleTppException(Exception ex) {
        log.info("{}", ex.getMessage());

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return new ResponseEntity(messageService.getMessage(status.name()), status);
    }
}
