package de.adorsys.psd2.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import de.adorsys.psd2.model.ChallengeData;
import de.adorsys.psd2.model.ChosenScaMethod;
import de.adorsys.psd2.model.ScaMethods;
import de.adorsys.psd2.model.TppMessage2XX;
import de.adorsys.psd2.model.TransactionStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * Body of the response for a successful cancel payment request.
 */
@ApiModel(description = "Body of the response for a successful cancel payment request.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-11-12T17:35:11.808068+02:00[Europe/Kiev]")

public class PaymentInitiationCancelResponse202   {
  @JsonProperty("transactionStatus")
  private TransactionStatus transactionStatus = null;

  @JsonProperty("scaMethods")
  private ScaMethods scaMethods = null;

  @JsonProperty("chosenScaMethod")
  private ChosenScaMethod chosenScaMethod = null;

  @JsonProperty("challengeData")
  private ChallengeData challengeData = null;

  @JsonProperty("_links")
  private Map _links = null;

  @JsonProperty("tppMessages")
  @Valid
  private List<TppMessage2XX> tppMessages = null;

  public PaymentInitiationCancelResponse202 transactionStatus(TransactionStatus transactionStatus) {
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

  public PaymentInitiationCancelResponse202 scaMethods(ScaMethods scaMethods) {
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

  public PaymentInitiationCancelResponse202 chosenScaMethod(ChosenScaMethod chosenScaMethod) {
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

  public PaymentInitiationCancelResponse202 challengeData(ChallengeData challengeData) {
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

  public PaymentInitiationCancelResponse202 _links(Map _links) {
    this._links = _links;
    return this;
  }

  /**
   * Get _links
   * @return _links
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("_links")
  public Map getLinks() {
    return _links;
  }

  public void setLinks(Map _links) {
    this._links = _links;
  }

  public PaymentInitiationCancelResponse202 tppMessages(List<TppMessage2XX> tppMessages) {
    this.tppMessages = tppMessages;
    return this;
  }

  public PaymentInitiationCancelResponse202 addTppMessagesItem(TppMessage2XX tppMessagesItem) {
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
  public List<TppMessage2XX> getTppMessages() {
    return tppMessages;
  }

  public void setTppMessages(List<TppMessage2XX> tppMessages) {
    this.tppMessages = tppMessages;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
}    PaymentInitiationCancelResponse202 paymentInitiationCancelResponse202 = (PaymentInitiationCancelResponse202) o;
    return Objects.equals(this.transactionStatus, paymentInitiationCancelResponse202.transactionStatus) &&
    Objects.equals(this.scaMethods, paymentInitiationCancelResponse202.scaMethods) &&
    Objects.equals(this.chosenScaMethod, paymentInitiationCancelResponse202.chosenScaMethod) &&
    Objects.equals(this.challengeData, paymentInitiationCancelResponse202.challengeData) &&
    Objects.equals(this._links, paymentInitiationCancelResponse202._links) &&
    Objects.equals(this.tppMessages, paymentInitiationCancelResponse202.tppMessages);
  }

  @Override
  public int hashCode() {
    return Objects.hash(transactionStatus, scaMethods, chosenScaMethod, challengeData, _links, tppMessages);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PaymentInitiationCancelResponse202 {\n");

    sb.append("    transactionStatus: ").append(toIndentedString(transactionStatus)).append("\n");
    sb.append("    scaMethods: ").append(toIndentedString(scaMethods)).append("\n");
    sb.append("    chosenScaMethod: ").append(toIndentedString(chosenScaMethod)).append("\n");
    sb.append("    challengeData: ").append(toIndentedString(challengeData)).append("\n");
    sb.append("    _links: ").append(toIndentedString(_links)).append("\n");
    sb.append("    tppMessages: ").append(toIndentedString(tppMessages)).append("\n");
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

