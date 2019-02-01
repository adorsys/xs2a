/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.xs2a.exception;

import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class RestException extends RuntimeException {
    private HttpStatus httpStatus;
    private String message;
    private MessageErrorCode messageErrorCode = MessageErrorCode.INTERNAL_SERVER_ERROR;

    public RestException(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public RestException(MessageErrorCode messageErrorCode) {
        this.messageErrorCode = messageErrorCode;
        this.httpStatus = HttpStatus.valueOf(messageErrorCode.getCode());
        this.message = httpStatus.getReasonPhrase();
    }
}
