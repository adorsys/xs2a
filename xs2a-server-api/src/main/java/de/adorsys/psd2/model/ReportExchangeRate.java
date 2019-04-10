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

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Exchange Rate
 */
@ApiModel(description = "Exchange Rate")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-04-08T13:20:46.558844+03:00[Europe/Kiev]")

public class ReportExchangeRate   {
  @JsonProperty("sourceCurrency")
  private String sourceCurrency = null;

  @JsonProperty("exchangeRate")
  private String exchangeRate = null;

  @JsonProperty("unitCurrency")
  private String unitCurrency = null;

  @JsonProperty("targetCurrency")
  private String targetCurrency = null;

  @JsonProperty("quotationDate")
  private LocalDate quotationDate = null;

  @JsonProperty("contractIdentification")
  private String contractIdentification = null;

  public ReportExchangeRate sourceCurrency(String sourceCurrency) {
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

  @JsonProperty("sourceCurrency")
  public String getSourceCurrency() {
    return sourceCurrency;
  }

  public void setSourceCurrency(String sourceCurrency) {
    this.sourceCurrency = sourceCurrency;
  }

  public ReportExchangeRate exchangeRate(String exchangeRate) {
    this.exchangeRate = exchangeRate;
    return this;
  }

  /**
   * Get exchangeRate
   * @return exchangeRate
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull



  @JsonProperty("exchangeRate")
  public String getExchangeRate() {
    return exchangeRate;
  }

  public void setExchangeRate(String exchangeRate) {
    this.exchangeRate = exchangeRate;
  }

  public ReportExchangeRate unitCurrency(String unitCurrency) {
    this.unitCurrency = unitCurrency;
    return this;
  }

  /**
   * Get unitCurrency
   * @return unitCurrency
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull



  @JsonProperty("unitCurrency")
  public String getUnitCurrency() {
    return unitCurrency;
  }

  public void setUnitCurrency(String unitCurrency) {
    this.unitCurrency = unitCurrency;
  }

  public ReportExchangeRate targetCurrency(String targetCurrency) {
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

  @JsonProperty("targetCurrency")
  public String getTargetCurrency() {
    return targetCurrency;
  }

  public void setTargetCurrency(String targetCurrency) {
    this.targetCurrency = targetCurrency;
  }

  public ReportExchangeRate quotationDate(LocalDate quotationDate) {
    this.quotationDate = quotationDate;
    return this;
  }

  /**
   * Get quotationDate
   * @return quotationDate
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid


  @JsonProperty("quotationDate")
  public LocalDate getQuotationDate() {
    return quotationDate;
  }

  public void setQuotationDate(LocalDate quotationDate) {
    this.quotationDate = quotationDate;
  }

  public ReportExchangeRate contractIdentification(String contractIdentification) {
    this.contractIdentification = contractIdentification;
    return this;
  }

  /**
   * Get contractIdentification
   * @return contractIdentification
  **/
  @ApiModelProperty(value = "")



  @JsonProperty("contractIdentification")
  public String getContractIdentification() {
    return contractIdentification;
  }

  public void setContractIdentification(String contractIdentification) {
    this.contractIdentification = contractIdentification;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ReportExchangeRate reportExchangeRate = (ReportExchangeRate) o;
    return Objects.equals(this.sourceCurrency, reportExchangeRate.sourceCurrency) &&
        Objects.equals(this.exchangeRate, reportExchangeRate.exchangeRate) &&
        Objects.equals(this.unitCurrency, reportExchangeRate.unitCurrency) &&
        Objects.equals(this.targetCurrency, reportExchangeRate.targetCurrency) &&
        Objects.equals(this.quotationDate, reportExchangeRate.quotationDate) &&
        Objects.equals(this.contractIdentification, reportExchangeRate.contractIdentification);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sourceCurrency, exchangeRate, unitCurrency, targetCurrency, quotationDate, contractIdentification);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ReportExchangeRate {\n");
    
    sb.append("    sourceCurrency: ").append(toIndentedString(sourceCurrency)).append("\n");
    sb.append("    exchangeRate: ").append(toIndentedString(exchangeRate)).append("\n");
    sb.append("    unitCurrency: ").append(toIndentedString(unitCurrency)).append("\n");
    sb.append("    targetCurrency: ").append(toIndentedString(targetCurrency)).append("\n");
    sb.append("    quotationDate: ").append(toIndentedString(quotationDate)).append("\n");
    sb.append("    contractIdentification: ").append(toIndentedString(contractIdentification)).append("\n");
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

