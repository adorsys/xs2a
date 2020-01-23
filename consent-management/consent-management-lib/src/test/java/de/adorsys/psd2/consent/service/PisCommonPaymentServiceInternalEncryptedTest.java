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

import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentRequest;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
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
    private static final PsuIdData PSU_DATA = new PsuIdData(null, null, null, null);

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
        when(securityDataService.encryptId(DECRYPTED_PAYMENT_ID)).thenReturn(Optional.of(ENCRYPTED_PAYMENT_ID));
        when(securityDataService.encryptId(DECRYPTED_PAYMENT_ID)).thenReturn(Optional.of(ENCRYPTED_PAYMENT_ID));
        when(pisCommonPaymentService.createCommonPayment(buildPisPaymentInfoRequest()))
            .thenReturn(Optional.of(buildCreatePisCommonPaymentResponse(DECRYPTED_PAYMENT_ID)));

        // When
        Optional<CreatePisCommonPaymentResponse> actual = pisCommonPaymentServiceInternalEncrypted.createCommonPayment(request);

        // Then
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
        verify(pisCommonPaymentService, times(1)).createCommonPayment(request);
    }

    @Test
    void getPisCommonPaymentStatusById_success() {
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.of(DECRYPTED_PAYMENT_ID));
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.of(DECRYPTED_PAYMENT_ID));
        when(pisCommonPaymentService.getPisCommonPaymentStatusById(DECRYPTED_PAYMENT_ID)).thenReturn(Optional.of(TRANSACTION_STATUS));

        // When
        Optional<TransactionStatus> actual = pisCommonPaymentServiceInternalEncrypted.getPisCommonPaymentStatusById(ENCRYPTED_PAYMENT_ID);

        // Then
        assertTrue(actual.isPresent());
        assertEquals(TRANSACTION_STATUS, actual.get());
        verify(pisCommonPaymentService, times(1)).getPisCommonPaymentStatusById(DECRYPTED_PAYMENT_ID);
    }

    @Test
    void getCommonPaymentById_success() {
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.of(DECRYPTED_PAYMENT_ID));
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.of(DECRYPTED_PAYMENT_ID));
        when(pisCommonPaymentService.getCommonPaymentById(DECRYPTED_PAYMENT_ID)).thenReturn(Optional.of(buildPisCommonPaymentResponse(DECRYPTED_PAYMENT_ID)));

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
    void updateCommonPaymentStatusById_success() {
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.of(DECRYPTED_PAYMENT_ID));
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.of(DECRYPTED_PAYMENT_ID));
        when(pisCommonPaymentService.updateCommonPaymentStatusById(DECRYPTED_PAYMENT_ID, TRANSACTION_STATUS)).thenReturn(Optional.of(true));

        // When
        Optional<Boolean> actual = pisCommonPaymentServiceInternalEncrypted.updateCommonPaymentStatusById(ENCRYPTED_PAYMENT_ID, TRANSACTION_STATUS);

        // Then
        assertTrue(actual.isPresent());
        assertTrue(actual.get());
        verify(pisCommonPaymentService, times(1)).updateCommonPaymentStatusById(DECRYPTED_PAYMENT_ID, TRANSACTION_STATUS);
    }

    @Test
    void getDecryptedId_success() {
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.of(DECRYPTED_PAYMENT_ID));
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.of(DECRYPTED_PAYMENT_ID));

        // When
        Optional<String> actual = pisCommonPaymentServiceInternalEncrypted.getDecryptedId(ENCRYPTED_PAYMENT_ID);

        // Then
        assertTrue(actual.isPresent());
        assertEquals(DECRYPTED_PAYMENT_ID, actual.get());
    }

    @Test
    void updateCommonPayment_success() {
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.of(DECRYPTED_PAYMENT_ID));
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.of(DECRYPTED_PAYMENT_ID));

        // Given
        PisCommonPaymentRequest request = buildPisCommonPaymentRequest();

        // When
        pisCommonPaymentServiceInternalEncrypted.updateCommonPayment(request, ENCRYPTED_PAYMENT_ID);

        // Then
        verify(pisCommonPaymentService, times(1))
            .updateCommonPayment(request, DECRYPTED_PAYMENT_ID);
    }

    @Test
    void getPsuDataListByPaymentId_success() {
        // Given
        List<PsuIdData> expected = buildPsuIdDataList();
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.of(DECRYPTED_PAYMENT_ID));
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.of(DECRYPTED_PAYMENT_ID));
        when(pisCommonPaymentService.getPsuDataListByPaymentId(DECRYPTED_PAYMENT_ID)).thenReturn(Optional.of(buildPsuIdDataList()));

        // When
        Optional<List<PsuIdData>> actual = pisCommonPaymentServiceInternalEncrypted.getPsuDataListByPaymentId(ENCRYPTED_PAYMENT_ID);

        // Then
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
        verify(pisCommonPaymentService, times(1)).getPsuDataListByPaymentId(DECRYPTED_PAYMENT_ID);
    }

    @Test
    void updateMultilevelSca_True() {
        // Given
        when(pisCommonPaymentService.updateMultilevelSca(DECRYPTED_PAYMENT_ID, true)).thenReturn(true);
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.of(DECRYPTED_PAYMENT_ID));

        // When
        boolean actualResponse = pisCommonPaymentServiceInternalEncrypted.updateMultilevelSca(ENCRYPTED_PAYMENT_ID, true);

        // Then
        assertTrue(actualResponse);
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
}
