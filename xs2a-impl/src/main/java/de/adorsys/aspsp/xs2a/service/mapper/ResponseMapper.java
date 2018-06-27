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

package de.adorsys.aspsp.xs2a.service.mapper;

import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import static org.springframework.http.HttpStatus.*;

@Component
@AllArgsConstructor
public class ResponseMapper {

    public ResponseEntity ok() {  //NOPMD short method name ok corresponds to status code
        return ResponseEntity.ok().build();
    }

    public <T> ResponseEntity<T> ok(ResponseObject<T> response) { //NOPMD short method name ok corresponds to status code
        return getEntity(response, OK);
    }

    public <T> ResponseEntity<T> created(ResponseObject<T> response) {
        return getEntity(response, CREATED);
    }

    public <T> ResponseEntity<T> delete(ResponseObject<T> response) {
        return getEntity(response, NO_CONTENT);
    }

    private <T> ResponseEntity<T> getEntity(ResponseObject<T> response, HttpStatus status) {
        return response.hasError()
            ? enrichError(response.getError())
            : new ResponseEntity<>(response.getBody(), status);
    }

    private ResponseEntity enrichError(MessageError error){
         return new ResponseEntity<>(error, valueOf(error.getTppMessage().getCode().getCode()));
    }
}
