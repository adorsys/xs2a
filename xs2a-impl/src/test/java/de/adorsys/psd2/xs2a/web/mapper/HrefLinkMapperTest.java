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

package de.adorsys.psd2.xs2a.web.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.xs2a.domain.Links;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;

public class HrefLinkMapperTest {
    private static final String LINK_NAME = "scaStatus";
    private static final String LINK_PATH = "http://localhost/v1/payments/";
    private static final String HREF = "href";

    private HrefLinkMapper hrefMapper;

    @Before
    public void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        hrefMapper = new HrefLinkMapper(objectMapper);
    }

    @Test
    public void mapToLinksMapWhenStringString() {
        //When:
        Map<String, Map<String, String>> linkMap = hrefMapper.mapToLinksMap(LINK_NAME, LINK_PATH);
        Map<String, String> hrefMap = linkMap.get(LINK_NAME);

        //Then:
        assertNotNull(linkMap);
        assertFalse(linkMap.isEmpty());
        assertEquals(1, linkMap.size());
        assertTrue(linkMap.containsKey(LINK_NAME));

        assertNotNull(hrefMap);
        assertFalse(hrefMap.isEmpty());
        assertEquals(1, hrefMap.size());
        assertTrue(hrefMap.containsKey(HREF));
        assertTrue(hrefMap.containsValue(LINK_PATH));
    }

    @Test
    public void mapToLinksMapWhenStringNull() {
        //When:
        Map<String, Map<String, String>> linkMap = hrefMapper.mapToLinksMap(LINK_NAME, null);
        Map<String, String> hrefMap = linkMap.get(LINK_NAME);

        //Then:
        assertNotNull(linkMap);
        assertFalse(linkMap.isEmpty());
        assertEquals(1, linkMap.size());
        assertTrue(linkMap.containsKey(LINK_NAME));

        assertNull(hrefMap);
    }

    @Test
    public void mapToLinksMap_withValidMap_shouldReturnLinks() {
        // Given
        Links links = new Links();
        links.setScaStatus(LINK_PATH);

        //When:
        Map<String, Map<String, String>> linkMap = hrefMapper.mapToLinksMap(links);

        //Then:
        assertNotNull(linkMap);

        Map<String, String> wrappedLink = linkMap.get(LINK_NAME);

        assertNotNull(wrappedLink);
        assertEquals(LINK_PATH, wrappedLink.get(HREF));
    }

    @Test
    public void mapToLinksMap_withNullMap_shouldReturnNull() {
        //When:
        Map<String, Map<String, String>> linkMap = hrefMapper.mapToLinksMap(null);

        //Then:
        assertNull(linkMap);
    }
}
