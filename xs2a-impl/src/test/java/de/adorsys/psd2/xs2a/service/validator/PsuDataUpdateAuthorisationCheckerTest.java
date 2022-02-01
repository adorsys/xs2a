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

package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PsuDataUpdateAuthorisationCheckerTest {
    private static final String PSU_ID_1 = "First";
    private static final String PSU_ID_2 = "Second";
    private static final PsuDataUpdateAuthorisationChecker psuDataUpdateAuthorisationChecker = new PsuDataUpdateAuthorisationChecker();

    @Test
    void canPsuUpdateAuthorisationPsuAuthorisationNull() {
        //When
        boolean canPsuUpdateAuthorisation = psuDataUpdateAuthorisationChecker.canPsuUpdateAuthorisation(buildPSU(PSU_ID_1), null);
        //Then
        assertTrue(canPsuUpdateAuthorisation);
    }

    @Test
    void canPsuUpdateAuthorisationPsuAuthorisationEmpty() {
        //When
        boolean canPsuUpdateAuthorisation = psuDataUpdateAuthorisationChecker.canPsuUpdateAuthorisation(buildPSU(PSU_ID_1), buildPSU(null));
        //Then
        assertTrue(canPsuUpdateAuthorisation);
    }

    @Test
    void canPsuUpdateAuthorisationPsuRequestEmpty() {
        //When
        boolean canPsuUpdateAuthorisation = psuDataUpdateAuthorisationChecker.canPsuUpdateAuthorisation(buildPSU(null), buildPSU(PSU_ID_1));
        //Then
        assertTrue(canPsuUpdateAuthorisation);
    }

    @Test
    void canPsuUpdateAuthorisationPsuRequestEqualsPsuAuthorisation() {
        //When
        boolean canPsuUpdateAuthorisation = psuDataUpdateAuthorisationChecker.canPsuUpdateAuthorisation(buildPSU(PSU_ID_1), buildPSU(PSU_ID_1));
        //Then
        assertTrue(canPsuUpdateAuthorisation);
    }

    @Test
    void canPsuUpdateAuthorisationPsuRequestNotEqualsPsuAuthorisation() {
        //When
        boolean canPsuUpdateAuthorisation = psuDataUpdateAuthorisationChecker.canPsuUpdateAuthorisation(buildPSU(PSU_ID_1), buildPSU(PSU_ID_2));
        //Then
        assertFalse(canPsuUpdateAuthorisation);
    }

    @Test
    void canPsuUpdateAuthorisationNoPsuRequestNoPsuAuthorisation() {
        //When
        boolean canPsuUpdateAuthorisation = psuDataUpdateAuthorisationChecker.canPsuUpdateAuthorisation(buildPSU(null), buildPSU(null));
        //Then
        assertFalse(canPsuUpdateAuthorisation);
    }

    @Test
    void canPsuUpdateAuthorisationNoPsuRequestPsuAuthorisationNull() {
        //When
        boolean canPsuUpdateAuthorisation = psuDataUpdateAuthorisationChecker.canPsuUpdateAuthorisation(buildPSU(null), null);
        //Then
        assertFalse(canPsuUpdateAuthorisation);
    }

    @Test
    void areBothPsusAbsent_PsuAuthorisationNull() {
        //When
        boolean areBothPsusAbsent = psuDataUpdateAuthorisationChecker.areBothPsusAbsent(buildPSU(null), null);
        //Then
        assertTrue(areBothPsusAbsent);
    }

    @Test
    void areBothPsusAbsent_PsuAuthorisationEmpty() {
        //When
        boolean areBothPsusAbsent = psuDataUpdateAuthorisationChecker.areBothPsusAbsent(buildPSU(null), buildPSU(null));
        //Then
        assertTrue(areBothPsusAbsent);
    }

    @Test
    void areBothPsusAbsent_PsuAuthorisationPresent() {
        //When
        boolean areBothPsusAbsent = psuDataUpdateAuthorisationChecker.areBothPsusAbsent(buildPSU(null), buildPSU(PSU_ID_1));
        //Then
        assertFalse(areBothPsusAbsent);
    }

    @Test
    void areBothPsusAbsent_PsuRequestPresent() {
        //When
        boolean areBothPsusAbsent = psuDataUpdateAuthorisationChecker.areBothPsusAbsent(buildPSU(PSU_ID_1), buildPSU(null));
        //Then
        assertFalse(areBothPsusAbsent);
    }

    @Test
    void areBothPsusAbsent_BothPsusPresent() {
        //When
        boolean areBothPsusAbsent = psuDataUpdateAuthorisationChecker.areBothPsusAbsent(buildPSU(PSU_ID_1), buildPSU(PSU_ID_2));
        //Then
        assertFalse(areBothPsusAbsent);
    }

    private PsuIdData buildPSU(String id) {
        return new PsuIdData(id, null, null, null, null);
    }
}
