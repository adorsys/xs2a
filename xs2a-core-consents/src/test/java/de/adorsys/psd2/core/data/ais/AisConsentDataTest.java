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

package de.adorsys.psd2.core.data.ais;

import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AisConsentDataTest {
    private static final JsonReader jsonReader = new JsonReader();

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

    @ParameterizedTest
    @MethodSource("consents")
    void getConsentRequestType(AisConsent aisConsent) {
        assertEquals(AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS, aisConsent.getConsentRequestType());
    }

    private static Stream<Arguments> consents() {
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/data/ais/ais-consent-all-available.json",
                                                             AisConsent.class);
        AisConsent aisConsentWithOwnerName = jsonReader.getObjectFromFile("json/data/ais/ais-consent-all-available-owner-name.json",
                                                                          AisConsent.class);
        AisConsent aisConsentWithBalance = jsonReader.getObjectFromFile("json/data/ais/ais-consent-all-available-with-balance.json",
                                                                        AisConsent.class);
        AisConsent aisConsentWithBalanceAndOwnerName = jsonReader.getObjectFromFile("json/data/ais/ais-consent-all-available-with-balance-owner-name.json",
                                                                                    AisConsent.class);

        return Stream.of(
            Arguments.arguments(aisConsent),
            Arguments.arguments(aisConsentWithOwnerName),
            Arguments.arguments(aisConsentWithBalance),
            Arguments.arguments(aisConsentWithBalanceAndOwnerName)
        );
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
        assertTrue(aisConsent.isWithBalance());
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
