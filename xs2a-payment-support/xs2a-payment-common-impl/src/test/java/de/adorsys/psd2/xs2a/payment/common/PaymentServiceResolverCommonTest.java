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

package de.adorsys.psd2.xs2a.payment.common;

import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentCancellationRequest;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.service.payment.cancel.CancelPaymentService;
import de.adorsys.psd2.xs2a.service.payment.create.CreateCommonPaymentService;
import de.adorsys.psd2.xs2a.service.payment.create.CreatePaymentService;
import de.adorsys.psd2.xs2a.service.payment.read.ReadCommonPaymentService;
import de.adorsys.psd2.xs2a.service.payment.read.ReadPaymentService;
import de.adorsys.psd2.xs2a.service.payment.status.ReadCommonPaymentStatusService;
import de.adorsys.psd2.xs2a.service.payment.status.ReadPaymentStatusService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class PaymentServiceResolverCommonTest {
    @Mock
    private CreateCommonPaymentService createCommonPaymentService;
    @Mock
    private ReadCommonPaymentService readCommonPaymentService;
    @Mock
    private ReadCommonPaymentStatusService readCommonPaymentStatusService;
    @Mock
    private CancelPaymentService cancelCommonPaymentService;

    @InjectMocks
    private PaymentServiceResolverCommon paymentServiceResolverCommon;

    @Test
    void getCreatePaymentService() {
        // When
        CreatePaymentService actualService =
            paymentServiceResolverCommon.getCreatePaymentService(new PaymentInitiationParameters());

        // Then
        assertEquals(createCommonPaymentService, actualService);
    }

    @Test
    void getReadPaymentService() {
        // When
        ReadPaymentService actualService =
            paymentServiceResolverCommon.getReadPaymentService(new PisCommonPaymentResponse());

        // Then
        assertEquals(readCommonPaymentService, actualService);
    }

    @Test
    void getReadPaymentStatusService() {
        // When
        ReadPaymentStatusService actualService =
            paymentServiceResolverCommon.getReadPaymentStatusService(new PisCommonPaymentResponse());

        // Then
        assertEquals(readCommonPaymentStatusService, actualService);
    }

    @Test
    void getCancelPaymentService() {
        // Given
        PisPaymentCancellationRequest paymentCancellationRequest =
            new PisPaymentCancellationRequest(null, null, null, null, null);

        // When
        CancelPaymentService actualService =
            paymentServiceResolverCommon.getCancelPaymentService(paymentCancellationRequest);

        // Then
        assertEquals(cancelCommonPaymentService, actualService);
    }
}
