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

package de.adorsys.psd2.consent.service.authorisation;

import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.consent.domain.Authorisable;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.domain.payment.PisPaymentData;
import de.adorsys.psd2.consent.service.ConfirmationExpirationService;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.pis.TransactionStatus.PATC;
import static de.adorsys.psd2.xs2a.core.pis.TransactionStatus.RCVD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PisAuthServiceTest {
    private static final String PARENT_ID = "payment ID";
    public static final byte[] PAYMENT_DATA_BYTES = "data".getBytes();

    @InjectMocks
    private PisAuthService service;

    @Mock
    private PsuService psuService;
    @Mock
    private AspspProfileService aspspProfileService;
    @Mock
    private AuthorisationService authorisationService;
    @Mock
    private ConfirmationExpirationService<PisCommonPaymentData> confirmationExpirationService;
    @Mock
    private CommonPaymentService commonPaymentService;

    @Test
    void getInteractableAuthorisationParent_success() {
        PisPaymentData pisPaymentData = new PisPaymentData();
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        pisCommonPaymentData.setTransactionStatus(PATC);
        pisCommonPaymentData.setPaymentType(PaymentType.SINGLE);
        PisPaymentData paymentData = new PisPaymentData();
        pisCommonPaymentData.setPayments(Collections.singletonList(paymentData));
        pisPaymentData.setPaymentData(pisCommonPaymentData);

        when(commonPaymentService.findByPaymentIdAndPaymentDataTransactionStatusIn(PARENT_ID, Arrays.asList(RCVD, PATC)))
            .thenReturn(Optional.of(Collections.singletonList(pisPaymentData)));
        when(commonPaymentService.checkAndUpdateOnConfirmationExpiration(pisCommonPaymentData))
            .thenReturn(pisCommonPaymentData);

        PisPayment pisPayment = new PisPayment();
        when(commonPaymentService.mapToPisPayment(paymentData)).thenReturn(pisPayment);
        when(commonPaymentService.buildPaymentData(Collections.singletonList(pisPayment), PaymentType.SINGLE))
            .thenReturn(PAYMENT_DATA_BYTES);

        service.getNotFinalisedAuthorisationParent(PARENT_ID);

        verify(commonPaymentService, times(1)).findByPaymentIdAndPaymentDataTransactionStatusIn(PARENT_ID, Arrays.asList(RCVD, PATC));
        verify(commonPaymentService, times(1)).checkAndUpdateOnConfirmationExpiration(pisCommonPaymentData);
        verify(commonPaymentService, times(1)).mapToPisPayment(paymentData);
        verify(commonPaymentService, times(1)).buildPaymentData(Collections.singletonList(pisPayment), PaymentType.SINGLE);
        verify(commonPaymentService, times(1)).save(pisCommonPaymentData);
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

        when(commonPaymentService.findByPaymentIdAndPaymentDataTransactionStatusIn(PARENT_ID, Arrays.asList(RCVD, PATC)))
            .thenReturn(Optional.of(Collections.singletonList(pisPaymentData)));
        when(commonPaymentService.checkAndUpdateOnConfirmationExpiration(pisCommonPaymentData))
            .thenReturn(pisCommonPaymentData);

        service.getNotFinalisedAuthorisationParent(PARENT_ID);

        verify(commonPaymentService, times(1)).findByPaymentIdAndPaymentDataTransactionStatusIn(PARENT_ID, Arrays.asList(RCVD, PATC));
        verify(commonPaymentService, times(1)).checkAndUpdateOnConfirmationExpiration(pisCommonPaymentData);
        verify(commonPaymentService, never()).mapToPisPayment(any());
        verify(commonPaymentService, never()).buildPaymentData(any(), any());
        verify(commonPaymentService, never()).save(any());
    }

    @Test
    void getInteractableAuthorisationParent_emptyPaymentCommonData() {
        when(commonPaymentService.findByPaymentIdAndPaymentDataTransactionStatusIn(PARENT_ID, Arrays.asList(RCVD, PATC)))
            .thenReturn(Optional.of(Collections.emptyList()));

        service.getNotFinalisedAuthorisationParent(PARENT_ID);

        verify(commonPaymentService, times(1)).findByPaymentIdAndPaymentDataTransactionStatusIn(PARENT_ID, Arrays.asList(RCVD, PATC));
        verify(commonPaymentService, never()).checkAndUpdateOnConfirmationExpiration(any());
        verify(commonPaymentService, never()).mapToPisPayment(any());
        verify(commonPaymentService, never()).buildPaymentData(any(), any());
        verify(commonPaymentService, never()).save(any());
    }

    @Test
    void getAuthorisationParent() {
        PisCommonPaymentData payment = new PisCommonPaymentData();
        when(commonPaymentService.findOneByPaymentId(PARENT_ID)).thenReturn(Optional.of(payment));

        assertEquals(Optional.of(payment), service.getAuthorisationParent(PARENT_ID));

        verify(commonPaymentService, times(1)).findOneByPaymentId(PARENT_ID);
    }

    @Test
    void checkAndUpdateOnConfirmationExpiration() {
        PisCommonPaymentData initialPayment = new PisCommonPaymentData();
        PisCommonPaymentData updatedPayment = new PisCommonPaymentData();
        when(confirmationExpirationService.checkAndUpdateOnConfirmationExpiration(initialPayment)).thenReturn(updatedPayment);

        Authorisable response = service.checkAndUpdateOnConfirmationExpiration(initialPayment);

        assertEquals(updatedPayment, response);
        verify(confirmationExpirationService).checkAndUpdateOnConfirmationExpiration(initialPayment);
    }

    @Test
    void isConfirmationExpired() {
        PisCommonPaymentData initialPayment = new PisCommonPaymentData();
        when(confirmationExpirationService.isConfirmationExpired(initialPayment)).thenReturn(true);

        boolean response = service.isConfirmationExpired(initialPayment);

        assertTrue(response);
        verify(confirmationExpirationService).isConfirmationExpired(initialPayment);
    }

    @Test
    void updateOnConfirmationExpiration() {
        PisCommonPaymentData initialPayment = new PisCommonPaymentData();
        PisCommonPaymentData updatedPayment = new PisCommonPaymentData();
        when(confirmationExpirationService.updateOnConfirmationExpiration(initialPayment)).thenReturn(updatedPayment);

        Authorisable response = service.updateOnConfirmationExpiration(initialPayment);

        assertEquals(updatedPayment, response);
        verify(confirmationExpirationService).updateOnConfirmationExpiration(initialPayment);
    }

    @Test
    void getAuthorisationType() {
        assertEquals(AuthorisationType.PIS_CREATION, service.getAuthorisationType());
    }
}
