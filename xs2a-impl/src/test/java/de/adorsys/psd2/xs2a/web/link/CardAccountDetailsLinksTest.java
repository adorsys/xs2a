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

package de.adorsys.psd2.xs2a.web.link;

import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CardAccountDetailsLinksTest {
    private static final String HTTP_URL = "http://url";
    private static final String ACCOUNT_ID = "33333-999999999";
    private static final String WRONG_ACCOUNT_ID = "wrong_account_id";

    private AccountAccess accountAccess;
    private AisConsent aisConsent;
    private Links expectedLinks;

    @BeforeEach
    void setUp() {
        JsonReader jsonReader = new JsonReader();
        accountAccess = jsonReader.getObjectFromFile("json/link/account_access.json", AccountAccess.class);
        aisConsent = new AisConsent();
        aisConsent.setAspspAccountAccesses(accountAccess);
        aisConsent.setConsentData(new AisConsentData(null, null, null, true));
        expectedLinks = new AbstractLinks(HTTP_URL);
    }

    @Test
    void create_success() {
        CardAccountDetailsLinks links = new CardAccountDetailsLinks(HTTP_URL, ACCOUNT_ID, aisConsent);

        expectedLinks.setBalances(new HrefType("http://url/v1/card-accounts/33333-999999999/balances"));
        expectedLinks.setTransactions(new HrefType("http://url/v1/card-accounts/33333-999999999/transactions"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void create_globalConsent_success() {
        aisConsent.setConsentData(new AisConsentData(null, AccountAccessType.ALL_ACCOUNTS, null, true));
        CardAccountDetailsLinks links = new CardAccountDetailsLinks(HTTP_URL, ACCOUNT_ID, aisConsent);

        expectedLinks.setBalances(new HrefType("http://url/v1/card-accounts/33333-999999999/balances"));
        expectedLinks.setTransactions(new HrefType("http://url/v1/card-accounts/33333-999999999/transactions"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void create_balancesDoNotMatchByAccountId() {
        accountAccess.getBalances().get(0).setResourceId(WRONG_ACCOUNT_ID);
        CardAccountDetailsLinks links = new CardAccountDetailsLinks(HTTP_URL, ACCOUNT_ID, aisConsent);

        expectedLinks.setTransactions(new HrefType("http://url/v1/card-accounts/33333-999999999/transactions"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void create_transactionsDoNotMatchByAccountId() {
        accountAccess.getTransactions().get(0).setResourceId(WRONG_ACCOUNT_ID);
        CardAccountDetailsLinks links = new CardAccountDetailsLinks(HTTP_URL, ACCOUNT_ID, aisConsent);

        expectedLinks.setBalances(new HrefType("http://url/v1/card-accounts/33333-999999999/balances"));
        assertEquals(expectedLinks, links);
    }

}
