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

import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponseType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Xs2aCreatePisCancellationAuthorisationResponseTest {
    @Test
    void getAuthorisationResponseType_shouldReturnStart() {
        // Given
        Xs2aCreatePisCancellationAuthorisationResponse response = new Xs2aCreatePisCancellationAuthorisationResponse("some cancellation id", ScaStatus.RECEIVED, PaymentType.SINGLE, null);

        // When
        AuthorisationResponseType actual = response.getAuthorisationResponseType();

        // Then
        assertEquals(AuthorisationResponseType.START, actual);
    }
}
