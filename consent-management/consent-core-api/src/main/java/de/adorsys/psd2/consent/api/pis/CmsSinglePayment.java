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

package de.adorsys.psd2.consent.api.pis;

import de.adorsys.psd2.consent.api.CmsAddress;
import de.adorsys.psd2.consent.api.ais.CmsAccountReference;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
public class CmsSinglePayment extends BaseCmsPayment {
    private String endToEndIdentification;
    private CmsAccountReference debtorAccount;
    private CmsAmount instructedAmount;
    private CmsAccountReference creditorAccount;
    private String creditorAgent;
    private String creditorName;
    private CmsAddress creditorAddress;
    private String remittanceInformationUnstructured;
    private TransactionStatus paymentStatus;
    private LocalDate requestedExecutionDate;
    private OffsetDateTime requestedExecutionTime;

    public CmsSinglePayment(String paymentProduct) {
        setPaymentProduct(paymentProduct);
    }

    @Override
    public PaymentType getPaymentType() {
        return PaymentType.SINGLE;
    }

}
