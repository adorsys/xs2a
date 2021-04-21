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
import de.adorsys.psd2.xs2a.web.header.ResponseHeaders;
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
@SuppressWarnings("rawtypes")
@RequiredArgsConstructor
@Component
public class ResponseMapper {
    /**
     * Generates {@link ResponseEntity} with the response body in response to a generic successful request
     *
     * @param response response object from the service with the body inside
     * @param mapper   function for transforming body into another type
     * @param <T>      type of the body in the response object
     * @param <R>      type of the transformed body
     * @return response entity with OK HTTP status and body
     */
    public <T, R> ResponseEntity ok(ResponseObject<T> response, Function<T, R> mapper) { //NOPMD short method name ok corresponds to status code
        return generateResponse(response, OK, mapper);
    }

    /**
     * Generates {@link ResponseEntity} with the response body in response to a successful resource creation request
     *
     * @param response response object from the service with the body inside
     * @param mapper   function for transforming body into another type
     * @param <T>      type of the body in the response object
     * @param <R>      type of the transformed body
     * @return response entity with Created HTTP status and body
     */
    public <T, R> ResponseEntity created(ResponseObject<T> response, Function<T, R> mapper) {
        return generateResponse(response, CREATED, mapper);
    }

    /**
     * Generates {@link ResponseEntity} with the response body in response to a successful resource deletion request
     *
     * @param response response object from the service with the body inside
     * @param mapper   function for transforming body into another type
     * @param <T>      type of the body in the response object
     * @param <R>      type of the transformed body
     * @return response entity with No Content HTTP status and body
     */
    public <T, R> ResponseEntity delete(ResponseObject<T> response, Function<T, R> mapper) {
        return generateResponse(response, NO_CONTENT, mapper);
    }

    /**
     * Generates {@link ResponseEntity} with the response body and headers in response to a generic successful request
     *
     * @param response        response object from the service with the body inside
     * @param mapper          function for transforming body into another type
     * @param responseHeaders headers to be returned in the response
     * @param <T>             type of the body in the response object
     * @param <R>             type of the body after applying mapper
     * @return response entity with OK HTTP status, body and headers
     */
    public <T, R> ResponseEntity ok(ResponseObject<T> response, Function<T, R> mapper, ResponseHeaders responseHeaders) { //NOPMD short method name ok corresponds to status code
        return generateResponse(response, OK, mapper, responseHeaders);
    }

    /**
     * Generates {@link ResponseEntity} with the response body in response to a successful resource creation request
     *
     * @param response        response object from the service with the body inside
     * @param mapper          function for transforming body into another type
     * @param responseHeaders headers to be returned in the response
     * @param <T>             type of the body in the response object
     * @param <R>             type of the body after applying mapper
     * @return response entity with Created HTTP status, body and headers
     */
    public <T, R> ResponseEntity created(ResponseObject<T> response, Function<T, R> mapper, ResponseHeaders responseHeaders) {
        return generateResponse(response, CREATED, mapper, responseHeaders);
    }

    /**
     * Generates {@link ResponseEntity} with the response body in response to a successful resource creation request
     *
     * @param response        response object from the service with the body inside
     * @param responseHeaders headers to be returned in the response
     * @param <T>             type of the body in the response object
     * @return response entity with Created HTTP status, body and headers
     */
    public <T> ResponseEntity created(ResponseObject<T> response, ResponseHeaders responseHeaders) {
        return generateResponse(response, CREATED, null, responseHeaders);
    }

    /**
     * Generates {@link ResponseEntity} with the response body in response to a generic successful request
     *
     * @param response response object from the service with the body inside
     * @param <T>      type of the body in the response object
     * @return response entity with OK HTTP status and body
     */
    public <T> ResponseEntity ok(ResponseObject<T> response) { //NOPMD short method name ok corresponds to status code
        return generateResponse(response, OK);
    }

    /**
     * Generates {@link ResponseEntity} with the response body in response to a successful resource creation request
     *
     * @param response response object from the service with the body inside
     * @param <T>      type of the body in the response object
     * @return response entity with Created HTTP status and body
     */
    public <T> ResponseEntity created(ResponseObject<T> response) {
        return generateResponse(response, CREATED);
    }

    /**
     * Generates {@link ResponseEntity} with the response body in response to a successful resource deletion request
     *
     * @param response response object from the service with the body inside
     * @param <T>      type of the body in the response object
     * @return response entity with No Content HTTP status and body
     */
    public <T> ResponseEntity delete(ResponseObject<T> response) {
        return generateResponse(response, NO_CONTENT);
    }

    /**
     * Generates {@link ResponseEntity} with the response body in response to a request that hasn't been processed yet
     *
     * @param response response object from the service with the body inside
     * @param <T>      type of the body in the response object
     * @return response entity with Accepted HTTP status and body
     */
    public <T> ResponseEntity accepted(ResponseObject<T> response) {
        return generateResponse(response, ACCEPTED);
    }

    private <T> ResponseEntity generateResponse(ResponseObject<T> response, HttpStatus positiveStatus) {
        return generateResponse(response, positiveStatus, null);
    }

    private <T, R> ResponseEntity generateResponse(ResponseObject<T> response,
                                                   HttpStatus positiveStatus,
                                                   Function<T, R> mapper) {
        ResponseHeaders emptyHeaders = ResponseHeaders.builder().build();
        return generateResponse(response, positiveStatus, mapper, emptyHeaders);
    }

    private <T, R> ResponseEntity generateResponse(ResponseObject<T> response,
                                                   HttpStatus positiveStatus,
                                                   Function<T, R> mapper,
                                                   ResponseHeaders responseHeaders) {
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
                   .headers(responseHeaders.getHttpHeaders())
                   .body(getBody(body, mapper));
    }

    private <T, R> Object getBody(T body, Function<T, R> mapper) {
        return mapper == null
                   ? body
                   : mapper.apply(body);
    }
}
