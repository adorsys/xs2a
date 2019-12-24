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

package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PsuDataUpdateAuthorisationCheckerTest {
    private static final String PSU_ID_1 = "First";
    private static final String PSU_ID_2 = "Second";
    private static final PsuDataUpdateAuthorisationChecker psuDataUpdateAuthorisationChecker = new PsuDataUpdateAuthorisationChecker();

    @Test
    public void canPsuUpdateAuthorisationPsuAuthorisationNull() {
        //When
        boolean canPsuUpdateAuthorisation = psuDataUpdateAuthorisationChecker.canPsuUpdateAuthorisation(buildPSU(PSU_ID_1), null);
        //Then
        assertTrue(canPsuUpdateAuthorisation);
    }

    @Test
    public void canPsuUpdateAuthorisationPsuAuthorisationEmpty() {
        //When
        boolean canPsuUpdateAuthorisation = psuDataUpdateAuthorisationChecker.canPsuUpdateAuthorisation(buildPSU(PSU_ID_1), buildPSU(null));
        //Then
        assertTrue(canPsuUpdateAuthorisation);
    }

    @Test
    public void canPsuUpdateAuthorisationPsuRequestEmpty() {
        //When
        boolean canPsuUpdateAuthorisation = psuDataUpdateAuthorisationChecker.canPsuUpdateAuthorisation(buildPSU(null), buildPSU(PSU_ID_1));
        //Then
        assertTrue(canPsuUpdateAuthorisation);
    }

    @Test
    public void canPsuUpdateAuthorisationPsuRequestEqualsPsuAuthorisation() {
        //When
        boolean canPsuUpdateAuthorisation = psuDataUpdateAuthorisationChecker.canPsuUpdateAuthorisation(buildPSU(PSU_ID_1), buildPSU(PSU_ID_1));
        //Then
        assertTrue(canPsuUpdateAuthorisation);
    }

    @Test
    public void canPsuUpdateAuthorisationPsuRequestNotEqualsPsuAuthorisation() {
        //When
        boolean canPsuUpdateAuthorisation = psuDataUpdateAuthorisationChecker.canPsuUpdateAuthorisation(buildPSU(PSU_ID_1), buildPSU(PSU_ID_2));
        //Then
        assertFalse(canPsuUpdateAuthorisation);
    }

    @Test
    public void canPsuUpdateAuthorisationNoPsuRequestNoPsuAuthorisation() {
        //When
        boolean canPsuUpdateAuthorisation = psuDataUpdateAuthorisationChecker.canPsuUpdateAuthorisation(buildPSU(null), buildPSU(null));
        //Then
        assertFalse(canPsuUpdateAuthorisation);
    }

    @Test
    public void canPsuUpdateAuthorisationNoPsuRequestPsuAuthorisationNull() {
        //When
        boolean canPsuUpdateAuthorisation = psuDataUpdateAuthorisationChecker.canPsuUpdateAuthorisation(buildPSU(null), null);
        //Then
        assertFalse(canPsuUpdateAuthorisation);
    }

    @Test
    public void areBothPsusAbsent_PsuAuthorisationNull() {
        //When
        boolean areBothPsusAbsent = psuDataUpdateAuthorisationChecker.areBothPsusAbsent(buildPSU(null), null);
        //Then
        assertTrue(areBothPsusAbsent);
    }

    @Test
    public void areBothPsusAbsent_PsuAuthorisationEmpty() {
        //When
        boolean areBothPsusAbsent = psuDataUpdateAuthorisationChecker.areBothPsusAbsent(buildPSU(null), buildPSU(null));
        //Then
        assertTrue(areBothPsusAbsent);
    }

    @Test
    public void areBothPsusAbsent_PsuAuthorisationPresent() {
        //When
        boolean areBothPsusAbsent = psuDataUpdateAuthorisationChecker.areBothPsusAbsent(buildPSU(null), buildPSU(PSU_ID_1));
        //Then
        assertFalse(areBothPsusAbsent);
    }

    @Test
    public void areBothPsusAbsent_PsuRequestPresent() {
        //When
        boolean areBothPsusAbsent = psuDataUpdateAuthorisationChecker.areBothPsusAbsent(buildPSU(PSU_ID_1), buildPSU(null));
        //Then
        assertFalse(areBothPsusAbsent);
    }

    @Test
    public void areBothPsusAbsent_BothPsusPresent() {
        //When
        boolean areBothPsusAbsent = psuDataUpdateAuthorisationChecker.areBothPsusAbsent(buildPSU(PSU_ID_1), buildPSU(PSU_ID_2));
        //Then
        assertFalse(areBothPsusAbsent);
    }

    private PsuIdData buildPSU(String id) {
        return new PsuIdData(id, null, null, null, null);
    }
}
