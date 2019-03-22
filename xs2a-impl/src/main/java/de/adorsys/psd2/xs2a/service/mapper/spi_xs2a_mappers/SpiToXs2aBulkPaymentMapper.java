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
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SpiToXs2aBulkPaymentMapper {
    private final SpiToXs2aAccountReferenceMapper spiToXs2aAccountReferenceMapper;
    private final SpiToXs2aSinglePaymentMapper spiToXs2aSinglePaymentMapper;

    public BulkPayment mapToXs2aBulkPayment(@NotNull SpiBulkPayment payment) {
        BulkPayment bulk = new BulkPayment();
        bulk.setPaymentId(payment.getPaymentId());
        bulk.setBatchBookingPreferred(payment.getBatchBookingPreferred());
        bulk.setRequestedExecutionDate(payment.getRequestedExecutionDate());
        bulk.setDebtorAccount(spiToXs2aAccountReferenceMapper.mapToXs2aAccountReference(payment.getDebtorAccount()).orElse(null));
        bulk.setTransactionStatus(payment.getPaymentStatus());
        bulk.setPayments(mapToListXs2aSinglePayments(payment.getPayments()));
        bulk.setStatusChangeTimestamp(payment.getStatusChangeTimestamp());
        return bulk;
    }

    private List<SinglePayment> mapToListXs2aSinglePayments(List<SpiSinglePayment> payments) {
        return Optional.ofNullable(payments)
                   .map(p -> p.stream().map(spiToXs2aSinglePaymentMapper::mapToXs2aSinglePayment).collect(Collectors.toList()))
                   .orElseGet(Collections::emptyList);
    }
}
