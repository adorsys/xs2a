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

package de.adorsys.psd2.scheduler;

import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.repository.AisConsentRepository;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.consent.service.AisConsentConfirmationExpirationService;
import de.adorsys.psd2.consent.service.PisCommonPaymentConfirmationExpirationService;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

@RunWith(MockitoJUnitRunner.class)
public class NotConfirmedPaymentExpirationScheduleTaskTest {
    @InjectMocks
    private NotConfirmedPaymentExpirationScheduleTask scheduleTask;

    @Mock
    private PisCommonPaymentConfirmationExpirationService pisCommonPaymentConfirmationExpirationService;
    @Mock
    private PisCommonPaymentDataRepository paymentDataRepository;

    @Captor
    private ArgumentCaptor<ArrayList<PisCommonPaymentData>> commonPaymentDataCaptor;

    @Test
    public void obsoleteNotConfirmedPaymentIfExpired() {
        List<PisCommonPaymentData> pisCommonPaymentDataList = new ArrayList<>();
        pisCommonPaymentDataList.add(new PisCommonPaymentData());
        pisCommonPaymentDataList.add(new PisCommonPaymentData());

        when(paymentDataRepository.findByTransactionStatusIn(EnumSet.of(TransactionStatus.RCVD)))
            .thenReturn(pisCommonPaymentDataList);
        when(pisCommonPaymentConfirmationExpirationService.isPaymentDataOnConfirmationExpired(any(PisCommonPaymentData.class))).thenReturn(true, false);
        when(pisCommonPaymentConfirmationExpirationService.updatePaymentDataListOnConfirmationExpiration(commonPaymentDataCaptor.capture()))
            .thenReturn(Collections.emptyList());

        scheduleTask.obsoleteNotConfirmedPaymentIfExpired();

        verify(paymentDataRepository, times(1)).findByTransactionStatusIn(EnumSet.of(TransactionStatus.RCVD));
        verify(pisCommonPaymentConfirmationExpirationService, times(2)).isPaymentDataOnConfirmationExpired(any(PisCommonPaymentData.class));
        verify(pisCommonPaymentConfirmationExpirationService, times(1)).updatePaymentDataListOnConfirmationExpiration(anyList());

        assertEquals(1, commonPaymentDataCaptor.getValue().size());
    }

    @Test
    public void obsoleteNotConfirmedPaymentIfExpired_emptyList() {
        when(paymentDataRepository.findByTransactionStatusIn(EnumSet.of(TransactionStatus.RCVD)))
            .thenReturn(Collections.emptyList());

        scheduleTask.obsoleteNotConfirmedPaymentIfExpired();

        verify(paymentDataRepository, times(1)).findByTransactionStatusIn(EnumSet.of(TransactionStatus.RCVD));
        verify(pisCommonPaymentConfirmationExpirationService, never()).isPaymentDataOnConfirmationExpired(any(PisCommonPaymentData.class));
        verify(pisCommonPaymentConfirmationExpirationService, never()).updatePaymentDataListOnConfirmationExpiration(anyList());
    }
}
