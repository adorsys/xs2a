/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.payment;

import de.adorsys.psd2.consent.api.service.UpdatePaymentAfterSpiServiceEncrypted;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.service.context.LoggingContextService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class Xs2AUpdatePaymentAfterSpiServiceTest {
    private static final TransactionStatus TRANSACTION_STATUS = TransactionStatus.ACSP;
    private static final String PAYMENT_ID = "d6cb50e5-bb88-4bbf-a5c1-42ee1ed1df2c";

    @InjectMocks
    private Xs2aUpdatePaymentAfterSpiService xs2AUpdatePaymentAfterSpiService;
    @Mock
    private UpdatePaymentAfterSpiServiceEncrypted updatePaymentStatusAfterSpiService;
    @Mock
    private LoggingContextService loggingContextService;


    @Test
    void updatePaymentStatus_success() {
        //Given
        when(updatePaymentStatusAfterSpiService.updatePaymentStatus(PAYMENT_ID, TRANSACTION_STATUS))
            .thenReturn(true);

        //When
        boolean actualResponse = xs2AUpdatePaymentAfterSpiService.updatePaymentStatus(PAYMENT_ID, TRANSACTION_STATUS);

        //Then
        assertThat(actualResponse).isTrue();
    }

    @Test
    void updatePaymentStatus_failed() {
        //Given

        //When
        boolean actualResponse = xs2AUpdatePaymentAfterSpiService.updatePaymentStatus(PAYMENT_ID, TRANSACTION_STATUS);

        //Then
        assertThat(actualResponse).isFalse();
    }

    @Test
    void updatePaymentStatus_success_shouldStoreTransactionStatusInLoggingContext() {
        //Given
        when(updatePaymentStatusAfterSpiService.updatePaymentStatus(PAYMENT_ID, TRANSACTION_STATUS))
            .thenReturn(true);

        //When
        boolean actualResponse = xs2AUpdatePaymentAfterSpiService.updatePaymentStatus(PAYMENT_ID, TRANSACTION_STATUS);

        //Then
        assertThat(actualResponse).isTrue();
        verify(loggingContextService).storeTransactionStatus(TRANSACTION_STATUS);
    }

    @Test
    void updatePaymentStatus_failure_shouldNotStoreTransactionStatusInLoggingContext() {
        //When
        boolean actualResponse = xs2AUpdatePaymentAfterSpiService.updatePaymentStatus(PAYMENT_ID, TRANSACTION_STATUS);

        //Then
        assertThat(actualResponse).isFalse();
        verify(loggingContextService, never()).storeTransactionStatus(any());
    }

    @Test
    void updatePaymentCancellationTppRedirectUri_success() {
        TppRedirectUri tppRedirectUri = new TppRedirectUri("ok.url", "nok.url");
        when(updatePaymentStatusAfterSpiService.updatePaymentCancellationTppRedirectUri(PAYMENT_ID, tppRedirectUri)).thenReturn(true);

        boolean actualResponse = xs2AUpdatePaymentAfterSpiService.updatePaymentCancellationTppRedirectUri(PAYMENT_ID, tppRedirectUri);
        assertThat(actualResponse).isTrue();
    }

    @Test
    void updatePaymentCancellationTppRedirectUri_failed() {
        TppRedirectUri tppRedirectUri = new TppRedirectUri("ok.url", "nok.url");
        when(updatePaymentStatusAfterSpiService.updatePaymentCancellationTppRedirectUri(PAYMENT_ID, tppRedirectUri)).thenReturn(false);

        boolean actualResponse = xs2AUpdatePaymentAfterSpiService.updatePaymentCancellationTppRedirectUri(PAYMENT_ID, tppRedirectUri);
        assertThat(actualResponse).isFalse();
    }
}
