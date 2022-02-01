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

import static org.assertj.core.api.Assertions.assertThat;

class Xs2aAccountReportLinksTest {
    private static final String HTTP_URL = "http://url";
    private static final String ACCOUNT_ID = "33333-999999999";
    private Links expectedLinks;

    private JsonReader jsonReader = new JsonReader();

    @BeforeEach
    void setUp() {
        expectedLinks = new AbstractLinks(HTTP_URL);
        expectedLinks.setAccount(new HrefType(HTTP_URL + "/v1/accounts/" + ACCOUNT_ID));
    }

    @Test
    void links_notNull() {
        // Given
        Links inputLinks = jsonReader.getObjectFromFile("json/link/test-links.json", Links.class);

        // When
        Xs2aAccountReportLinks actual = new Xs2aAccountReportLinks(HTTP_URL, ACCOUNT_ID,  inputLinks);

        // Then
        assertThat(actual).isEqualTo(expectedLinks);
    }

}
