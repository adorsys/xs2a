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

import com.fasterxml.jackson.core.type.TypeReference;
import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.sca.ChallengeData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorizationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthenticationObject;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisCancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {AuthorisationModelMapperImpl.class, AuthorisationModelMapperTest.TestConfiguration.class})
public class AuthorisationModelMapperTest {
    @Autowired
    private AuthorisationModelMapper authorisationModelMapper;

    @Autowired
    private HrefLinkMapper mockHrefLinkMapper;
    @Autowired
    private CoreObjectsMapper mockCoreObjectsMapper;
    @Autowired
    private ScaMethodsMapper mockScaMethodsMapper;
    @Autowired
    private ChosenScaMethodMapper mockChosenScaMethodMapper;


    private JsonReader jsonReader = new JsonReader();

    @Before
    public void setUp() {
        when(mockCoreObjectsMapper.mapToModelScaStatus(ScaStatus.RECEIVED)).thenReturn(de.adorsys.psd2.model.ScaStatus.RECEIVED);

        //noinspection unchecked
        Map<String, HrefType> links = jsonReader.getObjectFromFile("json/web/mapper/links.json", Map.class);
        Links xs2aLinks = jsonReader.getObjectFromFile("json/web/mapper/xs2a-links.json", Links.class);
        when(mockHrefLinkMapper.mapToLinksMap(xs2aLinks)).thenReturn(links);

        Xs2aAuthenticationObject xs2aAuthenticationObject = jsonReader.getObjectFromFile("json/web/mapper/chosenScaMethod.json", Xs2aAuthenticationObject.class);
        ChosenScaMethod chosenScaMethod = jsonReader.getObjectFromFile("json/web/mapper/chosenScaMethod.json", ChosenScaMethod.class);
        when(mockChosenScaMethodMapper.mapToChosenScaMethod(xs2aAuthenticationObject)).thenReturn(chosenScaMethod);

        List<Xs2aAuthenticationObject> xs2aAuthenticationObjects = jsonReader.getObjectFromFile("json/web/mapper/scaMethods.json", new TypeReference<List<Xs2aAuthenticationObject>>() {
        });
        ScaMethods scaMethods = jsonReader.getObjectFromFile("json/web/mapper/scaMethods.json", ScaMethods.class);
        when(mockScaMethodsMapper.mapToScaMethods(xs2aAuthenticationObjects)).thenReturn(scaMethods);

        ChallengeData xs2aChallengeData = jsonReader.getObjectFromFile("json/web/mapper/challengeData.json", ChallengeData.class);
        de.adorsys.psd2.model.ChallengeData modelChallengeData = jsonReader.getObjectFromFile("json/web/mapper/challengeData.json", de.adorsys.psd2.model.ChallengeData.class);
        when(mockCoreObjectsMapper.mapToChallengeData(xs2aChallengeData)).thenReturn(modelChallengeData);
    }

    @After
    public void resetMocks() {
        // Resetting is necessary because these mocks are injected into the mapper as singleton beans
        // and are not being recreated after each test
        Mockito.reset(mockHrefLinkMapper, mockCoreObjectsMapper, mockCoreObjectsMapper, mockChosenScaMethodMapper);
    }

    @Test
    public void mapToStartScaProcessResponse_withConsentResponse() {
        // Given
        CreateConsentAuthorizationResponse objectFromFile = jsonReader.getObjectFromFile("json/web/mapper/CreateConsentAuthorizationResponse.json", CreateConsentAuthorizationResponse.class);
        StartScaprocessResponse expected = jsonReader.getObjectFromFile("json/web/mapper/ConsentStartScaProcessResponse.json", StartScaprocessResponse.class);

        // When
        StartScaprocessResponse actual = authorisationModelMapper.mapToStartScaProcessResponse(objectFromFile);

        // Then
        assertEquals(expected, actual);

        verify(mockCoreObjectsMapper).mapToModelScaStatus(any(ScaStatus.class));
        verify(mockHrefLinkMapper).mapToLinksMap(any(Links.class));

        assertNotNull(actual.getAuthorisationId());
        assertNotNull(actual.getScaStatus());
        assertNotNull(actual.getPsuMessage());
        assertFalse(actual.getLinks().isEmpty());
    }

