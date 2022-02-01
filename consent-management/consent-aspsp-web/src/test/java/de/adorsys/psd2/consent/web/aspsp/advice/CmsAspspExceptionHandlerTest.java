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
