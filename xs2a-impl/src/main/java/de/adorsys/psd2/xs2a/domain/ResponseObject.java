/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.xs2a.domain;

import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import lombok.Getter;

/**
 * Response Object passing the information about performed operation
 *
 * @see MessageError
 */
@Getter
public class ResponseObject<T> {
    private final T body;
    private final MessageError error;

    private ResponseObject(ResponseBuilder<T> builder) {
        this.body = builder.body;
        this.error = builder.error;
    }

    public static <T> ResponseBuilder<T> builder() {
        return new ResponseBuilder<>();
    }

    public static class ResponseBuilder<T> {
        private T body;
        private MessageError error;

        private ResponseBuilder() {
        }

        public ResponseBuilder<T> body(T body) {
            this.body = body;
            return this;
        }

        public ResponseBuilder<T> fail(MessageError error) {
            this.error = error;
            return this;
        }

        public ResponseBuilder<T> fail(ErrorType errorType, TppMessageInformation... tppMessageInformation) {
            this.error = new MessageError(errorType, tppMessageInformation);
            return this;
        }

        public ResponseBuilder<T> fail(ErrorHolder errorHolder) {
            this.error = new MessageError(errorHolder);
            return this;
        }

        public ResponseObject<T> build() {
            return new ResponseObject<>(this);
        }
    }

    public boolean hasError() {
        return error != null;
    }
}
