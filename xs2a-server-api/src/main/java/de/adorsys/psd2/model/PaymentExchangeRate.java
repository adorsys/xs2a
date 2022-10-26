/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
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
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Objects;

/**
 * Exchange Rate.
 */
@Schema(description = "Exchange Rate.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-10-26T13:16:54.081225+03:00[Europe/Kiev]")


public class PaymentExchangeRate   {
  @JsonProperty("unitCurrency")
  private String unitCurrency = null;

  @JsonProperty("exchangeRate")
  private String exchangeRate = null;

  @JsonProperty("contractIdentification")
  private String contractIdentification = null;

  @JsonProperty("rateType")
  private ExchangeRateTypeCode rateType = null;

  public PaymentExchangeRate unitCurrency(String unitCurrency) {
    this.unitCurrency = unitCurrency;
    return this;
  }

    /**
     * ISO 4217 Alpha 3 currency code.
     *
     * @return unitCurrency
     **/
    @Schema(example = "EUR", description = "ISO 4217 Alpha 3 currency code. ")
    @JsonProperty("unitCurrency")

    @Pattern(regexp = "[A-Z]{3}")
    public String getUnitCurrency() {
        return unitCurrency;
    }

  public void setUnitCurrency(String unitCurrency) {
    this.unitCurrency = unitCurrency;
  }

  public PaymentExchangeRate exchangeRate(String exchangeRate) {
    this.exchangeRate = exchangeRate;
    return this;
  }

    /**
     * Get exchangeRate
     *
     * @return exchangeRate
     **/
    @Schema(description = "")
    @JsonProperty("exchangeRate")

    public String getExchangeRate() {
        return exchangeRate;
  }

  public void setExchangeRate(String exchangeRate) {
    this.exchangeRate = exchangeRate;
  }

  public PaymentExchangeRate contractIdentification(String contractIdentification) {
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

  public PaymentExchangeRate rateType(ExchangeRateTypeCode rateType) {
    this.rateType = rateType;
      return this;
  }

    /**
     * Get rateType
     *
     * @return rateType
     **/
    @Schema(description = "")
    @JsonProperty("rateType")

    @Valid
    public ExchangeRateTypeCode getRateType() {
    return rateType;
  }

  public void setRateType(ExchangeRateTypeCode rateType) {
    this.rateType = rateType;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PaymentExchangeRate paymentExchangeRate = (PaymentExchangeRate) o;
    return Objects.equals(this.unitCurrency, paymentExchangeRate.unitCurrency) &&
        Objects.equals(this.exchangeRate, paymentExchangeRate.exchangeRate) &&
        Objects.equals(this.contractIdentification, paymentExchangeRate.contractIdentification) &&
        Objects.equals(this.rateType, paymentExchangeRate.rateType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(unitCurrency, exchangeRate, contractIdentification, rateType);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PaymentExchangeRate {\n");

    sb.append("    unitCurrency: ").append(toIndentedString(unitCurrency)).append("\n");
    sb.append("    exchangeRate: ").append(toIndentedString(exchangeRate)).append("\n");
    sb.append("    contractIdentification: ").append(toIndentedString(contractIdentification)).append("\n");
    sb.append("    rateType: ").append(toIndentedString(rateType)).append("\n");
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
