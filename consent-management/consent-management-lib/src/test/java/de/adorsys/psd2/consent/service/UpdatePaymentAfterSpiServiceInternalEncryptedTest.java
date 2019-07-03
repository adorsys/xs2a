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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.service.UpdatePaymentAfterSpiService;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UpdatePaymentAfterSpiServiceInternalEncryptedTest {
    private static final String ENCRYPTED_PAYMENT_ID = "encrypted payment id";
    private static final String WRONG_ENCRYPTED_PAYMENT_ID = "wrong encrypted payment id";
    private static final String UNDECRYPTABLE_PAYMENT_ID = "undecryptable payment id";
    private static final String DECRYPTED_PAYMENT_ID = "91cd2158-4344-44f4-bdbb-c736ededa436";
    private static final String WRONG_DECRYPTED_PAYMENT_ID = "386d670d-4323-43ba-a953-8d0c97d9deca";

    @InjectMocks
    private UpdatePaymentAfterSpiServiceInternalEncrypted updatePaymentStatusAfterSpiServiceInternalEncrypted;
    @Mock
    private UpdatePaymentAfterSpiService updatePaymentStatusAfterSpiService;
    @Mock
    private SecurityDataService securityDataService;
    private TppRedirectUri tppRedirectUri;

    @Before
    public void setUp() {
        tppRedirectUri = new TppRedirectUri("ok_url", "nok_url");
    }

    @Test
    public void updatePaymentStatus_success() {
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.of(DECRYPTED_PAYMENT_ID));
        when(updatePaymentStatusAfterSpiService.updatePaymentStatus(eq(DECRYPTED_PAYMENT_ID), any())).thenReturn(true);

        // When
        boolean actual = updatePaymentStatusAfterSpiServiceInternalEncrypted.updatePaymentStatus(ENCRYPTED_PAYMENT_ID, TransactionStatus.ACCP);

        // Then
        assertTrue(actual);
        verify(updatePaymentStatusAfterSpiService, times(1)).updatePaymentStatus(any(), any());
    }

    @Test
    public void updatePaymentStatus_failure_wrongPaymentId() {
        when(securityDataService.decryptId(WRONG_ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.of(WRONG_DECRYPTED_PAYMENT_ID));
        when(updatePaymentStatusAfterSpiService.updatePaymentStatus(eq(WRONG_DECRYPTED_PAYMENT_ID), any())).thenReturn(false);

        // When
        boolean actual = updatePaymentStatusAfterSpiServiceInternalEncrypted.updatePaymentStatus(WRONG_ENCRYPTED_PAYMENT_ID, TransactionStatus.ACCP);

        // Then
        assertFalse(actual);
        verify(updatePaymentStatusAfterSpiService, times(1)).updatePaymentStatus(any(), any());
    }

    @Test
    public void updatePaymentStatus_failure_decryptionFailed() {
        when(securityDataService.decryptId(UNDECRYPTABLE_PAYMENT_ID)).thenReturn(Optional.empty());

        // When
        boolean actual = updatePaymentStatusAfterSpiServiceInternalEncrypted.updatePaymentStatus(UNDECRYPTABLE_PAYMENT_ID, TransactionStatus.ACCP);

        // Then
        assertFalse(actual);
        verify(updatePaymentStatusAfterSpiService, never()).updatePaymentStatus(any(), any());
    }

    @Test
    public void updatePaymentCancellationTppRedirectUri_success() {
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.of(DECRYPTED_PAYMENT_ID));
        when(updatePaymentStatusAfterSpiService.updatePaymentCancellationTppRedirectUri(eq(DECRYPTED_PAYMENT_ID), eq(tppRedirectUri))).thenReturn(true);

        assertTrue(updatePaymentStatusAfterSpiServiceInternalEncrypted.updatePaymentCancellationTppRedirectUri(ENCRYPTED_PAYMENT_ID, tppRedirectUri));

        verify(securityDataService, times(1)).decryptId(ENCRYPTED_PAYMENT_ID);
        verify(updatePaymentStatusAfterSpiService, times(1)).updatePaymentCancellationTppRedirectUri(eq(DECRYPTED_PAYMENT_ID), eq(tppRedirectUri));
    }

    @Test
    public void updatePaymentCancellationTppRedirectUri_failure_wrongPaymentId() {
        when(securityDataService.decryptId(WRONG_ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.of(WRONG_DECRYPTED_PAYMENT_ID));
        when(updatePaymentStatusAfterSpiService.updatePaymentCancellationTppRedirectUri(eq(WRONG_DECRYPTED_PAYMENT_ID), eq(tppRedirectUri))).thenReturn(false);

        assertFalse(updatePaymentStatusAfterSpiServiceInternalEncrypted.updatePaymentCancellationTppRedirectUri(WRONG_ENCRYPTED_PAYMENT_ID, tppRedirectUri));

        verify(securityDataService, times(1)).decryptId(WRONG_ENCRYPTED_PAYMENT_ID);
        verify(updatePaymentStatusAfterSpiService, times(1)).updatePaymentCancellationTppRedirectUri(eq(WRONG_DECRYPTED_PAYMENT_ID), eq(tppRedirectUri));
    }

    @Test
    public void updatePaymentCancellationTppRedirectUri_failure_decryptionFailed() {
        when(securityDataService.decryptId(ENCRYPTED_PAYMENT_ID)).thenReturn(Optional.empty());

        assertFalse(updatePaymentStatusAfterSpiServiceInternalEncrypted.updatePaymentCancellationTppRedirectUri(ENCRYPTED_PAYMENT_ID, tppRedirectUri));

        verify(securityDataService, times(1)).decryptId(ENCRYPTED_PAYMENT_ID);
        verify(updatePaymentStatusAfterSpiService, never()).updatePaymentCancellationTppRedirectUri(eq(DECRYPTED_PAYMENT_ID), eq(tppRedirectUri));
    }
}
