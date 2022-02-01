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

import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.Links;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransactionsReportCardLinksTest {

    private static final String HTTP_URL = "http://url";
    private static final String ACCOUNT_ID = "33333-999999999";
    private Links expectedLinks;

    @BeforeEach
    void setUp() {
        expectedLinks = new AbstractLinks(HTTP_URL);
    }

    @Test
    void success_noBalance() {
        boolean withOutBalance = false;

        TransactionsReportCardLinks links = new TransactionsReportCardLinks(HTTP_URL, ACCOUNT_ID, withOutBalance);

        expectedLinks.setAccount(new HrefType("http://url/v1/card-accounts/33333-999999999"));
        assertEquals(expectedLinks, links);
    }

    @Test
    void success_with_balance() {
        boolean withBalance = true;

        TransactionsReportCardLinks links = new TransactionsReportCardLinks(HTTP_URL, ACCOUNT_ID, withBalance);

        expectedLinks.setAccount(new HrefType("http://url/v1/card-accounts/33333-999999999"));
        expectedLinks.setBalances(new HrefType("http://url/v1/card-accounts/33333-999999999/balances"));
        assertEquals(expectedLinks, links);
    }
}
