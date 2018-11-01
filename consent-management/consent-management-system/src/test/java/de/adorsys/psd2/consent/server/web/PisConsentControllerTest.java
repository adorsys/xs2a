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

package de.adorsys.psd2.consent.server.web;

import de.adorsys.psd2.consent.api.CmsAuthorisationType;
import de.adorsys.psd2.consent.api.pis.PisConsentStatusResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.CreatePisConsentAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.GetPisConsentAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisConsentPsuDataRequest;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisConsentPsuDataResponse;
import de.adorsys.psd2.consent.api.pis.proto.CreatePisConsentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisConsentRequest;
import de.adorsys.psd2.consent.api.pis.proto.PisConsentResponse;
import de.adorsys.psd2.consent.server.service.PisConsentService;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.RECEIVED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PisConsentControllerTest {

    private static final String AUTHORISATION_ID = "345-9245-2359";
    private static final String PAYMENT_ID = "33333-999999999";
    private static final String CONSENT_ID = "12345";
    private static final String STATUS_RECEIVED = "RECEIVED";
    private static final String PSU_ID = "testPSU";
    private static final String PASSWORD = "password";

    private static final String WRONG_AUTHORISATION_ID = "3254890-5";
    private static final String WRONG_PAYMENT_ID = "32343-999997777";
    private static final String WRONG_CONSENT_ID = "67890";

    private static final PsuIdData PSU_DATA = new PsuIdData(PSU_ID, null, null, null);

    @InjectMocks
    private PisConsentController pisConsentController;

    @Mock
    private PisConsentService pisConsentService;

    @Before
    public void setUp() {
        when(pisConsentService.createPaymentConsent(getPisConsentRequest())).thenReturn(Optional.of(getCreatePisConsentResponse()));
        when(pisConsentService.getConsentStatusById(CONSENT_ID)).thenReturn(Optional.of(RECEIVED));
        when(pisConsentService.getConsentById(CONSENT_ID)).thenReturn(Optional.of(getPisConsentResponse()));
        when(pisConsentService.updateConsentStatusById(CONSENT_ID, RECEIVED)).thenReturn(Optional.of(Boolean.TRUE));
        when(pisConsentService.createAuthorization(PAYMENT_ID, CmsAuthorisationType.CREATED, PSU_DATA)).thenReturn(Optional.of(getCreatePisConsentAuthorisationResponse()));
        when(pisConsentService.updateConsentAuthorization(AUTHORISATION_ID, getUpdatePisConsentPsuDataRequest(), CmsAuthorisationType.CREATED)).thenReturn(Optional.of(getUpdatePisConsentPsuDataResponse()));
    }

    @Test
    public void createPaymentConsent_Success() {
        //Given
        ResponseEntity<CreatePisConsentResponse> expected = new ResponseEntity<>(new CreatePisConsentResponse(CONSENT_ID), HttpStatus.CREATED);

        //When
        ResponseEntity<CreatePisConsentResponse> actual = pisConsentController.createPaymentConsent(getPisConsentRequest());

        //Then
        assertEquals(actual, expected);
    }

    @Test
    public void createPaymentConsent_Failure() {
        //Given
        when(pisConsentService.createPaymentConsent(getPisConsentRequest())).thenReturn(Optional.empty());
        ResponseEntity<CreatePisConsentResponse> expected = new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        //When
        ResponseEntity<CreatePisConsentResponse> actual = pisConsentController.createPaymentConsent(getPisConsentRequest());

        //Then
        assertEquals(actual, expected);
    }

    @Test
    public void getConsentStatusById_Success() {
        //Given
        ResponseEntity<PisConsentStatusResponse> expected = new ResponseEntity<>(new PisConsentStatusResponse(RECEIVED), HttpStatus.OK);

        //When
        ResponseEntity<PisConsentStatusResponse> actual = pisConsentController.getConsentStatusById(CONSENT_ID);

        //Then
        assertEquals(actual, expected);
    }

    @Test
    public void getConsentStatusById_Failure() {
        //Given
        when(pisConsentService.getConsentStatusById(WRONG_CONSENT_ID)).thenReturn(Optional.empty());
        ResponseEntity<PisConsentStatusResponse> expected = new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        //When
        ResponseEntity<PisConsentStatusResponse> actual = pisConsentController.getConsentStatusById(WRONG_CONSENT_ID);

        //Then
        assertEquals(actual, expected);
    }

    @Test
    public void getConsentById_Success() {
        //Given
        ResponseEntity<PisConsentResponse> expected = new ResponseEntity<>(new PisConsentResponse(), HttpStatus.OK);

        //When
        ResponseEntity<PisConsentResponse> actual = pisConsentController.getConsentById(CONSENT_ID);

        //Then
        assertEquals(actual, expected);
    }

    @Test
    public void getConsentById_Failure() {
        //Given
        when(pisConsentService.getConsentById(WRONG_CONSENT_ID)).thenReturn(Optional.empty());
        ResponseEntity<PisConsentResponse> expected = new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        //When
        ResponseEntity<PisConsentResponse> actual = pisConsentController.getConsentById(WRONG_CONSENT_ID);

        //Then
        assertEquals(actual, expected);
    }

    @Test
    public void updateConsentStatus_Success() {
        //Given
        ResponseEntity<Void> expected = new ResponseEntity<>(HttpStatus.OK);

        //When
        ResponseEntity<Void> actual = pisConsentController.updateConsentStatus(CONSENT_ID, STATUS_RECEIVED);

        //Then
        assertEquals(actual, expected);
    }

    @Test
    public void updateConsentStatus_Failure() {
        //Given
        when(pisConsentService.updateConsentStatusById(WRONG_CONSENT_ID, RECEIVED)).thenReturn(Optional.empty());
        ResponseEntity<Void> expected = new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        //Then
        ResponseEntity<Void> actual = pisConsentController.updateConsentStatus(WRONG_CONSENT_ID, STATUS_RECEIVED);

        //Then
        assertEquals(actual, expected);
    }

    @Test
    public void createConsentAuthorization_Success() {
        //Given
        ResponseEntity<CreatePisConsentAuthorisationResponse> expected = new ResponseEntity<>(new CreatePisConsentAuthorisationResponse(AUTHORISATION_ID), HttpStatus.CREATED);

        //When
        ResponseEntity<CreatePisConsentAuthorisationResponse> actual = pisConsentController.createConsentAuthorization(PAYMENT_ID, PSU_DATA);

        //Then
        assertEquals(actual, expected);
    }

    @Test
    public void createConsentAuthorization_Failure() {
        //Given
        when(pisConsentService.createAuthorization(WRONG_PAYMENT_ID, CmsAuthorisationType.CREATED, PSU_DATA)).thenReturn(Optional.empty());
        ResponseEntity<CreatePisConsentAuthorisationResponse> expected = new ResponseEntity<>(HttpStatus.NOT_FOUND);

        //When
        ResponseEntity<CreatePisConsentAuthorisationResponse> actual = pisConsentController.createConsentAuthorization(WRONG_PAYMENT_ID, PSU_DATA);

        //Then
        assertEquals(actual, expected);
    }

    @Test
    public void updateConsentAuthorization_Success() {
        //Given
        ResponseEntity<UpdatePisConsentPsuDataResponse> expected =
            new ResponseEntity<>(new UpdatePisConsentPsuDataResponse(ScaStatus.RECEIVED), HttpStatus.OK);

        //When
        ResponseEntity<UpdatePisConsentPsuDataResponse> actual = pisConsentController.updateConsentAuthorization(AUTHORISATION_ID, getUpdatePisConsentPsuDataRequest());

        //Then
        assertEquals(actual, expected);
    }

    @Test
    public void updateConsentAuthorization_Failure() {
        //Given
        when(pisConsentService.updateConsentAuthorization(WRONG_AUTHORISATION_ID, getUpdatePisConsentPsuDataRequest(), CmsAuthorisationType.CREATED)).thenReturn(Optional.empty());
        ResponseEntity<UpdatePisConsentPsuDataResponse> expected = new ResponseEntity<>(HttpStatus.NOT_FOUND);

        //When
        ResponseEntity<UpdatePisConsentPsuDataResponse> actual = pisConsentController.updateConsentAuthorization(WRONG_AUTHORISATION_ID, getUpdatePisConsentPsuDataRequest());

        //Then
        assertEquals(actual, expected);
    }

    @Test
    public void getConsentAuthorization_Success() {
        GetPisConsentAuthorisationResponse response = getGetPisConsentAuthorisationResponse();
        when(pisConsentService.getPisConsentAuthorizationById(any(), any(CmsAuthorisationType.class))).thenReturn(Optional.of(response));

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
        when(pisConsentService.getPisConsentAuthorizationById(any(), any(CmsAuthorisationType.class))).thenReturn(Optional.empty());

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
        response.setScaStatus(ScaStatus.STARTED);
        response.setConsentId(CONSENT_ID);
        response.setPassword(PASSWORD);
        response.setPayments(Collections.emptyList());
        response.setPaymentType(PaymentType.SINGLE);
        return response;
    }

    private PisConsentRequest getPisConsentRequest() {
        return new PisConsentRequest();
    }

    private CreatePisConsentResponse getCreatePisConsentResponse() {
        return new CreatePisConsentResponse(CONSENT_ID);
    }

    private PisConsentResponse getPisConsentResponse() {
        return new PisConsentResponse();
    }

    private CreatePisConsentAuthorisationResponse getCreatePisConsentAuthorisationResponse() {
        return new CreatePisConsentAuthorisationResponse(AUTHORISATION_ID);
    }

    private UpdatePisConsentPsuDataRequest getUpdatePisConsentPsuDataRequest() {
        UpdatePisConsentPsuDataRequest request = new UpdatePisConsentPsuDataRequest();
        request.setPaymentId(PAYMENT_ID);
        return request;
    }

    private UpdatePisConsentPsuDataResponse getUpdatePisConsentPsuDataResponse() {
        return new UpdatePisConsentPsuDataResponse(ScaStatus.RECEIVED);
    }
}
