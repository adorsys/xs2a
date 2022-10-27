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
import java.util.Objects;

/**
 * Body of the JSON response for a successful get status request for a consent.
 */
@Schema(description = "Body of the JSON response for a successful get status request for a consent.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-10-26T13:16:54.081225+03:00[Europe/Kiev]")


public class ConsentStatusResponse200   {
  @JsonProperty("consentStatus")
  private ConsentStatus consentStatus = null;

  @JsonProperty("psuMessage")
  private String psuMessage = null;

  public ConsentStatusResponse200 consentStatus(ConsentStatus consentStatus) {
    this.consentStatus = consentStatus;
    return this;
  }

    /**
     * Get consentStatus
     *
     * @return consentStatus
     **/
    @Schema(required = true, description = "")
    @JsonProperty("consentStatus")
    @NotNull

    @Valid
    public ConsentStatus getConsentStatus() {
        return consentStatus;
    }

  public void setConsentStatus(ConsentStatus consentStatus) {
    this.consentStatus = consentStatus;
  }

  public ConsentStatusResponse200 psuMessage(String psuMessage) {
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
    ConsentStatusResponse200 consentStatusResponse200 = (ConsentStatusResponse200) o;
    return Objects.equals(this.consentStatus, consentStatusResponse200.consentStatus) &&
        Objects.equals(this.psuMessage, consentStatusResponse200.psuMessage);
  }

  @Override
  public int hashCode() {
    return Objects.hash(consentStatus, psuMessage);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConsentStatusResponse200 {\n");

    sb.append("    consentStatus: ").append(toIndentedString(consentStatus)).append("\n");
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
