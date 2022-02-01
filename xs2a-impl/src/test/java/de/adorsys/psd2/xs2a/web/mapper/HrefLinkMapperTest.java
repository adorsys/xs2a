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

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.Links;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HrefLinkMapperTest {
    private static final String LINK_NAME = "scaStatus";
    private static final String LINK_PATH = "http://localhost/v1/payments/";

    private HrefLinkMapper hrefMapper;

    @BeforeEach
    void setUp() {
        Xs2aObjectMapper xs2aObjectMapper = new Xs2aObjectMapper();
        hrefMapper = new HrefLinkMapper(xs2aObjectMapper);
    }

    @Test
    void mapToLinksMap_withValidMap_shouldReturnLinks() {
        // Given
        Links links = new Links();
        links.setScaStatus(new HrefType(LINK_PATH));

        //When:
        Map<String, HrefType> linkMap = hrefMapper.mapToLinksMap(links);

        //Then:
        assertNotNull(linkMap);

        HrefType wrappedLink = linkMap.get(LINK_NAME);

        assertNotNull(wrappedLink);
        assertEquals(LINK_PATH, wrappedLink.getHref());
    }

    @Test
    void mapToLinksMap_withNullMap_shouldReturnNull() {
        //When:
        Map<String, HrefType> linkMap = hrefMapper.mapToLinksMap(null);

        //Then:
        assertNull(linkMap);
    }
}
