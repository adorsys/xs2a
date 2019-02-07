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

package de.adorsys.psd2.xs2a.spi.domain.authorisation;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SpiAuthorizationCodeResultTest {

    @Test
    public void isEmpty_works_by_null_ChallengeData() {
        SpiAuthorizationCodeResult spiAuthorizationCodeResult = new SpiAuthorizationCodeResult();
        spiAuthorizationCodeResult.setSelectedScaMethod(new SpiAuthenticationObject());

        assertFalse(spiAuthorizationCodeResult.isEmpty());
    }

    @Test
    public void isEmpty_works_by_null_ChallengeData_and_AuthObject() {
        SpiAuthorizationCodeResult spiAuthorizationCodeResult = new SpiAuthorizationCodeResult();

        assertTrue(spiAuthorizationCodeResult.isEmpty());
    }

}
