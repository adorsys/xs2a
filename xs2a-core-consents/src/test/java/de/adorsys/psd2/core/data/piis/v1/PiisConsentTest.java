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
