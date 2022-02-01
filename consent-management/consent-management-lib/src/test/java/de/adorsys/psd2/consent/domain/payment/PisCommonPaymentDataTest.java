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
