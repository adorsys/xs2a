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
import java.util.Objects;

/**
 * Body of the JSON response for a successful create signing basket request.
 */
@Schema(description = "Body of the JSON response for a successful create signing basket request.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-10-26T13:16:54.081225+03:00[Europe/Kiev]")


public class SigningBasketResponse201   {
  @JsonProperty("transactionStatus")
  private TransactionStatusSBS transactionStatus = null;

  @JsonProperty("basketId")
  private String basketId = null;

  @JsonProperty("scaMethods")
  private ScaMethods scaMethods = null;

  @JsonProperty("chosenScaMethod")
  private ChosenScaMethod chosenScaMethod = null;

  @JsonProperty("challengeData")
  private ChallengeData challengeData = null;

    @JsonProperty("_links")
    private LinksSigningBasket _links = null;

  @JsonProperty("psuMessage")
  private String psuMessage = null;

  @JsonProperty("tppMessages")
  @Valid
  private List<TppMessage2XX> tppMessages = null;

  public SigningBasketResponse201 transactionStatus(TransactionStatusSBS transactionStatus) {
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
    public TransactionStatusSBS getTransactionStatus() {
        return transactionStatus;
    }

  public void setTransactionStatus(TransactionStatusSBS transactionStatus) {
    this.transactionStatus = transactionStatus;
  }

  public SigningBasketResponse201 basketId(String basketId) {
    this.basketId = basketId;
    return this;
  }

    /**
     * Resource identification of the generated signing basket resource.
     *
     * @return basketId
     **/
    @Schema(example = "1234-basket-567", required = true, description = "Resource identification of the generated signing basket resource.")
    @JsonProperty("basketId")
    @NotNull

    public String getBasketId() {
        return basketId;
  }

  public void setBasketId(String basketId) {
    this.basketId = basketId;
  }

  public SigningBasketResponse201 scaMethods(ScaMethods scaMethods) {
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

  public SigningBasketResponse201 chosenScaMethod(ChosenScaMethod chosenScaMethod) {
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

  public SigningBasketResponse201 challengeData(ChallengeData challengeData) {
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

    public SigningBasketResponse201 _links(LinksSigningBasket _links) {
        this._links = _links;
        return this;
    }

    /**
     * Get _links
     *
     * @return _links
     **/
    @Schema(required = true, description = "")
    @JsonProperty("_links")
    @NotNull

    @Valid
    public LinksSigningBasket getLinks() {
        return _links;
    }

    public void setLinks(LinksSigningBasket _links) {
    this._links = _links;
  }

  public SigningBasketResponse201 psuMessage(String psuMessage) {
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

  public SigningBasketResponse201 tppMessages(List<TppMessage2XX> tppMessages) {
    this.tppMessages = tppMessages;
    return this;
  }

  public SigningBasketResponse201 addTppMessagesItem(TppMessage2XX tppMessagesItem) {
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
    }
    SigningBasketResponse201 signingBasketResponse201 = (SigningBasketResponse201) o;
    return Objects.equals(this.transactionStatus, signingBasketResponse201.transactionStatus) &&
        Objects.equals(this.basketId, signingBasketResponse201.basketId) &&
        Objects.equals(this.scaMethods, signingBasketResponse201.scaMethods) &&
        Objects.equals(this.chosenScaMethod, signingBasketResponse201.chosenScaMethod) &&
        Objects.equals(this.challengeData, signingBasketResponse201.challengeData) &&
        Objects.equals(this._links, signingBasketResponse201._links) &&
        Objects.equals(this.psuMessage, signingBasketResponse201.psuMessage) &&
        Objects.equals(this.tppMessages, signingBasketResponse201.tppMessages);
  }

  @Override
  public int hashCode() {
    return Objects.hash(transactionStatus, basketId, scaMethods, chosenScaMethod, challengeData, _links, psuMessage, tppMessages);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SigningBasketResponse201 {\n");

    sb.append("    transactionStatus: ").append(toIndentedString(transactionStatus)).append("\n");
    sb.append("    basketId: ").append(toIndentedString(basketId)).append("\n");
    sb.append("    scaMethods: ").append(toIndentedString(scaMethods)).append("\n");
    sb.append("    chosenScaMethod: ").append(toIndentedString(chosenScaMethod)).append("\n");
    sb.append("    challengeData: ").append(toIndentedString(challengeData)).append("\n");
    sb.append("    _links: ").append(toIndentedString(_links)).append("\n");
    sb.append("    psuMessage: ").append(toIndentedString(psuMessage)).append("\n");
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
