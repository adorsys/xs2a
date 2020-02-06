/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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
import de.adorsys.psd2.consent.api.WrongChecksumException;
import de.adorsys.psd2.consent.api.ais.AisAccountConsent;
import de.adorsys.psd2.consent.api.ais.AisConsentStatusResponse;
import de.adorsys.psd2.consent.api.ais.CreateAisConsentRequest;
import de.adorsys.psd2.consent.api.ais.CreateAisConsentResponse;
import de.adorsys.psd2.consent.api.authorisation.AisAuthorisationParentHolder;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationRequest;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationResponse;
import de.adorsys.psd2.consent.api.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.consent.api.service.AccountServiceEncrypted;
import de.adorsys.psd2.consent.api.service.AisConsentServiceEncrypted;
import de.adorsys.psd2.consent.api.service.AuthorisationServiceEncrypted;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AisConsentControllerTest {
    private static final String CONSENT_ID = "ed4190c7-64ee-42fb-b671-d62645f54672";
    private static final String ACCOUNT_ID = "testAccountId";
    private static final String WRONG_CONSENT_ID = "Wrong consent id";
    private static final ConsentStatus CONSENT_STATUS = ConsentStatus.VALID;
    private static final CreateAuthorisationRequest CONSENT_AUTHORISATION_REQUEST = getConsentAuthorisationRequest();
    private static final UpdateAuthorisationRequest WRONG_CONSENT_AUTHORISATION_REQUEST = getWrongConsentAuthorisationRequest();
    private static final String PSU_ID = "4e5dbef0-2377-483f-9ab9-ad510c1a557a";
    private static final String WRONG_PSU_ID = "Wrong psu id";
    private static final String AUTHORISATION_ID = "2400de4c-1c74-4ca0-941d-8f56b828f31d";
    private static final String AUTHORISATION_ID_1 = "4400de4c-1c74-4ca0-941d-8f56b828f31d";
    private static final String WRONG_AUTHORISATION_ID = "Wrong authorization id";
    private static final Authorisation CONSENT_AUTHORISATION_RESPONSE = getConsentAuthorisationResponse();

    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;

    @InjectMocks
    private AisConsentController aisConsentController;

    @Mock
    private AisConsentServiceEncrypted aisConsentService;
    @Mock
    private AccountServiceEncrypted accountServiceEncrypted;
    @Mock
    private AuthorisationServiceEncrypted authorisationServiceEncrypted;

    @Captor
    private ArgumentCaptor<AisAuthorisationParentHolder> authorisationParentHolderCaptor;

    @Test
    void createConsent_success() throws WrongChecksumException {
        // Given
        CreateAisConsentRequest createRequest = new CreateAisConsentRequest();
        CreateAisConsentResponse serviceResponse = new CreateAisConsentResponse(CONSENT_ID, new AisAccountConsent(), Arrays.asList(NotificationSupportedMode.LAST, NotificationSupportedMode.SCA));
        when(aisConsentService.createConsent(createRequest))
            .thenReturn(CmsResponse.<CreateAisConsentResponse>builder().payload(serviceResponse).build());

        // When
        ResponseEntity<CreateAisConsentResponse> actualResponse = aisConsentController.createConsent(createRequest);

        // Then
        assertEquals(HttpStatus.CREATED, actualResponse.getStatusCode());
        assertEquals(serviceResponse, actualResponse.getBody());
    }

    @Test
    void createConsent_emptyServiceResponse() throws WrongChecksumException {
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
    void getConsentStatusById_Success() {
        // Given
        when(aisConsentService.getConsentStatusById(eq(CONSENT_ID)))
            .thenReturn(CmsResponse.<ConsentStatus>builder().payload(ConsentStatus.RECEIVED).build());

        //When:
        ResponseEntity<AisConsentStatusResponse> responseEntity = aisConsentController.getConsentStatusById(CONSENT_ID);

        //Then:
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(ConsentStatus.RECEIVED, responseEntity.getBody().getConsentStatus());
    }

    @Test
    void getConsentStatusById_Fail() {
        when(aisConsentService.getConsentStatusById(eq(WRONG_CONSENT_ID)))
            .thenReturn(CmsResponse.<ConsentStatus>builder().error(CmsError.TECHNICAL_ERROR).build());

        //When:
        ResponseEntity<AisConsentStatusResponse> responseEntity = aisConsentController.getConsentStatusById(WRONG_CONSENT_ID);

        //Then:
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    void updateConsentStatus_Success() throws WrongChecksumException {
        when(aisConsentService.updateConsentStatusById(eq(CONSENT_ID), eq(CONSENT_STATUS)))
            .thenReturn(CmsResponse.<Boolean>builder().payload(true).build());

        //When:
        ResponseEntity responseEntity = aisConsentController.updateConsentStatus(CONSENT_ID, CONSENT_STATUS.name());

        //Then:
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    void updateConsentStatus_Fail() throws WrongChecksumException {
        when(aisConsentService.updateConsentStatusById(WRONG_CONSENT_ID, CONSENT_STATUS))
            .thenReturn(CmsResponse.<Boolean>builder().payload(false).build());

        //When:
        ResponseEntity responseEntity = aisConsentController.updateConsentStatus(WRONG_CONSENT_ID, CONSENT_STATUS.name());

        //Then:
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    void createConsentAuthorization_Success() {
        when(authorisationServiceEncrypted.createAuthorisation(authorisationParentHolderCaptor.capture(), eq(CONSENT_AUTHORISATION_REQUEST)))
            .thenReturn(CmsResponse.<CreateAuthorisationResponse>builder().payload(buildCreateAisConsentAuthorisationResponse()).build());

        //Given:
        CreateAuthorisationRequest expectedRequest = getConsentAuthorisationRequest();

        //When:
        ResponseEntity<CreateAuthorisationResponse> responseEntity = aisConsentController.createConsentAuthorization(CONSENT_ID, expectedRequest);

        //Then:
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals(AUTHORISATION_ID, responseEntity.getBody().getAuthorizationId());
        assertEquals(CONSENT_ID, authorisationParentHolderCaptor.getValue().getParentId());
    }

    @Test
    void createConsentAuthorization_Fail_WrongConsentId() {
        when(authorisationServiceEncrypted.createAuthorisation(authorisationParentHolderCaptor.capture(), eq(CONSENT_AUTHORISATION_REQUEST)))
            .thenReturn(CmsResponse.<CreateAuthorisationResponse>builder().error(CmsError.TECHNICAL_ERROR).build());

        //Given:
        CreateAuthorisationRequest expectedRequest = getConsentAuthorisationRequest();

        //When:
        ResponseEntity<CreateAuthorisationResponse> responseEntity = aisConsentController.createConsentAuthorization(WRONG_CONSENT_ID, expectedRequest);

        //Then:
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(WRONG_CONSENT_ID, authorisationParentHolderCaptor.getValue().getParentId());
    }

    @Test
    void createConsentAuthorization_Fail_WrondRequest() {
        when(authorisationServiceEncrypted.createAuthorisation(authorisationParentHolderCaptor.capture(), eq(getConsentAuthorisationRequest())))
            .thenReturn(CmsResponse.<CreateAuthorisationResponse>builder().error(CmsError.TECHNICAL_ERROR).build());

        //Given:
        CreateAuthorisationRequest expectedRequest = getConsentAuthorisationRequest();

        //When:
        ResponseEntity<CreateAuthorisationResponse> responseEntity = aisConsentController.createConsentAuthorization(CONSENT_ID, expectedRequest);

        //Then:
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(CONSENT_ID, authorisationParentHolderCaptor.getValue().getParentId());
    }

    @Test
    void updateConsentAuthorization_Success() {
        UpdateAuthorisationRequest request = new UpdateAuthorisationRequest();
        Authorisation authorisation = new Authorisation();
        authorisation.setParentId(CONSENT_ID);
        when(authorisationServiceEncrypted.updateAuthorisation(anyString(), eq(request)))
            .thenReturn(CmsResponse.<Authorisation>builder().payload(authorisation).build());

        //Given:

        //When:
        ResponseEntity responseEntity = aisConsentController.updateConsentAuthorization(AUTHORISATION_ID_1, request);

        //Then:
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    void updateConsentAuthorization_Fail_WrongConsentId() {

        //Given:
        UpdateAuthorisationRequest request = new UpdateAuthorisationRequest();
        when(authorisationServiceEncrypted.updateAuthorisation(AUTHORISATION_ID, request))
            .thenReturn(CmsResponse.<Authorisation>builder().payload(new Authorisation()).build());

        //When:
        ResponseEntity responseEntity = aisConsentController.updateConsentAuthorization(AUTHORISATION_ID, request);

        //Then:
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    void updateConsentAuthorization_Fail_WrongAuthorizationId() {
        //Given:
        UpdateAuthorisationRequest request = new UpdateAuthorisationRequest();
        when(authorisationServiceEncrypted.updateAuthorisation(WRONG_AUTHORISATION_ID, request))
            .thenReturn(CmsResponse.<Authorisation>builder().payload(new Authorisation()).build());

        //When:
        ResponseEntity responseEntity = aisConsentController.updateConsentAuthorization(WRONG_AUTHORISATION_ID, request);

        //Then:
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    void updateConsentAuthorization_Fail_WrongRequest() {
        when(authorisationServiceEncrypted.updateAuthorisation(AUTHORISATION_ID, WRONG_CONSENT_AUTHORISATION_REQUEST))
            .thenReturn(CmsResponse.<Authorisation>builder().payload(new Authorisation()).build());
        //When:
        ResponseEntity responseEntity = aisConsentController.updateConsentAuthorization(AUTHORISATION_ID, WRONG_CONSENT_AUTHORISATION_REQUEST);

        //Then:
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    void getConsentAuthorization_Success() {
        when(authorisationServiceEncrypted.getAuthorisationById(eq(AUTHORISATION_ID)))
            .thenReturn(CmsResponse.<Authorisation>builder().payload(CONSENT_AUTHORISATION_RESPONSE).build());

        //When:
        ResponseEntity<Authorisation> responseEntity = aisConsentController.getConsentAuthorization(CONSENT_ID, AUTHORISATION_ID);

        //Then:
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(getConsentAuthorisationResponse(), responseEntity.getBody());
    }

    @Test
    void getConsentAuthorization_Fail_WrongConsentId() {
        when(authorisationServiceEncrypted.getAuthorisationById(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<Authorisation>builder().error(CmsError.TECHNICAL_ERROR).build());

        //When:
        ResponseEntity<Authorisation> responseEntity = aisConsentController.getConsentAuthorization(WRONG_CONSENT_ID, AUTHORISATION_ID);

        //Then:
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    void getConsentAuthorization_Fail_WrongAuthorizationId() {
        when(authorisationServiceEncrypted.getAuthorisationById(eq(WRONG_AUTHORISATION_ID)))
            .thenReturn(CmsResponse.<Authorisation>builder().error(CmsError.TECHNICAL_ERROR).build());

        //When:
        ResponseEntity<Authorisation> responseEntity = aisConsentController.getConsentAuthorization(CONSENT_ID, WRONG_AUTHORISATION_ID);

        //Then:
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    void getConsentAuthorizationScaStatus_success() {
        when(authorisationServiceEncrypted.getAuthorisationScaStatus(AUTHORISATION_ID, new AisAuthorisationParentHolder(CONSENT_ID)))
            .thenReturn(CmsResponse.<ScaStatus>builder().payload(SCA_STATUS).build());

        // When
        ResponseEntity<ScaStatus> result = aisConsentController.getConsentAuthorizationScaStatus(CONSENT_ID, AUTHORISATION_ID);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(SCA_STATUS, result.getBody());
    }

    @Test
    void getConsentAuthorizationScaStatus_failure_wrongIds() {
        when(authorisationServiceEncrypted.getAuthorisationScaStatus(WRONG_AUTHORISATION_ID, new AisAuthorisationParentHolder(WRONG_CONSENT_ID)))
            .thenReturn(CmsResponse.<ScaStatus>builder().error(CmsError.TECHNICAL_ERROR).build());

        // When
        ResponseEntity<ScaStatus> result = aisConsentController.getConsentAuthorizationScaStatus(WRONG_CONSENT_ID, WRONG_AUTHORISATION_ID);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNull(result.getBody());
    }

    @Test
    void getAuthorisationScaApproach_success() {
        when(authorisationServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<AuthorisationScaApproachResponse>builder().payload(new AuthorisationScaApproachResponse(ScaApproach.EMBEDDED)).build());

        ResponseEntity<AuthorisationScaApproachResponse> response = aisConsentController.getAuthorisationScaApproach(AUTHORISATION_ID);

        verify(authorisationServiceEncrypted, times(1)).getAuthorisationScaApproach(eq(AUTHORISATION_ID));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ScaApproach.EMBEDDED, response.getBody().getScaApproach());
    }

    @Test
    void getAuthorisationScaApproach_error() {
        when(authorisationServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<AuthorisationScaApproachResponse>builder().error(CmsError.TECHNICAL_ERROR).build());

        ResponseEntity<AuthorisationScaApproachResponse> response = aisConsentController.getAuthorisationScaApproach(AUTHORISATION_ID);

        verify(authorisationServiceEncrypted, times(1)).getAuthorisationScaApproach(eq(AUTHORISATION_ID));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void saveNumberOfTransactions_success() {
        when(accountServiceEncrypted.saveNumberOfTransactions(CONSENT_ID, ACCOUNT_ID, 5))
            .thenReturn(Boolean.TRUE);

        ResponseEntity<Boolean> response = aisConsentController.saveNumberOfTransactions(CONSENT_ID, ACCOUNT_ID, 5);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void saveNumberOfTransactions_error() {
        when(accountServiceEncrypted.saveNumberOfTransactions(CONSENT_ID, ACCOUNT_ID, 5))
            .thenReturn(Boolean.FALSE);

        ResponseEntity<Boolean> response = aisConsentController.saveNumberOfTransactions(CONSENT_ID, ACCOUNT_ID, 5);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    private static CreateAuthorisationRequest getConsentAuthorisationRequest() {
        CreateAuthorisationRequest request = new CreateAuthorisationRequest();
        request.setPsuData(new PsuIdData(PSU_ID, null, null, null, null));

        return request;
    }

    private static UpdateAuthorisationRequest getWrongConsentAuthorisationRequest() {
        UpdateAuthorisationRequest request = new UpdateAuthorisationRequest();
        request.setPsuData(new PsuIdData(WRONG_PSU_ID, null, null, null, null));
        request.setPassword("zzz");

        return request;
    }

    private static Authorisation getConsentAuthorisationResponse() {
        Authorisation authorizationResponse = new Authorisation();
        authorizationResponse.setAuthorisationId(AUTHORISATION_ID);
        authorizationResponse.setParentId(CONSENT_ID);
        authorizationResponse.setPsuIdData(new PsuIdData(PSU_ID, null, null, null, null));

        return authorizationResponse;
    }

    private CreateAuthorisationResponse buildCreateAisConsentAuthorisationResponse() {
        return new CreateAuthorisationResponse(AUTHORISATION_ID, ScaStatus.RECEIVED, "", null);
    }
}
