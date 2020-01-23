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

import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TppRedirectUriMapperTest {

    private TppRedirectUriMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TppRedirectUriMapper();
    }

    @Test
    void mapToTppRedirectUri_success() {
        TppRedirectUri tppRedirectUri = mapper.mapToTppRedirectUri("ok_url", "nok_url");
        assertNotNull(tppRedirectUri);
        assertEquals("ok_url", tppRedirectUri.getUri());
        assertEquals("nok_url", tppRedirectUri.getNokUri());
    }

    @Test
    void mapToTppRedirectUri_blankValues() {
        TppRedirectUri tppRedirectUri = mapper.mapToTppRedirectUri("", "");
        assertNotNull(tppRedirectUri);
        assertEquals("", tppRedirectUri.getUri());
        assertEquals("", tppRedirectUri.getNokUri());
    }
}
