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

    @Test
    void handleTransactionUri_WithoutPageUpperCaseBookingStatus() {
        // Given
        String transactionUri = "v1/accounts/account_id/transactions";
        String bookingStatus = "BOOKED";

        // When
        String actual = requestUriHandler.handleTransactionUri(transactionUri, bookingStatus, null);
        String expected = "v1/accounts/account_id/transactions?bookingStatus=booked";

        // Then
        assertThat(actual).isEqualTo(expected);
    }
}
