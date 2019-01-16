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

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import de.adorsys.psd2.model.Address;
import de.adorsys.psd2.model.Amount;
import de.adorsys.psd2.model.ExchangeRateList;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * Card transaction information
 */
@ApiModel(description = "Card transaction information")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-01-11T12:48:04.675377+02:00[Europe/Kiev]")

public class CardTransaction   {
  @JsonProperty("cardTransactionId")
  private String cardTransactionId = null;

  @JsonProperty("terminalId")
  private String terminalId = null;

  @JsonProperty("transactionDate")
  private LocalDate transactionDate = null;

  @JsonProperty("bookingDate")
  private LocalDate bookingDate = null;

  @JsonProperty("transactionAmount")
  private Amount transactionAmount = null;

  @JsonProperty("exchangeRate")
  private ExchangeRateList exchangeRate = null;

  @JsonProperty("originalAmount")
  private Amount originalAmount = null;

  @JsonProperty("markupFee")
  private Amount markupFee = null;

  @JsonProperty("markupFeePercentage")
  private String markupFeePercentage = null;

  @JsonProperty("cardAcceptorId")
  private String cardAcceptorId = null;

  @JsonProperty("cardAcceptorAddress")
  private Address cardAcceptorAddress = null;

  @JsonProperty("cardAcceptorCategoryCode")
  private String cardAcceptorCategoryCode = null;

  @JsonProperty("maskedPAN")
  private String maskedPAN = null;

  @JsonProperty("transactionDetails")
  private String transactionDetails = null;

  @JsonProperty("invoiced")
  private Boolean invoiced = null;

  @JsonProperty("proprietaryBankTransactionCode")
  private String proprietaryBankTransactionCode = null;

  public CardTransaction cardTransactionId(String cardTransactionId) {
    this.cardTransactionId = cardTransactionId;
    return this;
  }

  /**
   * Get cardTransactionId
   * @return cardTransactionId
  **/
  @ApiModelProperty(value = "")

@Size(max=35) 
  public String getCardTransactionId() {
    return cardTransactionId;
  }

  public void setCardTransactionId(String cardTransactionId) {
    this.cardTransactionId = cardTransactionId;
  }

  public CardTransaction terminalId(String terminalId) {
    this.terminalId = terminalId;
    return this;
  }

  /**
   * Get terminalId
   * @return terminalId
  **/
  @ApiModelProperty(value = "")

@Size(max=35) 
  public String getTerminalId() {
    return terminalId;
  }

  public void setTerminalId(String terminalId) {
    this.terminalId = terminalId;
  }

  public CardTransaction transactionDate(LocalDate transactionDate) {
    this.transactionDate = transactionDate;
    return this;
  }

  /**
   * Get transactionDate
   * @return transactionDate
  **/
  @ApiModelProperty(value = "")

  @Valid

  public LocalDate getTransactionDate() {
    return transactionDate;
  }

  public void setTransactionDate(LocalDate transactionDate) {
    this.transactionDate = transactionDate;
  }

  public CardTransaction bookingDate(LocalDate bookingDate) {
    this.bookingDate = bookingDate;
    return this;
  }

  /**
   * Get bookingDate
   * @return bookingDate
  **/
  @ApiModelProperty(value = "")

  @Valid

  public LocalDate getBookingDate() {
    return bookingDate;
  }

  public void setBookingDate(LocalDate bookingDate) {
    this.bookingDate = bookingDate;
  }

  public CardTransaction transactionAmount(Amount transactionAmount) {
    this.transactionAmount = transactionAmount;
    return this;
  }

  /**
   * Get transactionAmount
   * @return transactionAmount
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid

  public Amount getTransactionAmount() {
    return transactionAmount;
  }

  public void setTransactionAmount(Amount transactionAmount) {
    this.transactionAmount = transactionAmount;
  }

  public CardTransaction exchangeRate(ExchangeRateList exchangeRate) {
    this.exchangeRate = exchangeRate;
    return this;
  }

  /**
   * Get exchangeRate
   * @return exchangeRate
  **/
  @ApiModelProperty(value = "")

  @Valid

  public ExchangeRateList getExchangeRate() {
    return exchangeRate;
  }

