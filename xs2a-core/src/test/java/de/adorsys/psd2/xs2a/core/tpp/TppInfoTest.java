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

package de.adorsys.psd2.xs2a.core.tpp;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class TppInfoTest {
    private static final String AUTHORISATION_NUMBER = "authorisation number";
    private static final String AUTHORISATION_NUMBER_2 = "authorisation number 2";

    @Test
    void equals_withOnlyAuthorisationNumberAndAuthorityIdSame_shouldReturnTrue() {
        TppInfo tppInfoFirst = buildTppInfo(AUTHORISATION_NUMBER);
        tppInfoFirst.setAuthorityId("authorisation number");
        tppInfoFirst.setTppName("some tpp name");
        tppInfoFirst.setTppRoles(Collections.singletonList(TppRole.PISP));
        tppInfoFirst.setAuthorityName("some authority name");
        tppInfoFirst.setCountry("some country");
        tppInfoFirst.setOrganisation("some organisation");
        tppInfoFirst.setOrganisationUnit("some country unit");
        tppInfoFirst.setCity("some city");
        tppInfoFirst.setState("some state");
        tppInfoFirst.setIssuerCN("some issuer CN");

        TppInfo tppInfoSecond = buildTppInfo(AUTHORISATION_NUMBER);
        tppInfoSecond.setAuthorityId("authorisation number");
        tppInfoSecond.setTppName("some other tpp name");
        tppInfoSecond.setTppRoles(Collections.singletonList(TppRole.PISP));
        tppInfoSecond.setAuthorityName("some other authority name");
        tppInfoSecond.setCountry("some other country");
        tppInfoSecond.setOrganisation("some other organisation");
        tppInfoSecond.setOrganisationUnit("some other country unit");
        tppInfoSecond.setCity("some other city");
        tppInfoSecond.setState("some other state");
        tppInfoSecond.setIssuerCN("some other issuer CN");

        assertEquals(tppInfoFirst, tppInfoSecond);
    }

    @Test
    void equals_withDifferentAuthorisationNumber_shouldReturnFalse() {
        TppInfo tppInfoFirst = buildTppInfo(AUTHORISATION_NUMBER);
        TppInfo tppInfoSecond = buildTppInfo(AUTHORISATION_NUMBER_2);

        assertNotEquals(tppInfoFirst, tppInfoSecond);
    }

    private TppInfo buildTppInfo(String authorisationNumber) {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(authorisationNumber);

        return tppInfo;
    }
}
