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
