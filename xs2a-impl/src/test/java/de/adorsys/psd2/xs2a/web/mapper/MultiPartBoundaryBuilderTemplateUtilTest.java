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

package de.adorsys.psd2.xs2a.web.mapper;

import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MultiPartBoundaryBuilderTemplateUtilTest {

    private static final String TEMPLATE_PATH = "/template" + "/multipart-payment-template.txt";

    private final JsonReader jsonReader = new JsonReader();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(MultiPartBoundaryBuilderTemplateUtil.class, "template", null);
    }

    @Test
    void getTemplate_exception() {
        String actual = MultiPartBoundaryBuilderTemplateUtil.getTemplate("random");
        assertThat(actual).isNull();
    }

    @Test
    void getTemplate() {

        String actual = MultiPartBoundaryBuilderTemplateUtil.getTemplate(TEMPLATE_PATH);

        String expected = jsonReader.getStringFromFile("txt/boundary-template.txt");

        assertEquals(expected, actual);
    }
}
