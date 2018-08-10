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
import de.adorsys.aspsp.xs2a.domain.code.FrequencyCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.Optional;

@Data
@ApiModel(description = "Periodic Payment Initialisation Request", value = "Periodic Payment")
public class PeriodicPayment extends SinglePayment {

    @ApiModelProperty(name = "startDate", required = true, example = "2019-03-03")
    @FutureOrPresent
    private LocalDate startDate;

    @ApiModelProperty(name = "executionRule", required = false, example = "preceeding")
    private String executionRule;

    @ApiModelProperty(name = "endDate", required = false, example = "2019-05-05")
    private LocalDate endDate;

    @ApiModelProperty(name = "frequency", required = true, example = "ANNUAL")
    private FrequencyCode frequency;

    @ApiModelProperty(name = "dayOfExecution", required = false, example = "14")
    @Max(31)
    @Min(1)
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
