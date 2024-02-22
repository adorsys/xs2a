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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Body of the JSON response for a Start single SCA authorisation request.
 */
@Schema(description = "Body of the JSON response for a Start single SCA authorisation request.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-05-06T13:00:06.283258+03:00[Europe/Kiev]")


public class ConsentsConfirmationOfFundsResponse {
  @JsonProperty("consentStatus")
  private ConsentStatus consentStatus = null;

  @JsonProperty("consentId")
  private String consentId = null;

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

  @JsonProperty("scaStatus")
  private ScaStatus scaStatus = null;

  @JsonProperty("tppMessage")
  @Valid
  private List<TppMessageGeneric> tppMessage = null;

  public ConsentsConfirmationOfFundsResponse consentStatus(ConsentStatus consentStatus) {
    this.consentStatus = consentStatus;
    return this;
  }

  /**
   * Get consentStatus
   * @return consentStatus
   **/
  @Schema(required = true, description = "")
      @NotNull

    @Valid
    public ConsentStatus getConsentStatus() {
    return consentStatus;
  }

  public void setConsentStatus(ConsentStatus consentStatus) {
    this.consentStatus = consentStatus;
  }

  public ConsentsConfirmationOfFundsResponse consentId(String consentId) {
    this.consentId = consentId;
    return this;
  }

  /**
   * ID of the corresponding consent object as returned by an Account Information Consent Request.
   * @return consentId
   **/
  @Schema(required = true, description = "ID of the corresponding consent object as returned by an Account Information Consent Request. ")
      @NotNull

    public String getConsentId() {
    return consentId;
  }

  public void setConsentId(String consentId) {
    this.consentId = consentId;
  }

  public ConsentsConfirmationOfFundsResponse scaMethods(ScaMethods scaMethods) {
    this.scaMethods = scaMethods;
    return this;
  }

  /**
   * Get scaMethods
   * @return scaMethods
   **/
  @Schema(description = "")

    @Valid
    public ScaMethods getScaMethods() {
    return scaMethods;
  }

  public void setScaMethods(ScaMethods scaMethods) {
    this.scaMethods = scaMethods;
  }

  public ConsentsConfirmationOfFundsResponse chosenScaMethod(ChosenScaMethod chosenScaMethod) {
    this.chosenScaMethod = chosenScaMethod;
    return this;
  }

  /**
   * Get chosenScaMethod
   * @return chosenScaMethod
   **/
  @Schema(description = "")

    @Valid
    public ChosenScaMethod getChosenScaMethod() {
    return chosenScaMethod;
  }

  public void setChosenScaMethod(ChosenScaMethod chosenScaMethod) {
    this.chosenScaMethod = chosenScaMethod;
  }

  public ConsentsConfirmationOfFundsResponse challengeData(ChallengeData challengeData) {
    this.challengeData = challengeData;
    return this;
  }

  /**
   * Get challengeData
   * @return challengeData
   **/
  @Schema(description = "")

    @Valid
    public ChallengeData getChallengeData() {
    return challengeData;
  }

  public void setChallengeData(ChallengeData challengeData) {
    this.challengeData = challengeData;
  }

  public ConsentsConfirmationOfFundsResponse _links(LinksStartScaProcessSingleSca _links) {
    this._links = _links;
    return this;
  }

  /**
   * Get _links
   * @return _links
   **/
  @Schema(required = true, description = "")
      @NotNull

    @Valid
    public Map getLinks() {
    return _links;
  }

  public void setLinks(Map _links) {
    this._links = _links;
  }

  public ConsentsConfirmationOfFundsResponse psuMessage(String psuMessage) {
    this.psuMessage = psuMessage;
    return this;
  }

  /**
   * Text to be displayed to the PSU
   * @return psuMessage
   **/
  @Schema(description = "Text to be displayed to the PSU")

  @Size(max=512)   public String getPsuMessage() {
    return psuMessage;
  }

  public void setPsuMessage(String psuMessage) {
    this.psuMessage = psuMessage;
  }

  public ConsentsConfirmationOfFundsResponse scaStatus(ScaStatus scaStatus) {
    this.scaStatus = scaStatus;
    return this;
  }

  /**
   * Get scaStatus
   * @return scaStatus
   **/
  @Schema(description = "")

    @Valid
    public ScaStatus getScaStatus() {
    return scaStatus;
  }

  public void setScaStatus(ScaStatus scaStatus) {
    this.scaStatus = scaStatus;
  }

  public ConsentsConfirmationOfFundsResponse tppMessage(List<TppMessageGeneric> tppMessage) {
    this.tppMessage = tppMessage;
    return this;
  }

  public ConsentsConfirmationOfFundsResponse addTppMessageItem(TppMessageGeneric tppMessageItem) {
    if (this.tppMessage == null) {
      this.tppMessage = new ArrayList<>();
    }
    this.tppMessage.add(tppMessageItem);
    return this;
  }

  /**
   * Messages to the TPP on operational issues.
   * @return tppMessage
   **/
  @Schema(description = "Messages to the TPP on operational issues.")
      @Valid
    public List<TppMessageGeneric> getTppMessage() {
    return tppMessage;
  }

  public void setTppMessage(List<TppMessageGeneric> tppMessage) {
    this.tppMessage = tppMessage;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConsentsConfirmationOfFundsResponse consentsConfirmationOfFundsResponse = (ConsentsConfirmationOfFundsResponse) o;
    return Objects.equals(this.consentStatus, consentsConfirmationOfFundsResponse.consentStatus) &&
        Objects.equals(this.consentId, consentsConfirmationOfFundsResponse.consentId) &&
        Objects.equals(this.scaMethods, consentsConfirmationOfFundsResponse.scaMethods) &&
        Objects.equals(this.chosenScaMethod, consentsConfirmationOfFundsResponse.chosenScaMethod) &&
        Objects.equals(this.challengeData, consentsConfirmationOfFundsResponse.challengeData) &&
        Objects.equals(this._links, consentsConfirmationOfFundsResponse._links) &&
        Objects.equals(this.psuMessage, consentsConfirmationOfFundsResponse.psuMessage) &&
        Objects.equals(this.scaStatus, consentsConfirmationOfFundsResponse.scaStatus) &&
        Objects.equals(this.tppMessage, consentsConfirmationOfFundsResponse.tppMessage);
  }

  @Override
  public int hashCode() {
    return Objects.hash(consentStatus, consentId, scaMethods, chosenScaMethod, challengeData, _links, psuMessage, scaStatus, tppMessage);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConsentsConfirmationOfFundsResponse {\n");

    sb.append("    consentStatus: ").append(toIndentedString(consentStatus)).append("\n");
    sb.append("    consentId: ").append(toIndentedString(consentId)).append("\n");
    sb.append("    scaMethods: ").append(toIndentedString(scaMethods)).append("\n");
    sb.append("    chosenScaMethod: ").append(toIndentedString(chosenScaMethod)).append("\n");
    sb.append("    challengeData: ").append(toIndentedString(challengeData)).append("\n");
    sb.append("    _links: ").append(toIndentedString(_links)).append("\n");
    sb.append("    psuMessage: ").append(toIndentedString(psuMessage)).append("\n");
    sb.append("    scaStatus: ").append(toIndentedString(scaStatus)).append("\n");
    sb.append("    tppMessage: ").append(toIndentedString(tppMessage)).append("\n");
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