  public void setExchangeRate(ExchangeRateList exchangeRate) {
    this.exchangeRate = exchangeRate;
  }

  public CardTransaction originalAmount(Amount originalAmount) {
    this.originalAmount = originalAmount;
    return this;
  }

  /**
   * Get originalAmount
   * @return originalAmount
  **/
  @ApiModelProperty(value = "")

  @Valid

  public Amount getOriginalAmount() {
    return originalAmount;
  }

  public void setOriginalAmount(Amount originalAmount) {
    this.originalAmount = originalAmount;
  }

  public CardTransaction markupFee(Amount markupFee) {
    this.markupFee = markupFee;
    return this;
  }

  /**
   * Get markupFee
   * @return markupFee
  **/
  @ApiModelProperty(value = "")

  @Valid

  public Amount getMarkupFee() {
    return markupFee;
  }

  public void setMarkupFee(Amount markupFee) {
    this.markupFee = markupFee;
  }

  public CardTransaction markupFeePercentage(String markupFeePercentage) {
    this.markupFeePercentage = markupFeePercentage;
    return this;
  }

  /**
   * Get markupFeePercentage
   * @return markupFeePercentage
  **/
  @ApiModelProperty(value = "")


  public String getMarkupFeePercentage() {
    return markupFeePercentage;
  }

  public void setMarkupFeePercentage(String markupFeePercentage) {
    this.markupFeePercentage = markupFeePercentage;
  }

  public CardTransaction cardAcceptorId(String cardAcceptorId) {
    this.cardAcceptorId = cardAcceptorId;
    return this;
  }

  /**
   * Get cardAcceptorId
   * @return cardAcceptorId
  **/
  @ApiModelProperty(value = "")

@Size(max=35) 
  public String getCardAcceptorId() {
    return cardAcceptorId;
  }

  public void setCardAcceptorId(String cardAcceptorId) {
    this.cardAcceptorId = cardAcceptorId;
  }

  public CardTransaction cardAcceptorAddress(Address cardAcceptorAddress) {
    this.cardAcceptorAddress = cardAcceptorAddress;
    return this;
  }

  /**
   * Get cardAcceptorAddress
   * @return cardAcceptorAddress
  **/
  @ApiModelProperty(value = "")

  @Valid

  public Address getCardAcceptorAddress() {
    return cardAcceptorAddress;
  }

  public void setCardAcceptorAddress(Address cardAcceptorAddress) {
    this.cardAcceptorAddress = cardAcceptorAddress;
  }

  public CardTransaction cardAcceptorCategoryCode(String cardAcceptorCategoryCode) {
    this.cardAcceptorCategoryCode = cardAcceptorCategoryCode;
    return this;
  }

  /**
   * Get cardAcceptorCategoryCode
   * @return cardAcceptorCategoryCode
  **/
  @ApiModelProperty(value = "")


  public String getCardAcceptorCategoryCode() {
    return cardAcceptorCategoryCode;
  }

  public void setCardAcceptorCategoryCode(String cardAcceptorCategoryCode) {
    this.cardAcceptorCategoryCode = cardAcceptorCategoryCode;
  }

  public CardTransaction maskedPAN(String maskedPAN) {
    this.maskedPAN = maskedPAN;
    return this;
  }

  /**
   * Get maskedPAN
   * @return maskedPAN
  **/
  @ApiModelProperty(value = "")

@Size(max=35) 
  public String getMaskedPAN() {
    return maskedPAN;
  }

  public void setMaskedPAN(String maskedPAN) {
    this.maskedPAN = maskedPAN;
  }

  public CardTransaction transactionDetails(String transactionDetails) {
    this.transactionDetails = transactionDetails;
    return this;
  }

  /**
   * Get transactionDetails
   * @return transactionDetails
  **/
  @ApiModelProperty(value = "")

@Size(max=140) 
  public String getTransactionDetails() {
    return transactionDetails;
  }

  public void setTransactionDetails(String transactionDetails) {
    this.transactionDetails = transactionDetails;
  }

  public CardTransaction invoiced(Boolean invoiced) {
    this.invoiced = invoiced;
    return this;
  }

  /**
   * Get invoiced
   * @return invoiced
  **/
  @ApiModelProperty(value = "")


  public Boolean isInvoiced() {
    return invoiced;
  }

