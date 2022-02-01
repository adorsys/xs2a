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

package de.adorsys.psd2.xs2a.web.mapper;

import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;

import static de.adorsys.psd2.xs2a.web.mapper.MultiPartBoundaryBuilder.DEFAULT_BOUNDARY;
import static org.assertj.core.api.Assertions.assertThat;

class MultiPartBoundaryBuilderTest {

    private MultiPartBoundaryBuilder builder;
    private MockHttpServletRequest request;

    private String content;
    private String xmlPart;
    private String jsonPart;

    @BeforeEach
    void setUp() {
        builder = new MultiPartBoundaryBuilder();
        request = new MockHttpServletRequest();
        JsonReader jsonReader = new JsonReader();

        content = jsonReader.getStringFromFile("json/service/mapper/multi-part-boundary-response.txt").trim();
        xmlPart = jsonReader.getStringFromFile("json/service/mapper/multi-part-xml-request.xml").trim();
        jsonPart = jsonReader.getStringFromFile("json/service/mapper/multi-part-json-request.json").trim();
    }

    @Test
    void getMultiPartContent() {
        String boundary = "1RECORD2_3BOUNDARY";
        request.addHeader(HttpHeaders.CONTENT_TYPE, "multipart/form-data; boundary=" + boundary);

        String actual = builder.getMultiPartContent(request, xmlPart, jsonPart);
        String expected = content.replace("BOUNDARY", boundary);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getMultiPartContent_emptyBoundary_default() {
        request.addHeader(HttpHeaders.CONTENT_TYPE, "multipart/form-data;");

        String actual = builder.getMultiPartContent(request, xmlPart, jsonPart);
        String expected = content.replace("BOUNDARY", DEFAULT_BOUNDARY.replace("--", ""));

        assertThat(actual).isEqualTo(expected);
    }
}
