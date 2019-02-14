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

import de.adorsys.psd2.consent.api.CmsAuthorisationType;
import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.CreatePisAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.GetPisAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentRequest;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentService;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
    private static final CmsAuthorisationType AUTHORISATION_TYPE = CmsAuthorisationType.CREATED;
    private static final String AUTHORISATION_ID = "46f2e3a7-1855-4815-8755-5ca76769a1a4";

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
            .thenReturn(Optional.of(buildCreatePisCommonPaymentResponse(DECRYPTED_PAYMENT_ID)));
        when(pisCommonPaymentService.getPisCommonPaymentStatusById(DECRYPTED_PAYMENT_ID))
            .thenReturn(Optional.of(TRANSACTION_STATUS));
        when(pisCommonPaymentService.getCommonPaymentById(DECRYPTED_PAYMENT_ID))
            .thenReturn(Optional.of(buildPisCommonPaymentResponse(DECRYPTED_PAYMENT_ID)));
        when(pisCommonPaymentService.updateCommonPaymentStatusById(DECRYPTED_PAYMENT_ID, TRANSACTION_STATUS))
            .thenReturn(Optional.of(true));
        when(pisCommonPaymentService.createAuthorization(DECRYPTED_PAYMENT_ID, AUTHORISATION_TYPE, buildPsuIdData()))
            .thenReturn(Optional.of(buildCreatePisAuthorisationResponse()));
        when(pisCommonPaymentService.createAuthorizationCancellation(DECRYPTED_PAYMENT_ID, AUTHORISATION_TYPE, buildPsuIdData()))
            .thenReturn(Optional.of(buildCreatePisAuthorisationResponse()));
        when(pisCommonPaymentService.updatePisAuthorisation(AUTHORISATION_ID, buildUpdatePisCommonPaymentPsuDataRequest()))
            .thenReturn(Optional.of(buildUpdatePisCommonPaymentPsuDataResponse()));
        when(pisCommonPaymentService.updatePisCancellationAuthorisation(AUTHORISATION_ID, buildUpdatePisCommonPaymentPsuDataRequest()))
            .thenReturn(Optional.of(buildUpdatePisCommonPaymentPsuDataResponse()));
        when(pisCommonPaymentService.getPisAuthorisationById(AUTHORISATION_ID))
            .thenReturn(Optional.of(buildGetPisAuthorisationResponse()));
        when(pisCommonPaymentService.getPisCancellationAuthorisationById(AUTHORISATION_ID))
            .thenReturn(Optional.of(buildGetPisAuthorisationResponse()));
        when(pisCommonPaymentService.getAuthorisationsByPaymentId(DECRYPTED_PAYMENT_ID, CmsAuthorisationType.CREATED))
            .thenReturn(Optional.of(buildPaymentAuthorisations()));
        when(pisCommonPaymentService.getPsuDataListByPaymentId(DECRYPTED_PAYMENT_ID))
            .thenReturn(Optional.of(buildPsuIdDataList()));
        when(pisCommonPaymentService.getAuthorisationScaStatus(DECRYPTED_PAYMENT_ID, AUTHORISATION_ID, CmsAuthorisationType.CREATED))
            .thenReturn(Optional.of(SCA_STATUS));
    }

    @Test
    public void createCommonPayment_success() {
        // Given
        PisPaymentInfo request = buildPisPaymentInfoRequest();
        CreatePisCommonPaymentResponse expected = buildCreatePisCommonPaymentResponse(ENCRYPTED_PAYMENT_ID);

        // When
        Optional<CreatePisCommonPaymentResponse> actual = pisCommonPaymentServiceInternalEncrypted.createCommonPayment(request);

        // Then
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
        verify(pisCommonPaymentService, times(1)).createCommonPayment(request);
    }

    @Test
    public void getPisCommonPaymentStatusById_success() {
        // When
        Optional<TransactionStatus> actual = pisCommonPaymentServiceInternalEncrypted.getPisCommonPaymentStatusById(ENCRYPTED_PAYMENT_ID);

        // Then
        assertTrue(actual.isPresent());
        assertEquals(TRANSACTION_STATUS, actual.get());
        verify(pisCommonPaymentService, times(1)).getPisCommonPaymentStatusById(DECRYPTED_PAYMENT_ID);
    }

    @Test
    public void getCommonPaymentById_success() {
        // Given
        PisCommonPaymentResponse expected = buildPisCommonPaymentResponse(DECRYPTED_PAYMENT_ID);

        // When
        Optional<PisCommonPaymentResponse> actual = pisCommonPaymentServiceInternalEncrypted.getCommonPaymentById(ENCRYPTED_PAYMENT_ID);

        // Then
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
        verify(pisCommonPaymentService, times(1)).getCommonPaymentById(DECRYPTED_PAYMENT_ID);
    }

    @Test
    public void updateCommonPaymentStatusById_success() {
        // When
        Optional<Boolean> actual = pisCommonPaymentServiceInternalEncrypted.updateCommonPaymentStatusById(ENCRYPTED_PAYMENT_ID, TRANSACTION_STATUS);

        // Then
        assertTrue(actual.isPresent());
        assertTrue(actual.get());
        verify(pisCommonPaymentService, times(1)).updateCommonPaymentStatusById(DECRYPTED_PAYMENT_ID, TRANSACTION_STATUS);
    }

    @Test
    public void getDecryptedId_success() {
        // When
        Optional<String> actual = pisCommonPaymentServiceInternalEncrypted.getDecryptedId(ENCRYPTED_PAYMENT_ID);

        // Then
        assertTrue(actual.isPresent());
        assertEquals(DECRYPTED_PAYMENT_ID, actual.get());
    }

    @Test
    public void createAuthorization_success() {
        // Given
        PsuIdData psuIdData = buildPsuIdData();
        CreatePisAuthorisationResponse expected = buildCreatePisAuthorisationResponse();

        // When
        Optional<CreatePisAuthorisationResponse> actual =
            pisCommonPaymentServiceInternalEncrypted.createAuthorization(ENCRYPTED_PAYMENT_ID, AUTHORISATION_TYPE, psuIdData);

        // Then
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
        verify(pisCommonPaymentService, times(1))
            .createAuthorization(DECRYPTED_PAYMENT_ID, AUTHORISATION_TYPE, psuIdData);
    }

    @Test
    public void createAuthorizationCancellation_success() {
        // Given
        PsuIdData psuIdData = buildPsuIdData();
        CreatePisAuthorisationResponse expected = buildCreatePisAuthorisationResponse();

        // When
        Optional<CreatePisAuthorisationResponse> actual =
            pisCommonPaymentServiceInternalEncrypted.createAuthorizationCancellation(ENCRYPTED_PAYMENT_ID, AUTHORISATION_TYPE, psuIdData);

        // Then
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
        verify(pisCommonPaymentService, times(1))
            .createAuthorizationCancellation(DECRYPTED_PAYMENT_ID, AUTHORISATION_TYPE, psuIdData);
    }

    @Test
    public void updatePisAuthorisation_success() {
        // Given
        UpdatePisCommonPaymentPsuDataRequest request = buildUpdatePisCommonPaymentPsuDataRequest();
        UpdatePisCommonPaymentPsuDataResponse expected = buildUpdatePisCommonPaymentPsuDataResponse();

        // When
        Optional<UpdatePisCommonPaymentPsuDataResponse> actual = pisCommonPaymentServiceInternalEncrypted.updatePisAuthorisation(AUTHORISATION_ID, request);

        // Then
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
        verify(pisCommonPaymentService, times(1))
            .updatePisAuthorisation(AUTHORISATION_ID, request);
    }

    @Test
    public void updatePisCancellationAuthorisation_success() {
        // Given
        UpdatePisCommonPaymentPsuDataRequest request = buildUpdatePisCommonPaymentPsuDataRequest();
        UpdatePisCommonPaymentPsuDataResponse expected = buildUpdatePisCommonPaymentPsuDataResponse();

        // When
        Optional<UpdatePisCommonPaymentPsuDataResponse> actual =
            pisCommonPaymentServiceInternalEncrypted.updatePisCancellationAuthorisation(AUTHORISATION_ID, request);

        // Then
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
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
        Optional<GetPisAuthorisationResponse> actual =
            pisCommonPaymentServiceInternalEncrypted.getPisAuthorisationById(AUTHORISATION_ID);

        // Then
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
        verify(pisCommonPaymentService, times(1)).getPisAuthorisationById(AUTHORISATION_ID);
    }

    @Test
    public void getPisCancellationAuthorisationById_success() {
        // Given
        GetPisAuthorisationResponse expected = buildGetPisAuthorisationResponse();

        // When
        Optional<GetPisAuthorisationResponse> actual =
            pisCommonPaymentServiceInternalEncrypted.getPisCancellationAuthorisationById(AUTHORISATION_ID);

        // Then
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
        verify(pisCommonPaymentService, times(1))
            .getPisCancellationAuthorisationById(AUTHORISATION_ID);
    }

    @Test
    public void getAuthorisationsByPaymentId_success() {
        // Given
        List<String> expected = buildPaymentAuthorisations();

        // When
        Optional<List<String>> actual = pisCommonPaymentServiceInternalEncrypted.getAuthorisationsByPaymentId(ENCRYPTED_PAYMENT_ID,
                                                                                                              CmsAuthorisationType.CREATED);

        // Then
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
        verify(pisCommonPaymentService, times(1))
            .getAuthorisationsByPaymentId(DECRYPTED_PAYMENT_ID, CmsAuthorisationType.CREATED);
    }

    @Test
    public void getAuthorisationScaStatus_success() {
        // When
        Optional<ScaStatus> actual = pisCommonPaymentServiceInternalEncrypted.getAuthorisationScaStatus(ENCRYPTED_PAYMENT_ID, AUTHORISATION_ID, CmsAuthorisationType.CREATED);

        // Then
        assertTrue(actual.isPresent());
        assertEquals(SCA_STATUS, actual.get());
        verify(pisCommonPaymentService, times(1)).getAuthorisationScaStatus(DECRYPTED_PAYMENT_ID, AUTHORISATION_ID, CmsAuthorisationType.CREATED);
    }

    @Test
    public void getPsuDataListByPaymentId_success() {
        // Given
        List<PsuIdData> expected = buildPsuIdDataList();

        // When
        Optional<List<PsuIdData>> actual = pisCommonPaymentServiceInternalEncrypted.getPsuDataListByPaymentId(ENCRYPTED_PAYMENT_ID);

        // Then
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
        verify(pisCommonPaymentService, times(1)).getPsuDataListByPaymentId(DECRYPTED_PAYMENT_ID);
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

    private PsuIdData buildPsuIdData() {
        return new PsuIdData(null, null, null, null);
    }

    private List<PsuIdData> buildPsuIdDataList() {
        return Collections.singletonList(buildPsuIdData());
    }

    private CreatePisAuthorisationResponse buildCreatePisAuthorisationResponse() {
        return new CreatePisAuthorisationResponse(AUTHORISATION_ID);
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
