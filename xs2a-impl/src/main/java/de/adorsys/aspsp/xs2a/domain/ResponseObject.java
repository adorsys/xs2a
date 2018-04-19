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
    private final T body;
    private final MessageError error;

    private ResponseObject(ResponseBuilder<T> builder) {
        this.body = builder.body;
        this.error = builder.error;
    }

    public static ResponseBuilder builder(){
        return new ResponseBuilder();
    }

    public static class ResponseBuilder<T> {
        private T body;
        private MessageError error;

        public ResponseBuilder body(T body){
            this.body = body;
            return this;
        }

        public ResponseBuilder fail(MessageError error){
            this.error = error;
            return this;
        }

        public ResponseObject build() {
            return new ResponseObject(this);
        }
    }
}
