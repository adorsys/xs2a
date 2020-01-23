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
