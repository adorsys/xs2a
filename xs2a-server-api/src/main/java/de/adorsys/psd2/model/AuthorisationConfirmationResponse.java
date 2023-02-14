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
import java.util.Map;
import java.util.Objects;

/**
 * Body of the JSON response for an authorisation confirmation request.
 */
@Schema(description = "Body of the JSON response for an authorisation confirmation request.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-10-26T13:16:54.081225+03:00[Europe/Kiev]")


public class AuthorisationConfirmationResponse {
    @JsonProperty("scaStatus")
    private ScaStatusAuthorisationConfirmation scaStatus = null;

    @JsonProperty("_links")
    private Map _links = null;

    @JsonProperty("psuMessage")
    private String psuMessage = null;

    public AuthorisationConfirmationResponse scaStatus(ScaStatusAuthorisationConfirmation scaStatus) {
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
    public ScaStatusAuthorisationConfirmation getScaStatus() {
        return scaStatus;
    }

  public void setScaStatus(ScaStatusAuthorisationConfirmation scaStatus) {
    this.scaStatus = scaStatus;
  }

    public AuthorisationConfirmationResponse _links(Map _links) {
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

  public AuthorisationConfirmationResponse psuMessage(String psuMessage) {
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

    @Size(max = 500)
    public String getPsuMessage() {
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
    AuthorisationConfirmationResponse authorisationConfirmationResponse = (AuthorisationConfirmationResponse) o;
    return Objects.equals(this.scaStatus, authorisationConfirmationResponse.scaStatus) &&
        Objects.equals(this._links, authorisationConfirmationResponse._links) &&
        Objects.equals(this.psuMessage, authorisationConfirmationResponse.psuMessage);
  }

  @Override
  public int hashCode() {
    return Objects.hash(scaStatus, _links, psuMessage);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AuthorisationConfirmationResponse {\n");

    sb.append("    scaStatus: ").append(toIndentedString(scaStatus)).append("\n");
    sb.append("    _links: ").append(toIndentedString(_links)).append("\n");
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
