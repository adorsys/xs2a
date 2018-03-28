package de.adorsys.aspsp.xs2a.domain;

import lombok.Getter;
//TODO Agree on design and aim

/**
 * Responce Object passing the information about performed operation
 *
 * @see OperationStatus
 * @see MessageCode
 */
@Getter
public class ResponseObject<T> {
    private OperationStatus operationStatus;
    private T operationTarget;
    private MessageCode message;

    /**
     * Success Response without any additional information
     */
    public ResponseObject() {
        this.operationStatus = OperationStatus.SUCCESS;
    }

    /**
     * Success Response including the Requested Object as a parameter
     *
     * @param operationTarget Targeted object. (Any object that has to be passed back to the service)
     */
    public ResponseObject(T operationTarget) {
        this.operationStatus = OperationStatus.SUCCESS;
        this.operationTarget = operationTarget;
    }

    /**
     * Failure Response including the Failure Message
     *
     * @param message MessageCode enum
     */
    public ResponseObject(MessageCode message) {
        this.operationStatus = OperationStatus.FAILURE;
        this.message = message;
    }

    /**
     * Custom Response including operation status and message
     *
     * @param operationStatus Operation status
     * @param message         MessageCode enum
     */
    public ResponseObject(OperationStatus operationStatus, MessageCode message) {
        this.operationStatus = operationStatus;
        this.message = message;
    }

    /**
     * Custom Response including all parameters
     *
     * @param operationStatus Operation status
     * @param message         MessageCode enum
     * @param operationTarget <T> Operation targeted object
     */
    public ResponseObject(OperationStatus operationStatus, T operationTarget, MessageCode message) {
        this.operationTarget = operationTarget;
        this.operationStatus = operationStatus;
        this.message = message;
    }
}
