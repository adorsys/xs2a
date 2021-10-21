package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Objects;

/**
 * Reference to an account by either:   * IBAN, of a payment accounts, or   * BBAN, for payment accounts if there is no IBAN, or    * the Primary Account Number (PAN) of a card, can be tokenised by the ASPSP due to PCI DSS requirements, or   * the Primary Account Number (PAN) of a card in a masked form, or   * an alias to access a payment account via a registered mobile phone number (MSISDN).
 */
@ApiModel(description = "Reference to an account by either:   * IBAN, of a payment accounts, or   * BBAN, for payment accounts if there is no IBAN, or    * the Primary Account Number (PAN) of a card, can be tokenised by the ASPSP due to PCI DSS requirements, or   * the Primary Account Number (PAN) of a card in a masked form, or   * an alias to access a payment account via a registered mobile phone number (MSISDN). ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-10-13T17:30:20.351194+03:00[Europe/Kiev]")

public class AccountReference   {
  @JsonProperty("iban")
  private String iban = null;

  @JsonProperty("bban")
  private String bban = null;

  @JsonProperty("pan")
  private String pan = null;

  @JsonProperty("maskedPan")
  private String maskedPan = null;

  @JsonProperty("msisdn")
  private String msisdn = null;

  @JsonProperty("currency")
  private String currency = null;

  @JsonProperty("other")
  private OtherType other = null;

  @JsonProperty("cashAccountType")
  private String cashAccountType = null;

  public AccountReference iban(String iban) {
    this.iban = iban;
    return this;
  }

  /**
   * Get iban
   * @return iban
  **/
  @ApiModelProperty(value = "")

@Pattern(regexp="[A-Z]{2,2}[0-9]{2,2}[a-zA-Z0-9]{1,30}")

  @JsonProperty("iban")
  public String getIban() {
    return iban;
  }

  public void setIban(String iban) {
    this.iban = iban;
  }

  public AccountReference bban(String bban) {
    this.bban = bban;
    return this;
  }

  /**
   * Get bban
   * @return bban
  **/
  @ApiModelProperty(value = "")

@Pattern(regexp="[a-zA-Z0-9]{1,30}")

  @JsonProperty("bban")
  public String getBban() {
    return bban;
  }

  public void setBban(String bban) {
    this.bban = bban;
  }

  public AccountReference pan(String pan) {
    this.pan = pan;
    return this;
  }

  /**
   * Get pan
   * @return pan
  **/
  @ApiModelProperty(value = "")

@Size(max=35)

  @JsonProperty("pan")
  public String getPan() {
    return pan;
  }

  public void setPan(String pan) {
    this.pan = pan;
  }

  public AccountReference maskedPan(String maskedPan) {
    this.maskedPan = maskedPan;
    return this;
  }

  /**
   * Get maskedPan
   * @return maskedPan
  **/
  @ApiModelProperty(value = "")

@Size(max=35)

  @JsonProperty("maskedPan")
  public String getMaskedPan() {
    return maskedPan;
  }

  public void setMaskedPan(String maskedPan) {
    this.maskedPan = maskedPan;
  }

  public AccountReference msisdn(String msisdn) {
    this.msisdn = msisdn;
    return this;
  }

  /**
   * Get msisdn
   * @return msisdn
  **/
  @ApiModelProperty(value = "")

@Size(max=35)

  @JsonProperty("msisdn")
  public String getMsisdn() {
    return msisdn;
  }

  public void setMsisdn(String msisdn) {
    this.msisdn = msisdn;
  }

  public AccountReference currency(String currency) {
    this.currency = currency;
    return this;
  }

  /**
   * Get currency
   * @return currency
  **/
  @ApiModelProperty(value = "")

@Pattern(regexp="[A-Z]{3}")

  @JsonProperty("currency")
  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public AccountReference other(OtherType other) {
    this.other = other;
    return this;
  }

  /**
   * Get other
   * @return other
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("other")
  public OtherType getOther() {
    return other;
  }

  public void setOther(OtherType other) {
    this.other = other;
  }

  public AccountReference cashAccountType(String cashAccountType) {
    this.cashAccountType = cashAccountType;
    return this;
  }

  /**
   * Get cashAccountType
   * @return cashAccountType
  **/
  @ApiModelProperty(value = "")



  @JsonProperty("cashAccountType")
  public String getCashAccountType() {
    return cashAccountType;
  }

  public void setCashAccountType(String cashAccountType) {
    this.cashAccountType = cashAccountType;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
}    AccountReference accountReference = (AccountReference) o;
    return Objects.equals(this.iban, accountReference.iban) &&
    Objects.equals(this.bban, accountReference.bban) &&
    Objects.equals(this.pan, accountReference.pan) &&
    Objects.equals(this.maskedPan, accountReference.maskedPan) &&
    Objects.equals(this.msisdn, accountReference.msisdn) &&
    Objects.equals(this.currency, accountReference.currency) &&
    Objects.equals(this.other, accountReference.other) &&
    Objects.equals(this.cashAccountType, accountReference.cashAccountType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(iban, bban, pan, maskedPan, msisdn, currency, other, cashAccountType);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AccountReference {\n");

    sb.append("    iban: ").append(toIndentedString(iban)).append("\n");
    sb.append("    bban: ").append(toIndentedString(bban)).append("\n");
    sb.append("    pan: ").append(toIndentedString(pan)).append("\n");
    sb.append("    maskedPan: ").append(toIndentedString(maskedPan)).append("\n");
    sb.append("    msisdn: ").append(toIndentedString(msisdn)).append("\n");
    sb.append("    currency: ").append(toIndentedString(currency)).append("\n");
    sb.append("    other: ").append(toIndentedString(other)).append("\n");
    sb.append("    cashAccountType: ").append(toIndentedString(cashAccountType)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

