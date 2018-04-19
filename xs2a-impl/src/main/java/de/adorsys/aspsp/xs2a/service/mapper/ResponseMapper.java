package de.adorsys.aspsp.xs2a.service.mapper;

import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ResponseMapper {

    public ResponseEntity ok(ResponseObject response) {
        return getEntity(response, HttpStatus.OK);
    }

    public ResponseEntity okOrNotFound(ResponseObject response) {
        return getEntity(response, response.getBody() != null
                                   ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    public ResponseEntity createdOrBadRequest(ResponseObject response) {
        return getEntity(response, response.getBody() != null
                                           ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity okOrBadRequest(ResponseObject response) {
        return getEntity(response, response.getBody() != null
                                   ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity getEntity(ResponseObject response, HttpStatus status) {
        MessageError messageError = response.getError();
        return messageError != null
               ? new ResponseEntity<>(messageError, HttpStatus.valueOf(messageError.getTppMessage().getCode().getCode()))
               : new ResponseEntity<>(response.getBody(), status);
    }
}
