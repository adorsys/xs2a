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

package de.adorsys.aspsp.xs2a.spi.mapper.v2;

import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentProduct;
import de.adorsys.aspsp.xs2a.spi.domain.v2.SpiSinglePayment;
import org.springframework.stereotype.Component;

import java.util.Optional;

// TODO need to be renamed after removing previous version of SpiPaymentMapper
@Component
public class NewSpiPaymentMapper {

    public de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment mapToSpiSinglePayment(SpiSinglePayment payment) {
        de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment single = new de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment();
        single.setEndToEndIdentification(payment.getEndToEndIdentification());
        single.setDebtorAccount(payment.getDebtorAccount());
        single.setInstructedAmount(payment.getInstructedAmount());
        single.setCreditorAccount(payment.getCreditorAccount());
        single.setCreditorAgent(payment.getCreditorAgent());
        single.setCreditorName(payment.getCreditorName());
        single.setCreditorAddress(payment.getCreditorAddress());
        single.setRemittanceInformationUnstructured(payment.getRemittanceInformationUnstructured());
        single.setPaymentStatus(SpiTransactionStatus.RCVD);
        return single;

    }

    public SpiSinglePayment mapToSpiSinglePayment(de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment payment, SpiPaymentProduct paymentProduct) {
        return Optional.ofNullable(payment)
                   .map(p -> {
                       SpiSinglePayment single = new SpiSinglePayment(paymentProduct);
                       single.setPaymentId(p.getPaymentId());
                       single.setEndToEndIdentification(p.getEndToEndIdentification());
                       single.setDebtorAccount(p.getDebtorAccount());
                       single.setInstructedAmount(p.getInstructedAmount());
                       single.setCreditorAccount(p.getCreditorAccount());
                       single.setCreditorAgent(p.getCreditorAgent());
                       single.setCreditorName(p.getCreditorName());
                       single.setCreditorAddress(p.getCreditorAddress());
                       single.setRemittanceInformationUnstructured(p.getRemittanceInformationUnstructured());
                       single.setPaymentStatus(SpiTransactionStatus.RCVD);
                       return single;
                   })
                   .orElse(null);
    }
}
