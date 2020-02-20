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
        expectedLinks = new Links();
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
