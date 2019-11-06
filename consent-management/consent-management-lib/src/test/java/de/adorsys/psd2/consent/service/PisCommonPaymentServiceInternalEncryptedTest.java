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
import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.*;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentRequest;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentService;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.pis.PaymentAuthorisationType;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
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
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PisCommonPaymentServiceInternalEncryptedTest {
    private static final TransactionStatus TRANSACTION_STATUS = TransactionStatus.RCVD;
    private static final ScaStatus SCA_STATUS = ScaStatus.SCAMETHODSELECTED;
    private static final String ENCRYPTED_PAYMENT_ID = "encrypted payment id";
    private static final String DECRYPTED_PAYMENT_ID = "1856e4fa-8af8-427b-85ec-4caf515ce074";
    private static final PaymentAuthorisationType AUTHORISATION_TYPE = PaymentAuthorisationType.CREATED;
    private static final String AUTHORISATION_ID = "46f2e3a7-1855-4815-8755-5ca76769a1a4";
    private static final PsuIdData PSU_DATA = new PsuIdData(null, null, null, null);
    private static final String TPP_REDIRECT_URI = "request/redirect_uri";
    private static final String TPP_NOK_REDIRECT_URI = "request/nok_redirect_uri";
    private static final TppRedirectUri TPP_REDIRECT_URIs = new TppRedirectUri(TPP_REDIRECT_URI, TPP_NOK_REDIRECT_URI);
    private static final CreatePisAuthorisationRequest CREATE_PIS_AUTHORISATION_REQUEST = new CreatePisAuthorisationRequest(PaymentAuthorisationType.CREATED, PSU_DATA, ScaApproach.REDIRECT, TPP_REDIRECT_URIs);
    private static final String INTERNAL_REQUEST_ID = "5c2d5564-367f-4e03-a621-6bef76fa4208";
    private static final String CANCELLATION_INTERNAL_REQUEST_ID = "5b8d8b12-9363-4d9e-9b7e-2219cbcfc311";

    @InjectMocks
    private PisCommonPaymentServiceInternalEncrypted pisCommonPaymentServiceInternalEncrypted;
    @Mock
    private PisCommonPaymentService pisCommonPaymentService;
    @Mock
    private SecurityDataService securityDataService;

    @Before
    public void setUp() {
        when(securityDataService.encryptId(DECRYPTED_PAYMENT_ID))
            .thenReturn(Optional.of(ENCRYPTED_PAYMENT_ID));
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID))
            .thenReturn(Optional.of(DECRYPTED_PAYMENT_ID));
        when(securityDataService.encryptId(DECRYPTED_PAYMENT_ID))
            .thenReturn(Optional.of(ENCRYPTED_PAYMENT_ID));
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID))
            .thenReturn(Optional.of(DECRYPTED_PAYMENT_ID));
        when(pisCommonPaymentService.createCommonPayment(buildPisPaymentInfoRequest()))
            .thenReturn(CmsResponse.<CreatePisCommonPaymentResponse>builder()
                            .payload(buildCreatePisCommonPaymentResponse(DECRYPTED_PAYMENT_ID))
                            .build());
        when(pisCommonPaymentService.getPisCommonPaymentStatusById(DECRYPTED_PAYMENT_ID))
            .thenReturn(CmsResponse.<TransactionStatus>builder()
                            .payload(TRANSACTION_STATUS)
                            .build());
        when(pisCommonPaymentService.getCommonPaymentById(DECRYPTED_PAYMENT_ID))
            .thenReturn(CmsResponse.<PisCommonPaymentResponse>builder()
                            .payload(buildPisCommonPaymentResponse(DECRYPTED_PAYMENT_ID))
                            .build());
        when(pisCommonPaymentService.updateCommonPaymentStatusById(DECRYPTED_PAYMENT_ID, TRANSACTION_STATUS))
            .thenReturn(CmsResponse.<Boolean>builder()
                            .payload(true)
                            .build());
        when(pisCommonPaymentService.createAuthorization(DECRYPTED_PAYMENT_ID, CREATE_PIS_AUTHORISATION_REQUEST))
            .thenReturn(CmsResponse.<CreatePisAuthorisationResponse>builder()
                            .payload(buildCreatePisAuthorisationResponse())
                            .build());
        when(pisCommonPaymentService.createAuthorizationCancellation(DECRYPTED_PAYMENT_ID, CREATE_PIS_AUTHORISATION_REQUEST))
            .thenReturn(CmsResponse.<CreatePisAuthorisationResponse>builder()
                            .payload(buildCreatePisAuthorisationResponse())
                            .build());
        when(pisCommonPaymentService.updatePisAuthorisation(AUTHORISATION_ID, buildUpdatePisCommonPaymentPsuDataRequest()))
            .thenReturn(CmsResponse.<UpdatePisCommonPaymentPsuDataResponse>builder()
                            .payload(buildUpdatePisCommonPaymentPsuDataResponse())
                            .build());
        when(pisCommonPaymentService.updatePisCancellationAuthorisation(AUTHORISATION_ID, buildUpdatePisCommonPaymentPsuDataRequest()))
            .thenReturn(CmsResponse.<UpdatePisCommonPaymentPsuDataResponse>builder()
                            .payload(buildUpdatePisCommonPaymentPsuDataResponse())
                            .build());
        when(pisCommonPaymentService.getPisAuthorisationById(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<GetPisAuthorisationResponse>builder()
                            .payload(buildGetPisAuthorisationResponse())
                            .build());
        when(pisCommonPaymentService.getPisCancellationAuthorisationById(AUTHORISATION_ID))
            .thenReturn(CmsResponse.<GetPisAuthorisationResponse>builder()
                            .payload(buildGetPisAuthorisationResponse())
                            .build());
        when(pisCommonPaymentService.getAuthorisationsByPaymentId(DECRYPTED_PAYMENT_ID, PaymentAuthorisationType.CREATED))
            .thenReturn(CmsResponse.<List<String>>builder()
                            .payload(buildPaymentAuthorisations())
                            .build());
        when(pisCommonPaymentService.getPsuDataListByPaymentId(DECRYPTED_PAYMENT_ID))
            .thenReturn(CmsResponse.<List<PsuIdData>>builder()
                            .payload(buildPsuIdDataList())
                            .build());
        when(pisCommonPaymentService.getAuthorisationScaStatus(DECRYPTED_PAYMENT_ID, AUTHORISATION_ID, PaymentAuthorisationType.CREATED))
            .thenReturn(CmsResponse.<ScaStatus>builder()
                            .payload(SCA_STATUS)
                            .build());
    }

    @Test
    public void createCommonPayment_success() {
        // Given
        PisPaymentInfo request = buildPisPaymentInfoRequest();
        CreatePisCommonPaymentResponse expected = buildCreatePisCommonPaymentResponse(ENCRYPTED_PAYMENT_ID);

        // When
        CmsResponse<CreatePisCommonPaymentResponse> actual = pisCommonPaymentServiceInternalEncrypted.createCommonPayment(request);

        // Then
        assertTrue(actual.isSuccessful());

        assertEquals(expected, actual.getPayload());
        verify(pisCommonPaymentService, times(1)).createCommonPayment(request);
    }

    @Test
    public void getPisCommonPaymentStatusById_success() {
        // When
        CmsResponse<TransactionStatus> actual = pisCommonPaymentServiceInternalEncrypted.getPisCommonPaymentStatusById(ENCRYPTED_PAYMENT_ID);

        // Then
        assertTrue(actual.isSuccessful());

        assertEquals(TRANSACTION_STATUS, actual.getPayload());
        verify(pisCommonPaymentService, times(1)).getPisCommonPaymentStatusById(DECRYPTED_PAYMENT_ID);
    }

    @Test
    public void getCommonPaymentById_success() {
        // Given
        PisCommonPaymentResponse expected = buildPisCommonPaymentResponse(DECRYPTED_PAYMENT_ID);

        // When
        CmsResponse<PisCommonPaymentResponse> actual = pisCommonPaymentServiceInternalEncrypted.getCommonPaymentById(ENCRYPTED_PAYMENT_ID);

        // Then
        assertTrue(actual.isSuccessful());

        assertEquals(expected, actual.getPayload());
        verify(pisCommonPaymentService, times(1)).getCommonPaymentById(DECRYPTED_PAYMENT_ID);
    }

    @Test
    public void updateCommonPaymentStatusById_success() {
        // When
        CmsResponse<Boolean> actual = pisCommonPaymentServiceInternalEncrypted.updateCommonPaymentStatusById(ENCRYPTED_PAYMENT_ID, TRANSACTION_STATUS);

        // Then
        assertTrue(actual.isSuccessful());

        assertTrue(actual.getPayload());
        verify(pisCommonPaymentService, times(1)).updateCommonPaymentStatusById(DECRYPTED_PAYMENT_ID, TRANSACTION_STATUS);
    }

    @Test
    public void getDecryptedId_success() {
        // When
        CmsResponse<String> actual = pisCommonPaymentServiceInternalEncrypted.getDecryptedId(ENCRYPTED_PAYMENT_ID);

        // Then
        assertTrue(actual.isSuccessful());

        assertEquals(DECRYPTED_PAYMENT_ID, actual.getPayload());
    }

    @Test
    public void createAuthorization_success() {
        // Given
        CreatePisAuthorisationResponse expected = buildCreatePisAuthorisationResponse();

        // When
        CmsResponse<CreatePisAuthorisationResponse> actual =
            pisCommonPaymentServiceInternalEncrypted.createAuthorization(ENCRYPTED_PAYMENT_ID, CREATE_PIS_AUTHORISATION_REQUEST);

        // Then
        assertTrue(actual.isSuccessful());

        assertEquals(expected, actual.getPayload());
        verify(pisCommonPaymentService, times(1))
            .createAuthorization(DECRYPTED_PAYMENT_ID, CREATE_PIS_AUTHORISATION_REQUEST);
    }

    @Test
    public void createAuthorizationCancellation_success() {
        // Given
        CreatePisAuthorisationResponse expected = buildCreatePisAuthorisationResponse();

        // When
        CmsResponse<CreatePisAuthorisationResponse> actual =
            pisCommonPaymentServiceInternalEncrypted.createAuthorizationCancellation(ENCRYPTED_PAYMENT_ID, CREATE_PIS_AUTHORISATION_REQUEST);

        // Then
        assertTrue(actual.isSuccessful());

        assertEquals(expected, actual.getPayload());
        verify(pisCommonPaymentService, times(1))
            .createAuthorizationCancellation(DECRYPTED_PAYMENT_ID, CREATE_PIS_AUTHORISATION_REQUEST);
    }

    @Test
    public void updatePisAuthorisation_success() {
        // Given
        UpdatePisCommonPaymentPsuDataRequest request = buildUpdatePisCommonPaymentPsuDataRequest();
        UpdatePisCommonPaymentPsuDataResponse expected = buildUpdatePisCommonPaymentPsuDataResponse();

        // When
        CmsResponse<UpdatePisCommonPaymentPsuDataResponse> actual = pisCommonPaymentServiceInternalEncrypted.updatePisAuthorisation(AUTHORISATION_ID, request);

        // Then
        assertTrue(actual.isSuccessful());

        assertEquals(expected, actual.getPayload());
        verify(pisCommonPaymentService, times(1))
            .updatePisAuthorisation(AUTHORISATION_ID, request);
    }

    @Test
    public void updatePisCancellationAuthorisation_success() {
        // Given
        UpdatePisCommonPaymentPsuDataRequest request = buildUpdatePisCommonPaymentPsuDataRequest();
        UpdatePisCommonPaymentPsuDataResponse expected = buildUpdatePisCommonPaymentPsuDataResponse();

        // When
        CmsResponse<UpdatePisCommonPaymentPsuDataResponse> actual =
            pisCommonPaymentServiceInternalEncrypted.updatePisCancellationAuthorisation(AUTHORISATION_ID, request);

        // Then
        assertTrue(actual.isSuccessful());

        assertEquals(expected, actual.getPayload());
        verify(pisCommonPaymentService, times(1))
            .updatePisCancellationAuthorisation(AUTHORISATION_ID, request);
    }

    @Test
    public void updateCommonPayment_success() {
        // Given
        PisCommonPaymentRequest request = buildPisCommonPaymentRequest();

        // When
        pisCommonPaymentServiceInternalEncrypted.updateCommonPayment(request, ENCRYPTED_PAYMENT_ID);

        // Then
        verify(pisCommonPaymentService, times(1))
            .updateCommonPayment(request, DECRYPTED_PAYMENT_ID);
    }

    @Test
    public void getPisAuthorisationById_success() {
        // Given
        GetPisAuthorisationResponse expected = buildGetPisAuthorisationResponse();

        // When
        CmsResponse<GetPisAuthorisationResponse> actual =
            pisCommonPaymentServiceInternalEncrypted.getPisAuthorisationById(AUTHORISATION_ID);

        // Then
        assertTrue(actual.isSuccessful());

        assertEquals(expected, actual.getPayload());
        verify(pisCommonPaymentService, times(1)).getPisAuthorisationById(AUTHORISATION_ID);
    }

    @Test
    public void getPisCancellationAuthorisationById_success() {
        // Given
        GetPisAuthorisationResponse expected = buildGetPisAuthorisationResponse();

        // When
        CmsResponse<GetPisAuthorisationResponse> actual =
            pisCommonPaymentServiceInternalEncrypted.getPisCancellationAuthorisationById(AUTHORISATION_ID);

        // Then
        assertTrue(actual.isSuccessful());

        assertEquals(expected, actual.getPayload());
        verify(pisCommonPaymentService, times(1))
            .getPisCancellationAuthorisationById(AUTHORISATION_ID);
    }

    @Test
    public void getAuthorisationsByPaymentId_success() {
        // Given
        List<String> expected = buildPaymentAuthorisations();

        // When
        CmsResponse<List<String>> actual = pisCommonPaymentServiceInternalEncrypted.getAuthorisationsByPaymentId(ENCRYPTED_PAYMENT_ID,
                                                                                                                 PaymentAuthorisationType.CREATED);

        // Then
        assertTrue(actual.isSuccessful());

        assertEquals(expected, actual.getPayload());
        verify(pisCommonPaymentService, times(1))
            .getAuthorisationsByPaymentId(DECRYPTED_PAYMENT_ID, PaymentAuthorisationType.CREATED);
    }

    @Test
    public void getAuthorisationScaStatus_success() {
        // When
        CmsResponse<ScaStatus> actual = pisCommonPaymentServiceInternalEncrypted.getAuthorisationScaStatus(ENCRYPTED_PAYMENT_ID, AUTHORISATION_ID, PaymentAuthorisationType.CREATED);

        // Then
        assertTrue(actual.isSuccessful());

        assertEquals(SCA_STATUS, actual.getPayload());
        verify(pisCommonPaymentService, times(1)).getAuthorisationScaStatus(DECRYPTED_PAYMENT_ID, AUTHORISATION_ID, PaymentAuthorisationType.CREATED);
    }

    @Test
    public void getPsuDataListByPaymentId_success() {
        // Given
        List<PsuIdData> expected = buildPsuIdDataList();

        // When
        CmsResponse<List<PsuIdData>> actual = pisCommonPaymentServiceInternalEncrypted.getPsuDataListByPaymentId(ENCRYPTED_PAYMENT_ID);

        // Then
        assertTrue(actual.isSuccessful());

        assertEquals(expected, actual.getPayload());
        verify(pisCommonPaymentService, times(1)).getPsuDataListByPaymentId(DECRYPTED_PAYMENT_ID);
    }

    @Test
    public void getAuthorisationScaApproach() {
        when(pisCommonPaymentService.getAuthorisationScaApproach(AUTHORISATION_ID, PaymentAuthorisationType.CREATED))
            .thenReturn(CmsResponse.<AuthorisationScaApproachResponse>builder()
                            .payload(new AuthorisationScaApproachResponse(ScaApproach.EMBEDDED))
                            .build());

        CmsResponse<AuthorisationScaApproachResponse> actual = pisCommonPaymentServiceInternalEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID, PaymentAuthorisationType.CREATED);

        assertTrue(actual.isSuccessful());

        assertEquals(ScaApproach.EMBEDDED, actual.getPayload().getScaApproach());
        verify(pisCommonPaymentService, times(1)).getAuthorisationScaApproach(eq(AUTHORISATION_ID), eq(PaymentAuthorisationType.CREATED));
    }

    @Test
    public void updateMultilevelSca_True() {
        // Given
        when(pisCommonPaymentService.updateMultilevelSca(DECRYPTED_PAYMENT_ID, true))
            .thenReturn(CmsResponse.<Boolean>builder()
                            .payload(true)
                            .build());

        // When
        CmsResponse<Boolean> actualResponse = pisCommonPaymentServiceInternalEncrypted.updateMultilevelSca(ENCRYPTED_PAYMENT_ID, true);

        // Then
        assertTrue(actualResponse.isSuccessful());

        assertTrue(actualResponse.getPayload());
        verify(pisCommonPaymentService, times(1)).updateMultilevelSca(DECRYPTED_PAYMENT_ID, true);
    }

    private PisPaymentInfo buildPisPaymentInfoRequest() {
        return new PisPaymentInfo();
    }

    private PisCommonPaymentRequest buildPisCommonPaymentRequest() {
        return new PisCommonPaymentRequest();
    }

    private CreatePisCommonPaymentResponse buildCreatePisCommonPaymentResponse(String id) {
        return new CreatePisCommonPaymentResponse(id);
    }

    private PisCommonPaymentResponse buildPisCommonPaymentResponse(String id) {
        PisCommonPaymentResponse response = new PisCommonPaymentResponse();
        response.setExternalId(id);
        return response;
    }

    private List<PsuIdData> buildPsuIdDataList() {
        return Collections.singletonList(PSU_DATA);
    }

    private CreatePisAuthorisationResponse buildCreatePisAuthorisationResponse() {
        return new CreatePisAuthorisationResponse(AUTHORISATION_ID, ScaStatus.RECEIVED, INTERNAL_REQUEST_ID, CANCELLATION_INTERNAL_REQUEST_ID);
    }

    private UpdatePisCommonPaymentPsuDataRequest buildUpdatePisCommonPaymentPsuDataRequest() {
        UpdatePisCommonPaymentPsuDataRequest request = new UpdatePisCommonPaymentPsuDataRequest();
        request.setAuthorizationId(AUTHORISATION_ID);
        return request;
    }

    private UpdatePisCommonPaymentPsuDataResponse buildUpdatePisCommonPaymentPsuDataResponse() {
        return new UpdatePisCommonPaymentPsuDataResponse(SCA_STATUS);
    }

    private GetPisAuthorisationResponse buildGetPisAuthorisationResponse() {
        return new GetPisAuthorisationResponse();
    }

    private List<String> buildPaymentAuthorisations() {
        return Collections.singletonList(AUTHORISATION_ID);
    }
}

