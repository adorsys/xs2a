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
