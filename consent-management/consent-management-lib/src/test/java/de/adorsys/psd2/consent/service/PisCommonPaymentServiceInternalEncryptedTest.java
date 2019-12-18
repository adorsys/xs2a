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

import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentRequest;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentService;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
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
    private static final String ENCRYPTED_PAYMENT_ID = "encrypted payment id";
    private static final String DECRYPTED_PAYMENT_ID = "1856e4fa-8af8-427b-85ec-4caf515ce074";
    private static final PsuIdData PSU_DATA = new PsuIdData(null, null, null, null, null);

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
        when(pisCommonPaymentService.getPsuDataListByPaymentId(DECRYPTED_PAYMENT_ID))
            .thenReturn(CmsResponse.<List<PsuIdData>>builder()
                            .payload(buildPsuIdDataList())
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
    public void createCommonPayment_technicalError() {
        PisPaymentInfo request = buildPisPaymentInfoRequest();
        when(pisCommonPaymentService.createCommonPayment(buildPisPaymentInfoRequest()))
            .thenReturn(CmsResponse.<CreatePisCommonPaymentResponse>builder()
                            .payload(buildCreatePisCommonPaymentResponse(DECRYPTED_PAYMENT_ID))
                            .build());
        when(securityDataService.encryptId(DECRYPTED_PAYMENT_ID)).thenReturn(Optional.empty());

        CmsResponse<CreatePisCommonPaymentResponse> actual = pisCommonPaymentServiceInternalEncrypted.createCommonPayment(request);

        assertTrue(actual.hasError());
        assertEquals(CmsError.TECHNICAL_ERROR, actual.getError());
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
    public void getPisCommonPaymentStatusById_technicalError() {
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.empty());

        CmsResponse<TransactionStatus> actual = pisCommonPaymentServiceInternalEncrypted.getPisCommonPaymentStatusById(ENCRYPTED_PAYMENT_ID);

        assertTrue(actual.hasError());
        assertEquals(CmsError.TECHNICAL_ERROR, actual.getError());
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
    public void getCommonPaymentById_technicalError() {
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.empty());

        CmsResponse<PisCommonPaymentResponse> actual = pisCommonPaymentServiceInternalEncrypted.getCommonPaymentById(ENCRYPTED_PAYMENT_ID);

        assertTrue(actual.hasError());
        assertEquals(CmsError.TECHNICAL_ERROR, actual.getError());
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
    public void updateCommonPaymentStatusById_technicalError() {
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.empty());

        CmsResponse<Boolean> actual = pisCommonPaymentServiceInternalEncrypted.updateCommonPaymentStatusById(ENCRYPTED_PAYMENT_ID, TRANSACTION_STATUS);

        assertTrue(actual.hasError());
        assertEquals(CmsError.TECHNICAL_ERROR, actual.getError());
        verify(pisCommonPaymentService, never()).updateCommonPaymentStatusById(DECRYPTED_PAYMENT_ID, TRANSACTION_STATUS);
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
    public void getDecryptedId_technicalError() {
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.empty());

        CmsResponse<String> actual = pisCommonPaymentServiceInternalEncrypted.getDecryptedId(ENCRYPTED_PAYMENT_ID);

        assertTrue(actual.hasError());
        assertEquals(CmsError.TECHNICAL_ERROR, actual.getError());
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

    @Test
    public void updateMultilevelSca_technicalError() {
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.empty());

        CmsResponse<Boolean> actual = pisCommonPaymentServiceInternalEncrypted.updateMultilevelSca(ENCRYPTED_PAYMENT_ID, true);

        assertTrue(actual.hasError());
        assertEquals(CmsError.TECHNICAL_ERROR, actual.getError());
    }

    @Test
    public void getPsuDataListByPaymentId_technicalError() {
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.empty());

        CmsResponse<List<PsuIdData>> actual = pisCommonPaymentServiceInternalEncrypted.getPsuDataListByPaymentId(ENCRYPTED_PAYMENT_ID);

        assertTrue(actual.hasError());
        assertEquals(CmsError.TECHNICAL_ERROR, actual.getError());
    }

    private PisPaymentInfo buildPisPaymentInfoRequest() {
        return new PisPaymentInfo();
    }

    private PisCommonPaymentRequest buildPisCommonPaymentRequest() {
        return new PisCommonPaymentRequest();
    }

    private CreatePisCommonPaymentResponse buildCreatePisCommonPaymentResponse(String id) {
        return new CreatePisCommonPaymentResponse(id, null);
    }

    private PisCommonPaymentResponse buildPisCommonPaymentResponse(String id) {
        PisCommonPaymentResponse response = new PisCommonPaymentResponse();
        response.setExternalId(id);
        return response;
    }

    private List<PsuIdData> buildPsuIdDataList() {
        return Collections.singletonList(PSU_DATA);
    }
}

