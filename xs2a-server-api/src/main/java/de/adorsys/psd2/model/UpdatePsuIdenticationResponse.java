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
import java.util.Map;
import java.util.Objects;

/**
 * Body of the JSON response for a successful update PSU identification request.
 */
@Schema(description = "Body of the JSON response for a successful update PSU identification request.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-10-26T13:16:54.081225+03:00[Europe/Kiev]")


public class UpdatePsuIdenticationResponse {
    @JsonProperty("transactionFees")
    private Amount transactionFees = null;

    @JsonProperty("currencyConversionFees")
    private Amount currencyConversionFees = null;

    @JsonProperty("estimatedTotalAmount")
    private Amount estimatedTotalAmount = null;

    @JsonProperty("estimatedInterbankSettlementAmount")
  private Amount estimatedInterbankSettlementAmount = null;

  @JsonProperty("scaMethods")
  private ScaMethods scaMethods = null;

  @JsonProperty("_links")
  private Map _links = null;

  @JsonProperty("scaStatus")
  private ScaStatus scaStatus = null;

  @JsonProperty("psuMessage")
  private String psuMessage = null;

  public UpdatePsuIdenticationResponse transactionFees(Amount transactionFees) {
    this.transactionFees = transactionFees;
    return this;
  }

    /**
     * Get transactionFees
     *
     * @return transactionFees
     **/
    @Schema(description = "")
    @JsonProperty("transactionFees")

    @Valid
    public Amount getTransactionFees() {
        return transactionFees;
    }

  public void setTransactionFees(Amount transactionFees) {
    this.transactionFees = transactionFees;
  }

  public UpdatePsuIdenticationResponse currencyConversionFees(Amount currencyConversionFees) {
    this.currencyConversionFees = currencyConversionFees;
    return this;
  }

    /**
     * Get currencyConversionFees
     *
     * @return currencyConversionFees
     **/
    @Schema(description = "")
    @JsonProperty("currencyConversionFees")

    @Valid
    public Amount getCurrencyConversionFees() {
        return currencyConversionFees;
  }

  public void setCurrencyConversionFees(Amount currencyConversionFees) {
    this.currencyConversionFees = currencyConversionFees;
  }

  public UpdatePsuIdenticationResponse estimatedTotalAmount(Amount estimatedTotalAmount) {
    this.estimatedTotalAmount = estimatedTotalAmount;
      return this;
  }

    /**
     * Get estimatedTotalAmount
     *
     * @return estimatedTotalAmount
     **/
    @Schema(description = "")
    @JsonProperty("estimatedTotalAmount")

    @Valid
    public Amount getEstimatedTotalAmount() {
        return estimatedTotalAmount;
  }

  public void setEstimatedTotalAmount(Amount estimatedTotalAmount) {
    this.estimatedTotalAmount = estimatedTotalAmount;
  }

  public UpdatePsuIdenticationResponse estimatedInterbankSettlementAmount(Amount estimatedInterbankSettlementAmount) {
    this.estimatedInterbankSettlementAmount = estimatedInterbankSettlementAmount;
      return this;
  }

    /**
     * Get estimatedInterbankSettlementAmount
     *
     * @return estimatedInterbankSettlementAmount
     **/
    @Schema(description = "")
    @JsonProperty("estimatedInterbankSettlementAmount")

    @Valid
    public Amount getEstimatedInterbankSettlementAmount() {
        return estimatedInterbankSettlementAmount;
  }

  public void setEstimatedInterbankSettlementAmount(Amount estimatedInterbankSettlementAmount) {
    this.estimatedInterbankSettlementAmount = estimatedInterbankSettlementAmount;
  }

  public UpdatePsuIdenticationResponse scaMethods(ScaMethods scaMethods) {
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

    public UpdatePsuIdenticationResponse _links(Map _links) {
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

  public UpdatePsuIdenticationResponse scaStatus(ScaStatus scaStatus) {
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

  public UpdatePsuIdenticationResponse psuMessage(String psuMessage) {
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
    UpdatePsuIdenticationResponse updatePsuIdenticationResponse = (UpdatePsuIdenticationResponse) o;
    return Objects.equals(this.transactionFees, updatePsuIdenticationResponse.transactionFees) &&
        Objects.equals(this.currencyConversionFees, updatePsuIdenticationResponse.currencyConversionFees) &&
        Objects.equals(this.estimatedTotalAmount, updatePsuIdenticationResponse.estimatedTotalAmount) &&
        Objects.equals(this.estimatedInterbankSettlementAmount, updatePsuIdenticationResponse.estimatedInterbankSettlementAmount) &&
        Objects.equals(this.scaMethods, updatePsuIdenticationResponse.scaMethods) &&
        Objects.equals(this._links, updatePsuIdenticationResponse._links) &&
        Objects.equals(this.scaStatus, updatePsuIdenticationResponse.scaStatus) &&
        Objects.equals(this.psuMessage, updatePsuIdenticationResponse.psuMessage);
  }

  @Override
  public int hashCode() {
    return Objects.hash(transactionFees, currencyConversionFees, estimatedTotalAmount, estimatedInterbankSettlementAmount, scaMethods, _links, scaStatus, psuMessage);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UpdatePsuIdenticationResponse {\n");

    sb.append("    transactionFees: ").append(toIndentedString(transactionFees)).append("\n");
    sb.append("    currencyConversionFees: ").append(toIndentedString(currencyConversionFees)).append("\n");
    sb.append("    estimatedTotalAmount: ").append(toIndentedString(estimatedTotalAmount)).append("\n");
    sb.append("    estimatedInterbankSettlementAmount: ").append(toIndentedString(estimatedInterbankSettlementAmount)).append("\n");
    sb.append("    scaMethods: ").append(toIndentedString(scaMethods)).append("\n");
    sb.append("    _links: ").append(toIndentedString(_links)).append("\n");
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
