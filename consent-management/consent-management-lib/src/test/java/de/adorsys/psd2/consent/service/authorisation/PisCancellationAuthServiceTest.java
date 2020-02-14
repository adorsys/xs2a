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

package de.adorsys.psd2.consent.service.authorisation;

import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.consent.domain.Authorisable;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.domain.payment.PisPaymentData;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.consent.repository.PisPaymentDataRepository;
import de.adorsys.psd2.consent.service.CorePaymentsConvertService;
import de.adorsys.psd2.consent.service.PisCommonPaymentConfirmationExpirationService;
import de.adorsys.psd2.consent.service.mapper.PisCommonPaymentMapper;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.pis.TransactionStatus.PATC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PisCancellationAuthServiceTest {
    private static final String PARENT_ID = "payment ID";
    public static final byte[] PAYMENT_DATA_BYTES = "data".getBytes();

    @InjectMocks
    private PisCancellationAuthService service;

    @Mock
    private PisPaymentDataRepository pisPaymentDataRepository;
    @Mock
    private PisCommonPaymentDataRepository pisCommonPaymentDataRepository;
    @Mock
    private PisCommonPaymentConfirmationExpirationService pisCommonPaymentConfirmationExpirationService;
    @Mock
    private PisCommonPaymentMapper pisCommonPaymentMapper;
    @Mock
    private CorePaymentsConvertService corePaymentsConvertService;

    @Test
    void getInteractableAuthorisationParent_success() {
        PisPaymentData pisPaymentData = new PisPaymentData();
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        pisCommonPaymentData.setTransactionStatus(PATC);
        pisCommonPaymentData.setPaymentType(PaymentType.SINGLE);
        PisPaymentData paymentData = new PisPaymentData();
        pisCommonPaymentData.setPayments(Collections.singletonList(paymentData));
        pisPaymentData.setPaymentData(pisCommonPaymentData);

        when(pisPaymentDataRepository.findByPaymentId(PARENT_ID))
            .thenReturn(Optional.of(Collections.singletonList(pisPaymentData)));

        PisPayment pisPayment = new PisPayment();
        when(pisCommonPaymentMapper.mapToPisPayment(paymentData)).thenReturn(pisPayment);
        when(corePaymentsConvertService.buildPaymentData(Collections.singletonList(pisPayment), PaymentType.SINGLE))
            .thenReturn(PAYMENT_DATA_BYTES);

        service.getNotFinalisedAuthorisationParent(PARENT_ID);

        verify(pisPaymentDataRepository, times(1)).findByPaymentId(PARENT_ID);
        verify(pisCommonPaymentMapper, times(1)).mapToPisPayment(paymentData);
        verify(corePaymentsConvertService, times(1)).buildPaymentData(Collections.singletonList(pisPayment), PaymentType.SINGLE);
        verify(pisCommonPaymentDataRepository, times(1)).save(pisCommonPaymentData);
    }

    @Test
    void getInteractableAuthorisationParent_paymentCommonDataOutOfTransactionStatus() {
        PisPaymentData pisPaymentData = new PisPaymentData();
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        pisCommonPaymentData.setTransactionStatus(TransactionStatus.CANC);
        pisCommonPaymentData.setPaymentType(PaymentType.SINGLE);
        PisPaymentData paymentData = new PisPaymentData();
        pisCommonPaymentData.setPayments(Collections.singletonList(paymentData));
        pisPaymentData.setPaymentData(pisCommonPaymentData);

        when(pisPaymentDataRepository.findByPaymentId(PARENT_ID))
            .thenReturn(Optional.of(Collections.singletonList(pisPaymentData)));

        service.getNotFinalisedAuthorisationParent(PARENT_ID);

        verify(pisPaymentDataRepository, times(1)).findByPaymentId(PARENT_ID);
        verify(pisCommonPaymentMapper, never()).mapToPisPayment(any());
        verify(corePaymentsConvertService, never()).buildPaymentData(any(), any());
        verify(pisCommonPaymentDataRepository, never()).save(any());
    }

    @Test
    void getInteractableAuthorisationParent_emptyPaymentCommonData() {
        when(pisPaymentDataRepository.findByPaymentId(PARENT_ID)).thenReturn(Optional.of(Collections.emptyList()));

        service.getNotFinalisedAuthorisationParent(PARENT_ID);

        verify(pisPaymentDataRepository, times(1)).findByPaymentId(PARENT_ID);
        verify(pisCommonPaymentConfirmationExpirationService, never()).checkAndUpdateOnConfirmationExpiration(any());
        verify(pisCommonPaymentMapper, never()).mapToPisPayment(any());
        verify(corePaymentsConvertService, never()).buildPaymentData(any(), any());
        verify(pisCommonPaymentDataRepository, never()).save(any());
    }

    @Test
    void getAuthorisationParent() {
        PisCommonPaymentData payment = new PisCommonPaymentData();
        when(pisCommonPaymentDataRepository.findByPaymentId(PARENT_ID)).thenReturn(Optional.of(payment));

        assertEquals(Optional.of(payment), service.getAuthorisationParent(PARENT_ID));

        verify(pisCommonPaymentDataRepository, times(1)).findByPaymentId(PARENT_ID);
    }

    @Test
    void checkAndUpdateOnConfirmationExpiration() {
        PisCommonPaymentData initialPayment = new PisCommonPaymentData();
        PisCommonPaymentData updatedPayment = new PisCommonPaymentData();
        when(pisCommonPaymentConfirmationExpirationService.checkAndUpdateOnConfirmationExpiration(initialPayment)).thenReturn(updatedPayment);

        Authorisable response = service.checkAndUpdateOnConfirmationExpiration(initialPayment);

        assertEquals(updatedPayment, response);
        verify(pisCommonPaymentConfirmationExpirationService).checkAndUpdateOnConfirmationExpiration(initialPayment);
    }

    @Test
    void isConfirmationExpired() {
        PisCommonPaymentData initialPayment = new PisCommonPaymentData();
        when(pisCommonPaymentConfirmationExpirationService.isConfirmationExpired(initialPayment)).thenReturn(true);

        boolean response = service.isConfirmationExpired(initialPayment);

        assertTrue(response);
        verify(pisCommonPaymentConfirmationExpirationService).isConfirmationExpired(initialPayment);
    }

    @Test
    void updateOnConfirmationExpiration() {
        PisCommonPaymentData initialPayment = new PisCommonPaymentData();
        PisCommonPaymentData updatedPayment = new PisCommonPaymentData();
        when(pisCommonPaymentConfirmationExpirationService.updateOnConfirmationExpiration(initialPayment)).thenReturn(updatedPayment);

        Authorisable response = service.updateOnConfirmationExpiration(initialPayment);

        assertEquals(updatedPayment, response);
        verify(pisCommonPaymentConfirmationExpirationService).updateOnConfirmationExpiration(initialPayment);
    }

    @Test
    void getAuthorisationType() {
        assertEquals(AuthorisationType.PIS_CANCELLATION, service.getAuthorisationType());
    }
}
