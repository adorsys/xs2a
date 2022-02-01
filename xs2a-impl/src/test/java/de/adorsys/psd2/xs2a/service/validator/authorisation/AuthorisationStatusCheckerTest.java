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

package de.adorsys.psd2.xs2a.service.validator.authorisation;

import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthorisationStatusCheckerTest {
    private static final PsuIdData PSU_ID_DATA = new PsuIdData("12345678", null, null, null, null);
    private AuthorisationStatusChecker checker;
    private List<Authorisation> authorisations;

    @BeforeEach
    void setUp() {
        checker = new AuthorisationStatusChecker();
        authorisations = new ArrayList<>();
    }

    @Test
    void isFinalised() {
        authorisations.add(createAuthorisation(PSU_ID_DATA, AuthorisationType.PIS_CREATION, ScaStatus.RECEIVED));
        assertFalse(checker.isFinalised(PSU_ID_DATA, authorisations, AuthorisationType.CONSENT));

        authorisations.add(createAuthorisation(PSU_ID_DATA, AuthorisationType.CONSENT, ScaStatus.RECEIVED));
        assertFalse(checker.isFinalised(PSU_ID_DATA, authorisations, AuthorisationType.CONSENT));

        authorisations.add(createAuthorisation(PSU_ID_DATA, AuthorisationType.CONSENT, ScaStatus.FINALISED));
        assertTrue(checker.isFinalised(PSU_ID_DATA, authorisations, AuthorisationType.CONSENT));
    }

    @Test
    void isFinalised_emptyList() {
        assertFalse(checker.isFinalised(PSU_ID_DATA, authorisations, AuthorisationType.CONSENT));
        assertFalse(checker.isFinalised(PSU_ID_DATA, authorisations, AuthorisationType.PIS_CREATION));
        assertFalse(checker.isFinalised(PSU_ID_DATA, authorisations, AuthorisationType.PIS_CANCELLATION));
    }

    private Authorisation createAuthorisation(PsuIdData psuIdData, AuthorisationType authorisationType, ScaStatus scaStatus) {
        Authorisation authorisation = new Authorisation();
        authorisation.setPsuIdData(psuIdData);
        authorisation.setAuthorisationType(authorisationType);
        authorisation.setScaStatus(scaStatus);
        return authorisation;
    }
}
