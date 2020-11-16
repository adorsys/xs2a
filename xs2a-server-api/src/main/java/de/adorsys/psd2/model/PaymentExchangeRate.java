package de.adorsys.psd2.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * Exchange Rate.
 */
@ApiModel(description = "Exchange Rate.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-11-12T17:35:11.808068+02:00[Europe/Kiev]")

public class PaymentExchangeRate   {
  @JsonProperty("unitCurrency")
  private String unitCurrency = null;

  @JsonProperty("exchangeRate")
  private String exchangeRate = null;

  @JsonProperty("contractIdentification")
  private String contractIdentification = null;

  /**
   * Gets or Sets rateType
   */
  public enum RateTypeEnum {
    SPOT("SPOT"),

    SALE("SALE"),

    AGRD("AGRD");

    private String value;

    RateTypeEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static RateTypeEnum fromValue(String text) {
      for (RateTypeEnum b : RateTypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("rateType")
  private RateTypeEnum rateType = null;

  public PaymentExchangeRate unitCurrency(String unitCurrency) {
    this.unitCurrency = unitCurrency;
    return this;
  }

  /**
   * Get unitCurrency
   * @return unitCurrency
  **/
  @ApiModelProperty(value = "")



  @JsonProperty("unitCurrency")
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
   * @return exchangeRate
  **/
  @ApiModelProperty(value = "")



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

  public PaymentExchangeRate rateType(RateTypeEnum rateType) {
    this.rateType = rateType;
    return this;
  }

  /**
   * Get rateType
   * @return rateType
  **/
  @ApiModelProperty(value = "")



  @JsonProperty("rateType")
  public RateTypeEnum getRateType() {
    return rateType;
  }

  public void setRateType(RateTypeEnum rateType) {
    this.rateType = rateType;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
}    PaymentExchangeRate paymentExchangeRate = (PaymentExchangeRate) o;
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

