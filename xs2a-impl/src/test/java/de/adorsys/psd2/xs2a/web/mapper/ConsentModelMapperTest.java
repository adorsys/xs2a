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

import de.adorsys.psd2.model.AuthenticationObject;
import de.adorsys.psd2.model.ConsentsResponse201;
import de.adorsys.psd2.model.ScaMethods;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentResponse;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConsentModelMapperTest {

    private final static String CONSENT_STATUS = "received";
    private final static String CONSENT_ID = "S7tlYXaar8j7l5IMK89iNJB8SkG5ricoOaEYHyku_AO9BF6MIP29SN_tXtDvaQb3c8b_NsohCWlFlYN0ds8u89WFnjze07vwpAgFM45MlQk=_=_psGLvQpt9Q";
    private final static String PSU_MESSAGE = "This test message is created in ASPSP and directed to PSU";
    private final static String SELF_LINK = "self";
    private final static String HREF = "href";
    private final static String LOCALHOST_LINK = "http://localhost";

    @InjectMocks
    private ConsentModelMapper consentModelMapper;

    @Mock
    private HrefLinkMapper hrefLinkMapper;

    @Mock
    private ScaMethodsMapper scaMethodsMapper;

    private CreateConsentResponse createConsentResponseWithScaMethods;
    private CreateConsentResponse createConsentResponseWithoutScaMethods;

    @Before
    public void setUp() {
        JsonReader jsonReader = new JsonReader();
        createConsentResponseWithScaMethods = jsonReader.getObjectFromFile("json/service/mapper/create-consent-response-with-sca-methods.json", CreateConsentResponse.class);
        createConsentResponseWithoutScaMethods = jsonReader.getObjectFromFile("json/service/mapper/create-consent-response-without-sca-methods.json", CreateConsentResponse.class);

        when(hrefLinkMapper.mapToLinksMap(any(Links.class))).thenReturn(buildLinks());
    }

    @Test
    public void mapToConsentsResponse201_withScaMethods_shouldReturnArrayOfThem() {
        // Given
        ScaMethods methods = new ScaMethods();
        methods.add(new AuthenticationObject());
        when(scaMethodsMapper.mapToScaMethods(anyList())).thenReturn(methods);

        // When
        ConsentsResponse201 actual = consentModelMapper.mapToConsentsResponse201(createConsentResponseWithScaMethods);

        // Then
        checkCommonFields(actual);
        assertFalse(actual.getScaMethods().isEmpty());
    }

    @Test
    public void mapToConsentsResponse201_withoutScaMethods_shouldNotReturnEmptyArray() {
        // When
        ConsentsResponse201 actual = consentModelMapper.mapToConsentsResponse201(createConsentResponseWithoutScaMethods);

        // Then
        checkCommonFields(actual);
        assertNull(actual.getScaMethods());
    }

    private void checkCommonFields(ConsentsResponse201 actual) {
        assertNotNull(actual);
        assertEquals(CONSENT_STATUS, actual.getConsentStatus().toString());
        assertEquals(CONSENT_ID, actual.getConsentId());
        assertEquals(PSU_MESSAGE, actual.getPsuMessage());
        assertFalse(actual.getLinks().isEmpty());

        assertNotNull(actual.getLinks().get(SELF_LINK));
        Map<String, String> selfMap = (Map<String, String>) actual.getLinks().get(SELF_LINK);
        assertEquals(LOCALHOST_LINK, selfMap.get(HREF));
    }

    private Map<String, Map<String, String>> buildLinks() {
        return Collections.singletonMap(SELF_LINK, Collections.singletonMap(HREF, LOCALHOST_LINK));
    }
}
