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

package de.adorsys.psd2.core.data.piis.v1;

import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.xs2a.core.authorisation.ConsentAuthorization;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PiisConsentTest {
    private static final String AUTHORISATION_ID = "a8fc1f02-3639-4528-bd19-3eacf1c67038";
    private JsonReader jsonReader = new JsonReader();

    @ParameterizedTest
    @EnumSource(ConsentType.class)
    void getConsentType(ConsentType consentType) {
        assertEquals(consentType, new PiisConsent(consentType).getConsentType());
    }

    @Test
    void getAccountReference() {
        PiisConsent piisConsent = jsonReader.getObjectFromFile("json/data/piis/piis-consent.json", PiisConsent.class);
        AccountReference accountReference = jsonReader.getObjectFromFile("json/data/piis/account-reference.json", AccountReference.class);

        assertEquals(accountReference, piisConsent.getAccountReference());
    }

    @Test
    void getAccountReference_aspspAccountsEmpty() {
        //Given
        PiisConsent piisConsent = jsonReader.getObjectFromFile("json/data/piis/piis-consent.json", PiisConsent.class);
        piisConsent.setTppAccountAccesses(piisConsent.getAspspAccountAccesses());
        piisConsent.setAspspAccountAccesses(AccountAccess.EMPTY_ACCESS);
        AccountReference expectedAccountReference = jsonReader.getObjectFromFile("json/data/piis/account-reference.json", AccountReference.class);
        //When
        AccountReference actualAccountReference = piisConsent.getAccountReference();
        //Then
        assertEquals(expectedAccountReference, actualAccountReference);
    }

    @ParameterizedTest
    @EnumSource(ConsentType.class)
    void getAccountReference_emptyCollection(ConsentType consentType) {
        assertNull(new PiisConsent(consentType).getAccountReference());
    }

    @Test
    void getPsuIdData() {
        PiisConsent piisConsent = jsonReader.getObjectFromFile("json/data/piis/piis-consent.json", PiisConsent.class);
        PsuIdData psuIdData = jsonReader.getObjectFromFile("json/data/piis/psu-id-data.json", PsuIdData.class);

        assertEquals(psuIdData, piisConsent.getPsuIdData());
    }

    @Test
    void findAuthorisationInConsent_success() {
        //Given
        PiisConsent piisConsent = jsonReader.getObjectFromFile("json/data/piis/piis-consent.json", PiisConsent.class);
        //When
        Optional<ConsentAuthorization> authorisationInConsent = piisConsent.findAuthorisationInConsent(AUTHORISATION_ID);
        //Then
        assertTrue(authorisationInConsent.isPresent());
    }

    @Test
    void findAuthorisationInConsent_failed() {
        //Given
        PiisConsent piisConsent = jsonReader.getObjectFromFile("json/data/piis/piis-consent.json", PiisConsent.class);
        //When
        Optional<ConsentAuthorization> authorisationInConsent = piisConsent.findAuthorisationInConsent("wrong authorisation id");
        //Then
        assertTrue(authorisationInConsent.isEmpty());
    }
}
