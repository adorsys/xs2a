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
import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentDataStatusResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.*;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.consent.api.service.PisAuthorisationServiceEncrypted;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.xs2a.core.pis.PaymentAuthorisationType;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PisCommonPaymentControllerTest {

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

    private static final PsuIdData PSU_DATA = new PsuIdData(PSU_ID, null, null, null);
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    private static final CreatePisAuthorisationRequest CREATE_PIS_AUTHORISATION_REQUEST = new CreatePisAuthorisationRequest(PaymentAuthorisationType.CREATED, PSU_DATA, ScaApproach.REDIRECT, TPP_REDIRECT_URIs);

    @InjectMocks
    private PisCommonPaymentController pisCommonPaymentController;

    @Mock
    private PisCommonPaymentServiceEncrypted pisCommonPaymentService;
    @Mock
    private PisAuthorisationServiceEncrypted pisAuthorisationServiceEncrypted;

    @Before
    public void setUp() {
        when(pisCommonPaymentService.createCommonPayment(getPisPaymentInfo()))
            .thenReturn(CmsResponse.<CreatePisCommonPaymentResponse>builder().payload(getCreatePisCommonPaymentResponse()).build());
        when(pisCommonPaymentService.getPisCommonPaymentStatusById(PAYMENT_ID))
            .thenReturn(CmsResponse.<TransactionStatus>builder().payload(TransactionStatus.RCVD).build());
        when(pisCommonPaymentService.getCommonPaymentById(PAYMENT_ID))
            .thenReturn(CmsResponse.<PisCommonPaymentResponse>builder().payload(getPisCommonPaymentResponse()).build());
        when(pisCommonPaymentService.updateCommonPaymentStatusById(PAYMENT_ID, TransactionStatus.RCVD))
            .thenReturn(CmsResponse.<Boolean>builder().payload(true).build());
        when(pisAuthorisationServiceEncrypted.createAuthorization(PAYMENT_ID, CREATE_PIS_AUTHORISATION_REQUEST))
            .thenReturn(CmsResponse.<CreatePisAuthorisationResponse>builder().payload(getCreatePisAuthorisationResponse()).build());
        when(pisAuthorisationServiceEncrypted.updatePisAuthorisation(AUTHORISATION_ID, getUpdatePisCommonPaymentPsuDataRequest()))
            .thenReturn(CmsResponse.<UpdatePisCommonPaymentPsuDataResponse>builder().payload(getUpdatePisCommonPaymentPsuDataResponse()).build());
    }

    @Test
    public void createCommonPayment_Success() {
        //Given
        ResponseEntity<CreatePisCommonPaymentResponse> expected = new ResponseEntity<>(new CreatePisCommonPaymentResponse(PAYMENT_ID, null), HttpStatus.CREATED);

        //When
        ResponseEntity<CreatePisCommonPaymentResponse> actual = pisCommonPaymentController.createCommonPayment(getPisPaymentInfo());

        //Then
        assertEquals(actual, expected);
    }

    @Test
    public void createCommonPayment_Failure() {
        //Given
        when(pisCommonPaymentService.createCommonPayment(getPisPaymentInfo()))
            .thenReturn(CmsResponse.<CreatePisCommonPaymentResponse>builder().error(CmsError.TECHNICAL_ERROR).build());
        ResponseEntity<CreatePisCommonPaymentResponse> expected = new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        //When
        ResponseEntity<CreatePisCommonPaymentResponse> actual = pisCommonPaymentController.createCommonPayment(getPisPaymentInfo());

        //Then
        assertEquals(actual, expected);
    }

    @Test
    public void getConsentStatusById_Success() {
        //Given
        ResponseEntity<PisCommonPaymentDataStatusResponse> expected = new ResponseEntity<>(new PisCommonPaymentDataStatusResponse(TransactionStatus.RCVD), HttpStatus.OK);

        //When
        ResponseEntity<PisCommonPaymentDataStatusResponse> actual = pisCommonPaymentController.getPisCommonPaymentStatusById(PAYMENT_ID);

        //Then
        assertEquals(actual, expected);
    }

    @Test
    public void getConsentStatusById_Failure() {
        //Given
        when(pisCommonPaymentService.getPisCommonPaymentStatusById(WRONG_PAYMENT_ID))
            .thenReturn(CmsResponse.<TransactionStatus>builder().error(CmsError.TECHNICAL_ERROR).build());
        ResponseEntity<PisCommonPaymentDataStatusResponse> expected = new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        //When
        ResponseEntity<PisCommonPaymentDataStatusResponse> actual = pisCommonPaymentController.getPisCommonPaymentStatusById(WRONG_PAYMENT_ID);

        //Then
        assertEquals(actual, expected);
    }

    @Test
    public void getConsentById_Success() {
        //Given
        ResponseEntity<PisCommonPaymentResponse> expected = new ResponseEntity<>(new PisCommonPaymentResponse(), HttpStatus.OK);

        //When
        ResponseEntity<PisCommonPaymentResponse> actual = pisCommonPaymentController.getCommonPaymentById(PAYMENT_ID);

        //Then
        assertEquals(actual, expected);
    }

    @Test
    public void getConsentById_Failure() {
        //Given
        when(pisCommonPaymentService.getCommonPaymentById(WRONG_PAYMENT_ID))
            .thenReturn(CmsResponse.<PisCommonPaymentResponse>builder().error(CmsError.TECHNICAL_ERROR).build());
        ResponseEntity<PisCommonPaymentResponse> expected = new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        //When
        ResponseEntity<PisCommonPaymentResponse> actual = pisCommonPaymentController.getCommonPaymentById(WRONG_PAYMENT_ID);

        //Then
        assertEquals(actual, expected);
    }

    @Test
    public void updateConsentStatus_Success() {
        //Given
        ResponseEntity<Void> expected = new ResponseEntity<>(HttpStatus.OK);

        //When
        ResponseEntity<Void> actual = pisCommonPaymentController.updateCommonPaymentStatus(PAYMENT_ID, STATUS_RECEIVED);

        //Then
        assertEquals(actual, expected);
    }

    @Test
    public void updateConsentStatus_Failure() {
        //Given
        when(pisCommonPaymentService.updateCommonPaymentStatusById(WRONG_PAYMENT_ID, TransactionStatus.RCVD))
            .thenReturn(CmsResponse.<Boolean>builder().payload(false).build());
        ResponseEntity<Void> expected = new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        //Then
        ResponseEntity<Void> actual = pisCommonPaymentController.updateCommonPaymentStatus(WRONG_PAYMENT_ID, STATUS_RECEIVED);

        //Then
        assertEquals(actual, expected);
    }

    @Test
    public void createConsentAuthorization_Success() {
        //Given
        ResponseEntity<CreatePisAuthorisationResponse> expected = new ResponseEntity<>(new CreatePisAuthorisationResponse(AUTHORISATION_ID, SCA_STATUS, null, null, null), HttpStatus.CREATED);

        //When
        ResponseEntity<CreatePisAuthorisationResponse> actual = pisCommonPaymentController.createAuthorization(PAYMENT_ID, CREATE_PIS_AUTHORISATION_REQUEST);

        //Then
        assertEquals(actual, expected);
    }

    @Test
    public void createConsentAuthorization_Failure() {
        //Given
        when(pisAuthorisationServiceEncrypted.createAuthorization(WRONG_PAYMENT_ID, CREATE_PIS_AUTHORISATION_REQUEST))
            .thenReturn(CmsResponse.<CreatePisAuthorisationResponse>builder().error(CmsError.TECHNICAL_ERROR).build());
        ResponseEntity<CreatePisAuthorisationResponse> expected = new ResponseEntity<>(HttpStatus.NOT_FOUND);

        //When
        ResponseEntity<CreatePisAuthorisationResponse> actual = pisCommonPaymentController.createAuthorization(WRONG_PAYMENT_ID, CREATE_PIS_AUTHORISATION_REQUEST);

        //Then
        assertEquals(actual, expected);
    }

    @Test
    public void updateConsentAuthorization_Success() {
        //Given
        ResponseEntity<UpdatePisCommonPaymentPsuDataResponse> expected =
            new ResponseEntity<>(new UpdatePisCommonPaymentPsuDataResponse(ScaStatus.RECEIVED), HttpStatus.OK);

        //When
        ResponseEntity<UpdatePisCommonPaymentPsuDataResponse> actual = pisCommonPaymentController.updateAuthorization(AUTHORISATION_ID, getUpdatePisCommonPaymentPsuDataRequest());

        //Then
        assertEquals(actual, expected);
    }

    @Test
    public void updateConsentAuthorization_Failure() {
        //Given
        when(pisAuthorisationServiceEncrypted.updatePisAuthorisation(WRONG_AUTHORISATION_ID, getUpdatePisCommonPaymentPsuDataRequest()))
            .thenReturn(CmsResponse.<UpdatePisCommonPaymentPsuDataResponse>builder().error(CmsError.TECHNICAL_ERROR).build());
        ResponseEntity<UpdatePisCommonPaymentPsuDataResponse> expected = new ResponseEntity<>(HttpStatus.NOT_FOUND);

        //When
        ResponseEntity<UpdatePisCommonPaymentPsuDataResponse> actual = pisCommonPaymentController.updateAuthorization(WRONG_AUTHORISATION_ID, getUpdatePisCommonPaymentPsuDataRequest());

        //Then
        assertEquals(actual, expected);
    }

    @Test
    public void getConsentAuthorization_Success() {
        GetPisAuthorisationResponse response = getGetPisAuthorisationResponse();
        when(pisAuthorisationServiceEncrypted.getPisAuthorisationById(any()))
            .thenReturn(CmsResponse.<GetPisAuthorisationResponse>builder().payload(response).build());

        // Given
        GetPisAuthorisationResponse expectedResponse = getGetPisAuthorisationResponse();

        // When
        ResponseEntity<GetPisAuthorisationResponse> result =
            pisCommonPaymentController.getAuthorization(AUTHORISATION_ID);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(expectedResponse);
    }

    @Test
    public void getAuthorization_Failure() {
        when(pisAuthorisationServiceEncrypted.getPisAuthorisationById(any()))
            .thenReturn(CmsResponse.<GetPisAuthorisationResponse>builder().error(CmsError.TECHNICAL_ERROR).build());

        // When
        ResponseEntity<GetPisAuthorisationResponse> result =
            pisCommonPaymentController.getAuthorization(AUTHORISATION_ID);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getBody()).isNull();
    }

    @Test
    public void getAuthorisationScaStatus_success() {
        when(pisAuthorisationServiceEncrypted.getAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID, PaymentAuthorisationType.CREATED))
            .thenReturn(CmsResponse.<ScaStatus>builder().payload(SCA_STATUS).build());

        // When
        ResponseEntity<ScaStatus> result = pisCommonPaymentController.getAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(SCA_STATUS);
    }

    @Test
    public void getAuthorisationScaStatus_failure_wrongIds() {
        when(pisAuthorisationServiceEncrypted.getAuthorisationScaStatus(WRONG_PAYMENT_ID, WRONG_AUTHORISATION_ID, PaymentAuthorisationType.CREATED))
            .thenReturn(CmsResponse.<ScaStatus>builder().error(CmsError.TECHNICAL_ERROR).build());

        // When
        ResponseEntity<ScaStatus> result = pisCommonPaymentController.getAuthorisationScaStatus(WRONG_PAYMENT_ID, WRONG_AUTHORISATION_ID);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getBody()).isNull();
    }

    @Test
    public void getCancellationAuthorisationScaStatus_success() {
        when(pisAuthorisationServiceEncrypted.getAuthorisationScaStatus(PAYMENT_ID, CANCELLATION_AUTHORISATION_ID, PaymentAuthorisationType.CANCELLED))
            .thenReturn(CmsResponse.<ScaStatus>builder().payload(SCA_STATUS).build());

        // When
        ResponseEntity<ScaStatus> result = pisCommonPaymentController.getCancellationAuthorisationScaStatus(PAYMENT_ID, CANCELLATION_AUTHORISATION_ID);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(SCA_STATUS);
    }

    @Test
    public void getCancellationAuthorisationScaStatus_failure_wrongIds() {
        when(pisAuthorisationServiceEncrypted.getAuthorisationScaStatus(WRONG_PAYMENT_ID, WRONG_CANCELLATION_AUTHORISATION_ID, PaymentAuthorisationType.CANCELLED))
            .thenReturn(CmsResponse.<ScaStatus>builder().error(CmsError.TECHNICAL_ERROR).build());

        // When
        ResponseEntity<ScaStatus> result = pisCommonPaymentController.getCancellationAuthorisationScaStatus(WRONG_PAYMENT_ID, WRONG_CANCELLATION_AUTHORISATION_ID);

        // Then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getBody()).isNull();
    }

    @Test
    public void getAuthorisationScaApproach_success() {
        when(pisAuthorisationServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID, PaymentAuthorisationType.CREATED))
            .thenReturn(CmsResponse.<AuthorisationScaApproachResponse>builder().payload(new AuthorisationScaApproachResponse(ScaApproach.EMBEDDED)).build());

        ResponseEntity<AuthorisationScaApproachResponse> response = pisCommonPaymentController.getAuthorisationScaApproach(AUTHORISATION_ID);

        verify(pisAuthorisationServiceEncrypted, times(1)).getAuthorisationScaApproach(eq(AUTHORISATION_ID), eq(PaymentAuthorisationType.CREATED));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getScaApproach()).isEqualTo(ScaApproach.EMBEDDED);
    }

    @Test
    public void getAuthorisationScaApproach_error() {
        when(pisAuthorisationServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID, PaymentAuthorisationType.CREATED))
            .thenReturn(CmsResponse.<AuthorisationScaApproachResponse>builder().error(CmsError.TECHNICAL_ERROR).build());

        ResponseEntity<AuthorisationScaApproachResponse> response = pisCommonPaymentController.getAuthorisationScaApproach(AUTHORISATION_ID);

        verify(pisAuthorisationServiceEncrypted, times(1)).getAuthorisationScaApproach(eq(AUTHORISATION_ID), eq(PaymentAuthorisationType.CREATED));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }

    @Test
    public void getCancellationAuthorisationScaApproach_success() {
        when(pisAuthorisationServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID, PaymentAuthorisationType.CANCELLED))
            .thenReturn(CmsResponse.<AuthorisationScaApproachResponse>builder().payload(new AuthorisationScaApproachResponse(ScaApproach.EMBEDDED)).build());

        ResponseEntity<AuthorisationScaApproachResponse> response = pisCommonPaymentController.getCancellationAuthorisationScaApproach(AUTHORISATION_ID);

        verify(pisAuthorisationServiceEncrypted, times(1)).getAuthorisationScaApproach(eq(AUTHORISATION_ID), eq(PaymentAuthorisationType.CANCELLED));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getScaApproach()).isEqualTo(ScaApproach.EMBEDDED);
    }

    @Test
    public void getCancellationAuthorisationScaApproach_error() {
        when(pisAuthorisationServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID, PaymentAuthorisationType.CANCELLED))
            .thenReturn(CmsResponse.<AuthorisationScaApproachResponse>builder().error(CmsError.TECHNICAL_ERROR).build());

        ResponseEntity<AuthorisationScaApproachResponse> response = pisCommonPaymentController.getCancellationAuthorisationScaApproach(AUTHORISATION_ID);

        verify(pisAuthorisationServiceEncrypted, times(1)).getAuthorisationScaApproach(eq(AUTHORISATION_ID), eq(PaymentAuthorisationType.CANCELLED));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }

    @Test
    public void updateMultilevelScaRequired_Ok() {
        when(pisCommonPaymentService.updateMultilevelSca(PAYMENT_ID, true))
            .thenReturn(CmsResponse.<Boolean>builder().payload(true).build());

        ResponseEntity<Boolean> actualResponse = pisCommonPaymentController.updateMultilevelScaRequired(PAYMENT_ID, true);

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actualResponse.getBody()).isEqualTo(true);
    }

    @Test
    public void updateMultilevelScaRequired_NotFound() {
        when(pisCommonPaymentService.updateMultilevelSca(PAYMENT_ID, true))
            .thenReturn(CmsResponse.<Boolean>builder().payload(false).build());

        ResponseEntity<Boolean> actualResponse = pisCommonPaymentController.updateMultilevelScaRequired(PAYMENT_ID, true);

        assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(actualResponse.getBody()).isNull();
    }

    private GetPisAuthorisationResponse getGetPisAuthorisationResponse() {
        GetPisAuthorisationResponse response = new GetPisAuthorisationResponse();
        response.setPsuIdData(new PsuIdData(PSU_ID, null, null, null));
        response.setScaStatus(ScaStatus.RECEIVED);
        response.setPaymentId(PAYMENT_ID);
        response.setPassword(PASSWORD);
        response.setPayments(Collections.emptyList());
        response.setPaymentType(PaymentType.SINGLE);
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

    private CreatePisAuthorisationResponse getCreatePisAuthorisationResponse() {
        return new CreatePisAuthorisationResponse(AUTHORISATION_ID, SCA_STATUS, null, null, null);
    }

    private UpdatePisCommonPaymentPsuDataRequest getUpdatePisCommonPaymentPsuDataRequest() {
        UpdatePisCommonPaymentPsuDataRequest request = new UpdatePisCommonPaymentPsuDataRequest();
        request.setPaymentId(PAYMENT_ID);
        return request;
    }

    private UpdatePisCommonPaymentPsuDataResponse getUpdatePisCommonPaymentPsuDataResponse() {
        return new UpdatePisCommonPaymentPsuDataResponse(ScaStatus.RECEIVED);
    }
}
