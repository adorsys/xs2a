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
 * Details of underlying standing orders.
 */
@Schema(description = "Details of underlying standing orders. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-10-26T13:16:54.081225+03:00[Europe/Kiev]")


public class StandingOrderDetails   {
  @JsonProperty("startDate")
  private LocalDate startDate = null;

  @JsonProperty("frequency")
  private FrequencyCode frequency = null;

  @JsonProperty("endDate")
  private LocalDate endDate = null;

  @JsonProperty("executionRule")
  private ExecutionRule executionRule = null;

  @JsonProperty("withinAMonthFlag")
  private Boolean withinAMonthFlag = null;

  @JsonProperty("monthsOfExecution")
  private MonthsOfExecution monthsOfExecution = null;

  @JsonProperty("multiplicator")
  private Integer multiplicator = null;

  @JsonProperty("dayOfExecution")
  private DayOfExecution dayOfExecution = null;

  @JsonProperty("limitAmount")
  private Amount limitAmount = null;

  public StandingOrderDetails startDate(LocalDate startDate) {
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

  public StandingOrderDetails frequency(FrequencyCode frequency) {
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

  public StandingOrderDetails endDate(LocalDate endDate) {
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

  public StandingOrderDetails executionRule(ExecutionRule executionRule) {
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

  public StandingOrderDetails withinAMonthFlag(Boolean withinAMonthFlag) {
    this.withinAMonthFlag = withinAMonthFlag;
      return this;
  }

    /**
     * This element is only used in case of frequency equals \"Monthly\".  If this element equals false it has no effect. If this element equals true, then the execution rule is overruled if the day of execution would fall into a different month using the execution rule.  Example: executionRule equals \"preceding\", dayOfExecution equals \"02\" and the second of a month is a Sunday.  In this case, the transaction date would be on the last day of the month before.  This would be overruled if withinAMonthFlag equals true and the payment is processed on Monday the third of the Month. Remark: This attribute is rarely supported in the market.
     *
     * @return withinAMonthFlag
     **/
    @Schema(description = "This element is only used in case of frequency equals \"Monthly\".  If this element equals false it has no effect. If this element equals true, then the execution rule is overruled if the day of execution would fall into a different month using the execution rule.  Example: executionRule equals \"preceding\", dayOfExecution equals \"02\" and the second of a month is a Sunday.  In this case, the transaction date would be on the last day of the month before.  This would be overruled if withinAMonthFlag equals true and the payment is processed on Monday the third of the Month. Remark: This attribute is rarely supported in the market. ")
    @JsonProperty("withinAMonthFlag")

    public Boolean isWithinAMonthFlag() {
    return withinAMonthFlag;
  }

  public void setWithinAMonthFlag(Boolean withinAMonthFlag) {
    this.withinAMonthFlag = withinAMonthFlag;
  }

  public StandingOrderDetails monthsOfExecution(MonthsOfExecution monthsOfExecution) {
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

  public StandingOrderDetails multiplicator(Integer multiplicator) {
      this.multiplicator = multiplicator;
      return this;
  }

    /**
     * This is multiplying the given frequency resulting the exact frequency, e.g. Frequency=weekly and multiplicator=3 means every 3 weeks. Remark: This attribute is rarely supported in the market.
     *
     * @return multiplicator
     **/
    @Schema(description = "This is multiplying the given frequency resulting the exact frequency, e.g. Frequency=weekly and multiplicator=3 means every 3 weeks. Remark: This attribute is rarely supported in the market. ")
    @JsonProperty("multiplicator")

  public Integer getMultiplicator() {
    return multiplicator;
  }

  public void setMultiplicator(Integer multiplicator) {
    this.multiplicator = multiplicator;
  }

  public StandingOrderDetails dayOfExecution(DayOfExecution dayOfExecution) {
      this.dayOfExecution = dayOfExecution;
      return this;
  }

    /**
     * Get dayOfExecution
     *
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

  public StandingOrderDetails limitAmount(Amount limitAmount) {
      this.limitAmount = limitAmount;
      return this;
  }

    /**
     * Get limitAmount
     *
     * @return limitAmount
     **/
    @Schema(description = "")
    @JsonProperty("limitAmount")

    @Valid
    public Amount getLimitAmount() {
    return limitAmount;
  }

  public void setLimitAmount(Amount limitAmount) {
    this.limitAmount = limitAmount;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    StandingOrderDetails standingOrderDetails = (StandingOrderDetails) o;
    return Objects.equals(this.startDate, standingOrderDetails.startDate) &&
        Objects.equals(this.frequency, standingOrderDetails.frequency) &&
        Objects.equals(this.endDate, standingOrderDetails.endDate) &&
        Objects.equals(this.executionRule, standingOrderDetails.executionRule) &&
        Objects.equals(this.withinAMonthFlag, standingOrderDetails.withinAMonthFlag) &&
        Objects.equals(this.monthsOfExecution, standingOrderDetails.monthsOfExecution) &&
        Objects.equals(this.multiplicator, standingOrderDetails.multiplicator) &&
        Objects.equals(this.dayOfExecution, standingOrderDetails.dayOfExecution) &&
        Objects.equals(this.limitAmount, standingOrderDetails.limitAmount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(startDate, frequency, endDate, executionRule, withinAMonthFlag, monthsOfExecution, multiplicator, dayOfExecution, limitAmount);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class StandingOrderDetails {\n");

    sb.append("    startDate: ").append(toIndentedString(startDate)).append("\n");
    sb.append("    frequency: ").append(toIndentedString(frequency)).append("\n");
    sb.append("    endDate: ").append(toIndentedString(endDate)).append("\n");
    sb.append("    executionRule: ").append(toIndentedString(executionRule)).append("\n");
    sb.append("    withinAMonthFlag: ").append(toIndentedString(withinAMonthFlag)).append("\n");
    sb.append("    monthsOfExecution: ").append(toIndentedString(monthsOfExecution)).append("\n");
    sb.append("    multiplicator: ").append(toIndentedString(multiplicator)).append("\n");
    sb.append("    dayOfExecution: ").append(toIndentedString(dayOfExecution)).append("\n");
    sb.append("    limitAmount: ").append(toIndentedString(limitAmount)).append("\n");
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
