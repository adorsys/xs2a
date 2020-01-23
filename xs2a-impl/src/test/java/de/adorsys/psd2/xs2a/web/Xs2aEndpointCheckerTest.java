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

package de.adorsys.psd2.xs2a.web;

import de.adorsys.psd2.xs2a.web.request.RequestPathResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Xs2aEndpointCheckerTest {
    private static final String XS2A_ENDPOINT = "/v1/accounts";
    private static final String NOT_XS2A_ENDPOINT = "/v1/gifts";

    @InjectMocks
    private Xs2aEndpointChecker xs2aEndpointChecker;
    @Mock
    private RequestPathResolver requestPathResolver;
    @Mock
    private MockHttpServletRequest request;

    @Test
    void isXs2aEndpoint_true() {
        // Given
        when(requestPathResolver.resolveRequestPath(request)).thenReturn(XS2A_ENDPOINT);

        // When
        boolean actual = xs2aEndpointChecker.isXs2aEndpoint(request);

        // Then
        assertTrue(actual);
    }

    @Test
    void isXs2aEndpoint_false() {
        // Given
        when(requestPathResolver.resolveRequestPath(request)).thenReturn(NOT_XS2A_ENDPOINT);

        // When
        boolean actual = xs2aEndpointChecker.isXs2aEndpoint(request);

        // Then
        assertFalse(actual);
    }
}
