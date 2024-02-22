/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.web.mapper.RemittanceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class Xs2aToSpiSinglePaymentMapper {
    private final Xs2aToSpiAmountMapper xs2aToSpiAmountMapper;
    private final Xs2aToSpiAddressMapper xs2aToSpiAddressMapper;
    private final Xs2aToSpiAccountReferenceMapper xs2aToSpiAccountReferenceMapper;
    private final Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper;
    private final RemittanceMapper remittanceMapper;
    private final Xs2aToSpiPisMapper xs2aToSpiPisMapper;
    private final Xs2aToSpiTransactionMapper xs2aToSpiTransactionMapper;

    public SpiSinglePayment mapToSpiSinglePayment(SinglePayment payment, String paymentProduct) {
        SpiSinglePayment single = new SpiSinglePayment(paymentProduct);
        single.setPaymentId(payment.getPaymentId());
        single.setEndToEndIdentification(payment.getEndToEndIdentification());
        single.setInstructionIdentification(payment.getInstructionIdentification());
        single.setDebtorAccount(xs2aToSpiAccountReferenceMapper.mapToSpiAccountReference(payment.getDebtorAccount()));
        single.setInstructedAmount(xs2aToSpiAmountMapper.mapToSpiAmount(payment.getInstructedAmount()));
        single.setCreditorAccount(xs2aToSpiAccountReferenceMapper.mapToSpiAccountReference(payment.getCreditorAccount()));
        single.setPaymentStatus(xs2aToSpiTransactionMapper.mapToSpiTransactionStatus(payment.getTransactionStatus()));
        single.setCreditorAgent(payment.getCreditorAgent());
        single.setCreditorName(payment.getCreditorName());
        single.setCreditorAddress(xs2aToSpiAddressMapper.mapToSpiAddress(payment.getCreditorAddress()));
        single.setRequestedExecutionTime(payment.getRequestedExecutionTime());
        single.setRequestedExecutionDate(payment.getRequestedExecutionDate());
        single.setPsuDataList(xs2aToSpiPsuDataMapper.mapToSpiPsuDataList(payment.getPsuDataList()));
        single.setStatusChangeTimestamp(payment.getStatusChangeTimestamp());
        single.setUltimateDebtor(payment.getUltimateDebtor());
        single.setUltimateCreditor(payment.getUltimateCreditor());
        single.setPurposeCode(xs2aToSpiPisMapper.mapToSpiPisPurposeCode(payment.getPurposeCode()));
        single.setRemittanceInformationUnstructured(payment.getRemittanceInformationUnstructured());
        single.setRemittanceInformationUnstructuredArray(payment.getRemittanceInformationUnstructuredArray());
        single.setRemittanceInformationStructured(remittanceMapper.mapToSpiRemittance(payment.getRemittanceInformationStructured()));
        single.setRemittanceInformationStructuredArray(remittanceMapper.mapToSpiRemittanceArray(payment.getRemittanceInformationStructuredArray()));
        single.setCreationTimestamp(payment.getCreationTimestamp());
        single.setContentType(payment.getContentType());
        single.setDebtorName(payment.getDebtorName());
        single.setInstanceId(payment.getInstanceId());
        single.setChargeBearer(Optional.ofNullable(payment.getChargeBearer())
                                   .map(Enum::name)
                                   .orElse(null));

        return single;
    }
}
