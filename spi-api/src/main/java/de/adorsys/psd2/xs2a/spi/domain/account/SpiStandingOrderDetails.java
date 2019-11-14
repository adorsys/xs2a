/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.spi.domain.account;

import de.adorsys.psd2.xs2a.core.pis.FrequencyCode;
import de.adorsys.psd2.xs2a.core.pis.PisDayOfExecution;
import de.adorsys.psd2.xs2a.core.pis.PisExecutionRule;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
import lombok.Value;

import java.time.LocalDate;
import java.util.List;

@Value
public class SpiStandingOrderDetails {

    private LocalDate startDate;
    private LocalDate endDate;
    private PisExecutionRule executionRule;
    private Boolean withinAMonthFlag;
    private FrequencyCode frequency;
    private List<String> monthsOfExecution;
    private Integer multiplicator;
    private PisDayOfExecution dayOfExecution;
    private SpiAmount limitAmount;
}
