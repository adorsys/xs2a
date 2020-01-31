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

package de.adorsys.psd2.consent.web.aspsp.advice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class CmsAspspExceptionHandlerTest {
    private static final String ERROR_MESSAGE = "Http message is not readable";

    @InjectMocks
    private CmsAspspExceptionHandler cmsAspspExceptionHandler;

    @Mock
    private WebRequest webRequest;

    @Test
    void handleHttpMessageNotReadable() {
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException(ERROR_MESSAGE);
        HttpHeaders headers = new HttpHeaders();
        HttpStatus status = HttpStatus.OK;

        ResponseEntity actualResponse = cmsAspspExceptionHandler.handleHttpMessageNotReadable(exception, headers, status, webRequest);

        assertEquals(HttpStatus.BAD_REQUEST, actualResponse.getStatusCode());
        assertEquals(ERROR_MESSAGE, actualResponse.getBody());
    }
}
