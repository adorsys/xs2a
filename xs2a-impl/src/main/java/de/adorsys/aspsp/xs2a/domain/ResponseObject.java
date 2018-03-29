package de.adorsys.aspsp.xs2a.domain;

import lombok.Getter;

/**
 * Responce Object passing the information about performed operation
 *
 * @see OperationStatus
 * @see MessageCode
 */
@Getter
public class ResponseObject<T> {
    final private boolean success;
    final private T operationTarget;
    final private MessageCode message;

    /**
     * Success Response without any additional information
     */
    public ResponseObject() {
        this.success = true;
        operationTarget = null;
        message = null;
    }

    /**
     * Success Response including the Requested Object as a parameter
     *
     * @param operationTarget Targeted object. (Any object that has to be passed back to the service)
     */
    public ResponseObject(T operationTarget) {
        this.success = true;
        this.operationTarget = operationTarget;
        this.message = null;
    }

    /**
     * Failure Response including the Failure Message
     *
     * @param message MessageCode enum
     */
    public ResponseObject(MessageCode message) {
        this.success = false;
        this.operationTarget = null;
        this.message = message;
    }
}
