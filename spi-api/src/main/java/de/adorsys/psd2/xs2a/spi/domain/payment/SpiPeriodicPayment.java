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

package de.adorsys.psd2.xs2a.spi.domain.payment;

import de.adorsys.psd2.xs2a.core.profile.PaymentProduct;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.spi.domain.code.SpiFrequencyCode;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class SpiPeriodicPayment extends SpiSinglePayment {
    private LocalDate startDate;
    private LocalDate endDate;
    private String executionRule;
    private SpiFrequencyCode frequency;
    private int dayOfExecution;

    public SpiPeriodicPayment(PaymentProduct paymentProduct) {
        super(paymentProduct);
    }

    @Override
    public final PaymentType getPaymentType() {
        return PaymentType.PERIODIC;
    }
}
