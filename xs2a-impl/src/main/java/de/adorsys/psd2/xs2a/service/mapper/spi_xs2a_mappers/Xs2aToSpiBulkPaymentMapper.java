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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiBulkPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class Xs2aToSpiBulkPaymentMapper {
    private final Xs2aToSpiSinglePaymentMapper xs2aToSpiSinglePaymentMapper;
    private final Xs2aToSpiAccountReferenceMapper xs2aToSpiAccountReferenceMapper;
    private final Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper;

    public SpiBulkPayment mapToSpiBulkPayment(BulkPayment payment, String paymentProduct) {
        SpiBulkPayment bulk = new SpiBulkPayment();
        bulk.setPaymentId(payment.getPaymentId());
        bulk.setBatchBookingPreferred(payment.getBatchBookingPreferred());
        bulk.setDebtorAccount(xs2aToSpiAccountReferenceMapper.mapToSpiAccountReference(payment.getDebtorAccount()));
        if (payment.getTransactionStatus() != null) {
            bulk.setPaymentStatus(payment.getTransactionStatus());
        }
        bulk.setPaymentProduct(paymentProduct);
        bulk.setRequestedExecutionDate(payment.getRequestedExecutionDate());
        bulk.setPayments(mapToListSpiSinglePayment(payment.getPayments(), paymentProduct));
        bulk.setPsuDataList(xs2aToSpiPsuDataMapper.mapToSpiPsuDataList(payment.getPsuDataList()));
        bulk.setStatusChangeTimestamp(payment.getStatusChangeTimestamp());
        return bulk;
    }

    private List<SpiSinglePayment> mapToListSpiSinglePayment(List<SinglePayment> payments, String paymentProduct) {
        return payments.stream()
                   .map(p -> xs2aToSpiSinglePaymentMapper.mapToSpiSinglePayment(p, paymentProduct))
                   .collect(Collectors.toList());
    }
}
