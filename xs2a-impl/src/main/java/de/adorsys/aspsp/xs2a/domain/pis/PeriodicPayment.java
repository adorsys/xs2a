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

package de.adorsys.aspsp.xs2a.domain.pis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.adorsys.aspsp.xs2a.domain.code.Xs2aFrequencyCode;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = true)
public class PeriodicPayment extends SinglePayment {

    @NotNull
    private LocalDate startDate;
    private String executionRule;
    private LocalDate endDate;

    @NotNull
    private Xs2aFrequencyCode frequency;

    @Min(1)
    @Max(31)
    private int dayOfExecution; //Day here max 31

    @JsonIgnore
    public boolean areValidExecutionAndPeriodDates() {
        return isValidExecutionDateAndTime() && isValidPeriod();
    }

    @JsonIgnore
    private boolean isValidPeriod() {
        return isValidStartDate()
                   && Optional.ofNullable(this.endDate)
                          .map(d -> d.isAfter(this.startDate))
                          .orElse(true);
    }

    @JsonIgnore
    private boolean isValidStartDate() {
        return this.startDate.isEqual(ChronoLocalDate.from(LocalDate.now()))
                   || this.startDate.isAfter(ChronoLocalDate.from(LocalDate.now()));
    }
}
