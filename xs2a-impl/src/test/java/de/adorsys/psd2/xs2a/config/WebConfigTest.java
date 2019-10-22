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

package de.adorsys.psd2.xs2a.config;

import de.adorsys.psd2.xs2a.config.converter.MappingJackson2TextMessageConverter;
import org.junit.Test;
import org.springframework.http.converter.HttpMessageConverter;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WebConfigTest {

    @Test
    public void extendMessageConverters() {
        WebConfig webConfig = new WebConfig(null, null, null,
                                            null, null, null,
                                            null, null, null,
                                            null, null, null);
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();

        webConfig.extendMessageConverters(messageConverters);

        assertEquals(1, messageConverters.size());
        assertTrue(messageConverters.get(0) instanceof MappingJackson2TextMessageConverter);
    }
}
