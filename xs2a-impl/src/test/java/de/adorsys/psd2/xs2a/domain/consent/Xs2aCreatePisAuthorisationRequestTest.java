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

package de.adorsys.psd2.xs2a.domain.consent;

import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.junit.jupiter.api.Test;

import static de.adorsys.psd2.xs2a.core.profile.PaymentType.SINGLE;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Xs2aCreatePisAuthorisationRequestTest {
    private static final String PAYMENT_ID = "payment id";
    private static final PsuIdData PSU_ID_DATA = new PsuIdData("psu id", null, null, null);
    private static final PsuIdData EMPTY_PSU_ID_DATA = new PsuIdData(null, null, null, null);
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
