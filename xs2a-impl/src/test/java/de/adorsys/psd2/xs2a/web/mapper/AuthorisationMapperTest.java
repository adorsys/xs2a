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

import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorizationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthorisationSubResources;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
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
public class AuthorisationMapperTest {

    private static final String SELF_LINK = "self";
    private static final String LOCALHOST_LINK = "http://localhost";

    private static final de.adorsys.psd2.model.ScaStatus SCA_STATUS = de.adorsys.psd2.model.ScaStatus.RECEIVED;

    private JsonReader jsonReader = new JsonReader();

    @InjectMocks
    private AuthorisationMapper mapper;

    @Mock
    private HrefLinkMapper hrefLinkMapper;

    @Mock
    private ScaMethodsMapper scaMethodsMapper;

    @Mock
    private CoreObjectsMapper coreObjectsMapper;

    @Mock
    private ScaApproachResolver scaApproachResolver;

    @Mock
    private RedirectLinkBuilder redirectLinkBuilder;

    @Before
    public void setUp() {
        when(hrefLinkMapper.mapToLinksMap(any(Links.class))).thenReturn(buildLinks());
        when(coreObjectsMapper.mapToModelScaStatus(any(ScaStatus.class))).thenReturn(SCA_STATUS);
    }

    @Test
    public void mapToAuthorisations_equals_success() {
        // given
        Authorisations expectedAuthorisations = jsonReader.getObjectFromFile("json/service/mapper/AuthorisationMapper-Authorisations.json", Authorisations.class);
        Xs2aAuthorisationSubResources xs2AAuthorisationSubResources = jsonReader.getObjectFromFile("json/service/mapper/AuthorisationMapper-Xs2aAutorisationSubResources.json", Xs2aAuthorisationSubResources.class);

        // when
        Authorisations actualAuthorisations = mapper.mapToAuthorisations(xs2AAuthorisationSubResources);

        // then
        // TODO change yaml-generator to correct Authorisations.AuthorisationsList equals https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/871#
        assertArrayEquals(expectedAuthorisations.getAuthorisationIds().toArray(), actualAuthorisations.getAuthorisationIds().toArray());
    }

    @Test
    public void mapToPisCreateOrUpdateAuthorisationResponse_for_Xs2aCreatePisAuthorisationResponse() {
        // given
        StartScaprocessResponse expectedStartScaProcessResponse = jsonReader.getObjectFromFile("json/service/mapper/AuthorisationMapper-StartScaProcessResponse-expected.json", StartScaprocessResponse.class);

        Xs2aCreatePisAuthorisationResponse xs2aCreatePisAuthorisationResponse = jsonReader.getObjectFromFile("json/service/mapper/AuthorisationMapper-StartScaProcessResponse-ResponseObject.json", Xs2aCreatePisAuthorisationResponse.class);
        ResponseObject<Xs2aCreatePisAuthorisationResponse> responseObject = ResponseObject.<Xs2aCreatePisAuthorisationResponse>builder()
                                                                                .body(xs2aCreatePisAuthorisationResponse)
                                                                                .build();
        // when
        StartScaprocessResponse actualStartScaProcessResponse = (StartScaprocessResponse) mapper.mapToPisCreateOrUpdateAuthorisationResponse(responseObject);

        // then
        assertEquals(expectedStartScaProcessResponse.getScaStatus(), actualStartScaProcessResponse.getScaStatus());
        assertEquals(expectedStartScaProcessResponse.getAuthorisationId(), actualStartScaProcessResponse.getAuthorisationId());
        assertNotNull(actualStartScaProcessResponse.getLinks());
        assertFalse(actualStartScaProcessResponse.getLinks().isEmpty());
        assertNull(actualStartScaProcessResponse.getPsuMessage());
    }

