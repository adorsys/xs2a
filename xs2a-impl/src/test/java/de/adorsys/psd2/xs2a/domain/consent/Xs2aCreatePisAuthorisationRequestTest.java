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

package de.adorsys.psd2.xs2a.domain.consent;

import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.junit.jupiter.api.Test;

import static de.adorsys.psd2.xs2a.core.profile.PaymentType.SINGLE;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Xs2aCreatePisAuthorisationRequestTest {
    private static final String PAYMENT_ID = "payment id";
    private static final PsuIdData PSU_ID_DATA = new PsuIdData("psu id", null, null, null, null);
    private static final PsuIdData EMPTY_PSU_ID_DATA = new PsuIdData(null, null, null, null, null);
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String PASSWORD = "some password";

    @Test
    void hasNoUpdateData_withNoPsuIdData_shouldReturnTrue() {
        // Given
        Xs2aCreatePisAuthorisationRequest request = new Xs2aCreatePisAuthorisationRequest(PAYMENT_ID, EMPTY_PSU_ID_DATA, PAYMENT_PRODUCT, SINGLE, PASSWORD);

        // When
        boolean actual = request.hasNoUpdateData();

        // Then
        assertTrue(actual);
    }

    @Test
    void hasNoUpdateData_withBlankPassword_shouldReturnTrue() {
        // Given
        Xs2aCreatePisAuthorisationRequest request = new Xs2aCreatePisAuthorisationRequest(PAYMENT_ID, PSU_ID_DATA, PAYMENT_PRODUCT, SINGLE, "");

        // When
        boolean actual = request.hasNoUpdateData();

        // Then
        assertTrue(actual);
    }

    @Test
    void hasNoUpdateData_withNullPassword_shouldReturnTrue() {
        // Given
        Xs2aCreatePisAuthorisationRequest request = new Xs2aCreatePisAuthorisationRequest(PAYMENT_ID, PSU_ID_DATA, PAYMENT_PRODUCT, SINGLE, null);

        // When
        boolean actual = request.hasNoUpdateData();

        // Then
        assertTrue(actual);
    }

    @Test
    void hasNoUpdateData_withNoPsuIdDataAndNoPassword_shouldReturnTrue() {
        // Given
        Xs2aCreatePisAuthorisationRequest request = new Xs2aCreatePisAuthorisationRequest(PAYMENT_ID, EMPTY_PSU_ID_DATA, PAYMENT_PRODUCT, SINGLE, null);

        // When
        boolean actual = request.hasNoUpdateData();

        // Then
        assertTrue(actual);
    }

    @Test
    void hasNoUpdateData_withPsuIdDataAndPassword_shouldReturnFalse() {
        // Given
        Xs2aCreatePisAuthorisationRequest request = new Xs2aCreatePisAuthorisationRequest(PAYMENT_ID, PSU_ID_DATA, PAYMENT_PRODUCT, SINGLE, PASSWORD);

        // When
        boolean actual = request.hasNoUpdateData();

        // Then
        assertFalse(actual);
    }
}
