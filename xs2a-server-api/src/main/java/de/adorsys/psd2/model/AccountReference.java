/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
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
 * Reference to an account by either   * IBAN, of a payment accounts, or   * BBAN, for payment accounts if there is no IBAN, or    * the Primary Account Number (PAN) of a card, can be tokenised by the ASPSP due to PCI DSS requirements, or   * the Primary Account Number (PAN) of a card in a masked form, or   * an alias to access a payment account via a registered mobile phone number (MSISDN), or   * a proprietary ID of the  respective account that uniquely identifies the account for this ASPSP.
 */
@Schema(description = "Reference to an account by either   * IBAN, of a payment accounts, or   * BBAN, for payment accounts if there is no IBAN, or    * the Primary Account Number (PAN) of a card, can be tokenised by the ASPSP due to PCI DSS requirements, or   * the Primary Account Number (PAN) of a card in a masked form, or   * an alias to access a payment account via a registered mobile phone number (MSISDN), or   * a proprietary ID of the  respective account that uniquely identifies the account for this ASPSP. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-10-26T13:16:54.081225+03:00[Europe/Kiev]")


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

  @JsonProperty("other")
  private OtherType other = null;

  @JsonProperty("currency")
  private String currency = null;

  @JsonProperty("cashAccountType")
  private String cashAccountType = null;

  public AccountReference iban(String iban) {
    this.iban = iban;
    return this;
  }

    /**
     * IBAN of an account.
     *
     * @return iban
     **/
    @Schema(example = "FR7612345987650123456789014", description = "IBAN of an account.")
    @JsonProperty("iban")

    @Pattern(regexp = "[A-Z]{2,2}[0-9]{2,2}[a-zA-Z0-9]{1,30}")
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
     * Basic Bank Account Number (BBAN) Identifier.  This data element can be used in the body of the consent request.   Message for retrieving account access consent from this account. This   data elements is used for payment accounts which have no IBAN.   ISO20022: Basic Bank Account Number (BBAN).       Identifier used nationally by financial institutions, i.e., in individual countries,    generally as part of a National Account Numbering Scheme(s),    which uniquely identifies the account of a customer.
     *
     * @return bban
     **/
    @Schema(example = "BARC12345612345678", description = "Basic Bank Account Number (BBAN) Identifier.  This data element can be used in the body of the consent request.   Message for retrieving account access consent from this account. This   data elements is used for payment accounts which have no IBAN.   ISO20022: Basic Bank Account Number (BBAN).       Identifier used nationally by financial institutions, i.e., in individual countries,    generally as part of a National Account Numbering Scheme(s),    which uniquely identifies the account of a customer. ")
    @JsonProperty("bban")

    @Pattern(regexp = "[a-zA-Z0-9]{1,30}")
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
     * Primary Account Number according to ISO/IEC 7812.
     *
     * @return pan
     **/
    @Schema(example = "5409050000000000", description = "Primary Account Number according to ISO/IEC 7812. ")
    @JsonProperty("pan")

    @Size(max = 35)
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
     * Masked Primary Account Number.
     *
     * @return maskedPan
     **/
    @Schema(example = "123456xxxxxx1234", description = "Masked Primary Account Number. ")
    @JsonProperty("maskedPan")

    @Size(max = 35)
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
     * Mobile phone number.
     *
     * @return msisdn
     **/
    @Schema(example = "+49 170 1234567", description = "Mobile phone number.")
    @JsonProperty("msisdn")

    @Size(max = 35)
    public String getMsisdn() {
    return msisdn;
  }

  public void setMsisdn(String msisdn) {
    this.msisdn = msisdn;
  }

  public AccountReference other(OtherType other) {
      this.other = other;
      return this;
  }

    /**
     * Get other
     *
     * @return other
     **/
    @Schema(description = "")
    @JsonProperty("other")

    @Valid
    public OtherType getOther() {
    return other;
  }

  public void setOther(OtherType other) {
    this.other = other;
  }

  public AccountReference currency(String currency) {
      this.currency = currency;
      return this;
  }

    /**
     * ISO 4217 Alpha 3 currency code.
     *
     * @return currency
     **/
    @Schema(example = "EUR", description = "ISO 4217 Alpha 3 currency code. ")
    @JsonProperty("currency")

    @Pattern(regexp="[A-Z]{3}")   public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public AccountReference cashAccountType(String cashAccountType) {
      this.cashAccountType = cashAccountType;
      return this;
  }

    /**
     * ExternalCashAccountType1Code from ISO 20022.
     *
     * @return cashAccountType
     **/
    @Schema(description = "ExternalCashAccountType1Code from ISO 20022. ")
    @JsonProperty("cashAccountType")

  public String getCashAccountType() {
    return cashAccountType;
  }

  public void setCashAccountType(String cashAccountType) {
    this.cashAccountType = cashAccountType;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AccountReference accountReference = (AccountReference) o;
    return Objects.equals(this.iban, accountReference.iban) &&
        Objects.equals(this.bban, accountReference.bban) &&
        Objects.equals(this.pan, accountReference.pan) &&
        Objects.equals(this.maskedPan, accountReference.maskedPan) &&
        Objects.equals(this.msisdn, accountReference.msisdn) &&
        Objects.equals(this.other, accountReference.other) &&
        Objects.equals(this.currency, accountReference.currency) &&
        Objects.equals(this.cashAccountType, accountReference.cashAccountType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(iban, bban, pan, maskedPan, msisdn, other, currency, cashAccountType);
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
    sb.append("    other: ").append(toIndentedString(other)).append("\n");
    sb.append("    currency: ").append(toIndentedString(currency)).append("\n");
    sb.append("    cashAccountType: ").append(toIndentedString(cashAccountType)).append("\n");
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