  public void setInvoiced(Boolean invoiced) {
    this.invoiced = invoiced;
  }

  public CardTransaction proprietaryBankTransactionCode(String proprietaryBankTransactionCode) {
    this.proprietaryBankTransactionCode = proprietaryBankTransactionCode;
    return this;
  }

  /**
   * Get proprietaryBankTransactionCode
   * @return proprietaryBankTransactionCode
  **/
  @ApiModelProperty(value = "")

@Size(max=35) 
  public String getProprietaryBankTransactionCode() {
    return proprietaryBankTransactionCode;
  }

  public void setProprietaryBankTransactionCode(String proprietaryBankTransactionCode) {
    this.proprietaryBankTransactionCode = proprietaryBankTransactionCode;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CardTransaction cardTransaction = (CardTransaction) o;
    return Objects.equals(this.cardTransactionId, cardTransaction.cardTransactionId) &&
        Objects.equals(this.terminalId, cardTransaction.terminalId) &&
        Objects.equals(this.transactionDate, cardTransaction.transactionDate) &&
        Objects.equals(this.bookingDate, cardTransaction.bookingDate) &&
        Objects.equals(this.transactionAmount, cardTransaction.transactionAmount) &&
        Objects.equals(this.exchangeRate, cardTransaction.exchangeRate) &&
        Objects.equals(this.originalAmount, cardTransaction.originalAmount) &&
        Objects.equals(this.markupFee, cardTransaction.markupFee) &&
        Objects.equals(this.markupFeePercentage, cardTransaction.markupFeePercentage) &&
        Objects.equals(this.cardAcceptorId, cardTransaction.cardAcceptorId) &&
        Objects.equals(this.cardAcceptorAddress, cardTransaction.cardAcceptorAddress) &&
        Objects.equals(this.cardAcceptorCategoryCode, cardTransaction.cardAcceptorCategoryCode) &&
        Objects.equals(this.maskedPAN, cardTransaction.maskedPAN) &&
        Objects.equals(this.transactionDetails, cardTransaction.transactionDetails) &&
        Objects.equals(this.invoiced, cardTransaction.invoiced) &&
        Objects.equals(this.proprietaryBankTransactionCode, cardTransaction.proprietaryBankTransactionCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cardTransactionId, terminalId, transactionDate, bookingDate, transactionAmount, exchangeRate, originalAmount, markupFee, markupFeePercentage, cardAcceptorId, cardAcceptorAddress, cardAcceptorCategoryCode, maskedPAN, transactionDetails, invoiced, proprietaryBankTransactionCode);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CardTransaction {\n");
    
    sb.append("    cardTransactionId: ").append(toIndentedString(cardTransactionId)).append("\n");
    sb.append("    terminalId: ").append(toIndentedString(terminalId)).append("\n");
    sb.append("    transactionDate: ").append(toIndentedString(transactionDate)).append("\n");
    sb.append("    bookingDate: ").append(toIndentedString(bookingDate)).append("\n");
    sb.append("    transactionAmount: ").append(toIndentedString(transactionAmount)).append("\n");
    sb.append("    exchangeRate: ").append(toIndentedString(exchangeRate)).append("\n");
    sb.append("    originalAmount: ").append(toIndentedString(originalAmount)).append("\n");
    sb.append("    markupFee: ").append(toIndentedString(markupFee)).append("\n");
    sb.append("    markupFeePercentage: ").append(toIndentedString(markupFeePercentage)).append("\n");
    sb.append("    cardAcceptorId: ").append(toIndentedString(cardAcceptorId)).append("\n");
    sb.append("    cardAcceptorAddress: ").append(toIndentedString(cardAcceptorAddress)).append("\n");
    sb.append("    cardAcceptorCategoryCode: ").append(toIndentedString(cardAcceptorCategoryCode)).append("\n");
    sb.append("    maskedPAN: ").append(toIndentedString(maskedPAN)).append("\n");
    sb.append("    transactionDetails: ").append(toIndentedString(transactionDetails)).append("\n");
    sb.append("    invoiced: ").append(toIndentedString(invoiced)).append("\n");
    sb.append("    proprietaryBankTransactionCode: ").append(toIndentedString(proprietaryBankTransactionCode)).append("\n");
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

