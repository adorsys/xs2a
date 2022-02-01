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

package de.adorsys.psd2.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class UpdatePsuAuthenticationResponseTest {

    private static final String AUTHORISATION_ID = "11111111111111";

    @Test
    void authorisationIdFieldIsPresent() {
        try {
            UpdatePsuAuthenticationResponse.class.getDeclaredField("authorisationId");
        } catch (NoSuchFieldException e) {
            fail();
        }
    }

    @Test
    void authorisationIdFieldIsAccessible() {
        UpdatePsuAuthenticationResponse response = new UpdatePsuAuthenticationResponse();
        response.setAuthorisationId(AUTHORISATION_ID);
        assertEquals(AUTHORISATION_ID, response.getAuthorisationId());
    }
}
