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
