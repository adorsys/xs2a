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


package de.adorsys.psd2.consent.web.xs2a.controller;

import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentDataStatusResponse;
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PisCommonPaymentControllerTest {
    private static final String PAYMENT_ID = "33333-999999999";
    private static final String STATUS_RECEIVED = "Received";
    private static final String WRONG_PAYMENT_ID = "32343-999997777";

    @InjectMocks
    private PisCommonPaymentController pisCommonPaymentController;

    @Mock
    private PisCommonPaymentServiceEncrypted pisCommonPaymentService;

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
    void updateMultilevelScaRequired_Ok() {
        when(pisCommonPaymentService.updateMultilevelSca(PAYMENT_ID, true))
            .thenReturn(CmsResponse.<Boolean>builder().payload(true).build());

        ResponseEntity<Boolean> actualResponse = pisCommonPaymentController.updateMultilevelScaRequired(PAYMENT_ID, true);

        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertNotNull(actualResponse.getBody());
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

    private PisPaymentInfo getPisPaymentInfo() {
        return new PisPaymentInfo();
    }

    private CreatePisCommonPaymentResponse getCreatePisCommonPaymentResponse() {
        return new CreatePisCommonPaymentResponse(PAYMENT_ID, null);
    }

    private PisCommonPaymentResponse getPisCommonPaymentResponse() {
        return new PisCommonPaymentResponse();
    }
}
