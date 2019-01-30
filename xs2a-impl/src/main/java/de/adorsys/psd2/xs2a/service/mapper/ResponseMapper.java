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

package de.adorsys.psd2.xs2a.service.mapper;

import de.adorsys.psd2.xs2a.domain.CustomContentTypeProvider;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.function.Function;

import static org.springframework.http.HttpStatus.*;

/**
 * ResponseMapper class should be used for success responses mapping only.
 * In case of unsuccessful error mapping IllegalArgumentException would be thrown - ResponseErrorMapper should be used for such cases.
 */
@RequiredArgsConstructor
@Component
public class ResponseMapper {
    public <T, R> ResponseEntity<?> ok(ResponseObject<T> response, Function<T, R> mapper) { //NOPMD short method name ok corresponds to status code
        return generateResponse(response, OK, mapper);
    }

    public <T, R> ResponseEntity<?> created(ResponseObject<T> response, Function<T, R> mapper) {
        return generateResponse(response, CREATED, mapper);
    }

    public <T, R> ResponseEntity<?> delete(ResponseObject<T> response, Function<T, R> mapper) {
        return generateResponse(response, NO_CONTENT, mapper);
    }

    public <T> ResponseEntity ok(ResponseObject<T> response) { //NOPMD short method name ok corresponds to status code
        return generateResponse(response, OK);
    }

    public <T> ResponseEntity created(ResponseObject<T> response) {
        return generateResponse(response, CREATED);
    }

    public <T> ResponseEntity delete(ResponseObject<T> response) {
        return generateResponse(response, NO_CONTENT);
    }

    public <T> ResponseEntity accepted(ResponseObject<T> response) {
        return generateResponse(response, ACCEPTED);
    }

    private <T> ResponseEntity generateResponse(ResponseObject<T> response, HttpStatus positiveStatus) {
        return generateResponse(response, positiveStatus, null);
    }

    private <T, R> ResponseEntity generateResponse(ResponseObject<T> response, HttpStatus positiveStatus, Function<T, R> mapper) {
        if (response.hasError()) {
            throw new IllegalArgumentException("Response includes an error: " + response.getError());
        }

        T body = response.getBody();

        ResponseEntity.BodyBuilder responseBuilder =
            ResponseEntity
                .status(positiveStatus);

        if (body instanceof CustomContentTypeProvider) {
            responseBuilder = responseBuilder
                                  .contentType(((CustomContentTypeProvider) body).getCustomContentType());
        }

        return responseBuilder
                   .body(getBody(body, mapper));
    }

    private <T, R> Object getBody(T body, Function<T, R> mapper) {
        return mapper == null
                   ? body
                   : mapper.apply(body);
    }
}
