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

package de.adorsys.aspsp.xs2a.spi.mapper;


import de.adorsys.psd2.xs2a.core.profile.PaymentProduct;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiBulkPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiBulkPaymentInitiationResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SpiBulkPaymentMapper {
    private final SpiSinglePaymentMapper spiSinglePaymentMapper;

    public de.adorsys.aspsp.xs2a.spi.domain.payment.SpiBulkPayment mapToAspspSpiBulkPayment(@NotNull SpiBulkPayment payment) {
        de.adorsys.aspsp.xs2a.spi.domain.payment.SpiBulkPayment bulk = new de.adorsys.aspsp.xs2a.spi.domain.payment.SpiBulkPayment();
        bulk.setDebtorAccount(payment.getDebtorAccount());
        bulk.setBatchBookingPreferred(payment.getBatchBookingPreferred());
        bulk.setRequestedExecutionDate(payment.getRequestedExecutionDate());
        bulk.setPayments(mapToListAspspSpiSinglePayment(payment));
        bulk.setPaymentStatus(SpiTransactionStatus.RCVD);
        return bulk;
    }

    public SpiBulkPaymentInitiationResponse mapToSpiBulkPaymentResponse(@NotNull de.adorsys.aspsp.xs2a.spi.domain.payment.SpiBulkPayment payment, PaymentProduct paymentProduct) {
        SpiBulkPaymentInitiationResponse spi = new SpiBulkPaymentInitiationResponse();
        spi.setPayments(mapToListSpiSinglePayments(payment.getPayments(), paymentProduct));
        spi.setPaymentId(payment.getPaymentId());

        if (payment.getPaymentId() == null) {
            spi.setTransactionStatus(SpiTransactionStatus.RJCT);
        } else {
            spi.setTransactionStatus(SpiTransactionStatus.RCVD);
        }

        return spi;
    }

    public SpiBulkPayment mapToSpiBulkPayment(@NotNull List<de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment> payments, PaymentProduct paymentProduct) {
        SpiBulkPayment bulk = new SpiBulkPayment();
        bulk.setPayments(mapToListSpiSinglePayments(payments, paymentProduct));
        return bulk;
    }

    private List<de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment> mapToListAspspSpiSinglePayment(@NotNull SpiBulkPayment payment) {
        return payment.getPayments().stream()
                   .map(spiSinglePaymentMapper::mapToAspspSpiSinglePayment)
                   .collect(Collectors.toList());
    }

    private List<SpiSinglePayment> mapToListSpiSinglePayments(@NotNull List<de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment> payments, PaymentProduct paymentProduct) {
        return payments.stream()
                   .map(p -> spiSinglePaymentMapper.mapToSpiSinglePayment(p, paymentProduct))
                   .collect(Collectors.toList());
    }
}
