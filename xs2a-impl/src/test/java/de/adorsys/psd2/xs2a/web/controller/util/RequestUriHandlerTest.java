/*
 * Copyright 2018-2021 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.web.controller.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RequestUriHandlerTest {
    private RequestUriHandler requestUriHandler;

    @BeforeEach
    void setUp() {
        requestUriHandler = new RequestUriHandler();
    }

    @Test
    void trimEndingSlash() {
        // Given
        String input = "/v1/accounts/";

        // When
        String actual = requestUriHandler.trimEndingSlash(input);
        String expected = "/v1/accounts";

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void trimEndingSlash_NoSlash() {
        // Given
        String input = "/v1/accounts";

        // When
        String actual = requestUriHandler.trimEndingSlash(input);

        // Then
        assertThat(actual).isEqualTo(input);
    }

    @Test
    void handleTransactionUri_WithPage() {
        // Given
        String transactionUri = "v1/accounts/account_id/transactions";
        String bookingStatus = "booked";
        Integer pageIndex = 9;

        // When
        String actual = requestUriHandler.handleTransactionUri(transactionUri, bookingStatus, pageIndex);
        String expected = "v1/accounts/account_id/transactions?bookingStatus=booked&pageIndex=9";

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void handleTransactionUri_WithoutPage() {
        // Given
        String transactionUri = "v1/accounts/account_id/transactions";
        String bookingStatus = "booked";

        // When
        String actual = requestUriHandler.handleTransactionUri(transactionUri, bookingStatus, null);
        String expected = "v1/accounts/account_id/transactions?bookingStatus=booked";

        // Then
        assertThat(actual).isEqualTo(expected);
    }
}
