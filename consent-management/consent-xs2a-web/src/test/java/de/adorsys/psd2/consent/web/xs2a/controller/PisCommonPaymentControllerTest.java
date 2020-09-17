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
import de.adorsys.psd2.consent.api.authorisation.*;
import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentDataStatusResponse;
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.consent.api.service.AuthorisationServiceEncrypted;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PisCommonPaymentControllerTest {

    private static final String AUTHORISATION_ID = "345-9245-2359";
    private static final String PAYMENT_ID = "33333-999999999";
    private static final String STATUS_RECEIVED = "Received";
    private static final String PSU_ID = "testPSU";
    private static final String PASSWORD = "password";
    private static final String CANCELLATION_AUTHORISATION_ID = "dd5d766f-eeb7-4efe-b730-24d5ed53f537";
    private static final String WRONG_CANCELLATION_AUTHORISATION_ID = "wrong cancellation authorisation id";

    private static final String WRONG_AUTHORISATION_ID = "3254890-5";
    private static final String WRONG_PAYMENT_ID = "32343-999997777";

    private static final String TPP_REDIRECT_URI = "request/redirect_uri";
    private static final String TPP_NOK_REDIRECT_URI = "request/nok_redirect_uri";
    private static final TppRedirectUri TPP_REDIRECT_URIs = new TppRedirectUri(TPP_REDIRECT_URI, TPP_NOK_REDIRECT_URI);

    private static final PsuIdData PSU_DATA = new PsuIdData(PSU_ID, null, null, null, null);
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    private static final CreateAuthorisationRequest CREATE_AUTHORISATION_REQUEST = new CreateAuthorisationRequest(PSU_DATA, ScaApproach.REDIRECT, TPP_REDIRECT_URIs);

    @InjectMocks
    private PisCommonPaymentController pisCommonPaymentController;

    @Mock
    private PisCommonPaymentServiceEncrypted pisCommonPaymentService;
    @Mock
    private AuthorisationServiceEncrypted authorisationServiceEncrypted;

    @Test
    void createCommonPayment_Success() {
        //Given
        ResponseEntity<CreatePisCommonPaymentResponse> expected = new ResponseEntity<>(new CreatePisCommonPaymentResponse(PAYMENT_ID, null), HttpStatus.CREATED);
        when(pisCommonPaymentService.createCommonPayment(getPisPaymentInfo()))
            .thenReturn(CmsResponse.<CreatePisCommonPaymentResponse>builder().payload(getCreatePisCommonPaymentResponse()).build());

        //When
        ResponseEntity<CreatePisCommonPaymentResponse> actual = pisCommonPaymentController.createCommonPayment(getPisPaymentInfo());

        //Then
        assertEquals(expected, actual);
    }

    @Test
    void createCommonPayment_Failure() {
        //Given
        when(pisCommonPaymentService.createCommonPayment(getPisPaymentInfo()))
            .thenReturn(CmsResponse.<CreatePisCommonPaymentResponse>builder().error(CmsError.TECHNICAL_ERROR).build());
        ResponseEntity<CreatePisCommonPaymentResponse> expected = new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        //When
        ResponseEntity<CreatePisCommonPaymentResponse> actual = pisCommonPaymentController.createCommonPayment(getPisPaymentInfo());

        //Then
        assertEquals(expected, actual);
    }

    @Test
    void getConsentStatusById_Success() {
        //Given
        ResponseEntity<PisCommonPaymentDataStatusResponse> expected = new ResponseEntity<>(new PisCommonPaymentDataStatusResponse(TransactionStatus.RCVD), HttpStatus.OK);
        when(pisCommonPaymentService.getPisCommonPaymentStatusById(PAYMENT_ID))
            .thenReturn(CmsResponse.<TransactionStatus>builder().payload(TransactionStatus.RCVD).build());

        //When
        ResponseEntity<PisCommonPaymentDataStatusResponse> actual = pisCommonPaymentController.getPisCommonPaymentStatusById(PAYMENT_ID);

        //Then
        assertEquals(expected, actual);
    }

    @Test
    void getConsentStatusById_Failure() {
        //Given
        when(pisCommonPaymentService.getPisCommonPaymentStatusById(WRONG_PAYMENT_ID))
            .thenReturn(CmsResponse.<TransactionStatus>builder().error(CmsError.TECHNICAL_ERROR).build());
        ResponseEntity<PisCommonPaymentDataStatusResponse> expected = new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        //When
        ResponseEntity<PisCommonPaymentDataStatusResponse> actual = pisCommonPaymentController.getPisCommonPaymentStatusById(WRONG_PAYMENT_ID);

        //Then
        assertEquals(expected, actual);
    }

    @Test
    void getConsentById_Success() {
        //Given
        ResponseEntity<PisCommonPaymentResponse> expected = new ResponseEntity<>(new PisCommonPaymentResponse(), HttpStatus.OK);
        when(pisCommonPaymentService.getCommonPaymentById(PAYMENT_ID))
            .thenReturn(CmsResponse.<PisCommonPaymentResponse>builder().payload(getPisCommonPaymentResponse()).build());

        //When
        ResponseEntity<PisCommonPaymentResponse> actual = pisCommonPaymentController.getCommonPaymentById(PAYMENT_ID);

        //Then
        assertEquals(expected, actual);
    }

    @Test
    void getConsentById_Failure() {
        //Given
        when(pisCommonPaymentService.getCommonPaymentById(WRONG_PAYMENT_ID))
            .thenReturn(CmsResponse.<PisCommonPaymentResponse>builder().error(CmsError.TECHNICAL_ERROR).build());
        ResponseEntity<PisCommonPaymentResponse> expected = new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        //When
        ResponseEntity<PisCommonPaymentResponse> actual = pisCommonPaymentController.getCommonPaymentById(WRONG_PAYMENT_ID);

        //Then
        assertEquals(expected, actual);
    }

    @Test
    void updateConsentStatus_Success() {
        //Given
        ResponseEntity<Void> expected = new ResponseEntity<>(HttpStatus.OK);
        when(pisCommonPaymentService.updateCommonPaymentStatusById(PAYMENT_ID, TransactionStatus.RCVD))
            .thenReturn(CmsResponse.<Boolean>builder().payload(true).build());

        //When
        ResponseEntity<Void> actual = pisCommonPaymentController.updateCommonPaymentStatus(PAYMENT_ID, STATUS_RECEIVED);

        //Then
        assertEquals(expected, actual);
    }

    @Test
    void updateConsentStatus_Failure() {
        //Given
        when(pisCommonPaymentService.updateCommonPaymentStatusById(WRONG_PAYMENT_ID, TransactionStatus.RCVD))
            .thenReturn(CmsResponse.<Boolean>builder().payload(false).build());
        ResponseEntity<Void> expected = new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        //Then
        ResponseEntity<Void> actual = pisCommonPaymentController.updateCommonPaymentStatus(WRONG_PAYMENT_ID, STATUS_RECEIVED);

        //Then
        assertEquals(expected, actual);
    }

    @Test
    void createConsentAuthorization_Success() {
        //Given
        ResponseEntity<CreateAuthorisationResponse> expected = new ResponseEntity<>(new CreateAuthorisationResponse(AUTHORISATION_ID, SCA_STATUS, null, null), HttpStatus.CREATED);
        when(authorisationServiceEncrypted.createAuthorisation(new PisAuthorisationParentHolder(PAYMENT_ID), CREATE_AUTHORISATION_REQUEST))
            .thenReturn(CmsResponse.<CreateAuthorisationResponse>builder().payload(getCreateAuthorisationResponse()).build());

        //When
        ResponseEntity<CreateAuthorisationResponse> actual = pisCommonPaymentController.createAuthorisation(PAYMENT_ID, CREATE_AUTHORISATION_REQUEST);

        //Then
        assertEquals(expected, actual);
    }

    @Test
    void createConsentAuthorization_Failure() {
        //Given
        when(authorisationServiceEncrypted.createAuthorisation(new PisAuthorisationParentHolder(WRONG_PAYMENT_ID), CREATE_AUTHORISATION_REQUEST))
            .thenReturn(CmsResponse.<CreateAuthorisationResponse>builder().error(CmsError.TECHNICAL_ERROR).build());
        ResponseEntity<CreateAuthorisationResponse> expected = new ResponseEntity<>(HttpStatus.NOT_FOUND);

        //When
        ResponseEntity<CreateAuthorisationResponse> actual = pisCommonPaymentController.createAuthorisation(WRONG_PAYMENT_ID, CREATE_AUTHORISATION_REQUEST);

        //Then
        assertEquals(expected, actual);
    }

    @Test
    void updateConsentAuthorization_Success() {
        //Given
        Authorisation authorisation = new Authorisation();
        authorisation.setScaStatus(ScaStatus.RECEIVED);
        UpdateAuthorisationRequest updateAuthorisationRequest = new UpdateAuthorisationRequest();
        when(authorisationServiceEncrypted.updateAuthorisation(AUTHORISATION_ID, updateAuthorisationRequest))
            .thenReturn(CmsResponse.<Authorisation>builder().payload(getUpdatePisCommonPaymentPsuDataResponse()).build());

        //When
        ResponseEntity<Authorisation> actual = pisCommonPaymentController.updateAuthorisation(AUTHORISATION_ID, updateAuthorisationRequest);

        //Then
        ResponseEntity<Authorisation> expected = new ResponseEntity<>(authorisation, HttpStatus.OK);
        assertEquals(expected, actual);
    }

    @Test
    void updateConsentAuthorization_Failure() {
        //Given
        Authorisation authorisation = new Authorisation();
        authorisation.setScaStatus(ScaStatus.RECEIVED);
        UpdateAuthorisationRequest updateAuthorisationRequest = new UpdateAuthorisationRequest();
        when(authorisationServiceEncrypted.updateAuthorisation(WRONG_AUTHORISATION_ID, updateAuthorisationRequest))
            .thenReturn(CmsResponse.<Authorisation>builder().error(CmsError.TECHNICAL_ERROR).build());

        //When
        ResponseEntity<Authorisation> actual = pisCommonPaymentController.updateAuthorisation(WRONG_AUTHORISATION_ID, updateAuthorisationRequest);

        //Then
        ResponseEntity<Authorisation> expected = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        assertEquals(expected, actual);
    }

    @Test
    void getConsentAuthorization_Success() {
        Authorisation response = getGetPisAuthorisationResponse();
        when(authorisationServiceEncrypted.getAuthorisationById(any()))
            .thenReturn(CmsResponse.<Authorisation>builder().payload(response).build());

        // Given
        Authorisation expectedResponse = getGetPisAuthorisationResponse();

        // When
        ResponseEntity<Authorisation> result = pisCommonPaymentController.getAuthorisation(AUTHORISATION_ID);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expectedResponse, result.getBody());
    }

    @Test
    void getAuthorization_Failure() {
        when(authorisationServiceEncrypted.getAuthorisationById(any()))
            .thenReturn(CmsResponse.<Authorisation>builder().error(CmsError.TECHNICAL_ERROR).build());

        // When
        ResponseEntity<Authorisation> result =
            pisCommonPaymentController.getAuthorisation(AUTHORISATION_ID);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNull(result.getBody());
    }

    @Test
    void getAuthorisationScaStatus_success() {
        when(authorisationServiceEncrypted.getAuthorisationScaStatus(AUTHORISATION_ID, new PisAuthorisationParentHolder(PAYMENT_ID)))
            .thenReturn(CmsResponse.<ScaStatus>builder().payload(SCA_STATUS).build());

        // When
        ResponseEntity<ScaStatus> result = pisCommonPaymentController.getAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(SCA_STATUS, result.getBody());
    }

    @Test
    void getAuthorisationScaStatus_failure_wrongIds() {
        when(authorisationServiceEncrypted.getAuthorisationScaStatus(WRONG_AUTHORISATION_ID, new PisAuthorisationParentHolder(WRONG_PAYMENT_ID)))
            .thenReturn(CmsResponse.<ScaStatus>builder().error(CmsError.TECHNICAL_ERROR).build());

        // When
        ResponseEntity<ScaStatus> result = pisCommonPaymentController.getAuthorisationScaStatus(WRONG_PAYMENT_ID, WRONG_AUTHORISATION_ID);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNull(result.getBody());
    }

    @Test
    void getCancellationAuthorisationScaStatus_success() {
        when(authorisationServiceEncrypted.getAuthorisationScaStatus(CANCELLATION_AUTHORISATION_ID, new PisCancellationAuthorisationParentHolder(PAYMENT_ID)))
            .thenReturn(CmsResponse.<ScaStatus>builder().payload(SCA_STATUS).build());

        // When
        ResponseEntity<ScaStatus> result = pisCommonPaymentController.getCancellationAuthorisationScaStatus(PAYMENT_ID, CANCELLATION_AUTHORISATION_ID);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(SCA_STATUS, result.getBody());
    }

    @Test
    void getCancellationAuthorisationScaStatus_failure_wrongIds() {
        when(authorisationServiceEncrypted.getAuthorisationScaStatus(WRONG_CANCELLATION_AUTHORISATION_ID, new PisCancellationAuthorisationParentHolder(WRONG_PAYMENT_ID)))
            .thenReturn(CmsResponse.<ScaStatus>builder().error(CmsError.TECHNICAL_ERROR).build());

        // When
        ResponseEntity<ScaStatus> result = pisCommonPaymentController.getCancellationAuthorisationScaStatus(WRONG_PAYMENT_ID, WRONG_CANCELLATION_AUTHORISATION_ID);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNull(result.getBody());
    }

    @Test
    void getAuthorisationScaApproach_success() {
        when(authorisationServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<AuthorisationScaApproachResponse>builder().payload(new AuthorisationScaApproachResponse(ScaApproach.EMBEDDED)).build());

        ResponseEntity<AuthorisationScaApproachResponse> response = pisCommonPaymentController.getAuthorisationScaApproach(AUTHORISATION_ID);

        verify(authorisationServiceEncrypted, times(1)).getAuthorisationScaApproach(eq(AUTHORISATION_ID));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ScaApproach.EMBEDDED, response.getBody().getScaApproach());
    }

    @Test
    void getAuthorisationScaApproach_error() {
        when(authorisationServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<AuthorisationScaApproachResponse>builder().error(CmsError.TECHNICAL_ERROR).build());

        ResponseEntity<AuthorisationScaApproachResponse> response = pisCommonPaymentController.getAuthorisationScaApproach(AUTHORISATION_ID);

        verify(authorisationServiceEncrypted, times(1)).getAuthorisationScaApproach(eq(AUTHORISATION_ID));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getCancellationAuthorisationScaApproach_success() {
        when(authorisationServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<AuthorisationScaApproachResponse>builder().payload(new AuthorisationScaApproachResponse(ScaApproach.EMBEDDED)).build());

        ResponseEntity<AuthorisationScaApproachResponse> response = pisCommonPaymentController.getCancellationAuthorisationScaApproach(AUTHORISATION_ID);

        verify(authorisationServiceEncrypted, times(1)).getAuthorisationScaApproach(eq(AUTHORISATION_ID));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ScaApproach.EMBEDDED, response.getBody().getScaApproach());
    }

    @Test
    void getCancellationAuthorisationScaApproach_error() {
        when(authorisationServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<AuthorisationScaApproachResponse>builder().error(CmsError.TECHNICAL_ERROR).build());

        ResponseEntity<AuthorisationScaApproachResponse> response = pisCommonPaymentController.getCancellationAuthorisationScaApproach(AUTHORISATION_ID);

        verify(authorisationServiceEncrypted, times(1)).getAuthorisationScaApproach(eq(AUTHORISATION_ID));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void updateMultilevelScaRequired_Ok() {
        when(pisCommonPaymentService.updateMultilevelSca(PAYMENT_ID, true))
            .thenReturn(CmsResponse.<Boolean>builder().payload(true).build());

        ResponseEntity<Boolean> actualResponse = pisCommonPaymentController.updateMultilevelScaRequired(PAYMENT_ID, true);

        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertTrue(actualResponse.getBody());
    }

    @Test
    void updateMultilevelScaRequired_NotFound() {
        when(pisCommonPaymentService.updateMultilevelSca(PAYMENT_ID, true))
            .thenReturn(CmsResponse.<Boolean>builder().payload(false).build());

        ResponseEntity<Boolean> actualResponse = pisCommonPaymentController.updateMultilevelScaRequired(PAYMENT_ID, true);

        assertEquals(HttpStatus.NOT_FOUND, actualResponse.getStatusCode());
        assertNull(actualResponse.getBody());
    }

    private Authorisation getGetPisAuthorisationResponse() {
        Authorisation response = new Authorisation();
        response.setPsuIdData(new PsuIdData(PSU_ID, null, null, null, null));
        response.setScaStatus(ScaStatus.RECEIVED);
        response.setParentId(PAYMENT_ID);
        response.setPassword(PASSWORD);
        return response;
    }

    private PisPaymentInfo getPisPaymentInfo() {
        return new PisPaymentInfo();
    }

    private CreatePisCommonPaymentResponse getCreatePisCommonPaymentResponse() {
        return new CreatePisCommonPaymentResponse(PAYMENT_ID, null);
    }

    private PisCommonPaymentResponse getPisCommonPaymentResponse() {
        return new PisCommonPaymentResponse();
    }

    private CreateAuthorisationResponse getCreateAuthorisationResponse() {
        return new CreateAuthorisationResponse(AUTHORISATION_ID, SCA_STATUS, null, null);
    }

    private Authorisation getUpdatePisCommonPaymentPsuDataResponse() {
        Authorisation authorisation = new Authorisation();
        authorisation.setScaStatus(ScaStatus.RECEIVED);
        return authorisation;
    }
}
