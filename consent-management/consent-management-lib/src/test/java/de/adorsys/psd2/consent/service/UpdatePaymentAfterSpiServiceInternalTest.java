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

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdatePaymentAfterSpiServiceInternalTest {
    private static final String PAYMENT_ID = "payment id";
    private static final String INTERNAL_REQUEST_ID = "5c2d5564-367f-4e03-a621-6bef76fa4208";

    @InjectMocks
    private UpdatePaymentAfterSpiServiceInternal updatePaymentAfterSpiServiceInternal;

    @Mock
    private CommonPaymentDataService commonPaymentDataService;
    private TppRedirectUri tppRedirectUri;
    private PisCommonPaymentData pisCommonPaymentData;

    @BeforeEach
    void setUp() {
        tppRedirectUri = new TppRedirectUri("ok_url", "nok_url");
        pisCommonPaymentData = new PisCommonPaymentData();
    }

    @Test
    void updatePaymentStatus_success() {
        // Given
        pisCommonPaymentData.setTransactionStatus(TransactionStatus.ACCP);

        when(commonPaymentDataService.getPisCommonPaymentData(PAYMENT_ID, null)).thenReturn(Optional.of(pisCommonPaymentData));
        when(commonPaymentDataService.updateStatusInPaymentData(pisCommonPaymentData, TransactionStatus.ACSP)).thenReturn(true);

        // When
        CmsResponse<Boolean> actual = updatePaymentAfterSpiServiceInternal.updatePaymentStatus(PAYMENT_ID, TransactionStatus.ACSP);

        // Then
        assertTrue(actual.isSuccessful());

        assertTrue(actual.getPayload());
        verify(commonPaymentDataService, times(1)).getPisCommonPaymentData(anyString(), isNull());
        verify(commonPaymentDataService, times(1)).updateStatusInPaymentData(any(PisCommonPaymentData.class), eq(TransactionStatus.ACSP));
    }

    @Test
    void updatePaymentStatus_transactionStatusIsFinalised() {
        // Given
        pisCommonPaymentData.setTransactionStatus(TransactionStatus.RJCT);

        when(commonPaymentDataService.getPisCommonPaymentData(PAYMENT_ID, null)).thenReturn(Optional.of(pisCommonPaymentData));

        // When
        CmsResponse<Boolean> actual = updatePaymentAfterSpiServiceInternal.updatePaymentStatus(PAYMENT_ID, TransactionStatus.ACSP);

        // Then
        assertTrue(actual.isSuccessful());

        assertFalse(actual.getPayload());

        verify(commonPaymentDataService, times(1)).getPisCommonPaymentData(anyString(), isNull());
        verify(commonPaymentDataService, never()).updateStatusInPaymentData(any(PisCommonPaymentData.class), any(TransactionStatus.class));
    }

    @Test
    void updatePaymentStatus_paymentDataIsEmpty() {
        // Given
        when(commonPaymentDataService.getPisCommonPaymentData(PAYMENT_ID, null)).thenReturn(Optional.empty());

        // When
        CmsResponse<Boolean> actual = updatePaymentAfterSpiServiceInternal.updatePaymentStatus(PAYMENT_ID, TransactionStatus.ACSP);

        // Then
        assertTrue(actual.isSuccessful());

        assertFalse(actual.getPayload());
        verify(commonPaymentDataService, times(1)).getPisCommonPaymentData(anyString(), isNull());
        verify(commonPaymentDataService, never()).updateStatusInPaymentData(any(PisCommonPaymentData.class), any(TransactionStatus.class));
    }

    @Test
    void updatePaymentCancellationTppRedirectUri_success() {
        // Given
        pisCommonPaymentData.setTransactionStatus(TransactionStatus.ACCP);

        when(commonPaymentDataService.getPisCommonPaymentData(PAYMENT_ID, null)).thenReturn(Optional.of(pisCommonPaymentData));
        when(commonPaymentDataService.updateCancelTppRedirectURIs(pisCommonPaymentData, tppRedirectUri)).thenReturn(true);

        // When
        CmsResponse<Boolean> actual = updatePaymentAfterSpiServiceInternal.updatePaymentCancellationTppRedirectUri(PAYMENT_ID, tppRedirectUri);

        // Then
        assertTrue(actual.isSuccessful());

        assertTrue(actual.getPayload());
        verify(commonPaymentDataService, times(1)).getPisCommonPaymentData(anyString(), isNull());
        verify(commonPaymentDataService, times(1)).updateCancelTppRedirectURIs(pisCommonPaymentData, tppRedirectUri);
    }

    @Test
    void updatePaymentCancellationTppRedirectUri_transactionStatusIsFinalised() {
        // Given
        pisCommonPaymentData.setTransactionStatus(TransactionStatus.RJCT);

        when(commonPaymentDataService.getPisCommonPaymentData(PAYMENT_ID, null)).thenReturn(Optional.of(pisCommonPaymentData));

        // When
        CmsResponse<Boolean> actual = updatePaymentAfterSpiServiceInternal.updatePaymentCancellationTppRedirectUri(PAYMENT_ID, tppRedirectUri);

        // Then
        assertTrue(actual.isSuccessful());

        assertFalse(actual.getPayload());
        verify(commonPaymentDataService, times(1)).getPisCommonPaymentData(anyString(), isNull());
        verify(commonPaymentDataService, never()).updateCancelTppRedirectURIs(any(PisCommonPaymentData.class), eq(tppRedirectUri));
    }

    @Test
    void updatePaymentCancellationTppRedirectUri_paymentDataIsEmpty() {
        // Given
        when(commonPaymentDataService.getPisCommonPaymentData(PAYMENT_ID, null)).thenReturn(Optional.empty());

        // When
        CmsResponse<Boolean> actual = updatePaymentAfterSpiServiceInternal.updatePaymentCancellationTppRedirectUri(PAYMENT_ID, tppRedirectUri);

        // Then
        assertTrue(actual.isSuccessful());

        assertFalse(actual.getPayload());
        verify(commonPaymentDataService, times(1)).getPisCommonPaymentData(anyString(), isNull());
        verify(commonPaymentDataService, never()).updateCancelTppRedirectURIs(any(PisCommonPaymentData.class), eq(tppRedirectUri));
    }

    @Test
    void updatePaymentCancellationInternalRequestId_success() {
        // Given
        pisCommonPaymentData.setTransactionStatus(TransactionStatus.ACCP);

        when(commonPaymentDataService.getPisCommonPaymentData(PAYMENT_ID, null)).thenReturn(Optional.of(pisCommonPaymentData));
        when(commonPaymentDataService.updatePaymentCancellationInternalRequestId(pisCommonPaymentData, INTERNAL_REQUEST_ID)).thenReturn(true);

        // When
        CmsResponse<Boolean> actual = updatePaymentAfterSpiServiceInternal.updatePaymentCancellationInternalRequestId(PAYMENT_ID, INTERNAL_REQUEST_ID);

        // Then
        assertTrue(actual.isSuccessful());

        assertTrue(actual.getPayload());
        verify(commonPaymentDataService, times(1)).getPisCommonPaymentData(anyString(), isNull());
        verify(commonPaymentDataService, times(1)).updatePaymentCancellationInternalRequestId(pisCommonPaymentData, INTERNAL_REQUEST_ID);
    }
}
