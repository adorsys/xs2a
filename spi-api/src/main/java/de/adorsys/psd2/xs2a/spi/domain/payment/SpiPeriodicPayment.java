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

package de.adorsys.psd2.xs2a.spi.domain.payment;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SpiPeriodicPayment extends SpiSinglePayment {
    private LocalDate startDate;
    private LocalDate endDate;
    private SpiPisExecutionRule executionRule;
    private SpiFrequencyCode frequency;
    private SpiPisDayOfExecution dayOfExecution;
    private List<String> monthsOfExecution;
    private String contentType;

    public SpiPeriodicPayment(String paymentProduct) {
        super(paymentProduct);
    }

    @Override
    public final SpiPaymentType getPaymentType() {
        return SpiPaymentType.PERIODIC;
    }
}
