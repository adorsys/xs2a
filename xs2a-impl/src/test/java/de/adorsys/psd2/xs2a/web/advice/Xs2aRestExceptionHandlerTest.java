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

package de.adorsys.psd2.xs2a.web.advice;

import de.adorsys.psd2.xs2a.web.Xs2aEndpointChecker;
import de.adorsys.psd2.xs2a.web.error.TppErrorMessageWriter;
import de.adorsys.psd2.xs2a.web.filter.TppErrorMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static de.adorsys.psd2.xs2a.core.domain.MessageCategory.ERROR;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.REQUESTED_FORMATS_INVALID;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.SERVICE_INVALID_405_METHOD_NOT_SUPPORTED;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Xs2aRestExceptionHandlerTest {
    private static final String METHOD_NAME = "DELETE";

    @InjectMocks
    private Xs2aRestExceptionHandler exceptionHandler;

    @Mock
    private TppErrorMessageWriter tppErrorMessageWriter;
    @Mock
    private Xs2aEndpointChecker xs2aEndpointChecker;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    private Object handler;
    private HttpRequestMethodNotSupportedException methodNotSupportedException;
    private HttpMediaTypeNotAcceptableException mediaTypeNotAcceptableException;

    @BeforeEach
    void setUp() {
        handler = new Object();
        methodNotSupportedException = new HttpRequestMethodNotSupportedException(METHOD_NAME);
        mediaTypeNotAcceptableException = new HttpMediaTypeNotAcceptableException("Error message");
    }

    @Test
    void doResolveException_resolve() {
        // When
        when(xs2aEndpointChecker.isXs2aEndpoint(request)).thenReturn(true);

        ModelAndView actual = exceptionHandler.doResolveException(request, response, handler, methodNotSupportedException);

        // Then
        assertNotNull(actual);
    }

    @Test
    void doResolveException_doNotResolve() {
        // Given
        when(xs2aEndpointChecker.isXs2aEndpoint(request)).thenReturn(false);

        // When
        ModelAndView actual = exceptionHandler.doResolveException(request, response, handler, methodNotSupportedException);

        // Then
        assertNull(actual);
    }

    @Test
    void handleHttpRequestMethodNotSupported() throws IOException {
        // Given
        TppErrorMessage tppErrorMessage = new TppErrorMessage(ERROR, SERVICE_INVALID_405_METHOD_NOT_SUPPORTED, methodNotSupportedException.getMethod());

        // When
        exceptionHandler.handleHttpRequestMethodNotSupported(methodNotSupportedException, request, response, handler);

        // Then
        verify(tppErrorMessageWriter, times(1)).writeError(response, tppErrorMessage);
    }

    @Test
    void handleHttpMediaTypeNotAcceptable() throws IOException {
        // Given
        TppErrorMessage tppErrorMessage = new TppErrorMessage(ERROR, REQUESTED_FORMATS_INVALID);

        // When
        exceptionHandler.handleHttpMediaTypeNotAcceptable(mediaTypeNotAcceptableException, request, response, handler);

        // Then
        verify(tppErrorMessageWriter, times(1)).writeError(response, tppErrorMessage);
    }
}
