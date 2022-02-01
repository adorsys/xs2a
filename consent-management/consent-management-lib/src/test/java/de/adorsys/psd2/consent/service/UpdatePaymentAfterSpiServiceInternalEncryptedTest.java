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
import de.adorsys.psd2.consent.api.service.UpdatePaymentAfterSpiService;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdatePaymentAfterSpiServiceInternalEncryptedTest {
    private static final String ENCRYPTED_PAYMENT_ID = "encrypted payment id";
    private static final String WRONG_ENCRYPTED_PAYMENT_ID = "wrong encrypted payment id";
    private static final String UNDECRYPTABLE_PAYMENT_ID = "undecryptable payment id";
    private static final String DECRYPTED_PAYMENT_ID = "91cd2158-4344-44f4-bdbb-c736ededa436";
    private static final String WRONG_DECRYPTED_PAYMENT_ID = "386d670d-4323-43ba-a953-8d0c97d9deca";
    private static final String INTERNAL_REQUEST_ID = "5c2d5564-367f-4e03-a621-6bef76fa4208";

    @InjectMocks
    private UpdatePaymentAfterSpiServiceInternalEncrypted updatePaymentStatusAfterSpiServiceInternalEncrypted;
    @Mock
    private UpdatePaymentAfterSpiService updatePaymentStatusAfterSpiService;
    @Mock
    private SecurityDataService securityDataService;
    private TppRedirectUri tppRedirectUri;

    @BeforeEach
    void setUp() {
        tppRedirectUri = new TppRedirectUri("ok_url", "nok_url");
    }

    @Test
    void updatePaymentStatus_success() {
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.of(DECRYPTED_PAYMENT_ID));
        when(updatePaymentStatusAfterSpiService.updatePaymentStatus(eq(DECRYPTED_PAYMENT_ID), any()))
            .thenReturn(CmsResponse.<Boolean>builder()
                            .payload(true)
                            .build());

        // When
        CmsResponse<Boolean> actual = updatePaymentStatusAfterSpiServiceInternalEncrypted.updatePaymentStatus(ENCRYPTED_PAYMENT_ID, TransactionStatus.ACCP);

        // Then
        assertTrue(actual.isSuccessful());

        assertTrue(actual.getPayload());
        verify(updatePaymentStatusAfterSpiService, times(1)).updatePaymentStatus(any(), any());
    }

    @Test
    void updatePaymentStatus_failure_wrongPaymentId() {
        when(securityDataService.decryptId(WRONG_ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.of(WRONG_DECRYPTED_PAYMENT_ID));
        when(updatePaymentStatusAfterSpiService.updatePaymentStatus(eq(WRONG_DECRYPTED_PAYMENT_ID), any()))
            .thenReturn(CmsResponse.<Boolean>builder()
                            .payload(false)
                            .build());

        // When
        CmsResponse<Boolean> actual = updatePaymentStatusAfterSpiServiceInternalEncrypted.updatePaymentStatus(WRONG_ENCRYPTED_PAYMENT_ID, TransactionStatus.ACCP);

        // Then
        assertTrue(actual.isSuccessful());

        assertFalse(actual.getPayload());
        verify(updatePaymentStatusAfterSpiService, times(1)).updatePaymentStatus(any(), any());
    }

    @Test
    void updatePaymentStatus_failure_decryptionFailed() {
        when(securityDataService.decryptId(UNDECRYPTABLE_PAYMENT_ID)).thenReturn(Optional.empty());

        // When
        CmsResponse<Boolean> actual = updatePaymentStatusAfterSpiServiceInternalEncrypted.updatePaymentStatus(UNDECRYPTABLE_PAYMENT_ID, TransactionStatus.ACCP);

        // Then
        assertTrue(actual.hasError());

        assertEquals(CmsError.TECHNICAL_ERROR, actual.getError());
        verify(updatePaymentStatusAfterSpiService, never()).updatePaymentStatus(any(), any());
    }

    @Test
    void updatePaymentCancellationTppRedirectUri_success() {
        // Given
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.of(DECRYPTED_PAYMENT_ID));
        when(updatePaymentStatusAfterSpiService.updatePaymentCancellationTppRedirectUri(DECRYPTED_PAYMENT_ID, tppRedirectUri))
            .thenReturn(CmsResponse.<Boolean>builder()
                            .payload(true)
                            .build());

        // When
        CmsResponse<Boolean> actual = updatePaymentStatusAfterSpiServiceInternalEncrypted.updatePaymentCancellationTppRedirectUri(ENCRYPTED_PAYMENT_ID, tppRedirectUri);

        // Then
        assertTrue(actual.isSuccessful());

        assertTrue(actual.getPayload());
        verify(securityDataService, times(1)).decryptId(ENCRYPTED_PAYMENT_ID);
        verify(updatePaymentStatusAfterSpiService, times(1)).updatePaymentCancellationTppRedirectUri(DECRYPTED_PAYMENT_ID, tppRedirectUri);
    }

    @Test
    void updatePaymentCancellationTppRedirectUri_failure_wrongPaymentId() {
        // Given
        when(securityDataService.decryptId(WRONG_ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.of(WRONG_DECRYPTED_PAYMENT_ID));
        when(updatePaymentStatusAfterSpiService.updatePaymentCancellationTppRedirectUri(WRONG_DECRYPTED_PAYMENT_ID, tppRedirectUri))
            .thenReturn(CmsResponse.<Boolean>builder()
                            .payload(false)
                            .build());

        // When
        CmsResponse<Boolean> actual = updatePaymentStatusAfterSpiServiceInternalEncrypted.updatePaymentCancellationTppRedirectUri(WRONG_ENCRYPTED_PAYMENT_ID, tppRedirectUri);

        // Then
        assertTrue(actual.isSuccessful());

        assertFalse(actual.getPayload());
        verify(securityDataService, times(1)).decryptId(WRONG_ENCRYPTED_PAYMENT_ID);
        verify(updatePaymentStatusAfterSpiService, times(1)).updatePaymentCancellationTppRedirectUri(WRONG_DECRYPTED_PAYMENT_ID, tppRedirectUri);
    }

    @Test
    void updatePaymentCancellationTppRedirectUri_failure_decryptionFailed() {
        // Given
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.empty());

        // When
        CmsResponse<Boolean> actual = updatePaymentStatusAfterSpiServiceInternalEncrypted.updatePaymentCancellationTppRedirectUri(ENCRYPTED_PAYMENT_ID, tppRedirectUri);

        // Then
        assertTrue(actual.hasError());

        assertEquals(CmsError.TECHNICAL_ERROR, actual.getError());
        verify(securityDataService, times(1)).decryptId(ENCRYPTED_PAYMENT_ID);
        verify(updatePaymentStatusAfterSpiService, never()).updatePaymentCancellationTppRedirectUri(DECRYPTED_PAYMENT_ID, tppRedirectUri);
    }

    @Test
    void updatePaymentCancellationInternalRequestId_success() {
        // Given
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.of(DECRYPTED_PAYMENT_ID));
        when(updatePaymentStatusAfterSpiService.updatePaymentCancellationInternalRequestId(DECRYPTED_PAYMENT_ID, INTERNAL_REQUEST_ID))
            .thenReturn(CmsResponse.<Boolean>builder()
                            .payload(true)
                            .build());

        // When
        CmsResponse<Boolean> actual = updatePaymentStatusAfterSpiServiceInternalEncrypted.updatePaymentCancellationInternalRequestId(ENCRYPTED_PAYMENT_ID, INTERNAL_REQUEST_ID);

        // Then
        assertTrue(actual.isSuccessful());

        assertTrue(actual.getPayload());
        verify(securityDataService, times(1)).decryptId(ENCRYPTED_PAYMENT_ID);
        verify(updatePaymentStatusAfterSpiService, times(1)).updatePaymentCancellationInternalRequestId(DECRYPTED_PAYMENT_ID, INTERNAL_REQUEST_ID);
    }
}
