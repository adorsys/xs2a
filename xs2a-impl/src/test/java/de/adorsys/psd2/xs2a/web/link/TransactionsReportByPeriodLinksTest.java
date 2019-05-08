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
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TransactionsReportByPeriodLinksTest {

    private static final String HTTP_URL = "http://url";
    private static final String ACCOUNT_ID = "33333-999999999";
    private Links expectedLinks;

    @Before
    public void setUp() {
        expectedLinks = new Links();
    }

    @Test
    public void success() {
        TransactionsReportByPeriodLinks links = new TransactionsReportByPeriodLinks(HTTP_URL, ACCOUNT_ID);

        expectedLinks.setAccount("http://url/v1/accounts/33333-999999999");
        assertEquals(expectedLinks, links);
    }
}
