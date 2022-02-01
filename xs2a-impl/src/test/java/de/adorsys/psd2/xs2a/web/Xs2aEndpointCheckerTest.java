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
