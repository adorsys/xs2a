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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.CmsScaMethod;
import de.adorsys.psd2.consent.api.authorisation.AuthorisationParentHolder;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationRequest;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationResponse;
import de.adorsys.psd2.consent.api.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.consent.config.AuthorisationRemoteUrls;
import de.adorsys.psd2.consent.config.CmsRestException;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorisationServiceRemoteTest {
    private static final String URL = "http://some.url";
    private static final AuthorisationType AUTHORISATION_TYPE = AuthorisationType.CONSENT;
    private static final String PARENT_ID = "parent id";
    private static final String AUTHORISATION_ID = "authorisation id";
    private static final String AUTHENTICATION_METHOD_ID = "authentication method id";

    @Mock
    private RestTemplate consentRestTemplate;
    @Mock
    private AuthorisationRemoteUrls authorisationRemoteUrls;

    @InjectMocks
    private AuthorisationServiceRemote authorisationServiceRemote;

    @Test
    void createAuthorisation() {
        // Given
        when(authorisationRemoteUrls.createAuthorisation()).thenReturn(URL);

        AuthorisationParentHolder authorisationParentHolder = new AuthorisationParentHolder(AUTHORISATION_TYPE, PARENT_ID);
        CreateAuthorisationRequest createAuthorisationRequest = new CreateAuthorisationRequest();
        CreateAuthorisationResponse controllerResponse = new CreateAuthorisationResponse(AUTHORISATION_ID, ScaStatus.RECEIVED, "internal request id", new PsuIdData(), null);

        when(consentRestTemplate.postForEntity(URL, createAuthorisationRequest, CreateAuthorisationResponse.class, AUTHORISATION_TYPE, PARENT_ID))
            .thenReturn(new ResponseEntity<>(controllerResponse, HttpStatus.CREATED));

        // When
        CmsResponse<CreateAuthorisationResponse> actualResponse = authorisationServiceRemote.createAuthorisation(authorisationParentHolder, createAuthorisationRequest);

        // Then
        assertTrue(actualResponse.isSuccessful());
        assertEquals(controllerResponse, actualResponse.getPayload());
    }

    @Test
    void createAuthorisation_cmsRestException() {
        // Given
        when(authorisationRemoteUrls.createAuthorisation()).thenReturn(URL);

        AuthorisationParentHolder authorisationParentHolder = new AuthorisationParentHolder(AUTHORISATION_TYPE, PARENT_ID);
        CreateAuthorisationRequest createAuthorisationRequest = new CreateAuthorisationRequest();

        when(consentRestTemplate.postForEntity(URL, createAuthorisationRequest, CreateAuthorisationResponse.class, AUTHORISATION_TYPE, PARENT_ID))
            .thenThrow(CmsRestException.class);

        // When
        CmsResponse<CreateAuthorisationResponse> actualResponse = authorisationServiceRemote.createAuthorisation(authorisationParentHolder, createAuthorisationRequest);

        // Then
        assertTrue(actualResponse.hasError());
        assertEquals(CmsError.TECHNICAL_ERROR, actualResponse.getError());
    }

    @Test
    void getAuthorisationById() {
        // Given
        when(authorisationRemoteUrls.getAuthorisationById()).thenReturn(URL);

        Authorisation controllerResponse = new Authorisation();

        when(consentRestTemplate.getForEntity(URL, Authorisation.class, AUTHORISATION_ID))
            .thenReturn(new ResponseEntity<>(controllerResponse, HttpStatus.OK));

        // When
        CmsResponse<Authorisation> actualResponse = authorisationServiceRemote.getAuthorisationById(AUTHORISATION_ID);

        // Then
        assertTrue(actualResponse.isSuccessful());
        assertEquals(controllerResponse, actualResponse.getPayload());
    }

    @Test
    void getAuthorisationById_cmsRestException() {
        // Given
        when(authorisationRemoteUrls.getAuthorisationById()).thenReturn(URL);

        when(consentRestTemplate.getForEntity(URL, Authorisation.class, AUTHORISATION_ID))
            .thenThrow(CmsRestException.class);

        // When
        CmsResponse<Authorisation> actualResponse = authorisationServiceRemote.getAuthorisationById(AUTHORISATION_ID);

        // Then
        assertTrue(actualResponse.hasError());
        assertEquals(CmsError.TECHNICAL_ERROR, actualResponse.getError());
    }

    @Test
    void updateAuthorisation() {
        // Given
        when(authorisationRemoteUrls.updateAuthorisation()).thenReturn(URL);

        UpdateAuthorisationRequest updateAuthorisationRequest = new UpdateAuthorisationRequest();
        Authorisation controllerResponse = new Authorisation();

        HttpEntity<UpdateAuthorisationRequest> httpEntity = new HttpEntity<>(updateAuthorisationRequest);
        when(consentRestTemplate.exchange(URL, HttpMethod.PUT, httpEntity, Authorisation.class, AUTHORISATION_ID))
            .thenReturn(new ResponseEntity<>(controllerResponse, HttpStatus.OK));

        // When
        CmsResponse<Authorisation> actualResponse = authorisationServiceRemote.updateAuthorisation(AUTHORISATION_ID, updateAuthorisationRequest);

        // Then
        assertTrue(actualResponse.isSuccessful());
        assertEquals(controllerResponse, actualResponse.getPayload());
    }

    @Test
    void updateAuthorisation_cmsRestException() {
        // Given
        when(authorisationRemoteUrls.updateAuthorisation()).thenReturn(URL);

        UpdateAuthorisationRequest updateAuthorisationRequest = new UpdateAuthorisationRequest();

        HttpEntity<UpdateAuthorisationRequest> httpEntity = new HttpEntity<>(updateAuthorisationRequest);
        when(consentRestTemplate.exchange(URL, HttpMethod.PUT, httpEntity, Authorisation.class, AUTHORISATION_ID))
            .thenThrow(CmsRestException.class);

        // When
        CmsResponse<Authorisation> actualResponse = authorisationServiceRemote.updateAuthorisation(AUTHORISATION_ID, updateAuthorisationRequest);

        // Then
        assertTrue(actualResponse.hasError());
        assertEquals(CmsError.TECHNICAL_ERROR, actualResponse.getError());
    }

    @Test
    void updateAuthorisationStatus() {
        // Given
        when(authorisationRemoteUrls.updateAuthorisationStatus()).thenReturn(URL);

        ScaStatus scaStatus = ScaStatus.RECEIVED;

        // When
        CmsResponse<Boolean> actualResponse = authorisationServiceRemote.updateAuthorisationStatus(AUTHORISATION_ID, scaStatus);

        // Then
        assertTrue(actualResponse.isSuccessful());
        assertTrue(actualResponse.getPayload());
        verify(consentRestTemplate).put(URL, null, AUTHORISATION_ID, scaStatus);
    }

    @Test
    void updateAuthorisationStatus_cmsRestException() {
        // Given
        when(authorisationRemoteUrls.updateAuthorisationStatus()).thenReturn(URL);

        ScaStatus scaStatus = ScaStatus.RECEIVED;

        doThrow(CmsRestException.class).when(consentRestTemplate).put(URL, null, AUTHORISATION_ID, scaStatus);

        // When
        CmsResponse<Boolean> actualResponse = authorisationServiceRemote.updateAuthorisationStatus(AUTHORISATION_ID, scaStatus);

        // Then
        assertTrue(actualResponse.isSuccessful());
        assertFalse(actualResponse.getPayload());
        verify(consentRestTemplate).put(URL, null, AUTHORISATION_ID, scaStatus);
    }

    @Test
    void getAuthorisationsByParentId() {
        // Given
        when(authorisationRemoteUrls.getAuthorisationsByParentId()).thenReturn(URL);

        AuthorisationParentHolder authorisationParentHolder = new AuthorisationParentHolder(AUTHORISATION_TYPE, PARENT_ID);
        List<String> controllerResponse = Collections.singletonList(AUTHORISATION_ID);

        // noinspection unchecked
        when(consentRestTemplate.exchange(eq(URL), eq(HttpMethod.GET), eq(null), any(ParameterizedTypeReference.class), eq(AUTHORISATION_TYPE), eq(PARENT_ID)))
            .thenReturn(new ResponseEntity<>(controllerResponse, HttpStatus.OK));

        // When
        CmsResponse<List<String>> actualResponse = authorisationServiceRemote.getAuthorisationsByParentId(authorisationParentHolder);

        // Then
        assertTrue(actualResponse.isSuccessful());
        assertEquals(controllerResponse, actualResponse.getPayload());
    }

    @Test
    void getAuthorisationsByParentId_cmsRestException() {
        // Given
        when(authorisationRemoteUrls.getAuthorisationsByParentId()).thenReturn(URL);

        AuthorisationParentHolder authorisationParentHolder = new AuthorisationParentHolder(AUTHORISATION_TYPE, PARENT_ID);

        // noinspection unchecked
        when(consentRestTemplate.exchange(eq(URL), eq(HttpMethod.GET), eq(null), any(ParameterizedTypeReference.class), eq(AUTHORISATION_TYPE), eq(PARENT_ID)))
            .thenThrow(CmsRestException.class);

        // When
        CmsResponse<List<String>> actualResponse = authorisationServiceRemote.getAuthorisationsByParentId(authorisationParentHolder);

        // Then
        assertTrue(actualResponse.hasError());
        assertEquals(CmsError.TECHNICAL_ERROR, actualResponse.getError());
    }

    @Test
    void getAuthorisationScaStatus() {
        // Given
        when(authorisationRemoteUrls.getAuthorisationScaStatus()).thenReturn(URL);

        AuthorisationParentHolder authorisationParentHolder = new AuthorisationParentHolder(AUTHORISATION_TYPE, PARENT_ID);
        ScaStatus controllerResponse = ScaStatus.RECEIVED;

        when(consentRestTemplate.getForEntity(URL, ScaStatus.class, AUTHORISATION_TYPE, PARENT_ID, AUTHORISATION_ID))
            .thenReturn(new ResponseEntity<>(controllerResponse, HttpStatus.OK));

        // When
        CmsResponse<ScaStatus> actualResponse = authorisationServiceRemote.getAuthorisationScaStatus(AUTHORISATION_ID, authorisationParentHolder);

        // Then
        assertTrue(actualResponse.isSuccessful());
        assertEquals(controllerResponse, actualResponse.getPayload());
    }

    @Test
    void getAuthorisationScaStatus_cmsRestException() {
        // Given
        when(authorisationRemoteUrls.getAuthorisationScaStatus()).thenReturn(URL);

        AuthorisationParentHolder authorisationParentHolder = new AuthorisationParentHolder(AUTHORISATION_TYPE, PARENT_ID);

        when(consentRestTemplate.getForEntity(URL, ScaStatus.class, AUTHORISATION_TYPE, PARENT_ID, AUTHORISATION_ID))
            .thenThrow(CmsRestException.class);

        // When
        CmsResponse<ScaStatus> actualResponse = authorisationServiceRemote.getAuthorisationScaStatus(AUTHORISATION_ID, authorisationParentHolder);

        // Then
        assertTrue(actualResponse.hasError());
        assertEquals(CmsError.TECHNICAL_ERROR, actualResponse.getError());
    }

    @Test
    void isAuthenticationMethodDecoupled() {
        // Given
        when(authorisationRemoteUrls.isAuthenticationMethodDecoupled()).thenReturn(URL);

        when(consentRestTemplate.getForEntity(URL, Boolean.class, AUTHORISATION_ID, AUTHENTICATION_METHOD_ID))
            .thenReturn(new ResponseEntity<>(true, HttpStatus.OK));

        // When
        CmsResponse<Boolean> actualResponse = authorisationServiceRemote.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID);

        // Then
        assertTrue(actualResponse.isSuccessful());
        assertTrue(actualResponse.getPayload());
    }

    @Test
    void saveAuthenticationMethods() {
        // Given
        when(authorisationRemoteUrls.saveAuthenticationMethods()).thenReturn(URL);

        List<CmsScaMethod> cmsScaMethods = Collections.singletonList(new CmsScaMethod(AUTHENTICATION_METHOD_ID, true));

        HttpEntity<List<CmsScaMethod>> httpEntity = new HttpEntity<>(cmsScaMethods);
        when(consentRestTemplate.exchange(URL, HttpMethod.POST, httpEntity, Void.class, AUTHORISATION_ID))
            .thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

        // When
        CmsResponse<Boolean> actualResponse = authorisationServiceRemote.saveAuthenticationMethods(AUTHORISATION_ID, cmsScaMethods);

        // Then
        assertTrue(actualResponse.isSuccessful());
        assertTrue(actualResponse.getPayload());
    }

    @Test
    void saveAuthenticationMethods_wrongStatusCode() {
        // Given
        when(authorisationRemoteUrls.saveAuthenticationMethods()).thenReturn(URL);

        List<CmsScaMethod> cmsScaMethods = Collections.singletonList(new CmsScaMethod(AUTHENTICATION_METHOD_ID, true));

        HttpEntity<List<CmsScaMethod>> httpEntity = new HttpEntity<>(cmsScaMethods);
        when(consentRestTemplate.exchange(URL, HttpMethod.POST, httpEntity, Void.class, AUTHORISATION_ID))
            .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // When
        CmsResponse<Boolean> actualResponse = authorisationServiceRemote.saveAuthenticationMethods(AUTHORISATION_ID, cmsScaMethods);

        // Then
        assertTrue(actualResponse.isSuccessful());
        assertFalse(actualResponse.getPayload());
    }

    @Test
    void saveAuthenticationMethods_cmsRestException() {
        // Given
        when(authorisationRemoteUrls.saveAuthenticationMethods()).thenReturn(URL);

        List<CmsScaMethod> cmsScaMethods = Collections.singletonList(new CmsScaMethod(AUTHENTICATION_METHOD_ID, true));

        HttpEntity<List<CmsScaMethod>> httpEntity = new HttpEntity<>(cmsScaMethods);
        when(consentRestTemplate.exchange(URL, HttpMethod.POST, httpEntity, Void.class, AUTHORISATION_ID))
            .thenThrow(CmsRestException.class);

        // When
        CmsResponse<Boolean> actualResponse = authorisationServiceRemote.saveAuthenticationMethods(AUTHORISATION_ID, cmsScaMethods);

        // Then
        assertTrue(actualResponse.isSuccessful());
        assertFalse(actualResponse.getPayload());
    }

    @Test
    void updateScaApproach() {
        // Given
        when(authorisationRemoteUrls.updateScaApproach()).thenReturn(URL);

        ScaApproach scaApproach = ScaApproach.REDIRECT;

        when(consentRestTemplate.exchange(URL, HttpMethod.PUT, null, Boolean.class, AUTHORISATION_ID, scaApproach))
            .thenReturn(new ResponseEntity<>(true, HttpStatus.NO_CONTENT));

        // When
        CmsResponse<Boolean> actualResponse = authorisationServiceRemote.updateScaApproach(AUTHORISATION_ID, scaApproach);

        // Then
        assertTrue(actualResponse.isSuccessful());
        assertTrue(actualResponse.getPayload());
    }

    @Test
    void updateScaApproach_cmsRestException() {
        // Given
        when(authorisationRemoteUrls.updateScaApproach()).thenReturn(URL);

        ScaApproach scaApproach = ScaApproach.REDIRECT;

        when(consentRestTemplate.exchange(URL, HttpMethod.PUT, null, Boolean.class, AUTHORISATION_ID, scaApproach))
            .thenThrow(CmsRestException.class);

        // When
        CmsResponse<Boolean> actualResponse = authorisationServiceRemote.updateScaApproach(AUTHORISATION_ID, scaApproach);

        // Then
        assertTrue(actualResponse.isSuccessful());
        assertFalse(actualResponse.getPayload());
    }

    @Test
    void getAuthorisationScaApproach() {
        // Given
        when(authorisationRemoteUrls.getAuthorisationScaApproach()).thenReturn(URL);

        AuthorisationScaApproachResponse controllerResponse = new AuthorisationScaApproachResponse(ScaApproach.REDIRECT);

        when(consentRestTemplate.getForEntity(URL, AuthorisationScaApproachResponse.class, AUTHORISATION_ID))
            .thenReturn(new ResponseEntity<>(controllerResponse, HttpStatus.NO_CONTENT));

        // When
        CmsResponse<AuthorisationScaApproachResponse> actualResponse = authorisationServiceRemote.getAuthorisationScaApproach(AUTHORISATION_ID);

        // Then
        assertTrue(actualResponse.isSuccessful());
        assertEquals(controllerResponse, actualResponse.getPayload());
    }

    @Test
    void getAuthorisationScaApproach_cmsRestException() {
        // Given
        when(authorisationRemoteUrls.getAuthorisationScaApproach()).thenReturn(URL);

        AuthorisationScaApproachResponse controllerResponse = new AuthorisationScaApproachResponse(ScaApproach.REDIRECT);

        when(consentRestTemplate.getForEntity(URL, AuthorisationScaApproachResponse.class, AUTHORISATION_ID))
            .thenThrow(CmsRestException.class);

        // When
        CmsResponse<AuthorisationScaApproachResponse> actualResponse = authorisationServiceRemote.getAuthorisationScaApproach(AUTHORISATION_ID);

        // Then
        assertTrue(actualResponse.hasError());
        assertEquals(CmsError.TECHNICAL_ERROR, actualResponse.getError());
    }
}
