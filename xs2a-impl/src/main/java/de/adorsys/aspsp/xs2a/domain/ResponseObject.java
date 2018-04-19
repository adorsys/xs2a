package de.adorsys.aspsp.xs2a.domain;

import de.adorsys.aspsp.xs2a.exception.MessageError;
import lombok.Getter;

/**
 * Responce Object passing the information about performed operation
 *
 * @see MessageCode
 */
@Getter
public class ResponseObject<T> {
    private T body;
    private MessageError error;

    /**
     * Success Response without any additional information
     */
    public ResponseObject() {
    }

    /**
     * Success Response including the Requested Object as a parameter
     *
     * @param body Targeted object. (Any object that has to be passed back to the service)
     */
    public ResponseObject(T body) {
        this.body = body;
    }

    /**
     * Failure Response including addition failure information for TPP
     *
     * @param error MessageError
     */
    public ResponseObject fail(MessageError error) {
        this.error = error;
        return this;
    }
}
