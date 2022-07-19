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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Content of the body of a consent confirmation of funds request.
 */
@Schema(description = "Content of the body of a consent confirmation of funds request. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-05-06T13:00:06.283258+03:00[Europe/Kiev]")


public class ConsentsConfirmationOfFunds   {
  @JsonProperty("account")
  private AccountReference account = null;

  @JsonProperty("cardNumber")
  private String cardNumber = null;

  @JsonProperty("cardExpiryDate")
  private LocalDate cardExpiryDate = null;

  @JsonProperty("cardInformation")
  private String cardInformation = null;

  @JsonProperty("registrationInformation")
  private String registrationInformation = null;

  public ConsentsConfirmationOfFunds account(AccountReference account) {
    this.account = account;
    return this;
  }

  /**
   * Get account
   * @return account
   **/
  @Schema(required = true, description = "")
      @NotNull

    @Valid
    public AccountReference getAccount() {
    return account;
  }

  public void setAccount(AccountReference account) {
    this.account = account;
  }

  public ConsentsConfirmationOfFunds cardNumber(String cardNumber) {
    this.cardNumber = cardNumber;
    return this;
  }

  /**
   * Card Number of the card issued by the PIISP. Should be delivered if available.
   * @return cardNumber
   **/
  @Schema(description = "Card Number of the card issued by the PIISP. Should be delivered if available.")

  @Size(max=35)   public String getCardNumber() {
    return cardNumber;
  }

  public void setCardNumber(String cardNumber) {
    this.cardNumber = cardNumber;
  }

  public ConsentsConfirmationOfFunds cardExpiryDate(LocalDate cardExpiryDate) {
    this.cardExpiryDate = cardExpiryDate;
    return this;
  }

  /**
   * Expiry date of the card issued by the PIISP.
   * @return cardExpiryDate
   **/
  @Schema(example = "Sun Jul 01 03:00:00 EEST 2018", description = "Expiry date of the card issued by the PIISP. ")

    @Valid
    public LocalDate getCardExpiryDate() {
    return cardExpiryDate;
  }

  public void setCardExpiryDate(LocalDate cardExpiryDate) {
    this.cardExpiryDate = cardExpiryDate;
  }

  public ConsentsConfirmationOfFunds cardInformation(String cardInformation) {
    this.cardInformation = cardInformation;
    return this;
  }

  /**
   * Addtional explanation for the card product.
   * @return cardInformation
   **/
  @Schema(description = "Addtional explanation for the card product.")

  @Size(max=140)   public String getCardInformation() {
    return cardInformation;
  }

  public void setCardInformation(String cardInformation) {
    this.cardInformation = cardInformation;
  }

  public ConsentsConfirmationOfFunds registrationInformation(String registrationInformation) {
    this.registrationInformation = registrationInformation;
    return this;
  }

  /**
   * Addtional information about the registration process for the PSU, e. g. a reference to the TPP / PSU contract.
   * @return registrationInformation
   **/
  @Schema(description = "Addtional information about the registration process for the PSU, e. g. a reference to the TPP / PSU contract.")

  @Size(max=140)   public String getRegistrationInformation() {
    return registrationInformation;
  }

  public void setRegistrationInformation(String registrationInformation) {
    this.registrationInformation = registrationInformation;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConsentsConfirmationOfFunds consentsConfirmationOfFunds = (ConsentsConfirmationOfFunds) o;
    return Objects.equals(this.account, consentsConfirmationOfFunds.account) &&
        Objects.equals(this.cardNumber, consentsConfirmationOfFunds.cardNumber) &&
        Objects.equals(this.cardExpiryDate, consentsConfirmationOfFunds.cardExpiryDate) &&
        Objects.equals(this.cardInformation, consentsConfirmationOfFunds.cardInformation) &&
        Objects.equals(this.registrationInformation, consentsConfirmationOfFunds.registrationInformation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(account, cardNumber, cardExpiryDate, cardInformation, registrationInformation);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConsentsConfirmationOfFunds {\n");

    sb.append("    account: ").append(toIndentedString(account)).append("\n");
    sb.append("    cardNumber: ").append(toIndentedString(cardNumber)).append("\n");
    sb.append("    cardExpiryDate: ").append(toIndentedString(cardExpiryDate)).append("\n");
    sb.append("    cardInformation: ").append(toIndentedString(cardInformation)).append("\n");
    sb.append("    registrationInformation: ").append(toIndentedString(registrationInformation)).append("\n");
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
