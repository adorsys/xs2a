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
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.ScaMethod;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.consent.service.authorisation.AuthService;
import de.adorsys.psd2.consent.service.authorisation.AuthServiceResolver;
import de.adorsys.psd2.consent.service.authorisation.AuthorisationClosingService;
import de.adorsys.psd2.consent.service.mapper.AuthorisationMapper;
import de.adorsys.psd2.consent.service.mapper.ScaMethodMapper;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorisationServiceInternalTest {
    private static final String PSU_ID = "anton.brueckner";
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(PSU_ID, null, null, null, null);
    private static final String AUTHORISATION_ID = "9304a6a0-8f02-4b79-aeab-00aa7e03a06d";
    private static final String WRONG_AUTHORISATION_ID = "00000000-0000-4b79-aeab-00aa7e03a06d";
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    private static final ScaApproach SCA_APPROACH = ScaApproach.EMBEDDED;
    private static final String AUTHENTICATION_METHOD_ID = "SMS";
    private static final String WRONG_AUTHENTICATION_METHOD_ID = "EMAIL";
    private static final String TPP_REDIRECT_URI = "request/redirect_uri";
    private static final String TPP_NOK_REDIRECT_URI = "request/nok_redirect_uri";
    private static final TppRedirectUri TPP_REDIRECT_URIs = new TppRedirectUri(TPP_REDIRECT_URI, TPP_NOK_REDIRECT_URI);
    private static final String INTERNAL_REQUEST_ID = "5c2d5564-367f-4e03-a621-6bef76fa4208";

    private static final JsonReader jsonReader = new JsonReader();

    @Mock
    private AuthorisationRepository authorisationRepository;
    @Mock
    private ScaMethodMapper scaMethodMapper;
    @Mock
    private AuthorisationMapper authorisationMapper;
    @Mock
    private AuthServiceResolver authServiceResolver;
    @Mock
    private AuthService authService;
    @Mock
    private AuthorisationClosingService authorisationClosingService;

    @InjectMocks
    private AuthorisationServiceInternal authorisationServiceInternal;

    @Test
    void createAuthorisation() {
        // Given
        String parentId = "parent id";
        AuthorisationType authorisationType = AuthorisationType.CONSENT;
        AuthorisationParentHolder authorisationParentHolder = new AuthorisationParentHolder(authorisationType, parentId);
        CreateAuthorisationRequest createAuthorisationRequest = new CreateAuthorisationRequest(AUTHORISATION_ID, PSU_ID_DATA, ScaApproach.REDIRECT, ScaStatus.STARTED, TPP_REDIRECT_URIs);
        CreateAuthorisationResponse expectedResponse = new CreateAuthorisationResponse(AUTHORISATION_ID, ScaStatus.RECEIVED, INTERNAL_REQUEST_ID, PSU_ID_DATA, null);

        ConsentEntity consentEntity = new ConsentEntity();
        consentEntity.setInternalRequestId(INTERNAL_REQUEST_ID);

        when(authServiceResolver.getAuthService(authorisationType)).thenReturn(authService);
        when(authService.getNotFinalisedAuthorisationParent(parentId)).thenReturn(Optional.of(consentEntity));

        AuthorisationEntity newAuthorisation = new AuthorisationEntity();
        newAuthorisation.setExternalId(AUTHORISATION_ID);
        newAuthorisation.setScaStatus(ScaStatus.RECEIVED);
        when(authService.saveAuthorisation(createAuthorisationRequest, consentEntity)).thenReturn(newAuthorisation);

        // When
        CmsResponse<CreateAuthorisationResponse> actualResponse = authorisationServiceInternal.createAuthorisation(authorisationParentHolder, createAuthorisationRequest);

        // Then
        assertTrue(actualResponse.isSuccessful());
        assertEquals(expectedResponse, actualResponse.getPayload());
        verify(authorisationClosingService).closePreviousAuthorisationsByParent(parentId, authorisationType, PSU_ID_DATA);
        verify(authService).saveAuthorisation(createAuthorisationRequest, consentEntity);
    }

    @Test
    void createAuthorisation_wrongParentId() {
        // Given
        String parentId = "parent id";
        AuthorisationParentHolder authorisationParentHolder = new AuthorisationParentHolder(AuthorisationType.CONSENT, parentId);
        CreateAuthorisationRequest createAuthorisationRequest = new CreateAuthorisationRequest(AUTHORISATION_ID, PSU_ID_DATA, ScaApproach.REDIRECT, ScaStatus.STARTED, TPP_REDIRECT_URIs);

        when(authServiceResolver.getAuthService(AuthorisationType.CONSENT)).thenReturn(authService);
        when(authService.getNotFinalisedAuthorisationParent(parentId)).thenReturn(Optional.empty());

        // When
        CmsResponse<CreateAuthorisationResponse> actualResponse = authorisationServiceInternal.createAuthorisation(authorisationParentHolder, createAuthorisationRequest);

        // Then
        assertTrue(actualResponse.hasError());
        assertEquals(CmsError.LOGICAL_ERROR, actualResponse.getError());
    }

    @Test
    void getAuthorisationById() {
        // Given
        AuthorisationEntity authorisationEntity = jsonReader.getObjectFromFile("json/service/authorisation-entity.json", AuthorisationEntity.class);
        when(authorisationRepository.findByExternalId(AUTHORISATION_ID)).thenReturn(Optional.of(authorisationEntity));

        Authorisation mappedAuthorisation = jsonReader.getObjectFromFile("json/service/authorisation.json", Authorisation.class);
        when(authorisationMapper.mapToAuthorisation(authorisationEntity)).thenReturn(mappedAuthorisation);

        // When
        CmsResponse<Authorisation> actualResponse = authorisationServiceInternal.getAuthorisationById(AUTHORISATION_ID);

        // Then
        assertTrue(actualResponse.isSuccessful());
        assertEquals(mappedAuthorisation, actualResponse.getPayload());
    }

    @Test
    void getAuthorisationById_wrongId() {
        // Given
        when(authorisationRepository.findByExternalId(WRONG_AUTHORISATION_ID)).thenReturn(Optional.empty());

        // When
        CmsResponse<Authorisation> actualResponse = authorisationServiceInternal.getAuthorisationById(WRONG_AUTHORISATION_ID);

        // Then
        assertTrue(actualResponse.hasError());
        assertEquals(CmsError.LOGICAL_ERROR, actualResponse.getError());
    }

    @Test
    void updateAuthorisation() {
        // Given
        AuthorisationEntity authorisationEntity = new AuthorisationEntity();
        authorisationEntity.setType(AuthorisationType.CONSENT);
        authorisationEntity.setScaStatus(ScaStatus.RECEIVED);
        when(authorisationRepository.findByExternalId(AUTHORISATION_ID)).thenReturn(Optional.of(authorisationEntity));

        UpdateAuthorisationRequest updateAuthorisationRequest = new UpdateAuthorisationRequest();
        updateAuthorisationRequest.setAuthorisationType(AuthorisationType.CONSENT);
        updateAuthorisationRequest.setPsuData(PSU_ID_DATA);

        when(authServiceResolver.getAuthService(AuthorisationType.CONSENT)).thenReturn(authService);

        AuthorisationEntity updatedAuthorisationEntity = jsonReader.getObjectFromFile("json/service/authorisation-entity.json", AuthorisationEntity.class);
        when(authService.doUpdateAuthorisation(authorisationEntity, updateAuthorisationRequest)).thenReturn(updatedAuthorisationEntity);

        Authorisation mappedAuthorisation = jsonReader.getObjectFromFile("json/service/authorisation.json", Authorisation.class);
        when(authorisationMapper.mapToAuthorisation(updatedAuthorisationEntity)).thenReturn(mappedAuthorisation);

        // When
        CmsResponse<Authorisation> actualResponse = authorisationServiceInternal.updateAuthorisation(AUTHORISATION_ID, updateAuthorisationRequest);

        // Then
        assertTrue(actualResponse.isSuccessful());
        assertEquals(mappedAuthorisation, actualResponse.getPayload());

        InOrder inOrder = inOrder(authorisationClosingService, authService);
        inOrder.verify(authorisationClosingService).closePreviousAuthorisationsByAuthorisation(authorisationEntity, PSU_ID_DATA);
        inOrder.verify(authService).doUpdateAuthorisation(authorisationEntity, updateAuthorisationRequest);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void updateAuthorisation_wrongId() {
        // Given
        when(authorisationRepository.findByExternalId(AUTHORISATION_ID)).thenReturn(Optional.empty());

        UpdateAuthorisationRequest updateAuthorisationRequest = new UpdateAuthorisationRequest();
        updateAuthorisationRequest.setAuthorisationType(AuthorisationType.CONSENT);

        // When
        CmsResponse<Authorisation> actualResponse = authorisationServiceInternal.updateAuthorisation(AUTHORISATION_ID, updateAuthorisationRequest);

        // Then
        assertTrue(actualResponse.hasError());
        assertEquals(CmsError.LOGICAL_ERROR, actualResponse.getError());
    }

    @Test
    void updateAuthorisation_finalisedStatus() {
        // Given
        AuthorisationEntity authorisationEntity = new AuthorisationEntity();
        authorisationEntity.setType(AuthorisationType.CONSENT);
        authorisationEntity.setScaStatus(ScaStatus.FINALISED);
        when(authorisationRepository.findByExternalId(AUTHORISATION_ID)).thenReturn(Optional.of(authorisationEntity));

        UpdateAuthorisationRequest updateAuthorisationRequest = new UpdateAuthorisationRequest();
        updateAuthorisationRequest.setAuthorisationType(AuthorisationType.CONSENT);

        // When
        CmsResponse<Authorisation> actualResponse = authorisationServiceInternal.updateAuthorisation(AUTHORISATION_ID, updateAuthorisationRequest);

        // Then
        assertTrue(actualResponse.hasError());
        assertEquals(CmsError.LOGICAL_ERROR, actualResponse.getError());
    }

    @Test
    void updateAuthorisationStatus() {
        // Given
        AuthorisationEntity authorisationEntity = new AuthorisationEntity();
        authorisationEntity.setScaStatus(ScaStatus.RECEIVED);
        when(authorisationRepository.findByExternalId(AUTHORISATION_ID)).thenReturn(Optional.of(authorisationEntity));

        ScaStatus newScaStatus = ScaStatus.PSUIDENTIFIED;

        // When
        CmsResponse<Boolean> actualResponse = authorisationServiceInternal.updateAuthorisationStatus(AUTHORISATION_ID, newScaStatus);

        // Then
        assertTrue(actualResponse.isSuccessful());
        assertTrue(actualResponse.getPayload());
    }

    @Test
    void updateAuthorisationStatus_wrongId() {
        // Given
        when(authorisationRepository.findByExternalId(WRONG_AUTHORISATION_ID)).thenReturn(Optional.empty());

        // When
        CmsResponse<Boolean> actualResponse = authorisationServiceInternal.updateAuthorisationStatus(WRONG_AUTHORISATION_ID, ScaStatus.PSUIDENTIFIED);

        // Then
        assertTrue(actualResponse.isSuccessful());
        assertFalse(actualResponse.getPayload());
        verify(authorisationRepository, never()).save(any());
    }

    @Test
    void getAuthorisationsByParentId() {
        // Given
        String parentId = "parent id";
        AuthorisationParentHolder authorisationParentHolder = new AuthorisationParentHolder(AuthorisationType.CONSENT, parentId);
        List<String> expectedAuthorisations = Collections.singletonList(AUTHORISATION_ID);

        when(authServiceResolver.getAuthService(AuthorisationType.CONSENT)).thenReturn(authService);

        ConsentEntity consentEntity = new ConsentEntity();
        consentEntity.setExternalId(parentId);
        when(authService.getAuthorisationParent(parentId)).thenReturn(Optional.of(consentEntity));

        when(authService.checkAndUpdateOnConfirmationExpiration(consentEntity)).thenReturn(consentEntity);

        AuthorisationEntity authorisationEntity = new AuthorisationEntity();
        authorisationEntity.setExternalId(AUTHORISATION_ID);
        when(authService.getAuthorisationsByParentId(parentId)).thenReturn(Collections.singletonList(authorisationEntity));

        // When
        CmsResponse<List<String>> actualResponse = authorisationServiceInternal.getAuthorisationsByParentId(authorisationParentHolder);

        // Then
        assertTrue(actualResponse.isSuccessful());
        assertEquals(expectedAuthorisations, actualResponse.getPayload());
    }

    @Test
    void getAuthorisationsByParentId_wrongId() {
        // Given
        String parentId = "wrong id";
        AuthorisationParentHolder authorisationParentHolder = new AuthorisationParentHolder(AuthorisationType.CONSENT, parentId);

        when(authServiceResolver.getAuthService(AuthorisationType.CONSENT)).thenReturn(authService);

        when(authService.getAuthorisationParent(parentId)).thenReturn(Optional.empty());

        // When
        CmsResponse<List<String>> actualResponse = authorisationServiceInternal.getAuthorisationsByParentId(authorisationParentHolder);

        // Then
        assertTrue(actualResponse.hasError());
        assertEquals(CmsError.LOGICAL_ERROR, actualResponse.getError());
    }

    @Test
    void getAuthorisationScaStatus() {
        // Given
        String parentId = "parent id";
        AuthorisationParentHolder authorisationParentHolder = new AuthorisationParentHolder(AuthorisationType.CONSENT, parentId);

        when(authServiceResolver.getAuthService(AuthorisationType.CONSENT)).thenReturn(authService);

        ConsentEntity consentEntity = new ConsentEntity();
        consentEntity.setExternalId(parentId);
        when(authService.getAuthorisationParent(parentId)).thenReturn(Optional.of(consentEntity));

        AuthorisationEntity authorisationEntity = new AuthorisationEntity();
        authorisationEntity.setParentExternalId(parentId);
        authorisationEntity.setScaStatus(SCA_STATUS);
        when(authService.getAuthorisationById(AUTHORISATION_ID)).thenReturn(Optional.of(authorisationEntity));

        // When
        CmsResponse<ScaStatus> actualResult = authorisationServiceInternal.getAuthorisationScaStatus(AUTHORISATION_ID, authorisationParentHolder);

        // Then
        assertTrue(actualResult.isSuccessful());
        assertEquals(SCA_STATUS, actualResult.getPayload());
    }

    @Test
    void getAuthorisationScaStatus_wrongParentId() {
        // Given
        String parentId = "wrong parent id";
        AuthorisationParentHolder authorisationParentHolder = new AuthorisationParentHolder(AuthorisationType.CONSENT, parentId);

        when(authServiceResolver.getAuthService(AuthorisationType.CONSENT)).thenReturn(authService);

        when(authService.getAuthorisationParent(parentId)).thenReturn(Optional.empty());

        // When
        CmsResponse<ScaStatus> actualResult = authorisationServiceInternal.getAuthorisationScaStatus(AUTHORISATION_ID, authorisationParentHolder);

        // Then
        assertTrue(actualResult.hasError());
        assertEquals(CmsError.LOGICAL_ERROR, actualResult.getError());
    }

    @Test
    void getAuthorisationScaStatus_expiredParent() {
        // Given
        String parentId = "parent id";
        AuthorisationParentHolder authorisationParentHolder = new AuthorisationParentHolder(AuthorisationType.CONSENT, parentId);

        when(authServiceResolver.getAuthService(AuthorisationType.CONSENT)).thenReturn(authService);

        ConsentEntity consentEntity = new ConsentEntity();
        consentEntity.setExternalId(parentId);
        when(authService.getAuthorisationParent(parentId)).thenReturn(Optional.of(consentEntity));
        when(authService.isConfirmationExpired(consentEntity)).thenReturn(true);

        // When
        CmsResponse<ScaStatus> actualResult = authorisationServiceInternal.getAuthorisationScaStatus(AUTHORISATION_ID, authorisationParentHolder);

        // Then
        assertTrue(actualResult.isSuccessful());
        assertEquals(ScaStatus.FAILED, actualResult.getPayload());
        verify(authService).updateOnConfirmationExpiration(consentEntity);
    }

    @Test
    void getAuthorisationScaStatus_wrongAuthorisationId() {
        // Given
        String parentId = "parent id";
        AuthorisationParentHolder authorisationParentHolder = new AuthorisationParentHolder(AuthorisationType.CONSENT, parentId);

        when(authServiceResolver.getAuthService(AuthorisationType.CONSENT)).thenReturn(authService);

        ConsentEntity consentEntity = new ConsentEntity();
        consentEntity.setExternalId(parentId);
        when(authService.getAuthorisationParent(parentId)).thenReturn(Optional.of(consentEntity));

        when(authService.getAuthorisationById(WRONG_AUTHORISATION_ID)).thenReturn(Optional.empty());

        // When
        CmsResponse<ScaStatus> actualResult = authorisationServiceInternal.getAuthorisationScaStatus(WRONG_AUTHORISATION_ID, authorisationParentHolder);

        // Then
        assertTrue(actualResult.hasError());
        assertEquals(CmsError.LOGICAL_ERROR, actualResult.getError());
    }

    @Test
    void getAuthorisationScaStatus_authorisationWithWrongParentId() {
        // Given
        String parentId = "parent id";
        AuthorisationParentHolder authorisationParentHolder = new AuthorisationParentHolder(AuthorisationType.CONSENT, parentId);

        when(authServiceResolver.getAuthService(AuthorisationType.CONSENT)).thenReturn(authService);

        ConsentEntity consentEntity = new ConsentEntity();
        consentEntity.setExternalId(parentId);
        when(authService.getAuthorisationParent(parentId)).thenReturn(Optional.of(consentEntity));

        AuthorisationEntity authorisationEntity = new AuthorisationEntity();
        authorisationEntity.setParentExternalId("wrong parent id");
        authorisationEntity.setScaStatus(SCA_STATUS);
        when(authService.getAuthorisationById(AUTHORISATION_ID)).thenReturn(Optional.of(authorisationEntity));

        // When
        CmsResponse<ScaStatus> actualResult = authorisationServiceInternal.getAuthorisationScaStatus(AUTHORISATION_ID, authorisationParentHolder);

        // Then
        assertTrue(actualResult.hasError());
        assertEquals(CmsError.LOGICAL_ERROR, actualResult.getError());
    }

    @Test
    void isAuthenticationMethodDecoupled() {
        // Given
        AuthorisationEntity authorisationEntity = new AuthorisationEntity();
        ScaMethod scaMethod = new ScaMethod();
        scaMethod.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);
        scaMethod.setDecoupled(true);
        authorisationEntity.setAvailableScaMethods(Collections.singletonList(scaMethod));
        when(authorisationRepository.findByExternalId(AUTHORISATION_ID)).thenReturn(Optional.of(authorisationEntity));

        // When
        CmsResponse<Boolean> actualResponse = authorisationServiceInternal.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID);

        // Then
        assertTrue(actualResponse.isSuccessful());
        assertTrue(actualResponse.getPayload());
    }

    @Test
    void isAuthenticationMethodDecoupled_wrongAuthorisationId() {
        // Given
        when(authorisationRepository.findByExternalId(WRONG_AUTHORISATION_ID)).thenReturn(Optional.empty());

        // When
        CmsResponse<Boolean> actualResponse = authorisationServiceInternal.isAuthenticationMethodDecoupled(WRONG_AUTHORISATION_ID, AUTHENTICATION_METHOD_ID);

        // Then
        assertTrue(actualResponse.isSuccessful());
        assertFalse(actualResponse.getPayload());
    }

    @Test
    void isAuthenticationMethodDecoupled_wrongMethodId() {
        // Given
        AuthorisationEntity authorisationEntity = new AuthorisationEntity();
        ScaMethod scaMethod = new ScaMethod();
        scaMethod.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);
        scaMethod.setDecoupled(true);
        authorisationEntity.setAvailableScaMethods(Collections.singletonList(scaMethod));
        when(authorisationRepository.findByExternalId(AUTHORISATION_ID)).thenReturn(Optional.of(authorisationEntity));

        // When
        CmsResponse<Boolean> actualResponse = authorisationServiceInternal.isAuthenticationMethodDecoupled(AUTHORISATION_ID, WRONG_AUTHENTICATION_METHOD_ID);

        // Then
        assertTrue(actualResponse.isSuccessful());
        assertFalse(actualResponse.getPayload());
    }

    @Test
    void isAuthenticationMethodDecoupled_noMethods() {
        // Given
        AuthorisationEntity authorisationEntity = new AuthorisationEntity();
        authorisationEntity.setAvailableScaMethods(Collections.emptyList());
        when(authorisationRepository.findByExternalId(AUTHORISATION_ID)).thenReturn(Optional.of(authorisationEntity));

        // When
        CmsResponse<Boolean> actualResponse = authorisationServiceInternal.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID);

        // Then
        assertTrue(actualResponse.isSuccessful());
        assertFalse(actualResponse.getPayload());
    }

    @Test
    void saveAuthenticationMethods() {
        // Given
        CmsScaMethod cmsScaMethod = new CmsScaMethod(AUTHENTICATION_METHOD_ID, true);
        ScaMethod scaMethod = new ScaMethod();
        scaMethod.setAuthenticationMethodId(AUTHENTICATION_METHOD_ID);
        scaMethod.setDecoupled(true);

        AuthorisationEntity authorisationEntity = new AuthorisationEntity();

        when(authorisationRepository.findByExternalId(AUTHORISATION_ID))
            .thenReturn(Optional.of(authorisationEntity));
        when(scaMethodMapper.mapToScaMethods(Collections.singletonList(cmsScaMethod)))
            .thenReturn(Collections.singletonList(scaMethod));

        // When
        CmsResponse<Boolean> actualResponse = authorisationServiceInternal.saveAuthenticationMethods(AUTHORISATION_ID, Collections.singletonList(cmsScaMethod));

        // Then
        assertTrue(actualResponse.isSuccessful());
        assertTrue(actualResponse.getPayload());
    }

    @Test
    void saveAuthenticationMethods_wrongId() {
        // Given
        CmsScaMethod cmsScaMethod = new CmsScaMethod(AUTHENTICATION_METHOD_ID, true);

        when(authorisationRepository.findByExternalId(WRONG_AUTHORISATION_ID))
            .thenReturn(Optional.empty());

        // When
        CmsResponse<Boolean> actualResponse = authorisationServiceInternal.saveAuthenticationMethods(WRONG_AUTHORISATION_ID, Collections.singletonList(cmsScaMethod));

        // Then
        assertTrue(actualResponse.isSuccessful());
        assertFalse(actualResponse.getPayload());
    }

    @Test
    void updateScaApproach() {
        // Given
        AuthorisationEntity authorisationEntity = new AuthorisationEntity();

        when(authorisationRepository.findByExternalId(AUTHORISATION_ID))
            .thenReturn(Optional.of(authorisationEntity));

        // When
        CmsResponse<Boolean> actualResponse = authorisationServiceInternal.updateScaApproach(AUTHORISATION_ID, SCA_APPROACH);

        // Then
        assertTrue(actualResponse.isSuccessful());
        assertTrue(actualResponse.getPayload());
    }

    @Test
    void updateScaApproach_wrongId() {
        // Given
        when(authorisationRepository.findByExternalId(WRONG_AUTHORISATION_ID))
            .thenReturn(Optional.empty());

        // When
        CmsResponse<Boolean> actualResponse = authorisationServiceInternal.updateScaApproach(WRONG_AUTHORISATION_ID, SCA_APPROACH);

        // Then
        assertTrue(actualResponse.isSuccessful());
        assertFalse(actualResponse.getPayload());
    }

    @Test
    void getAuthorisationScaApproach() {
        // Given
        AuthorisationEntity authorisationEntity = new AuthorisationEntity();
        authorisationEntity.setScaApproach(SCA_APPROACH);

        when(authorisationRepository.findByExternalId(AUTHORISATION_ID))
            .thenReturn(Optional.of(authorisationEntity));

        // When
        CmsResponse<AuthorisationScaApproachResponse> actualResponse = authorisationServiceInternal.getAuthorisationScaApproach(AUTHORISATION_ID);

        // Then
        assertTrue(actualResponse.isSuccessful());

        AuthorisationScaApproachResponse expectedResponse = new AuthorisationScaApproachResponse(SCA_APPROACH);
        assertEquals(expectedResponse, actualResponse.getPayload());
    }

    @Test
    void getAuthorisationScaApproach_wrongId() {
        // Given
        when(authorisationRepository.findByExternalId(AUTHORISATION_ID))
            .thenReturn(Optional.empty());

        // When
        CmsResponse<AuthorisationScaApproachResponse> actualResponse = authorisationServiceInternal.getAuthorisationScaApproach(AUTHORISATION_ID);

        // Then
        assertFalse(actualResponse.isSuccessful());
        assertEquals(CmsError.LOGICAL_ERROR, actualResponse.getError());
    }
}
