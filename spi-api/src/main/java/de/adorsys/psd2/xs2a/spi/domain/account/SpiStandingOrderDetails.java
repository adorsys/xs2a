/*
 * Copyright 2018-2023 adorsys GmbH & Co KG
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
 * contact us at psd2@adorsys.com.
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
    @Deprecated // TODO: change with SpiPisExecutionRule in 14.8
    private PisExecutionRule executionRule;
    private Boolean withinAMonthFlag;
    @Deprecated // TODO: change with SpiFrequencyCode in 14.8
    private FrequencyCode frequency;
    private List<String> monthsOfExecution;
    private Integer multiplicator;
    @Deprecated // TODO: change with SpiPisDayOfExecution in 14.8
    private PisDayOfExecution dayOfExecution;
    private SpiAmount limitAmount;
}
