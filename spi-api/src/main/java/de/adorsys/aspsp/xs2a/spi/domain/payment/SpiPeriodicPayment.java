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

package de.adorsys.aspsp.xs2a.spi.domain.payment;

import lombok.Data;

import java.time.LocalDate;

/**
 * @deprecated since 1.8. Will be removed in 1.10
 * @see de.adorsys.psd2.xs2a.spi.domain.payment.SpiPeriodicPayment
 */
@Data
public class SpiPeriodicPayment extends SpiSinglePayment {

    private LocalDate startDate;
    private LocalDate endDate;
    private String executionRule;
    private String frequency; // TODO consider using an enum similar to FrequencyCode based on the the "EventFrequency7Code" of ISO 20022
    private int dayOfExecution; //Day here max 31
}
