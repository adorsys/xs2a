package de.adorsys.aspsp.xs2a.service.mapper;

import de.adorsys.aspsp.xs2a.domain.MessageCode;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.TppMessageInformation;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.exception.MessageCategory;
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

    public ResponseEntity okOrByTransactionStatus(ResponseObject response) {
        PaymentInitialisationResponse pi = (PaymentInitialisationResponse) response.getBody();
        return (pi.getTransactionStatus() == TransactionStatus.ACCP)
               ? new ResponseEntity<>(response.getBody(), HttpStatus.OK)
               : new ResponseEntity<>(
        new MessageError(pi.getTransactionStatus(), new TppMessageInformation(MessageCategory.ERROR, MessageCode.PAYMENT_FAILED)),
        HttpStatus.valueOf(MessageCode.PAYMENT_FAILED.getCode()));
    }

    public ResponseEntity delete(ResponseObject response) {
        return getEntity(response, response.getBody() != null
                                   ? HttpStatus.NO_CONTENT : HttpStatus.NOT_FOUND);
    }

    private ResponseEntity getEntity(ResponseObject response, HttpStatus status) {
        MessageError messageError = response.getError();
        return messageError != null
               ? new ResponseEntity<>(messageError, HttpStatus.valueOf(messageError.getTppMessage().getCode().getCode()))
               : new ResponseEntity<>(response.getBody(), status);
    }
}
