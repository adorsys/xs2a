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
import de.adorsys.psd2.consent.api.service.AuthorisationService;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorisationServiceInternalEncryptedTest {
    private static final String ENCRYPTED_PARENT_ID = "Uv0juhXijIas3D1bKucTox9U0XMzd5S4V4YoVF0JQTr8YXQK5v1TT2IFXDJ8ScfQZmgpuZAiXHmZbsJp852YydWFnjze07vwpAgFM45MlQk=_=_psGLvQpt9Q";
    private static final String MALFORMED_ENCRYPTED_PARENT_ID = "wrong_id";
    private static final String DECRYPTED_PARENT_ID = "d7cb0df1-72d8-43b3-8dc4-569598802d07";
    private static final AuthorisationType AUTHORISATION_TYPE = AuthorisationType.CONSENT;
    private static final String AUTHORISATION_ID = "6963bceb-01d8-4961-977e-2424ce60fc7b";
    public static final String AUTHENTICATION_METHOD_ID = "SMS";

    @Mock
    private SecurityDataService securityDataService;
    @Mock
    private AuthorisationService authorisationService;

    @InjectMocks
    private AuthorisationServiceInternalEncrypted authorisationServiceInternalEncrypted;

    @Test
    void createAuthorisation() {
        // Given
        AuthorisationParentHolder encryptedParentHolder = new AuthorisationParentHolder(AUTHORISATION_TYPE, ENCRYPTED_PARENT_ID);
        CreateAuthorisationRequest createAuthorisationRequest = new CreateAuthorisationRequest();

        when(securityDataService.decryptId(ENCRYPTED_PARENT_ID)).thenReturn(Optional.of(DECRYPTED_PARENT_ID));

        AuthorisationParentHolder decryptedParentHolder = new AuthorisationParentHolder(AUTHORISATION_TYPE, DECRYPTED_PARENT_ID);
        CreateAuthorisationResponse createAuthorisationResponse = new CreateAuthorisationResponse(AUTHORISATION_ID, ScaStatus.RECEIVED, "internal request id", new PsuIdData(), ScaApproach.EMBEDDED);
        CmsResponse<CreateAuthorisationResponse> innerServiceResponse = CmsResponse.<CreateAuthorisationResponse>builder().payload(createAuthorisationResponse).build();
        when(authorisationService.createAuthorisation(decryptedParentHolder, createAuthorisationRequest)).thenReturn(innerServiceResponse);

        // When
        CmsResponse<CreateAuthorisationResponse> actualResponse =
            authorisationServiceInternalEncrypted.createAuthorisation(encryptedParentHolder, createAuthorisationRequest);

        // Then
        assertEquals(innerServiceResponse, actualResponse);
        verify(authorisationService).createAuthorisation(decryptedParentHolder, createAuthorisationRequest);
    }

    @Test
    void createAuthorisation_malformedEncryptedId() {
        // Given
        AuthorisationParentHolder encryptedParentHolder = new AuthorisationParentHolder(AUTHORISATION_TYPE, MALFORMED_ENCRYPTED_PARENT_ID);
        CreateAuthorisationRequest createAuthorisationRequest = new CreateAuthorisationRequest();

        when(securityDataService.decryptId(MALFORMED_ENCRYPTED_PARENT_ID)).thenReturn(Optional.empty());

        // When
        CmsResponse<CreateAuthorisationResponse> actualResponse =
            authorisationServiceInternalEncrypted.createAuthorisation(encryptedParentHolder, createAuthorisationRequest);

        // Then
        assertTrue(actualResponse.hasError());
        assertEquals(CmsError.TECHNICAL_ERROR, actualResponse.getError());
        verify(authorisationService, never()).createAuthorisation(any(), any());
    }

    @Test
    void getAuthorisationById() {
        // Given
        Authorisation authorisation = new Authorisation();
        CmsResponse<Authorisation> innerServiceResponse = CmsResponse.<Authorisation>builder().payload(authorisation).build();
        when(authorisationService.getAuthorisationById(AUTHORISATION_ID))
            .thenReturn(innerServiceResponse);

        // When
        CmsResponse<Authorisation> actualResponse = authorisationServiceInternalEncrypted.getAuthorisationById(AUTHORISATION_ID);

        // Then
        assertEquals(innerServiceResponse, actualResponse);
        verify(authorisationService).getAuthorisationById(AUTHORISATION_ID);
    }

    @Test
    void updateAuthorisation() {
        // Given
        UpdateAuthorisationRequest updateAuthorisationRequest = new UpdateAuthorisationRequest();
        Authorisation authorisation = new Authorisation();
        CmsResponse<Authorisation> innerServiceResponse = CmsResponse.<Authorisation>builder().payload(authorisation).build();
        when(authorisationService.updateAuthorisation(AUTHORISATION_ID, updateAuthorisationRequest))
            .thenReturn(innerServiceResponse);

        // When
        CmsResponse<Authorisation> actualResponse = authorisationServiceInternalEncrypted.updateAuthorisation(AUTHORISATION_ID, updateAuthorisationRequest);

        // Then
        assertEquals(innerServiceResponse, actualResponse);
        verify(authorisationService).updateAuthorisation(AUTHORISATION_ID, updateAuthorisationRequest);
    }

    @Test
    void updateAuthorisationStatus() {
        // Given
        ScaStatus scaStatus = ScaStatus.RECEIVED;
        CmsResponse<Boolean> innerServiceResponse = CmsResponse.<Boolean>builder().payload(true).build();
        when(authorisationService.updateAuthorisationStatus(AUTHORISATION_ID, scaStatus))
            .thenReturn(innerServiceResponse);

        // When
        CmsResponse<Boolean> actualResponse = authorisationServiceInternalEncrypted.updateAuthorisationStatus(AUTHORISATION_ID, scaStatus);

        // Then
        assertEquals(innerServiceResponse, actualResponse);
        verify(authorisationService).updateAuthorisationStatus(AUTHORISATION_ID, scaStatus);
    }

    @Test
    void getAuthorisationsByParentId() {
        // Given
        AuthorisationParentHolder encryptedParentHolder = new AuthorisationParentHolder(AUTHORISATION_TYPE, ENCRYPTED_PARENT_ID);

        when(securityDataService.decryptId(ENCRYPTED_PARENT_ID)).thenReturn(Optional.of(DECRYPTED_PARENT_ID));

        AuthorisationParentHolder decryptedParentHolder = new AuthorisationParentHolder(AUTHORISATION_TYPE, DECRYPTED_PARENT_ID);
        List<String> authorisationIds = Collections.singletonList(AUTHORISATION_ID);
        CmsResponse<List<String>> innerServiceResponse = CmsResponse.<List<String>>builder().payload(authorisationIds).build();
        when(authorisationService.getAuthorisationsByParentId(decryptedParentHolder)).thenReturn(innerServiceResponse);

        // When
        CmsResponse<List<String>> actualResponse =
            authorisationServiceInternalEncrypted.getAuthorisationsByParentId(encryptedParentHolder);

        // Then
        assertEquals(innerServiceResponse, actualResponse);
        verify(authorisationService).getAuthorisationsByParentId(decryptedParentHolder);
    }

    @Test
    void getAuthorisationsByParentId_malformedEncryptedId() {
        // Given
        AuthorisationParentHolder encryptedParentHolder = new AuthorisationParentHolder(AUTHORISATION_TYPE, MALFORMED_ENCRYPTED_PARENT_ID);

        when(securityDataService.decryptId(MALFORMED_ENCRYPTED_PARENT_ID)).thenReturn(Optional.empty());

        // When
        CmsResponse<List<String>> actualResponse =
            authorisationServiceInternalEncrypted.getAuthorisationsByParentId(encryptedParentHolder);

        // Then
        assertTrue(actualResponse.hasError());
        assertEquals(CmsError.TECHNICAL_ERROR, actualResponse.getError());
        verify(authorisationService, never()).getAuthorisationsByParentId(any());
    }

    @Test
    void getAuthorisationScaStatus() {
        // Given
        AuthorisationParentHolder encryptedParentHolder = new AuthorisationParentHolder(AUTHORISATION_TYPE, ENCRYPTED_PARENT_ID);

        when(securityDataService.decryptId(ENCRYPTED_PARENT_ID)).thenReturn(Optional.of(DECRYPTED_PARENT_ID));

        AuthorisationParentHolder decryptedParentHolder = new AuthorisationParentHolder(AUTHORISATION_TYPE, DECRYPTED_PARENT_ID);
        ScaStatus scaStatus = ScaStatus.RECEIVED;
        CmsResponse<ScaStatus> innerServiceResponse = CmsResponse.<ScaStatus>builder().payload(scaStatus).build();
        when(authorisationService.getAuthorisationScaStatus(AUTHORISATION_ID, decryptedParentHolder)).thenReturn(innerServiceResponse);

        // When
        CmsResponse<ScaStatus> actualResponse =
            authorisationServiceInternalEncrypted.getAuthorisationScaStatus(AUTHORISATION_ID, encryptedParentHolder);

        // Then
        assertEquals(innerServiceResponse, actualResponse);
        verify(authorisationService).getAuthorisationScaStatus(AUTHORISATION_ID, decryptedParentHolder);
    }

    @Test
    void getAuthorisationScaStatus_malformedEncryptedId() {
        // Given
        AuthorisationParentHolder encryptedParentHolder = new AuthorisationParentHolder(AUTHORISATION_TYPE, MALFORMED_ENCRYPTED_PARENT_ID);

        when(securityDataService.decryptId(MALFORMED_ENCRYPTED_PARENT_ID)).thenReturn(Optional.empty());

        // When
        CmsResponse<ScaStatus> actualResponse =
            authorisationServiceInternalEncrypted.getAuthorisationScaStatus(AUTHORISATION_ID, encryptedParentHolder);

        // Then
        assertTrue(actualResponse.hasError());
        assertEquals(CmsError.TECHNICAL_ERROR, actualResponse.getError());
        verify(authorisationService, never()).getAuthorisationScaStatus(any(), any());
    }

    @Test
    void isAuthenticationMethodDecoupled() {
        // Given
        CmsResponse<Boolean> innerServiceResponse = CmsResponse.<Boolean>builder().payload(true).build();
        when(authorisationService.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID))
            .thenReturn(innerServiceResponse);

        // When
        CmsResponse<Boolean> actualResponse = authorisationServiceInternalEncrypted.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID);

        // Then
        assertEquals(innerServiceResponse, actualResponse);
        verify(authorisationService).isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID);
    }

    @Test
    void saveAuthenticationMethods() {
        // Given
        List<CmsScaMethod> scaMethods = Collections.singletonList(new CmsScaMethod(AUTHENTICATION_METHOD_ID, true));
        CmsResponse<Boolean> innerServiceResponse = CmsResponse.<Boolean>builder().payload(true).build();
        when(authorisationService.saveAuthenticationMethods(AUTHORISATION_ID, scaMethods))
            .thenReturn(innerServiceResponse);

        // When
        CmsResponse<Boolean> actualResponse = authorisationServiceInternalEncrypted.saveAuthenticationMethods(AUTHORISATION_ID, scaMethods);

        // Then
        assertEquals(innerServiceResponse, actualResponse);
        verify(authorisationService).saveAuthenticationMethods(AUTHORISATION_ID, scaMethods);
    }

    @Test
    void updateScaApproach() {
        // Given
        ScaApproach scaApproach = ScaApproach.REDIRECT;
        CmsResponse<Boolean> innerServiceResponse = CmsResponse.<Boolean>builder().payload(true).build();
        when(authorisationService.updateScaApproach(AUTHORISATION_ID, scaApproach))
            .thenReturn(innerServiceResponse);

        // When
        CmsResponse<Boolean> actualResponse = authorisationServiceInternalEncrypted.updateScaApproach(AUTHORISATION_ID, scaApproach);

        // Then
        assertEquals(innerServiceResponse, actualResponse);
        verify(authorisationService).updateScaApproach(AUTHORISATION_ID, scaApproach);
    }

    @Test
    void getAuthorisationScaApproach() {
        // Given
        AuthorisationScaApproachResponse approachResponse = new AuthorisationScaApproachResponse(ScaApproach.REDIRECT);
        CmsResponse<AuthorisationScaApproachResponse> innerServiceResponse = CmsResponse.<AuthorisationScaApproachResponse>builder().payload(approachResponse).build();
        when(authorisationService.getAuthorisationScaApproach(AUTHORISATION_ID))
            .thenReturn(innerServiceResponse);

        // When
        CmsResponse<AuthorisationScaApproachResponse> actualResponse = authorisationServiceInternalEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID);

        // Then
        assertEquals(innerServiceResponse, actualResponse);
        verify(authorisationService).getAuthorisationScaApproach(AUTHORISATION_ID);
    }
}
