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
