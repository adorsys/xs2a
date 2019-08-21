/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.web.advice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.discovery.ServiceTypeDiscoveryService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorMapperContainer;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceTypeToErrorTypeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.SERVICE_INVALID_405;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class Xs2aRestExceptionHandler extends ResponseEntityExceptionHandler {

    private final ErrorMapperContainer errorMapperContainer;
    private final ServiceTypeDiscoveryService serviceTypeDiscoveryService;
    private final ServiceTypeToErrorTypeMapper errorTypeMapper;
    private final ObjectMapper objectMapper;

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                         HttpHeaders headers,
                                                                         HttpStatus status,
                                                                         WebRequest request) {
        String responseStringWithError = StringUtils.EMPTY;

        try {
            responseStringWithError = objectMapper.writeValueAsString(createError(ex.getMethod()));
        } catch (JsonProcessingException e) {
            log.warn("Can't convert object to json: {}", e.getMessage());
        }

        return ResponseEntity
                   .status(HttpStatus.METHOD_NOT_ALLOWED)
                   .contentType(MediaType.APPLICATION_JSON)
                   .body(responseStringWithError);
    }

    private Object createError(String methodName) {
        MessageError messageError = new MessageError(errorTypeMapper.mapToErrorType(serviceTypeDiscoveryService.getServiceType(), SERVICE_INVALID_405.getCode()), buildErrorTppMessages(methodName));
        return errorMapperContainer.getErrorBody(messageError).getBody();
    }

    private TppMessageInformation buildErrorTppMessages(String methodName) {
        return of(SERVICE_INVALID_405, String.format("HTTP method '%s' is not supported", methodName));
    }

}
