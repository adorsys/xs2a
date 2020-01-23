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
