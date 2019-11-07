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

package de.adorsys.psd2.consent.api;

import org.junit.Test;

import static org.junit.Assert.*;

public class CmsResponseTest {
    private final CmsError TECHNICAL_ERROR = CmsError.TECHNICAL_ERROR;

    @Test
    public void builder_should_pass_on_failure_without_payload() {
        // Given
        CmsResponse.CmsResponseBuilder<Object> builder = CmsResponse.builder();

        // When
        CmsResponse<Object> response = builder
                                           .payload(null)
                                           .error(TECHNICAL_ERROR)
                                           .build();

        // Then
        assertFalse(response.isSuccessful());
        assertTrue(response.hasError());
        assertNotNull(response.getError());
    }

    @Test
    public void builder_build_success_response() {
        CmsResponse.CmsResponseBuilder<String> builder = CmsResponse.builder();
        CmsResponse<String> response =
            builder
                .payload("some payload")
                .build();

        assertEquals("some payload", response.getPayload());
        assertTrue(response.isSuccessful());
        assertFalse(response.hasError());
        assertNull(response.getError());
    }

    @Test
    public void builder_build_should_generate_message_on_fail() {
        // When
        CmsResponse<String> response = CmsResponse.<String>builder()
                                           .error(CmsError.TECHNICAL_ERROR)
                                           .build();

        // Then
        assertTrue(response.hasError());
        assertNotNull(response.getError());
        assertEquals(response.getError(), TECHNICAL_ERROR);
    }

    @Test
    public void builder_build_with_null_payload_should_generate_error_message() {
        // When
        CmsResponse<String> response = CmsResponse.<String>builder().payload(null).build();

        // Then
        assertTrue(response.hasError());
        assertEquals(TECHNICAL_ERROR, response.getError());
    }

    @Test
    public void builder_build_without_payload_should_generate_error_message() {
        // When
        CmsResponse<String> response = CmsResponse.<String>builder().build();

        // Then
        assertTrue(response.hasError());
        assertEquals(TECHNICAL_ERROR, response.getError());
    }

    @Test
    public void is_Sucussful_with_payload_test() {
        // When
        CmsResponse<String> response = CmsResponse.<String>builder().payload("some payload").build();

        // Then
        assertTrue(response.isSuccessful());
        assertFalse(response.hasError());
    }

    @Test
    public void is_Sucussful_without_payload_with_error_test() {
        // When
        CmsResponse<String> response = CmsResponse.<String>builder().error(TECHNICAL_ERROR).build();

        // Then
        assertFalse(response.isSuccessful());
        assertTrue(response.hasError());
    }

    @Test
    public void is_Sucussful_without_payload_without_error_test() {
        // When
        CmsResponse<String> response = CmsResponse.<String>builder().build();

        // Then
        assertFalse(response.isSuccessful());
        assertTrue(response.hasError());
    }
}
