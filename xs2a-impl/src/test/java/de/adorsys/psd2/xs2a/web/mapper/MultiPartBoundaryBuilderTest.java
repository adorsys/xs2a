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

package de.adorsys.psd2.xs2a.web.mapper;

import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;

import static de.adorsys.psd2.xs2a.web.mapper.MultiPartBoundaryBuilder.DEFAULT_BOUNDARY;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

        assertEquals(content.replaceAll("BOUNDARY", boundary), actual);
    }

    @Test
    void getMultiPartContent_emptyBoundary_default() {
        request.addHeader(HttpHeaders.CONTENT_TYPE, "multipart/form-data;");

        String actual = builder.getMultiPartContent(request, xmlPart, jsonPart);

        assertEquals(content.replaceAll("BOUNDARY", DEFAULT_BOUNDARY.replace("--", "")), actual);
    }
}
