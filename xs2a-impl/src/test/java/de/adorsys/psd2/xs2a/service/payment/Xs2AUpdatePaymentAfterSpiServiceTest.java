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


package de.adorsys.psd2.xs2a.service.payment;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.service.UpdatePaymentAfterSpiServiceEncrypted;
import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
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
            .thenReturn(CmsResponse.<Boolean>builder().payload(true).build());

        //When
        boolean actualResponse = xs2AUpdatePaymentAfterSpiService.updatePaymentStatus(PAYMENT_ID, TRANSACTION_STATUS);

        //Then
        assertThat(actualResponse).isTrue();
    }

    @Test
    void updatePaymentStatus_failed() {
        //Given
        when(updatePaymentStatusAfterSpiService.updatePaymentStatus(PAYMENT_ID, TRANSACTION_STATUS))
            .thenReturn(CmsResponse.<Boolean>builder().payload(false).build());

        //When
        boolean actualResponse = xs2AUpdatePaymentAfterSpiService.updatePaymentStatus(PAYMENT_ID, TRANSACTION_STATUS);

        //Then
        assertThat(actualResponse).isFalse();
    }

    @Test
    void updatePaymentStatus_success_shouldStoreTransactionStatusInLoggingContext() {
        //Given
        when(updatePaymentStatusAfterSpiService.updatePaymentStatus(PAYMENT_ID, TRANSACTION_STATUS))
            .thenReturn(CmsResponse.<Boolean>builder().payload(true).build());

        //When
        boolean actualResponse = xs2AUpdatePaymentAfterSpiService.updatePaymentStatus(PAYMENT_ID, TRANSACTION_STATUS);

        //Then
        assertThat(actualResponse).isTrue();
        verify(loggingContextService).storeTransactionStatus(TRANSACTION_STATUS);
    }

    @Test
    void updatePaymentStatus_failure_shouldNotStoreTransactionStatusInLoggingContext() {
        //Given
        when(updatePaymentStatusAfterSpiService.updatePaymentStatus(PAYMENT_ID, TRANSACTION_STATUS))
            .thenReturn(CmsResponse.<Boolean>builder().payload(false).build());

        //When
        boolean actualResponse = xs2AUpdatePaymentAfterSpiService.updatePaymentStatus(PAYMENT_ID, TRANSACTION_STATUS);

        //Then
        assertThat(actualResponse).isFalse();
        verify(loggingContextService, never()).storeTransactionStatus(any());
    }

    @Test
    void updatePaymentCancellationTppRedirectUri_success() {
        TppRedirectUri tppRedirectUri = new TppRedirectUri("ok.url", "nok.url");
        when(updatePaymentStatusAfterSpiService.updatePaymentCancellationTppRedirectUri(PAYMENT_ID, tppRedirectUri))
            .thenReturn(CmsResponse.<Boolean>builder().payload(true).build());

        boolean actualResponse = xs2AUpdatePaymentAfterSpiService.updatePaymentCancellationTppRedirectUri(PAYMENT_ID, tppRedirectUri);
        assertThat(actualResponse).isTrue();
    }

    @Test
    void updatePaymentCancellationTppRedirectUri_failed() {
        TppRedirectUri tppRedirectUri = new TppRedirectUri("ok.url", "nok.url");
        when(updatePaymentStatusAfterSpiService.updatePaymentCancellationTppRedirectUri(PAYMENT_ID, tppRedirectUri))
            .thenReturn(CmsResponse.<Boolean>builder().payload(false).build());

        boolean actualResponse = xs2AUpdatePaymentAfterSpiService.updatePaymentCancellationTppRedirectUri(PAYMENT_ID, tppRedirectUri);
        assertThat(actualResponse).isFalse();
    }
}
