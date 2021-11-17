package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.Objects;

/**
 * JSON Body of a establish signing basket request. The body shall contain at least one entry.
 */
@ApiModel(description = "JSON Body of a establish signing basket request. The body shall contain at least one entry. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-11-05T12:22:49.487689+02:00[Europe/Kiev]")

public class SigningBasket   {
  @JsonProperty("paymentIds")
  private PaymentIdList paymentIds = null;

  @JsonProperty("consentIds")
  private ConsentIdList consentIds = null;

  public SigningBasket paymentIds(PaymentIdList paymentIds) {
    this.paymentIds = paymentIds;
    return this;
  }

  /**
   * Get paymentIds
   * @return paymentIds
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("paymentIds")
  public PaymentIdList getPaymentIds() {
    return paymentIds;
  }

  public void setPaymentIds(PaymentIdList paymentIds) {
    this.paymentIds = paymentIds;
  }

  public SigningBasket consentIds(ConsentIdList consentIds) {
    this.consentIds = consentIds;
    return this;
  }

  /**
   * Get consentIds
   * @return consentIds
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("consentIds")
  public ConsentIdList getConsentIds() {
    return consentIds;
  }

  public void setConsentIds(ConsentIdList consentIds) {
    this.consentIds = consentIds;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
}    SigningBasket signingBasket = (SigningBasket) o;
    return Objects.equals(this.paymentIds, signingBasket.paymentIds) &&
    Objects.equals(this.consentIds, signingBasket.consentIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(paymentIds, consentIds);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SigningBasket {\n");

    sb.append("    paymentIds: ").append(toIndentedString(paymentIds)).append("\n");
    sb.append("    consentIds: ").append(toIndentedString(consentIds)).append("\n");
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

