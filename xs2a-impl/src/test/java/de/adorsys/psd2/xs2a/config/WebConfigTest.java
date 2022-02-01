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

package de.adorsys.psd2.xs2a.config;

import de.adorsys.psd2.xs2a.config.converter.MappingJackson2TextMessageConverter;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.HttpMessageConverter;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebConfigTest {
    @Test
    void extendMessageConverters() {
        WebConfig webConfig = new WebConfig(null, null,
                                            null, null, null,
                                            null, null, null,
                                            null, null, null,
                                            null, null, null);
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();

        webConfig.extendMessageConverters(messageConverters);

        assertEquals(1, messageConverters.size());
        assertTrue(messageConverters.get(0) instanceof MappingJackson2TextMessageConverter);
    }
}
