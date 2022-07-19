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
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-05-06T13:00:42.214155+03:00[Europe/Kiev]")


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
   * @return unitCurrency
   **/
  @Schema(example = "EUR", description = "ISO 4217 Alpha 3 currency code. ")

  @Pattern(regexp="[A-Z]{3}")   public String getUnitCurrency() {
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
   * @return exchangeRate
   **/
  @Schema(description = "")

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
   * @return contractIdentification
   **/
  @Schema(description = "")

  @Size(max=35)   public String getContractIdentification() {
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
   * @return rateType
   **/
  @Schema(description = "")

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
