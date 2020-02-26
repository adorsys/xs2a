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

import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PiisConsentTest {
    private JsonReader jsonReader = new JsonReader();

    @Test
    void getConsentType() {
        assertEquals(ConsentType.PIIS_ASPSP, new PiisConsent().getConsentType());
    }

    @Test
    void getAccountReference() {
        PiisConsent piisConsent = jsonReader.getObjectFromFile("json/data/piis/piis-consent.json", PiisConsent.class);
        AccountReference accountReference = jsonReader.getObjectFromFile("json/data/piis/account-reference.json", AccountReference.class);

        assertEquals(accountReference, piisConsent.getAccountReference());
    }

    @Test
    void getAccountReference_emptyCollection() {
        assertNull(new PiisConsent().getAccountReference());
    }

    @Test
    void getPsuIdData() {
        PiisConsent piisConsent = jsonReader.getObjectFromFile("json/data/piis/piis-consent.json", PiisConsent.class);
        PsuIdData psuIdData = jsonReader.getObjectFromFile("json/data/piis/psu-id-data.json", PsuIdData.class);

        assertEquals(psuIdData, piisConsent.getPsuIdData());
    }

    @Test
    void getPsuIdData_emptyCollection() {
        assertNull(new PiisConsent().getPsuIdData());
    }
}
