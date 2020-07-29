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

package de.adorsys.psd2.consent.domain.payment;

import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PisCommonPaymentDataTest {

    @Test
    void getInternalRequestId_PisAuthorisation() {
        // Given
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        String expectedInternalRequestId = "internal-request-id";
        pisCommonPaymentData.setInternalRequestId(expectedInternalRequestId);

        // When
        String actualInternalRequestId = pisCommonPaymentData.getInternalRequestId(AuthorisationType.PIS_CREATION);

        // Then
        assertEquals(expectedInternalRequestId, actualInternalRequestId);
    }

    @Test
    void getInternalRequestId_PisCancellationAuthorisation() {
        // Given
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        String expectedInternalRequestId = "internal-request-id";
        pisCommonPaymentData.setCancellationInternalRequestId(expectedInternalRequestId);

        // When
        String actualInternalRequestId = pisCommonPaymentData.getInternalRequestId(AuthorisationType.PIS_CANCELLATION);

        // Then
        assertEquals(expectedInternalRequestId, actualInternalRequestId);
    }

    @Test
    void getInternalRequestId_AisAuthorisation_shouldThrowException() {
        // Given
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();

        // Then
        assertThrows(IllegalArgumentException.class,
                     () -> pisCommonPaymentData.getInternalRequestId(AuthorisationType.CONSENT));
    }
}
