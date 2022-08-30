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

package de.adorsys.psd2.xs2a.spi.domain.response;

import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class SpiResponseTest {

    private final TppMessage FORMAT_ERROR = new TppMessage(MessageErrorCode.FORMAT_ERROR);

    @Test
    void builder_should_pass_on_failure_without_payload() {
        SpiResponse.SpiResponseBuilder<Object> builder = SpiResponse.builder();

        SpiResponse<Object> response = builder
                                           .payload(null)
                                           .error(FORMAT_ERROR)
                                           .build();

        assertEquals(Collections.singletonList(FORMAT_ERROR), response.getErrors());
    }

    @Test
    void builder_build_success_response() {
        SpiResponse.SpiResponseBuilder<String> builder = SpiResponse.builder();
        SpiResponse<String> response =
            builder
                .payload("some payload")
                .build();

        assertEquals("some payload", response.getPayload());
        assertTrue(response.isSuccessful());
        assertFalse(response.hasError());
        assertEquals(0, response.getErrors().size());
    }

    @Test
    void builder_build_should_generate_message_on_fail() {
        // When
        SpiResponse<String> response = SpiResponse.<String>builder()
                                           .error(FORMAT_ERROR)
                                           .build();

        // Then
        assertTrue(response.hasError());
        assertFalse(response.getErrors().isEmpty());
    }

    @Test
    void builder_build_should_generate_message_on_error() {
        // Given
        TppMessage errorMessage = new TppMessage(MessageErrorCode.CONSENT_UNKNOWN_400);

        // When
        SpiResponse<String> response = SpiResponse.<String>builder().error(errorMessage).build();

        // Then
        assertTrue(response.hasError());
        assertEquals(Collections.singletonList(errorMessage), response.getErrors());
    }

    @Test
    void builder_build_with_null_payload_should_generate_error_message() {
        // Given
        TppMessage errorMessage = new TppMessage(MessageErrorCode.INTERNAL_SERVER_ERROR);

        // When
        SpiResponse<String> response = SpiResponse.<String>builder().payload(null).build();

        // Then
        assertTrue(response.hasError());
        assertEquals(Collections.singletonList(errorMessage), response.getErrors());
    }

    @Test
    void builder_build_without_payload_should_generate_error_message() {
        // Given
        TppMessage errorMessage = new TppMessage(MessageErrorCode.INTERNAL_SERVER_ERROR);

        // When
        SpiResponse<String> response = SpiResponse.<String>builder().build();

        // Then
        assertTrue(response.hasError());
        assertEquals(Collections.singletonList(errorMessage), response.getErrors());
    }
}
