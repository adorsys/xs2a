/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentRequest;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentService;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PisCommonPaymentServiceInternalEncryptedTest {
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

    @Test
    void createCommonPayment_success() {
        // Given
        PisPaymentInfo request = buildPisPaymentInfoRequest();
        CreatePisCommonPaymentResponse expected = buildCreatePisCommonPaymentResponse(ENCRYPTED_PAYMENT_ID);
        when(securityDataService.encryptId(DECRYPTED_PAYMENT_ID))
            .thenReturn(Optional.of(ENCRYPTED_PAYMENT_ID));
        when(pisCommonPaymentService.createCommonPayment(buildPisPaymentInfoRequest()))
            .thenReturn(CmsResponse.<CreatePisCommonPaymentResponse>builder()
                            .payload(buildCreatePisCommonPaymentResponse(DECRYPTED_PAYMENT_ID))
                            .build());

        // When
        CmsResponse<CreatePisCommonPaymentResponse> actual = pisCommonPaymentServiceInternalEncrypted.createCommonPayment(request);

        // Then
        assertTrue(actual.isSuccessful());

        assertEquals(expected, actual.getPayload());
        verify(pisCommonPaymentService, times(1)).createCommonPayment(request);
    }

    @Test
    void createCommonPayment_technicalError() {
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
    void getPisCommonPaymentStatusById_success() {
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.of(DECRYPTED_PAYMENT_ID));
        when(pisCommonPaymentService.getPisCommonPaymentStatusById(DECRYPTED_PAYMENT_ID))
            .thenReturn(CmsResponse.<TransactionStatus>builder()
                            .payload(TRANSACTION_STATUS)
                            .build());
        // When
        CmsResponse<TransactionStatus> actual = pisCommonPaymentServiceInternalEncrypted.getPisCommonPaymentStatusById(ENCRYPTED_PAYMENT_ID);

        // Then
        assertTrue(actual.isSuccessful());

        assertEquals(TRANSACTION_STATUS, actual.getPayload());
        verify(pisCommonPaymentService, times(1)).getPisCommonPaymentStatusById(DECRYPTED_PAYMENT_ID);
    }

    @Test
    void getPisCommonPaymentStatusById_technicalError() {
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.empty());

        CmsResponse<TransactionStatus> actual = pisCommonPaymentServiceInternalEncrypted.getPisCommonPaymentStatusById(ENCRYPTED_PAYMENT_ID);

        assertTrue(actual.hasError());
        assertEquals(CmsError.TECHNICAL_ERROR, actual.getError());
    }

    @Test
    void getCommonPaymentById_success() {
        // Given
        PisCommonPaymentResponse expected = buildPisCommonPaymentResponse(DECRYPTED_PAYMENT_ID);
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.of(DECRYPTED_PAYMENT_ID));
        when(pisCommonPaymentService.getCommonPaymentById(DECRYPTED_PAYMENT_ID))
            .thenReturn(CmsResponse.<PisCommonPaymentResponse>builder()
                            .payload(buildPisCommonPaymentResponse(DECRYPTED_PAYMENT_ID))
                            .build());

        // When
        CmsResponse<PisCommonPaymentResponse> actual = pisCommonPaymentServiceInternalEncrypted.getCommonPaymentById(ENCRYPTED_PAYMENT_ID);

        // Then
        assertTrue(actual.isSuccessful());

        assertEquals(expected, actual.getPayload());
        verify(pisCommonPaymentService, times(1)).getCommonPaymentById(DECRYPTED_PAYMENT_ID);
    }

    @Test
    void getCommonPaymentById_technicalError() {
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.empty());

        CmsResponse<PisCommonPaymentResponse> actual = pisCommonPaymentServiceInternalEncrypted.getCommonPaymentById(ENCRYPTED_PAYMENT_ID);

        assertTrue(actual.hasError());
        assertEquals(CmsError.TECHNICAL_ERROR, actual.getError());
    }

    @Test
    void updateCommonPaymentStatusById_success() {
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.of(DECRYPTED_PAYMENT_ID));
        when(pisCommonPaymentService.updateCommonPaymentStatusById(DECRYPTED_PAYMENT_ID, TRANSACTION_STATUS))
            .thenReturn(CmsResponse.<Boolean>builder()
                            .payload(true)
                            .build());
        // When
        CmsResponse<Boolean> actual = pisCommonPaymentServiceInternalEncrypted.updateCommonPaymentStatusById(ENCRYPTED_PAYMENT_ID, TRANSACTION_STATUS);

        // Then
        assertTrue(actual.isSuccessful());

        assertTrue(actual.getPayload());
        verify(pisCommonPaymentService, times(1)).updateCommonPaymentStatusById(DECRYPTED_PAYMENT_ID, TRANSACTION_STATUS);
    }

    @Test
    void updateCommonPaymentStatusById_technicalError() {
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.empty());

        CmsResponse<Boolean> actual = pisCommonPaymentServiceInternalEncrypted.updateCommonPaymentStatusById(ENCRYPTED_PAYMENT_ID, TRANSACTION_STATUS);

        assertTrue(actual.hasError());
        assertEquals(CmsError.TECHNICAL_ERROR, actual.getError());
        verify(pisCommonPaymentService, never()).updateCommonPaymentStatusById(DECRYPTED_PAYMENT_ID, TRANSACTION_STATUS);
    }

    @Test
    void getDecryptedId_success() {
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.of(DECRYPTED_PAYMENT_ID));

        // When
        CmsResponse<String> actual = pisCommonPaymentServiceInternalEncrypted.getDecryptedId(ENCRYPTED_PAYMENT_ID);

        // Then
        assertTrue(actual.isSuccessful());

        assertEquals(DECRYPTED_PAYMENT_ID, actual.getPayload());
    }

    @Test
    void getDecryptedId_technicalError() {
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.empty());

        CmsResponse<String> actual = pisCommonPaymentServiceInternalEncrypted.getDecryptedId(ENCRYPTED_PAYMENT_ID);

        assertTrue(actual.hasError());
        assertEquals(CmsError.TECHNICAL_ERROR, actual.getError());
    }

    @Test
    void getPsuDataListByPaymentId_success() {
        // Given
        List<PsuIdData> expected = buildPsuIdDataList();
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.of(DECRYPTED_PAYMENT_ID));
        when(pisCommonPaymentService.getPsuDataListByPaymentId(DECRYPTED_PAYMENT_ID))
            .thenReturn(CmsResponse.<List<PsuIdData>>builder()
                            .payload(buildPsuIdDataList())
                            .build());

        // When
        CmsResponse<List<PsuIdData>> actual = pisCommonPaymentServiceInternalEncrypted.getPsuDataListByPaymentId(ENCRYPTED_PAYMENT_ID);

        // Then
        assertTrue(actual.isSuccessful());

        assertEquals(expected, actual.getPayload());
        verify(pisCommonPaymentService, times(1)).getPsuDataListByPaymentId(DECRYPTED_PAYMENT_ID);
    }

    @Test
    void updateMultilevelSca_True() {
        // Given
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.of(DECRYPTED_PAYMENT_ID));
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
    void updateMultilevelSca_technicalError() {
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.empty());

        CmsResponse<Boolean> actual = pisCommonPaymentServiceInternalEncrypted.updateMultilevelSca(ENCRYPTED_PAYMENT_ID, true);

        assertTrue(actual.hasError());
        assertEquals(CmsError.TECHNICAL_ERROR, actual.getError());
    }

    @Test
    void getPsuDataListByPaymentId_technicalError() {
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

