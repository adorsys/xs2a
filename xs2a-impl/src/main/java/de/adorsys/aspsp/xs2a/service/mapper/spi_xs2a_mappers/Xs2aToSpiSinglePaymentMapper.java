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

import de.adorsys.aspsp.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.core.profile.PaymentProduct;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Xs2aToSpiSinglePaymentMapper {
    private final Xs2aToSpiAmountMapper xs2aToSpiAmountMapper;
    private final Xs2aToSpiAddressMapper xs2aToSpiAddressMapper;
    private final Xs2aToSpiAccountReferenceMapper xs2aToSpiAccountReferenceMapper;

    public SpiSinglePayment mapToSpiSinglePayment(SinglePayment payment, PaymentProduct paymentProduct) {
        SpiSinglePayment single = new SpiSinglePayment(paymentProduct);
        single.setPaymentId(payment.getPaymentId());
        single.setEndToEndIdentification(payment.getEndToEndIdentification());
        single.setDebtorAccount(xs2aToSpiAccountReferenceMapper.mapToSpiAccountReference(payment.getDebtorAccount()));
        single.setInstructedAmount(xs2aToSpiAmountMapper.mapToSpiAmount(payment.getInstructedAmount()));
        single.setCreditorAccount(xs2aToSpiAccountReferenceMapper.mapToSpiAccountReference(payment.getCreditorAccount()));
        single.setCreditorAgent(payment.getCreditorAgent());
        single.setCreditorName(payment.getCreditorName());
        single.setCreditorAddress(xs2aToSpiAddressMapper.mapToSpiAddress(payment.getCreditorAddress()));
        single.setRemittanceInformationUnstructured(payment.getRemittanceInformationUnstructured());
        single.setPaymentStatus(SpiTransactionStatus.RCVD);
        return single;
    }
}
