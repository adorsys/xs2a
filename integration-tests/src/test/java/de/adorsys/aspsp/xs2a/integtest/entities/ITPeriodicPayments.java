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

package de.adorsys.aspsp.xs2a.integtest.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayment;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.Optional;

@Data
public class ITPeriodicPayments extends SinglePayment {

    private LocalDate startDate;
    private String executionRule;
    private LocalDate endDate;
    private String frequency;

    @Max(31)
    @Min(1)
    private int dayOfExecution; //Day here max 31

    @JsonIgnore
    public boolean isValidDate() {
        return isValidDate() && isValidStartDate() //TODO Should be removed with https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/167
                   &&
                   Optional.ofNullable(this.endDate)
                       .map(d -> d.isAfter(this.startDate))
                       .orElse(true);
    }

    @JsonIgnore
    private boolean isValidStartDate() { //TODO Should be removed with https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/167
        return this.startDate.isEqual(ChronoLocalDate.from(LocalDate.now()))
                   || this.startDate.isAfter(ChronoLocalDate.from(LocalDate.now()));
    }
}
