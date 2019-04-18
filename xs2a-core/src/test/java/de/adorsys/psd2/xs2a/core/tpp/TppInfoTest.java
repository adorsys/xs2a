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

package de.adorsys.psd2.xs2a.core.tpp;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class TppInfoTest {
    private static final String AUTHORITY_ID = "authority id";
    private static final String AUTHORITY_ID_2 = "authority id 2";
    private static final String AUTHORISATION_NUMBER = "authorisation number";
    private static final String AUTHORISATION_NUMBER_2 = "authorisation number 2";

    @Test
    public void equals_withOnlyAuthorisationNumberAndAuthorityIdSame_shouldReturnTrue() {
        TppInfo tppInfoFirst = buildTppInfo(AUTHORISATION_NUMBER, AUTHORITY_ID);
        tppInfoFirst.setTppName("some tpp name");
        tppInfoFirst.setTppRoles(Collections.singletonList(TppRole.PISP));
        tppInfoFirst.setAuthorityName("some authority name");
        tppInfoFirst.setCountry("some country");
        tppInfoFirst.setOrganisation("some organisation");
        tppInfoFirst.setOrganisationUnit("some country unit");
        tppInfoFirst.setCity("some city");
        tppInfoFirst.setState("some state");
        tppInfoFirst.setTppRedirectUri(new TppRedirectUri("some uri", "some nok uri"));
        tppInfoFirst.setIssuerCN("some issuer CN");

        TppInfo tppInfoSecond = buildTppInfo(AUTHORISATION_NUMBER, AUTHORITY_ID);
        tppInfoSecond.setTppName("some other tpp name");
        tppInfoSecond.setTppRoles(Collections.singletonList(TppRole.PISP));
        tppInfoSecond.setAuthorityName("some other authority name");
        tppInfoSecond.setCountry("some other country");
        tppInfoSecond.setOrganisation("some other organisation");
        tppInfoSecond.setOrganisationUnit("some other country unit");
        tppInfoSecond.setCity("some other city");
        tppInfoSecond.setState("some other state");
        tppInfoSecond.setTppRedirectUri(new TppRedirectUri("some other uri", "some other nok uri"));
        tppInfoSecond.setIssuerCN("some other issuer CN");

        assertEquals(tppInfoFirst, tppInfoSecond);
    }

    @Test
    public void equals_withDifferentAuthorisationNumber_shouldReturnFalse() {
        TppInfo tppInfoFirst = buildTppInfo(AUTHORISATION_NUMBER, AUTHORITY_ID);
        TppInfo tppInfoSecond = buildTppInfo(AUTHORISATION_NUMBER_2, AUTHORITY_ID);

        assertNotEquals(tppInfoFirst, tppInfoSecond);
    }

    @Test
    public void equals_withDifferentAuthorityId_shouldReturnFalse() {
        TppInfo tppInfoFirst = buildTppInfo(AUTHORISATION_NUMBER, AUTHORITY_ID);
        TppInfo tppInfoSecond = buildTppInfo(AUTHORISATION_NUMBER, AUTHORITY_ID_2);

        assertNotEquals(tppInfoFirst, tppInfoSecond);
    }

    @Test
    public void equals_withDifferentAuthorisationNumberAndAuthorityId_shouldReturnFalse() {
        TppInfo tppInfoFirst = buildTppInfo(AUTHORISATION_NUMBER, AUTHORITY_ID);
        TppInfo tppInfoSecond = buildTppInfo(AUTHORISATION_NUMBER_2, AUTHORITY_ID_2);

        assertNotEquals(tppInfoFirst, tppInfoSecond);
    }

    private TppInfo buildTppInfo(String authorisationNumber, String authorityId) {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(authorisationNumber);
        tppInfo.setAuthorityId(authorityId);
        return tppInfo;
    }
}
