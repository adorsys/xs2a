/*
 * Copyright 2018-2023 adorsys GmbH & Co KG
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
 * Body of the JSON response for a Start SCA authorisation request.
 */
@Schema(description = "Body of the JSON response for a Start SCA authorisation request.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-10-26T13:16:54.081225+03:00[Europe/Kiev]")


public class StartScaprocessResponse   {
  @JsonProperty("scaStatus")
  private ScaStatus scaStatus = null;

  @JsonProperty("authorisationId")
  private String authorisationId = null;

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
  private List<TppMessageGeneric> tppMessages = null;

  public StartScaprocessResponse scaStatus(ScaStatus scaStatus) {
    this.scaStatus = scaStatus;
    return this;
  }

    /**
     * Get scaStatus
     *
     * @return scaStatus
     **/
    @Schema(required = true, description = "")
    @JsonProperty("scaStatus")
    @NotNull

    @Valid
    public ScaStatus getScaStatus() {
        return scaStatus;
    }

  public void setScaStatus(ScaStatus scaStatus) {
    this.scaStatus = scaStatus;
  }

  public StartScaprocessResponse authorisationId(String authorisationId) {
    this.authorisationId = authorisationId;
    return this;
  }

    /**
     * Resource identification of the related SCA.
     *
     * @return authorisationId
     **/
    @Schema(example = "123auth456", required = true, description = "Resource identification of the related SCA.")
    @JsonProperty("authorisationId")
    @NotNull

    public String getAuthorisationId() {
        return authorisationId;
  }

  public void setAuthorisationId(String authorisationId) {
    this.authorisationId = authorisationId;
  }

  public StartScaprocessResponse scaMethods(ScaMethods scaMethods) {
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

  public StartScaprocessResponse chosenScaMethod(ChosenScaMethod chosenScaMethod) {
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

  public StartScaprocessResponse challengeData(ChallengeData challengeData) {
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

    public StartScaprocessResponse _links(Map _links) {
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
  public Map getLinks() {
    return _links;
  }

  public void setLinks(Map _links) {
    this._links = _links;
  }

  public StartScaprocessResponse psuMessage(String psuMessage) {
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

  public StartScaprocessResponse tppMessages(List<TppMessageGeneric> tppMessages) {
    this.tppMessages = tppMessages;
    return this;
  }

  public StartScaprocessResponse addTppMessagesItem(TppMessageGeneric tppMessagesItem) {
    if (this.tppMessages == null) {
      this.tppMessages = new ArrayList<>();
    }
      this.tppMessages.add(tppMessagesItem);
      return this;
  }

    /**
     * Messages to the TPP on operational issues.
     *
     * @return tppMessages
     **/
    @Schema(description = "Messages to the TPP on operational issues.")
    @JsonProperty("tppMessages")
    @Valid
  public List<TppMessageGeneric> getTppMessages() {
    return tppMessages;
  }

  public void setTppMessages(List<TppMessageGeneric> tppMessages) {
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
    StartScaprocessResponse startScaprocessResponse = (StartScaprocessResponse) o;
    return Objects.equals(this.scaStatus, startScaprocessResponse.scaStatus) &&
        Objects.equals(this.authorisationId, startScaprocessResponse.authorisationId) &&
        Objects.equals(this.scaMethods, startScaprocessResponse.scaMethods) &&
        Objects.equals(this.chosenScaMethod, startScaprocessResponse.chosenScaMethod) &&
        Objects.equals(this.challengeData, startScaprocessResponse.challengeData) &&
        Objects.equals(this._links, startScaprocessResponse._links) &&
        Objects.equals(this.psuMessage, startScaprocessResponse.psuMessage) &&
        Objects.equals(this.tppMessages, startScaprocessResponse.tppMessages);
  }

  @Override
  public int hashCode() {
    return Objects.hash(scaStatus, authorisationId, scaMethods, chosenScaMethod, challengeData, _links, psuMessage, tppMessages);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class StartScaprocessResponse {\n");

    sb.append("    scaStatus: ").append(toIndentedString(scaStatus)).append("\n");
    sb.append("    authorisationId: ").append(toIndentedString(authorisationId)).append("\n");
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
