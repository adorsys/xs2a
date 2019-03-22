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

import de.adorsys.psd2.xs2a.domain.code.Xs2aFrequencyCode;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPeriodicPayment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SpiToXs2aPeriodicPaymentMapper {
    private final SpiToXs2aAccountReferenceMapper spiToXs2aAccountReferenceMapper;
    private final SpiToXs2aAmountMapper spiToXs2aAmountMapper;
    private final SpiToXs2aAddressMapper spiToXs2aAddressMapper;

    public PeriodicPayment mapToXs2aPeriodicPayment(SpiPeriodicPayment payment) {
        return Optional.ofNullable(payment)
                   .map(p -> {
                       PeriodicPayment periodic = new PeriodicPayment();
                       periodic.setPaymentId(p.getPaymentId());
                       periodic.setEndToEndIdentification(p.getEndToEndIdentification());
                       periodic.setDebtorAccount(spiToXs2aAccountReferenceMapper.mapToXs2aAccountReference(p.getDebtorAccount()).orElse(null));
                       periodic.setInstructedAmount(spiToXs2aAmountMapper.mapToXs2aAmount(p.getInstructedAmount()));
                       periodic.setCreditorAccount(spiToXs2aAccountReferenceMapper.mapToXs2aAccountReference(p.getCreditorAccount()).orElse(null));
                       periodic.setCreditorAgent(p.getCreditorAgent());
                       periodic.setCreditorName(p.getCreditorName());
                       periodic.setCreditorAddress(spiToXs2aAddressMapper.mapToAddress(p.getCreditorAddress()));
                       periodic.setRemittanceInformationUnstructured(p.getRemittanceInformationUnstructured());
                       periodic.setStartDate(p.getStartDate());
                       periodic.setEndDate(p.getEndDate());
                       periodic.setExecutionRule(p.getExecutionRule());
                       periodic.setFrequency(Xs2aFrequencyCode.valueOf(p.getFrequency().name()));
                       periodic.setDayOfExecution(p.getDayOfExecution());
                       periodic.setTransactionStatus(payment.getPaymentStatus());
                       periodic.setStatusChangeTimestamp(payment.getStatusChangeTimestamp());
                       return periodic;
                   }).orElse(null);
    }
}
