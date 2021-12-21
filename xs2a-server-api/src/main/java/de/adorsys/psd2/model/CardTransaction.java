package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Card transaction information.
 */
@ApiModel(description = "Card transaction information.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-11-16T16:54:38.691901+02:00[Europe/Kiev]")

public class CardTransaction   {
  @JsonProperty("cardTransactionId")
  private String cardTransactionId = null;

  @JsonProperty("terminalId")
  private String terminalId = null;

  @JsonProperty("transactionDate")
  private LocalDate transactionDate = null;

  @JsonProperty("acceptorTransactionDateTime")
  private OffsetDateTime acceptorTransactionDateTime = null;

  @JsonProperty("bookingDate")
  private LocalDate bookingDate = null;

  @JsonProperty("valueDate")
  private LocalDate valueDate = null;

  @JsonProperty("transactionAmount")
  private Amount transactionAmount = null;

  @JsonProperty("grandTotalAmount")
  private GrandTotalAmount grandTotalAmount = null;

  @JsonProperty("currencyExchange")
  private ReportExchangeRateList currencyExchange = null;

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

  @JsonProperty("cardAcceptorPhone")
  private String cardAcceptorPhone = null;

  @JsonProperty("merchantCategoryCode")
  private String merchantCategoryCode = null;

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

  @JsonProperty("cardTransactionId")
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

  @JsonProperty("terminalId")
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


  @JsonProperty("transactionDate")
  public LocalDate getTransactionDate() {
    return transactionDate;
  }

  public void setTransactionDate(LocalDate transactionDate) {
    this.transactionDate = transactionDate;
  }

  public CardTransaction acceptorTransactionDateTime(OffsetDateTime acceptorTransactionDateTime) {
    this.acceptorTransactionDateTime = acceptorTransactionDateTime;
    return this;
  }

  /**
   * Timestamp of the actual card transaction within the acceptance system
   * @return acceptorTransactionDateTime
  **/
  @ApiModelProperty(value = "Timestamp of the actual card transaction within the acceptance system")

  @Valid


  @JsonProperty("acceptorTransactionDateTime")
  public OffsetDateTime getAcceptorTransactionDateTime() {
    return acceptorTransactionDateTime;
  }

