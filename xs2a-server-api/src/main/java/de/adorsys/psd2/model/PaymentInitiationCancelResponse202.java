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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Body of the response for a successful cancel payment request.
 */
@Schema(description = "Body of the response for a successful cancel payment request.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-10-26T13:16:54.081225+03:00[Europe/Kiev]")


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
  private List<TppMessageGeneric> tppMessages = null;

  @JsonProperty("scaStatus")
  private ScaStatus scaStatus = null;

  @JsonProperty("psuMessage")
  private String psuMessage = null;

  public PaymentInitiationCancelResponse202 transactionStatus(TransactionStatus transactionStatus) {
    this.transactionStatus = transactionStatus;
    return this;
  }

    /**
     * Get transactionStatus
     *
     * @return transactionStatus
     **/
    @Schema(required = true, description = "")
    @JsonProperty("transactionStatus")
    @NotNull

    @Valid
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
     *
     * @return scaMethods
     **/
    @Schema(description = "")
    @JsonProperty("scaMethods")

    @Valid
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
     *
     * @return chosenScaMethod
     **/
    @Schema(description = "")
    @JsonProperty("chosenScaMethod")

    @Valid
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
     *
     * @return challengeData
     **/
    @Schema(description = "")
    @JsonProperty("challengeData")

    @Valid
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
     *
     * @return _links
     **/
    @Schema(description = "")
    @JsonProperty("_links")

    @Valid
    public Map getLinks() {
        return _links;
    }

    public void setLinks(Map _links) {
    this._links = _links;
  }

  public PaymentInitiationCancelResponse202 tppMessages(List<TppMessageGeneric> tppMessages) {
    this.tppMessages = tppMessages;
    return this;
  }

  public PaymentInitiationCancelResponse202 addTppMessagesItem(TppMessageGeneric tppMessagesItem) {
    if (this.tppMessages == null) {
      this.tppMessages = new ArrayList<>();
    }
      this.tppMessages.add(tppMessagesItem);
      return this;
  }

    /**
     * Get tppMessages
     *
     * @return tppMessages
     **/
    @Schema(description = "")
    @JsonProperty("tppMessages")
    @Valid
    public List<TppMessageGeneric> getTppMessages() {
    return tppMessages;
  }

  public void setTppMessages(List<TppMessageGeneric> tppMessages) {
    this.tppMessages = tppMessages;
  }

  public PaymentInitiationCancelResponse202 scaStatus(ScaStatus scaStatus) {
      this.scaStatus = scaStatus;
      return this;
  }

    /**
     * Get scaStatus
     *
     * @return scaStatus
     **/
    @Schema(description = "")
    @JsonProperty("scaStatus")

    @Valid
    public ScaStatus getScaStatus() {
    return scaStatus;
  }

  public void setScaStatus(ScaStatus scaStatus) {
    this.scaStatus = scaStatus;
  }

  public PaymentInitiationCancelResponse202 psuMessage(String psuMessage) {
      this.psuMessage = psuMessage;
      return this;
  }

    /**
     * Text to be displayed to the PSU.
     *
     * @return psuMessage
     **/
    @Schema(description = "Text to be displayed to the PSU.")
    @JsonProperty("psuMessage")

@Size(max=500)   public String getPsuMessage() {
    return psuMessage;
  }

  public void setPsuMessage(String psuMessage) {
    this.psuMessage = psuMessage;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PaymentInitiationCancelResponse202 paymentInitiationCancelResponse202 = (PaymentInitiationCancelResponse202) o;
    return Objects.equals(this.transactionStatus, paymentInitiationCancelResponse202.transactionStatus) &&
        Objects.equals(this.scaMethods, paymentInitiationCancelResponse202.scaMethods) &&
        Objects.equals(this.chosenScaMethod, paymentInitiationCancelResponse202.chosenScaMethod) &&
        Objects.equals(this.challengeData, paymentInitiationCancelResponse202.challengeData) &&
        Objects.equals(this._links, paymentInitiationCancelResponse202._links) &&
        Objects.equals(this.tppMessages, paymentInitiationCancelResponse202.tppMessages) &&
        Objects.equals(this.scaStatus, paymentInitiationCancelResponse202.scaStatus) &&
        Objects.equals(this.psuMessage, paymentInitiationCancelResponse202.psuMessage);
  }

  @Override
  public int hashCode() {
    return Objects.hash(transactionStatus, scaMethods, chosenScaMethod, challengeData, _links, tppMessages, scaStatus, psuMessage);
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
    sb.append("    scaStatus: ").append(toIndentedString(scaStatus)).append("\n");
    sb.append("    psuMessage: ").append(toIndentedString(psuMessage)).append("\n");
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
