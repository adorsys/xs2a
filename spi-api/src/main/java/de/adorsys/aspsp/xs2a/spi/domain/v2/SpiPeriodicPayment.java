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

import de.adorsys.aspsp.xs2a.spi.domain.code.SpiFrequencyCode;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentProduct;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

import static de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentType.PERIODIC;

@Data
@EqualsAndHashCode(callSuper = true)
public class SpiPeriodicPayment extends SpiSinglePayment {
    private LocalDate startDate;
    private LocalDate endDate;
    private String executionRule;
    private SpiFrequencyCode frequency;
    private int dayOfExecution;

    public SpiPeriodicPayment(SpiPaymentProduct paymentProduct) {
        super(paymentProduct);
    }

    @Override
    public final SpiPaymentType getPaymentType() {
        return PERIODIC;
    }
}
