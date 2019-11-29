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


package de.adorsys.psd2.consent.web.xs2a.controller;


import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.ais.*;
import de.adorsys.psd2.consent.api.service.AccountServiceEncrypted;
import de.adorsys.psd2.consent.api.service.AisConsentAuthorisationServiceEncrypted;
import de.adorsys.psd2.consent.api.service.AisConsentServiceEncrypted;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AisConsentControllerTest {

    private static final String CONSENT_ID = "ed4190c7-64ee-42fb-b671-d62645f54672";
    private static final String ACCOUNT_ID = "testAccountId";
    private static final String WRONG_CONSENT_ID = "Wrong consent id";
    private static final ConsentStatus CONSENT_STATUS = ConsentStatus.VALID;
    private static final ConsentStatus WRONG_CONSENT_STATUS = ConsentStatus.EXPIRED;
    private static final AisConsentAuthorizationRequest CONSENT_AUTHORISATION_REQUEST = getConsentAuthorisationRequest();
    private static final AisConsentAuthorizationRequest WRONG_CONSENT_AUTHORISATION_REQUEST = getWrongConsentAuthorisationRequest();
    private static final String PSU_ID = "4e5dbef0-2377-483f-9ab9-ad510c1a557a";
    private static final String WRONG_PSU_ID = "Wrong psu id";
    private static final String AUTHORISATION_ID = "2400de4c-1c74-4ca0-941d-8f56b828f31d";
    private static final String AUTHORISATION_ID_1 = "4400de4c-1c74-4ca0-941d-8f56b828f31d";
    private static final String WRONG_AUTHORISATION_ID = "Wrong authorization id";
    private static final AisConsentAuthorizationResponse CONSENT_AUTHORISATION_RESPONSE = getConsentAuthorisationResponse();

    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;

    @InjectMocks
    private AisConsentController aisConsentController;

    @Mock
    private AisConsentServiceEncrypted aisConsentService;
    @Mock
    private AccountServiceEncrypted accountServiceEncrypted;
    @Mock
    private AisConsentAuthorisationServiceEncrypted aisAuthorisationServiceEncrypted;

    @Before
    public void setUp() {
        when(aisConsentService.getConsentStatusById(eq(CONSENT_ID)))
            .thenReturn(CmsResponse.<ConsentStatus>builder().payload(ConsentStatus.RECEIVED).build());
        when(aisConsentService.getConsentStatusById(eq(WRONG_CONSENT_ID)))
            .thenReturn(CmsResponse.<ConsentStatus>builder().error(CmsError.TECHNICAL_ERROR).build());
        when(aisConsentService.updateConsentStatusById(CONSENT_ID, CONSENT_STATUS))
            .thenReturn(CmsResponse.<Boolean>builder().payload(true).build());
        when(aisConsentService.updateConsentStatusById(WRONG_CONSENT_ID, WRONG_CONSENT_STATUS))
            .thenReturn(CmsResponse.<Boolean>builder().payload(false).build());
        when(aisConsentService.updateConsentStatusById(WRONG_CONSENT_ID, CONSENT_STATUS))
            .thenReturn(CmsResponse.<Boolean>builder().payload(false).build());
        when(aisAuthorisationServiceEncrypted.createAuthorizationWithResponse(eq(CONSENT_ID), eq(CONSENT_AUTHORISATION_REQUEST)))
            .thenReturn(CmsResponse.<CreateAisConsentAuthorizationResponse>builder().payload(buildCreateAisConsentAuthorizationResponse()).build());
        when(aisAuthorisationServiceEncrypted.createAuthorizationWithResponse(eq(WRONG_CONSENT_ID), eq(CONSENT_AUTHORISATION_REQUEST)))
            .thenReturn(CmsResponse.<CreateAisConsentAuthorizationResponse>builder().error(CmsError.TECHNICAL_ERROR).build());
        when(aisAuthorisationServiceEncrypted.createAuthorizationWithResponse(eq(CONSENT_ID), eq(WRONG_CONSENT_AUTHORISATION_REQUEST)))
            .thenReturn(CmsResponse.<CreateAisConsentAuthorizationResponse>builder().error(CmsError.TECHNICAL_ERROR).build());
        when(aisAuthorisationServiceEncrypted.updateConsentAuthorization(AUTHORISATION_ID_1, CONSENT_AUTHORISATION_REQUEST))
            .thenReturn(CmsResponse.<Boolean>builder().payload(true).build());
        when(aisAuthorisationServiceEncrypted.updateConsentAuthorization(AUTHORISATION_ID, CONSENT_AUTHORISATION_REQUEST))
            .thenReturn(CmsResponse.<Boolean>builder().payload(false).build());
        when(aisAuthorisationServiceEncrypted.updateConsentAuthorization(WRONG_AUTHORISATION_ID, CONSENT_AUTHORISATION_REQUEST))
            .thenReturn(CmsResponse.<Boolean>builder().payload(false).build());
        when(aisAuthorisationServiceEncrypted.updateConsentAuthorization(AUTHORISATION_ID, WRONG_CONSENT_AUTHORISATION_REQUEST))
            .thenReturn(CmsResponse.<Boolean>builder().payload(false).build());
        when(aisAuthorisationServiceEncrypted.updateConsentAuthorization(WRONG_AUTHORISATION_ID, WRONG_CONSENT_AUTHORISATION_REQUEST))
            .thenReturn(CmsResponse.<Boolean>builder().payload(false).build());
        when(aisAuthorisationServiceEncrypted.getAccountConsentAuthorizationById(eq(AUTHORISATION_ID), eq(CONSENT_ID)))
            .thenReturn(CmsResponse.<AisConsentAuthorizationResponse>builder().payload(CONSENT_AUTHORISATION_RESPONSE).build());
        when(aisAuthorisationServiceEncrypted.getAccountConsentAuthorizationById(eq(AUTHORISATION_ID), eq(WRONG_CONSENT_ID)))
            .thenReturn(CmsResponse.<AisConsentAuthorizationResponse>builder().error(CmsError.TECHNICAL_ERROR).build());
        when(aisAuthorisationServiceEncrypted.getAccountConsentAuthorizationById(eq(WRONG_AUTHORISATION_ID), eq(CONSENT_ID)))
            .thenReturn(CmsResponse.<AisConsentAuthorizationResponse>builder().error(CmsError.TECHNICAL_ERROR).build());
    }

    @Test
    public void createConsent_success() {
        // Given
        CreateAisConsentRequest createRequest = new CreateAisConsentRequest();
        CreateAisConsentResponse serviceResponse = new CreateAisConsentResponse(CONSENT_ID, new AisAccountConsent(), null);
        when(aisConsentService.createConsent(createRequest))
            .thenReturn(CmsResponse.<CreateAisConsentResponse>builder().payload(serviceResponse).build());

        // When
        ResponseEntity<CreateAisConsentResponse> actualResponse = aisConsentController.createConsent(createRequest);

        // Then
        assertEquals(HttpStatus.CREATED, actualResponse.getStatusCode());
        assertEquals(serviceResponse, actualResponse.getBody());
    }

    @Test
    public void createConsent_emptyServiceResponse() {
        // Given
        CreateAisConsentRequest createRequest = new CreateAisConsentRequest();
        when(aisConsentService.createConsent(createRequest))
            .thenReturn(CmsResponse.<CreateAisConsentResponse>builder().error(CmsError.TECHNICAL_ERROR).build());

        // When
        ResponseEntity<CreateAisConsentResponse> actualResponse = aisConsentController.createConsent(createRequest);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, actualResponse.getStatusCode());
        assertNull(actualResponse.getBody());
    }

    @Test
    public void getConsentStatusById_Success() {

        //When:
        ResponseEntity<AisConsentStatusResponse> responseEntity = aisConsentController.getConsentStatusById(CONSENT_ID);

        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().getConsentStatus()).isEqualTo(ConsentStatus.RECEIVED);
    }

    @Test
    public void getConsentStatusById_Fail() {

        //When:
        ResponseEntity<AisConsentStatusResponse> responseEntity = aisConsentController.getConsentStatusById(WRONG_CONSENT_ID);

        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void updateConsentStatus_Success() {
        //When:
        ResponseEntity responseEntity = aisConsentController.updateConsentStatus(CONSENT_ID, CONSENT_STATUS.name());

        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void updateConsentStatus_Fail() {
        //When:
        ResponseEntity responseEntity = aisConsentController.updateConsentStatus(WRONG_CONSENT_ID, CONSENT_STATUS.name());

        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void createConsentAuthorization_Success() {

        //Given:
        AisConsentAuthorizationRequest expectedRequest = getConsentAuthorisationRequest();

        //When:
        ResponseEntity<CreateAisConsentAuthorizationResponse> responseEntity = aisConsentController.createConsentAuthorization(CONSENT_ID, expectedRequest);

        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody().getAuthorizationId()).isEqualTo(AUTHORISATION_ID);
    }

    @Test
    public void createConsentAuthorization_Fail_WrongConsentId() {

        //Given:
        AisConsentAuthorizationRequest expectedRequest = getConsentAuthorisationRequest();

        //When:
        ResponseEntity<CreateAisConsentAuthorizationResponse> responseEntity = aisConsentController.createConsentAuthorization(WRONG_CONSENT_ID, expectedRequest);

        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void createConsentAuthorization_Fail_WrondRequest() {

        //Given:
        AisConsentAuthorizationRequest expectedRequest = getWrongConsentAuthorisationRequest();

        //When:
        ResponseEntity<CreateAisConsentAuthorizationResponse> responseEntity = aisConsentController.createConsentAuthorization(CONSENT_ID, expectedRequest);

        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void updateConsentAuthorization_Success() {
        doReturn(CmsResponse.<Boolean>builder().payload(true).build())
            .when(aisAuthorisationServiceEncrypted).updateConsentAuthorization(anyString(), any(AisConsentAuthorizationRequest.class));

        //Given:
        AisConsentAuthorizationRequest expectedRequest = getConsentAuthorisationRequest();

        //When:
        ResponseEntity responseEntity = aisConsentController.updateConsentAuthorization(AUTHORISATION_ID_1, expectedRequest);

        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void updateConsentAuthorization_Fail_WrongConsentId() {
        //When:
        ResponseEntity responseEntity = aisConsentController.updateConsentAuthorization(AUTHORISATION_ID, CONSENT_AUTHORISATION_REQUEST);

        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void updateConsentAuthorization_Fail_WrongAuthorizationId() {
        //When:
        ResponseEntity responseEntity = aisConsentController.updateConsentAuthorization(WRONG_AUTHORISATION_ID, CONSENT_AUTHORISATION_REQUEST);

        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void updateConsentAuthorization_Fail_WrongRequest() {
        //When:
        ResponseEntity responseEntity = aisConsentController.updateConsentAuthorization(AUTHORISATION_ID, WRONG_CONSENT_AUTHORISATION_REQUEST);

        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void getConsentAuthorization_Success() {

        //When:
        ResponseEntity<AisConsentAuthorizationResponse> responseEntity = aisConsentController.getConsentAuthorization(CONSENT_ID, AUTHORISATION_ID);

        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo(getConsentAuthorisationResponse());
    }

    @Test
    public void getConsentAuthorization_Fail_WrongConsentId() {

        //When:
        ResponseEntity<AisConsentAuthorizationResponse> responseEntity = aisConsentController.getConsentAuthorization(WRONG_CONSENT_ID, AUTHORISATION_ID);

        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void getConsentAuthorization_Fail_WrongAuthorizationId() {

        //When:
        ResponseEntity<AisConsentAuthorizationResponse> responseEntity = aisConsentController.getConsentAuthorization(CONSENT_ID, WRONG_AUTHORISATION_ID);

        //Then:
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void getConsentAuthorizationScaStatus_success() {
        when(aisAuthorisationServiceEncrypted.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID))
            .thenReturn(CmsResponse.<ScaStatus>builder().payload(SCA_STATUS).build());

        // When
        ResponseEntity<ScaStatus> result = aisConsentController.getConsentAuthorizationScaStatus(CONSENT_ID, AUTHORISATION_ID);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(SCA_STATUS);
    }

    @Test
    public void getConsentAuthorizationScaStatus_failure_wrongIds() {
        when(aisAuthorisationServiceEncrypted.getAuthorisationScaStatus(WRONG_CONSENT_ID, WRONG_AUTHORISATION_ID))
            .thenReturn(CmsResponse.<ScaStatus>builder().error(CmsError.TECHNICAL_ERROR).build());

        // When
        ResponseEntity<ScaStatus> result = aisConsentController.getConsentAuthorizationScaStatus(WRONG_CONSENT_ID, WRONG_AUTHORISATION_ID);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getBody()).isNull();
    }

    @Test
    public void getAuthorisationScaApproach_success() {
        when(aisAuthorisationServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<AuthorisationScaApproachResponse>builder().payload(new AuthorisationScaApproachResponse(ScaApproach.EMBEDDED)).build());

        ResponseEntity<AuthorisationScaApproachResponse> response = aisConsentController.getAuthorisationScaApproach(AUTHORISATION_ID);

        verify(aisAuthorisationServiceEncrypted, times(1)).getAuthorisationScaApproach(eq(AUTHORISATION_ID));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getScaApproach()).isEqualTo(ScaApproach.EMBEDDED);
    }

    @Test
    public void getAuthorisationScaApproach_error() {
        when(aisAuthorisationServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<AuthorisationScaApproachResponse>builder().error(CmsError.TECHNICAL_ERROR).build());

        ResponseEntity<AuthorisationScaApproachResponse> response = aisConsentController.getAuthorisationScaApproach(AUTHORISATION_ID);

        verify(aisAuthorisationServiceEncrypted, times(1)).getAuthorisationScaApproach(eq(AUTHORISATION_ID));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }

    @Test
    public void saveNumberOfTransactions_success() {
        when(accountServiceEncrypted.saveNumberOfTransactions(CONSENT_ID, ACCOUNT_ID, 5))
            .thenReturn(Boolean.TRUE);

        ResponseEntity<Boolean> response = aisConsentController.saveNumberOfTransactions(CONSENT_ID, ACCOUNT_ID, 5);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    public void saveNumberOfTransactions_error() {
        when(accountServiceEncrypted.saveNumberOfTransactions(CONSENT_ID, ACCOUNT_ID, 5))
            .thenReturn(Boolean.FALSE);

        ResponseEntity<Boolean> response = aisConsentController.saveNumberOfTransactions(CONSENT_ID, ACCOUNT_ID, 5);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }

    private static AisConsentAuthorizationRequest getConsentAuthorisationRequest() {
        AisConsentAuthorizationRequest request = new AisConsentAuthorizationRequest();
        request.setPsuData(new PsuIdData(PSU_ID, null, null, null));
        request.setPassword("zzz");

        return request;
    }

    private static AisConsentAuthorizationRequest getWrongConsentAuthorisationRequest() {
        AisConsentAuthorizationRequest request = new AisConsentAuthorizationRequest();
        request.setPsuData(new PsuIdData(WRONG_PSU_ID, null, null, null));
        request.setPassword("zzz");

        return request;
    }

    private static AisConsentAuthorizationResponse getConsentAuthorisationResponse() {
        AisConsentAuthorizationResponse authorizationResponse = new AisConsentAuthorizationResponse();
        authorizationResponse.setAuthorizationId(AUTHORISATION_ID);
        authorizationResponse.setConsentId(CONSENT_ID);
        authorizationResponse.setPsuIdData(new PsuIdData(PSU_ID, null, null, null));

        return authorizationResponse;
    }

    private CreateAisConsentAuthorizationResponse buildCreateAisConsentAuthorizationResponse() {
        return new CreateAisConsentAuthorizationResponse(AUTHORISATION_ID, ScaStatus.RECEIVED, "", null);
    }
}