    @Test
    public void mapToStartScaProcessResponse_withNullConsentResponse_shouldReturnNull() {
        // When
        StartScaprocessResponse actual = authorisationModelMapper.mapToStartScaProcessResponse((CreateConsentAuthorizationResponse) null);

        // Then
        assertNull(actual);
    }

    @Test
    public void mapToStartScaProcessResponse_withPaymentResponse() {
        // Given
        Xs2aCreatePisAuthorisationResponse objectFromFile = jsonReader.getObjectFromFile("json/web/mapper/Xs2aCreatePisAuthorisationResponse.json", Xs2aCreatePisAuthorisationResponse.class);
        StartScaprocessResponse expected = jsonReader.getObjectFromFile("json/web/mapper/PaymentStartScaProcessResponse.json", StartScaprocessResponse.class);

        // When
        StartScaprocessResponse actual = authorisationModelMapper.mapToStartScaProcessResponse(objectFromFile);

        // Then
        assertEquals(expected, actual);

        verify(mockCoreObjectsMapper).mapToModelScaStatus(any(ScaStatus.class));
        verify(mockHrefLinkMapper).mapToLinksMap(any(Links.class));

        assertNotNull(actual.getAuthorisationId());
        assertNotNull(actual.getScaStatus());
        assertFalse(actual.getLinks().isEmpty());
    }

    @Test
    public void mapToStartScaProcessResponse_withNullPaymentResponse_shouldReturnNull() {
        // When
        StartScaprocessResponse actual = authorisationModelMapper.mapToStartScaProcessResponse((Xs2aCreatePisAuthorisationResponse) null);

        // Then
        assertNull(actual);
    }

    @Test
    public void mapToStartCancellationScaProcessResponse_withPaymentResponse() {
        // Given
        Xs2aCreatePisCancellationAuthorisationResponse objectFromFile = jsonReader.getObjectFromFile("json/web/mapper/Xs2aCreatePisCancellationAuthorisationResponse.json", Xs2aCreatePisCancellationAuthorisationResponse.class);
        StartCancellationScaProcessResponse expected = jsonReader.getObjectFromFile("json/web/mapper/StartCancellationScaProcessResponse.json", StartCancellationScaProcessResponse.class);

        // When
        StartCancellationScaProcessResponse actual = authorisationModelMapper.mapToStartCancellationScaProcessResponse(objectFromFile);

        // Then
        assertEquals(expected, actual);

        verify(mockCoreObjectsMapper).mapToModelScaStatus(any(ScaStatus.class));
        verify(mockHrefLinkMapper).mapToLinksMap(any(Links.class));

        assertNotNull(actual.getCancellationId());
        assertNotNull(actual.getScaStatus());
        assertFalse(actual.getLinks().isEmpty());
    }

    @Test
    public void mapToStartCancellationScaProcessResponse_withNullPaymentResponse_shouldReturnNull() {
        // When
        StartCancellationScaProcessResponse actual = authorisationModelMapper.mapToStartCancellationScaProcessResponse(null);

        // Then
        assertNull(actual);
    }

    @Test
    public void mapToUpdateCancellationPsuAuthenticationResponse() {
        // Given
        Xs2aUpdatePisCommonPaymentPsuDataResponse objectFromFile = jsonReader.getObjectFromFile("json/web/mapper/Xs2aUpdatePisCommonPaymentPsuDataResponse.json", Xs2aUpdatePisCommonPaymentPsuDataResponse.class);
        UpdateCancellationPsuAuthenticationResponse expected = jsonReader.getObjectFromFile("json/web/mapper/UpdateCancellationPsuAuthenticationResponse.json", UpdateCancellationPsuAuthenticationResponse.class);

        // When
        UpdateCancellationPsuAuthenticationResponse actual = authorisationModelMapper.mapToUpdateCancellationPsuAuthenticationResponse(objectFromFile);

        // Then
        assertEquals(expected, actual);

        verify(mockCoreObjectsMapper).mapToModelScaStatus(any(ScaStatus.class));
        verify(mockHrefLinkMapper).mapToLinksMap(any(Links.class));

        assertNotNull(actual.getChosenScaMethod());
        assertNotNull(actual.getChallengeData());
        assertNotNull(actual.getScaMethods());
        assertNotNull(actual.getPsuMessage());
        assertNotNull(actual.getCancellationId());
        assertNotNull(actual.getScaStatus());
        assertFalse(actual.getLinks().isEmpty());
    }

