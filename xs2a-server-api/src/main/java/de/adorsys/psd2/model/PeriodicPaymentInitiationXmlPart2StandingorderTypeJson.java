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

package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Objects;

/**
 * The body part 2 of a periodic payment initation request containes the execution related informations  of the periodic payment.
 */
@Schema(description = "The body part 2 of a periodic payment initation request containes the execution related informations  of the periodic payment. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-10-26T13:16:54.081225+03:00[Europe/Kiev]")


public class PeriodicPaymentInitiationXmlPart2StandingorderTypeJson   {
  @JsonProperty("startDate")
  private LocalDate startDate = null;

  @JsonProperty("endDate")
  private LocalDate endDate = null;

  @JsonProperty("executionRule")
  private ExecutionRule executionRule = null;

  @JsonProperty("frequency")
  private FrequencyCode frequency = null;

  @JsonProperty("dayOfExecution")
  private DayOfExecution dayOfExecution = null;

  @JsonProperty("monthsOfExecution")
  private MonthsOfExecution monthsOfExecution = null;

  public PeriodicPaymentInitiationXmlPart2StandingorderTypeJson startDate(LocalDate startDate) {
    this.startDate = startDate;
    return this;
  }

    /**
     * The first applicable day of execution starting from this date is the first payment.
     *
     * @return startDate
     **/
    @Schema(required = true, description = "The first applicable day of execution starting from this date is the first payment. ")
    @JsonProperty("startDate")
    @NotNull

    @Valid
    public LocalDate getStartDate() {
        return startDate;
    }

  public void setStartDate(LocalDate startDate) {
    this.startDate = startDate;
  }

  public PeriodicPaymentInitiationXmlPart2StandingorderTypeJson endDate(LocalDate endDate) {
    this.endDate = endDate;
    return this;
  }

    /**
     * The last applicable day of execution. If not given, it is an infinite standing order.
     *
     * @return endDate
     **/
    @Schema(description = "The last applicable day of execution. If not given, it is an infinite standing order. ")
    @JsonProperty("endDate")

    @Valid
    public LocalDate getEndDate() {
        return endDate;
  }

  public void setEndDate(LocalDate endDate) {
    this.endDate = endDate;
  }

  public PeriodicPaymentInitiationXmlPart2StandingorderTypeJson executionRule(ExecutionRule executionRule) {
    this.executionRule = executionRule;
      return this;
  }

    /**
     * Get executionRule
     *
     * @return executionRule
     **/
    @Schema(description = "")
    @JsonProperty("executionRule")

    @Valid
    public ExecutionRule getExecutionRule() {
        return executionRule;
  }

  public void setExecutionRule(ExecutionRule executionRule) {
    this.executionRule = executionRule;
  }

  public PeriodicPaymentInitiationXmlPart2StandingorderTypeJson frequency(FrequencyCode frequency) {
    this.frequency = frequency;
      return this;
  }

    /**
     * Get frequency
     *
     * @return frequency
     **/
    @Schema(required = true, description = "")
    @JsonProperty("frequency")
    @NotNull

    @Valid
    public FrequencyCode getFrequency() {
    return frequency;
  }

  public void setFrequency(FrequencyCode frequency) {
    this.frequency = frequency;
  }

  public PeriodicPaymentInitiationXmlPart2StandingorderTypeJson dayOfExecution(DayOfExecution dayOfExecution) {
    this.dayOfExecution = dayOfExecution;
    return this;
  }

  /**
   * Get dayOfExecution
   * @return dayOfExecution
   **/
  @Schema(description = "")
  @JsonProperty("dayOfExecution")

    @Valid
  public DayOfExecution getDayOfExecution() {
    return dayOfExecution;
  }

  public void setDayOfExecution(DayOfExecution dayOfExecution) {
    this.dayOfExecution = dayOfExecution;
  }

  public PeriodicPaymentInitiationXmlPart2StandingorderTypeJson monthsOfExecution(MonthsOfExecution monthsOfExecution) {
    this.monthsOfExecution = monthsOfExecution;
      return this;
  }

    /**
     * Get monthsOfExecution
     *
     * @return monthsOfExecution
     **/
    @Schema(description = "")
    @JsonProperty("monthsOfExecution")

    @Valid
    public MonthsOfExecution getMonthsOfExecution() {
    return monthsOfExecution;
  }

  public void setMonthsOfExecution(MonthsOfExecution monthsOfExecution) {
    this.monthsOfExecution = monthsOfExecution;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PeriodicPaymentInitiationXmlPart2StandingorderTypeJson periodicPaymentInitiationXmlPart2StandingorderTypeJson = (PeriodicPaymentInitiationXmlPart2StandingorderTypeJson) o;
    return Objects.equals(this.startDate, periodicPaymentInitiationXmlPart2StandingorderTypeJson.startDate) &&
        Objects.equals(this.endDate, periodicPaymentInitiationXmlPart2StandingorderTypeJson.endDate) &&
        Objects.equals(this.executionRule, periodicPaymentInitiationXmlPart2StandingorderTypeJson.executionRule) &&
        Objects.equals(this.frequency, periodicPaymentInitiationXmlPart2StandingorderTypeJson.frequency) &&
        Objects.equals(this.dayOfExecution, periodicPaymentInitiationXmlPart2StandingorderTypeJson.dayOfExecution) &&
        Objects.equals(this.monthsOfExecution, periodicPaymentInitiationXmlPart2StandingorderTypeJson.monthsOfExecution);
  }

  @Override
  public int hashCode() {
    return Objects.hash(startDate, endDate, executionRule, frequency, dayOfExecution, monthsOfExecution);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PeriodicPaymentInitiationXmlPart2StandingorderTypeJson {\n");

    sb.append("    startDate: ").append(toIndentedString(startDate)).append("\n");
    sb.append("    endDate: ").append(toIndentedString(endDate)).append("\n");
    sb.append("    executionRule: ").append(toIndentedString(executionRule)).append("\n");
    sb.append("    frequency: ").append(toIndentedString(frequency)).append("\n");
    sb.append("    dayOfExecution: ").append(toIndentedString(dayOfExecution)).append("\n");
    sb.append("    monthsOfExecution: ").append(toIndentedString(monthsOfExecution)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
