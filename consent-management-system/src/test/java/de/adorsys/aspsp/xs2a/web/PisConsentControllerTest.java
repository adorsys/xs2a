/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.aspsp.xs2a.web;

import de.adorsys.aspsp.xs2a.consent.api.CmsAspspConsentData;
import de.adorsys.aspsp.xs2a.consent.api.CmsScaStatus;
import de.adorsys.aspsp.xs2a.consent.api.pis.PisPaymentType;
import de.adorsys.aspsp.xs2a.consent.api.pis.authorisation.GetPisConsentAuthorisationResponse;
import de.adorsys.aspsp.xs2a.service.PisConsentService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PisConsentControllerTest {
    private static final String AUTHORISATION_ID = "auth_id";
    private static final String CONSENT_ID = "139c5d0e-0c03-473b-b047-30fd66d869db";
    private static final String PASSWORD = "password";
    private static final String PSU_ID = "testPSU";
    private static final byte[] CONSENT_DATA = "consent data".getBytes();

    @InjectMocks
    private PisConsentController pisConsentController;
    @Mock
    private PisConsentService pisConsentService;

    @Test
    public void getConsentAuthorization_Success() {
        GetPisConsentAuthorisationResponse response = getGetPisConsentAuthorisationResponse();
        when(pisConsentService.getPisConsentAuthorizationById(any())).thenReturn(Optional.of(response));

        // Given
        GetPisConsentAuthorisationResponse expectedResponse = getGetPisConsentAuthorisationResponse();

        // When
        ResponseEntity<GetPisConsentAuthorisationResponse> result =
            pisConsentController.getConsentAuthorization(AUTHORISATION_ID);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(expectedResponse);
    }

    @Test
    public void getConsentAuthorization_Failure() {
        when(pisConsentService.getPisConsentAuthorizationById(any())).thenReturn(Optional.empty());

        // When
        ResponseEntity<GetPisConsentAuthorisationResponse> result =
            pisConsentController.getConsentAuthorization(AUTHORISATION_ID);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getBody()).isNull();
    }

    private GetPisConsentAuthorisationResponse getGetPisConsentAuthorisationResponse() {
        GetPisConsentAuthorisationResponse response = new GetPisConsentAuthorisationResponse();
        response.setPsuId(PSU_ID);
        response.setScaStatus(CmsScaStatus.STARTED);
        response.setConsentId(CONSENT_ID);
        response.setPassword(PASSWORD);
        response.setPayments(Collections.emptyList());
        response.setPaymentType(PisPaymentType.SINGLE);
        response.setCmsAspspConsentData(new CmsAspspConsentData(CONSENT_DATA));
        return response;
    }
}
