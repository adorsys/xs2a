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

package de.adorsys.aspsp.xs2a.spi.domain.v2;

import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiAddress;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentProduct;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentType;
import de.adorsys.aspsp.xs2a.spi.service.v2.SpiPayment;
import lombok.Data;

import static de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentType.SINGLE;

@Data
class SpiSinglePayment implements SpiPayment {
    private String paymentId;
    private String endToEndIdentification;
    private SpiAccountReference debtorAccount;
    private SpiAmount instructedAmount;
    private SpiAccountReference creditorAccount;
    private String creditorAgent;
    private String creditorName;
    private SpiAddress creditorAddress;
    private String remittanceInformationUnstructured;
    private SpiTransactionStatus paymentStatus;
    protected SpiPaymentProduct paymentProduct;

    public SpiSinglePayment(SpiPaymentProduct paymentProduct) {
        this.paymentProduct = paymentProduct;
    }

    @Override
    public SpiPaymentType getPaymentType() {
        return SINGLE;
    }

    @Override
    public SpiPaymentProduct getPaymentProduct() {
        return paymentProduct;
    }
}
