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

import de.adorsys.psd2.consent.api.ais.AisConsentAuthorizationResponse;
import de.adorsys.psd2.consent.config.AisConsentRemoteUrls;
import de.adorsys.psd2.consent.config.CmsRestException;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AisConsentAuthorisationServiceRemoteTest {
    private static final String CONSENT_ID = "some consent id";
    private static final String AUTHORISATION_ID = "2400de4c-1c74-4ca0-941d-8f56b828f31d";
    private static final String URL = "http://base.url";

    @InjectMocks
    private AisConsentAuthorisationServiceRemote service;

    @Mock
    private RestTemplate consentRestTemplate;
    @Mock
    private AisConsentRemoteUrls remoteAisConsentUrls;
    @Mock
    private CmsRestException cmsRestException;

    @Test
    void getAuthorisationScaApproach() {
        when(remoteAisConsentUrls.getAuthorisationScaApproach()).thenReturn(URL);
        when(consentRestTemplate.getForEntity(URL, AuthorisationScaApproachResponse.class, AUTHORISATION_ID))
            .thenReturn(ResponseEntity.ok(new AuthorisationScaApproachResponse(ScaApproach.EMBEDDED)));

        service.getAuthorisationScaApproach(AUTHORISATION_ID);

        verify(remoteAisConsentUrls, times(1)).getAuthorisationScaApproach();
        verify(consentRestTemplate).getForEntity(eq(URL), eq(AuthorisationScaApproachResponse.class), eq(AUTHORISATION_ID));
    }

    @Test
    void getAccountConsentAuthorizationById_success() {
        // Given
        AisConsentAuthorizationResponse consentAuthorisationResponse = new AisConsentAuthorizationResponse();
        consentAuthorisationResponse.setConsentId(CONSENT_ID);
        consentAuthorisationResponse.setAuthorizationId(AUTHORISATION_ID);

        when(remoteAisConsentUrls.getAisConsentAuthorizationById())
            .thenReturn(URL);

        when(consentRestTemplate.getForEntity(URL, AisConsentAuthorizationResponse.class, CONSENT_ID, AUTHORISATION_ID))
            .thenReturn(ResponseEntity.ok(consentAuthorisationResponse));

        // When
        Optional<AisConsentAuthorizationResponse> actualResponse = service.getAccountConsentAuthorizationById(AUTHORISATION_ID, CONSENT_ID);

        // Then
        assertEquals(Optional.of(consentAuthorisationResponse), actualResponse);
    }

    @Test
    void getAccountConsentAuthorizationById_withNullBodyInResponse_shouldReturnEmpty() {
        // Given
        AisConsentAuthorizationResponse consentAuthorisationResponse = new AisConsentAuthorizationResponse();
        consentAuthorisationResponse.setConsentId(CONSENT_ID);
        consentAuthorisationResponse.setAuthorizationId(AUTHORISATION_ID);

        when(remoteAisConsentUrls.getAisConsentAuthorizationById())
            .thenReturn(URL);

        when(consentRestTemplate.getForEntity(URL, AisConsentAuthorizationResponse.class, CONSENT_ID, AUTHORISATION_ID))
            .thenReturn(ResponseEntity.ok(null));

        // When
        Optional<AisConsentAuthorizationResponse> actualResponse = service.getAccountConsentAuthorizationById(AUTHORISATION_ID, CONSENT_ID);

        // Then
        assertFalse(actualResponse.isPresent());
    }

    @Test
    void getAccountConsentAuthorizationById_withCmsRestException_shouldReturnEmpty() {
        // Given
        AisConsentAuthorizationResponse consentAuthorisationResponse = new AisConsentAuthorizationResponse();
        consentAuthorisationResponse.setConsentId(CONSENT_ID);
        consentAuthorisationResponse.setAuthorizationId(AUTHORISATION_ID);

        when(remoteAisConsentUrls.getAisConsentAuthorizationById())
            .thenReturn(URL);

        when(consentRestTemplate.getForEntity(URL, AisConsentAuthorizationResponse.class, CONSENT_ID, AUTHORISATION_ID))
            .thenThrow(cmsRestException);

        // When
        Optional<AisConsentAuthorizationResponse> actualResponse = service.getAccountConsentAuthorizationById(AUTHORISATION_ID, CONSENT_ID);

        // Then
        assertFalse(actualResponse.isPresent());
    }
}
