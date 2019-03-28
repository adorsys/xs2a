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

package de.adorsys.psd2.xs2a.web;

import de.adorsys.psd2.xs2a.service.RequestProviderService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LinkExtractorTest {
    private static final UUID X_REQUEST_ID = UUID.fromString("1af360bc-13cb-40ab-9aa0-cc0d6af4510c");
    private static final String LINK_NAME = "link";
    private static final String LINK_VALUE = "actual link value";
    private static final String HREF = "href";

    @Mock
    private RequestProviderService requestProviderService;

    @InjectMocks
    private LinkExtractor linkExtractor;

    @Before
    public void setUp() {
        when(requestProviderService.getRequestId()).thenReturn(X_REQUEST_ID);
    }

    @Test
    public void extract_withValidMap_shouldReturnExtractedLink() {
        // Given
        Map<String, Map<String, String>> linksMap = Collections.singletonMap(LINK_NAME,
                                                                             Collections.singletonMap(HREF, LINK_VALUE));

        // When
        Optional<String> actualLink = linkExtractor.extract(linksMap, LINK_NAME);

        // Then
        assertTrue(actualLink.isPresent());
        assertEquals(LINK_VALUE, actualLink.get());
    }

    @Test
    public void extract_withNoLinkInMap_shouldReturnEmpty() {
        // Given
        Map<String, Map<String, String>> linksMap = Collections.singletonMap("another link",
                                                                             Collections.singletonMap(HREF, "another link value"));

        // When
        Optional<String> actualLink = linkExtractor.extract(linksMap, LINK_NAME);

        // Then
        assertFalse(actualLink.isPresent());
    }

    @Test
    public void extract_withNullMap_shouldReturnEmpty() {
        // When
        Optional<String> actualLink = linkExtractor.extract(null, LINK_NAME);

        // Then
        assertFalse(actualLink.isPresent());
    }

    @Test
    public void extract_withEmptyMap_shouldReturnEmpty() {
        // When
        Optional<String> actualLink = linkExtractor.extract(Collections.emptyMap(), LINK_NAME);

        // Then
        assertFalse(actualLink.isPresent());
    }

    @Test
    public void extract_withMalformedMap_shouldReturnEmpty() {
        // Given
        Map<String, String> malformedMap = Collections.singletonMap(LINK_NAME, "some malformed value");

        // When
        Optional<String> actualLink = linkExtractor.extract(malformedMap, LINK_NAME);

        // Then
        assertFalse(actualLink.isPresent());
    }
}
