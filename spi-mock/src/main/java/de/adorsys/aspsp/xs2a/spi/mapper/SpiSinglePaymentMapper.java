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

package de.adorsys.aspsp.xs2a.spi.mapper;

import de.adorsys.psd2.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiSinglePaymentInitiationResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
public class SpiSinglePaymentMapper {

    public de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment mapToSpiSinglePayment(@NotNull SpiSinglePayment payment) {
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

    public SpiSinglePaymentInitiationResponse mapToSpiSinglePaymentResponse(@NotNull de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment payment) {
        SpiSinglePaymentInitiationResponse spi = new SpiSinglePaymentInitiationResponse();
        spi.setPaymentId(payment.getPaymentId());
        if (payment.getPaymentId() == null) {
            spi.setTransactionStatus(SpiTransactionStatus.RJCT);
        } else {
            spi.setTransactionStatus(SpiTransactionStatus.RCVD);
        }
        return spi;
    }
}
