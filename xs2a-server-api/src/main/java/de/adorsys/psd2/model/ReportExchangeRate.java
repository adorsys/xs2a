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
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Exchange Rate.
 */
@Schema(description = "Exchange Rate.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-10-26T13:16:54.081225+03:00[Europe/Kiev]")


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
     * ISO 4217 Alpha 3 currency code.
     *
     * @return sourceCurrency
     **/
    @Schema(example = "EUR", required = true, description = "ISO 4217 Alpha 3 currency code. ")
    @JsonProperty("sourceCurrency")
    @NotNull

    @Pattern(regexp = "[A-Z]{3}")
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
     *
     * @return exchangeRate
     **/
    @Schema(required = true, description = "")
    @JsonProperty("exchangeRate")
    @NotNull

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
     * ISO 4217 Alpha 3 currency code.
     *
     * @return unitCurrency
     **/
    @Schema(example = "EUR", required = true, description = "ISO 4217 Alpha 3 currency code. ")
    @JsonProperty("unitCurrency")
    @NotNull

    @Pattern(regexp = "[A-Z]{3}")
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
     * ISO 4217 Alpha 3 currency code.
     *
     * @return targetCurrency
     **/
    @Schema(example = "EUR", required = true, description = "ISO 4217 Alpha 3 currency code. ")
    @JsonProperty("targetCurrency")
    @NotNull

    @Pattern(regexp = "[A-Z]{3}")
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
     *
     * @return quotationDate
     **/
    @Schema(required = true, description = "")
    @JsonProperty("quotationDate")
    @NotNull

    @Valid
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
     *
     * @return contractIdentification
     **/
    @Schema(description = "")
    @JsonProperty("contractIdentification")

    @Size(max = 35)
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
