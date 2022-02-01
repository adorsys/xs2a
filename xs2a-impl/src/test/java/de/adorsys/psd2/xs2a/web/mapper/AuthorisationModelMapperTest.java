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

import com.fasterxml.jackson.core.type.TypeReference;
import de.adorsys.psd2.model.ChosenScaMethod;
import de.adorsys.psd2.model.ScaMethods;
import de.adorsys.psd2.model.StartScaprocessResponse;
import de.adorsys.psd2.model.UpdatePsuAuthenticationResponse;
import de.adorsys.psd2.xs2a.core.sca.ChallengeData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.authorisation.CancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorizationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisCancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AuthorisationModelMapperImpl.class, AuthorisationModelMapperTest.TestConfiguration.class})
class AuthorisationModelMapperTest {
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
    @Autowired
    private TppMessageGenericMapper tppMessageGenericMapper;

    @Mock
    private CancellationAuthorisationResponse cancellationAuthorisationResponse;

    private final JsonReader jsonReader = new JsonReader();

    @BeforeEach
    void setUp() {
        when(mockCoreObjectsMapper.mapToModelScaStatus(ScaStatus.RECEIVED)).thenReturn(de.adorsys.psd2.model.ScaStatus.RECEIVED);

        //noinspection unchecked
        Map<String, HrefType> links = jsonReader.getObjectFromFile("json/web/mapper/links.json", Map.class);
        Links xs2aLinks = jsonReader.getObjectFromFile("json/web/mapper/xs2a-links.json", Links.class);
        when(mockHrefLinkMapper.mapToLinksMap(xs2aLinks)).thenReturn(links);

        de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject xs2aAuthenticationObject = jsonReader.getObjectFromFile("json/web/mapper/chosenScaMethod.json", de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject.class);
        ChosenScaMethod chosenScaMethod = jsonReader.getObjectFromFile("json/web/mapper/chosenScaMethod.json", ChosenScaMethod.class);
        when(mockChosenScaMethodMapper.mapToChosenScaMethod(xs2aAuthenticationObject)).thenReturn(chosenScaMethod);

        List<de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject> xs2aAuthenticationObjects = jsonReader.getObjectFromFile("json/web/mapper/scaMethods.json", new TypeReference<List<de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject>>() {
        });
        ScaMethods scaMethods = jsonReader.getObjectFromFile("json/web/mapper/scaMethods.json", ScaMethods.class);
        when(mockScaMethodsMapper.mapToScaMethods(xs2aAuthenticationObjects)).thenReturn(scaMethods);

        ChallengeData xs2aChallengeData = jsonReader.getObjectFromFile("json/web/mapper/challengeData.json", ChallengeData.class);
        de.adorsys.psd2.model.ChallengeData modelChallengeData = jsonReader.getObjectFromFile("json/web/mapper/challengeData.json", de.adorsys.psd2.model.ChallengeData.class);
        when(mockCoreObjectsMapper.mapToChallengeData(xs2aChallengeData)).thenReturn(modelChallengeData);
    }

    @AfterEach
    void resetMocks() {
        // Resetting is necessary because these mocks are injected into the mapper as singleton beans
        // and are not being recreated after each test
        Mockito.reset(mockHrefLinkMapper, mockCoreObjectsMapper, mockCoreObjectsMapper, mockChosenScaMethodMapper);
    }

