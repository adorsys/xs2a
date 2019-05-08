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

package de.adorsys.psd2.xs2a.web.link;

import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AccountDetailsLinksTest {
    private static final String HTTP_URL = "http://url";
    private static final String ACCOUNT_ID = "33333-999999999";
    private static final String WRONG_ACCOUNT_ID = "wrong_account_id";

    private Xs2aAccountAccess xs2aAccountAccess;
    private Links expectedLinks;

    @Before
    public void setUp() {
        JsonReader jsonReader = new JsonReader();
        xs2aAccountAccess = jsonReader.getObjectFromFile("json/link/account_access.json", Xs2aAccountAccess.class);
        expectedLinks = new Links();
    }

    @Test
    public void create_success() {
        AccountDetailsLinks links = new AccountDetailsLinks(HTTP_URL, ACCOUNT_ID, xs2aAccountAccess);

        expectedLinks.setBalances("http://url/v1/accounts/33333-999999999/balances");
        expectedLinks.setTransactions("http://url/v1/accounts/33333-999999999/transactions");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void create_balancesDoNotMatchByAccountId() {
        xs2aAccountAccess.getBalances().get(0).setResourceId(WRONG_ACCOUNT_ID);
        AccountDetailsLinks links = new AccountDetailsLinks(HTTP_URL, ACCOUNT_ID, xs2aAccountAccess);

        expectedLinks.setTransactions("http://url/v1/accounts/33333-999999999/transactions");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void create_transactionsDoNotMatchByAccountId() {
        xs2aAccountAccess.getTransactions().get(0).setResourceId(WRONG_ACCOUNT_ID);
        AccountDetailsLinks links = new AccountDetailsLinks(HTTP_URL, ACCOUNT_ID, xs2aAccountAccess);

        expectedLinks.setBalances("http://url/v1/accounts/33333-999999999/balances");
        assertEquals(expectedLinks, links);
    }
}
