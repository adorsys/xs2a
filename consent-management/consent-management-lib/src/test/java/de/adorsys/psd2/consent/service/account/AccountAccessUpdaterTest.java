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

package de.adorsys.psd2.consent.service.account;

import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AccountAccessUpdaterTest {
    private JsonReader jsonReader = new JsonReader();

    private AccountAccessUpdater accountAccessUpdater = new AccountAccessUpdater();

    @Test
    void updateAccountReferencesInAccess() {
        AccountAccess existingAccess = jsonReader.getObjectFromFile("json/service/account/account-access.json", AccountAccess.class);
        AccountAccess newAccess = jsonReader.getObjectFromFile("json/service/account/account-access-aspsp.json", AccountAccess.class);

        AccountAccess updatedAccess = accountAccessUpdater.updateAccountReferencesInAccess(existingAccess, newAccess);

        assertEquals(newAccess, updatedAccess);
    }

    @Test
    void updateAccountReferencesInAccess_additionalInformationReferences() {
        AccountAccess existingAccess = jsonReader.getObjectFromFile("json/service/account/account-access-additional-info.json", AccountAccess.class);
        AccountAccess newAccess = jsonReader.getObjectFromFile("json/service/account/account-access-additional-info-aspsp.json", AccountAccess.class);

        AccountAccess updatedAccess = accountAccessUpdater.updateAccountReferencesInAccess(existingAccess, newAccess);

        assertEquals(newAccess, updatedAccess);
    }

    @Test
    void updateAccountReferencesInAccess_noExistingReferences() {
        AccountAccess existingAccess = jsonReader.getObjectFromFile("json/service/account/account-access-no-references.json", AccountAccess.class);
        AccountAccess newAccess = jsonReader.getObjectFromFile("json/service/account/account-access-aspsp.json", AccountAccess.class);
        AccountAccess expectedAccess = jsonReader.getObjectFromFile("json/service/account/account-access-aspsp.json", AccountAccess.class);

        AccountAccess updatedAccess = accountAccessUpdater.updateAccountReferencesInAccess(existingAccess, newAccess);

        assertEquals(expectedAccess, updatedAccess);
    }

    @Test
    void updateAccountReferencesInAccess_ignoreExtraReferences() {
        AccountAccess existingAccess = jsonReader.getObjectFromFile("json/service/account/account-access.json", AccountAccess.class);
        AccountAccess newAccess = jsonReader.getObjectFromFile("json/service/account/account-access-aspsp-extra-references.json", AccountAccess.class);
        AccountAccess expectedAccess = jsonReader.getObjectFromFile("json/service/account/account-access-extra-updated.json", AccountAccess.class);

        AccountAccess updatedAccess = accountAccessUpdater.updateAccountReferencesInAccess(existingAccess, newAccess);

        assertEquals(expectedAccess, updatedAccess);
    }
}
