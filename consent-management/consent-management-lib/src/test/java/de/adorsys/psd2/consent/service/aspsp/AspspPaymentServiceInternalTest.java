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

package de.adorsys.psd2.consent.service.aspsp;

import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.service.CommonPaymentDataService;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AspspPaymentServiceInternalTest {
    private static final String DEFAULT_SERVICE_INSTANCE_ID = "UNDEFINED";
    private static final String PAYMENT_ID = "payment id";
    private static final TransactionStatus TRANSACTION_STATUS = TransactionStatus.RJCT;

    private PisCommonPaymentData pisCommonPaymentData;

    @InjectMocks
    private AspspPaymentServiceInternal aspspPaymentServiceInternal;

    @Mock
    private CommonPaymentDataService commonPaymentDataService;

    @BeforeEach
    void setUp() {
        pisCommonPaymentData = buildPisCommonPaymentData();
    }

    @Test
    void updatePaymentStatus_Success() {
        // Given
        when(commonPaymentDataService.getPisCommonPaymentData(PAYMENT_ID, DEFAULT_SERVICE_INSTANCE_ID)).thenReturn(Optional.of(pisCommonPaymentData));
        when(commonPaymentDataService.updateStatusInPaymentData(pisCommonPaymentData, TRANSACTION_STATUS)).thenReturn(true);

        // When
        boolean actualResponse = aspspPaymentServiceInternal.updatePaymentStatus(PAYMENT_ID, TRANSACTION_STATUS, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertTrue(actualResponse);
    }

    @Test
    void updatePaymentStatus_Error() {
        // Given
        when(commonPaymentDataService.getPisCommonPaymentData(PAYMENT_ID, DEFAULT_SERVICE_INSTANCE_ID)).thenReturn(Optional.empty());

        // When
        boolean actualResponse = aspspPaymentServiceInternal.updatePaymentStatus(PAYMENT_ID, TRANSACTION_STATUS, DEFAULT_SERVICE_INSTANCE_ID);

        // Then
        assertFalse(actualResponse);
    }

    private PisCommonPaymentData buildPisCommonPaymentData() {
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        pisCommonPaymentData.setTransactionStatus(TransactionStatus.RCVD);
        pisCommonPaymentData.setPaymentType(PaymentType.SINGLE);
        pisCommonPaymentData.setPaymentId(PAYMENT_ID);
        pisCommonPaymentData.setCreationTimestamp(OffsetDateTime.of(2018, 10, 10, 10, 10, 10, 10, ZoneOffset.UTC));
        return pisCommonPaymentData;
    }
}
