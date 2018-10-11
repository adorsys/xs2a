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
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayment;
import de.adorsys.aspsp.xs2a.spi.domain.v2.SpiSinglePayment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpiToXs2aPaymentMapper {
    private final SpiToXs2aAccountReferenceMapper spiToXs2aAccountReferenceMapper;
    private final SpiToXs2aAmountMapper spiToXs2aAmountMapper;
    private final SpiToXs2aAddressMapper spiToXs2aAddressMapper;

    public SinglePayment mapToSinglePayment(SpiSinglePayment payment) {
        SinglePayment single = new SinglePayment();
        single.setEndToEndIdentification(payment.getEndToEndIdentification());
        single.setDebtorAccount(spiToXs2aAccountReferenceMapper.mapToXs2aAccountReference(payment.getDebtorAccount()));
        single.setInstructedAmount(spiToXs2aAmountMapper.mapToXs2aAmount(payment.getInstructedAmount()));
        single.setCreditorAccount(spiToXs2aAccountReferenceMapper.mapToXs2aAccountReference(payment.getCreditorAccount()));
        single.setCreditorAgent(payment.getCreditorAgent());
        single.setCreditorName(payment.getCreditorName());
        single.setCreditorAddress(spiToXs2aAddressMapper.mapToAddress(payment.getCreditorAddress()));
        single.setRemittanceInformationUnstructured(payment.getRemittanceInformationUnstructured());
        single.setPaymentId(payment.getPaymentId());
        single.setTransactionStatus(Xs2aTransactionStatus.getByValue(payment.getPaymentStatus().getName()));
        return single;
    }
}