    @Test
    public void mapToPisCreateOrUpdateAuthorisationResponse_for_Xs2aUpdatePisCommonPaymentPsuDataResponse() {
        // given
        UpdatePsuAuthenticationResponse expectedUpdatePsuAuthenticationResponse =
            jsonReader.getObjectFromFile("json/service/mapper/AuthorisationMapper-UpdatePsuAuthenticationResponse-expected.json", UpdatePsuAuthenticationResponse.class);

        Xs2aUpdatePisCommonPaymentPsuDataResponse xs2aUpdatePisCommonPaymentPsuDataResponse =
            jsonReader.getObjectFromFile("json/service/mapper/AuthorisationMapper-UpdatePsuAuthenticationResponse-ResponseObject.json", Xs2aUpdatePisCommonPaymentPsuDataResponse.class);
        ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> responseObject = ResponseObject.<Xs2aUpdatePisCommonPaymentPsuDataResponse>builder()
                                                                                       .body(xs2aUpdatePisCommonPaymentPsuDataResponse)
                                                                                       .build();
        ScaMethods methods = new ScaMethods();
        methods.add(new AuthenticationObject());
        when(scaMethodsMapper.mapToScaMethods(anyList())).thenReturn(methods);

        ChallengeData challengeData = new ChallengeData();
        when(coreObjectsMapper.mapToChallengeData(any(de.adorsys.psd2.xs2a.core.sca.ChallengeData.class))).thenReturn(challengeData);

        // when
        UpdatePsuAuthenticationResponse actualUpdatePsuAuthenticationResponse =
            (UpdatePsuAuthenticationResponse) mapper.mapToPisCreateOrUpdateAuthorisationResponse(responseObject);

        // then
        assertNotNull(actualUpdatePsuAuthenticationResponse.getLinks());
        assertFalse(actualUpdatePsuAuthenticationResponse.getLinks().isEmpty());

        assertFalse(actualUpdatePsuAuthenticationResponse.getScaMethods().isEmpty());

        // TODO change yaml-generator to correct ChosenScaMethod equals https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/871#
        assertThatChosenScaMethodsEquals(expectedUpdatePsuAuthenticationResponse.getChosenScaMethod(), actualUpdatePsuAuthenticationResponse.getChosenScaMethod());

        assertEquals(expectedUpdatePsuAuthenticationResponse.getPsuMessage(), actualUpdatePsuAuthenticationResponse.getPsuMessage());
        assertEquals(expectedUpdatePsuAuthenticationResponse.getAuthorisationId(), actualUpdatePsuAuthenticationResponse.getAuthorisationId());
        assertNotNull(actualUpdatePsuAuthenticationResponse.getChallengeData());
        assertEquals(expectedUpdatePsuAuthenticationResponse.getScaStatus(), actualUpdatePsuAuthenticationResponse.getScaStatus());
    }

    @Test
    public void mapToAisCreateOrUpdateAuthorisationResponse_for_CreateConsentAuthorizationResponse() {
        // Given
        when(hrefLinkMapper.mapToLinksMap(null)).thenReturn(null);
        CreateConsentAuthorizationResponse createConsentAuthorizationResponse =
            jsonReader.getObjectFromFile("json/service/mapper/AuthorisationMapper-CreateConsentAuthorisationResponse.json", CreateConsentAuthorizationResponse.class);

        StartScaprocessResponse expected =
            jsonReader.getObjectFromFile("json/service/mapper/AuthorisationMapper-start-scaprocess-response-expected.json", StartScaprocessResponse.class);

        ResponseObject<AuthorisationResponse> responseObject = ResponseObject.<AuthorisationResponse>builder()
                                                                   .body(createConsentAuthorizationResponse)
                                                                   .build();
        // When
        StartScaprocessResponse actualStartScaProcessResponse =
            (StartScaprocessResponse) mapper.mapToAisCreateOrUpdateAuthorisationResponse(responseObject);

        // Then
        assertEquals(expected, actualStartScaProcessResponse);
    }

    private void assertThatChosenScaMethodsEquals(ChosenScaMethod expectedChosenScaMethod, ChosenScaMethod actualChosenScaMethod) {
        assertEquals(expectedChosenScaMethod.getAuthenticationMethodId(), actualChosenScaMethod.getAuthenticationMethodId());
        assertEquals(expectedChosenScaMethod.getAuthenticationType(), actualChosenScaMethod.getAuthenticationType());
        assertEquals(expectedChosenScaMethod.getName(), actualChosenScaMethod.getName());
        assertEquals(expectedChosenScaMethod.getExplanation(), actualChosenScaMethod.getExplanation());
        assertEquals(expectedChosenScaMethod.getAuthenticationVersion(), actualChosenScaMethod.getAuthenticationVersion());
    }

    private Map<String, HrefType> buildLinks() {
        return Collections.singletonMap(SELF_LINK, new HrefType(LOCALHOST_LINK));
    }
}
