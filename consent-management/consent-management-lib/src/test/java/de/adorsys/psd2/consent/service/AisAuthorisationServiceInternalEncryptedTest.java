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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.CmsScaMethod;
import de.adorsys.psd2.consent.api.ais.AisConsentAuthorizationRequest;
import de.adorsys.psd2.consent.api.ais.AisConsentAuthorizationResponse;
import de.adorsys.psd2.consent.api.service.AisConsentAuthorisationService;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AisAuthorisationServiceInternalEncryptedTest {
    private static final String ENCRYPTED_CONSENT_ID = "encrypted consent id";
    private static final String UNDECRYPTABLE_CONSENT_ID = "undecryptable consent id";
    private static final String DECRYPTED_CONSENT_ID = "255574b2-f115-4f3c-8d77-c1897749c060";
    private static final String AUTHORISATION_ID = "b3ecf205-da94-4e83-837b-5cd93ab88120";
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    private static final String AUTHENTICATION_METHOD_ID = "Method id";

    @InjectMocks
    private AisAuthorisationServiceInternalEncrypted aisAuthorisationServiceInternalEncrypted;
    @Mock
    private AisConsentAuthorisationService aisConsentAuthorisationService;
    @Mock
    private SecurityDataService securityDataService;

    @Before
    public void setUp() {
        when(securityDataService.encryptId(DECRYPTED_CONSENT_ID))
            .thenReturn(Optional.of(ENCRYPTED_CONSENT_ID));
        when(securityDataService.decryptId(ENCRYPTED_CONSENT_ID))
            .thenReturn(Optional.of(DECRYPTED_CONSENT_ID));
        when(securityDataService.decryptId(UNDECRYPTABLE_CONSENT_ID))
            .thenReturn(Optional.empty());
        when(aisConsentAuthorisationService.createAuthorization(DECRYPTED_CONSENT_ID, buildAisConsentAuthorisationRequest()))
            .thenReturn(Optional.of(AUTHORISATION_ID));
        when(aisConsentAuthorisationService.getAccountConsentAuthorizationById(AUTHORISATION_ID, DECRYPTED_CONSENT_ID))
            .thenReturn(Optional.of(buildAisConsentAuthorizationResponse()));
        when(aisConsentAuthorisationService.updateConsentAuthorization(AUTHORISATION_ID, buildAisConsentAuthorisationRequest()))
            .thenReturn(true);
        when(aisConsentAuthorisationService.getAuthorisationsByConsentId(DECRYPTED_CONSENT_ID))
            .thenReturn(Optional.of(buildAuthorisations()));

        when(aisConsentAuthorisationService.getAuthorisationScaStatus(DECRYPTED_CONSENT_ID, AUTHORISATION_ID))
            .thenReturn(Optional.of(SCA_STATUS));
    }

    @Test
    public void createAuthorization_success() {
        // Given
        AisConsentAuthorizationRequest request = buildAisConsentAuthorisationRequest();

        // When
        Optional<String> actual = aisAuthorisationServiceInternalEncrypted.createAuthorization(ENCRYPTED_CONSENT_ID, request);

        // Then
        assertTrue(actual.isPresent());

        assertEquals(AUTHORISATION_ID, actual.get());
        verify(aisConsentAuthorisationService, times(1)).createAuthorization(DECRYPTED_CONSENT_ID, request);
    }

    @Test
    public void createAuthorization_internalServiceFailed() {
        when(aisConsentAuthorisationService.createAuthorization(any(), any())).thenReturn(Optional.empty());

        // Given
        AisConsentAuthorizationRequest request = buildAisConsentAuthorisationRequest();

        // When
        Optional<String> actual = aisAuthorisationServiceInternalEncrypted.createAuthorization(ENCRYPTED_CONSENT_ID, request);

        // Then
        assertFalse(actual.isPresent());
        verify(aisConsentAuthorisationService, times(1)).createAuthorization(DECRYPTED_CONSENT_ID, request);
    }

    @Test
    public void createAuthorization_decryptionFailed() {
        // Given
        AisConsentAuthorizationRequest request = buildAisConsentAuthorisationRequest();

        // When
        Optional<String> actual = aisAuthorisationServiceInternalEncrypted.createAuthorization(UNDECRYPTABLE_CONSENT_ID, request);

        // Then
        assertFalse(actual.isPresent());
        verify(aisConsentAuthorisationService, never()).createAuthorization(any(), any());
    }

    @Test
    public void getAccountConsentAuthorizationById_success() {
        // Given
        AisConsentAuthorizationResponse expected = buildAisConsentAuthorizationResponse();

        // When
        Optional<AisConsentAuthorizationResponse> actual = aisAuthorisationServiceInternalEncrypted.getAccountConsentAuthorizationById(AUTHORISATION_ID, ENCRYPTED_CONSENT_ID);

        // Then
        assertTrue(actual.isPresent());

        assertEquals(expected, actual.get());
        verify(aisConsentAuthorisationService, times(1)).getAccountConsentAuthorizationById(AUTHORISATION_ID, DECRYPTED_CONSENT_ID);
    }

    @Test
    public void getAccountConsentAuthorizationById_internalServiceFailed() {
        when(aisConsentAuthorisationService.getAccountConsentAuthorizationById(any(), any())).thenReturn(Optional.empty());

        // When
        Optional<AisConsentAuthorizationResponse> actual = aisAuthorisationServiceInternalEncrypted.getAccountConsentAuthorizationById(AUTHORISATION_ID, ENCRYPTED_CONSENT_ID);

        // Then
        assertFalse(actual.isPresent());
        verify(aisConsentAuthorisationService, times(1)).getAccountConsentAuthorizationById(AUTHORISATION_ID, DECRYPTED_CONSENT_ID);
    }

    @Test
    public void getAccountConsentAuthorizationById_decryptionFailed() {
        // When
        Optional<AisConsentAuthorizationResponse> actual = aisAuthorisationServiceInternalEncrypted.getAccountConsentAuthorizationById(AUTHORISATION_ID, UNDECRYPTABLE_CONSENT_ID);

        // Then
        assertFalse(actual.isPresent());
        verify(aisConsentAuthorisationService, never()).getAccountConsentAuthorizationById(any(), any());
    }

    @Test
    public void updateConsentAuthorization_success() {
        // Given
        AisConsentAuthorizationRequest request = buildAisConsentAuthorisationRequest();

        // When
        boolean actual = aisAuthorisationServiceInternalEncrypted.updateConsentAuthorization(AUTHORISATION_ID, request);

        // Then
        assertTrue(actual);
        verify(aisConsentAuthorisationService, times(1)).updateConsentAuthorization(AUTHORISATION_ID, request);
    }

    @Test
    public void updateConsentAuthorization_internalServiceFailed() {
        when(aisConsentAuthorisationService.updateConsentAuthorization(any(), any())).thenReturn(false);

        // Given
        AisConsentAuthorizationRequest request = buildAisConsentAuthorisationRequest();

        // When
        boolean actual = aisAuthorisationServiceInternalEncrypted.updateConsentAuthorization(AUTHORISATION_ID, request);

        // Then
        assertFalse(actual);
        verify(aisConsentAuthorisationService, times(1)).updateConsentAuthorization(AUTHORISATION_ID, request);
    }

    @Test
    public void getAuthorisationsByConsentId_success() {
        // Given
        List<String> expected = buildAuthorisations();

        // When
        Optional<List<String>> actual = aisAuthorisationServiceInternalEncrypted.getAuthorisationsByConsentId(ENCRYPTED_CONSENT_ID);

        // Then
        assertTrue(actual.isPresent());

        assertEquals(expected, actual.get());
        verify(aisConsentAuthorisationService, times(1)).getAuthorisationsByConsentId(DECRYPTED_CONSENT_ID);
    }

    @Test
    public void getAuthorisationsByConsentId_internalServiceFailed() {
        when(aisConsentAuthorisationService.getAuthorisationsByConsentId(any())).thenReturn(Optional.empty());

        // When
        Optional<List<String>> actual = aisAuthorisationServiceInternalEncrypted.getAuthorisationsByConsentId(ENCRYPTED_CONSENT_ID);

        // Then
        assertFalse(actual.isPresent());
        verify(aisConsentAuthorisationService, times(1)).getAuthorisationsByConsentId(DECRYPTED_CONSENT_ID);
    }

    @Test
    public void getAuthorisationsByConsentId_decryptionFailed() {
        // When
        Optional<List<String>> actual = aisAuthorisationServiceInternalEncrypted.getAuthorisationsByConsentId(UNDECRYPTABLE_CONSENT_ID);

        // Then
        assertFalse(actual.isPresent());
        verify(aisConsentAuthorisationService, never()).getAuthorisationsByConsentId(any());
    }

    @Test
    public void getAuthorisationScaStatus_success() {
        // When
        Optional<ScaStatus> actual = aisAuthorisationServiceInternalEncrypted.getAuthorisationScaStatus(ENCRYPTED_CONSENT_ID, AUTHORISATION_ID);

        // Then
        assertTrue(actual.isPresent());
        assertEquals(SCA_STATUS, actual.get());
        verify(aisConsentAuthorisationService, times(1)).getAuthorisationScaStatus(DECRYPTED_CONSENT_ID, AUTHORISATION_ID);
    }

    @Test
    public void getAuthorisationScaStatus_internalServiceFailed() {
        when(aisConsentAuthorisationService.getAuthorisationScaStatus(anyString(), anyString())).thenReturn(Optional.empty());

        // When
        Optional<ScaStatus> actual = aisAuthorisationServiceInternalEncrypted.getAuthorisationScaStatus(ENCRYPTED_CONSENT_ID, AUTHORISATION_ID);

        // Then
        assertFalse(actual.isPresent());
        verify(aisConsentAuthorisationService, times(1)).getAuthorisationScaStatus(DECRYPTED_CONSENT_ID, AUTHORISATION_ID);
    }

    @Test
    public void getAuthorisationScaStatus_decryptionFailed() {
        // When
        Optional<ScaStatus> actual = aisAuthorisationServiceInternalEncrypted.getAuthorisationScaStatus(UNDECRYPTABLE_CONSENT_ID, AUTHORISATION_ID);

        // Then
        assertFalse(actual.isPresent());
        verify(aisConsentAuthorisationService, never()).getAuthorisationsByConsentId(any());
    }

    @Test
    public void isAuthenticationMethodDecoupled_success() {
        // Given
        when(aisConsentAuthorisationService.isAuthenticationMethodDecoupled(anyString(), anyString())).thenReturn(true);

        // When
        boolean actual = aisAuthorisationServiceInternalEncrypted.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID);

        // Then
        assertTrue(actual);
        verify(aisConsentAuthorisationService, times(1)).isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID);
    }

    @Test
    public void isAuthenticationMethodDecoupled_internalServiceFailed() {
        // Given
        when(aisConsentAuthorisationService.isAuthenticationMethodDecoupled(anyString(), anyString())).thenReturn(false);

        // When
        boolean actual = aisAuthorisationServiceInternalEncrypted.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID);

        // Then
        assertFalse(actual);
        verify(aisConsentAuthorisationService, times(1)).isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID);
    }

    @Test
    public void saveAuthenticationMethods_success() {
        // Given
        when(aisConsentAuthorisationService.saveAuthenticationMethods(anyString(), any())).thenReturn(true);
        List<CmsScaMethod> cmsScaMethods = Collections.singletonList(buildCmsScaMethod());

        // When
        boolean actual = aisAuthorisationServiceInternalEncrypted.saveAuthenticationMethods(AUTHORISATION_ID, cmsScaMethods);

        // Then
        assertTrue(actual);
        verify(aisConsentAuthorisationService, times(1)).saveAuthenticationMethods(AUTHORISATION_ID, cmsScaMethods);
    }

    @Test
    public void saveAuthenticationMethods_internalServiceFailed() {
        // Given
        when(aisConsentAuthorisationService.saveAuthenticationMethods(anyString(), any())).thenReturn(false);
        List<CmsScaMethod> cmsScaMethods = Collections.singletonList(buildCmsScaMethod());

        // When
        boolean actual = aisAuthorisationServiceInternalEncrypted.saveAuthenticationMethods(AUTHORISATION_ID, cmsScaMethods);

        // Then
        assertFalse(actual);
        verify(aisConsentAuthorisationService, times(1)).saveAuthenticationMethods(AUTHORISATION_ID, cmsScaMethods);
    }

    private AisConsentAuthorizationRequest buildAisConsentAuthorisationRequest() {
        return new AisConsentAuthorizationRequest();
    }

    private AisConsentAuthorizationResponse buildAisConsentAuthorizationResponse() {
        return new AisConsentAuthorizationResponse();
    }

    private List<String> buildAuthorisations() {
        return Collections.singletonList(AUTHORISATION_ID);
    }

    private CmsScaMethod buildCmsScaMethod() {
        return new CmsScaMethod(AUTHENTICATION_METHOD_ID, true);
    }
}
