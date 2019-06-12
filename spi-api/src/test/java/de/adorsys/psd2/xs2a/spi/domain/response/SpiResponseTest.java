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

package de.adorsys.psd2.xs2a.spi.domain.response;

import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class SpiResponseTest {

    private static final AspspConsentData SOME_ASPSP_CONSENT_DATA =
        new AspspConsentData(new byte[0], "Some consent ID");
    private static final String SOME_PAYLOAD = "some payload";
    private static final SpiResponseStatus SOME_STATUS = SpiResponseStatus.LOGICAL_FAILURE;

    @Test
    public void builder_should_pass_on_failure_without_payload() {
        SpiResponse.SpiResponseBuilder<Object> builder = SpiResponse.builder();

        builder
            .aspspConsentData(SOME_ASPSP_CONSENT_DATA)
            .message("some message")
            .payload(null)
            .fail(SOME_STATUS);
    }

    @Test(expected = IllegalStateException.class)
    public void builder_should_fail_on_success_without_payload() {
        SpiResponse.SpiResponseBuilder<Object> builder = SpiResponse.builder();

        builder
            .aspspConsentData(SOME_ASPSP_CONSENT_DATA)
            .success();
    }

    @Test(expected = IllegalStateException.class)
    public void builder_should_fail_on_null_payload() {
        SpiResponse.SpiResponseBuilder<Object> builder = SpiResponse.builder();

        builder
            .payload(null)
            .success();
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_should_fail_without_payload() {
        //noinspection ConstantConditions
        new SpiResponse<>(null, SOME_ASPSP_CONSENT_DATA);
    }

    @Test
    public void second_constructor_should_fail_by_success_without_payload() {
        boolean exceptionCatched = false;
        try {
            new SpiResponse<>(null, SOME_ASPSP_CONSENT_DATA, SpiResponseStatus.SUCCESS, null);
        } catch (IllegalArgumentException e) {
            exceptionCatched = true;
        }
        assertTrue(exceptionCatched);
    }

    @Test
    public void second_constructor_should_pass_by_failure_without_payload() {
        boolean exceptionCatched = false;
        try {
            new SpiResponse<>(null, SOME_ASPSP_CONSENT_DATA, SOME_STATUS, null);
        } catch (IllegalArgumentException e) {
            exceptionCatched = true;
        }
        assertFalse(exceptionCatched);
    }

    @Test
    public void second_constructor_without_status_defaults_to_error() {
        SpiResponse<Object> spiResponse = new SpiResponse<>(SOME_PAYLOAD, SOME_ASPSP_CONSENT_DATA, null, null);
        assertTrue(spiResponse.hasError());
        assertFalse(spiResponse.isSuccessful());
    }

    @Test
    public void second_constructor_with_null_payload_should_generate_error_message2() {
        // Given
        TppMessage errorMessage = new TppMessage(MessageErrorCode.FORMAT_ERROR, "");

        // When
        SpiResponse<String> response = new SpiResponse<>(null, null, SpiResponseStatus.LOGICAL_FAILURE, Collections.emptyList());

        // Then
        assertTrue(response.hasError());
        assertEquals(Collections.singletonList(errorMessage), response.getErrors());
    }

    @Test
    public void builder_build_transfers_every_field() {
        SpiResponse.SpiResponseBuilder<String> builder = SpiResponse.builder();
        SpiResponse<String> response =
            builder
                .payload("some payload")
                .message("some message 1")
                .message(Arrays.asList("Some message 2", "Some message 3"))
                .error(new TppMessage(MessageErrorCode.CONSENT_UNKNOWN_400, "Consent Unknown", "reason"))
                .error(Arrays.asList(new TppMessage(MessageErrorCode.CONSENT_UNKNOWN_400, "Consent Unknown", "reason")))
                .build();

        assertEquals("some payload", response.getPayload());
        assertFalse(response.isSuccessful());
        assertTrue(response.hasError());
        assertEquals(5, response.getMessages().size());
        assertEquals(5, response.getErrors().size());
        assertEquals(SpiResponseStatus.LOGICAL_FAILURE, response.getResponseStatus());
    }

    @Test
    public void builder_build_success_response() {
        SpiResponse.SpiResponseBuilder<String> builder = SpiResponse.builder();
        SpiResponse<String> response =
            builder
                .payload("some payload")
                .build();

        assertEquals("some payload", response.getPayload());
        assertTrue(response.isSuccessful());
        assertFalse(response.hasError());
        assertEquals(0, response.getMessages().size());
        assertEquals(0, response.getErrors().size());
        assertEquals(SpiResponseStatus.SUCCESS, response.getResponseStatus());
    }

    @Test
    public void builder_build_should_generate_message_on_fail() {
        // When
        SpiResponse<String> response = SpiResponse.<String>builder().fail(SpiResponseStatus.LOGICAL_FAILURE);

        // Then
        assertTrue(response.hasError());
        assertFalse(response.getErrors().isEmpty());
    }

    @Test
    public void builder_build_should_generate_message_on_error() {
        // Given
        TppMessage errorMessage = new TppMessage(MessageErrorCode.CONSENT_UNKNOWN_400, "");

        // When
        SpiResponse<String> response = SpiResponse.<String>builder().error(errorMessage).build();

        // Then
        assertTrue(response.hasError());
        assertEquals(Collections.singletonList(errorMessage), response.getErrors());
    }

    @Test
    public void builder_build_with_null_payload_should_generate_error_message() {
        // Given
        TppMessage errorMessage = new TppMessage(MessageErrorCode.INTERNAL_SERVER_ERROR, "");

        // When
        SpiResponse<String> response = SpiResponse.<String>builder().payload(null).build();

        // Then
        assertTrue(response.hasError());
        assertEquals(Collections.singletonList(errorMessage), response.getErrors());
    }

    @Test
    public void builder_build_without_payload_should_generate_error_message() {
        // Given
        TppMessage errorMessage = new TppMessage(MessageErrorCode.INTERNAL_SERVER_ERROR, "");

        // When
        SpiResponse<String> response = SpiResponse.<String>builder().build();

        // Then
        assertTrue(response.hasError());
        assertEquals(Collections.singletonList(errorMessage), response.getErrors());
    }
}
