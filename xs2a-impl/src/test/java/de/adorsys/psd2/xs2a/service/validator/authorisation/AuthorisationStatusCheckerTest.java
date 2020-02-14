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
        assertFalse(checker.isFinalised(PSU_ID_DATA, authorisations, AuthorisationType.AIS));

        authorisations.add(createAuthorisation(PSU_ID_DATA, AuthorisationType.AIS, ScaStatus.RECEIVED));
        assertFalse(checker.isFinalised(PSU_ID_DATA, authorisations, AuthorisationType.AIS));

        authorisations.add(createAuthorisation(PSU_ID_DATA, AuthorisationType.AIS, ScaStatus.FINALISED));
        assertTrue(checker.isFinalised(PSU_ID_DATA, authorisations, AuthorisationType.AIS));
    }

    @Test
    void isFinalised_emptyList() {
        assertFalse(checker.isFinalised(PSU_ID_DATA, authorisations, AuthorisationType.AIS));
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
