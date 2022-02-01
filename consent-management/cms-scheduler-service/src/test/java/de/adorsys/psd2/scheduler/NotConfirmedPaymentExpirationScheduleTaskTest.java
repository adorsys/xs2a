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

package de.adorsys.psd2.scheduler;

import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.consent.service.PisCommonPaymentConfirmationExpirationService;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

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

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(scheduleTask, "pageSize", 100);
    }

    @Test
    void obsoleteNotConfirmedPaymentIfExpired() {
        // Given
        List<PisCommonPaymentData> pisCommonPaymentDataList = new ArrayList<>();
        pisCommonPaymentDataList.add(new PisCommonPaymentData());
        pisCommonPaymentDataList.add(new PisCommonPaymentData());

        when(paymentDataRepository.countByTransactionStatusIn(EnumSet.of(TransactionStatus.RCVD, TransactionStatus.PATC)))
            .thenReturn(10L);
        when(paymentDataRepository.findByTransactionStatusIn(EnumSet.of(TransactionStatus.RCVD, TransactionStatus.PATC), PageRequest.of(0, 100)))
            .thenReturn(pisCommonPaymentDataList);
        when(pisCommonPaymentConfirmationExpirationService.isConfirmationExpired(any(PisCommonPaymentData.class)))
            .thenReturn(true, false);
        when(pisCommonPaymentConfirmationExpirationService.updatePaymentDataListOnConfirmationExpiration(commonPaymentDataCaptor.capture()))
            .thenReturn(Collections.emptyList());

        // When
        scheduleTask.obsoleteNotConfirmedPaymentIfExpired();

        // Then
        verify(paymentDataRepository, times(1))
            .countByTransactionStatusIn(EnumSet.of(TransactionStatus.RCVD, TransactionStatus.PATC));
        verify(paymentDataRepository, times(1))
            .findByTransactionStatusIn(EnumSet.of(TransactionStatus.RCVD, TransactionStatus.PATC), PageRequest.of(0, 100));
        verify(pisCommonPaymentConfirmationExpirationService, times(2)).isConfirmationExpired(any(PisCommonPaymentData.class));
        verify(pisCommonPaymentConfirmationExpirationService, times(1)).updatePaymentDataListOnConfirmationExpiration(anyList());

        assertEquals(1, commonPaymentDataCaptor.getValue().size());
    }

    @Test
    void obsoleteNotConfirmedPaymentIfExpiredAndSigningBasketBlocked() {
        // Given
        List<PisCommonPaymentData> pisCommonPaymentDataList = new ArrayList<>();
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        pisCommonPaymentData.setSigningBasketBlocked(true);
        pisCommonPaymentDataList.add(pisCommonPaymentData);
        pisCommonPaymentDataList.add(new PisCommonPaymentData());
        pisCommonPaymentDataList.add(new PisCommonPaymentData());

        when(paymentDataRepository.countByTransactionStatusIn(EnumSet.of(TransactionStatus.RCVD, TransactionStatus.PATC)))
            .thenReturn(10L);
        when(paymentDataRepository.findByTransactionStatusIn(EnumSet.of(TransactionStatus.RCVD, TransactionStatus.PATC), PageRequest.of(0, 100)))
            .thenReturn(pisCommonPaymentDataList);
        when(pisCommonPaymentConfirmationExpirationService.isConfirmationExpired(any(PisCommonPaymentData.class)))
            .thenReturn(true, false);
        when(pisCommonPaymentConfirmationExpirationService.updatePaymentDataListOnConfirmationExpiration(commonPaymentDataCaptor.capture()))
            .thenReturn(Collections.emptyList());

        // When
        scheduleTask.obsoleteNotConfirmedPaymentIfExpired();

        // Then
        verify(paymentDataRepository, times(1))
            .countByTransactionStatusIn(EnumSet.of(TransactionStatus.RCVD, TransactionStatus.PATC));
        verify(paymentDataRepository, times(1))
            .findByTransactionStatusIn(EnumSet.of(TransactionStatus.RCVD, TransactionStatus.PATC), PageRequest.of(0, 100));
        verify(pisCommonPaymentConfirmationExpirationService, times(2)).isConfirmationExpired(any(PisCommonPaymentData.class));
        verify(pisCommonPaymentConfirmationExpirationService, times(1)).updatePaymentDataListOnConfirmationExpiration(anyList());

        assertEquals(1, commonPaymentDataCaptor.getValue().size());
    }

    @Test
    void obsoleteNotConfirmedPaymentIfExpired_emptyList() {
        // Given
        when(paymentDataRepository.countByTransactionStatusIn(EnumSet.of(TransactionStatus.RCVD, TransactionStatus.PATC)))
            .thenReturn(0L);

        // When
        scheduleTask.obsoleteNotConfirmedPaymentIfExpired();

        // Then
        verify(paymentDataRepository, times(1)).countByTransactionStatusIn(EnumSet.of(TransactionStatus.RCVD, TransactionStatus.PATC));
        verify(paymentDataRepository, never()).findByTransactionStatusIn(anySet(), any(Pageable.class));
        verify(pisCommonPaymentConfirmationExpirationService, never()).isConfirmationExpired(any(PisCommonPaymentData.class));
        verify(pisCommonPaymentConfirmationExpirationService, never()).updatePaymentDataListOnConfirmationExpiration(anyList());
    }
}
