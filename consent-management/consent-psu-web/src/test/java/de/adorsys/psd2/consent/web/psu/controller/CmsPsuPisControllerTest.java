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

package de.adorsys.psd2.consent.web.psu.controller;

import de.adorsys.psd2.consent.api.pis.CmsPayment;
import de.adorsys.psd2.consent.api.pis.CmsPaymentResponse;
import de.adorsys.psd2.consent.api.pis.CmsSinglePayment;
import de.adorsys.psd2.consent.psu.api.CmsPsuPisService;
import de.adorsys.psd2.consent.psu.api.pis.CmsPisPsuDataAuthorisation;
import de.adorsys.psd2.xs2a.core.exception.AuthorisationIsExpiredException;
import de.adorsys.psd2.xs2a.core.exception.RedirectUrlIsExpiredException;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthenticationDataHolder;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CmsPsuPisControllerTest {

    private static final String PAYMENT_ID = "payment id";
    private static final String AUTHORISATION_ID = "authorisation id";
    private static final String PSU_ID = "psu id";
    private static final String PSU_ID_TYPE = "psu id type";
    private static final String PSU_CORPORATE_ID = "psu corporate id";
    private static final String PSU_CORPORATE_ID_TYPE = "psu corporate id type";
    private static final String PSU_IP_ADDRESS = "psu ip address";
    private static final String INSTANCE_ID = "instance id";
    private static final String SCA_STATUS_RECEIVED = "RECEIVED";
    private static final String TPP_NOK_REDIRECT_URI = "tpp nok redirect uri";
    private static final String REDIRECT_ID = "redirect_id";
    private final PsuIdData PSU_ID_DATA = buildPsuIdData();
    private static final String METHOD_ID = "SMS";
    private static final String AUTHENTICATION_DATA = "123456";

    private AuthenticationDataHolder authenticationDataHolder;

    @InjectMocks
    private CmsPsuPisController cmsPsuPisController;

    @Mock
    private CmsPsuPisService cmsPsuPisService;

    @Before
    public void init() {
        authenticationDataHolder = new AuthenticationDataHolder(METHOD_ID, AUTHENTICATION_DATA);
    }

    @Test
    public void updateAuthorisationStatus_withValidRequest_shouldReturnOk() throws AuthorisationIsExpiredException {
        // Given
        PsuIdData psuIdData = new PsuIdData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, null);
        when(cmsPsuPisService.updateAuthorisationStatus(psuIdData, PAYMENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID, authenticationDataHolder))
            .thenReturn(true);

        // When
        ResponseEntity<Void> actualResponse = cmsPsuPisController.updateAuthorisationStatus(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, PAYMENT_ID, AUTHORISATION_ID, SCA_STATUS_RECEIVED, INSTANCE_ID, authenticationDataHolder);

        // Then
        verify(cmsPsuPisService).updateAuthorisationStatus(psuIdData, PAYMENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID, authenticationDataHolder);

        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
    }

    @Test
    public void updateAuthorisationStatus_withValidRequestAndLowercaseScaStatus_shouldReturnOk() throws AuthorisationIsExpiredException {
        // Given
        PsuIdData psuIdData = new PsuIdData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, null);
        when(cmsPsuPisService.updateAuthorisationStatus(psuIdData, PAYMENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID, authenticationDataHolder))
            .thenReturn(true);
        String lowercaseScaStatus = SCA_STATUS_RECEIVED.toLowerCase();

        // When
        ResponseEntity<Void> actualResponse = cmsPsuPisController.updateAuthorisationStatus(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, PAYMENT_ID, AUTHORISATION_ID, lowercaseScaStatus, INSTANCE_ID, authenticationDataHolder);

        // Then
        verify(cmsPsuPisService).updateAuthorisationStatus(psuIdData, PAYMENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID, authenticationDataHolder);

        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
    }

    @Test
    public void updateAuthorisationStatus_withFalseFromService_shouldReturnBadRequest() throws AuthorisationIsExpiredException {
        // Given
        PsuIdData psuIdData = new PsuIdData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, null);
        when(cmsPsuPisService.updateAuthorisationStatus(psuIdData, PAYMENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID, authenticationDataHolder))
            .thenReturn(false);

        // When
        ResponseEntity<Void> actualResponse = cmsPsuPisController.updateAuthorisationStatus(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, PAYMENT_ID, AUTHORISATION_ID, SCA_STATUS_RECEIVED, INSTANCE_ID, authenticationDataHolder);

        // Then
        verify(cmsPsuPisService).updateAuthorisationStatus(psuIdData, PAYMENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID, authenticationDataHolder);

        assertEquals(HttpStatus.BAD_REQUEST, actualResponse.getStatusCode());
    }

    @Test
    public void updateAuthorisationStatus_AuthorisationIsExpired_requestTimeout() throws AuthorisationIsExpiredException {
        // Given
        PsuIdData psuIdData = new PsuIdData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, null);
        when(cmsPsuPisService.updateAuthorisationStatus(psuIdData, PAYMENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID, authenticationDataHolder))
            .thenThrow(new AuthorisationIsExpiredException(TPP_NOK_REDIRECT_URI));

        // When
        ResponseEntity<CmsPaymentResponse> actualResponse = cmsPsuPisController.updateAuthorisationStatus(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, PAYMENT_ID, AUTHORISATION_ID, SCA_STATUS_RECEIVED, INSTANCE_ID, authenticationDataHolder);

        // Then
        verify(cmsPsuPisService, times(1)).updateAuthorisationStatus(psuIdData, PAYMENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED, INSTANCE_ID, authenticationDataHolder);
        assertEquals(HttpStatus.REQUEST_TIMEOUT, actualResponse.getStatusCode());
        assertEquals(TPP_NOK_REDIRECT_URI, actualResponse.getBody().getTppNokRedirectUri());
    }

    @Test
    public void updateAuthorisationStatus_withInvalidScaStatus_shouldReturnBadRequest() throws AuthorisationIsExpiredException {
        // Given
        String invalidScaStatus = "invalid SCA status";

        // When
        ResponseEntity<Void> actualResponse = cmsPsuPisController.updateAuthorisationStatus(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, PAYMENT_ID, AUTHORISATION_ID, invalidScaStatus, INSTANCE_ID, authenticationDataHolder);

        // Then
        verify(cmsPsuPisService, never()).updateAuthorisationStatus(any(), anyString(), anyString(), any(), anyString(), any());

        assertEquals(HttpStatus.BAD_REQUEST, actualResponse.getStatusCode());
        assertNull(actualResponse.getBody());
    }

    @Test
    public void updatePsuInPayment_success() throws AuthorisationIsExpiredException {
        when(cmsPsuPisService.updatePsuInPayment(PSU_ID_DATA, AUTHORISATION_ID, INSTANCE_ID)).thenReturn(true);

        // When
        ResponseEntity<Void> actualResponse = cmsPsuPisController.updatePsuInPayment(AUTHORISATION_ID, INSTANCE_ID, PSU_ID_DATA);
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
    }

    @Test
    public void updatePsuInPayment_shouldReturnBadRequest() throws AuthorisationIsExpiredException {
        when(cmsPsuPisService.updatePsuInPayment(PSU_ID_DATA, AUTHORISATION_ID, INSTANCE_ID)).thenReturn(false);

        // When
        ResponseEntity<Void> actualResponse = cmsPsuPisController.updatePsuInPayment(AUTHORISATION_ID, INSTANCE_ID, PSU_ID_DATA);
        assertEquals(HttpStatus.BAD_REQUEST, actualResponse.getStatusCode());
    }

    @Test
    public void updatePsuInPayment_requestTimeout() throws AuthorisationIsExpiredException {
        when(cmsPsuPisService.updatePsuInPayment(PSU_ID_DATA, AUTHORISATION_ID, INSTANCE_ID)).thenThrow(new AuthorisationIsExpiredException(TPP_NOK_REDIRECT_URI));

        // When
        ResponseEntity<CmsPaymentResponse> actualResponse = cmsPsuPisController.updatePsuInPayment(AUTHORISATION_ID, INSTANCE_ID, PSU_ID_DATA);
        assertEquals(HttpStatus.REQUEST_TIMEOUT, actualResponse.getStatusCode());
        assertEquals(TPP_NOK_REDIRECT_URI, actualResponse.getBody().getTppNokRedirectUri());
    }

    @Test
    public void getPaymentByPaymentId_success() {
        ArgumentCaptor<PsuIdData> psuIdDataCaptor = ArgumentCaptor.forClass(PsuIdData.class);
        when(cmsPsuPisService.getPayment(psuIdDataCaptor.capture(), eq(PAYMENT_ID), eq(INSTANCE_ID))).thenReturn(Optional.of(new CmsSinglePayment("payment_product")));

        // When
        ResponseEntity<CmsPayment> actualResponse = cmsPsuPisController.getPaymentByPaymentId(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, PAYMENT_ID, INSTANCE_ID);
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());

        assertEquals(PSU_ID, psuIdDataCaptor.getValue().getPsuId());
        assertEquals(PSU_ID_TYPE, psuIdDataCaptor.getValue().getPsuIdType());
        assertEquals(PSU_CORPORATE_ID, psuIdDataCaptor.getValue().getPsuCorporateId());
        assertEquals(PSU_CORPORATE_ID_TYPE, psuIdDataCaptor.getValue().getPsuCorporateIdType());
    }

    @Test
    public void getPaymentIdByRedirectId_success() throws RedirectUrlIsExpiredException {
        CmsPaymentResponse cmsPaymentResponse = new CmsPaymentResponse();
        CmsSinglePayment payment = new CmsSinglePayment("payment product");
        payment.setPaymentId(PAYMENT_ID);
        cmsPaymentResponse.setPayment(payment);
        when(cmsPsuPisService.checkRedirectAndGetPayment(REDIRECT_ID, INSTANCE_ID)).thenReturn(Optional.of(cmsPaymentResponse));

        ResponseEntity<CmsPaymentResponse> actualResponse = cmsPsuPisController.getPaymentIdByRedirectId(REDIRECT_ID, INSTANCE_ID);
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(PAYMENT_ID, actualResponse.getBody().getPayment().getPaymentId());
    }

    @Test
    public void getPaymentIdByRedirectId_shouldReturnNotFound() throws RedirectUrlIsExpiredException {
        when(cmsPsuPisService.checkRedirectAndGetPayment(REDIRECT_ID, INSTANCE_ID)).thenReturn(Optional.empty());

        ResponseEntity<CmsPaymentResponse> actualResponse = cmsPsuPisController.getPaymentIdByRedirectId(REDIRECT_ID, INSTANCE_ID);
        assertEquals(HttpStatus.NOT_FOUND, actualResponse.getStatusCode());
    }

    @Test
    public void getPaymentIdByRedirectId_RedirectUriIsExpired_requestTimeout() throws RedirectUrlIsExpiredException {
        when(cmsPsuPisService.checkRedirectAndGetPayment(REDIRECT_ID, INSTANCE_ID)).thenThrow(new RedirectUrlIsExpiredException(TPP_NOK_REDIRECT_URI));

        ResponseEntity<CmsPaymentResponse> actualResponse = cmsPsuPisController.getPaymentIdByRedirectId(REDIRECT_ID, INSTANCE_ID);
        assertEquals(HttpStatus.REQUEST_TIMEOUT, actualResponse.getStatusCode());
        assertEquals(TPP_NOK_REDIRECT_URI, actualResponse.getBody().getTppNokRedirectUri());
    }

    @Test
    public void getPaymentIdByRedirectIdForCancellation_success() throws RedirectUrlIsExpiredException {
        CmsPaymentResponse cmsPaymentResponse = new CmsPaymentResponse();
        CmsSinglePayment payment = new CmsSinglePayment("payment product");
        payment.setPaymentId(PAYMENT_ID);
        cmsPaymentResponse.setPayment(payment);
        when(cmsPsuPisService.checkRedirectAndGetPaymentForCancellation(REDIRECT_ID, INSTANCE_ID)).thenReturn(Optional.of(cmsPaymentResponse));

        ResponseEntity<CmsPaymentResponse> actualResponse = cmsPsuPisController.getPaymentIdByRedirectIdForCancellation(REDIRECT_ID, INSTANCE_ID);
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(PAYMENT_ID, actualResponse.getBody().getPayment().getPaymentId());
    }

    @Test
    public void getPaymentIdByRedirectIdForCancellation_shouldReturnNotFound() throws RedirectUrlIsExpiredException {
        when(cmsPsuPisService.checkRedirectAndGetPaymentForCancellation(REDIRECT_ID, INSTANCE_ID)).thenReturn(Optional.empty());

        ResponseEntity<CmsPaymentResponse> actualResponse = cmsPsuPisController.getPaymentIdByRedirectIdForCancellation(REDIRECT_ID, INSTANCE_ID);
        assertEquals(HttpStatus.NOT_FOUND, actualResponse.getStatusCode());
    }

    @Test
    public void getPaymentIdByRedirectIdForCancellation_RedirectUriIsExpired_requestTimeout() throws RedirectUrlIsExpiredException {
        when(cmsPsuPisService.checkRedirectAndGetPaymentForCancellation(REDIRECT_ID, INSTANCE_ID)).thenThrow(new RedirectUrlIsExpiredException(TPP_NOK_REDIRECT_URI));

        ResponseEntity<CmsPaymentResponse> actualResponse = cmsPsuPisController.getPaymentIdByRedirectIdForCancellation(REDIRECT_ID, INSTANCE_ID);
        assertEquals(HttpStatus.REQUEST_TIMEOUT, actualResponse.getStatusCode());
        assertEquals(TPP_NOK_REDIRECT_URI, actualResponse.getBody().getTppNokRedirectUri());
    }

    @Test
    public void updatePaymentStatus_success() {
        when(cmsPsuPisService.updatePaymentStatus(PAYMENT_ID, TransactionStatus.ACCP, INSTANCE_ID)).thenReturn(true);

        ResponseEntity<Void> actualResponse = cmsPsuPisController.updatePaymentStatus(PAYMENT_ID, TransactionStatus.ACCP.name(), INSTANCE_ID);

        verify(cmsPsuPisService, times(1)).updatePaymentStatus(anyString(), any(TransactionStatus.class), anyString());
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
    }

    @Test
    public void updatePaymentStatus_badRequest() {
        when(cmsPsuPisService.updatePaymentStatus(PAYMENT_ID, TransactionStatus.ACCP, INSTANCE_ID)).thenReturn(false);

        ResponseEntity<Void> actualResponse = cmsPsuPisController.updatePaymentStatus(PAYMENT_ID, TransactionStatus.ACCP.name(), INSTANCE_ID);

        verify(cmsPsuPisService, times(1)).updatePaymentStatus(anyString(), any(TransactionStatus.class), anyString());
        assertEquals(HttpStatus.BAD_REQUEST, actualResponse.getStatusCode());
    }

    @Test
    public void psuAuthorisationStatuses_success() {
        when(cmsPsuPisService.getPsuDataAuthorisations(PAYMENT_ID, INSTANCE_ID)).thenReturn(Optional.of(new ArrayList<>()));

        ResponseEntity<List<CmsPisPsuDataAuthorisation>> actualResponse = cmsPsuPisController.psuAuthorisationStatuses(PAYMENT_ID, INSTANCE_ID);

        verify(cmsPsuPisService, times(1)).getPsuDataAuthorisations(anyString(), anyString());
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
    }

    @Test
    public void psuAuthorisationStatuses_notFound() {
        when(cmsPsuPisService.getPsuDataAuthorisations(PAYMENT_ID, INSTANCE_ID)).thenReturn(Optional.empty());

        ResponseEntity<List<CmsPisPsuDataAuthorisation>> actualResponse = cmsPsuPisController.psuAuthorisationStatuses(PAYMENT_ID, INSTANCE_ID);

        verify(cmsPsuPisService, times(1)).getPsuDataAuthorisations(anyString(), anyString());
        assertEquals(HttpStatus.NOT_FOUND, actualResponse.getStatusCode());
    }

    private PsuIdData buildPsuIdData() {
        return new PsuIdData(
            "psuId",
            "psuIdType",
            "psuCorporateId",
            "psuCorporateIdType",
            "psuIpAddress"
        );
    }
}
