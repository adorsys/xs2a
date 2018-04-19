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
    public ResponseObject(MessageError error) {
        this.error = error;
    }
}
