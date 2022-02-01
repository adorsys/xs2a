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
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransactionsReportDownloadLinksTest {

    private static final String HTTP_URL = "http://url";
    private static final String ACCOUNT_ID = "33333-999999999";
    private static final String BALANCES_LINK = "http://url/v1/accounts/33333-999999999/balances";
    private Links expectedLinks;

    private JsonReader jsonReader = new JsonReader();

    @BeforeEach
    void setUp() {
        expectedLinks = new AbstractLinks(HTTP_URL);
        expectedLinks.setDownload(new HrefType(HTTP_URL + "/v1/accounts/" + ACCOUNT_ID + "/transactions/download/encoded-string"));
    }

    @Test
    void success_noBalance() {
        // Given
        boolean withBalance = false;

        // When
        TransactionsReportDownloadLinks links = new TransactionsReportDownloadLinks(HTTP_URL, ACCOUNT_ID, withBalance, "encoded-string");

        // Then
        assertEquals(expectedLinks, links);
    }

    @Test
    void success_with_balance() {
        // Given
        boolean withBalance = true;

        // When
        TransactionsReportDownloadLinks links = new TransactionsReportDownloadLinks(HTTP_URL, ACCOUNT_ID, withBalance, "encoded-string");

        expectedLinks.setBalances(new HrefType(BALANCES_LINK));

        // Then
        assertEquals(expectedLinks, links);
    }
}
