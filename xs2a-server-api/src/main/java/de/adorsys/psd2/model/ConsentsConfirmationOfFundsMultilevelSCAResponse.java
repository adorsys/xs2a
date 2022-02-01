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
 * Body of the JSON response for a Start Multilevel SCA authorisation request.
 */
@ApiModel(description = "Body of the JSON response for a Start Multilevel SCA authorisation request.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-07-01T15:25:06.394043+03:00[Europe/Kiev]")

public class ConsentsConfirmationOfFundsMultilevelSCAResponse   {
  @JsonProperty("consentStatus")
  private ConsentStatus consentStatus = null;

  @JsonProperty("consentId")
  private String consentId = null;

  @JsonProperty("_links")
  private Map _links = null;

  @JsonProperty("psuMessage")
  private String psuMessage = null;

  @JsonProperty("tppMessages")
  @Valid
  private List<TppMessage2XX> tppMessages = null;

  public ConsentsConfirmationOfFundsMultilevelSCAResponse consentStatus(ConsentStatus consentStatus) {
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

  public ConsentsConfirmationOfFundsMultilevelSCAResponse consentId(String consentId) {
    this.consentId = consentId;
    return this;
  }

  /**
   * Get consentId
   * @return consentId
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull



  @JsonProperty("consentId")
  public String getConsentId() {
    return consentId;
  }

  public void setConsentId(String consentId) {
    this.consentId = consentId;
  }

  public ConsentsConfirmationOfFundsMultilevelSCAResponse _links(Map _links) {
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

  public ConsentsConfirmationOfFundsMultilevelSCAResponse psuMessage(String psuMessage) {
    this.psuMessage = psuMessage;
    return this;
  }

  /**
   * Get psuMessage
   * @return psuMessage
  **/
  @ApiModelProperty(value = "")

@Size(max=512)

  @JsonProperty("psuMessage")
  public String getPsuMessage() {
    return psuMessage;
  }

  public void setPsuMessage(String psuMessage) {
    this.psuMessage = psuMessage;
  }

  public ConsentsConfirmationOfFundsMultilevelSCAResponse tppMessages(List<TppMessage2XX> tppMessages) {
    this.tppMessages = tppMessages;
    return this;
  }

  public ConsentsConfirmationOfFundsMultilevelSCAResponse addTppMessagesItem(TppMessage2XX tppMessagesItem) {
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
    }
    ConsentsConfirmationOfFundsMultilevelSCAResponse consentsConfirmationOfFundsMultilevelSCAResponse = (ConsentsConfirmationOfFundsMultilevelSCAResponse) o;
    return Objects.equals(this.consentStatus, consentsConfirmationOfFundsMultilevelSCAResponse.consentStatus) &&
        Objects.equals(this.consentId, consentsConfirmationOfFundsMultilevelSCAResponse.consentId) &&
        Objects.equals(this._links, consentsConfirmationOfFundsMultilevelSCAResponse._links) &&
        Objects.equals(this.psuMessage, consentsConfirmationOfFundsMultilevelSCAResponse.psuMessage) &&
        Objects.equals(this.tppMessages, consentsConfirmationOfFundsMultilevelSCAResponse.tppMessages);
  }

  @Override
  public int hashCode() {
    return Objects.hash(consentStatus, consentId, _links, psuMessage, tppMessages);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConsentsConfirmationOfFundsMultilevelSCAResponse {\n");

    sb.append("    consentStatus: ").append(toIndentedString(consentStatus)).append("\n");
    sb.append("    consentId: ").append(toIndentedString(consentId)).append("\n");
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

