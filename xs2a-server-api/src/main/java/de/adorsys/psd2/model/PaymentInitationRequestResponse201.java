package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Body of the response for a successful payment initiation request.
 */
@ApiModel(description = "Body of the response for a successful payment initiation request.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-11-05T12:22:49.487689+02:00[Europe/Kiev]")

public class PaymentInitationRequestResponse201   {
  @JsonProperty("transactionStatus")
  private TransactionStatus transactionStatus = null;

  @JsonProperty("paymentId")
  private String paymentId = null;

  @JsonProperty("transactionFees")
  private Amount transactionFees = null;

  @JsonProperty("currencyConversionFee")
  private Amount currencyConversionFee = null;

  @JsonProperty("estimatedTotalAmount")
  private Amount estimatedTotalAmount = null;

  @JsonProperty("estimatedInterbankSettlementAmount")
  private Amount estimatedInterbankSettlementAmount = null;

  @JsonProperty("transactionFeeIndicator")
  private Boolean transactionFeeIndicator = null;

  @JsonProperty("scaMethods")
  private ScaMethods scaMethods = null;

  @JsonProperty("chosenScaMethod")
  private ChosenScaMethod chosenScaMethod = null;

  @JsonProperty("challengeData")
  private ChallengeData challengeData = null;

  @JsonProperty("_links")
  private Map _links = null;

  @JsonProperty("psuMessage")
  private String psuMessage = null;

  @JsonProperty("tppMessages")
  @Valid
  private List<TppMessage201PaymentInitiation> tppMessages = null;

  @JsonProperty("scaStatus")
  private ScaStatus scaStatus = null;

  public PaymentInitationRequestResponse201 transactionStatus(TransactionStatus transactionStatus) {
    this.transactionStatus = transactionStatus;
    return this;
  }

  /**
   * Get transactionStatus
   * @return transactionStatus
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid


  @JsonProperty("transactionStatus")
  public TransactionStatus getTransactionStatus() {
    return transactionStatus;
  }

  public void setTransactionStatus(TransactionStatus transactionStatus) {
    this.transactionStatus = transactionStatus;
  }

  public PaymentInitationRequestResponse201 paymentId(String paymentId) {
    this.paymentId = paymentId;
    return this;
  }

  /**
   * Get paymentId
   * @return paymentId
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull



  @JsonProperty("paymentId")
  public String getPaymentId() {
    return paymentId;
  }

  public void setPaymentId(String paymentId) {
    this.paymentId = paymentId;
  }

  public PaymentInitationRequestResponse201 transactionFees(Amount transactionFees) {
    this.transactionFees = transactionFees;
    return this;
  }

  /**
   * Get transactionFees
   * @return transactionFees
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("transactionFees")
  public Amount getTransactionFees() {
    return transactionFees;
  }

  public void setTransactionFees(Amount transactionFees) {
    this.transactionFees = transactionFees;
  }

  public PaymentInitationRequestResponse201 currencyConversionFee(Amount currencyConversionFee) {
    this.currencyConversionFee = currencyConversionFee;
    return this;
  }

  /**
   * Get currencyConversionFee
   * @return currencyConversionFee
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("currencyConversionFee")
  public Amount getCurrencyConversionFee() {
    return currencyConversionFee;
  }

  public void setCurrencyConversionFee(Amount currencyConversionFee) {
    this.currencyConversionFee = currencyConversionFee;
  }

  public PaymentInitationRequestResponse201 estimatedTotalAmount(Amount estimatedTotalAmount) {
    this.estimatedTotalAmount = estimatedTotalAmount;
    return this;
  }

  /**
   * Get estimatedTotalAmount
   * @return estimatedTotalAmount
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("estimatedTotalAmount")
  public Amount getEstimatedTotalAmount() {
    return estimatedTotalAmount;
  }

  public void setEstimatedTotalAmount(Amount estimatedTotalAmount) {
    this.estimatedTotalAmount = estimatedTotalAmount;
  }

  public PaymentInitationRequestResponse201 estimatedInterbankSettlementAmount(Amount estimatedInterbankSettlementAmount) {
    this.estimatedInterbankSettlementAmount = estimatedInterbankSettlementAmount;
    return this;
  }

  /**
   * Get estimatedInterbankSettlementAmount
   * @return estimatedInterbankSettlementAmount
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("estimatedInterbankSettlementAmount")
  public Amount getEstimatedInterbankSettlementAmount() {
    return estimatedInterbankSettlementAmount;
  }

  public void setEstimatedInterbankSettlementAmount(Amount estimatedInterbankSettlementAmount) {
    this.estimatedInterbankSettlementAmount = estimatedInterbankSettlementAmount;
  }

  public PaymentInitationRequestResponse201 transactionFeeIndicator(Boolean transactionFeeIndicator) {
    this.transactionFeeIndicator = transactionFeeIndicator;
    return this;
  }

  /**
   * Get transactionFeeIndicator
   * @return transactionFeeIndicator
  **/
  @ApiModelProperty(value = "")



