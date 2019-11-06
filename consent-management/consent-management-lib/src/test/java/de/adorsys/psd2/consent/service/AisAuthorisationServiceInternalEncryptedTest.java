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

import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.CmsScaMethod;
import de.adorsys.psd2.consent.api.ais.AisConsentAuthorizationRequest;
import de.adorsys.psd2.consent.api.ais.AisConsentAuthorizationResponse;
import de.adorsys.psd2.consent.api.service.AisConsentAuthorisationService;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AisAuthorisationServiceInternalEncryptedTest {
    private static final String ENCRYPTED_CONSENT_ID = "encrypted consent id";
    private static final String UNDECRYPTABLE_CONSENT_ID = "undecryptable consent id";
    private static final String DECRYPTED_CONSENT_ID = "255574b2-f115-4f3c-8d77-c1897749c060";
    private static final String AUTHORISATION_ID = "b3ecf205-da94-4e83-837b-5cd93ab88120";
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    private static final ScaApproach SCA_APPROACH = ScaApproach.EMBEDDED;
    private static final String AUTHENTICATION_METHOD_ID = "Method id";

    @InjectMocks
    private AisAuthorisationServiceInternalEncrypted aisAuthorisationServiceInternalEncrypted;
    @Mock
    private AisConsentAuthorisationService aisConsentAuthorisationService;
    @Mock
    private SecurityDataService securityDataService;

    @Before
    public void setUp() {
        when(securityDataService.decryptId(ENCRYPTED_CONSENT_ID))
            .thenReturn(Optional.of(DECRYPTED_CONSENT_ID));
        when(securityDataService.decryptId(UNDECRYPTABLE_CONSENT_ID))
            .thenReturn(Optional.empty());
        when(aisConsentAuthorisationService.getAccountConsentAuthorizationById(AUTHORISATION_ID, DECRYPTED_CONSENT_ID))
            .thenReturn(CmsResponse.<AisConsentAuthorizationResponse>builder()
                            .payload(buildAisConsentAuthorizationResponse())
                            .build());
        when(aisConsentAuthorisationService.updateConsentAuthorization(AUTHORISATION_ID, buildAisConsentAuthorisationRequest()))
            .thenReturn(CmsResponse.<Boolean>builder()
                            .payload(true)
                            .build());
        when(aisConsentAuthorisationService.getAuthorisationsByConsentId(DECRYPTED_CONSENT_ID))
            .thenReturn(CmsResponse.<List<String>>builder()
                            .payload(buildAuthorisations())
                            .build());

        when(aisConsentAuthorisationService.getAuthorisationScaStatus(DECRYPTED_CONSENT_ID, AUTHORISATION_ID))
            .thenReturn(CmsResponse.<ScaStatus>builder()
                            .payload(SCA_STATUS)
                            .build());
    }

    @Test
    public void getAccountConsentAuthorizationById_success() {
        // Given
        AisConsentAuthorizationResponse expected = buildAisConsentAuthorizationResponse();

        // When
        CmsResponse<AisConsentAuthorizationResponse> actual = aisAuthorisationServiceInternalEncrypted.getAccountConsentAuthorizationById(AUTHORISATION_ID, ENCRYPTED_CONSENT_ID);

        // Then
        assertTrue(actual.isSuccessful());

        assertEquals(expected, actual.getPayload());
        verify(aisConsentAuthorisationService, times(1)).getAccountConsentAuthorizationById(AUTHORISATION_ID, DECRYPTED_CONSENT_ID);
    }

    @Test
    public void getAccountConsentAuthorizationById_internalServiceFailed() {
        when(aisConsentAuthorisationService.getAccountConsentAuthorizationById(any(), any()))
            .thenReturn(CmsResponse.<AisConsentAuthorizationResponse>builder()
                            .error(CmsError.LOGICAL_ERROR)
                            .build());

        // When
        CmsResponse<AisConsentAuthorizationResponse> actual = aisAuthorisationServiceInternalEncrypted.getAccountConsentAuthorizationById(AUTHORISATION_ID, ENCRYPTED_CONSENT_ID);

        // Then
        assertTrue(actual.hasError());
        assertEquals(CmsError.LOGICAL_ERROR, actual.getError());
        verify(aisConsentAuthorisationService, times(1)).getAccountConsentAuthorizationById(AUTHORISATION_ID, DECRYPTED_CONSENT_ID);
    }

    @Test
    public void getAccountConsentAuthorizationById_decryptionFailed() {
        // When
        CmsResponse<AisConsentAuthorizationResponse> actual = aisAuthorisationServiceInternalEncrypted.getAccountConsentAuthorizationById(AUTHORISATION_ID, UNDECRYPTABLE_CONSENT_ID);

        // Then
        assertTrue(actual.hasError());
        assertEquals(CmsError.TECHNICAL_ERROR, actual.getError());
        verify(aisConsentAuthorisationService, never()).getAccountConsentAuthorizationById(any(), any());
    }

    @Test
    public void updateConsentAuthorization_success() {
        // Given
        AisConsentAuthorizationRequest request = buildAisConsentAuthorisationRequest();

        // When
        CmsResponse<Boolean> actual = aisAuthorisationServiceInternalEncrypted.updateConsentAuthorization(AUTHORISATION_ID, request);

        // Then
        assertTrue(actual.isSuccessful());

        assertTrue(actual.getPayload());
        verify(aisConsentAuthorisationService, times(1)).updateConsentAuthorization(AUTHORISATION_ID, request);
    }

    @Test
    public void updateConsentAuthorization_internalServiceFailed() {
        when(aisConsentAuthorisationService.updateConsentAuthorization(any(), any())).thenReturn(CmsResponse.<Boolean>builder().error(CmsError.LOGICAL_ERROR).build());

        // Given
        AisConsentAuthorizationRequest request = buildAisConsentAuthorisationRequest();

        // When
        CmsResponse<Boolean> actual = aisAuthorisationServiceInternalEncrypted.updateConsentAuthorization(AUTHORISATION_ID, request);

        // Then
        assertTrue(actual.hasError());

        assertEquals(CmsError.LOGICAL_ERROR, actual.getError());
        verify(aisConsentAuthorisationService, times(1)).updateConsentAuthorization(AUTHORISATION_ID, request);
    }

    @Test
    public void getAuthorisationsByConsentId_success() {
        // Given
        List<String> expected = buildAuthorisations();

        // When
        CmsResponse<List<String>> actual = aisAuthorisationServiceInternalEncrypted.getAuthorisationsByConsentId(ENCRYPTED_CONSENT_ID);

        // Then
        assertTrue(actual.isSuccessful());

        assertEquals(expected, actual.getPayload());
        verify(aisConsentAuthorisationService, times(1)).getAuthorisationsByConsentId(DECRYPTED_CONSENT_ID);
    }

    @Test
    public void getAuthorisationsByConsentId_internalServiceFailed() {
        when(aisConsentAuthorisationService.getAuthorisationsByConsentId(any()))
            .thenReturn(CmsResponse.<List<String>>builder()
                            .error(CmsError.LOGICAL_ERROR)
                            .build());

        // When
        CmsResponse<List<String>> actual = aisAuthorisationServiceInternalEncrypted.getAuthorisationsByConsentId(ENCRYPTED_CONSENT_ID);

        // Then
        assertTrue(actual.hasError());

        assertEquals(CmsError.LOGICAL_ERROR, actual.getError());
        verify(aisConsentAuthorisationService, times(1)).getAuthorisationsByConsentId(DECRYPTED_CONSENT_ID);
    }

    @Test
    public void getAuthorisationsByConsentId_decryptionFailed() {
        // When
        CmsResponse<List<String>> actual = aisAuthorisationServiceInternalEncrypted.getAuthorisationsByConsentId(UNDECRYPTABLE_CONSENT_ID);

        // Then
        assertTrue(actual.hasError());

        assertEquals(CmsError.TECHNICAL_ERROR, actual.getError());
        verify(aisConsentAuthorisationService, never()).getAuthorisationsByConsentId(any());
    }

    @Test
    public void getAuthorisationScaApproach_success() {
        //Given
        when(aisConsentAuthorisationService.getAuthorisationScaApproach(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<AuthorisationScaApproachResponse>builder()
                            .payload(new AuthorisationScaApproachResponse(SCA_APPROACH))
                            .build());
        // When
        CmsResponse<AuthorisationScaApproachResponse> actual = aisAuthorisationServiceInternalEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID);

        // Then
        assertTrue(actual.isSuccessful());
        assertEquals(SCA_APPROACH, actual.getPayload().getScaApproach());

        verify(aisConsentAuthorisationService, times(1)).getAuthorisationScaApproach(eq(AUTHORISATION_ID));
    }

    @Test
    public void getAuthorisationScaStatus_success() {
        // When
        CmsResponse<ScaStatus> actual = aisAuthorisationServiceInternalEncrypted.getAuthorisationScaStatus(ENCRYPTED_CONSENT_ID, AUTHORISATION_ID);

        // Then
        assertTrue(actual.isSuccessful());
        assertEquals(SCA_STATUS, actual.getPayload());
        verify(aisConsentAuthorisationService, times(1)).getAuthorisationScaStatus(DECRYPTED_CONSENT_ID, AUTHORISATION_ID);
    }

    @Test
    public void getAuthorisationScaStatus_internalServiceFailed() {
        when(aisConsentAuthorisationService.getAuthorisationScaStatus(anyString(), anyString()))
            .thenReturn(CmsResponse.<ScaStatus>builder()
                            .error(CmsError.LOGICAL_ERROR)
                            .build());

        // When
        CmsResponse<ScaStatus> actual = aisAuthorisationServiceInternalEncrypted.getAuthorisationScaStatus(ENCRYPTED_CONSENT_ID, AUTHORISATION_ID);

        // Then
        assertTrue(actual.hasError());

        assertEquals(CmsError.LOGICAL_ERROR, actual.getError());
        verify(aisConsentAuthorisationService, times(1)).getAuthorisationScaStatus(DECRYPTED_CONSENT_ID, AUTHORISATION_ID);
    }

    @Test
    public void getAuthorisationScaStatus_decryptionFailed() {
        // When
        CmsResponse<ScaStatus> actual = aisAuthorisationServiceInternalEncrypted.getAuthorisationScaStatus(UNDECRYPTABLE_CONSENT_ID, AUTHORISATION_ID);

        // Then
        assertTrue(actual.hasError());

        assertEquals(CmsError.TECHNICAL_ERROR, actual.getError());
        verify(aisConsentAuthorisationService, never()).getAuthorisationsByConsentId(any());
    }

    @Test
    public void isAuthenticationMethodDecoupled_success() {
        // Given
        when(aisConsentAuthorisationService.isAuthenticationMethodDecoupled(anyString(), anyString()))
            .thenReturn(CmsResponse.<Boolean>builder()
                            .payload(true)
                            .build());

        // When
        CmsResponse<Boolean> actual = aisAuthorisationServiceInternalEncrypted.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID);

        // Then
        assertTrue(actual.isSuccessful());

        assertTrue(actual.getPayload());
        verify(aisConsentAuthorisationService, times(1)).isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID);
    }

    @Test
    public void isAuthenticationMethodDecoupled_internalServiceFailed() {
        // Given
        when(aisConsentAuthorisationService.isAuthenticationMethodDecoupled(anyString(), anyString()))
            .thenReturn(CmsResponse.<Boolean>builder()
                            .error(CmsError.LOGICAL_ERROR)
                            .build());

        // When
        CmsResponse<Boolean> actual = aisAuthorisationServiceInternalEncrypted.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID);

        // Then
        assertTrue(actual.hasError());

        assertEquals(CmsError.LOGICAL_ERROR, actual.getError());
        verify(aisConsentAuthorisationService, times(1)).isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID);
    }

    @Test
    public void saveAuthenticationMethods_success() {
        // Given
        when(aisConsentAuthorisationService.saveAuthenticationMethods(anyString(), any()))
            .thenReturn(CmsResponse.<Boolean>builder()
                            .payload(true)
                            .build());
        List<CmsScaMethod> cmsScaMethods = Collections.singletonList(buildCmsScaMethod());

        // When
        CmsResponse<Boolean> actual = aisAuthorisationServiceInternalEncrypted.saveAuthenticationMethods(AUTHORISATION_ID, cmsScaMethods);

        // Then
        assertTrue(actual.isSuccessful());

        assertTrue(actual.getPayload());
        verify(aisConsentAuthorisationService, times(1)).saveAuthenticationMethods(AUTHORISATION_ID, cmsScaMethods);
    }

    @Test
    public void saveAuthenticationMethods_internalServiceFailed() {
        // Given
        when(aisConsentAuthorisationService.saveAuthenticationMethods(anyString(), any()))
            .thenReturn(CmsResponse.<Boolean>builder()
                            .error(CmsError.LOGICAL_ERROR)
                            .build());
        List<CmsScaMethod> cmsScaMethods = Collections.singletonList(buildCmsScaMethod());

        // When
        CmsResponse<Boolean> actual = aisAuthorisationServiceInternalEncrypted.saveAuthenticationMethods(AUTHORISATION_ID, cmsScaMethods);

        // Then
        assertTrue(actual.hasError());

        assertEquals(CmsError.LOGICAL_ERROR, actual.getError());
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

