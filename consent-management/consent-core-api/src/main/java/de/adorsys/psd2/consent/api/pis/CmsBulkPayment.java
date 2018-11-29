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

import de.adorsys.psd2.consent.api.ais.CmsAccountReference;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CmsBulkPayment implements CmsPayment {

    private String paymentId;
    private boolean batchBookingPreferred;
    private CmsAccountReference debtorAccount;
    private LocalDate requestedExecutionDate;
    private TransactionStatus paymentStatus;
    private List<CmsSinglePayment> payments;
    private String paymentProduct;

    @Override
    public PaymentType getPaymentType() {
        return PaymentType.BULK;
    }

    @Override
    public String getPaymentProduct() {
        return paymentProduct;
    }
}