  @JsonProperty("transactionFeeIndicator")
  public Boolean getTransactionFeeIndicator() {
    return transactionFeeIndicator;
  }

  public void setTransactionFeeIndicator(Boolean transactionFeeIndicator) {
    this.transactionFeeIndicator = transactionFeeIndicator;
  }

  public PaymentInitationRequestResponse201 scaMethods(ScaMethods scaMethods) {
    this.scaMethods = scaMethods;
    return this;
  }

  /**
   * Get scaMethods
   * @return scaMethods
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("scaMethods")
  public ScaMethods getScaMethods() {
    return scaMethods;
  }

  public void setScaMethods(ScaMethods scaMethods) {
    this.scaMethods = scaMethods;
  }

  public PaymentInitationRequestResponse201 chosenScaMethod(ChosenScaMethod chosenScaMethod) {
    this.chosenScaMethod = chosenScaMethod;
    return this;
  }

  /**
   * Get chosenScaMethod
   * @return chosenScaMethod
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("chosenScaMethod")
  public ChosenScaMethod getChosenScaMethod() {
    return chosenScaMethod;
  }

  public void setChosenScaMethod(ChosenScaMethod chosenScaMethod) {
    this.chosenScaMethod = chosenScaMethod;
  }

  public PaymentInitationRequestResponse201 challengeData(ChallengeData challengeData) {
    this.challengeData = challengeData;
    return this;
  }

  /**
   * Get challengeData
   * @return challengeData
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("challengeData")
  public ChallengeData getChallengeData() {
    return challengeData;
  }

  public void setChallengeData(ChallengeData challengeData) {
    this.challengeData = challengeData;
  }

  public PaymentInitationRequestResponse201 _links(Map _links) {
    this._links = _links;
    return this;
  }

  /**
   * Get _links
   * @return _links
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid


  @JsonProperty("_links")
  public Map getLinks() {
    return _links;
  }

  public void setLinks(Map _links) {
    this._links = _links;
  }

  public PaymentInitationRequestResponse201 psuMessage(String psuMessage) {
    this.psuMessage = psuMessage;
    return this;
  }

  /**
   * Get psuMessage
   * @return psuMessage
  **/
  @ApiModelProperty(value = "")

@Size(max=500)

  @JsonProperty("psuMessage")
  public String getPsuMessage() {
    return psuMessage;
  }

  public void setPsuMessage(String psuMessage) {
    this.psuMessage = psuMessage;
  }

  public PaymentInitationRequestResponse201 tppMessages(List<TppMessage201PaymentInitiation> tppMessages) {
    this.tppMessages = tppMessages;
    return this;
  }

  public PaymentInitationRequestResponse201 addTppMessagesItem(TppMessage201PaymentInitiation tppMessagesItem) {
    if (this.tppMessages == null) {
      this.tppMessages = new ArrayList<>();
    }
    this.tppMessages.add(tppMessagesItem);
    return this;
  }

