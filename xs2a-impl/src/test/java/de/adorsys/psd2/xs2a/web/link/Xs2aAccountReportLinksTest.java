/*
 * Copyright 2018-2021 adorsys GmbH & Co KG
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
