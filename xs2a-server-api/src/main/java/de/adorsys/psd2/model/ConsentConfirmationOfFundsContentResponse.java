/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Body of the JSON response for a confirmation of funds content request.
 */
@ApiModel(description = "Body of the JSON response for a confirmation of funds content request.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-07-01T15:25:06.394043+03:00[Europe/Kiev]")

public class ConsentConfirmationOfFundsContentResponse   {
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

  @JsonProperty("consentStatus")
  private ConsentStatus consentStatus = null;

  public ConsentConfirmationOfFundsContentResponse account(AccountReference account) {
    this.account = account;
    return this;
  }

  /**
   * Get account
   * @return account
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid


  @JsonProperty("account")
  public AccountReference getAccount() {
    return account;
  }

  public void setAccount(AccountReference account) {
    this.account = account;
  }

  public ConsentConfirmationOfFundsContentResponse cardNumber(String cardNumber) {
    this.cardNumber = cardNumber;
    return this;
  }

  /**
   * Get cardNumber
   * @return cardNumber
  **/
  @ApiModelProperty(value = "")

@Size(max=35)

  @JsonProperty("cardNumber")
  public String getCardNumber() {
    return cardNumber;
  }

  public void setCardNumber(String cardNumber) {
    this.cardNumber = cardNumber;
  }

  public ConsentConfirmationOfFundsContentResponse cardExpiryDate(LocalDate cardExpiryDate) {
    this.cardExpiryDate = cardExpiryDate;
    return this;
  }

  /**
   * Get cardExpiryDate
   * @return cardExpiryDate
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("cardExpiryDate")
  public LocalDate getCardExpiryDate() {
    return cardExpiryDate;
  }

  public void setCardExpiryDate(LocalDate cardExpiryDate) {
    this.cardExpiryDate = cardExpiryDate;
  }

  public ConsentConfirmationOfFundsContentResponse cardInformation(String cardInformation) {
    this.cardInformation = cardInformation;
    return this;
  }

  /**
   * Get cardInformation
   * @return cardInformation
  **/
  @ApiModelProperty(value = "")

@Size(max=140)

  @JsonProperty("cardInformation")
  public String getCardInformation() {
    return cardInformation;
  }

  public void setCardInformation(String cardInformation) {
    this.cardInformation = cardInformation;
  }

  public ConsentConfirmationOfFundsContentResponse registrationInformation(String registrationInformation) {
    this.registrationInformation = registrationInformation;
    return this;
  }

  /**
   * Get registrationInformation
   * @return registrationInformation
  **/
  @ApiModelProperty(value = "")

@Size(max=140)

  @JsonProperty("registrationInformation")
  public String getRegistrationInformation() {
    return registrationInformation;
  }

  public void setRegistrationInformation(String registrationInformation) {
    this.registrationInformation = registrationInformation;
  }

  public ConsentConfirmationOfFundsContentResponse consentStatus(ConsentStatus consentStatus) {
    this.consentStatus = consentStatus;
    return this;
  }

  /**
   * Get consentStatus
   * @return consentStatus
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid


  @JsonProperty("consentStatus")
  public ConsentStatus getConsentStatus() {
    return consentStatus;
  }

  public void setConsentStatus(ConsentStatus consentStatus) {
    this.consentStatus = consentStatus;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConsentConfirmationOfFundsContentResponse consentConfirmationOfFundsContentResponse = (ConsentConfirmationOfFundsContentResponse) o;
    return Objects.equals(this.account, consentConfirmationOfFundsContentResponse.account) &&
        Objects.equals(this.cardNumber, consentConfirmationOfFundsContentResponse.cardNumber) &&
        Objects.equals(this.cardExpiryDate, consentConfirmationOfFundsContentResponse.cardExpiryDate) &&
        Objects.equals(this.cardInformation, consentConfirmationOfFundsContentResponse.cardInformation) &&
        Objects.equals(this.registrationInformation, consentConfirmationOfFundsContentResponse.registrationInformation) &&
        Objects.equals(this.consentStatus, consentConfirmationOfFundsContentResponse.consentStatus);
  }

  @Override
  public int hashCode() {
    return Objects.hash(account, cardNumber, cardExpiryDate, cardInformation, registrationInformation, consentStatus);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConsentConfirmationOfFundsContentResponse {\n");

    sb.append("    account: ").append(toIndentedString(account)).append("\n");
    sb.append("    cardNumber: ").append(toIndentedString(cardNumber)).append("\n");
    sb.append("    cardExpiryDate: ").append(toIndentedString(cardExpiryDate)).append("\n");
    sb.append("    cardInformation: ").append(toIndentedString(cardInformation)).append("\n");
    sb.append("    registrationInformation: ").append(toIndentedString(registrationInformation)).append("\n");
    sb.append("    consentStatus: ").append(toIndentedString(consentStatus)).append("\n");
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