  /**
   * Get tppMessages
   * @return tppMessages
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("tppMessages")
  public List<TppMessage201PaymentInitiation> getTppMessages() {
    return tppMessages;
  }

  public void setTppMessages(List<TppMessage201PaymentInitiation> tppMessages) {
    this.tppMessages = tppMessages;
  }

  public PaymentInitationRequestResponse201 scaStatus(ScaStatus scaStatus) {
    this.scaStatus = scaStatus;
    return this;
  }

  /**
   * Get scaStatus
   * @return scaStatus
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("scaStatus")
  public ScaStatus getScaStatus() {
    return scaStatus;
  }

  public void setScaStatus(ScaStatus scaStatus) {
    this.scaStatus = scaStatus;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
}    PaymentInitationRequestResponse201 paymentInitationRequestResponse201 = (PaymentInitationRequestResponse201) o;
    return Objects.equals(this.transactionStatus, paymentInitationRequestResponse201.transactionStatus) &&
    Objects.equals(this.paymentId, paymentInitationRequestResponse201.paymentId) &&
    Objects.equals(this.transactionFees, paymentInitationRequestResponse201.transactionFees) &&
    Objects.equals(this.currencyConversionFee, paymentInitationRequestResponse201.currencyConversionFee) &&
    Objects.equals(this.estimatedTotalAmount, paymentInitationRequestResponse201.estimatedTotalAmount) &&
    Objects.equals(this.estimatedInterbankSettlementAmount, paymentInitationRequestResponse201.estimatedInterbankSettlementAmount) &&
    Objects.equals(this.transactionFeeIndicator, paymentInitationRequestResponse201.transactionFeeIndicator) &&
    Objects.equals(this.scaMethods, paymentInitationRequestResponse201.scaMethods) &&
    Objects.equals(this.chosenScaMethod, paymentInitationRequestResponse201.chosenScaMethod) &&
    Objects.equals(this.challengeData, paymentInitationRequestResponse201.challengeData) &&
    Objects.equals(this._links, paymentInitationRequestResponse201._links) &&
    Objects.equals(this.psuMessage, paymentInitationRequestResponse201.psuMessage) &&
    Objects.equals(this.tppMessages, paymentInitationRequestResponse201.tppMessages) &&
    Objects.equals(this.scaStatus, paymentInitationRequestResponse201.scaStatus);
  }

  @Override
  public int hashCode() {
    return Objects.hash(transactionStatus, paymentId, transactionFees, currencyConversionFee, estimatedTotalAmount, estimatedInterbankSettlementAmount, transactionFeeIndicator, scaMethods, chosenScaMethod, challengeData, _links, psuMessage, tppMessages, scaStatus);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PaymentInitationRequestResponse201 {\n");

    sb.append("    transactionStatus: ").append(toIndentedString(transactionStatus)).append("\n");
    sb.append("    paymentId: ").append(toIndentedString(paymentId)).append("\n");
    sb.append("    transactionFees: ").append(toIndentedString(transactionFees)).append("\n");
    sb.append("    currencyConversionFee: ").append(toIndentedString(currencyConversionFee)).append("\n");
    sb.append("    estimatedTotalAmount: ").append(toIndentedString(estimatedTotalAmount)).append("\n");
    sb.append("    estimatedInterbankSettlementAmount: ").append(toIndentedString(estimatedInterbankSettlementAmount)).append("\n");
    sb.append("    transactionFeeIndicator: ").append(toIndentedString(transactionFeeIndicator)).append("\n");
    sb.append("    scaMethods: ").append(toIndentedString(scaMethods)).append("\n");
    sb.append("    chosenScaMethod: ").append(toIndentedString(chosenScaMethod)).append("\n");
    sb.append("    challengeData: ").append(toIndentedString(challengeData)).append("\n");
    sb.append("    _links: ").append(toIndentedString(_links)).append("\n");
    sb.append("    psuMessage: ").append(toIndentedString(psuMessage)).append("\n");
    sb.append("    tppMessages: ").append(toIndentedString(tppMessages)).append("\n");
    sb.append("    scaStatus: ").append(toIndentedString(scaStatus)).append("\n");
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

