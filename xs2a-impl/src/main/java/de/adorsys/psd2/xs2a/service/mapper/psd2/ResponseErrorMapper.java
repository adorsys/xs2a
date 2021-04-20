/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.mapper.psd2;

import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.web.error.ServiceUnavailableError;
import de.adorsys.psd2.xs2a.web.header.ResponseHeaders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@SuppressWarnings("rawtypes")
@RequiredArgsConstructor
public class ResponseErrorMapper {
    private static final String LOG_PATTERN = "Generate error: [{}]";
    private final ErrorMapperContainer errorMapperContainer;

    /**
     * Generates {@link ResponseEntity} with given error in the body
     *
     * @param error error to be returned in the body
     * @return response entity with appropriate error status and body
     */
    public ResponseEntity generateErrorResponse(MessageError error) {
        ErrorMapperContainer.ErrorBody errorBody = errorMapperContainer.getErrorBody(error);
        log.info(LOG_PATTERN, error);
        return new ResponseEntity<>(errorBody.getBody(), errorBody.getStatus());
    }

    /**
     * Generates {@link ResponseEntity} with given error in the body and response headers
     *
     * @param error           error to be returned in the body
     * @param responseHeaders headers to be returned in the response
     * @return response entity with appropriate error status, body and headers
     */
    public ResponseEntity generateErrorResponse(MessageError error, ResponseHeaders responseHeaders) {
        ErrorMapperContainer.ErrorBody errorBody = errorMapperContainer.getErrorBody(error);
        log.info(LOG_PATTERN, error);
        return new ResponseEntity<>(errorBody.getBody(), responseHeaders.getHttpHeaders(), errorBody.getStatus());
    }

    public ResponseEntity generateServiceUnavailableErrorResponse(String error) {
        log.info(LOG_PATTERN, error);
        return new ResponseEntity<>(new ServiceUnavailableError(), HttpStatus.SERVICE_UNAVAILABLE);
    }
}
