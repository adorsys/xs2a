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

package de.adorsys.psd2.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * Exchange Rate
 */
@ApiModel(description = "Exchange Rate")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-01-11T12:48:04.675377+02:00[Europe/Kiev]")

public class ExchangeRate   {
  @JsonProperty("sourceCurrency")
  private String sourceCurrency = null;

  @JsonProperty("rate")
  private String rate = null;

  @JsonProperty("unitCurrency")
  private String unitCurrency = null;

  @JsonProperty("targetCurrency")
  private String targetCurrency = null;

  @JsonProperty("rateDate")
  private LocalDate rateDate = null;

  @JsonProperty("rateContract")
  private String rateContract = null;

  public ExchangeRate sourceCurrency(String sourceCurrency) {
    this.sourceCurrency = sourceCurrency;
    return this;
  }

  /**
   * Get sourceCurrency
   * @return sourceCurrency
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

@Pattern(regexp="[A-Z]{3}") 
  public String getSourceCurrency() {
    return sourceCurrency;
  }

  public void setSourceCurrency(String sourceCurrency) {
    this.sourceCurrency = sourceCurrency;
  }

  public ExchangeRate rate(String rate) {
    this.rate = rate;
    return this;
  }

  /**
   * Get rate
   * @return rate
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull


  public String getRate() {
    return rate;
  }

  public void setRate(String rate) {
    this.rate = rate;
  }

  public ExchangeRate unitCurrency(String unitCurrency) {
    this.unitCurrency = unitCurrency;
    return this;
  }

  /**
   * Get unitCurrency
   * @return unitCurrency
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull


  public String getUnitCurrency() {
    return unitCurrency;
  }

  public void setUnitCurrency(String unitCurrency) {
    this.unitCurrency = unitCurrency;
  }

  public ExchangeRate targetCurrency(String targetCurrency) {
    this.targetCurrency = targetCurrency;
    return this;
  }

  /**
   * Get targetCurrency
   * @return targetCurrency
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

@Pattern(regexp="[A-Z]{3}") 
  public String getTargetCurrency() {
    return targetCurrency;
  }

  public void setTargetCurrency(String targetCurrency) {
    this.targetCurrency = targetCurrency;
  }

  public ExchangeRate rateDate(LocalDate rateDate) {
    this.rateDate = rateDate;
    return this;
  }

  /**
   * Get rateDate
   * @return rateDate
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid

  public LocalDate getRateDate() {
    return rateDate;
  }

  public void setRateDate(LocalDate rateDate) {
    this.rateDate = rateDate;
  }

  public ExchangeRate rateContract(String rateContract) {
    this.rateContract = rateContract;
    return this;
  }

  /**
   * Get rateContract
   * @return rateContract
  **/
  @ApiModelProperty(value = "")


  public String getRateContract() {
    return rateContract;
  }

  public void setRateContract(String rateContract) {
    this.rateContract = rateContract;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ExchangeRate exchangeRate = (ExchangeRate) o;
    return Objects.equals(this.sourceCurrency, exchangeRate.sourceCurrency) &&
        Objects.equals(this.rate, exchangeRate.rate) &&
        Objects.equals(this.unitCurrency, exchangeRate.unitCurrency) &&
        Objects.equals(this.targetCurrency, exchangeRate.targetCurrency) &&
        Objects.equals(this.rateDate, exchangeRate.rateDate) &&
        Objects.equals(this.rateContract, exchangeRate.rateContract);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sourceCurrency, rate, unitCurrency, targetCurrency, rateDate, rateContract);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExchangeRate {\n");
    
    sb.append("    sourceCurrency: ").append(toIndentedString(sourceCurrency)).append("\n");
    sb.append("    rate: ").append(toIndentedString(rate)).append("\n");
    sb.append("    unitCurrency: ").append(toIndentedString(unitCurrency)).append("\n");
    sb.append("    targetCurrency: ").append(toIndentedString(targetCurrency)).append("\n");
    sb.append("    rateDate: ").append(toIndentedString(rateDate)).append("\n");
    sb.append("    rateContract: ").append(toIndentedString(rateContract)).append("\n");
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