    @Test
    void mapToStartScaProcessResponse_withConsentResponse() {
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
    void mapToStartScaProcessResponse_withNullConsentResponse_shouldReturnNull() {
        // When
        StartScaprocessResponse actual = authorisationModelMapper.mapToStartScaProcessResponse((CreateConsentAuthorizationResponse) null);

        // Then
        assertNull(actual);
    }

    @Test
    void mapToStartScaProcessResponse_withPaymentResponse() {
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
    void mapToStartScaProcessResponse_withNullPaymentResponse_shouldReturnNull() {
        // When
        StartScaprocessResponse actual = authorisationModelMapper.mapToStartScaProcessResponse((Xs2aCreatePisAuthorisationResponse) null);

        // Then
        assertNull(actual);
    }

    @Test
    void mapToStartScaProcessResponseCancellation_withPaymentResponse() {
        // Given
        Xs2aCreatePisCancellationAuthorisationResponse objectFromFile = jsonReader.getObjectFromFile("json/web/mapper/Xs2aCreatePisCancellationAuthorisationResponse.json", Xs2aCreatePisCancellationAuthorisationResponse.class);
        StartScaprocessResponse expected = jsonReader.getObjectFromFile("json/web/mapper/StartScaprocessResponseCancellation.json", StartScaprocessResponse.class);

        // When
        StartScaprocessResponse actual = authorisationModelMapper.mapToStartScaProcessResponseCancellation(objectFromFile);

        // Then
        assertEquals(expected, actual);

        verify(mockCoreObjectsMapper).mapToModelScaStatus(any(ScaStatus.class));
        verify(mockHrefLinkMapper).mapToLinksMap(any(Links.class));

        assertNotNull(actual.getAuthorisationId());
        assertNotNull(actual.getScaStatus());
        assertFalse(actual.getLinks().isEmpty());
    }

    @Test
    void mapToStartScaProcessResponseCancellation_withNullPaymentResponse_shouldReturnNull() {
        // When
        StartScaprocessResponse actual = authorisationModelMapper.mapToStartScaProcessResponseCancellation(null);

        // Then
        assertNull(actual);
    }

    @Test
    void mapToUpdatePsuAuthenticationResponse() {
        // Given
        Xs2aUpdatePisCommonPaymentPsuDataResponse objectFromFile = jsonReader.getObjectFromFile("json/web/mapper/Xs2aUpdatePisCommonPaymentPsuDataResponse.json", Xs2aUpdatePisCommonPaymentPsuDataResponse.class);
        UpdatePsuAuthenticationResponse expected = jsonReader.getObjectFromFile("json/web/mapper/UpdatePsuAuthenticationResponse.json", UpdatePsuAuthenticationResponse.class);

        // When
        UpdatePsuAuthenticationResponse actual = authorisationModelMapper.mapToUpdatePsuAuthenticationResponse(objectFromFile);

        // Then
        assertEquals(expected, actual);

        verify(mockCoreObjectsMapper).mapToModelScaStatus(any(ScaStatus.class));
        verify(mockHrefLinkMapper).mapToLinksMap(any(Links.class));

        assertNotNull(actual.getChosenScaMethod());
        assertNotNull(actual.getChallengeData());
        assertNotNull(actual.getScaMethods());
        assertNotNull(actual.getPsuMessage());
        assertNotNull(actual.getAuthorisationId());
        assertNotNull(actual.getScaStatus());
        assertFalse(actual.getLinks().isEmpty());
    }

    @Test
    void mapToStartOrUpdateCancellationResponse_throwsException() {
        //When
        when(cancellationAuthorisationResponse.getAuthorisationResponseType()).thenReturn(null);
        //Then
        assertThrows(IllegalArgumentException.class,
            () -> authorisationModelMapper.mapToStartOrUpdateCancellationResponse(cancellationAuthorisationResponse));
    }

    @Test
    void mapToUpdatePsuAuthenticationResponse_withNullXs2aResponse_shouldReturnNull() {
        // When
        UpdatePsuAuthenticationResponse actual = authorisationModelMapper.mapToUpdatePsuAuthenticationResponse(null);

        // Then
        assertNull(actual);
    }

    @Test
    void mapToStartOrUpdateCancellationResponse_withCreateResponse_shouldReturnStartResponse() {
        // Given
        Xs2aCreatePisCancellationAuthorisationResponse objectFromFile = jsonReader.getObjectFromFile("json/web/mapper/Xs2aCreatePisCancellationAuthorisationResponse.json", Xs2aCreatePisCancellationAuthorisationResponse.class);
        StartScaprocessResponse expected = jsonReader.getObjectFromFile("json/web/mapper/StartScaprocessResponseCancellation.json", StartScaprocessResponse.class);

        // When
        Object actual = authorisationModelMapper.mapToStartOrUpdateCancellationResponse(objectFromFile);

        // Then
        assertTrue(actual instanceof StartScaprocessResponse);

        StartScaprocessResponse actualStartResponse = (StartScaprocessResponse) actual;
        assertEquals(expected, actual);

        verify(mockCoreObjectsMapper).mapToModelScaStatus(any(ScaStatus.class));
        verify(mockHrefLinkMapper).mapToLinksMap(any(Links.class));

        assertNotNull(actualStartResponse.getAuthorisationId());
        assertNotNull(actualStartResponse.getScaStatus());
        assertFalse(actualStartResponse.getLinks().isEmpty());
    }

    @Test
    void mapToStartOrUpdateCancellationResponse_withUpdateResponse_shouldReturnUpdateResponse() {
        // Given
        Xs2aUpdatePisCommonPaymentPsuDataResponse objectFromFile = jsonReader.getObjectFromFile("json/web/mapper/Xs2aUpdatePisCommonPaymentPsuDataResponse.json", Xs2aUpdatePisCommonPaymentPsuDataResponse.class);
        UpdatePsuAuthenticationResponse expected = jsonReader.getObjectFromFile("json/web/mapper/UpdatePsuAuthenticationResponse.json", UpdatePsuAuthenticationResponse.class);

        // When
        Object actual = authorisationModelMapper.mapToStartOrUpdateCancellationResponse(objectFromFile);

        // Then
        assertTrue(actual instanceof UpdatePsuAuthenticationResponse);

        UpdatePsuAuthenticationResponse actualUpdateResponse = (UpdatePsuAuthenticationResponse) actual;
        assertEquals(expected, actual);

        verify(mockCoreObjectsMapper).mapToModelScaStatus(any(ScaStatus.class));
        verify(mockHrefLinkMapper).mapToLinksMap(any(Links.class));

        assertNotNull(actualUpdateResponse.getChosenScaMethod());
        assertNotNull(actualUpdateResponse.getChallengeData());
        assertNotNull(actualUpdateResponse.getScaMethods());
        assertNotNull(actualUpdateResponse.getPsuMessage());
        assertNotNull(actualUpdateResponse.getAuthorisationId());
        assertNotNull(actualUpdateResponse.getScaStatus());
        assertFalse(actualUpdateResponse.getLinks().isEmpty());
    }

    @Test
    void mapToStartOrUpdateCancellationResponse_withNullCancellationResponse_shouldReturnNull() {
        // When
        Object actual = authorisationModelMapper.mapToStartOrUpdateCancellationResponse(null);

        // Then
        assertNull(actual);
    }

    @Configuration
    static class TestConfiguration {
        @Bean
        HrefLinkMapper mockHrefLinkMapper() {
            return mock(HrefLinkMapper.class);
        }

        @Bean
        CoreObjectsMapper mockCoreObjectsMapper() {
            return mock(CoreObjectsMapper.class);
        }

        @Bean
        ScaMethodsMapper mockScaMethodsMapper() {
            return mock(ScaMethodsMapper.class);
        }

        @Bean
        TppMessageGenericMapper mockTppMessageGenericMapper() {
            return mock(TppMessageGenericMapper.class);
        }

        @Bean
        ChosenScaMethodMapper mockChosenScaMethodMapper() {
            return mock(ChosenScaMethodMapper.class);
        }
    }
}
