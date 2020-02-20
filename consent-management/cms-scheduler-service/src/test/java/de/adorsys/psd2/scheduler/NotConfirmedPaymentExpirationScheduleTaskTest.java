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

package de.adorsys.psd2.scheduler;

import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.consent.service.PisCommonPaymentConfirmationExpirationService;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotConfirmedPaymentExpirationScheduleTaskTest {

    @InjectMocks
    private NotConfirmedPaymentExpirationScheduleTask scheduleTask;

    @Mock
    private PisCommonPaymentConfirmationExpirationService pisCommonPaymentConfirmationExpirationService;
    @Mock
    private PisCommonPaymentDataRepository paymentDataRepository;

    @Captor
    private ArgumentCaptor<ArrayList<PisCommonPaymentData>> commonPaymentDataCaptor;

    @Test
    void obsoleteNotConfirmedPaymentIfExpired() {
        // Given
        List<PisCommonPaymentData> pisCommonPaymentDataList = new ArrayList<>();
        pisCommonPaymentDataList.add(new PisCommonPaymentData());
        pisCommonPaymentDataList.add(new PisCommonPaymentData());

        when(paymentDataRepository.findByTransactionStatusIn(EnumSet.of(TransactionStatus.RCVD, TransactionStatus.PATC)))
            .thenReturn(pisCommonPaymentDataList);
        when(pisCommonPaymentConfirmationExpirationService.isConfirmationExpired(any(PisCommonPaymentData.class)))
            .thenReturn(true, false);
        when(pisCommonPaymentConfirmationExpirationService.updatePaymentDataListOnConfirmationExpiration(commonPaymentDataCaptor.capture()))
            .thenReturn(Collections.emptyList());

        // When
        scheduleTask.obsoleteNotConfirmedPaymentIfExpired();

        // Then
        verify(paymentDataRepository, times(1)).findByTransactionStatusIn(EnumSet.of(TransactionStatus.RCVD, TransactionStatus.PATC));
        verify(pisCommonPaymentConfirmationExpirationService, times(2)).isConfirmationExpired(any(PisCommonPaymentData.class));
        verify(pisCommonPaymentConfirmationExpirationService, times(1)).updatePaymentDataListOnConfirmationExpiration(anyList());

        assertEquals(1, commonPaymentDataCaptor.getValue().size());
    }

    @Test
    void obsoleteNotConfirmedPaymentIfExpired_emptyList() {
        // Given
        when(paymentDataRepository.findByTransactionStatusIn(EnumSet.of(TransactionStatus.RCVD, TransactionStatus.PATC)))
            .thenReturn(Collections.emptyList());

        // When
        scheduleTask.obsoleteNotConfirmedPaymentIfExpired();

        // Then
        verify(paymentDataRepository, times(1)).findByTransactionStatusIn(EnumSet.of(TransactionStatus.RCVD, TransactionStatus.PATC));
        verify(pisCommonPaymentConfirmationExpirationService, never()).isConfirmationExpired(any(PisCommonPaymentData.class));
        verify(pisCommonPaymentConfirmationExpirationService, never()).updatePaymentDataListOnConfirmationExpiration(anyList());
    }
}