    @Test
    public void mapToUpdateCancellationPsuAuthenticationResponse_withNullXs2aResponse_shouldReturnNull() {
        // When
        UpdateCancellationPsuAuthenticationResponse actual = authorisationModelMapper.mapToUpdateCancellationPsuAuthenticationResponse(null);

        // Then
        assertNull(actual);
    }

    @Test
    public void mapToStartOrUpdateCancellationResponse_withCreateResponse_shouldReturnStartResponse() {
        // Given
        Xs2aCreatePisCancellationAuthorisationResponse objectFromFile = jsonReader.getObjectFromFile("json/web/mapper/Xs2aCreatePisCancellationAuthorisationResponse.json", Xs2aCreatePisCancellationAuthorisationResponse.class);
        StartCancellationScaProcessResponse expected = jsonReader.getObjectFromFile("json/web/mapper/StartCancellationScaProcessResponse.json", StartCancellationScaProcessResponse.class);

        // When
        Object actual = authorisationModelMapper.mapToStartOrUpdateCancellationResponse(objectFromFile);

        // Then
        assertTrue(actual instanceof StartCancellationScaProcessResponse);

        StartCancellationScaProcessResponse actualStartResponse = (StartCancellationScaProcessResponse) actual;
        assertEquals(expected, actual);

        verify(mockCoreObjectsMapper).mapToModelScaStatus(any(ScaStatus.class));
        verify(mockHrefLinkMapper).mapToLinksMap(any(Links.class));

        assertNotNull(actualStartResponse.getCancellationId());
        assertNotNull(actualStartResponse.getScaStatus());
        assertFalse(actualStartResponse.getLinks().isEmpty());
    }

    @Test
    public void mapToStartOrUpdateCancellationResponse_withUpdateResponse_shouldReturnUpdateResponse() {
        // Given
        Xs2aUpdatePisCommonPaymentPsuDataResponse objectFromFile = jsonReader.getObjectFromFile("json/web/mapper/Xs2aUpdatePisCommonPaymentPsuDataResponse.json", Xs2aUpdatePisCommonPaymentPsuDataResponse.class);
        UpdateCancellationPsuAuthenticationResponse expected = jsonReader.getObjectFromFile("json/web/mapper/UpdateCancellationPsuAuthenticationResponse.json", UpdateCancellationPsuAuthenticationResponse.class);

        // When
        Object actual = authorisationModelMapper.mapToStartOrUpdateCancellationResponse(objectFromFile);

        // Then
        assertTrue(actual instanceof UpdateCancellationPsuAuthenticationResponse);

        UpdateCancellationPsuAuthenticationResponse actualUpdateResponse = (UpdateCancellationPsuAuthenticationResponse) actual;
        assertEquals(expected, actual);

        verify(mockCoreObjectsMapper).mapToModelScaStatus(any(ScaStatus.class));
        verify(mockHrefLinkMapper).mapToLinksMap(any(Links.class));

        assertNotNull(actualUpdateResponse.getChosenScaMethod());
        assertNotNull(actualUpdateResponse.getChallengeData());
        assertNotNull(actualUpdateResponse.getScaMethods());
        assertNotNull(actualUpdateResponse.getPsuMessage());
        assertNotNull(actualUpdateResponse.getCancellationId());
        assertNotNull(actualUpdateResponse.getScaStatus());
        assertFalse(actualUpdateResponse.getLinks().isEmpty());
    }

    @Test
    public void mapToStartOrUpdateCancellationResponse_withNullCancellationResponse_shouldReturnNull() {
        // When
        Object actual = authorisationModelMapper.mapToStartOrUpdateCancellationResponse(null);

        // Then
        assertNull(actual);
    }

    @Configuration
    public static class TestConfiguration {
        @Bean
        public HrefLinkMapper mockHrefLinkMapper() {
            return mock(HrefLinkMapper.class);
        }

        @Bean
        public CoreObjectsMapper mockCoreObjectsMapper() {
            return mock(CoreObjectsMapper.class);
        }

        @Bean
        public ScaMethodsMapper mockScaMethodsMapper() {
            return mock(ScaMethodsMapper.class);
        }

        @Bean
        public ChosenScaMethodMapper mockChosenScaMethodMapper() {
            return mock(ChosenScaMethodMapper.class);
        }
    }
}
