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

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.ais.AisAccountConsent;
import de.adorsys.psd2.consent.api.ais.CreateAisConsentRequest;
import de.adorsys.psd2.consent.api.ais.CreateAisConsentResponse;
import de.adorsys.psd2.consent.config.AisConsentRemoteUrls;
import de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AisConsentServiceRemoteTest {
    private static final String URL = "http://some.url";
    private static final String CONSENT_ID = "some consent id";

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private AisConsentRemoteUrls remoteAisConsentUrls;

    @InjectMocks
    private AisConsentServiceRemote aisConsentServiceRemote;

    @Test
    void createConsent_shouldReturnResponse() {
        // Given
        when(remoteAisConsentUrls.createAisConsent()).thenReturn(URL);
        CreateAisConsentRequest createRequest = new CreateAisConsentRequest();
        CreateAisConsentResponse controllerResponse = new CreateAisConsentResponse(CONSENT_ID, new AisAccountConsent(), Arrays.asList(NotificationSupportedMode.LAST, NotificationSupportedMode.SCA));
        when(restTemplate.postForEntity(URL, createRequest, CreateAisConsentResponse.class))
            .thenReturn(new ResponseEntity<>(controllerResponse, HttpStatus.CREATED));

        // When
        CmsResponse<CreateAisConsentResponse> actualResponse = aisConsentServiceRemote.createConsent(createRequest);

        // Then
        assertTrue(actualResponse.isSuccessful());
        assertEquals(controllerResponse, actualResponse.getPayload());
    }

    @Test
    void createConsent_withNullBodyInResponse_shouldReturnEmpty() {
        // Given
        when(remoteAisConsentUrls.createAisConsent()).thenReturn(URL);
        CreateAisConsentRequest createRequest = new CreateAisConsentRequest();
        when(restTemplate.postForEntity(URL, createRequest, CreateAisConsentResponse.class))
            .thenReturn(new ResponseEntity<>(null, HttpStatus.CREATED));

        // When
        CmsResponse<CreateAisConsentResponse> actualResponse = aisConsentServiceRemote.createConsent(createRequest);

        // Then
        assertFalse(actualResponse.isSuccessful());
    }
}
