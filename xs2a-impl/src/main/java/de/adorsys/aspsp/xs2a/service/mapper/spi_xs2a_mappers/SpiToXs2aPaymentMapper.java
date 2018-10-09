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

package de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.aspsp.xs2a.domain.Xs2aTransactionStatus;
import de.adorsys.aspsp.xs2a.domain.code.Xs2aFrequencyCode;
import de.adorsys.aspsp.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.aspsp.xs2a.spi.domain.v2.SpiPeriodicPayment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpiToXs2aPaymentMapper {
    private final SpiToXs2aAccountReferenceMapper spiToXs2aAccountReferenceMapper;
    private final SpiToXs2aAmountMapper spiToXs2aAmountMapper;
    private final SpiToXs2aAddressMapper spiToXs2aAddressMapper;

    public PeriodicPayment mapToXs2aPeriodicPayment(SpiPeriodicPayment payment) {
        PeriodicPayment periodic = new PeriodicPayment();

        periodic.setEndToEndIdentification(payment.getEndToEndIdentification());
        periodic.setDebtorAccount(spiToXs2aAccountReferenceMapper.mapToXs2aAccountReference(payment.getDebtorAccount()));
        periodic.setInstructedAmount(spiToXs2aAmountMapper.mapToXs2aAmount(payment.getInstructedAmount()));
        periodic.setCreditorAccount(spiToXs2aAccountReferenceMapper.mapToXs2aAccountReference(payment.getCreditorAccount()));
        periodic.setCreditorAgent(payment.getCreditorAgent());
        periodic.setCreditorName(payment.getCreditorName());
        periodic.setCreditorAddress(spiToXs2aAddressMapper.mapToXs2aAddress(payment.getCreditorAddress()));
        periodic.setRemittanceInformationUnstructured(payment.getRemittanceInformationUnstructured());
        periodic.setTransactionStatus(Xs2aTransactionStatus.RCVD);
        periodic.setStartDate(payment.getStartDate());
        periodic.setEndDate(payment.getEndDate());
        periodic.setExecutionRule(payment.getExecutionRule());
        periodic.setFrequency(Xs2aFrequencyCode.valueOf(payment.getFrequency().name()));
        periodic.setDayOfExecution(payment.getDayOfExecution());

        return periodic;
    }
}
