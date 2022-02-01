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

package de.adorsys.psd2.xs2a.component;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

@ExtendWith(MockitoExtension.class)
class MultiReadHttpServletResponseTest {
    private static final String CONTENT = "some string";

    @Mock
    private HttpServletResponse response;

    @Test
    void getCachedContent_shouldReturnContentFromOutputStream() throws IOException {
        // Given
        MultiReadHttpServletResponse multiReadHttpServletResponse = new MultiReadHttpServletResponse(response);
        multiReadHttpServletResponse.getOutputStream().write(CONTENT.getBytes());

        // When
        byte[] cachedContent = multiReadHttpServletResponse.getCachedContent();

        // Then
        assertArrayEquals(CONTENT.getBytes(), cachedContent);
    }

    @Test
    void getCachedContent_shouldReturnContentFromWriter() throws IOException {
        // Given
        MultiReadHttpServletResponse multiReadHttpServletResponse = new MultiReadHttpServletResponse(response);
        multiReadHttpServletResponse.getWriter().write(CONTENT);

        // When
        byte[] cachedContent = multiReadHttpServletResponse.getCachedContent();

        // Then
        assertArrayEquals(CONTENT.getBytes(), cachedContent);
    }
}