  public void setAcceptorTransactionDateTime(OffsetDateTime acceptorTransactionDateTime) {
    this.acceptorTransactionDateTime = acceptorTransactionDateTime;
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


  @JsonProperty("bookingDate")
  public LocalDate getBookingDate() {
    return bookingDate;
  }

  public void setBookingDate(LocalDate bookingDate) {
    this.bookingDate = bookingDate;
  }

  public CardTransaction valueDate(LocalDate valueDate) {
    this.valueDate = valueDate;
    return this;
  }

  /**
   * The Date at which assets become available to the account owner in case of a credit, or cease to be available to the account owner in case of a debit entry. For card transactions this is the payment due date of related booked transactions of a card.
   * @return valueDate
  **/
  @ApiModelProperty(value = "The Date at which assets become available to the account owner in case of a credit, or cease to be available to the account owner in case of a debit entry. For card transactions this is the payment due date of related booked transactions of a card.")

  @Valid


  @JsonProperty("valueDate")
  public LocalDate getValueDate() {
    return valueDate;
  }

  public void setValueDate(LocalDate valueDate) {
    this.valueDate = valueDate;
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


  @JsonProperty("transactionAmount")
  public Amount getTransactionAmount() {
    return transactionAmount;
  }

  public void setTransactionAmount(Amount transactionAmount) {
    this.transactionAmount = transactionAmount;
  }

  public CardTransaction grandTotalAmount(GrandTotalAmount grandTotalAmount) {
    this.grandTotalAmount = grandTotalAmount;
    return this;
  }

  /**
   * Get grandTotalAmount
   * @return grandTotalAmount
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("grandTotalAmount")
  public GrandTotalAmount getGrandTotalAmount() {
    return grandTotalAmount;
  }

  public void setGrandTotalAmount(GrandTotalAmount grandTotalAmount) {
    this.grandTotalAmount = grandTotalAmount;
  }

  public CardTransaction currencyExchange(ReportExchangeRateList currencyExchange) {
    this.currencyExchange = currencyExchange;
    return this;
  }

  /**
   * Get currencyExchange
   * @return currencyExchange
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("currencyExchange")
  public ReportExchangeRateList getCurrencyExchange() {
    return currencyExchange;
  }

  public void setCurrencyExchange(ReportExchangeRateList currencyExchange) {
    this.currencyExchange = currencyExchange;
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


  @JsonProperty("originalAmount")
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


  @JsonProperty("markupFee")
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
  @ApiModelProperty(example = "0.3", value = "")



  @JsonProperty("markupFeePercentage")
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

  @JsonProperty("cardAcceptorId")
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


  @JsonProperty("cardAcceptorAddress")
  public Address getCardAcceptorAddress() {
    return cardAcceptorAddress;
  }

  public void setCardAcceptorAddress(Address cardAcceptorAddress) {
    this.cardAcceptorAddress = cardAcceptorAddress;
  }

  public CardTransaction cardAcceptorPhone(String cardAcceptorPhone) {
    this.cardAcceptorPhone = cardAcceptorPhone;
    return this;
  }

  /**
   * Get cardAcceptorPhone
   * @return cardAcceptorPhone
  **/
  @ApiModelProperty(value = "")

@Pattern(regexp="\\+[0-9]{1,3}\\-[0-9()+\\-]{1,30}")

  @JsonProperty("cardAcceptorPhone")
  public String getCardAcceptorPhone() {
    return cardAcceptorPhone;
  }

  public void setCardAcceptorPhone(String cardAcceptorPhone) {
    this.cardAcceptorPhone = cardAcceptorPhone;
  }

  public CardTransaction merchantCategoryCode(String merchantCategoryCode) {
    this.merchantCategoryCode = merchantCategoryCode;
    return this;
  }

  /**
   * Get merchantCategoryCode
   * @return merchantCategoryCode
  **/
  @ApiModelProperty(value = "")

@Size(min=4,max=4)

  @JsonProperty("merchantCategoryCode")
  public String getMerchantCategoryCode() {
    return merchantCategoryCode;
  }

  public void setMerchantCategoryCode(String merchantCategoryCode) {
    this.merchantCategoryCode = merchantCategoryCode;
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

  @JsonProperty("maskedPAN")
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

@Size(max=1000)

  @JsonProperty("transactionDetails")
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



  @JsonProperty("invoiced")
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

  @JsonProperty("proprietaryBankTransactionCode")
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
}    CardTransaction cardTransaction = (CardTransaction) o;
    return Objects.equals(this.cardTransactionId, cardTransaction.cardTransactionId) &&
    Objects.equals(this.terminalId, cardTransaction.terminalId) &&
    Objects.equals(this.transactionDate, cardTransaction.transactionDate) &&
    Objects.equals(this.acceptorTransactionDateTime, cardTransaction.acceptorTransactionDateTime) &&
    Objects.equals(this.bookingDate, cardTransaction.bookingDate) &&
    Objects.equals(this.valueDate, cardTransaction.valueDate) &&
    Objects.equals(this.transactionAmount, cardTransaction.transactionAmount) &&
    Objects.equals(this.grandTotalAmount, cardTransaction.grandTotalAmount) &&
    Objects.equals(this.currencyExchange, cardTransaction.currencyExchange) &&
    Objects.equals(this.originalAmount, cardTransaction.originalAmount) &&
    Objects.equals(this.markupFee, cardTransaction.markupFee) &&
    Objects.equals(this.markupFeePercentage, cardTransaction.markupFeePercentage) &&
    Objects.equals(this.cardAcceptorId, cardTransaction.cardAcceptorId) &&
    Objects.equals(this.cardAcceptorAddress, cardTransaction.cardAcceptorAddress) &&
    Objects.equals(this.cardAcceptorPhone, cardTransaction.cardAcceptorPhone) &&
    Objects.equals(this.merchantCategoryCode, cardTransaction.merchantCategoryCode) &&
    Objects.equals(this.maskedPAN, cardTransaction.maskedPAN) &&
    Objects.equals(this.transactionDetails, cardTransaction.transactionDetails) &&
    Objects.equals(this.invoiced, cardTransaction.invoiced) &&
    Objects.equals(this.proprietaryBankTransactionCode, cardTransaction.proprietaryBankTransactionCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cardTransactionId, terminalId, transactionDate, acceptorTransactionDateTime, bookingDate, valueDate, transactionAmount, grandTotalAmount, currencyExchange, originalAmount, markupFee, markupFeePercentage, cardAcceptorId, cardAcceptorAddress, cardAcceptorPhone, merchantCategoryCode, maskedPAN, transactionDetails, invoiced, proprietaryBankTransactionCode);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CardTransaction {\n");

    sb.append("    cardTransactionId: ").append(toIndentedString(cardTransactionId)).append("\n");
    sb.append("    terminalId: ").append(toIndentedString(terminalId)).append("\n");
    sb.append("    transactionDate: ").append(toIndentedString(transactionDate)).append("\n");
    sb.append("    acceptorTransactionDateTime: ").append(toIndentedString(acceptorTransactionDateTime)).append("\n");
    sb.append("    bookingDate: ").append(toIndentedString(bookingDate)).append("\n");
    sb.append("    valueDate: ").append(toIndentedString(valueDate)).append("\n");
    sb.append("    transactionAmount: ").append(toIndentedString(transactionAmount)).append("\n");
    sb.append("    grandTotalAmount: ").append(toIndentedString(grandTotalAmount)).append("\n");
    sb.append("    currencyExchange: ").append(toIndentedString(currencyExchange)).append("\n");
    sb.append("    originalAmount: ").append(toIndentedString(originalAmount)).append("\n");
    sb.append("    markupFee: ").append(toIndentedString(markupFee)).append("\n");
    sb.append("    markupFeePercentage: ").append(toIndentedString(markupFeePercentage)).append("\n");
    sb.append("    cardAcceptorId: ").append(toIndentedString(cardAcceptorId)).append("\n");
    sb.append("    cardAcceptorAddress: ").append(toIndentedString(cardAcceptorAddress)).append("\n");
    sb.append("    cardAcceptorPhone: ").append(toIndentedString(cardAcceptorPhone)).append("\n");
    sb.append("    merchantCategoryCode: ").append(toIndentedString(merchantCategoryCode)).append("\n");
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

