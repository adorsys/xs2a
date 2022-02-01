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

import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPeriodicPayment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Xs2aToSpiPeriodicPaymentMapper {
    private final Xs2aToSpiAmountMapper xs2aToSpiAmountMapper;
    private final Xs2aToSpiAddressMapper xs2aToSpiAddressMapper;
    private final Xs2aToSpiAccountReferenceMapper xs2aToSpiAccountReferenceMapper;
    private final Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper;

    public SpiPeriodicPayment mapToSpiPeriodicPayment(PeriodicPayment payment, String paymentProduct) {
        SpiPeriodicPayment periodic = new SpiPeriodicPayment(paymentProduct);
        periodic.setPaymentId(payment.getPaymentId());
        periodic.setEndToEndIdentification(payment.getEndToEndIdentification());
        periodic.setInstructionIdentification(payment.getInstructionIdentification());
        periodic.setDebtorAccount(xs2aToSpiAccountReferenceMapper.mapToSpiAccountReference(payment.getDebtorAccount()));
        periodic.setInstructedAmount(xs2aToSpiAmountMapper.mapToSpiAmount(payment.getInstructedAmount()));
        periodic.setCreditorAccount(xs2aToSpiAccountReferenceMapper.mapToSpiAccountReference(payment.getCreditorAccount()));
        periodic.setCreditorAgent(payment.getCreditorAgent());
        periodic.setCreditorName(payment.getCreditorName());
        periodic.setCreditorAddress(xs2aToSpiAddressMapper.mapToSpiAddress(payment.getCreditorAddress()));
        periodic.setRemittanceInformationUnstructured(payment.getRemittanceInformationUnstructured());
        periodic.setStartDate(payment.getStartDate());
        periodic.setEndDate(payment.getEndDate());
        periodic.setExecutionRule(payment.getExecutionRule());
        if (payment.getTransactionStatus() != null) {
            periodic.setPaymentStatus(payment.getTransactionStatus());
        }
        periodic.setFrequency(payment.getFrequency());
        periodic.setDayOfExecution(payment.getDayOfExecution());
        periodic.setMonthsOfExecution(payment.getMonthsOfExecution());
        periodic.setRequestedExecutionTime(payment.getRequestedExecutionTime());
        periodic.setRequestedExecutionDate(payment.getRequestedExecutionDate());
        periodic.setPsuDataList(xs2aToSpiPsuDataMapper.mapToSpiPsuDataList(payment.getPsuDataList()));
        periodic.setStatusChangeTimestamp(payment.getStatusChangeTimestamp());
        periodic.setUltimateDebtor(payment.getUltimateDebtor());
        periodic.setUltimateCreditor(payment.getUltimateCreditor());
        periodic.setPurposeCode(payment.getPurposeCode());
        periodic.setRemittanceInformationStructured(payment.getRemittanceInformationStructured());
        periodic.setRemittanceInformationStructuredArray(payment.getRemittanceInformationStructuredArray());
        periodic.setCreationTimestamp(payment.getCreationTimestamp());
        periodic.setContentType(payment.getContentType());
        periodic.setDebtorName(payment.getDebtorName());
        periodic.setInstanceId(payment.getInstanceId());

        return periodic;
    }
}
