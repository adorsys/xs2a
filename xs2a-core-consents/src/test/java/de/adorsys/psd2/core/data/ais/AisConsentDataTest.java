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

package de.adorsys.psd2.core.data.ais;

import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AisConsentDataTest {
    private JsonReader jsonReader = new JsonReader();

    @Test
    void getConsentType() {
        assertEquals(ConsentType.AIS, new AisConsent().getConsentType());
    }

    @Test
    void getConsentRequestType_bankOffered() {
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/data/ais/ais-consent-bank-offered.json", AisConsent.class);

        assertEquals(AisConsentRequestType.BANK_OFFERED, aisConsent.getConsentRequestType());
    }

    @Test
    void getConsentRequestType_bankOfferedEmptyArray() {
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/data/ais/ais-consent-bank-offered-empty-array.json", AisConsent.class);

        assertEquals(AisConsentRequestType.BANK_OFFERED, aisConsent.getConsentRequestType());
    }

    @Test
    void getConsentRequestType_tpp_allAvailableAccounts() {
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/data/ais/ais-consent-all-available.json",
                                                             AisConsent.class);

        assertEquals(AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS, aisConsent.getConsentRequestType());
    }

    @Test
    void getConsentRequestType_tpp_allAvailableAccountsWithOwnerName() {
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/data/ais/ais-consent-all-available-owner-name.json",
                                                             AisConsent.class);

        assertEquals(AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS, aisConsent.getConsentRequestType());
    }

    @Test
    void getConsentRequestType_tpp_allAvailableAccountsWithBalance() {
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/data/ais/ais-consent-all-available-with-balance.json",
                                                             AisConsent.class);

        assertEquals(AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS, aisConsent.getConsentRequestType());
    }

    @Test
    void getConsentRequestType_tpp_allAvailableAccountsWithBalanceAndOwnerName() {
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/data/ais/ais-consent-all-available-with-balance-owner-name.json",
                                                             AisConsent.class);

        assertEquals(AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS, aisConsent.getConsentRequestType());
    }

    @Test
    void getConsentRequestType_tpp_dedicated() {
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/data/ais/ais-consent-tpp-dedicated.json",
                                                             AisConsent.class);

        assertEquals(AisConsentRequestType.DEDICATED_ACCOUNTS, aisConsent.getConsentRequestType());
    }

    @Test
    void getConsentRequestType_tpp_global() {
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/data/ais/ais-consent-global.json",
                                                             AisConsent.class);

        assertEquals(AisConsentRequestType.GLOBAL, aisConsent.getConsentRequestType());
    }

    @Test
    void getConsentRequestType_tpp_globalWithOwnerName() {
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/data/ais/ais-consent-global-owner-name.json",
                                                             AisConsent.class);

        assertEquals(AisConsentRequestType.GLOBAL, aisConsent.getConsentRequestType());
    }

    @Test
    void getConsentRequestType_aspsp_dedicated() {
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/data/ais/ais-consent-aspsp-dedicated.json",
                                                             AisConsent.class);

        assertEquals(AisConsentRequestType.DEDICATED_ACCOUNTS, aisConsent.getConsentRequestType());
    }

    @Test
    void getUsedAccess_emptyAccesses() {
        AisConsent bankOfferedConsent = jsonReader.getObjectFromFile("json/data/ais/ais-consent-bank-offered.json", AisConsent.class);
        AccountAccess emptyAccess = new AccountAccess(null, null, null, null);

        assertEquals(emptyAccess, bankOfferedConsent.getAccess());
    }

    @Test
    void getUsedAccess_tppAccess() {
        AisConsent consentWithTppAccess = jsonReader.getObjectFromFile("json/data/ais/ais-consent-tpp-dedicated.json", AisConsent.class);
        List<AccountReference> accountReferences = Collections.singletonList(new AccountReference(AccountReferenceType.IBAN, "DE98500105171757213183", null));
        AccountAccess dedicatedAccess = new AccountAccess(accountReferences, accountReferences, accountReferences, null);

        assertEquals(dedicatedAccess, consentWithTppAccess.getAccess());
    }

    @Test
    void getUsedAccess_aspspAccess() {
        AisConsent consentWithAspspAccess = jsonReader.getObjectFromFile("json/data/ais/ais-consent-aspsp-dedicated.json", AisConsent.class);
        List<AccountReference> accountReferences = Collections.singletonList(new AccountReference(AccountReferenceType.IBAN, "DE98500105171757213183", null));
        AccountAccess dedicatedAccess = new AccountAccess(accountReferences, accountReferences, accountReferences, null);

        assertEquals(dedicatedAccess, consentWithAspspAccess.getAccess());
    }

    @Test
    void getUsedAccess_globalConsentWithAspspReferences_shouldReturnTppAccess() {
        AisConsent globalConsentDataWithAccountReferences = jsonReader.getObjectFromFile("json/data/ais/ais-consent-global-aspsp-accounts.json", AisConsent.class);
        AccountAccess globalAccess = new AccountAccess(null, null, null, null);

        assertEquals(globalAccess, globalConsentDataWithAccountReferences.getAccess());
    }
}
