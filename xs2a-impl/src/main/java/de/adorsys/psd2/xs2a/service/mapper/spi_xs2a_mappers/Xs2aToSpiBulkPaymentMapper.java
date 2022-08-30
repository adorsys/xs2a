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
        bulk.setDebtorName(payment.getDebtorName());
        if (payment.getTransactionStatus() != null) {
            bulk.setPaymentStatus(payment.getTransactionStatus());
        }
        bulk.setPaymentProduct(paymentProduct);
        bulk.setRequestedExecutionDate(payment.getRequestedExecutionDate());
        bulk.setRequestedExecutionTime(payment.getRequestedExecutionTime());
        bulk.setPayments(mapToListSpiSinglePayment(payment.getPayments(), paymentProduct));
        bulk.setPsuDataList(xs2aToSpiPsuDataMapper.mapToSpiPsuDataList(payment.getPsuDataList()));
        bulk.setStatusChangeTimestamp(payment.getStatusChangeTimestamp());
        bulk.setCreationTimestamp(payment.getCreationTimestamp());
        bulk.setContentType(payment.getContentType());
        bulk.setInstanceId(payment.getInstanceId());
        return bulk;
    }

    private List<SpiSinglePayment> mapToListSpiSinglePayment(List<SinglePayment> payments, String paymentProduct) {
        return payments.stream()
                   .map(p -> xs2aToSpiSinglePaymentMapper.mapToSpiSinglePayment(p, paymentProduct))
                   .collect(Collectors.toList());
    }
}
